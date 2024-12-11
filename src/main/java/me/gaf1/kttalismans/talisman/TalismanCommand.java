package me.gaf1.kttalismans.talisman;

import me.gaf1.kttalismans.Plugin;
import me.gaf1.kttalismans.talisman.editmenu.TalismanMainEdit;
import me.gaf1.kttalismans.utils.ChatUtil;
import me.gaf1.kttalismans.utils.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TalismanCommand implements TabExecutor {
    private final TalismanManager tManager = new TalismanManager();
    private final YamlConfiguration talismanCfg = ConfigManager.instance.configs.get("talismans.yml");
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!sender.hasPermission("kttalisman.admin")){
            ChatUtil.sendConfigMessage(sender,"Messages.not_enough_perm");
            return true;
        }
        if (args.length == 0){
            ChatUtil.sendConfigMessage(sender,"Messages.error_command");
            return true;
        }
        String args0 = args[0].toLowerCase();
        switch (args0){
            case "create":
                if (args.length == 1) {
                    ChatUtil.sendMessage(sender,"&f/talisman create <id>");
                    return true;
                }
                if (talismanCfg.getKeys(false).contains(args[1])){
                    ChatUtil.sendConfigMessage(sender,"Messages.talisman_exist");
                    return true;
                }
                if (sender instanceof Player){
                    Player player = (Player) sender;
                    tManager.createTalisman(args[1],sender);
                    new TalismanMainEdit(player,args[1]).getMainWindow().open();
                    return true;
                }
                else {
                    tManager.createTalisman(args[1], sender);
                    return true;
                }
            case "edit":
                if (args.length == 1) {
                    ChatUtil.sendMessage(sender,"&f/talisman edit <id>");
                    return true;
                }
                if (!talismanCfg.getKeys(false).contains(args[1])) {
                    ChatUtil.sendConfigMessage(sender, "Messages.talisman_not_exist");
                    return true;
                }
                if (!(sender instanceof Player)){
                    Bukkit.getLogger().info("Ты консоль, тебе нельзя!");
                    return true;
                }
                Player player2 = (Player) sender;
                new TalismanMainEdit(player2,args[1]).getMainWindow().open();
                return true;
            case "give":
                if (args.length == 1) {
                    ChatUtil.sendMessage(sender,"&f/talisman give <id> <player>");
                    return true;
                }
                else if (args.length == 2) {
                    if (!talismanCfg.getKeys(false).contains(args[1])) {
                        ChatUtil.sendConfigMessage(sender, "Messages.talisman_not_exist");
                        return true;
                    }
                    if (!(sender instanceof Player)) {
                        Bukkit.getLogger().info("&cТы консоль! Тебе нельзя!");
                        return true;
                    }
                    Player player = (Player) sender;
                    player.getInventory().addItem(tManager.getTalisman(args[1]));
                    return true;
                }
                else if (args.length == 3){
                    if (!talismanCfg.getKeys(false).contains(args[1])) {
                        ChatUtil.sendConfigMessage(sender, "Messages.talisman_not_exist");
                        return true;
                    }
                    Player player1 = Bukkit.getPlayer(args[2]);
                    if (player1 == null){
                        ChatUtil.sendConfigMessage(sender, "Messages.player_not_exist");
                        return true;
                    }
                    ChatUtil.sendConfigMessage(player1,"Messages.talisman_received",
                            Map.of("%talisman_name%",talismanCfg.getString(args[1]+".name")));
                    ChatUtil.sendConfigMessage(sender, "Messages.talisman_gived",
                            Map.of("%talisman_name%",talismanCfg.getString(args[1]+".name"),"%player%",player1.getName()));
                    player1.getInventory().addItem(tManager.getTalisman(args[1]));
                    return true;
                }
            case "remove":
                if (args.length == 1) {
                    ChatUtil.sendMessage(sender,"&f/talisman remove <id>");
                    return true;
                }
                if (!talismanCfg.getKeys(false).contains(args[1])){
                    ChatUtil.sendConfigMessage(sender,"Messages.talisman_not_exist");
                    return true;
                }
                talismanCfg.set(args[1],null);
                ChatUtil.sendConfigMessage(sender,"Messages.talisman_deleted");
                try {
                    talismanCfg.save(new File(Plugin.getInstance().getDataFolder().getAbsolutePath() + "/talismans.yml"));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return true;
            case "reload":
                ConfigManager.instance.reloadConfigs();
                Plugin.getInstance().reloadConfig();
                ChatUtil.sendConfigMessage(sender,"Messages.reload_config");
                return true;
            default:
                ChatUtil.sendConfigMessage(sender,"Messages.error_command");
                return true;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if (!sender.hasPermission("kttalisman.admin")) {
            return null;
        }
        if (args.length == 1){
            return List.of("give","create","remove","reload","edit");
        }
        else if (args.length == 2 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("edit"))){
            List<String> list = new ArrayList<>();
            for (String key: talismanCfg.getKeys(false)){
                list.add(key);
            }
            return list.stream()
                    .filter(sound -> sound.contains(args[1])) // Проверяем наличие подстроки
                    .collect(Collectors.toList());
        } else if (args.length == 2 && args[0].equalsIgnoreCase("create")) {
            return null;
        }
        return null;
    }
}
