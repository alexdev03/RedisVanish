package org.alexdev.redisvanish.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.VelocityBrigadierMessage;
import com.velocitypowered.api.proxy.Player;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.alexdev.redisvanish.RedisVanish;
import org.alexdev.redisvanish.data.VanishLevel;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@RequiredArgsConstructor
public final class RedisVanishCommand {

    private final RedisVanish plugin;


    @NotNull
    public BrigadierCommand getCommand() {
        final LiteralArgumentBuilder<CommandSource> builder = LiteralArgumentBuilder
                .<CommandSource>literal("redisvanish")
                .executes(ctx -> {

                    if (!(ctx.getSource() instanceof Player player)) {
                        plugin.getConfigManager().getMessages().sendMessage(ctx.getSource(), "mustBeAPlayer");
                        return Command.SINGLE_SUCCESS;
                    }

                    if (!player.hasPermission("redisvanish.vanish")) {
                        plugin.getConfigManager().getMessages().sendMessage(player, "noPermission");
                        return Command.SINGLE_SUCCESS;
                    }

                    if (plugin.getVanishManager().isVanished(player)) {
                        plugin.getVanishManager().unVanish(player);
                        plugin.getConfigManager().getMessages().sendMessage(player, "unVanished");
                    } else {
                        plugin.getVanishManager().vanish(player);
                        plugin.getConfigManager().getMessages().sendMessage(player, "vanished");
                    }

                    return Command.SINGLE_SUCCESS;
                })
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("name", StringArgumentType.word())
                        .suggests((ctx, builder1) -> {
                            String input = (ctx.getArguments().containsKey("name") ? StringArgumentType.getString(ctx, "name") : "").toLowerCase();
                            plugin.getServer().getAllPlayers().stream()
                                    .map(Player::getUsername)
                                    .filter(u -> input.isEmpty() || u.toLowerCase().startsWith(input))
                                    .forEach(u -> builder1.suggest(u, VelocityBrigadierMessage.tooltip(MiniMessage.miniMessage().deserialize("<rainbow>" + u))));
                            return builder1.buildFuture();
                        })
                        .executes(ctx -> {
                            if (!(ctx.getSource() instanceof Player player)) {
                                plugin.getConfigManager().getMessages().sendMessage(ctx.getSource(), "mustBeAPlayer");
                                return Command.SINGLE_SUCCESS;
                            }

                            if (!player.hasPermission("redisvanish.vanish.others")) {
                                plugin.getConfigManager().getMessages().sendMessage(player, "noPermission");
                                return Command.SINGLE_SUCCESS;
                            }

                            String name = StringArgumentType.getString(ctx, "name");
                            Optional<Player> target = plugin.getServer().getPlayer(name);

                            if (target.isEmpty()) {
                                plugin.getConfigManager().getMessages().sendMessage(player, "playerNotFound", "%player%", name);
                                return Command.SINGLE_SUCCESS;
                            }

                            if (plugin.getVanishManager().isVanished(target.get())) {
                                plugin.getVanishManager().unVanish(target.get());
                                plugin.getConfigManager().getMessages().sendMessage(player, "unVanishedOther", "%player%", target.get().getUsername());
                            } else {
                                plugin.getVanishManager().vanish(target.get());
                                plugin.getConfigManager().getMessages().sendMessage(player, "vanishedOther", "%player%", target.get().getUsername());
                            }

                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(LiteralArgumentBuilder.<CommandSource>literal("reload")
                        .requires(src -> src.hasPermission("redisvanish.command.reload"))
                        .executes(ctx -> {
                            plugin.getConfigManager().reload();
                            plugin.getRedis().publishVanishLevels();
                            plugin.getConfigManager().getMessages().sendMessage(ctx.getSource(), "reloaded");
                            return Command.SINGLE_SUCCESS;
                        })
                )
                //debug
                .then(LiteralArgumentBuilder.<CommandSource>literal("debug")
                        .requires(src -> src.hasPermission("redisvanish.command.debug"))
                        .executes(ctx -> {
                            if (!(ctx.getSource() instanceof Player player)) {
                                plugin.getConfigManager().getMessages().sendMessage(ctx.getSource(), "mustBeAPlayer");
                                return Command.SINGLE_SUCCESS;
                            }

                            Optional<VanishLevel> vanishLevel = plugin.getVanishManager().getVanishLevel(player);

                            if (vanishLevel.isEmpty()) {
                                plugin.getConfigManager().getMessages().sendMessage(player, "noVanishLevel");
                                return Command.SINGLE_SUCCESS;
                            }

                            plugin.getConfigManager().getMessages().sendMessage(ctx.getSource(), "debug", "%message%", "Vanish level: " + vanishLevel.get().name() + " (" + vanishLevel.get().permission() + ")"
                                    + " vanished: " + plugin.getVanishManager().isVanished(player));
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("name", StringArgumentType.word())
                                .suggests((ctx, builder1) -> {
                                    String input = (ctx.getArguments().containsKey("name") ? StringArgumentType.getString(ctx, "name") : "").toLowerCase();
                                    plugin.getServer().getAllPlayers().stream()
                                            .map(Player::getUsername)
                                            .filter(u -> input.isEmpty() || u.toLowerCase().startsWith(input))
                                            .forEach(u -> builder1.suggest(u, VelocityBrigadierMessage.tooltip(MiniMessage.miniMessage().deserialize("<rainbow>" + u))));
                                    return builder1.buildFuture();
                                })
                                .executes(ctx -> {
                                    String name = StringArgumentType.getString(ctx, "name");
                                    Optional<Player> target = plugin.getServer().getPlayer(name);

                                    if (target.isEmpty()) {
                                        plugin.getConfigManager().getMessages().sendMessage(ctx.getSource(), "playerNotFound", "%player%", name);
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    Optional<VanishLevel> vanishLevel = plugin.getVanishManager().getVanishLevel(target.get());

                                    if (vanishLevel.isEmpty()) {
                                        plugin.getConfigManager().getMessages().sendMessage(ctx.getSource(), "noVanishLevelOther", "%player%", target.get().getUsername());
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    plugin.getConfigManager().getMessages().sendMessage(ctx.getSource(), "debug", "%message%", "Vanish level: " + vanishLevel.get().name() + " (" + vanishLevel.get().permission() + ")"
                                            + " vanished: " + plugin.getVanishManager().isVanished(target.get()));
                                    return Command.SINGLE_SUCCESS;
                                })

                ));

        return new BrigadierCommand(builder);
    }
}
