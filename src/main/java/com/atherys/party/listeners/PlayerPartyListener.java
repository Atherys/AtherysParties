package com.atherys.party.listeners;

import com.atherys.party.AtherysParties;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.filter.cause.Root;

public class PlayerPartyListener {

    @Listener
    public void onPlayerDamage(DamageEntityEvent event, @Root Player player) {
        if ( event.getTargetEntity() instanceof Player ) {
            if (AtherysParties.getPartyService().areUsersInSameParty(player, (User) event.getTargetEntity())) {
                AtherysParties.getPartyService().getUserParty(player).ifPresent(party -> event.setCancelled(!party.isPvp()));
            }
        }
    }

}
