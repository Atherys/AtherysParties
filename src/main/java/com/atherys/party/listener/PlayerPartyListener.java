package com.atherys.party.listener;

import com.atherys.core.utils.EntityUtils;
import com.atherys.party.AtherysParties;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.Root;

public class PlayerPartyListener {

    @Listener(order = Order.LAST)
    public void onPlayerDamage(DamageEntityEvent event, @Root EntityDamageSource source, @Getter("getTargetEntity") Player target) {
        EntityUtils.playerAttackedEntity(source).ifPresent(player -> {
            AtherysParties.getInstance().getPartyFacade().onPlayerAttack(event, player, target);
        });
    }

}
