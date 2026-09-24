# AetherHop

## Overview

AetherHop is a Minecraft mod that lets you connect to a server through a proxy. This can be useful for:

*   Bypassing Network Restrictions: If your network blocks direct access to a server, a proxy can help you circumvent these restrictions.
*   Improving Connection Stability: In some cases, a proxy can provide a more stable connection.
*   Enhanced Privacy: Using a proxy can add an extra layer of privacy to your connection.

## Features

*   Proxy Support: Connect through an HTTP or SOCKS proxy.
*   Configurable Settings: Easily configure your proxy settings within the game.
*   Authentication: Supports proxy servers that require username and password authentication.
*   Easy to use: The mod is easy to use and configure.

## Configuration

The mod's configuration can be accessed in-game via Mod Menu. You can configure the following settings:

*   Proxy Enabled: Toggle the use of a proxy on or off.
*   Proxy Address: The address of your proxy server (e.g., `proxy.example.com:8080`).
*   Proxy Type: HTTP or SOCKS.
*   Proxy Username: The username for proxy authentication (if required).
*   Proxy Password: The password for proxy authentication (if required).

## Supported versions

Fabric only. One jar per group of Minecraft versions:

| Minecraft | Jar |
|---|---|
| 1.21 – 1.21.10 | `aetherhop-<version>+mc1.21-1.21.10.jar` |
| 1.21.11 | `aetherhop-<version>+mc1.21.11.jar` |
| 26.1 – 26.1.2 | `aetherhop-<version>+mc26.1-26.1.2.jar` |
| 26.2 – 26.3 | `aetherhop-<version>+mc26.2-26.3.jar` |

## Requirements

*   [Fabric Loader](https://fabricmc.net/) and a supported Minecraft version (see above).
*   **[Cloth Config](https://modrinth.com/mod/cloth-config)** — required, it provides the settings screen. Without it the game reports the missing mod at startup.
*   [Mod Menu](https://modrinth.com/mod/modmenu) — optional. The **Proxy** button on the multiplayer server list works without it.

## Installation

1.  Ensure you have a compatible version of Minecraft, Fabric Loader and Cloth Config installed.
2.  Download the latest release of the AetherHop mod for your Minecraft version.
3.  Place the downloaded `.jar` file into your Minecraft `mods` folder.
4.  Launch Minecraft and enjoy!

## Usage

1.  Once the mod is installed, launch Minecraft.
2.  Open the mod's configuration menu through Mod Menu.
3.  Enter your proxy details (address, type, username, password).
4.  Enable the proxy.
5.  Connect to a server as usual. Your connection will now be routed through the configured proxy.

If the proxy is enabled but can't be used (empty or malformed address, unresolvable host, refused login), the connection **fails** with an error in the log instead of going out directly, so you never connect unprotected by accident. Check `logs/latest.log` for lines starting with `AetherHop`.

## Support

If you encounter any issues or have questions, please open an issue on the project's GitHub repository.

## Contributing

Contributions are welcome! If you'd like to contribute to the development of AetherHop, please fork the repository and submit a pull request.

## Development

This project targets Minecraft 1.21 through 26.3 on Fabric via [Stonecutter](https://stonecutter.kikugie.dev/). One shared source tree lives under `src/main/java`; version differences are gated with `//? if <condition> {` comments rather than duplicated per-version folders. (NeoForge and Quilt targets were dropped — Fabric only.)

There are four Stonecutter builds, one per API-compatible group of Minecraft versions. Each is compiled against the group's newest version (the project name) and declares the whole group in `fabric.mod.json`:

| Build | Covers | Why it's a separate build |
|---|---|---|
| `1.21.10` | 1.21 – 1.21.10 | `Connection.connect(…, boolean, …)`, `Minecraft.setScreen` |
| `1.21.11` | 1.21.11 | `Connection.connect` takes an `EventLoopGroupHolder` |
| `26.1.2` | 26.1 – 26.1.2 | unobfuscated jars, same API as 26.1.x |
| `26.3` | 26.2 – 26.3 | `Minecraft.setScreen` removed in 26.2 → `setScreenAndShow`; 26.3 verified identical for everything the mod uses |

The groups were verified member-by-member (official mappings for 1.21.x, `javap` on the client jars for 26.x, `javap` on Cloth Config across versions). Re-check every API AetherHop touches before widening a group or adding a Minecraft version.

- Edit code against the version set in `.sc_active_version` (currently `26.3`).
- Switch the active version with `./gradlew stonecutterSwitchTo<version>`, e.g. `./gradlew stonecutterSwitchTo1.21.10`.
- Build one group: `./gradlew :<build>:build` (e.g. `:26.3:build`).
- Per-build dependency coordinates (Fabric API, Cloth Config, Mod Menu, Netty) and each jar's Minecraft range live as Kotlin maps at the top of `stonecutter.gradle.kts`.
- Minecraft bundles neither `netty-handler-proxy` nor `netty-codec-socks` (and `netty-codec-http` only from 1.21.9), so the build jar-in-jars them — `nettyModules` in `stonecutter.gradle.kts`.
- `libs/` holds Mod Menu jars (flatDir fallback) because `maven.terraformersmc.com` has a deterministic incompatibility with Gradle's HTTP client; Modrinth's maven proxy (`maven.modrinth:modmenu:<version>`) is the primary source and occasionally has its own outages. Fetch the fallback jars from the **Modrinth CDN** and check them against the SHA-1 Modrinth publishes — the same terraformersmc server silently truncates `curl` downloads too, and `file`/header checks won't notice (use `unzip -t`, or the hash).
- `@Inject`/`@Mixin` target method strings are plain strings, not checked by `javac` — a clean build does **not** prove a mixin target is correct. Verify against the real Mojang mappings (or a decompiled/`javap`'d class) for the exact Minecraft version before trusting one, especially across a version range this wide.
