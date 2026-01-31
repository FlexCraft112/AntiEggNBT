package me.antiegghnbt;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
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
       ПРОВЕРКА — ЯЙЦО С NBT
       =============================== */
    private boolean isNBTSpawnEgg(ItemStack item) {
        if (item == null) return false;
        if (!item.getType().name().endsWith("_SPAWN_EGG")) return false;
        if (!item.hasItemMeta()) return false;
        // Как в твоём оригинале — любое яйцо с NBT > 2 символов
        return item.getItemMeta().getAsString().length() > 2;
    }

    /* ===============================
       ОСНОВНАЯ ЛОГИКА — ТОЛЬКО ПРИ СПАВНЕ
       =============================== */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onUseEgg(PlayerInteractEvent event) {
        // Отменяем только если это действие спавна (ПКМ по блоку или воздуху)
        if (!event.getAction().toString().contains("RIGHT_CLICK")) return;

        ItemStack item = event.getItem();
        if (item == null) return;
        if (!isNBTSpawnEgg(item)) return;

        // Блокируем спавн + мгновенно удаляем яйцо
        event.setCancelled(true);
        event.getPlayer().getInventory().remove(item);
        event.getPlayer().updateInventory(); // синхронизация

        event.getPlayer().sendMessage("§cЗапрещено спавнить мобов из NBT-яиц! Яйцо удалено.");
    }
}
