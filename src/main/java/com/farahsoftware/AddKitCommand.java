package com.farahsoftware;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;

public class AddKitCommand implements CommandExecutor{
    private final gpvp plugin;

    public AddKitCommand(gpvp plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.isOp() || !(sender instanceof Player player)) {
            sender.sendMessage("&4You do not have permission to run this command.");
            return true;
        }
        if (args.length != 1) {
            player.sendMessage("&cUsage: /addpvpkit <kit_name>");
            return true;
        }
        String kit = args[0].toLowerCase();
        List<String> items = new ArrayList<>();
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                items.add(item.getType().name());
            }

            FileConfiguration config = plugin.getConfig();
            config.set("Kits." + kit, items);
            plugin.saveConfig();

            player.sendMessage("&cKit for layer " + kit + " saved to config");
            return true;
        }
        return false;
    }
}

