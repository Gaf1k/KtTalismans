package me.gaf1.kttalismans.talisman;

import me.gaf1.kttalismans.Plugin;
import me.gaf1.kttalismans.utils.ChatUtil;
import me.gaf1.kttalismans.utils.ConfigManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TalismanManager {
    private final YamlConfiguration talismanCfg = ConfigManager.instance.configs.get("talismans.yml");

    public void createTalisman(String id, CommandSender sender){
        if (talismanCfg.getKeys(false).contains(id)){
            ChatUtil.sendConfigMessage(sender,"Messages.talisman_exist");
            return;
        }
        talismanCfg.createSection(id);
        talismanCfg.set(id+".name", "&fНазвание");
        talismanCfg.set(id+".lore",List.of("&fОписание"));
        talismanCfg.set(id+".itemflags", List.of("HIDE_ENCHANTS","HIDE_ATTRIBUTES"));
        talismanCfg.set(id+".effects.STRENGTH",2);
        talismanCfg.set(id+".attributes.1.type", "GENERIC_ATTACK_DAMAGE");
        talismanCfg.set(id+".attributes.1.operation","ADD_NUMBER");
        talismanCfg.set(id+".attributes.1.amount",1.0);

        ConfigManager.instance.configs.put("talismans.yml",talismanCfg);

        try {
            talismanCfg.save(new File(Plugin.getInstance().getDataFolder().getAbsolutePath() + "/talismans.yml"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ChatUtil.sendConfigMessage(sender,"Messages.talisman_created");
    }
    public ItemStack getTalisman(String id){

        ItemStack itemStack = new ItemStack(Material.TOTEM_OF_UNDYING);
        ItemMeta meta = itemStack.getItemMeta();

        String displayName = talismanCfg.getString(id + ".name");
        if (displayName.contains("%status%")) {
            meta.displayName(ChatUtil.color(displayName.replace("%status%", Plugin.getInstance().getConfig().getString("Messages.talisman_active"))));
        }
        else {
            meta.displayName(ChatUtil.color(displayName));
        }
        List<Component> lore = new ArrayList<>();
        for (String loreText : talismanCfg.getStringList(id + ".lore")) {
            if (loreText.contains("%status%")){
                lore.add(ChatUtil.color(loreText.replace("%status%", Plugin.getInstance().getConfig().getString("Messages.talisman_active"))));
                continue;
            }
            lore.add(ChatUtil.color(loreText));
        }
        meta.lore(lore);

        meta.addEnchant(Enchantment.DURABILITY,1,true);

        ConfigurationSection attributes = talismanCfg.getConfigurationSection(id+".attributes");
        if (!attributes.getKeys(false).isEmpty()) {
            for (String key : attributes.getKeys(false)) {
                Attribute attribute = Attribute.valueOf(talismanCfg.getString(id + ".attributes." + key + ".type"));
                AttributeModifier.Operation operation = AttributeModifier.Operation.valueOf(talismanCfg.getString(id + ".attributes." + key + ".operation"));
                double amount = talismanCfg.getDouble(id + ".attributes." + key + ".amount");
                meta.addAttributeModifier(attribute, new AttributeModifier(UUID.randomUUID(),attribute.toString(),amount,operation,EquipmentSlot.OFF_HAND));
            }
        }
        for (String flag: talismanCfg.getStringList(id+".itemflags")){
            meta.addItemFlags(ItemFlag.valueOf(flag));
        }

        meta.getPersistentDataContainer().set(NamespacedKey.fromString("talisman_"+id), PersistentDataType.STRING,"true");

        itemStack.setItemMeta(meta);

        return itemStack;

    }

    public ItemStack getDestroyedTalisman(String id) {

        ItemStack itemStack = new ItemStack(Material.TOTEM_OF_UNDYING);
        ItemMeta meta = itemStack.getItemMeta();

        String displayName = talismanCfg.getString(id + ".name");
        if (displayName.contains("%status%")) {
            meta.displayName(ChatUtil.color(displayName.replace("%status%", Plugin.getInstance().getConfig().getString("Messages.talisman_destroyed"))));
        }
        else {
            meta.displayName(ChatUtil.color(displayName));
        }
        List<Component> lore = new ArrayList<>();
        for (String loreText : talismanCfg.getStringList(id + ".lore")) {
            if (loreText.contains("%status%")){
                lore.add(ChatUtil.color(loreText.replace("%status%", Plugin.getInstance().getConfig().getString("Messages.talisman_destroyed"))));
                continue;
            }
            lore.add(ChatUtil.color(loreText));
        }
        meta.lore(lore);

        for (String flag : talismanCfg.getStringList(id + ".itemflags")) {
            meta.addItemFlags(ItemFlag.valueOf(flag));
        }

        meta.getPersistentDataContainer().set(NamespacedKey.fromString("talisman_" + id), PersistentDataType.STRING, "false");

        itemStack.setItemMeta(meta);

        return itemStack;
    }

    public Map<String, Integer> getMapEffects(){
        Map<String, Integer> effects = new HashMap<>();
        effects.put("speed", 1);
        effects.put("slowness", 2);
        effects.put("haste", 3);
        effects.put("mining_fatigue", 4);
        effects.put("strength", 5);
        effects.put("instant_health", 6);
        effects.put("instant_damage", 7);
        effects.put("jump_boost", 8);
        effects.put("nausea", 9);
        effects.put("regeneration", 10);
        effects.put("resistance", 11);
        effects.put("fire_resistance", 12);
        effects.put("water_breathing", 13);
        effects.put("invisibility", 14);
        effects.put("blindness", 15);
        effects.put("night_vision", 16);
        effects.put("hunger", 17);
        effects.put("weakness", 18);
        effects.put("poison", 19);
        effects.put("wither", 20);
        effects.put("health_boost", 21);
        effects.put("absorption", 22);
        effects.put("saturation", 23);
        effects.put("glowing", 24);
        effects.put("levitation", 25);
        effects.put("luck", 26);
        effects.put("unluck", 27);
        effects.put("slow_falling", 28);
        effects.put("conduit_power", 29);
        effects.put("dolphins_grace", 30);
        effects.put("bad_omen", 31);
        effects.put("hero_of_the_village", 32);
        return effects;
    }

}
