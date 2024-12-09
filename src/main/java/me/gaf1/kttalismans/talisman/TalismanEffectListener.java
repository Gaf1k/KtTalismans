package me.gaf1.kttalismans.talisman;

import me.gaf1.kttalismans.utils.ChatUtil;
import me.gaf1.kttalismans.utils.ConfigManager;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TalismanEffectListener implements Listener {
    private final YamlConfiguration config = ConfigManager.instance.configs.get("talismans.yml");
    private final Map<Player, List<PotionEffect>> playerEffects = new HashMap<>();
    private final TalismanManager tManager = new TalismanManager();

    @EventHandler
    public void onOffHand(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        List<PotionEffect> effectList = new ArrayList<>();

        // Обработка, когда предмет в левой руке является амулетом
        if (event.getOffHandItem().getType() == Material.TOTEM_OF_UNDYING) {
            if (!event.getOffHandItem().hasItemMeta()) {
                return;
            }

            boolean isTalisman = event.getOffHandItem().getItemMeta().getPersistentDataContainer().getKeys().stream()
                    .anyMatch(nsk -> config.getKeys(false).stream().anyMatch(nsk.toString()::contains));

            String id = isTalisman
                    ? config.getKeys(false).stream()
                    .filter(key -> event.getOffHandItem().getItemMeta().getPersistentDataContainer().getKeys().stream()
                            .anyMatch(nsk -> nsk.toString().contains(key)))
                    .findFirst()
                    .orElse(null)
                    : null;

            if (!isTalisman || id == null) {
                return;
            }

            for (String key: config.getConfigurationSection(id + ".effects").getKeys(false)) {
                effectList.add(new PotionEffect(PotionEffectType.getById(tManager.getMapEffects().get(key.toLowerCase())), Integer.MAX_VALUE, config.getInt(id + ".effects." + key) - 1, false, false, false));
            }

            for (PotionEffect effect : effectList) {
                player.addPotionEffect(effect);
            }
            playerEffects.put(player, effectList);

        } else if (event.getMainHandItem().getType() == Material.TOTEM_OF_UNDYING) {
            if (!event.getMainHandItem().hasItemMeta()) {
                return;
            }

            boolean isTalisman = event.getMainHandItem().getItemMeta().getPersistentDataContainer().getKeys().stream()
                    .anyMatch(nsk -> config.getKeys(false).stream().anyMatch(nsk.toString()::contains));

            if (!isTalisman) {
                return;
            }

            if (playerEffects.containsKey(player)) {
                for (PotionEffect effect : playerEffects.get(player)) {
                    player.removePotionEffect(effect.getType());
                }
                playerEffects.remove(player);
            }
        }
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem(); // Это предмет, на который кликнул игрок
        ItemStack cursorItem = event.getCursor(); // Это предмет, который игрок пытается переместить
        ItemStack offHandItem = player.getInventory().getItemInOffHand();

        if (event.getClick() == ClickType.NUMBER_KEY) {
            int hotbarSlot = event.getHotbarButton(); // Индекс хотбара (0-8)
            ItemStack hotbarItem = player.getInventory().getItem(hotbarSlot); // Предмет из хотбара

            // Перемещение из хотбара в левую руку
            if (event.getSlot() == 40) { // Слот 40 — левая рука (offhand)
                if (hotbarItem != null && hotbarItem.getType() == Material.TOTEM_OF_UNDYING && hotbarItem.hasItemMeta()) {
                    boolean isTalisman = hotbarItem.getItemMeta().getPersistentDataContainer().getKeys().stream()
                            .anyMatch(nsk -> config.getKeys(false).stream().anyMatch(nsk.toString()::contains));

                    if (isTalisman) {
                        List<PotionEffect> effectList = new ArrayList<>();
                        String id = config.getKeys(false).stream()
                                .filter(key -> hotbarItem.getItemMeta().getPersistentDataContainer().getKeys().stream()
                                        .anyMatch(nsk -> nsk.toString().contains(key)))
                                .findFirst()
                                .orElse(null);

                        if (id != null) {
                            for (String key : config.getConfigurationSection(id + ".effects").getKeys(false)) {
                                effectList.add(new PotionEffect(
                                        PotionEffectType.getById(tManager.getMapEffects().get(key.toLowerCase())),
                                        Integer.MAX_VALUE,
                                        config.getInt(id + ".effects." + key) - 1,
                                        false, false, false
                                ));
                            }

                            for (PotionEffect effect : effectList) {
                                player.addPotionEffect(effect);
                            }
                            playerEffects.put(player, effectList);
                        }
                    }
                }
            }

            // Перемещение из левой руки в хотбар
            if (event.getSlot() == 40) {// Слоты хотбара (0-8)
                if (offHandItem != null && offHandItem.getType() == Material.TOTEM_OF_UNDYING && offHandItem.hasItemMeta()) {
                    boolean wasTalisman = offHandItem.getItemMeta().getPersistentDataContainer().getKeys().stream()
                            .anyMatch(nsk -> config.getKeys(false).stream().anyMatch(nsk.toString()::contains));

                    if (wasTalisman) {
                        if (playerEffects.containsKey(player)) {
                            for (PotionEffect effect : playerEffects.get(player)) {
                                player.removePotionEffect(effect.getType());
                            }
                            playerEffects.remove(player);
                        }
                    }
                }
            }
            return;
        }

        if (event.getClick() == ClickType.SWAP_OFFHAND) {

            // Проверяем, был ли раньше в левой руке не амулет и теперь там амулет
            if (clickedItem != null && clickedItem.getType() == Material.TOTEM_OF_UNDYING && clickedItem.hasItemMeta()) {
                boolean isTalisman = clickedItem.getItemMeta().getPersistentDataContainer().getKeys().stream()
                        .anyMatch(nsk -> config.getKeys(false).stream().anyMatch(nsk.toString()::contains));

                if (isTalisman) {
                    // Применяем эффекты, если в левой руке оказался амулет
                    List<PotionEffect> effectList = new ArrayList<>();
                    String id = config.getKeys(false).stream()
                            .filter(key -> clickedItem.getItemMeta().getPersistentDataContainer().getKeys().stream()
                                    .anyMatch(nsk -> nsk.toString().contains(key)))
                            .findFirst()
                            .orElse(null);

                    if (id != null) {
                        for (String key : config.getConfigurationSection(id + ".effects").getKeys(false)) {
                            effectList.add(new PotionEffect(PotionEffectType.getById(tManager.getMapEffects().get(key.toLowerCase())), Integer.MAX_VALUE, config.getInt(id + ".effects." + key) - 1, false, false, false));
                        }

                        for (PotionEffect effect : effectList) {
                            player.addPotionEffect(effect);
                        }
                        playerEffects.put(player, effectList);
                    }
                }
            } else {
                // Если в левой руке был амулет, а теперь его там нет, удаляем эффекты
                if (offHandItem != null && offHandItem.getType() == Material.TOTEM_OF_UNDYING && offHandItem.hasItemMeta()) {
                    boolean wasTalisman = offHandItem.getItemMeta().getPersistentDataContainer().getKeys().stream()
                            .anyMatch(nsk -> config.getKeys(false).stream().anyMatch(nsk.toString()::contains));

                    if (wasTalisman) {
                        // Убираем эффекты, если амулет был убран из левой руки
                        if (playerEffects.containsKey(player)) {
                            for (PotionEffect effect : playerEffects.get(player)) {
                                player.removePotionEffect(effect.getType());
                            }
                            playerEffects.remove(player);
                        }
                    }
                }
            }
        }

        // Дополнительная проверка для обычного перемещения предметов в левую руку (слот 40)
        if (event.getSlot() == 40) { // Слот для левой руки (offhand) всегда индекс 40
            if (cursorItem != null && cursorItem.getType() == Material.TOTEM_OF_UNDYING && cursorItem.hasItemMeta()) {
                boolean isTalisman = cursorItem.getItemMeta().getPersistentDataContainer().getKeys().stream()
                        .anyMatch(nsk -> config.getKeys(false).stream().anyMatch(nsk.toString()::contains));

                if (isTalisman) {
                    List<PotionEffect> effectList = new ArrayList<>();
                    String id = config.getKeys(false).stream()
                            .filter(key -> cursorItem.getItemMeta().getPersistentDataContainer().getKeys().stream()
                                    .anyMatch(nsk -> nsk.toString().contains(key)))
                            .findFirst()
                            .orElse(null);

                    if (id != null) {
                        for (String key : config.getConfigurationSection(id + ".effects").getKeys(false)) {
                            effectList.add(new PotionEffect(PotionEffectType.getById(tManager.getMapEffects().get(key.toLowerCase())), Integer.MAX_VALUE, config.getInt(id + ".effects." + key) - 1, false, false, false));
                        }

                        for (PotionEffect effect : effectList) {
                            player.addPotionEffect(effect);
                        }
                        playerEffects.put(player, effectList);
                    }
                }
            } else {
                // Если в левую руку помещен не амулет или рука пустая, убираем эффекты
                if (playerEffects.containsKey(player)) {
                    for (PotionEffect effect : playerEffects.get(player)) {
                        player.removePotionEffect(effect.getType());
                    }
                    playerEffects.remove(player);
                }
            }
        }
    }

}


