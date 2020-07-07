package com.atherys.party.chat;

import com.atherys.chat.model.AtherysChannel;
import com.atherys.party.AtherysParties;
import com.atherys.party.entity.Party;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.channel.MessageReceiver;

import java.util.*;

public class PartyChannel extends AtherysChannel {
    public static final String PERMISSION = "atherysparties.chat";

    public PartyChannel() {
        super("party");
        Set<String> aliases= new HashSet<>();
        aliases.add("pc");
        this.setAliases(aliases);
        this.setPermission(PERMISSION);
        this.setPrefix("&f[&3P&f]&r");
        this.setSuffix("");
        this.setFormat("%cprefix %player: %message %csuffix");
        this.setName("&3Party");
    }

    @Override
    public Collection<MessageReceiver> getMembers(Object sender) {
        if (sender instanceof Player) {
            Optional<Party> party = AtherysParties.getInstance().getPartyFacade().getPlayerParty((Player) sender);

            if (party.isPresent()) {
                return new HashSet<>(AtherysParties.getInstance().getPartyFacade().getOnlinePartyMembers(party.get()));
            }
        }

        return Collections.emptySet();
    }
}
