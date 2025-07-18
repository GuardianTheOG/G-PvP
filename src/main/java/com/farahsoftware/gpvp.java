package com.farahsoftware;

import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.protection.RegionResultSet;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.Set;
import java.util.UUID;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;


public class gpvp extends JavaPlugin {
    private BukkitAudiences adventure;
    public PvpRegionListener PvPRegionListener;
    public PvPKillListener PvPKillListener;

    @Override
    public void onEnable() {
        PvPRegionListener = new PvpRegionListener(this);
        getServer().getPluginManager().registerEvents(PvPRegionListener, this);

        this.adventure().console().sendMessage(MiniMessage.miniMessage().deserialize("<dark_green><bold><<<<< Plugin enabled >>>>>"));
        getServer().getPluginManager().registerEvents(new PvpRegionListener(this), this);
        getServer().getPluginManager().registerEvents(new PvPKillListener(this), this);
        this.getCommand("addpvpkit").setExecutor(new AddKitCommand(this));
        this.adventure = BukkitAudiences.create(this);
        PvPRegionListener.loadPvPState();

    }
    public void onDisable() {
        for (UUID uuid : PvPRegionListener.getPlayerInPvP()) {
            PvPRegionListener.savePvPState(uuid, true);
        }
        this.adventure().console().sendMessage(MiniMessage.miniMessage().deserialize("<dark_red><bold><<<<< Plugin disables >>>>>"));
        if (this.adventure != null) this.adventure.close();
    }

    public BukkitAudiences adventure() {
        return adventure;
    }

}
