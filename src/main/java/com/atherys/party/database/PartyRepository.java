package com.atherys.party.database;

import com.atherys.core.db.AtherysRepository;
import com.atherys.party.AtherysParties;
import com.atherys.party.Party;

import java.util.UUID;

public class PartyRepository extends AtherysRepository<Party, UUID> {

    private static final PartyRepository instance = new PartyRepository();

    private PartyRepository() {
        super(Party.class, AtherysParties.getInstance().getLogger());
    }

    public static PartyRepository getInstance() {
        return instance;
    }
}
