package com.atherys.party.database;

import com.atherys.core.db.HibernateRepository;
import com.atherys.party.Party;

import java.util.UUID;

public class PartyRepository extends HibernateRepository<Party, UUID> {

    private static final PartyRepository instance = new PartyRepository();

    private PartyRepository() {
        super(Party.class);
    }

    public static PartyRepository getInstance() {
        return instance;
    }
}
