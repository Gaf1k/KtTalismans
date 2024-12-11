package me.gaf1.kttalismans.talisman.editmenu;


import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.AnvilWindow;
import xyz.xenondevs.invui.window.Window;

import java.util.ArrayList;
import java.util.List;

public class TalismanEditLore {
    Player player;
    Window window;
    List<String> lore;

    public TalismanEditLore(Player player,Window window,List<String> lore) {
        this.player = player;
        this.window = window;
        this.lore = lore;
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
                window.open();
            }
        });
        for (int i = 0; i< lore.size(); i++){
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

}
