package me.antiegghnbt;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class AntiEggNBT extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("AntiEggNBT включён — яйца с NBT можно носить, но спавнить нельзя");
    }

    /* ===============================
       УНИВЕРСАЛЬНАЯ ПРОВЕРКА ЯЙЦА
       =============================== */
    private boolean isNBTSpawnEgg(ItemStack item) {
        if (item == null) return false;
        if (!item.getType().name().endsWith("_SPAWN_EGG")) return false;
        if (!item.hasItemMeta()) return false;
        // Как в твоём оригинале — любое яйцо с NBT > 2 символов
        return item.getItemMeta().getAsString().length() > 2;
    }

    /* ===============================
       ОСНОВНАЯ ЛОГИКА — ПРАВАЯ И ЛЕВАЯ РУКА
       =============================== */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onUseEgg(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        ItemStack mainHand = event.getItem();               // правая рука
        ItemStack offHand = player.getInventory().getItemInOffHand();  // левая рука

        boolean removed = false;

        // Правая рука (основная логика спавна)
        if (mainHand != null && isNBTSpawnEgg(mainHand)) {
            // Любое ПКМ (блок или воздух) → блокируем и удаляем
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK || 
                event.getAction() == Action.RIGHT_CLICK_AIR) {
                
                event.setCancelled(true);
                player.getInventory().remove(mainHand);
                player.updateInventory();
                removed = true;
            }
        }

        // Левая рука (offhand) — удаляем при любой попытке взаимодействия правой рукой
        if (offHand != null && isNBTSpawnEgg(offHand)) {
            // Если игрок делает ПКМ правой рукой — чистим offhand
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK || 
                event.getAction() == Action.RIGHT_CLICK_AIR) {
                
                player.getInventory().setItemInOffHand(null);
                player.updateInventory();
                removed = true;
            }
        }

        // Общее сообщение, если удалили что-то
        if (removed) {
            player.sendMessage("§cNBT-яйцо удалено из инвентаря (запрещено спавнить)");
        }
    }
}
