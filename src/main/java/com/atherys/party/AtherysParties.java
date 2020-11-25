package com.atherys.party;

import com.atherys.core.AtherysCore;
import com.atherys.core.command.CommandService;
import com.atherys.party.commands.PartyCommand;
import com.atherys.party.data.PartyData;
import com.atherys.party.data.PartyKeys;
import com.atherys.party.facade.PartyFacade;
import com.atherys.party.facade.PartyMessagingFacade;
import com.atherys.party.integration.AtherysChatIntegration;
import com.atherys.party.listener.PlayerPartyListener;
import com.atherys.party.service.PartyService;
import com.google.inject.Injector;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import javax.inject.Inject;

import static com.atherys.party.AtherysParties.*;

@Plugin(id = ID, version = VERSION, name = NAME, description = DESCRIPTION, dependencies = {
        @Dependency(id = "atheryscore"),
        @Dependency(id = "atheryschat", optional = true)
})
public class AtherysParties {

    public static final String ID = "atherysparties";
    public static final String NAME = "A'therys Parties";
    public static final String DESCRIPTION = "A Party plugin for the A'therys Horizons server";
    public static final String VERSION = "%PROJECT_VERSION%";

    private static AtherysParties instance;

    private static boolean init = false;

    @Inject
    private Logger logger;

    @Inject
    PluginContainer container;

    @Inject
    Injector spongeInjector;
    private Injector partyInjector;

    private Components components;

    private void init() {
        instance = this;

        components = new Components();
        partyInjector = spongeInjector.createChildInjector(new AtherysPartiesModule());
        partyInjector.injectMembers(components);

        init = true;
    }

    private void start() {
        Sponge.getEventManager().registerListeners(this, components.partyListener);

        if (Sponge.getPluginManager().isLoaded("atherysparties")) {
            AtherysChatIntegration.registerPartyChat();
        }

        try {
            AtherysCore.getCommandService().register(new PartyCommand(), this);
        } catch (CommandService.AnnotatedCommandException e) {
            e.printStackTrace();
        }
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

    public Logger getLogger() {
        return logger;
    }


    public static AtherysParties getInstance() {
        return instance;
    }

    public PartyService getPartyService() {
        return components.partyService;
    }

    public PartyFacade getPartyFacade() {
        return components.partyFacade;
    }

    public PlayerPartyListener getPartyListener() {
        return components.partyListener;
    }

    public PartyMessagingFacade getPartyMessagingFacade() {
        return components.partyMessagingFacade;
    }

    private static class Components {
        @Inject
        private PartyService partyService;

        @Inject
        private PartyFacade partyFacade;

        @Inject
        private PlayerPartyListener partyListener;

        @Inject
        private PartyMessagingFacade partyMessagingFacade;
    }
}
