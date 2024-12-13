package me.gaf1.kttalismans.talisman.editmenu;

import lombok.Getter;
import me.gaf1.kttalismans.Plugin;
import me.gaf1.kttalismans.talisman.TalismanManager;
import me.gaf1.kttalismans.utils.ConfigManager;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.AnvilWindow;
import xyz.xenondevs.invui.window.Window;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class TalismanMainEdit {
    Player player;
    String id;
    private final YamlConfiguration talismanCfg = ConfigManager.instance.configs.get("talismans.yml");
    private String name;
    private final List<String> lore = new ArrayList<>();
    private final List<TalismanEditAttribute.AttributeTalisman> attributeList = new ArrayList<>();
    private final List<PotionEffect> effects = new ArrayList<>();
    private final List<String> itemFlags = new ArrayList<>();
    private final TalismanEditLore editLore;
    private final TalismanEditAttribute editAttribute;
    private final TalismanEditEffect editEffect;
    @Getter
    private final Window mainWindow;
    public TalismanMainEdit(Player player, String id) {
        this.player = player;
        this.id = id;
        name = talismanCfg.getString(id+".name");
        fillObjects();
        mainWindow = Window.single().setViewer(player).setTitle(id).setGui(getMainGui()).build();
        editLore = new TalismanEditLore(player,mainWindow,lore);
        editAttribute = new TalismanEditAttribute(player, mainWindow,attributeList);
        editEffect = new TalismanEditEffect(player, mainWindow,effects);
    }

    public void fillObjects(){
        for (String line: talismanCfg.getStringList(id+".lore")){
            lore.add(line.replace("&","§"));
        }
        if (!talismanCfg.getConfigurationSection(id+".attributes").getKeys(false).isEmpty()) {
            for (String key : talismanCfg.getConfigurationSection(id + ".attributes").getKeys(false)) {
                attributeList.add(new TalismanEditAttribute.AttributeTalisman(
                        Attribute.valueOf(talismanCfg.getString(id + ".attributes." + key + ".type")),
                        AttributeModifier.Operation.valueOf(talismanCfg.getString(id + ".attributes." + key + ".operation")),
                        talismanCfg.getDouble(id + ".attributes." + key + ".amount")
                ));
            }
        }
        if (!talismanCfg.getConfigurationSection(id + ".effects").getKeys(false).isEmpty()) {
            for (String key : talismanCfg.getConfigurationSection(id + ".effects").getKeys(false)) {
                effects.add(new PotionEffect(PotionEffectType.getById(new TalismanManager().getMapEffects().get(key.toLowerCase())),
                        Integer.MAX_VALUE,
                        talismanCfg.getInt(id + ".effects." + key)
                ));
            }
        }
        for (String flag: talismanCfg.getStringList(id+".itemflags")){
            itemFlags.add(flag.toUpperCase());
        }
    }

    public Gui getMainGui(){
        Gui gui = Gui.empty(9,3);
        gui.setItem(0, new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                return new ItemBuilder(Material.NAME_TAG).setDisplayName("§fНазвание: " + name.replace("&","§"));
            }

            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                openNameAnvilWindow();
                notifyWindows();
            }
        });
        gui.setItem(1, new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                return new ItemBuilder(Material.WRITABLE_BOOK).setDisplayName("§fОписание").setLegacyLore(lore);
            }

            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                player.closeInventory();
                editLore.openLoreMenu();
            }
        });

        gui.setItem(2, new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                return new ItemBuilder(Material.DIAMOND_SWORD).addAllItemFlags().setDisplayName("§fАтрибуты");
            }

            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                editAttribute.openAttributeWindow();
            }
        });
        gui.setItem(3, new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                return new ItemBuilder(Material.POTION).addAllItemFlags().setDisplayName("§fЭффекты");
            }

            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                editEffect.openEffectsWindow();
            }
        });
        gui.setItem(4, new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                return new ItemBuilder(Material.ORANGE_BANNER).setDisplayName("§fФлаги");
            }

            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                openFlagsWindow();
            }
        });

        gui.setItem(22, new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                return new ItemBuilder(Material.LIME_CONCRETE).setDisplayName("§aСохранить");
            }

            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                talismanCfg.set(id+".lore",List.of());
                talismanCfg.set(id+".itemflags", List.of());
                talismanCfg.set(id+".effects", null);
                talismanCfg.set(id+".attributes", null);
                talismanCfg.createSection(id+".effects");
                talismanCfg.createSection(id+".attributes");
                for (int i = 0;i<lore.size();i++){
                    lore.set(i,lore.get(i).replace("§","&"));
                }
                talismanCfg.set(id+".name",name.replace("§","&"));
                talismanCfg.set(id+".lore",lore);
                talismanCfg.set(id+".itemflags",itemFlags);
                for (int i = 0;i<effects.size();i++){
                    String type = null;
                    for (Map.Entry<String, Integer> entry : new TalismanManager().getMapEffects().entrySet()) {
                        if (entry.getValue().equals(effects.get(i).getType().getId())) {
                            type = entry.getKey();
                        }
                    }

                    talismanCfg.set(id+".effects."+type.toUpperCase(), effects.get(i).getAmplifier());
                }
                for (int i = 0;i<attributeList.size();i++){
                    talismanCfg.set(id+".attributes."+i+".type", String.valueOf(attributeList.get(i).getType()));
                    talismanCfg.set(id+".attributes."+i+".operation",String.valueOf(attributeList.get(i).getOperation()));
                    talismanCfg.set(id+".attributes."+i+".amount",attributeList.get(i).getAmount());
                }

                ConfigManager.instance.configs.put("talismans.yml",talismanCfg);
                try {
                    talismanCfg.save(new File(Plugin.getInstance().getDataFolder().getAbsolutePath() + "/talismans.yml"));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                player.closeInventory();
            }
        });

        return gui;
    }
    public void openNameAnvilWindow(){
        AnvilWindow.single()
                .setViewer(player)
                .addRenameHandler(s -> name = s)
                .setGui(getNameAnvilGui())
                .build()
                .open();
    }

    public Gui getNameAnvilGui(){
        Gui gui = Gui.empty(9,1);
        gui.setItem(0, new SimpleItem(new ItemBuilder(Material.PAPER).setDisplayName(name.replace("§","&"))));
        gui.setItem(1, new AbstractItem(){
            @Override
            public ItemProvider getItemProvider() {
                return new ItemBuilder(Material.RED_CONCRETE).setDisplayName("§fЗакрыть");
            }

            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                mainWindow.open();
            }
        });
        return gui;
    }
    public void openFlagsWindow(){
        Window.single().setGui(getFlagsGui()).setViewer(player).build().open();
    }
    public Gui getFlagsGui(){
        Gui gui = Gui.empty(9,2);

        gui.setItem(0, new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                boolean isActive = itemFlags.contains(ItemFlag.HIDE_ATTRIBUTES.toString());
                if (isActive) {
                    return new ItemBuilder(Material.LIME_DYE)
                            .setDisplayName("§fHIDE_ATTRIBUTES")
                            .addLoreLines("§aВключено");
                } else {
                    return new ItemBuilder(Material.RED_DYE)
                            .setDisplayName("§fHIDE_ATTRIBUTES")
                            .addLoreLines("§cВыключено");
                }
            }

            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                boolean isActive = itemFlags.contains(ItemFlag.HIDE_ATTRIBUTES.toString());
                if (isActive) {
                    itemFlags.remove(ItemFlag.HIDE_ATTRIBUTES.toString()); // Убираем флаг
                } else {
                    itemFlags.add(ItemFlag.HIDE_ATTRIBUTES.toString()); // Добавляем флаг
                }
                notifyWindows(); // Обновляем интерфейс
            }
        });
        gui.setItem(1, new AbstractItem() {
            private boolean isActive;
            @Override
            public ItemProvider getItemProvider() {
                isActive = itemFlags.contains(ItemFlag.HIDE_ENCHANTS.toString());
                if (isActive){
                    return new ItemBuilder(Material.LIME_DYE).setDisplayName("§fHIDE_ENCHANTS").addLoreLines("§aВключено");
                }
                else {
                    return new ItemBuilder(Material.RED_DYE).setDisplayName("§fHIDE_ENCHANTS").addLoreLines("§cВыключено");
                }
            }

            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                if (isActive){
                    itemFlags.remove(ItemFlag.HIDE_ENCHANTS.toString());
                }
                else {
                    itemFlags.add(ItemFlag.HIDE_ENCHANTS.toString());
                }
                notifyWindows();
            }
        });
        gui.setItem(17, new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                return new ItemBuilder(Material.LIME_CONCRETE).setDisplayName("§aСохранить");
            }

            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                mainWindow.open();
            }
        });

        return gui;
    }
}
