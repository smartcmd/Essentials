package me.daoge.essentials;

import lombok.Getter;
import me.daoge.essentials.command.BackCommand;
import me.daoge.essentials.command.HomeCommand;
import me.daoge.essentials.command.PingCommand;
import me.daoge.essentials.command.TpaCommand;
import me.daoge.essentials.command.WarpCommand;

import org.allaymc.api.command.CommandRegistry;
import org.allaymc.api.eventbus.EventBus;
import org.allaymc.api.plugin.Plugin;
import org.allaymc.api.registry.Registries;
import org.allaymc.api.server.Server;

/**
 * Essentials plugin for Allay server
 * 
 * @author daoge
 */
public class EssentialsPlugin extends Plugin {
    
    @Getter
    private static EssentialsPlugin instance;
    
    private Listener deathListener;
    @Getter
    private WarpManager warpManager;
    @Getter
    private HomeManager homeManager;
    
    @Override
    public void onLoad() {
        instance = this;
        this.pluginLogger.info("Essentials plugin is loading...");
    }
    
    @Override
    public void onEnable() {
        this.pluginLogger.info("Essentials plugin is enabling...");
        
        // Initialize managers
        warpManager = new WarpManager(this.pluginContainer.dataFolder());
        homeManager = new HomeManager(this.pluginContainer.dataFolder());
        
        // Register commands
        CommandRegistry commandRegistry = Registries.COMMANDS;
        commandRegistry.register(new TpaCommand());
        commandRegistry.register(new BackCommand());
        commandRegistry.register(new PingCommand());
        commandRegistry.register(new WarpCommand(warpManager));
        commandRegistry.register(new HomeCommand(homeManager));
        
        // Register event listeners
        EventBus eventBus = Server.getInstance().getEventBus();
        deathListener = new Listener();
        eventBus.registerListener(deathListener);
        
        this.pluginLogger.info("Essentials plugin enabled successfully!");
    }
    
    @Override
    public void onDisable() {
        this.pluginLogger.info("Essentials plugin is disabling...");
        
        // Unregister event listeners
        if (deathListener != null) {
            Server.getInstance().getEventBus().unregisterListener(deathListener);
        }
        
        this.pluginLogger.info("Essentials plugin disabled!");
    }
}
