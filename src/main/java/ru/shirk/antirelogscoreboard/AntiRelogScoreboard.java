package ru.shirk.antirelogscoreboard;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import ru.shirk.antirelogscoreboard.boards.BoardManager;
import ru.shirk.antirelogscoreboard.commands.ReloadCommand;
import ru.shirk.antirelogscoreboard.configs.ConfigurationManager;
import ru.shirk.antirelogscoreboard.listeners.Events;

import java.io.File;
import java.util.Objects;

public final class AntiRelogScoreboard extends JavaPlugin {

    @Getter
    private static AntiRelogScoreboard instance;
    @Getter
    private static final BoardManager boardManager = new BoardManager();
    @Getter
    private static final ConfigurationManager configurationManager = new ConfigurationManager();

    @Override
    public void onEnable() {
        if (!this.getServer().getPluginManager().isPluginEnabled("TAB") ||
                !this.getServer().getPluginManager().isPluginEnabled("AntiRelog")) {
            this.getLogger().severe("Плагин не может работать без TAB и AntiRelog, " +
                    "пожалуйста установите эти плагины для корректной работы.");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        instance = this;
        loadConfigs();
        Objects.requireNonNull(this.getCommand("antirelogscoreboard")).setExecutor(new ReloadCommand());
        Objects.requireNonNull(this.getCommand("antirelogscoreboard")).setTabCompleter(new ReloadCommand());
        this.getServer().getPluginManager().registerEvents(new Events(), this);
    }

    @Override
    public void onDisable() {
        boardManager.resetAll();
        instance = null;
    }

    private void loadConfigs() {
        try {
            if (!(new File(getDataFolder(), "settings.yml")).exists()) {
                getConfigurationManager().createFile("settings.yml");
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
