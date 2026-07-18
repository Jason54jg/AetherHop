package fr.jason.aetherhop.proxy;

import fr.jason.aetherhop.config.ProxyConfig;

import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;

/** Turns the saved {@link ProxyConfig} into a connectable {@link java.net.Proxy}, or null if disabled/unset. */
public final class ProxyConnector {
    private ProxyConnector() {
    }

    public static java.net.Proxy resolve(ProxyConfig config) {
        if (!config.enabled || config.address.isEmpty()) return null;

        String[] parts = config.address.split(":", 2);
        if (parts.length != 2) return null;
        InetSocketAddress address = InetSocketAddress.createUnresolved(parts[0], Integer.parseInt(parts[1]));

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
