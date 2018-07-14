package com.atherys.party;

import com.atherys.core.AtherysCore;
import com.atherys.core.command.CommandService;
import com.atherys.party.commands.PartyCommand;
import com.atherys.party.data.PartyData;
import com.atherys.party.data.PartyKeys;
import com.atherys.party.database.PartyManager;
import com.atherys.party.listeners.PlayerPartyListener;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import javax.inject.Inject;
import java.io.IOException;

import static com.atherys.party.AtherysParties.*;

@Plugin(id = ID, version = VERSION, name = NAME, description = DESCRIPTION, dependencies = {
        @Dependency(id = "atheryscore")
})
public class AtherysParties {

    public static final String ID = "atherysparties";
    public static final String NAME = "A'therys Parties";
    public static final String DESCRIPTION = "A Party plugin for the A'therys Horizons server";
    public static final String VERSION = "1.0.0a";

    private static AtherysParties instance;

    private static boolean init = false;

    private static PartyConfig config;

    @Inject
    private Logger logger;

    @Inject
    PluginContainer container;

    private void init() {
        instance = this;

        try {
            config = new PartyConfig();
            config.init();
        } catch (IOException e) {
            e.printStackTrace();
            init = false;
            return;
        }

        if (config.DEFAULT) {
            logger.error("AtherysCore config set to default. Plugin will halt. Please modify defaultConfig in config.conf to 'false' once non-default values have been inserted.");
            init = false;
            return;
        }

        init = true;

    }

    private void start() {
        Sponge.getEventManager().registerListeners(this, new PlayerPartyListener());
        PartyManager.getInstance().loadAll();

        try {
            AtherysCore.getCommandService().register(new PartyCommand(), this);
        } catch (CommandService.AnnotatedCommandException e) {
            e.printStackTrace();
        }
    }

    private void stop() {
        if (init) PartyManager.getInstance().saveAll();
    }

    @Listener
    public void preInit(GamePreInitializationEvent event) {
        PartyKeys.PARTY_DATA_REGISTRATION = DataRegistration.builder()
                .dataClass(PartyData.class)
                .immutableClass(PartyData.Immutable.class)
                .builder(new PartyData.Builder())
                .dataName("Party")
                .manipulatorId("party")
                .buildAndRegister(this.container);
    }

    @Listener(order = Order.EARLY)
    public void onInit(GameInitializationEvent event) {
        init();
    }

    @Listener
    public void onStart(GameStartingServerEvent event) {
        if (init) {
            start();
        }
    }

    @Listener
    public void onStop(GameStoppingServerEvent event) {
        if (init) {
            stop();
        }
    }

    public Logger getLogger() {
        return logger;
    }

    public static PartyConfig getConfig() {
        return config;
    }

    public static AtherysParties getInstance() {
        return instance;
    }
}
