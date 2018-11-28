package com.atherys.party.commands;

import com.atherys.core.command.ParameterizedCommand;
import com.atherys.core.command.UserCommand;
import com.atherys.core.command.annotation.Aliases;
import com.atherys.core.command.annotation.Permission;
import com.atherys.core.utils.Question;
import com.atherys.party.Party;
import com.atherys.party.PartyMsg;
import com.atherys.party.PartyService;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Optional;

@Aliases({"add", "invite"})
@Permission("atherysparties.party.invite")
public class PartyInviteCommand extends UserCommand implements ParameterizedCommand {

    @Nonnull
    @Override
    public CommandResult execute(@Nonnull User source, @Nonnull CommandContext args) throws CommandException {

        args.<User>getOne("invitedPlayer").ifPresent(invitedPlayer -> {
            PartyService.getInstance().inviteToParty(source, invitedPlayer);
        });

        return CommandResult.success();
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[]{
                GenericArguments.player(Text.of("invitedPlayer"))
        };
    }
}
