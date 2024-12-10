package me.gaf1.kttalismans.talisman.editmenu;

import lombok.Getter;
import lombok.Setter;
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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class TalismanEditMenu {
    Player player;
    String id;
    private String name = "&eТалисман";
    private List<String> lore = new ArrayList<>(List.of("§7Новая строка"));
    private final List<AttributeTalisman> attributeList = new ArrayList<>();
    private final List<PotionEffect> effects = new ArrayList<>();
    private final List<String> itemFlags = new ArrayList<>();
    private final TalismanManager tManager = new TalismanManager();

    public TalismanEditMenu(Player player, String id) {
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
                return new ItemBuilder(Material.DIAMOND_SWORD).addAllItemFlags().setDisplayName("§fАтрибуты");
            }

            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                openAttributeWindow();
            }
        });
        gui.setItem(3, new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                return new ItemBuilder(Material.POTION).addAllItemFlags().setDisplayName("§fЭффекты");
            }

            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                openEffectsWindow();
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
                YamlConfiguration talismanCfg = ConfigManager.instance.configs.get("talismans.yml");
                talismanCfg.set(id+".attributes.1",null);
                talismanCfg.set(id+".effects.STRENGTH",null);
                talismanCfg.set(id+".itemflags",new ArrayList<>());
                for (int i = 0;i<lore.size();i++){
                    lore.set(i,lore.get(i).replace("§","&"));
                }
                talismanCfg.set(id+".name",name.replace("§","&"));
                talismanCfg.set(id+".lore",lore);
                talismanCfg.set(id+".itemflags",itemFlags);
                for (int i = 0;i<effects.size();i++){
                    String type = null;

                    for (Map.Entry<String, Integer> entry : tManager.getMapEffects().entrySet()) {
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

        String text = lore.get(slot);
        gui.setItem(slot, new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                return new ItemBuilder(Material.PAPER)
                        .setDisplayName("§7" + (slot + 1) + " строка")
                        .addLoreLines(text,"","§7Зажмите шифт и нажмите лкм","§7Чтобы удалить строку");
            }

            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                if (clickType == ClickType.SHIFT_LEFT){
                    gui.remove(slot);
                    lore.remove(slot);
                    openLoreMenu();
                }
                else {
                    openLoreAnvilWindow(slot);
                }
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

        String text = lore.get(slot);
        gui.setItem(slot, new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                return new ItemBuilder(Material.PAPER)
                        .setDisplayName("§7" + (slot + 1) + " строка")
                        .addLoreLines(text,"","§7Зажмите шифт и нажмите лкм","§7Чтобы удалить строку");
            }

            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                if (clickType == ClickType.SHIFT_LEFT){
                    gui.remove(slot);
                    lore.remove(slot);
                    openLoreMenu();
                }
                else {
                    player.closeInventory();
                    openLoreAnvilWindow(slot);
                }
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
                    return new ItemBuilder(Material.PAPER).setDisplayName("§f"+(finalI +1)+" атрибут")
                            .addLoreLines("§7Зажмите шифт и нажмите лкм","§7Чтобы удалить атрибут");
                }

                @Override
                public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                    if (clickType == ClickType.SHIFT_LEFT){
                        gui.remove(finalI);
                        attributeList.remove(finalI);
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
                    gui.remove(i);
                    attributeList.remove(i);
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

    public void openEffectsWindow(){
        Window.single().setGui(getEffectsGui()).setViewer(player).build().open();
    }
    public Gui getEffectsGui(){
        Gui gui = Gui.empty(9,3);

        gui.setItem(21, new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                return new ItemBuilder(Material.LIME_DYE).setDisplayName("§aДобавить эффект");
            }

            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                addEffectSlot(gui);
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
        fillEffects(gui);

        return gui;
    }
    public void fillEffects(Gui gui){
        for (int i = 0;i<effects.size();i++) {
            int finalI = i;
            gui.setItem(i, new AbstractItem() {
                @Override
            public ItemProvider getItemProvider() {
                    String type = null;

                    for (Map.Entry<String, Integer> entry : tManager.getMapEffects().entrySet()) {
                        if (entry.getValue().equals(effects.get(finalI).getType().getId())) {
                            type = entry.getKey();
                        }
                    }

                return new ItemBuilder(Material.PAPER).setDisplayName("§f"+(finalI+1)+" Эффект")
                        .addLoreLines(type.toUpperCase()+" "+String.valueOf(effects.get(finalI).getAmplifier())
                                ," "
                                ,"§7Зажмите шифт и нажмите лкм"
                                ,"§7Чтобы удалить эффект");
            }

                @Override
                public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {

                    if (clickType == ClickType.SHIFT_LEFT){
                        gui.remove(finalI);
                        effects.remove(finalI);
                        openEffectsWindow();
                    }
                    else {
                        editEffectSlotWindow(finalI);
                    }
                }
            });
        }
    }

    public void addEffectSlot(Gui gui){
        effects.add(new PotionEffect(PotionEffectType.INCREASE_DAMAGE,Integer.MAX_VALUE,1));
        int i = effects.size();
        gui.setItem(i-1,new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                String type = null;

                for (Map.Entry<String, Integer> entry : tManager.getMapEffects().entrySet()) {
                    if (entry.getValue().equals(effects.get(i-1).getType().getId())) {
                        type = entry.getKey();
                    }
                }

                return new ItemBuilder(Material.PAPER).setDisplayName("§f"+i+" Эффект")
                        .addLoreLines(type.toUpperCase()+" "+String.valueOf(effects.get(i-1).getAmplifier())
                                ," "
                                ,"§7Зажмите шифт и нажмите лкм"
                                ,"§7Чтобы удалить эффект");
            }


            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {

                if (clickType == ClickType.SHIFT_LEFT){
                    gui.remove(i-1);
                    effects.remove(i-1);
                    openEffectsWindow();
                }
                else {
                    editEffectSlotWindow(i-1);
                }
            }
        });
    }
    public void editEffectSlotWindow(Integer value){
        Window.single().setGui(getEditEffectGui(value)).setViewer(player).build().open();
    }
    public Gui getEditEffectGui(Integer value){
        Gui gui = Gui.empty(9,2);
        gui.setItem(0, new AbstractItem() {
            final List<String> effectKeys = new ArrayList<>(tManager.getMapEffects().keySet());
            private int i = 0;

            @Override
            public ItemProvider getItemProvider() {
                String currentType = null;
                for (Map.Entry<String, Integer> entry : tManager.getMapEffects().entrySet()) {
                    if (entry.getValue().equals(effects.get(value).getType().getId())) {
                        currentType = entry.getKey();
                        break;
                    }
                }
                i = effectKeys.indexOf(currentType);
                List<String> lore = generateLore(i);
                return new ItemBuilder(Material.POTION)
                        .setDisplayName("§fЭффект")
                        .addAllItemFlags()
                        .setLegacyLore(lore);
            }

            private List<String> generateLore(int index) {
                List<String> lore = new ArrayList<>();
                int effectCount = effectKeys.size();

                // Расчёт индексов соседних эффектов  (обратите внимание на проверку на пустой список)
                if (effectCount == 0) return lore; // Предотвращение ошибки при пустом списке

                int prev1Index = (index - 2 + effectCount) % effectCount;
                int prevIndex = (index - 1 + effectCount) % effectCount;
                int nextIndex = (index + 1) % effectCount;
                int next2Index = (index + 2) % effectCount;

                lore.add("§7" + effectKeys.get(prev1Index).toUpperCase());
                lore.add("§7" + effectKeys.get(prevIndex).toUpperCase());
                lore.add("§f>" + effectKeys.get(index).toUpperCase());
                lore.add("§7" + effectKeys.get(nextIndex).toUpperCase());
                lore.add("§7" + effectKeys.get(next2Index).toUpperCase());

                return lore;
            }

            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                if (clickType == ClickType.RIGHT) {
                    i--;
                } else if (clickType == ClickType.LEFT) {
                    i++;
                }
                i = Math.floorMod(i, effectKeys.size());

                // Обновление выбранного эффекта
                String selectedEffectKey = effectKeys.get(i);
                PotionEffectType selectedEffectType = PotionEffectType.getById(tManager.getMapEffects().get(selectedEffectKey));
                effects.set(value, new PotionEffect(selectedEffectType, Integer.MAX_VALUE, effects.get(value).getAmplifier()));
                notifyWindows();
            }
        });

        gui.setItem(1, new AbstractItem() {
            private int i = 1;
            private boolean click = false;

            @Override
            public ItemProvider getItemProvider() {
                return new ItemBuilder(Material.POTION)
                        .setDisplayName("§fЗначение")
                        .addAllItemFlags()
                        .addLoreLines(click ? String.valueOf(i) : String.valueOf(effects.get(value).getAmplifier()));
            }

            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                click = true;
                if (clickType == ClickType.LEFT) {
                    i++;
                } else if (clickType == ClickType.RIGHT) {
                    i--;
                }
                if (i <= 0) {
                    i = 1;
                }
                effects.set(value, new PotionEffect(effects.get(value).getType(), Integer.MAX_VALUE, i));
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
                openEffectsWindow();
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
            private boolean isActive;
            @Override
            public ItemProvider getItemProvider() {
                isActive = itemFlags.contains(ItemFlag.HIDE_ATTRIBUTES);

                if (isActive){
                    return new ItemBuilder(Material.LIME_DYE).setDisplayName("§fHIDE_ATTRIBUTES").addLoreLines("§aВключено");
                }
                else {
                    return new ItemBuilder(Material.RED_DYE).setDisplayName("§fHIDE_ATTRIBUTES").addLoreLines("§cВыключено");
                }
            }

            @Override
            public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
                if (isActive){
                    isActive = false;
                    itemFlags.remove(String.valueOf(ItemFlag.HIDE_ATTRIBUTES));
                }
                else {
                    isActive = true;
                    itemFlags.add(String.valueOf(ItemFlag.HIDE_ATTRIBUTES));
                }
                notifyWindows();
            }
        });
        gui.setItem(1, new AbstractItem() {
            private boolean isActive;
            @Override
            public ItemProvider getItemProvider() {
                isActive = itemFlags.contains(ItemFlag.HIDE_ENCHANTS);
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
                    isActive = false;
                    itemFlags.remove(String.valueOf(ItemFlag.HIDE_ENCHANTS));
                }
                else {
                    isActive = true;
                    itemFlags.add(String.valueOf(ItemFlag.HIDE_ENCHANTS));
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
                openMain();
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

        public AttributeTalisman() {
            this.type = Attribute.GENERIC_ATTACK_DAMAGE;
            this.operation = AttributeModifier.Operation.ADD_NUMBER;
            this.amount = 1.0;
        }
    }

}
