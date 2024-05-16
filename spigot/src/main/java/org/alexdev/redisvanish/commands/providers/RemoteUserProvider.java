package org.alexdev.redisvanish.commands.providers;

import com.jonahseguin.drink.argument.CommandArg;
import com.jonahseguin.drink.exception.CommandExitMessage;
import com.jonahseguin.drink.parametric.DrinkProvider;
import lombok.RequiredArgsConstructor;
import org.alexdev.redisvanish.RedisVanish;
import org.alexdev.redisvanish.data.RemoteUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.List;

@RequiredArgsConstructor
public class RemoteUserProvider extends DrinkProvider<RemoteUser> {

    private final RedisVanish plugin;

    @Override
    public boolean doesConsumeArgument() {
        return true;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Nullable
    @Override
    public RemoteUser provide(@NotNull CommandArg arg, @NotNull List<? extends Annotation> annotations) throws CommandExitMessage {
        return plugin.getUserManager().getRemoteUser(arg.get()).orElseThrow(() -> new CommandExitMessage("User " + arg.get() + " not found"));
    }

    @Override
    public String argumentDescription() {
        return "";
    }

    @Override
    public List<String> getSuggestions(@NotNull String prefix) {
        final String lowerCasePrefix = prefix.toLowerCase();
        return plugin.getUserManager().getRemoteUserByName()
                .keySet()
                .stream()
                .filter(name -> name.toLowerCase().startsWith(lowerCasePrefix))
                .toList();
    }
}
