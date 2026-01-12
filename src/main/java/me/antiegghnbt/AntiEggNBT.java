package me.antiegghnbt;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;

public class AntiEggNBT extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("AntiEggNBT enabled");
    }

    @EventHandler
    public void onUseEgg(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR &&
            event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (item == null) return;

        if (!item.getType().name().endsWith("_SPAWN_EGG")) return;

        ItemMeta meta = item.getItemMeta();

        // ❌ Любой spawn egg с метой (NBT / кастом)
        if (meta != null && (
                meta.hasCustomModelData()
                || meta.hasDisplayName()
                || meta.hasLore()
        )) {

            event.setCancelled(true);

            Player p = event.getPlayer();
            p.sendMessage("§cЭтот spawn egg запрещён.");

            getLogger().warning(
                    "Blocked NBT spawn egg from " + p.getName()
            );
        }
    }
}
