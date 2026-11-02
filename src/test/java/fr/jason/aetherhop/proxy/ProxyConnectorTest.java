package fr.jason.aetherhop.proxy;

import fr.jason.aetherhop.config.ProxyConfig;
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.ConnectException;
import java.net.Inet6Address;

import static org.junit.jupiter.api.Assertions.*;

class ProxyConnectorTest {
    private ProxyConfig config(String address) {
        ProxyConfig config = new ProxyConfig();
        config.enabled = true;
        config.address = address;
        return config;
    }

    @Test
    void disabledProxyHasNoTarget() throws Exception {
        assertNull(ProxyConnector.resolve(new ProxyConfig()));
    }

    @Test
    void ipv4AndHostnameAddressesStillWork() throws Exception {
        assertEquals(8080, ProxyConnector.resolve(config(" 127.0.0.1 : 8080 ")).address().getPort());
        assertFalse(ProxyConnector.resolve(config("localhost:1080")).address().isUnresolved());
    }

    @Test
    void bracketedIpv6IsResolvedWithItsPort() throws Exception {
        var target = ProxyConnector.resolve(config("[::1]:1080"));
        assertInstanceOf(Inet6Address.class, target.address().getAddress());
        assertEquals(1080, target.address().getPort());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "127.0.0.1", "127.0.0.1:abc", "127.0.0.1:0", "127.0.0.1:65536",
        "::1:1080", "[::1]", "[::1:1080", "[::1]1080", "[::1]:", "[::1]:1080:80", "[localhost]:80"})
    void invalidAddressesFailWithAConnectionError(String address) {
        assertThrows(ConnectException.class, () -> ProxyConnector.resolve(config(address)));
    }

    @Test
    void missingValuesFailWithAConnectionError() {
        assertThrows(ConnectException.class, () -> ProxyConnector.resolve(null));
        ProxyConfig config = config("127.0.0.1:8080");
        config.address = null;
        assertThrows(ConnectException.class, () -> ProxyConnector.resolve(config));
        config.address = "127.0.0.1:8080";
        config.type = null;
        assertThrows(ConnectException.class, () -> ProxyConnector.resolve(config));
    }

    @Test
    void proxyTypesCreateIndependentHandlers() throws Exception {
        ProxyConfig config = config("127.0.0.1:8080");
        var http = ProxyConnector.resolve(config);
        assertInstanceOf(HttpProxyHandler.class, http.newHandler());
        assertNotSame(http.newHandler(), http.newHandler());
        config.type = ProxyConfig.Type.SOCKS;
        assertInstanceOf(Socks5ProxyHandler.class, ProxyConnector.resolve(config).newHandler());
    }
}
