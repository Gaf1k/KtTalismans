package me.gaf1.kttalismans.utils;

import me.gaf1.kttalismans.Plugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class ChatUtil {

    public static void sendMessage(Player player, String msg) {
        player.sendMessage(color(msg));
    }
    public static void sendMessage(CommandSender sender, String msg) {
        sender.sendMessage(color(msg));
    }

    public static void sendConfigMessage(CommandSender sender, String path) {
        sender.sendMessage(color(Plugin.getInstance().getConfig().getString(path)));
    }

    public static void sendConfigMessage(Player recipient, String configPath, Map<String, String> args) {
        String message = Plugin.getInstance().getConfig().getString(configPath);

        for (String key : args.keySet()) {
            message = message.replace(key, args.get(key));
        }

        sendMessage(recipient, message);
    }
    public static void sendConfigMessage(CommandSender recipient, String configPath, Map<String, String> args) {
        String message = Plugin.getInstance().getConfig().getString(configPath);

        for (String key : args.keySet()) {
            message = message.replace(key, args.get(key));
        }

        sendMessage(recipient, message);
    }

    public static Component color(String value) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(value).decoration(TextDecoration.ITALIC, false);
    }
}

