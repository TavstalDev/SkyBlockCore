package io.github.tavstaldev.skyBlockCore.commands;

import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.minecorelib.models.command.SubCommandData;
import io.github.tavstaldev.minecorelib.utils.ChatUtils;
import io.github.tavstaldev.skyBlockCore.SkyBlockCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.OfflinePlayer;
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

/**
 * Handles the /levelxp command and its subcommands.
 * Provides functionality for managing player experience levels, including help, getting, adding, removing, setting, and resetting experience.
 */
public class CommandLevelXp implements CommandExecutor {
    @SuppressWarnings("FieldCanBeLocal")
    private final PluginLogger _logger = SkyBlockCore.logger().withModule(CommandLevelXp.class); // Logger for command-related operations.
    private final String baseCommand = "levelxp"; // Base command name.
    private final List<SubCommandData> _subCommands = new ArrayList<>() {
        {
            // HELP subcommand
            add(new SubCommandData("help", "skyblockcore.commands.levelxp.help", Map.of(
                    "syntax", "",
                    "description", "Commands.LevelXP.Help.Desc"
            )));
            // GET subcommand
            add(new SubCommandData("get", "skyblockcore.commands.levelxp.get", Map.of(
                    "syntax", "Commands.LevelXP.Get.Syntax",
                    "description", "Commands.LevelXP.Get.Desc"
            )));
            // ADD subcommand
            add(new SubCommandData("add", "skyblockcore.commands.levelxp.add", Map.of(
                    "syntax", "Commands.LevelXP.Add.Syntax",
                    "description", "Commands.LevelXP.Add.Desc"
            )));
            // REMOVE subcommand
            add(new SubCommandData("remove", "skyblockcore.commands.levelxp.remove", Map.of(
                    "syntax", "Commands.LevelXP.Remove.Syntax",
                    "description", "Commands.LevelXP.Remove.Desc"
            )));
            // SET subcommand
            add(new SubCommandData("set", "skyblockcore.commands.levelxp.set", Map.of(
                    "syntax", "Commands.LevelXP.Set.Syntax",
                    "description", "Commands.LevelXP.Set.Desc"
            )));
            // RESET subcommand
            add(new SubCommandData("reset", "skyblockcore.commands.levelxp.reset", Map.of(
                    "syntax", "Commands.LevelXP.Reset.Syntax",
                    "description", "Commands.LevelXP.Reset.Desc"
            )));
        }
    };

    /**
     * Initializes the /levelxp command by setting its executor.
     * Logs an error if the command is not found in the plugin.yml file.
     */
    public CommandLevelXp() {
        var command = SkyBlockCore.Instance.getCommand(baseCommand);
        if (command == null) {
            _logger.error("Could not get command /" + baseCommand + " from plugin.yml! Disabling command...");
            return;
        }
        command.setExecutor(this);
    }

