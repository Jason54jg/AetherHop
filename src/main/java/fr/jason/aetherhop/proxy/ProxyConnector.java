package fr.jason.aetherhop.proxy;

import fr.jason.aetherhop.config.ProxyConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;

/** Turns the saved {@link ProxyConfig} into a connectable {@link java.net.Proxy}, or null if disabled/unset. */
public final class ProxyConnector {
    private static final Logger LOGGER = LoggerFactory.getLogger("AetherHop");

    private ProxyConnector() {
    }

    public static java.net.Proxy resolve(ProxyConfig config) {
        if (!config.enabled) {
            LOGGER.info("Proxy disabled in config, connecting directly");
            return null;
        }
        if (config.address.isEmpty()) {
            LOGGER.warn("Proxy enabled but address is empty, connecting directly");
            return null;
        }

        String[] parts = config.address.split(":", 2);
        if (parts.length != 2) {
            LOGGER.warn("Proxy address '{}' is not in host:port format, connecting directly", config.address);
            return null;
        }
        int port;
        try {
            port = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            LOGGER.warn("Proxy port '{}' is not a number, connecting directly", parts[1]);
            return null;
        }
        InetSocketAddress address = InetSocketAddress.createUnresolved(parts[0], port);
        LOGGER.info("Routing connection through {} proxy {}:{}", config.type, parts[0], port);

        if (!config.username.isEmpty() && !config.password.isEmpty()) {
            Authenticator.setDefault(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(config.username, config.password.toCharArray());
                }
            });
        }

        java.net.Proxy.Type type = config.type == ProxyConfig.Type.SOCKS
            ? java.net.Proxy.Type.SOCKS
            : java.net.Proxy.Type.HTTP;
        return new java.net.Proxy(type, address);
    }
}
