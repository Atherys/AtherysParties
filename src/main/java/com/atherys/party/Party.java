package com.atherys.party;

import com.atherys.core.db.SpongeIdentifiable;
import org.hibernate.annotations.GenericGenerator;

import javax.annotation.Nonnull;
import javax.persistence.*;
import java.util.*;

@Entity
public class Party implements SpongeIdentifiable {

    @Id
    private UUID uuid;

    private UUID leader;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<UUID> members;

    private boolean pvp;

    Party() {}

    Party(UUID leader, Set<UUID> members) {
        this.uuid = UUID.randomUUID();
        this.leader = leader;
        this.members = members;
    }

    @Nonnull
    @Override
    public UUID getId() {
        return uuid;
    }

    public UUID getLeader() {
        return leader;
    }

    public void setLeader(UUID leader) {
        this.leader = leader;
    }

    public Set<UUID> getMembers() {
        return members;
    }

    public void removeMember(UUID member) {
        members.remove(member);
    }

    public void addMember(UUID member) {
        members.add(member);
    }

    public void setMembers(Set<UUID> members) {
        this.members = members;
    }

    public boolean isPvp() {
        return pvp;
    }

    public void setPvp(boolean pvp) {
        this.pvp = pvp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Party party = (Party) o;
        return Objects.equals(uuid, party.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

}
