package fr.jason.aetherhop.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Plain, loader-agnostic settings store. Cloth Config only builds the screen; this owns persistence. */
public class ProxyConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER = LoggerFactory.getLogger("AetherHop");

    private transient boolean loadError;

    public boolean enabled = false;
    public String address = "";
    public String username = "";
    public String password = "";
    public Type type = Type.HTTP;

    public enum Type {
        HTTP, SOCKS
    }

    public static ProxyConfig load(Path file) {
        if (Files.notExists(file)) return new ProxyConfig();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            JsonElement json = JsonParser.parseReader(reader);
            if (!json.isJsonObject()) throw new JsonParseException("Expected a configuration object");
            JsonObject object = json.getAsJsonObject();
            if (object.has("enabled") && (!object.get("enabled").isJsonPrimitive()
                || !object.getAsJsonPrimitive("enabled").isBoolean())) {
                throw new JsonParseException("Expected a boolean for enabled");
            }
            for (String key : new String[]{"address", "username", "password", "type"}) {
                if (object.has(key) && (!object.get(key).isJsonPrimitive()
                    || !object.getAsJsonPrimitive(key).isString())) {
                    throw new JsonParseException("Expected a string for " + key);
                }
            }
            ProxyConfig config = GSON.fromJson(object, ProxyConfig.class);
            if (config.type == null) throw new JsonParseException("Unknown proxy type");
            return config;
        } catch (IOException | JsonParseException e) {
            // Do not fall back to a disabled proxy: a damaged file must not expose the user's IP.
            // Keep the original file intact until the user explicitly saves the repaired settings.
            LOGGER.error("Could not load proxy config from {} ({}). Connections are blocked until the settings are reviewed and saved.",
                file, e.getClass().getSimpleName());
            ProxyConfig config = new ProxyConfig();
            config.enabled = true;
            config.loadError = true;
            return config;
        }
    }

    public boolean hasLoadError() {
        return loadError;
    }

    public void save(Path file) {
        Path temporary = null;
        try {
            Path destination = file.toAbsolutePath();
            Files.createDirectories(destination.getParent());
            temporary = Files.createTempFile(destination.getParent(), destination.getFileName().toString(), ".tmp");
            try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            }
            try {
                Files.move(temporary, destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING);
            }
            loadError = false;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save Proxy config to " + file, e);
        } finally {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException e) {
                    LOGGER.warn("Could not remove temporary proxy config {}", temporary);
                }
            }
        }
    }
}
