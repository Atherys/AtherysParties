package com.atherys.party.commands;

import com.atherys.core.command.UserCommand;
import com.atherys.core.command.annotation.Aliases;
import com.atherys.core.command.annotation.Children;
import com.atherys.core.command.annotation.Permission;
import com.atherys.party.AtherysParties;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;

import java.util.Optional;

@Aliases("party")
@Children({
        PartyInviteCommand.class,
        PartyKickCommand.class,
        PartyLeaderCommand.class,
        PartyLeaveCommand.class,
        PartyDisbandCommand.class,
        PartyPvpCommand.class
})
@Permission("atherysparties.party")
public class PartyCommand extends UserCommand {

    @Override
    public CommandResult execute(User user, CommandContext args) throws CommandException {

        ((Player) user).sendMessage(AtherysParties.getPartyService().printUserParty(user));

        return CommandResult.success();
    }

}
