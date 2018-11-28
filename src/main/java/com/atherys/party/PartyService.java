package com.atherys.party;

import com.atherys.core.utils.Question;
import com.atherys.core.utils.UserUtils;
import com.atherys.party.data.PartyData;
import com.atherys.party.database.PartyRepository;
import org.apache.commons.lang3.RandomUtils;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * The primary class responsible for tracking all parties and their members. Is also responsible for
 * saving/loading parties and their members to/from the database.
 */
public final class PartyService {

    private static final PartyService instance = new PartyService();

    public static PartyService getInstance() {
        return instance;
    }

    private PartyRepository repository = PartyRepository.getInstance();

    public void loadAll() {
        repository.cacheAll();
    }

    public void saveAll() {
        repository.flushCache();
    }

    public Optional<Party> getParty(UUID partyUUID) {
        return repository.findById(partyUUID);
    }

    public void removeParty(Party party) {
        getPartyMembers(party).forEach(this::removeUserFromParty);
        repository.deleteOne(party);
    }

    public Party createParty(User leader, User... members) {
        Party party = new Party(leader.getUniqueId(), new HashSet<>());
        for (User member : members) {
            party.addMember(member.getUniqueId());
        }

        repository.saveOne(party);

        return party;
    }

    public <T extends User> Optional<Party> getUserParty(T user) {
        Optional<PartyData> partyData = user.get(PartyData.class);
        if (partyData.isPresent()) return partyData.get().getParty();
        else return Optional.empty();
    }

    public <T extends User> void setUserParty(T user, Party party) {
        user.offer(new PartyData(party.getUniqueId()));
        party.getMembers().add(user.getUniqueId());
    }

    public <T extends User> void removeUserFromParty(T user) {
        getUserParty(user).ifPresent(party -> {
            party.removeMember(user.getUniqueId());
            user.remove(PartyData.class);

            if ( party.getMembers().size() <= 0 ) removeParty(party);
        });
    }

    public void setRandomPartyMemberAsLeader(Party party) {
        UUID newLeader = (UUID) party.getMembers().toArray()[RandomUtils.nextInt(0, party.getMembers().size() - 1)];
        party.setLeader(newLeader);
    }

    public void removeUserFromPartyByUUID(UUID uuid, Party party) {
        party.removeMember(uuid);

        // If only 1 member is left in the party, remove it
        if (party.getMembers().size() <= 1) {
            removeParty(party);
            return;
        }

        if (isUserPartyLeader(uuid, party)) {
            setRandomPartyMemberAsLeader(party);
        }
    }

    public boolean isUserPartyLeader(User user, Party party) {
        return party.getLeader().equals(user.getUniqueId());
    }

    public boolean isUserPartyLeader(UUID uuid, Party party) {
        return party.getLeader().equals(uuid);
    }

    /**
     * Retrieves all members of the party as {@link User} objects. If a uuid does not correspond to a User, it will be
     * pruned from the member list.
     *
     * @param party the party to get the members of
     * @return A collection containing all users who are members of the party
     */
    public Collection<User> getPartyMembersAndPrune(Party party) {
        Set<UUID> members = party.getMembers();
        Set<User> userMembers = new HashSet<>(members.size());

        for (UUID uuid : members) {
            Optional<? extends User> user = UserUtils.getUser(uuid);
            if (user.isPresent()) {
                userMembers.add(user.get());
            } else {
                removeUserFromPartyByUUID(uuid, party);
            }
        }

        return userMembers;
    }

    /**
     * Same as {@link #getPartyMembersAndPrune(Party)}, but without pruning.
     *
     * @param party the party whose members are to be retrieved
     * @return A collection of users who are members of the party
     */
    public Collection<User> getPartyMembers(Party party) {
        Set<UUID> members = party.getMembers();
        Set<User> userMembers = new HashSet<>(members.size());

        for (UUID uuid : members) {
            UserUtils.getUser(uuid).ifPresent(userMembers::add);
        }

        return userMembers;
    }

    /**
     * Sends an info message to all members of the given party. Uses {@link Party#getMembers()}
     *
     * @param party The party to whose members the message will be sent.
     * @param msg   The message. Will later be wrapped in a {@link Text} object.
     */
    public void sendInfoToParty(Party party, Object... msg) {
        for (User user : getPartyMembersAndPrune(party)) {
            PartyMsg.info(user, msg);
        }
    }

