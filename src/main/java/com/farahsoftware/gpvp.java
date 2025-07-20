package com.farahsoftware;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.UUID;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;



public class gpvp extends JavaPlugin {
    private BukkitAudiences adventure;
    public PvpRegionListener PvPRegionListener;

    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        saveDefaultConfig();
        PvPRegionListener = new PvpRegionListener(this);
        getServer().getPluginManager().registerEvents(PvPRegionListener, this);
        this.adventure = BukkitAudiences.create(this);
        this.adventure().console().sendMessage(MiniMessage.miniMessage().deserialize("<dark_green><bold><<<<< Plugin enabled >>>>>"));
        getServer().getPluginManager().registerEvents(new PvpRegionListener(this), this);
        getServer().getPluginManager().registerEvents(new PvPKillListener(this), this);
        this.getCommand("gpvp").setExecutor(new gpvpCommand(this));
        this.getCommand("gpvp").setTabCompleter(new gpvpCommand(this));
        PvPRegionListener.loadPvPState();

    }
    public void onDisable() {
        for (UUID uuid : PvPRegionListener.getPlayerInPvP()) {
            PvPRegionListener.savePvPState(uuid, true);
        }
        this.adventure().console().sendMessage(MiniMessage.miniMessage().deserialize("<dark_red><bold><<<<< Plugin disables >>>>>"));
        //if (this.adventure != null) this.adventure.close();
    }

    public BukkitAudiences adventure() {
        return adventure;
    }

}
