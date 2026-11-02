package fr.jason.aetherhop.config;

import fr.jason.aetherhop.proxy.ProxyConnector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.ConnectException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ProxyConfigTest {
    @TempDir Path directory;

    @Test
    void missingFileStartsWithProxyDisabled() {
        ProxyConfig config = ProxyConfig.load(directory.resolve("missing.json"));
        assertFalse(config.enabled);
        assertFalse(config.hasLoadError());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "null", "[]", "{\"enabled\":true,", "{\"address\":null}",
        "{\"username\":null}", "{\"password\":null}", "{\"type\":null}", "{\"type\":\"SOCKS5\"}",
        "{\"enabled\":null}", "{\"enabled\":\"false\"}", "{\"enabled\":42}"})
    void damagedFileRemainsIntactAndBlocksConnectionsUntilSaved(String json) throws Exception {
        Path file = directory.resolve("aetherhop.json");
        Files.writeString(file, json);
        ProxyConfig config = assertDoesNotThrow(() -> ProxyConfig.load(file));
        assertTrue(config.enabled);
        assertTrue(config.hasLoadError());
        assertEquals(json, Files.readString(file));
        assertThrows(ConnectException.class, () -> ProxyConnector.resolve(config));

        // An in-memory toggle alone must not clear the recovery lock.
        config.enabled = false;
        assertThrows(ConnectException.class, () -> ProxyConnector.resolve(config));
        config.save(file);
        assertFalse(config.hasLoadError());
        assertNull(ProxyConnector.resolve(config));
        ProxyConfig reloaded = ProxyConfig.load(file);
        assertFalse(reloaded.hasLoadError());
        assertFalse(reloaded.enabled);
        assertFalse(Files.readString(file).contains("loadError"));
    }

    @Test
    void unreadablePathBlocksConnectionsWithoutCrashingStartup() {
        ProxyConfig config = assertDoesNotThrow(() -> ProxyConfig.load(directory));
        assertTrue(config.hasLoadError());
        assertThrows(ConnectException.class, () -> ProxyConnector.resolve(config));
    }

    @Test
    void failedSaveDoesNotUnlockDirectConnectionsOrOverwriteTheDamagedFile() throws Exception {
        Path original = directory.resolve("aetherhop.json");
        Files.writeString(original, "{broken");
        ProxyConfig config = ProxyConfig.load(original);
        config.enabled = false;
        assertThrows(RuntimeException.class, () -> config.save(directory));
        assertTrue(config.hasLoadError());
        assertThrows(ConnectException.class, () -> ProxyConnector.resolve(config));
        assertEquals("{broken", Files.readString(original));
    }

    @Test
    void savingReplacesTheFileAndKeepsAllSettings() throws Exception {
        Path file = directory.resolve("nested/aetherhop.json");
        ProxyConfig config = new ProxyConfig();
        config.enabled = true;
        config.type = ProxyConfig.Type.SOCKS;
        config.address = "[::1]:1080";
        config.username = "test-user";
        config.password = "test-password";
        config.save(file);
        config.password = "changed-password";
        config.save(file);

        ProxyConfig loaded = ProxyConfig.load(file);
        assertTrue(loaded.enabled);
        assertFalse(loaded.hasLoadError());
        assertEquals(config.address, loaded.address);
        assertEquals(config.type, loaded.type);
        assertEquals(config.username, loaded.username);
        assertEquals(config.password, loaded.password);
        try (var files = Files.list(file.getParent())) {
            assertEquals(1, files.count(), "Temporary files must be removed");
        }
    }
}
