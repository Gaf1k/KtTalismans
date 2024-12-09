package me.gaf1.kttalismans.talisman;

import me.gaf1.kttalismans.Plugin;
import me.gaf1.kttalismans.utils.ConfigManager;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class TalismanRecoveryListener implements Listener {
    private final YamlConfiguration config = ConfigManager.instance.configs.get("talismans.yml");
    private final TalismanManager tManager = new TalismanManager();
    @EventHandler
    public void onRepair(PrepareAnvilEvent event){
        AnvilInventory anvil = event.getInventory();
        ItemStack firstStack = anvil.getFirstItem();
        ItemStack secondStack = anvil.getSecondItem();
        if (firstStack == null || secondStack == null) {
            event.setResult(new ItemStack(Material.AIR));
            return;
        }

        if (!firstStack.hasItemMeta()){
            return;
        }

        boolean isTalisman = firstStack.getItemMeta().getPersistentDataContainer().getKeys().stream()
                .anyMatch(nsk -> config.getKeys(false).stream().anyMatch(nsk.toString()::contains));

        String id = isTalisman
                ? config.getKeys(false).stream()
                .filter(key -> firstStack.getItemMeta().getPersistentDataContainer().getKeys().stream()
                        .anyMatch(nsk -> nsk.toString().contains(key)))
                .findFirst()
                .orElse(null)
                : null;

        if (!isTalisman || id == null) {
            return;
        }
        String status = firstStack.getItemMeta().getPersistentDataContainer().get(NamespacedKey.fromString("talisman_"+id), PersistentDataType.STRING);
        if (status.equals("true")){
            return;
        }

        String materialName = Plugin.getInstance().getConfig().getString("Repair.item_for_repair.material");
        if (materialName == null || Material.matchMaterial(materialName) == null) {
            return;
        }

        Material repairMaterial = Material.matchMaterial(materialName);
        int repairAmount = Plugin.getInstance().getConfig().getInt("Repair.item_for_repair.amount", 1);
        if (secondStack.getType() != repairMaterial || secondStack.getAmount() != repairAmount) {
            return;
        }
        anvil.setRepairCost(Plugin.getInstance().getConfig().getInt("Repair.levels"));
        anvil.setResult(tManager.getTalisman(id));
        event.setResult(tManager.getTalisman(id));
    }

}
