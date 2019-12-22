package com.atherys.party;

import com.atherys.party.facade.PartyFacade;
import com.atherys.party.facade.PartyMessagingFacade;
import com.atherys.party.listener.PlayerPartyListener;
import com.atherys.party.service.PartyService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class AtherysPartiesModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(PartyFacade.class).in(Scopes.SINGLETON);
        bind(PartyService.class).in(Scopes.SINGLETON);
        bind(PartyMessagingFacade.class).in(Scopes.SINGLETON);
        bind(PlayerPartyListener.class).in(Scopes.SINGLETON);
    }
}
