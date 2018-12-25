package com.atherys.party.commands;

import com.atherys.core.command.UserCommand;
import com.atherys.core.command.annotation.Aliases;
import com.atherys.core.command.annotation.Permission;
import com.atherys.party.AtherysParties;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.User;

import javax.annotation.Nonnull;

@Aliases({"disband", "remove"})
@Permission("atherysparties.party.disband")
public class PartyDisbandCommand extends UserCommand {

    @Nonnull
    @Override
    public CommandResult execute(@Nonnull User source, @Nonnull CommandContext args) throws CommandException {

        AtherysParties.getPartyService().disbandParty(source);

        return CommandResult.success();
    }

}
