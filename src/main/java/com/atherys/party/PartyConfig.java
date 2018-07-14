package com.atherys.party;

import com.atherys.core.database.mongo.MongoDatabaseConfig;
import com.atherys.core.utils.PluginConfig;
import ninja.leaping.configurate.objectmapping.Setting;

import java.io.IOException;

public class PartyConfig extends PluginConfig {

    @Setting("is-default")
    public boolean DEFAULT = true;

    @Setting("database")
    public MongoDatabaseConfig DATABASE = new MongoDatabaseConfig();

    protected PartyConfig() throws IOException {
        super("config/" + AtherysParties.ID, "config.conf");
    }
}
