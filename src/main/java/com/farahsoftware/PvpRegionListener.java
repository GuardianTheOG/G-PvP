package com.farahsoftware;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class PvpRegionListener implements Listener {

    private final gpvp plugin;
    private final Set<UUID> playersInPvP = new HashSet<>();
    private File PvPStateFile;
    private YamlConfiguration PvPStateConfig;

    public PvpRegionListener(gpvp plugin) {
        this.plugin = plugin;
    }

    public Set<UUID> getPlayerInPvP() {
        return playersInPvP;
    }

    private void saveInventoryToFile(Player player) {
        File folder = new File(plugin.getDataFolder(), "Inventories");
        if (!folder.exists()) folder.mkdirs();

        File file = new File(folder, player.getUniqueId() + ".yml");
        YamlConfiguration config = new YamlConfiguration();

        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null) {
                config.set("Inventory." + i, contents[i]);
            }
        }

        ItemStack[] armor = player.getInventory().getArmorContents();
        for (int i = 0; i < armor.length; i++) {
            if (armor[i] != null) {
                config.set("Armor." + i, armor[i]);
            }
        }

        ItemStack offhand = player.getInventory().getItemInOffHand();
        if (offhand != null && offhand.getType() != Material.AIR) {
            config.set("OffHand", offhand);
        }

        try {
            config.save(file);
        }
        catch (IOException e) {
            plugin.adventure().console().sendMessage(MiniMessage.miniMessage().deserialize("<dark_red>Inventory could not be saved for " + player.getName()));
            e.printStackTrace();
        }
    }

    private void restoreInventoryFromFile(Player player) {
        File file = new File(plugin.getDataFolder(), "Inventories/" + player.getUniqueId() + ".yml");

        if (!file.exists()) {
            plugin.adventure().console().sendMessage(MiniMessage.miniMessage().deserialize("<dark_red>Player inventory file does not exist for player " + player.getName()));
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        if (config.contains("Inventory")) {
            ItemStack[] contents = new ItemStack[36];
            for (String key : config.getConfigurationSection("Inventory").getKeys(false)) {
                int slot = Integer.parseInt(key);
                contents[slot] = config.getItemStack("Inventory." + key);
            }
            player.getInventory().setContents(contents);
        }

        if (config.contains("Armor")) {
            ItemStack[] armor = new ItemStack[4];
            for (String key : config.getConfigurationSection("Armor").getKeys(false)) {
                int slot = Integer.parseInt(key);
                armor[slot] = config.getItemStack("Armor." + key);
            }
            player.getInventory().setArmorContents(armor);
        }
        if (config.contains("OffHand"))  {
            player.getInventory().setItemInOffHand(config.getItemStack("OffHand"));
        }

        file.delete();
    }

    private void givePvPKit(Player player, String kitName) {
        List<String> itemNames = plugin.getConfig().getStringList("Kits." + kitName);
        for (String name : itemNames) {
            try {
                Material mat = Material.valueOf(name.toUpperCase());
                player.getInventory().addItem(new ItemStack(mat));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid item in kit " + kitName + ": " + name);
            }
        }
    }

    public void loadPvPState() {
        PvPStateFile = new File(plugin.getDataFolder(), "InPvP.yml");
        if (!PvPStateFile.exists()) {
            try {
                PvPStateFile.createNewFile();
            }
            catch (IOException e) {
                plugin.adventure().console().sendMessage( MiniMessage.miniMessage().deserialize("<dark_red>InPvP.yml could not be created."));
                e.printStackTrace();
            }
        }
        PvPStateConfig = YamlConfiguration.loadConfiguration(PvPStateFile);
    }

    public void savePvPState(UUID uuid, boolean inPvP) {
        if (inPvP) {
            PvPStateConfig.set(uuid.toString(), true);
        } else {
            PvPStateConfig.set(uuid.toString(), null);
        }
        try {
            PvPStateConfig.save(PvPStateFile);
        }
        catch (IOException e) {
            plugin.adventure().console().sendMessage(MiniMessage.miniMessage().deserialize("<dark_red>InPvP.ylm could not be saved."));
            e.printStackTrace();
        }
    }

    private boolean wasInPvP(UUID uuid) {
        return PvPStateConfig.getBoolean(uuid.toString(), false);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlock().equals(event.getTo().getBlock()))
            return;

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        com.sk89q.worldedit.util.Location weLoc = BukkitAdapter.adapt(player.getLocation());
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get((World) weLoc.getExtent());

        if (regions == null) return;

        ApplicableRegionSet regionSet = regions.getApplicableRegions((weLoc.toVector().toBlockPoint()));
        String configuredRegion = plugin.getConfig().getString("PvPRegion");

        boolean isInPvP = regionSet.getRegions().stream()
                .anyMatch(r -> r.getId().equalsIgnoreCase(configuredRegion));

        if (isInPvP && !playersInPvP.contains(uuid)) {
            playersInPvP.add(uuid);
            savePvPState(uuid, true);
            player.sendMessage(MiniMessage.miniMessage().deserialize((plugin.getConfig().getString("EnterMessage"))));
            saveInventoryToFile(player);
            player.getInventory().clear();
            givePvPKit(player, "kit1"); // We'll assume this is kits.kit1 in config
        }

        if (!isInPvP && playersInPvP.contains(uuid)) {
            playersInPvP.remove(uuid);
            savePvPState(uuid, false);
            player.getInventory().clear();
            restoreInventoryFromFile(player);
            player.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfig().getString("ExitMessage")));
        }
    }

}
