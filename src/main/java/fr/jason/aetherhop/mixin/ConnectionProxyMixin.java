package fr.jason.aetherhop.mixin;

import fr.jason.aetherhop.proxy.ProxyConnector;
import io.netty.channel.ChannelHandler;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Routes the outgoing server connection through the configured SOCKS/HTTP proxy.
 * <p>
 * Vanilla's static {@code Connection.connect} builds a Netty {@code Bootstrap} and hands it a channel
 * initializer via {@code Bootstrap.handler(...)}. That single call is wrapped so the proxy handler is
 * added in front of whatever vanilla sets up; nothing else is replaced. Unlike the 1.8.9 version of
 * this mod, no custom {@code SocketChannel} factory or ASM coremod is needed: Netty's
 * {@code Socks5ProxyHandler}/{@code HttpProxyHandler} redirect the TCP connect to the proxy themselves.
 * <p>
 * Checked against every supported Minecraft version: {@code connect} contains exactly one
 * {@code Bootstrap.handler(ChannelHandler)} call, on 1.21 through 26.3.
 */
@Mixin(Connection.class)
public abstract class ConnectionProxyMixin {

    // Mojang replaced the raw `boolean useEpoll` parameter with an EventLoopGroupHolder
    // starting 1.21.11 (same release as the ResourceLocation->Identifier rename).
    // Verified against both versions' official mappings - see mc_ecosystem_facts_2026 memory.
    //? if <1.21.11 {
    /*@ModifyArg(
        method = "connect(Ljava/net/InetSocketAddress;ZLnet/minecraft/network/Connection;)Lio/netty/channel/ChannelFuture;",
        at = @At(value = "INVOKE", target = "Lio/netty/bootstrap/Bootstrap;handler(Lio/netty/channel/ChannelHandler;)Lio/netty/bootstrap/AbstractBootstrap;", remap = false),
        index = 0)
    *///? } else {
    @ModifyArg(
        method = "connect(Ljava/net/InetSocketAddress;Lnet/minecraft/server/network/EventLoopGroupHolder;Lnet/minecraft/network/Connection;)Lio/netty/channel/ChannelFuture;",
        at = @At(value = "INVOKE", target = "Lio/netty/bootstrap/Bootstrap;handler(Lio/netty/channel/ChannelHandler;)Lio/netty/bootstrap/AbstractBootstrap;", remap = false),
        index = 0)
    //? }
    private static ChannelHandler aetherHop$routeThroughProxy(ChannelHandler vanillaInitializer) {
        return ProxyConnector.wrap(vanillaInitializer);
    }
}
