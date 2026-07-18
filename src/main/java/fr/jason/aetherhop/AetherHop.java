package fr.jason.aetherhop;

import fr.jason.aetherhop.config.ProxyConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public final class AetherHop implements ClientModInitializer {
    public static final String MOD_ID = /*$ mod_id*/ "aetherhop";
    public static final String VERSION = /*$ mod_version*/ "1.0.0";

    private static ProxyConfig config;

    @Override
    public void onInitializeClient() {
        config = ProxyConfig.load(configFile());
    }

    public static ProxyConfig config() {
        return config;
    }

    public static void saveConfig() {
        config.save(configFile());
    }

    private static Path configFile() {
        return FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + ".json");
    }
}
