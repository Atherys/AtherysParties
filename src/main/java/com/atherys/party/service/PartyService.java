package com.atherys.party.service;

import com.atherys.core.utils.UserUtils;
import com.atherys.party.AtherysParties;
import com.atherys.party.data.PartyData;
import com.atherys.party.entity.Party;
import com.google.inject.Singleton;
import org.apache.commons.lang3.RandomUtils;
import org.spongepowered.api.entity.living.player.User;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The primary class responsible for tracking all parties and their members. Is also responsible for
 * saving/loading parties and their members to/from the database.
 */
@Singleton
public final class PartyService {

    private Map<UUID, Party> parties = new HashMap<>();

    public Optional<Party> getParty(UUID partyUUID) {
        return Optional.ofNullable(parties.get(partyUUID));
    }

    public void removeParty(Party party) {
        getPartyMembers(party).forEach(member -> removeMember(party, member));
        parties.remove(party.getId());
    }

    public Party createParty(User leader, User... members) {
        Party party = new Party(leader.getUniqueId(), new HashSet<>());

        addMember(party, leader);
        for (User member : members) {
            addMember(party, member);
        }

        parties.put(party.getId(), party);

        return party;
    }

    public void addMember(Party party, User member) {
        AtherysParties.getInstance().getLogger().info("Added {} to party", member.getName());
        party.addMember(member.getUniqueId());
        member.offer(new PartyData(party.getUniqueId()));
    }

    public void removeMember(Party party, User member) {
        party.removeMember(member.getUniqueId());
        member.remove(PartyData.class);
    }

    public void setPartyPvp(Party party, boolean status) {
        party.setPvp(status);
    }

    public void setPartyLeader(Party party, UUID leader) {
        party.setLeader(leader);
    }

    public void setRandomPartyMemberAsLeader(Party party) {
        UUID newLeader = (UUID) party.getMembers().toArray()[RandomUtils.nextInt(0, party.getMembers().size() - 1)];
        party.setLeader(newLeader);
    }

    /**
     *
     * @param party the party whose members are to be retrieved
     * @return A collection of users who are members of the party
     */
    public Collection<User> getPartyMembers(Party party) {
        return party.getMembers().stream()
                .map(UserUtils::getUser)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }
}


