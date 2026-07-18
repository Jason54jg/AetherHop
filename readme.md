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

## Installation

1.  Ensure you have a compatible version of Minecraft and Fabric Loader installed.
2.  Download the latest release of the AetherHop mod.
3.  Place the downloaded `.jar` file into your Minecraft `mods` folder.
4.  Launch Minecraft and enjoy!

## Usage

1.  Once the mod is installed, launch Minecraft.
2.  Open the mod's configuration menu through Mod Menu.
3.  Enter your proxy details (address, type, username, password).
4.  Enable the proxy.
5.  Connect to a server as usual. Your connection will now be routed through the configured proxy.

## Support

If you encounter any issues or have questions, please open an issue on the project's GitHub repository.

## Contributing

Contributions are welcome! If you'd like to contribute to the development of AetherHop, please fork the repository and submit a pull request.

## Development

This project targets Minecraft 1.21 through 26.2 on Fabric via [Stonecutter](https://stonecutter.kikugie.dev/). One shared source tree lives under `src/main/java`; version differences are gated with `//? if <condition> {` comments rather than duplicated per-version folders. (NeoForge and Quilt targets were dropped — Fabric only.)

- Edit code against the version set in `.sc_active_version` (currently `26.2`).
- Switch the active version with `./gradlew stonecutterSwitchTo<version>`, e.g. `./gradlew stonecutterSwitchTo1.21`.
- Build one version: `./gradlew :<version>:build` (e.g. `:26.2:build`).
- Per-version dependency coordinates (Fabric API, Cloth Config, Mod Menu, Netty) live as Kotlin maps at the top of `stonecutter.gradle.kts` — add an entry there for any new Minecraft version.
- `libs/` holds Mod Menu jars fetched directly (flatDir fallback) because `maven.terraformersmc.com` has a deterministic incompatibility with Gradle's HTTP client; Modrinth's maven proxy (`maven.modrinth:modmenu:<version>`) is the primary source and occasionally has its own outages.
- `@Inject`/`@Mixin` target method strings are plain strings, not checked by `javac` — a clean build does **not** prove a mixin target is correct. Verify against the real Mojang mappings (or a decompiled/`javap`'d class) for the exact Minecraft version before trusting one, especially across a version range this wide.