    /**
     * Sends an error message to all members of the given party. Uses {@link Party#getMembers()}
     *
     * @param party The party to whose members the message will be sent.
     * @param msg   The message. Will later be wrapped in a {@link Text} object.
     */
    public void sendErrorToParty(Party party, Object... msg) {
        for (User user : getPartyMembersAndPrune(party)) {
            PartyMsg.error(user, msg);
        }
    }

    /**
     * Retrieve information in Text form for the user's party.
     *
     * @param user the user whose party is to be looked at
     * @return A Text object with the required information
     */
    public Text printUserParty(User user) {
        Optional<Party> party = PartyService.getInstance().getUserParty(user);
        Text result;

        if (party.isPresent()) {
            Text.Builder partyMembers = Text.builder();

            getPartyMembersAndPrune(party.get()).forEach(partyMember -> {
                if (isUserPartyLeader(user, party.get())) {// is leader?
                    partyMembers.append(Text.of(TextColors.RED, partyMember.getName(), "; "));
                } else {
                    partyMembers.append(Text.of(TextColors.DARK_AQUA, partyMember.getName(), "; "));
                }
            });

            result = PartyMsg.formatInfo("Party Members: ", partyMembers.build());
        } else {
            result = PartyMsg.formatError("You are not currently in a party with anyone else.");
        }

        return result;
    }

    /**
     * Checks if the provided users are in the same party.
     * If both users are in a party, and both parties share the same UUID (i.e. they are the same ), this returns true.
     * Under any other circumstances, including if neither user is in a party, this will return false.
     *
     * @param user1 The first user
     * @param user2 The second user
     */
    public <T extends User> boolean areUsersInSameParty(User user1, User user2) {
        Optional<Party> party1 = getUserParty(user1);
        Optional<Party> party2 = getUserParty(user2);
        return party1.isPresent() && party2.isPresent() && party1.get().equals(party2.get());
    }

    /**
     * Disband the party which the provided User is part of, and is the leader of.
     *
     * @param disbander The disbanding User
     */
    public void disbandParty(User disbander) {
        Optional<Party> userParty = getUserParty(disbander);

        // If the user is not in a party, error
        if (!userParty.isPresent()) {
            PartyMsg.error(disbander, "You are not in a party!");
        } else {
            // If the disbanding user is not the party leader, error
            if (!isUserPartyLeader(disbander, userParty.get())) {
                PartyMsg.error(disbander, "You are not the leader of this party.");
            } else {
                sendErrorToParty(userParty.get(), "Your party has been disbanded.");
                removeParty(userParty.get());
            }
        }
    }

    /**
     * When a user invites another user to their party. If the inviter is not party of a party, a new one will be created.
     *
     * @param inviter The inviting user
     * @param invitee the invited user
     */
    public void inviteToParty(User inviter, User invitee) {

        if (inviter.getUniqueId().equals(invitee.getUniqueId())) {
            PartyMsg.error(inviter, "You can't invite yourself!");
        }

        Optional<Party> inviterParty = getUserParty(inviter);
        Optional<Party> inviteeParty = getUserParty(invitee);

        Party party;

        // If neither the inviter nor the invitee are already in a party, create a new one
        if (!inviterParty.isPresent() && !inviteeParty.isPresent()) {
            party = createParty(inviter, invitee);
            invite(inviter, invitee, party);
        }

        // If the inviter is in a party, check if they are a leader ( can invite new members )
        if (inviterParty.isPresent()) {

            party = inviterParty.get();

            // If the inviter is the party leader, invite the invitee
            if (isUserPartyLeader(inviter, party)) {
                invite(inviter, invitee, party);
            } else {
                PartyMsg.error(inviter, "You are not the party leader!");
            }
        }

        // If the invitee is in a party, send error
        if (inviteeParty.isPresent()) {
            PartyMsg.error(inviter, "That player is already in another party!");
        }
    }

