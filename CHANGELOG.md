# Changelog

All notable changes to AetherHop are documented here.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project follows [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.3] - 2026-11-02

### Added

- Dedicated build and jar for **Minecraft 26.2** (separate from 26.3).
- Mixin on server list ping: disables the legacy direct-connection status fallback while the proxy is enabled.
- Masked password field in the configuration screen.
- Unit tests for proxy configuration loading, password entry, pinger mixin, and connector validation.

### Changed

- Corrupt or invalid `config/aetherhop.json` no longer falls back to a disabled proxy; connections stay blocked until settings are fixed and saved.
- Configuration is saved via a temporary file, then atomically replaced.
- Stricter validation of proxy address and type before connecting.
- Project icon replaced with the Twemoji “Cyclone” artwork (CC BY 4.0); see `readme.md` and `ICON-LICENSE.txt`.

### Fixed

- Connections no longer bypass the proxy when the proxy is enabled but misconfigured (empty address, bad host, auth failure, etc.).

## [1.0.2] - 2026-09-24

### Added

- Support for **Minecraft 26.3**.

### Changed

- `ConnectionProxyMixin` and `ProxyConnector` updated for newer Minecraft / Netty APIs across the supported range.

## [1.0.0] - 2026-07-18

### Added

- Full rewrite of the mod as **AetherHop** on **Fabric** (successor to “Proxy Hypixel” on 1.8.9 Forge).
- Multi-version builds via Stonecutter for Minecraft **1.21 – 26.2** (one jar per compatible group).
- HTTP and SOCKS proxy routing for multiplayer connections.
- In-game configuration through **Cloth Config**; optional **Mod Menu** entry and multiplayer **Proxy** button.
- Bundled Netty modules required for SOCKS proxy support.

[1.0.3]: https://github.com/Jason54jg/AetherHop/releases/tag/v1.0.3
[1.0.2]: https://github.com/Jason54jg/AetherHop/releases/tag/v1.0.2
[1.0.0]: https://github.com/Jason54jg/AetherHop/releases/tag/v1.0.0
