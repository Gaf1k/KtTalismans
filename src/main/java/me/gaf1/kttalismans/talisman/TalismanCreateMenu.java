package me.gaf1.kttalismans.talisman.createmenu;

import lombok.Getter;
import lombok.Setter;
import me.gaf1.kttalismans.Plugin;
import me.gaf1.kttalismans.utils.ConfigManager;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.EquipmentSlot;
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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainMenu {
    Player player;
    String id;
    private String name = "&eТалисман";
    private List<String> lore = new ArrayList<>(List.of("§7Новая строка"));
    private Boolean glow = false;
    private final List<AttributeTalisman> attributeList = new ArrayList<>();

    public MainMenu(Player player, String id) {
        this.player = player;
        this.id = id;
    }

    public void openMain(){
        Window.single().setViewer(player).setTitle(id).setGui(getMainGui()).build().open();
    }
    public Gui getMainGui(){
        Gui gui = Gui.empty(9,3);
        gui.setItem(0, new AbstractItem() {
            final String displayName = name.replace("&","§");
            @Override
            public ItemProvider getItemProvider() {
                return new ItemBuilder(Material.NAME_TAG).setDisplayName("§fНазвание: " + displayName);
            }

            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                player.closeInventory();
                openNameAnvilWindow();
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
                openLoreMenu();
            }
        });
        gui.setItem(2, new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                if (glow){
                    return new ItemBuilder(Material.ENCHANTED_BOOK)
                            .setDisplayName("§fСвечение")
                            .addLoreLines("§aВключено");
                }
                else {
                    return new ItemBuilder(Material.BOOK)
                            .setDisplayName("§fСвечение")
                            .addLoreLines("§cВыключено");
                }
            }

            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                if (glow){
                    glow = false;
                }
                else {
                    glow = true;
                }
                notifyWindows();
            }
        });

        gui.setItem(3, new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                return new ItemBuilder(Material.DIAMOND_SWORD).setDisplayName("§fАтрибуты");
            }

            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                openAttributeWindow();
            }
        });

        gui.setItem(22, new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                return new ItemBuilder(Material.LIME_CONCRETE).setDisplayName("§aСохранить");
            }

            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                YamlConfiguration talismanCfg = ConfigManager.instance.configs.get("talismans.yml");
                for (int i = 0;i<lore.size();i++){
                    lore.set(i,lore.get(i).replace("§","&"));
                }
                talismanCfg.set(id+".name",name.replace("§","&"));
                talismanCfg.set(id+".lore",lore);
                talismanCfg.set(id+".glow",glow);
                for (int i = 0;i<attributeList.size();i++){
                    talismanCfg.set(id+".attributes."+i+".type", String.valueOf(attributeList.get(i).getType()));
                    talismanCfg.set(id+".attributes."+i+".operation",String.valueOf(attributeList.get(i).getOperation()));
                    talismanCfg.set(id+".attributes."+i+".amount",String.valueOf(attributeList.get(i).getAmount()));
                    talismanCfg.set(id+".attributes."+i+".slot",String.valueOf(attributeList.get(i).getSlot()));
                }
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
                .addRenameHandler(s -> name = s.replace("&","§"))
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
                openMain();
            }
        });
        return gui;
    }

    public void openLoreMenu(){
        Window.single().setGui(getLoreGui()).setViewer(player).build().open();
    }
    public Gui getLoreGui(){
        Gui gui = Gui.empty(9,3);

        gui.setItem(21, new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                return new ItemBuilder(Material.LIME_DYE).setDisplayName("§aДобавить строку");
            }

            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                setSlot(gui);
            }
        });
        gui.setItem(23, new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                return new ItemBuilder(Material.LIME_CONCRETE).setDisplayName("§aСохранить");
            }

            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                openMain();
            }
        });
        for (int i = 0;i<lore.size();i++){
            fillSlot(gui,i);
        }
        return gui;
    }
    public void setSlot(Gui gui) {
        lore.add("§7Новая строка");

        int slot = lore.size() - 1;

        if (slot >= gui.getSize()) {
            return;
        }

        // Добавляем элемент в GUI
        String text = lore.get(slot); // Получаем строку
        gui.setItem(slot, new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                return new ItemBuilder(Material.PAPER)
                        .setDisplayName("§7" + (slot + 1) + " строка")
                        .addLoreLines(text);
            }

            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                openLoreAnvilWindow(slot);
            }
        });
    }
    public void fillSlot(Gui gui, int slot) {
        if (slot < 0 || slot >= lore.size()) {
            return;
        }
        if (slot >= 17) {
            return;
        }

        String text = lore.get(slot); // Получаем строку напрямую
        gui.setItem(slot, new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                return new ItemBuilder(Material.PAPER)
                        .setDisplayName("§7" + (slot + 1) + " строка") // Человекочитаемый номер строки
                        .addLoreLines(text); // Добавляем содержимое строки
            }

            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                player.closeInventory();
                openLoreAnvilWindow(slot); // Передаём индекс строки для редактирования
            }
        });
    }
    public void openLoreAnvilWindow(Integer slot){
        AnvilWindow.single()
                .setViewer(player)
                .addRenameHandler(s -> lore.set(slot,s.replace("&","§")))
                .setGui(getLoreAnvilGui(slot))
                .build()
                .open();
    }
    public Gui getLoreAnvilGui(Integer slot){
        Gui gui = Gui.empty(9,1);
        gui.setItem(0, new SimpleItem(new ItemBuilder(Material.PAPER).setDisplayName(lore.get(slot).replace("§","&"))));
        gui.setItem(1, new AbstractItem(){
            @Override
            public ItemProvider getItemProvider() {
                return new ItemBuilder(Material.RED_CONCRETE).setDisplayName("§fЗакрыть");
            }

            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                player.closeInventory();
                openLoreMenu();
            }
        });
        return gui;
    }
    public void openAttributeWindow(){
        Window.single().setGui(getAttributeGui()).setViewer(player).build().open();
    }
    public Gui getAttributeGui(){
        Gui gui = Gui.empty(9,3);
        gui.setItem(21, new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                return new ItemBuilder(Material.LIME_DYE).setDisplayName("§aДобавить атрибут");
            }

            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                attributeList.add(new AttributeTalisman());
                addAttribute(gui);
            }
        });
        gui.setItem(23, new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                return new ItemBuilder(Material.LIME_CONCRETE).setDisplayName("§aСохранить");
            }

            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                openMain();
            }
        });
        fillAttributes(gui);
        return gui;
    }
    public void fillAttributes(Gui gui){
        for (int i = 0; i<attributeList.size();i++){
            if (i > 17){
                break;
            }
            int finalI = i;
            gui.setItem(finalI, new AbstractItem() {
                @Override
                public ItemProvider getItemProvider() {
                    return new ItemBuilder(Material.PAPER).setDisplayName("§f"+(finalI +1)+" атрибут");
                }

                @Override
                public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                    editAttributeWindow(finalI);
                }
            });
        }
    }
    public void addAttribute(Gui gui) {
        int i = attributeList.size()-1;
        gui.setItem(i, new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                return new ItemBuilder(Material.PAPER).setDisplayName("§f" + attributeList.size() + " атрибут");
            }

            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                editAttributeWindow(i);  // передаем индекс последнего атрибута
            }
        });
    }

    public void editAttributeWindow(Integer value){
        Window.single().setViewer(player).setGui(getEditAttributeGui(value)).build().open();
    }
    public Gui getEditAttributeGui(Integer value){
        Gui gui = Gui.empty(9,2);
        gui.setItem(0, new AbstractItem() {
            final List<Attribute> list = List.of(Attribute.values());
            private int i = 0;
            private Boolean click = false;
            @Override
            public ItemProvider getItemProvider() {
                if (click) {
                    return new ItemBuilder(Material.OAK_SIGN)
                            .setDisplayName("§fТип атрибута")
                            .addLoreLines(String.valueOf(list.get(i)));
                }
                else {
                    return new ItemBuilder(Material.OAK_SIGN)
                            .setDisplayName("§fТип атрибута")
                            .addLoreLines(String.valueOf(attributeList.get(value).getType()));
                }
            }

            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                click = true;
                if (clickType == ClickType.RIGHT){
                    i++;
                }
                else if (clickType == ClickType.LEFT){
                    i--;
                }
                // Проверяем, чтобы индекс оставался в пределах допустимых значений
                if (i >= list.size()) {
                    i = 0; // если индекс превышает максимальный, сбрасываем его на 0
                } else if (i < 0) {
                    i = list.size() - 1; // если индекс меньше 0, устанавливаем его на последний элемент
                }
                notifyWindows();
                attributeList.get(value).setType(list.get(i));
            }
        });
        gui.setItem(1, new AbstractItem() {
            final List<AttributeModifier.Operation> operations = List.of(AttributeModifier.Operation.values());
            private int i = 0;
            private Boolean click = false;


            @Override
            public ItemProvider getItemProvider() {
                if (click) {
                    return new ItemBuilder(Material.BIRCH_SIGN)
                            .setDisplayName("§fТип добавления")
                            .addLoreLines(String.valueOf(operations.get(i)));
                }
                else {
                    return new ItemBuilder(Material.BIRCH_SIGN)
                            .setDisplayName("§fТип добавления")
                            .addLoreLines(String.valueOf(attributeList.get(value).getOperation()));
                }
            }

            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                click = true;
                if (clickType == ClickType.RIGHT){
                    i++;
                }
                else if (clickType == ClickType.LEFT){
                    i--;
                }
                // Проверяем, чтобы индекс оставался в пределах допустимых значений
                if (i >= operations.size()) {
                    i = 0; // если индекс превышает максимальный, сбрасываем его на 0
                } else if (i < 0) {
                    i = operations.size() - 1; // если индекс меньше 0, устанавливаем его на последний элемент
                }
                notifyWindows();
                attributeList.get(value).setOperation(operations.get(i));
            }
        });
        gui.setItem(2, new AbstractItem() {
            boolean click = false;
            double i = attributeList.get(value).getAmount();

            @Override
            public ItemProvider getItemProvider() {
                DecimalFormat df = new DecimalFormat("#.#");
                    return new ItemBuilder(Material.BIRCH_SIGN)
                            .setDisplayName("§fЗначение")
                            .addLoreLines(df.format(i));
            }

            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                click = true;
                if (clickType == ClickType.RIGHT){
                    i = i-0.1;
                }
                else if(clickType == ClickType.SHIFT_RIGHT){
                    i = i-1;
                }
                if (clickType == ClickType.LEFT){
                    i = i+0.1;
                }
                else if(clickType == ClickType.SHIFT_LEFT){
                    i = i+1;
                }
                notifyWindows();
                DecimalFormat df = new DecimalFormat("#.#");
                attributeList.get(value).setAmount(Double.parseDouble(df.format(i)));
            }
        });
        gui.setItem(3, new AbstractItem() {

            List<EquipmentSlot> slots = List.of(EquipmentSlot.values());
            private int i = 0;
            boolean click = false;


            @Override
            public ItemProvider getItemProvider() {
                if (click) {
                    return new ItemBuilder(Material.BIRCH_SIGN)
                            .setDisplayName("§fСлот")
                            .addLoreLines(String.valueOf(slots.get(i)));
                }
                else {

                    return new ItemBuilder(Material.BIRCH_SIGN)
                            .setDisplayName("§fСлот")
                            .addLoreLines(String.valueOf(attributeList.get(value).getSlot()));
                }
            }

            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                click = true;
                if (clickType == ClickType.RIGHT){
                    i++;
                }
                else if (clickType == ClickType.LEFT){
                    i--;
                }
                // Проверяем, чтобы индекс оставался в пределах допустимых значений
                if (i >= slots.size()) {
                    i = 0; // если индекс превышает максимальный, сбрасываем его на 0
                } else if (i < 0) {
                    i = slots.size() - 1; // если индекс меньше 0, устанавливаем его на последний элемент
                }
                notifyWindows();
                attributeList.get(value).setSlot(slots.get(i));
            }
        });

        gui.setItem(17, new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                return new ItemBuilder(Material.LIME_CONCRETE).setDisplayName("§aСохранить");
            }

            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                openAttributeWindow();
            }
        });

        return gui;
    }

    public class AttributeTalisman {
        @Getter
        @Setter
        private Attribute type;
        @Getter
        @Setter
        private AttributeModifier.Operation operation;
        @Getter
        @Setter
        private double amount;
        @Getter
        @Setter
        private EquipmentSlot slot;

        public AttributeTalisman() {
            this.type = Attribute.GENERIC_ATTACK_DAMAGE;
            this.operation = AttributeModifier.Operation.ADD_NUMBER;
            this.amount = 1.0;
            this.slot = EquipmentSlot.OFF_HAND;
        }
        public AttributeTalisman(Attribute type, AttributeModifier.Operation operation, double amount, EquipmentSlot slot) {
            this.type = type;
            this.operation = operation;
            this.amount = amount;
            this.slot = slot;
        }
        public void setAttribute(Attribute type, AttributeModifier.Operation operation, double amount, EquipmentSlot slot) {
            this.type = type;
            this.operation = operation;
            this.amount = amount;
            this.slot = slot;
        }
    }


}
