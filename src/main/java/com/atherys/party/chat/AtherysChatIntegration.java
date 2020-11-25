package com.atherys.party.chat;

import com.atherys.chat.AtherysChat;
import com.atherys.chat.model.AtherysChannel;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Optional;

public final class AtherysChatIntegration {
    public static void registerPartyChat() {
        AtherysChat.getInstance().getChatService().registerChannel(new PartyChannel());
    }

    public static void removePlayerFromPartyChannel(Player player) {
        Optional<AtherysChannel> channel = AtherysChat.getInstance().getChatService().getChannelById("party");
        channel.ifPresent(atherysChannel -> AtherysChat.getInstance().getChannelFacade().removePlayerFromChannel(player, atherysChannel));
    }
}
