package org.alexdev.redisvanish.config;

import de.exlll.configlib.YamlConfigurationProperties;
import de.exlll.configlib.YamlConfigurations;
import lombok.Getter;
import org.alexdev.redisvanish.RedisVanish;

import java.io.File;

@Getter
public class ConfigManager {

    private final RedisVanish plugin;
    private Config config;


    public ConfigManager(RedisVanish plugin) {
        this.plugin = plugin;
        loadConfigs();
    }

    public void loadConfigs() {
        YamlConfigurationProperties properties = YamlConfigurationProperties.newBuilder()
                .footer("Authors: AlexDev_")
                .build();
        File settingsFile = new File(plugin.getDataFolder(), "config.yml");

        config = YamlConfigurations.update(
                settingsFile.toPath(),
                Config.class,
                properties
        );
    }

    public void saveConfigs() {
        YamlConfigurationProperties properties = YamlConfigurationProperties.newBuilder()
                .footer("Authors: AlexDev_")
                .build();
        YamlConfigurations.save(new File(plugin.getDataFolder(), "config.yml").toPath(), Config.class, config, properties);
    }

    public void reload() {
        YamlConfigurationProperties properties = YamlConfigurationProperties.newBuilder()
                .footer("Authors: AlexDev_")
                .build();
        config = YamlConfigurations.load(new File(plugin.getDataFolder(), "config.yml").toPath(), Config.class, properties);
    }
}
