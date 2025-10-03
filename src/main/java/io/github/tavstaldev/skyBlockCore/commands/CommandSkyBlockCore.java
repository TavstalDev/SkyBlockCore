package io.github.tavstaldev.skyBlockCore.commands;

import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.minecorelib.models.command.SubCommandData;
import io.github.tavstaldev.minecorelib.utils.ChatUtils;
import io.github.tavstaldev.skyBlockCore.SkyBlockCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandSkyBlockCore implements CommandExecutor {
    private final PluginLogger _logger = SkyBlockCore.Logger().withModule(CommandSkyBlockCore.class);
    @SuppressWarnings("FieldCanBeLocal")
    private final String baseCommand = "sbc";
    private final List<SubCommandData> _subCommands = new ArrayList<>() {
        {
            // HELP
            add(new SubCommandData("help", "skyblockcore.commands.help", Map.of(
                    "syntax", "",
                    "description", "Commands.Help.Desc"
            )));
            // VERSION
            add(new SubCommandData("version", "skyblockcore.commands.version", Map.of(
                    "syntax", "",
                    "description", "Commands.Version.Desc"
            )));
            // RELOAD
            add(new SubCommandData("reload", "skyblockcore.commands.reload", Map.of(
                    "syntax", "",
                    "description", "Commands.Reload.Desc"
            )));
        }
    };

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        // Handle subcommands based on the first argument
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "help":
                case "?": {
                    // Handle commands sent from the console
                    if (sender instanceof ConsoleCommandSender) {
                        SkyBlockCore.Instance.sendCommandReply(sender, "Commands.NotPlayer");
                        return true;
                    }
                    Player player = (Player) sender;

                    // Check if the player has permission to use the help command
                    if (!player.hasPermission("skyblockcore.commands.help")) {
                        SkyBlockCore.Instance.sendCommandReply(player, "General.NoPermission");
                        return true;
                    }

                    // Parse the page number for the help command
                    int page = 1;
                    if (args.length > 1) {
                        try {
                            page = Integer.parseInt(args[1]);
                        } catch (Exception ex) {
                            SkyBlockCore.Instance.sendCommandReply(player, "Commands.Common.InvalidPage");
                            return true;
                        }
                    }

                    help(player, page);
                    return true;
                }
                case "version": {
                    // Handle commands sent from the console
                    if (sender instanceof ConsoleCommandSender) {
                        SkyBlockCore.Instance.sendCommandReply(sender, "Commands.NotPlayer");
                        return true;
                    }
                    Player player = (Player) sender;

                    // Check if the player has permission to use the version command
                    if (!player.hasPermission("skyblockcore.commands.version")) {
                        SkyBlockCore.Instance.sendCommandReply(player, "General.NoPermission");
                        return true;
                    }

                    // Send the current plugin version to the player
                    Map<String, Object> parameters = new HashMap<>();
                    parameters.put("version", SkyBlockCore.Instance.getVersion());
                    SkyBlockCore.Instance.sendCommandReply(player, "Commands.Version.Current", parameters);

                    // Check if the plugin is up-to-date
                    SkyBlockCore.Instance.isUpToDate().thenAccept(upToDate -> {
                        if (upToDate) {
                            SkyBlockCore.Instance.sendCommandReply(player, "Commands.Version.UpToDate");
                        } else {
                            SkyBlockCore.Instance.sendCommandReply(player, "Commands.Version.Outdated", Map.of("link", SkyBlockCore.Instance.getDownloadUrl()));
                        }
                    }).exceptionally(e -> {
                        _logger.error("Failed to determine update status: " + e.getMessage());
                        return null;
                    });
                    return true;
                }
                case "reload": {
                    // Handle commands sent from the console
                    if (sender instanceof ConsoleCommandSender) {
                        SkyBlockCore.Instance.sendCommandReply(sender, "Commands.NotPlayer");
                        return true;
                    }
                    Player player = (Player) sender;

                    // Check if the player has permission to use the reload command
                    if (!player.hasPermission("skyblockcore.commands.reload")) {
                        SkyBlockCore.Instance.sendCommandReply(player, "General.NoPermission");
                        return true;
                    }

                    // Reload the plugin configuration
                    SkyBlockCore.Instance.reload();
                    SkyBlockCore.Instance.sendCommandReply(player, "Commands.Reload.Done");
                    return true;
                }
            }

            // Send an error message if the subcommand is invalid
            SkyBlockCore.Instance.sendCommandReply(sender, "Commands.Common.InvalidArguments");
            return true;
        }

        // Default to the help command if no arguments are provided
        if (!sender.hasPermission("skyblockcore.commands.help")) {
            SkyBlockCore.Instance.sendCommandReply(sender, "General.NoPermission");
            return true;
        }
        help(sender, 1);
        return true;
    }

    private void help(CommandSender sender, int page) {
        int maxPage = 1 + (_subCommands.size() / 15);

        // Ensure the page number is within valid bounds
        if (page > maxPage)
            page = maxPage;
        if (page < 1)
            page = 1;
        int finalPage = page;

        // Send the help menu title and info
        SkyBlockCore.Instance.sendCommandReply(sender, "Commands.Help.Title", Map.of(
                        "current_page", finalPage,
                        "max_page", maxPage
                )
        );
        SkyBlockCore.Instance.sendCommandReply(sender, "Commands.Help.Info");

        boolean reachedEnd = false;
        int itemIndex = 0;

        // Display up to 15 subcommands per page
        for (int i = 0; i < 15; i++) {
            int index = itemIndex + (page - 1) * 15;
            if (index >= _subCommands.size()) {
                reachedEnd = true;
                break;
            }
            itemIndex++;

            SubCommandData subCommand = _subCommands.get(index);
            if (!subCommand.hasPermission(sender)) {
                i--;
                continue;
            }

            subCommand.send(SkyBlockCore.Instance, sender, baseCommand);
        }

        // Display navigation buttons for the help menu
        String previousBtn, nextBtn, bottomMsg;
        if (sender instanceof Player player) {
            previousBtn = SkyBlockCore.Instance.localize(player, "Commands.Help.PrevBtn");
            nextBtn = SkyBlockCore.Instance.localize(player, "Commands.Help.NextBtn");
            bottomMsg = SkyBlockCore.Instance.localize(player, "Commands.Help.Bottom", Map.of(
                    "current_page", String.valueOf(page),
                    "max_page", String.valueOf(maxPage))
            );
        } else {
            previousBtn = SkyBlockCore.Instance.localize("Commands.Help.PrevBtn");
            nextBtn = SkyBlockCore.Instance.localize("Commands.Help.NextBtn");
            bottomMsg = SkyBlockCore.Instance.localize("Commands.Help.Bottom", Map.of(
                    "current_page", String.valueOf(page),
                    "max_page", String.valueOf(maxPage))
            );
        }

        Map<String, Component> bottomParams = new HashMap<>();
        bottomParams.put("previous_btn", ChatUtils.translateColors(previousBtn, true)
                .clickEvent(page > 1 ? ClickEvent.runCommand(String.format("/%s help %s", baseCommand, page - 1)) : null));
        bottomParams.put("next_btn", ChatUtils.translateColors(nextBtn, true)
                .clickEvent(!reachedEnd && maxPage >= page + 1 ? ClickEvent.runCommand(String.format("/%s help %s", baseCommand, page + 1)) : null));

        Component bottomComp = ChatUtils.buildWithButtons(bottomMsg, bottomParams);
        sender.sendMessage(bottomComp);
    }
}