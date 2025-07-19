package com.farahsoftware;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class gpvpCommand implements CommandExecutor, TabCompleter {

    private final FileConfiguration config;
    private final gpvp plugin;

    public gpvpCommand(gpvp plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<dark_red>You are not authorized to run this command."));
            return true;
        }

        if (!player.isOp()) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<dark_red>You don't have the permission to run this command."));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<dark_green>Use /gpvp help for help."));
            return true;
        }

        String subcommand = args[0].toLowerCase();

        switch (subcommand) {

            case "addkit":
                if (!player.hasPermission("gpvp.admin")) {
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<dark_red>You do not have permission to run this command."));
                    return true;
                }
                return handleAddKit(player, args);

            case "setcheckpoint":
                if (!player.hasPermission("gpvp.admin")) {
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<dark_red>You do not have permission to run this command."));
                    return true;
                }
                return handlesetcheckpoint(player, args);

            case "getloot":
                if (!player.hasPermission("gpvp.loot")) {
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<dark_red>You do not have permission to run this command."));
                    return true;
                }
                return handlegetloot(player);

            case "help":
                return handleHelp(player);

            default:
                player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>User /gpvp help for help."));
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            if (sender.hasPermission("gpvp.loot")) {
                if ("help".startsWith(args[0].toLowerCase())) suggestions.add("help");
                if ("getloot".startsWith(args[0].toLowerCase())) suggestions.add("getloot");
            }
            if (sender.hasPermission("gpvp.admin")) {
                if ("addkit".startsWith(args[0].toLowerCase())) suggestions.add("addkit");
                if ("setcheckpoint".startsWith(args[0].toLowerCase())) suggestions.add("setcheckpoint");
            }
        }

        else if (args.length == 2 && args[0].equalsIgnoreCase("setcheckpoint")) {
            if (sender.hasPermission("gpvp.admin")) {
                if ("start".startsWith(args[1].toLowerCase())) suggestions.add("start");
                if ("stop".startsWith(args[1].toLowerCase())) suggestions.add("stop");
            }
        }

        return suggestions;
    }


    private boolean handleAddKit(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Usage: gpvp addkit <kit_name>"));
            return true;
        }

        String kitName = args[1].toLowerCase();

        ItemStack[] contents = player.getInventory().getContents();
        List<Map<String, Object>> serializeditems = new ArrayList<>();

        for (ItemStack items : contents) {
            if (items != null) {
                serializeditems.add(items.serialize());
            } else {
                serializeditems.add(null);
            }
        }

        config.set("Kits." + kitName, serializeditems);
        plugin.saveConfig();
        player.sendMessage(MiniMessage.miniMessage().deserialize("<dark_green>Kit " + kitName + " has been added to config.yml"));
        return true;
    }

    private boolean handlesetcheckpoint(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Usage: /gpvp setcheckpoint <start | stop>"));
            return true;
        }

        String action = args[1].toLowerCase();

        if (action.equals("start")) {
            ItemStack chest = new ItemStack((Material.CHEST));
            chest.getItemMeta().setDisplayName("Check Point Setter");
            player.getInventory().addItem(chest);
            player.sendMessage(MiniMessage.miniMessage().deserialize("<dark_green>Place the chest where you want to set a block for PvP'er to svae their loot."));
        } else if (action.equals("stop")) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<color:#ff8800>Checkpoint saved to config.yml. Please confirm locations have been saved."));
        } else {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<dark_red>Invalid argument: Usage /gpvp setcheckpoint <start | stop>"));
        }

        return true;
    }

    private boolean handlegetloot(Player player) {
        player.sendMessage(MiniMessage.miniMessage().deserialize("<white><bold>To be handled in the future."));
        return true;
    }

    private boolean handleHelp(Player player) {
        player.sendMessage(MiniMessage.miniMessage().deserialize("<green><<<<< G-PvP Help >>>>>"));
        player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>/gpvp help: <white>Show this help menu"));
        player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>/gpvp getloot: <white>Open GUI with loot saved in checkpoint chests in PvP."));
        if (player.hasPermission("gpvp.admin")) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>/gpvp addkit <kit_name>: <white>Add kit to config.yml to be given to players in PvP."));
            player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>/gpvp setcheckpoint <start | stop>: <white>Set checkpoint chest for players to save their loot in PvP"));
        }
        return true;
    }

}