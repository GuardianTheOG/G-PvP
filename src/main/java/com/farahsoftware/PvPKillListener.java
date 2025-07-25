package com.farahsoftware;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import java.util.List;

public class PvPKillListener implements Listener{
    private final gpvp plugin;
    private PvpRegionListener pvpRegionListener;
    public PvPKillListener(gpvp plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (pvpRegionListener.getPlayerInPvP().contains((player.getUniqueId()))) {
            ItemStack key = new ItemStack(Material.TRIPWIRE_HOOK);
            ItemMeta meta = key.getItemMeta();
            meta.setDisplayName(MiniMessage.miniMessage().stripTags("<green><bold>Level 2 Key <dark_green>1"));
            meta.lore(List.of((MiniMessage.miniMessage().deserialize("<aqua>This key allows access to Level 2"))));
            meta.getPersistentDataContainer().set( new NamespacedKey(plugin, "Access_Card"), PersistentDataType.BOOLEAN, true);
            key.setItemMeta(meta);

            player.getWorld().dropItemNaturally(player.getLocation(), key);
        }

    }

}
