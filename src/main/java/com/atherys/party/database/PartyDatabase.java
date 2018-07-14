package com.atherys.party.database;

import com.atherys.core.database.mongo.MorphiaDatabase;
import com.atherys.party.AtherysParties;

public class PartyDatabase extends MorphiaDatabase {

    private static PartyDatabase instance = new PartyDatabase();

    private PartyDatabase() {
        super(AtherysParties.getConfig().DATABASE, "com.atherys.core");
    }

    public static PartyDatabase getInstance() {
        return instance;
    }

}
