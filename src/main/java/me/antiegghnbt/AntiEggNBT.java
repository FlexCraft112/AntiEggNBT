package me.antiegghnbt;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AntiEggNBT extends JavaPlugin implements Listener {

    private final Set<UUID> notified = new HashSet<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("AntiEggNBT включен — NBT-яйца полностью блокируются и удаляются");

        // Периодическая проверка инвентарей всех игроков
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    checkAndRemoveIllegalEggs(player, false);
                }
            }
        }.runTaskTimer(this, 20L, 20L); // каждую секунду
    }

    /**
     * Самая надёжная проверка: яйцо с NBT длиннее, чем у ванильного
     */
    private boolean isIllegalNBTSpawnEgg(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        String typeName = item.getType().name();
        if (!typeName.endsWith("_SPAWN_EGG")) return false;

        // Ванильное яйцо почти всегда имеет очень короткий NBT
        if (!item.hasItemMeta()) return false;

        String metaString = item.getItemMeta().getAsString();
        // Ванильные яйца обычно имеют длину NBT ~20–60 символов
        // Если сильно больше — почти 100% модифицированное
        return metaString.length() > 80;
    }

    /**
     * Проверяет и удаляет нелегальные яйца из всего инвентаря игрока
     * @param player игрок
     * @param notify отправлять сообщение или нет
     * @return true — если было удалено хоть одно яйцо
     */
    private boolean checkAndRemoveIllegalEggs(Player player, boolean notify) {
        if (player == null || !player.isOnline()) return false;

        PlayerInventory inv = player.getInventory();
        boolean removed = false;

        // Проверяем весь инвентарь + экипировку + offhand
        ItemStack[] contents = inv.getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (isIllegalNBTSpawnEgg(item)) {
                inv.setItem(i, null);
                removed = true;
            }
        }

        // Шлем, нагрудник, поножи, ботинки
        ItemStack[] armor = inv.getArmorContents();
        for (int i = 0; i < armor.length; i++) {
            if (isIllegalNBTSpawnEgg(armor[i])) {
                armor[i] = null;
                removed = true;
            }
        }
        inv.setArmorContents(armor);

        // Offhand
        if (isIllegalNBTSpawnEgg(inv.getItemInOffHand())) {
            inv.setItemInOffHand(null);
            removed = true;
        }

        if (removed && notify && !notified.contains(player.getUniqueId())) {
            player.sendMessage("§cОбнаружены запрещённые NBT-яйца — они были удалены из вашего инвентаря.");
            notified.add(player.getUniqueId());
            getLogger().warning("[AntiEggNBT] У игрока " + player.getName() + " удалены нелегальные NBT-яйца");
        }

        return removed;
    }

    // ────────────────────────────────────────────────
    //                СОБЫТИЯ
    // ────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        // Проверяем сразу при входе
        Bukkit.getScheduler().runTaskLater(this, () -> 
            checkAndRemoveIllegalEggs(e.getPlayer(), true), 20L);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (!e.hasItem()) return;
        if (isIllegalNBTSpawnEgg(e.getItem())) {
            e.setCancelled(true);
            e.getPlayer().getInventory().remove(e.getItem());
            e.getPlayer().updateInventory();
            checkAndRemoveIllegalEggs(e.getPlayer(), true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();

        boolean changed = false;

        // Проверяем курсор
        if (isIllegalNBTSpawnEgg(e.getCursor())) {
            e.setCursor(null);
            changed = true;
        }

        // Проверяем текущий предмет
        if (isIllegalNBTSpawnEgg(e.getCurrentItem())) {
            e.setCurrentItem(null);
            changed = true;
        }

        if (changed) {
            e.setCancelled(true);
            checkAndRemoveIllegalEggs(p, true);
            p.updateInventory();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCreativeInventory(InventoryCreativeEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();

        if (isIllegalNBTSpawnEgg(e.getCursor())) {
            e.setCursor(null);
            e.setCancelled(true);
            checkAndRemoveIllegalEggs(p, true);
        }
    }
}
