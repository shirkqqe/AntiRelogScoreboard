package ru.shirk.antirelogscoreboard.configs;

import ru.shirk.antirelogscoreboard.AntiRelogScoreboard;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;

public class ConfigurationManager {

    private final HashMap<String, Configuration> Configs;

    public ConfigurationManager() {
        this.Configs = new HashMap<>();
    }


    public Configuration getConfig(String name) {
        if (this.Configs.containsKey(name)) {
            return this.Configs.get(name);
        }
        Configuration config = new Configuration(AntiRelogScoreboard.getInstance(), name);
        this.Configs.put(name, config);
        return config;
    }

    public void reloadConfigs() {
        for (Configuration c : Configs.values()) c.reload();
    }


    public void createFile(String name) {
        InputStream is = null;
        OutputStream os = null;
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            is = classLoader.getResourceAsStream(name.replace('\\', '/'));


            if (is == null) {
                AntiRelogScoreboard.getInstance().getSLF4JLogger().warn("FAILED TO CREATE PLUGIN CONFIG! [No source file in the plugin jar]");
            } else {
                AntiRelogScoreboard.getInstance().getDataFolder().mkdirs();
                os = Files.newOutputStream(new File(AntiRelogScoreboard.getInstance().getDataFolder(), name).toPath());
                byte[] buffer = new byte[99];
                int length = is.read(buffer);
                while (length > 0) {
                    os.write(buffer, 0, length);
                    length = is.read(buffer);
                }
            }
        } catch (IOException ex) {
            AntiRelogScoreboard.getInstance().getSLF4JLogger().warn("FAILED TO CREATE PLUGIN CONFIG! [IOException 1]");
            throw new RuntimeException(ex.getMessage(), ex);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ex) {
                AntiRelogScoreboard.getInstance().getSLF4JLogger().warn("FAILED TO CREATE PLUGIN CONFIG! [IOException 2]");
                ex.printStackTrace();
            }
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException ex) {
                AntiRelogScoreboard.getInstance().getSLF4JLogger().warn("FAILED TO CREATE PLUGIN CONFIG! [IOException 3]");
                ex.printStackTrace();
            }
        }
    }
}
