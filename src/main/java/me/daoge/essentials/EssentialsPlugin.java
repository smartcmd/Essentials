package me.daoge.essentials;

import lombok.Getter;
import me.daoge.essentials.command.*;
import org.allaymc.api.command.CommandRegistry;
import org.allaymc.api.eventbus.EventBus;
import org.allaymc.api.plugin.Plugin;
import org.allaymc.api.registry.Registries;
import org.allaymc.api.server.Server;
import org.allaymc.api.utils.config.Config;
import org.allaymc.api.utils.config.ConfigSection;

import java.io.File;
import java.io.InputStream;

/**
 * Essentials plugin for Allay server
 *
 * @author daoge
 */
public class EssentialsPlugin extends Plugin {

    @Getter
    private static EssentialsPlugin instance;

    private EventListener eventListener;
    @Getter
    private WarpManager warpManager;
    @Getter
    private HomeManager homeManager;
    @Getter
    private Config config;

    @Override
    public void onLoad() {
        instance = this;
        this.pluginLogger.info("Essentials plugin is loading...");

        // Load configuration
        loadConfig();
    }

    @Override
    public void onEnable() {
        this.pluginLogger.info("Essentials plugin is enabling...");

        // Initialize managers
        warpManager = new WarpManager(this.pluginContainer.dataFolder());
        homeManager = new HomeManager(this.pluginContainer.dataFolder());

        // Get feature configuration
        ConfigSection features = config.getSection("features");

        // Register commands based on configuration
        CommandRegistry commandRegistry = Registries.COMMANDS;

        if (features.getBoolean("ping", true)) {
            commandRegistry.register(new PingCommand());
            this.pluginLogger.info("Registered command: /ping");
        }

        if (features.getBoolean("back", true)) {
            commandRegistry.register(new BackCommand());
            this.pluginLogger.info("Registered command: /back");
        }

        if (features.getBoolean("tpa", true)) {
            commandRegistry.register(new TpaCommand());
            this.pluginLogger.info("Registered command: /tpa");
        }

        if (features.getBoolean("warp", true)) {
            commandRegistry.register(new WarpCommand(warpManager));
            this.pluginLogger.info("Registered command: /warp");
        }

        if (features.getBoolean("home", true)) {
            commandRegistry.register(new HomeCommand(homeManager));
            this.pluginLogger.info("Registered command: /home");
        }

        if (features.getBoolean("notice", true)) {
            commandRegistry.register(new NoticeCommand());
            this.pluginLogger.info("Registered command: /notice");
        }

        // Register event listeners
        // Need to listen to PlayerJoinEvent if notice is enabled
        // Need to listen to EntityDieEvent and PlayerDisconnectEvent if back or tpa is enabled
        if (features.getBoolean("back", true) || features.getBoolean("tpa", true) || features.getBoolean("notice", true)) {
            EventBus eventBus = Server.getInstance().getEventBus();
            eventListener = new EventListener();
            eventBus.registerListener(eventListener);
        }

        this.pluginLogger.info("Essentials plugin enabled successfully!");
    }

    @Override
    public void onDisable() {
        this.pluginLogger.info("Essentials plugin is disabling...");

        // Unregister event listeners
        if (eventListener != null) {
            Server.getInstance().getEventBus().unregisterListener(eventListener);
        }

        this.pluginLogger.info("Essentials plugin disabled!");
    }

    /**
     * Load configuration from config.yml
     * If config file doesn't exist, it will be created with default values
     */
    private void loadConfig() {
        try {
            // Create config file path
            File configFile = this.pluginContainer.dataFolder().resolve("config.yml").toFile();

            // Create default config section
            ConfigSection defaultConfig = new ConfigSection();
            ConfigSection features = new ConfigSection();
            features.put("ping", true);
            features.put("back", true);
            features.put("tpa", true);
            features.put("home", true);
            features.put("warp", true);
            features.put("notice", true);
            defaultConfig.put("features", features);

            // Create default notice section
            ConfigSection noticeSection = new ConfigSection();
            noticeSection.put("content", "Welcome to the server!\\n\\nPlease read the rules and have fun!");
            noticeSection.put("title", "Server Notice");
            defaultConfig.put("notice", noticeSection);

            // Load config with defaults
            config = new Config(configFile, Config.YAML, defaultConfig);

            // If config file doesn't exist, save default config from resources
            if (!configFile.exists()) {
                try (InputStream defaultConfigStream = this.getClass().getResourceAsStream("/config.yml")) {
                    if (defaultConfigStream != null) {
                        config.load(defaultConfigStream);
                        config.save();
                        this.pluginLogger.info("Created default config.yml");
                    }
                } catch (Exception e) {
                    this.pluginLogger.warn("Could not load default config from resources, using hardcoded defaults", e);
                }
            }

            this.pluginLogger.info("Configuration loaded successfully!");
        } catch (Exception e) {
            this.pluginLogger.error("Failed to load configuration!", e);
        }
    }
}
