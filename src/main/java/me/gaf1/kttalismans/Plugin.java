package me.gaf1.kttalismans;

import lombok.Getter;
import me.gaf1.kttalismans.talisman.TalismanCommand;
import me.gaf1.kttalismans.talisman.TalismanEffectListener;
import me.gaf1.kttalismans.talisman.TalismanRecoveryListener;
import me.gaf1.kttalismans.utils.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Plugin extends JavaPlugin {
    @Getter
    private static Plugin instance;
    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        ConfigManager.instance.init("talismans");
        getCommand("talisman").setExecutor(new TalismanCommand());
        getCommand("talisman").setTabCompleter(new TalismanCommand());
        getServer().getPluginManager().registerEvents(new TalismanEffectListener(),this);
        getServer().getPluginManager().registerEvents(new TalismanRecoveryListener(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
