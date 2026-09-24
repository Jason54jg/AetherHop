package fr.jason.aetherhop.proxy;

import fr.jason.aetherhop.AetherHop;
import fr.jason.aetherhop.config.ProxyConfig;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.handler.proxy.ProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
import io.netty.util.internal.PlatformDependent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * Turns the saved {@link ProxyConfig} into a Netty proxy handler and slots it in front of
 * Minecraft's own connection pipeline.
 */
public final class ProxyConnector {
    private static final Logger LOGGER = LoggerFactory.getLogger("AetherHop");

    private ProxyConnector() {
    }

    /** A validated proxy: the address is already resolved, which Netty's {@link ProxyHandler} requires. */
    public record Target(ProxyConfig.Type type, InetSocketAddress address, String username, String password) {
        /** A new handler; they are stateful, so every channel needs its own. */
        public ProxyHandler newHandler() {
            boolean authenticated = !username.isEmpty();
            // The credentials must go to Netty itself: java.net.Authenticator only affects the JDK's own
            // HTTP/socket stack, so a proxy that requires a login rejected every connection without it.
            return switch (type) {
                case SOCKS -> authenticated
                    ? new Socks5ProxyHandler(address, username, password)
                    : new Socks5ProxyHandler(address);
                case HTTP -> authenticated
                    ? new HttpProxyHandler(address, username, password)
                    : new HttpProxyHandler(address);
            };
        }

        String describe() {
            return type + " proxy " + address.getHostString() + ":" + address.getPort()
                + (username.isEmpty() ? "" : " (with login)");
        }
    }

    /**
     * @return the proxy to route through, or {@code null} when the proxy is switched off
     * @throws ConnectException when the proxy is switched on but can't be used. The connection then
     *                          fails instead of silently going out directly, which would expose your real IP.
     */
    public static Target resolve(ProxyConfig config) throws ConnectException {
        if (!config.enabled) return null;

        String address = config.address.trim();
        if (address.isEmpty()) {
            throw new ConnectException("AetherHop: the proxy is enabled but no address is set");
        }
        String[] parts = address.split(":", 2);
        if (parts.length != 2 || parts[0].isEmpty()) {
            throw new ConnectException("AetherHop: proxy address '" + address + "' is not in host:port format");
        }

        int port;
        try {
            port = Integer.parseInt(parts[1].trim());
        } catch (NumberFormatException e) {
            throw new ConnectException("AetherHop: proxy port '" + parts[1] + "' is not a number");
        }
        if (port < 1 || port > 65535) {
            throw new ConnectException("AetherHop: proxy port " + port + " is out of range (1-65535)");
        }

        // Must be resolved: Netty's ProxyHandler connects to this address directly, bypassing the
        // bootstrap's resolver, and an unresolved address makes that connect fail.
        InetSocketAddress resolved = new InetSocketAddress(parts[0].trim(), port);
        if (resolved.isUnresolved()) {
            throw new ConnectException("AetherHop: can't resolve the proxy host '" + parts[0] + "'");
        }
        return new Target(config.type, resolved, config.username, config.password);
    }

    /**
     * Wraps Minecraft's own channel initializer so the proxy handler sits first in the pipeline.
     * Everything else (read timeout, packet framing, bandwidth monitor, Minecraft's handlers) stays
     * exactly as vanilla builds it, and Minecraft's event loop is reused.
     */
    public static ChannelHandler wrap(ChannelHandler vanilla) {
        Target target;
        try {
            target = resolve(AetherHop.config());
        } catch (ConnectException e) {
            LOGGER.error(e.getMessage());
            PlatformDependent.throwException(e);
            return vanilla; // unreachable, throwException never returns
        }
        if (target == null) {
            LOGGER.debug("Proxy disabled, connecting directly");
            return vanilla;
        }

        ProxyHandler proxy = target.newHandler();
        LOGGER.debug("Routing connection through {}", target.describe());
        proxy.connectFuture().addListener(future -> {
            SocketAddress destination = proxy.destinationAddress();
            if (future.isSuccess()) {
                LOGGER.info("Connected to {} through the {}", destination, target.describe());
            } else {
                LOGGER.error("The {} could not connect to {}: {}", target.describe(), destination, future.cause());
            }
        });

        return new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel channel) {
                channel.pipeline().addLast("aetherhop_proxy", proxy);
                channel.pipeline().addLast(vanilla);
                channel.closeFuture().addListener(closed -> {
                    if (!proxy.connectFuture().isDone()) {
                        LOGGER.error("Could not reach the {} (connection closed before the tunnel was set up)", target.describe());
                    }
                });
            }
        };
    }
}
