package me.gaf1.kttalismans.talisman.editmenu;


import me.gaf1.kttalismans.talisman.TalismanManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.window.Window;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TalismanEditEffect {
    Player player;
    Window window;
    List<PotionEffect> effects;
    private final TalismanManager tManager = new TalismanManager();
    public TalismanEditEffect(Player player,Window window,List<PotionEffect> effects) {
        this.player = player;
        this.window = window;
        this.effects = effects;
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
                window.open();
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
                        effects.remove(finalI);
                        gui.remove(finalI);
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
                    effects.remove(i-1);
                    gui.remove(i-1);
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
}
