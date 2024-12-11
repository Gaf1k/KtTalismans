package me.gaf1.kttalismans.talisman.editmenu;

import lombok.Getter;
import lombok.Setter;
import me.gaf1.kttalismans.Plugin;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.window.Window;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class TalismanEditAttribute {
    Player player;
    Window window;
    List<TalismanEditAttribute.AttributeTalisman> attributeList;
    public TalismanEditAttribute(Player player,Window window,List<TalismanEditAttribute.AttributeTalisman> attributeList) {
        this.player = player;
        this.window = window;
        this.attributeList = attributeList;
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
                window.open();
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
                    return new ItemBuilder(Material.PAPER).setDisplayName("§f"+(finalI+1)+" атрибут")
                            .addLoreLines("§7Зажмите шифт и нажмите лкм","§7Чтобы удалить атрибут");
                }

                @Override
                public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                    if (clickType == ClickType.SHIFT_LEFT){
                        attributeList.remove(finalI);
                        gui.remove(finalI);
                        openAttributeWindow();
                    } else {
                        editAttributeWindow(finalI);
                    }
                }
            });
        }
    }
    public void addAttribute(Gui gui) {
        int i = attributeList.size()-1;
        gui.setItem(i, new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                return new ItemBuilder(Material.PAPER).setDisplayName("§f" + attributeList.size() + " атрибут")
                        .addLoreLines("§7Зажмите шифт и нажмите лкм","§7Чтобы удалить атрибут");
            }

            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                if (clickType == ClickType.SHIFT_LEFT){
                    attributeList.remove(i);
                    gui.remove(i);
                    openAttributeWindow();
                }
                else {
                    editAttributeWindow(i);
                }
            }
        });
    }

    public void editAttributeWindow(Integer value){
        Window.single().setViewer(player).setGui(getEditAttributeGui(value)).build().open();
    }
    public Gui getEditAttributeGui(Integer value){
        Gui gui = Gui.empty(9,2);
        gui.setItem(0, new AbstractItem() {
            final List<Attribute> attributes = List.of(Attribute.values());
            private int i = 0;
            @Override
            public ItemProvider getItemProvider() {

                Attribute currentType = null;
                for (Attribute type: attributes){
                    if (type == attributeList.get(value).getType()){
                        currentType = type;
                    }
                }
                i = attributes.indexOf(currentType);
                List<String> lore = generateLore(i);
                return new ItemBuilder(Material.OAK_SIGN)
                        .setDisplayName("§fТип атрибута")
                        .setLegacyLore(lore);
            }
            private List<String> generateLore(int index) {
                List<String> lore = new ArrayList<>();
                int count = attributes.size();

                // Расчёт индексов соседних эффектов  (обратите внимание на проверку на пустой список)
                if (count == 0) return lore; // Предотвращение ошибки при пустом списке

                int prev1Index = (index - 2 + count) % count;
                int prevIndex = (index - 1 + count) % count;
                int nextIndex = (index + 1) % count;
                int next2Index = (index + 2) % count;

                lore.add("§7" + attributes.get(prev1Index));
                lore.add("§7" + attributes.get(prevIndex));
                lore.add("§f>" + attributes.get(index));
                lore.add("§7" + attributes.get(nextIndex));
                lore.add("§7" + attributes.get(next2Index));

                return lore;
            }

            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                if (clickType == ClickType.RIGHT) {
                    i--;
                } else if (clickType == ClickType.LEFT) {
                    i++;
                }
                i = Math.floorMod(i, attributes.size());
                attributeList.get(value).setType(attributes.get(i));
                notifyWindows();
            }
        });
        gui.setItem(1, new AbstractItem() {
            final List<AttributeModifier.Operation> operations = List.of(AttributeModifier.Operation.values());
            private int i = 0;


            @Override
            public ItemProvider getItemProvider() {
                AttributeModifier.Operation currentType = null;
                for (AttributeModifier.Operation type: operations){
                    if (type == attributeList.get(value).getOperation()){
                        currentType = type;
                    }
                }
                i = operations.indexOf(currentType);
                return new ItemBuilder(Material.BIRCH_SIGN)
                        .setDisplayName("§fТип добавления")
                        .setLegacyLore(generateLore(i));
            }
            private List<String> generateLore(int index) {
                List<String> lore = new ArrayList<>();
                int count = operations.size();

                if (count == 0) return lore;

                int prevIndex = (index - 1 + count) % count;
                int nextIndex = (index + 1) % count;

                lore.add("§7" + operations.get(prevIndex));
                lore.add("§f>" + operations.get(index));
                lore.add("§7" + operations.get(nextIndex));

                return lore;
            }

            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                if (clickType == ClickType.RIGHT) {
                    i--;
                } else if (clickType == ClickType.LEFT) {
                    i++;
                }
                i = Math.floorMod(i, operations.size());
                attributeList.get(value).setOperation(operations.get(i));
                notifyWindows();
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
                if (i<0){
                    i=0.0;
                }
                notifyWindows();
                DecimalFormat df = new DecimalFormat("#.#");
                attributeList.get(value).setAmount(Double.parseDouble(df.format(i)));
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
    public static class AttributeTalisman {

        @Getter
        @Setter
        private Attribute type;

        @Getter
        @Setter
        private AttributeModifier.Operation operation;

        @Getter
        @Setter
        private double amount;

        // Конструктор по умолчанию
        public AttributeTalisman() {
            this.type = Attribute.GENERIC_ATTACK_DAMAGE;
            this.operation = AttributeModifier.Operation.ADD_NUMBER;
            this.amount = 1.0;
        }

        // Конструктор с параметрами
        public AttributeTalisman(Attribute type, AttributeModifier.Operation operation, double amount) {
            this.type = type;
            this.operation = operation;
            this.amount = amount;
        }
    }

}
