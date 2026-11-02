package fr.jason.aetherhop.mixin;

import fr.jason.aetherhop.AetherHop;
import fr.jason.aetherhop.config.ProxyConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ServerStatusPingerMixinTest {
    @TempDir Path directory;

    private void setConfig(ProxyConfig config) throws Exception {
        var field = AetherHop.class.getDeclaredField("config");
        field.setAccessible(true);
        field.set(null, config);
    }

    @AfterEach
    void clearConfig() throws Exception {
        setConfig(null);
    }

    private boolean fallbackBlocked(ProxyConfig config) throws Exception {
        setConfig(config);
        var method = ServerStatusPingerMixin.class.getDeclaredMethod("aetherHop$blockDirectFallback", CallbackInfo.class);
        method.setAccessible(true);
        var callback = new CallbackInfo("pingLegacyServer", true);
        method.invoke(new ServerStatusPingerMixin() {}, callback);
        return callback.isCancelled();
    }

    @Test
    void enabledProxyPreventsDirectFallback() throws Exception {
        ProxyConfig config = new ProxyConfig();
        config.enabled = true;
        assertTrue(fallbackBlocked(config));
    }

    @Test
    void disabledProxyPreservesVanillaFallback() throws Exception {
        assertFalse(fallbackBlocked(new ProxyConfig()));
    }

    @Test
    void damagedOrUninitializedConfigNeverAllowsDirectFallback() throws Exception {
        assertTrue(fallbackBlocked(null));
        Path file = directory.resolve("aetherhop.json");
        Files.writeString(file, "{broken");
        ProxyConfig config = ProxyConfig.load(file);
        config.enabled = false;
        assertTrue(fallbackBlocked(config));
    }
}