    private void invite(User inviter, User invitee, Party party) {
        Question question = Question.of(Text.of(inviter.getName(), " has invited you to their party."))
                .addAnswer(Question.Answer.of(Text.of("Accept"), playerInvitee -> {
                    setUserParty(invitee, party);

                    PartyMsg.info(playerInvitee, "You have accepted ", inviter.getName(), "'s invite");
                    sendInfoToParty(party, playerInvitee.getName(), " has joined the party!");
                }))
                .addAnswer(Question.Answer.of(Text.of("Reject"), playerInvitee -> {
                    PartyMsg.error(playerInvitee, "You have rejected ", inviter.getName(), "'s invite");
                }))
                .build();

        question.pollChat((Player) invitee);

        sendInfoToParty(party, invitee.getName(), " has been invited to the party.");
    }

    /**
     * Kicks a user from a party by another user. If the kicker is not the leader, this will fail.
     *
     * @param kicker the user doing the kicking
     * @param kickee The user being kicked
     */
    public void kickFromParty(User kicker, User kickee) {
        // if the kicker is trying to kick themselves, trigger leaveParty instead
        if (kicker.getUniqueId().equals(kickee.getUniqueId())) {
            leaveParty(kicker);
            return;
        }

        Optional<Party> kickerParty = getUserParty(kicker);
        Optional<Party> kickeeParty = getUserParty(kickee);

        // if the kicker does not have a party, error
        if (!kickerParty.isPresent()) {
            PartyMsg.error(kicker, "You are not in a party!");
        } else {
            // if the kickee is not in a party, or it is not the same party as the kicker, error
            if (!kickeeParty.isPresent() || kickerParty.get().equals(kickeeParty.get())) {
                PartyMsg.error(kicker, "That user is not in your party!");
                return;
            }

            // if the kicker is not the leader of the party, error
            if (!isUserPartyLeader(kicker, kickerParty.get())) {
                PartyMsg.error(kicker, "You are not the leader of this party.");
                return;
            }

            removeUserFromParty(kickee);
            sendErrorToParty(kickerParty.get(), kickee.getName(), " has been kicked from the party by ", kicker.getName());
            PartyMsg.error(kickee, "You have been kicked from the party");
        }
    }

    /**
     * When a user leaves a party
     *
     * @param leaver the user leaving their party
     */
    public void leaveParty(User leaver) {

        Optional<Party> userParty = getUserParty(leaver);

        if (!userParty.isPresent()) {
            PartyMsg.error(leaver, "You are not in a party!");
        } else {
            if (userParty.get().getMembers().size() <= 1) {
                removeParty(userParty.get());
                PartyMsg.info(leaver, "Your party has been disbanded.");
            } else {
                removeUserFromParty(leaver);
                sendInfoToParty(userParty.get(), leaver.getName(), " has left the party.");
                PartyMsg.info(leaver, "You have left the party.");
            }
        }

    }

    /**
     * Switches the party leader from the current
     *
     * @param current The current leader
     * @param next    The next leader
     */
    public void setPartyLeader(User current, User next) {

        // if the kicker is trying to set leader to themselves, error
        if (current.getUniqueId().equals(next.getUniqueId())) {
            PartyMsg.error(current, "You can't set yourself as leader!");
            return;
        }

        Optional<Party> currentParty = getUserParty(current);
        Optional<Party> nextParty = getUserParty(next);

        if (!currentParty.isPresent()) {
            PartyMsg.error(current, "You are not in a party!");
        } else {
            if (!nextParty.isPresent() || !currentParty.get().equals(nextParty.get())) {
                PartyMsg.error(current, "That player is not in your party!");
                return;
            }

            if (!isUserPartyLeader(current, currentParty.get())) {
                PartyMsg.error(current, "You are not the leader of this party.");
            } else {
                currentParty.get().setLeader(next.getUniqueId());
                sendInfoToParty(currentParty.get(), next.getName(), " is now the leader of the party.");
            }
        }

    }

    /**
     * Set the user's party PvP state
     *
     * @param setter the user whose party to set
     * @param state  The state to set pvp to
     */
    public void setPartyPvp(User setter, boolean state) {

        Optional<Party> userParty = getUserParty(setter);

        if (!userParty.isPresent()) {
            PartyMsg.error(setter, "You are not in a party!");
        } else {
            if (isUserPartyLeader(setter, userParty.get())) {
                PartyMsg.error(setter, "You are not the leader of this party.");
                return;
            }

            userParty.get().setPvp(true);
            sendInfoToParty(userParty.get(), "Party PvP set to ", state ? TextColors.GREEN : TextColors.RED, state);
        }

    }
}
