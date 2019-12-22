package com.atherys.party.facade;

import com.atherys.core.utils.AbstractMessagingFacade;
import com.atherys.party.entity.Party;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

/**
 * A utility class for formatting and sending {@link Party}-related messages to players.
 */
@Singleton
public final class PartyMessagingFacade extends AbstractMessagingFacade {

    @Inject
    private PartyFacade partyFacade;

    public PartyMessagingFacade() {
        super("Party");
    }


    /**
     * Sends an info message to all members of the given party. Uses {@link Party#getMembers()}
     *
     * @param party The party to whose members the message will be sent.
     * @param msg   The message. Will later be wrapped in a {@link Text} object.
     */
    public void sendInfoToParty(Party party, Object... msg) {
        for (Player member : partyFacade.getOnlinePartyMembers(party)) {
            info(member, msg);
        }
    }

    /**
     * Sends an error message to all members of the given party. Uses {@link Party#getMembers()}
     *
     * @param party The party to whose members the message will be sent.
     * @param msg   The message. Will later be wrapped in a {@link Text} object.
     */
    public void sendErrorToParty(Party party, Object... msg) {
        for (Player member : partyFacade.getOnlinePartyMembers(party)) {
            error(member, msg);
        }
    }

}