    /**
     * Handles the execution of the /levelxp command and its subcommands.
     *
     * @param sender  The sender of the command (player or console).
     * @param command The command being executed.
     * @param label   The alias of the command used.
     * @param args    The arguments provided with the command.
     * @return True if the command was successfully executed, false otherwise.
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        // Handle subcommands based on the first argument
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "help":
                case "?": {
                    // Check if the player has permission to use the help command
                    if (!sender.hasPermission("skyblockcore.commands.levelxp.help")) {
                        SkyBlockCore.Instance.sendCommandReply(sender, "General.NoPermission");
                        return true;
                    }

                    // Parse the page number for the help command
                    int page = 1;
                    if (args.length > 1) {
                        try {
                            page = Integer.parseInt(args[1]);
                        } catch (Exception ex) {
                            SkyBlockCore.Instance.sendCommandReply(sender, "Commands.Common.InvalidPage");
                            return true;
                        }
                    }

                    help(sender, page);
                    return true;
                }
                case "get": {
                    // Check if the player has permission to use the get command
                    if (!sender.hasPermission("skyblockcore.commands.levelxp.get")) {
                        SkyBlockCore.Instance.sendCommandReply(sender, "General.NoPermission");
                        return true;
                    }

                    if (args.length == 2) {
                        if (!sender.hasPermission("skyblockcore.commands.levelxp.get.others")) {
                            SkyBlockCore.Instance.sendCommandReply(sender, "General.NoPermission");
                            return true;
                        }
                        OfflinePlayer target = SkyBlockCore.Instance.getServer().getOfflinePlayer(args[1]);
                        if (!target.hasPlayedBefore() && !target.isOnline()) {
                            SkyBlockCore.Instance.sendCommandReply(sender, "Commands.Common.InvalidPlayer", Map.of("player", args[1]));
                            return true;
                        }

                        var playerData = SkyBlockCore.database().getPlayerData(target.getUniqueId());
                        if (playerData.isEmpty()) {
                            SkyBlockCore.Instance.sendCommandReply(sender, "General.NoPlayerData", Map.of("player", args[1]));
                            return true;
                        }

                        SkyBlockCore.Instance.sendCommandReply(sender, "Commands.LevelXP.Get.OtherLevel", Map.of(
                                        "player", args[1],
                                        "level", String.valueOf(playerData.get().getExperience())
                                )
                        );
                        return true;
                    }

                    // Handle commands sent from the console
                    if (sender instanceof ConsoleCommandSender) {
                        SkyBlockCore.Instance.sendCommandReply(sender, "Commands.NotPlayer");
                        return true;
                    }
                    Player player = (Player) sender;

                    var playerData = SkyBlockCore.database().getPlayerData(player.getUniqueId());
                    if (playerData.isEmpty()) {
                        SkyBlockCore.Instance.sendCommandReply(player, "General.NoPlayerData", Map.of("player", player.getName()));
                        return true;
                    }
                    SkyBlockCore.Instance.sendCommandReply(sender, "Commands.LevelXP.Get.YourLevel", Map.of(
                                    "level", String.valueOf(playerData.get().getExperience())
                            )
                    );
                    return true;
                }
                case "add": {
                    // Check if the player has permission to use the add command
                    if (!sender.hasPermission("skyblockcore.commands.levelxp.add")) {
                        SkyBlockCore.Instance.sendCommandReply(sender, "General.NoPermission");
                        return true;
                    }

                    if (args.length != 3) {
                        SkyBlockCore.Instance.sendCommandReply(sender, "Commands.Common.InvalidArguments");
                        return true;
                    }

                    OfflinePlayer target = SkyBlockCore.Instance.getServer().getOfflinePlayer(args[1]);
                    if (!target.hasPlayedBefore() && !target.isOnline()) {
                        SkyBlockCore.Instance.sendCommandReply(sender, "Commands.Common.InvalidPlayer", Map.of("player", args[1]));
                        return true;
                    }

                    int amount;
                    try {
                        amount = Integer.parseInt(args[2]);
                    } catch (Exception ex) {
                        SkyBlockCore.Instance.sendCommandReply(sender, "Commands.Common.InvalidNumber", Map.of("number", args[2]));
                        return true;
                    }

                    var playerData = SkyBlockCore.database().getPlayerData(target.getUniqueId()).orElse(null);
                    if (playerData == null) {
                        SkyBlockCore.Instance.sendCommandReply(sender, "General.NoPlayerData", Map.of("player", args[1]));
                        return true;
                    }

                    playerData.setExperience(playerData.getExperience() + amount);
                    SkyBlockCore.database().updatePlayerData(playerData);
                    SkyBlockCore.Instance.sendCommandReply(sender, "Commands.LevelXP.Add.Success", Map.of(
                                    "player", args[1],
                                    "amount", String.valueOf(amount),
                                    "new_level", String.valueOf(playerData.getExperience())
                            )
                    );
                    if (target.isOnline()) {
                        SkyBlockCore.Instance.sendCommandReply(target.getPlayer(), "Commands.LevelXP.Add.NewLevel", Map.of("level", String.valueOf(playerData.getExperience())));
                    }
                    return true;
                }
                case "remove": {
                    // Check if the player has permission to use the remove command
                    if (!sender.hasPermission("skyblockcore.commands.levelxp.remove")) {
                        SkyBlockCore.Instance.sendCommandReply(sender, "General.NoPermission");
                        return true;
                    }

                    if (args.length != 3) {
                        SkyBlockCore.Instance.sendCommandReply(sender, "Commands.Common.InvalidArguments");
                        return true;
                    }

                    OfflinePlayer target = SkyBlockCore.Instance.getServer().getOfflinePlayer(args[1]);
                    if (!target.hasPlayedBefore() && !target.isOnline()) {
                        SkyBlockCore.Instance.sendCommandReply(sender, "Commands.Common.InvalidPlayer", Map.of("player", args[1]));
                        return true;
                    }

                    int amount;
                    try {
                        amount = Integer.parseInt(args[2]);
                    } catch (Exception ex) {
                        SkyBlockCore.Instance.sendCommandReply(sender, "Commands.Common.InvalidNumber", Map.of("number", args[2]));
                        return true;
                    }

                    var playerData = SkyBlockCore.database().getPlayerData(target.getUniqueId()).orElse(null);
                    if (playerData == null) {
                        SkyBlockCore.Instance.sendCommandReply(sender, "General.NoPlayerData", Map.of("player", args[1]));
                        return true;
                    }

                    playerData.setExperience(playerData.getExperience() - amount);
                    SkyBlockCore.database().updatePlayerData(playerData);
                    SkyBlockCore.Instance.sendCommandReply(sender, "Commands.LevelXP.Remove.Success", Map.of(
                                    "player", args[1],
                                    "amount", String.valueOf(amount),
                                    "new_level", String.valueOf(playerData.getExperience())
                            )
                    );
                    if (target.isOnline()) {
                        SkyBlockCore.Instance.sendCommandReply(target.getPlayer(), "Commands.LevelXP.Remove.NewLevel", Map.of("level", String.valueOf(playerData.getExperience())));
                    }
                    return true;
                }
                case "set": {
                    // Check if the player has permission to use the set command
                    if (!sender.hasPermission("skyblockcore.commands.levelxp.set")) {
                        SkyBlockCore.Instance.sendCommandReply(sender, "General.NoPermission");
                        return true;
                    }

                    if (args.length != 3) {
                        SkyBlockCore.Instance.sendCommandReply(sender, "Commands.Common.InvalidArguments");
                        return true;
                    }

                    OfflinePlayer target = SkyBlockCore.Instance.getServer().getOfflinePlayer(args[1]);
                    if (!target.hasPlayedBefore() && !target.isOnline()) {
                        SkyBlockCore.Instance.sendCommandReply(sender, "Commands.Common.InvalidPlayer", Map.of("player", args[1]));
                        return true;
                    }

                    int amount;
                    try {
                        amount = Integer.parseInt(args[2]);
                    } catch (Exception ex) {
                        SkyBlockCore.Instance.sendCommandReply(sender, "Commands.Common.InvalidNumber", Map.of("number", args[2]));
                        return true;
                    }

                    var playerData = SkyBlockCore.database().getPlayerData(target.getUniqueId()).orElse(null);
                    if (playerData == null) {
                        SkyBlockCore.Instance.sendCommandReply(sender, "General.NoPlayerData", Map.of("player", args[1]));
                        return true;
                    }

                    playerData.setExperience(amount);
                    SkyBlockCore.database().updatePlayerData(playerData);
                    SkyBlockCore.Instance.sendCommandReply(sender, "Commands.LevelXP.Set.Success", Map.of(
                                    "player", args[1],
                                    "amount", String.valueOf(amount),
                                    "new_level", String.valueOf(playerData.getExperience())
                            )
                    );
                    if (target.isOnline()) {
                        SkyBlockCore.Instance.sendCommandReply(target.getPlayer(), "Commands.LevelXP.Set.NewLevel", Map.of("level", String.valueOf(playerData.getExperience())));
                    }
                    return true;
                }
                case "reset": {
                    // Check if the player has permission to use the reset command
                    if (!sender.hasPermission("skyblockcore.commands.levelxp.reset")) {
                        SkyBlockCore.Instance.sendCommandReply(sender, "General.NoPermission");
                        return true;
                    }

                    if (args.length != 3) {
                        SkyBlockCore.Instance.sendCommandReply(sender, "Commands.Common.InvalidArguments");
                        return true;
                    }

                    OfflinePlayer target = SkyBlockCore.Instance.getServer().getOfflinePlayer(args[1]);
                    if (!target.hasPlayedBefore() && !target.isOnline()) {
                        SkyBlockCore.Instance.sendCommandReply(sender, "Commands.Common.InvalidPlayer", Map.of("player", args[1]));
                        return true;
                    }

                    int amount;
                    try {
                        amount = Integer.parseInt(args[2]);
                    } catch (Exception ex) {
                        SkyBlockCore.Instance.sendCommandReply(sender, "Commands.Common.InvalidNumber", Map.of("number", args[2]));
                        return true;
                    }

                    var playerData = SkyBlockCore.database().getPlayerData(target.getUniqueId()).orElse(null);
                    if (playerData == null) {
                        SkyBlockCore.Instance.sendCommandReply(sender, "General.NoPlayerData", Map.of("player", args[1]));
                        return true;
                    }

                    playerData.setExperience(0);
                    SkyBlockCore.database().updatePlayerData(playerData);
                    SkyBlockCore.Instance.sendCommandReply(sender, "Commands.LevelXP.Reset.Success", Map.of(
                                    "player", args[1],
                                    "amount", String.valueOf(amount),
                                    "new_level", String.valueOf(playerData.getExperience())
                            )
                    );
                    if (target.isOnline()) {
                        SkyBlockCore.Instance.sendCommandReply(target.getPlayer(), "Commands.LevelXP.Reset.NewLevel");
                    }
                    return true;
                }
            }

            // Send an error message if the subcommand is invalid
            SkyBlockCore.Instance.sendCommandReply(sender, "Commands.Common.InvalidArguments");
            return true;
        }

        // Default to the get subcommand if no arguments are provided
        if (!(sender instanceof Player player)) {
            help(sender, 1);
            return true;
        }

        var playerData = SkyBlockCore.database().getPlayerData(player.getUniqueId());
        if (playerData.isEmpty()) {
            SkyBlockCore.Instance.sendCommandReply(player, "General.NoPlayerData", Map.of("player", player.getName()));
            return true;
        }
        SkyBlockCore.Instance.sendCommandReply(sender, "Commands.LevelXP.Get.YourLevel", Map.of(
                        "level", String.valueOf(playerData.get().getExperience())
                )
        );
        return true;
    }

    /**
     * Displays the help menu for the /levelxp command.
     *
     * @param sender The sender of the command.
     * @param page   The page number of the help menu to display.
     */
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