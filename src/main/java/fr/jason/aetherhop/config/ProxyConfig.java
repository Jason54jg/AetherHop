package fr.jason.aetherhop.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Plain, loader-agnostic settings store. Cloth Config only builds the screen; this owns persistence. */
public class ProxyConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public boolean enabled = false;
    public String address = "";
    public String username = "";
    public String password = "";
    public Type type = Type.HTTP;

    public enum Type {
        HTTP, SOCKS
    }

    public static ProxyConfig load(Path file) {
        if (!Files.isRegularFile(file)) return new ProxyConfig();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            ProxyConfig config = GSON.fromJson(reader, ProxyConfig.class);
            return config != null ? config : new ProxyConfig();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load Proxy config from " + file, e);
        }
    }

    public void save(Path file) {
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save Proxy config to " + file, e);
        }
    }
}
