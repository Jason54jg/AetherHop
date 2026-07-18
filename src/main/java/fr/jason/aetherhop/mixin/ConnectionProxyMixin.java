package fr.jason.aetherhop.mixin;

import fr.jason.aetherhop.AetherHop;
import fr.jason.aetherhop.proxy.ProxyConnector;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
import net.minecraft.network.BandwidthDebugMonitor;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * Routes the outgoing server connection through the configured SOCKS/HTTP proxy.
 * <p>
 * Unlike the 1.8.9 version of this mod, this doesn't need a custom {@code SocketChannel}
 * factory or an ASM coremod: modern Netty ships {@link Socks5ProxyHandler}/{@link HttpProxyHandler}
 * that perform the proxy handshake as the first pipeline stage, so a plain {@link NioSocketChannel}
 * works unchanged.
 */
@Mixin(Connection.class)
public abstract class ConnectionProxyMixin {

    @Shadow
    private static void configureSerialization(io.netty.channel.ChannelPipeline pipeline, PacketFlow flow, boolean transferring, BandwidthDebugMonitor monitor) {
        throw new AssertionError();
    }

    // Mojang replaced the raw `boolean useEpoll` parameter with an EventLoopGroupHolder
    // starting 1.21.11 (same release as the ResourceLocation->Identifier rename below).
    // Verified against both versions' official mappings - see mc_ecosystem_facts_2026 memory.
    //? if <1.21.11 {
    /*@Inject(method = "connect(Ljava/net/InetSocketAddress;ZLnet/minecraft/network/Connection;)Lio/netty/channel/ChannelFuture;", at = @At("HEAD"), cancellable = true)
    private static void aetherHop$connect(InetSocketAddress address, boolean useEpoll, Connection connection, CallbackInfoReturnable<ChannelFuture> cir) {
    *///? } else {
    @Inject(method = "connect(Ljava/net/InetSocketAddress;Lnet/minecraft/server/network/EventLoopGroupHolder;Lnet/minecraft/network/Connection;)Lio/netty/channel/ChannelFuture;", at = @At("HEAD"), cancellable = true)
    private static void aetherHop$connect(InetSocketAddress address, net.minecraft.server.network.EventLoopGroupHolder eventLoopGroupHolder, Connection connection, CallbackInfoReturnable<ChannelFuture> cir) {
    //? }
        Proxy proxy = ProxyConnector.resolve(AetherHop.config());
        if (proxy == null) return;

        Bootstrap bootstrap = new Bootstrap()
            .group(new NioEventLoopGroup(0))
            .channel(NioSocketChannel.class)
            .handler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel channel) {
                    channel.config().setOption(ChannelOption.TCP_NODELAY, true);
                    InetSocketAddress proxyAddress = (InetSocketAddress) proxy.address();
                    if (proxy.type() == Proxy.Type.SOCKS) {
                        channel.pipeline().addLast("proxy", new Socks5ProxyHandler(proxyAddress));
                    } else {
                        channel.pipeline().addLast("proxy", new HttpProxyHandler(proxyAddress));
                    }
                    configureSerialization(channel.pipeline(), PacketFlow.CLIENTBOUND, false, null);
                    channel.pipeline().addLast("packet_handler", connection);
                }
            });

        ChannelFuture future = bootstrap.connect(address.getHostString(), address.getPort()).syncUninterruptibly();
        cir.setReturnValue(future);
        cir.cancel();
    }
}
