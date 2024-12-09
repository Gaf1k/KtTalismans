package me.gaf1.kttalismans.utils;

import me.gaf1.kttalismans.Plugin;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class ConfigManager {
    public final static ConfigManager instance = new ConfigManager();

    public Map<String, YamlConfiguration> configs = new HashMap<>();

    public void init(String fileName) {
        fileName = fileName + ".yml";

        File file = new File(Plugin.getInstance().getDataFolder().getAbsolutePath() + "/" + fileName);

        if (!file.exists()) {
            Plugin.getInstance().saveResource(fileName, false);
        }

        configs.put(fileName, YamlConfiguration.loadConfiguration(file));
    }

    public void reloadConfigs(){
        for (String fileName: configs.keySet()){
            YamlConfiguration config = configs.get(fileName);
            try {
                config.load(new File(Plugin.getInstance().getDataFolder().getAbsolutePath() + "/" + fileName));
            } catch (IOException | InvalidConfigurationException e) {
                throw new RuntimeException(e);
            }
            configs.put(fileName,config);
        }
    }

}
