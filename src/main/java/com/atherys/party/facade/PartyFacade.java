package com.atherys.party.facade;

import com.atherys.core.utils.Question;
import com.atherys.party.AtherysParties;
import com.atherys.party.data.PartyData;
import com.atherys.party.entity.Party;
import com.atherys.party.exception.PartyCommandException;
import com.atherys.party.service.PartyService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public final class PartyFacade {
    @Inject
    private PartyService partyService;

    @Inject
    private PartyMessagingFacade partyMsg;

    /**
     * Disband the party which the provided User is part of, and is the leader of.
     *
     * @param source The disbanding User
     */
    public void disbandParty(Player source) throws PartyCommandException {
        Party party = getPlayerPartyOrThrow(source);

        if (isPlayerPartyLeader(source, party)) {
            partyMsg.sendErrorToParty(party, "Your party has been disbanded.");
            partyService.removeParty(party);
        } else {
            throw PartyCommandException.notLeader();
        }
    }

    /**
     * When a user invites another user to their party. If the inviter is not party of a party, a new one will be created.
     *
     * @param source The inviting user
     * @param target the invited user
     */
    public void inviteToParty(Player source, Player target) throws PartyCommandException {

        if (source.getUniqueId().equals(target.getUniqueId())) {
            throw new PartyCommandException("You can't invite yourself!");
        }

        Optional<Party> inviterParty = getPlayerParty(source);
        Optional<Party> inviteeParty = getPlayerParty(target);

        Party party;

        // If neither the inviter nor the invitee are already in a party, create a new one
        if (!inviterParty.isPresent() && !inviteeParty.isPresent()) {
            AtherysParties.getInstance().getLogger().info("Yes");
            party = partyService.createParty(source);
            invite(source, target, party);
        }

        // If the inviter is in a party, check if they are a leader ( can invite new members )
        if (inviterParty.isPresent()) {

            party = inviterParty.get();

            // If the inviter is the party leader, invite the invitee
            if (isPlayerPartyLeader(source, party)) {
                invite(source, target, party);
            } else {
                throw PartyCommandException.notLeader();
            }
        }
    }

    private void invite(Player source, Player target, Party party) {
        Question question = Question.of(Text.of(source.getName(), " has invited you to their party."))
                .addAnswer(Question.Answer.of(Text.of("Accept"), playerInvitee -> {
                    partyService.addMember(party, target);

                    partyMsg.info(playerInvitee, "You have accepted ", source.getName(), "'s invite");
                    partyMsg.sendInfoToParty(party, playerInvitee.getName(), " has joined the party!");
                }))
                .addAnswer(Question.Answer.of(Text.of("Reject"), playerInvitee -> {
                    partyMsg.error(playerInvitee, "You have rejected ", source.getName(), "'s invite");
                }))
                .build();

        question.pollChat(target);

        partyMsg.sendInfoToParty(party, target.getName(), " has been invited to the party.");
    }

    /**
     * Kicks a user from a party by another user. If the kicker is not the leader, this will fail.
     *
     * @param source the user doing the kicking
     * @param target The user being kicked
     */
    public void kickFromParty(Player source, User target) throws PartyCommandException {
        // if the kicker is trying to kick themselves, trigger leaveParty instead
        if (source.getUniqueId().equals(target.getUniqueId())) {
            leaveParty(source);
            return;
        }

        Party kickerParty = getPlayerPartyOrThrow(source);
        Party kickeeParty = getOtherPlayerPartyOrThrow(target);

        if (!kickeeParty.equals(kickerParty)) {
            throw PartyCommandException.notInParty(target);
        }

        // if the kicker is not the leader of the party, error
        if (isPlayerPartyLeader(source, kickerParty)) {
            partyService.removeMember(kickerParty, target);
            partyMsg.sendErrorToParty(kickerParty, target.getName(), " has been kicked from the party.");
            if (target instanceof MessageReceiver) {
                partyMsg.error((MessageReceiver) target, "You have been kicked from the party.");
            }
        } else {
            throw PartyCommandException.notLeader();
        }

    }

    /**
     * When a user leaves a party
     *
     * @param source the user leaving their party
     */
    public void leaveParty(Player source) throws PartyCommandException {

        Party party = getPlayerPartyOrThrow(source);

        if (party.getMembers().size() <= 1) {
            partyService.removeParty(party);
            partyMsg.info(source, "Your party has been disbanded.");
        } else {
            partyService.removeMember(party, source);
            partyMsg.sendInfoToParty(party, source.getName(), " has left the party.");
            partyMsg.info(source, "You have left the party.");
        }
    }

    /**
     * Switches the party leader from the current
     *
     * @param source The current leader
     * @param target The next leader
     */
    public void setPartyLeader(Player source, Player target) throws PartyCommandException {

        // if the kicker is trying to set leader to themselves, error
        if (source.getUniqueId().equals(target.getUniqueId())) {
            throw new PartyCommandException("You can't set yourself as leader!");
        }

        Party currentParty = getPlayerPartyOrThrow(source);
        Party nextParty = getOtherPlayerPartyOrThrow(target);

        if (isPlayerPartyLeader(source, currentParty)) {
            if (currentParty.equals(nextParty)) {
                partyService.setPartyLeader(currentParty, target.getUniqueId());
                partyMsg.sendInfoToParty(currentParty, target.getName(), " is now the leader of the party.");
            } else {
                throw PartyCommandException.notInParty(target);
            }
        } else {
            throw PartyCommandException.notLeader();
        }
    }

    /**
     * Set the user's party PvP state
     *
     * @param source the user whose party to set
     * @param state  The state to set pvp to
     */
    public void setPartyPvp(Player source, boolean state) throws PartyCommandException {
        Party party = getPlayerPartyOrThrow(source);

        if (isPlayerPartyLeader(source, party)) {
            partyService.setPartyPvp(party, state);
            partyMsg.sendInfoToParty(party, "Party PvP set to ", state ? TextColors.GREEN : TextColors.RED, state, ".");
        } else {
            throw PartyCommandException.notLeader();
        }
    }

    /**
     * Retrieve information in Text form for the user's party.
     *
     * @param source the user whose party is to be looked at
     * @return A Text object with the required information
     */
    public void printPlayerParty(Player source) throws PartyCommandException {
        Party party = getPlayerPartyOrThrow(source);

        Text.Builder partyMembers = Text.builder();

        getOnlinePartyMembers(party).forEach(partyMember -> {
            if (isPlayerPartyLeader(partyMember, party)) {
                partyMembers.append(Text.of(TextColors.GOLD, TextStyles.BOLD, partyMember.getName(), "; "));
            } else {
                partyMembers.append(Text.of(TextColors.GOLD, TextStyles.RESET, partyMember.getName(), "; "));
            }
        });

        source.sendMessage(partyMembers.build());
    }

    public void onPlayerAttack(DamageEntityEvent event, Player source, Player target) {
        if (arePlayersInSameParty(source, target)) {
            getPlayerParty(source).ifPresent(party -> event.setCancelled(!party.isPvp()));
        }
    }

    public Party getPlayerPartyOrThrow(Player source) throws PartyCommandException {
        PartyData partyData = source.get(PartyData.class).orElseThrow(PartyCommandException::noParty);
        return partyData.getParty().orElseThrow(PartyCommandException::noParty);
    }

    public Party getOtherPlayerPartyOrThrow(User source) throws PartyCommandException {
        PartyData partyData = source.get(PartyData.class).orElseThrow(() -> PartyCommandException.notInParty(source));
        return partyData.getParty().orElseThrow(() -> PartyCommandException.notInParty(source));
    }

    public Optional<Party> getPlayerParty(Player source) {
        return source.get(PartyData.class).flatMap(PartyData::getParty);
    }

    /**
     * Checks if the provided users are in the same party.
     * If both users are in a party, and both parties share the same UUID (i.e. they are the same ), this returns true.
     * Under any other circumstances, including if neither user is in a party, this will return false.
     *
     * @param source The first user
     * @param other The second user
     */
    public <T extends User> boolean arePlayersInSameParty(Player source, Player other) {
        Optional<Party> party1 = getPlayerParty(source);
        Optional<Party> party2 = getPlayerParty(other);
        return party1.isPresent() && party2.isPresent() && party1.get().equals(party2.get());
    }

    public boolean isPlayerPartyLeader(Player source, Party party) {
        return source.getUniqueId().equals(party.getLeader());
    }

    public Set<Player> getOnlinePartyMembers(Party party) {
        return party.getMembers().stream()
                .map(uuid -> Sponge.getServer().getPlayer(uuid))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }
}
