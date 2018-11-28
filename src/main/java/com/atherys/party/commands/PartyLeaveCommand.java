package com.atherys.party.commands;

import com.atherys.core.command.UserCommand;
import com.atherys.core.command.annotation.Aliases;
import com.atherys.core.command.annotation.Permission;
import com.atherys.party.PartyService;
import com.atherys.party.PartyMsg;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.User;

@Aliases("leave")
@Permission("atherysparties.party.leave")
public class PartyLeaveCommand extends UserCommand {

    @Override
    public CommandResult execute(User source, CommandContext args) throws CommandException {

        PartyService.getInstance().leaveParty(source);

        return CommandResult.success();
    }

}
