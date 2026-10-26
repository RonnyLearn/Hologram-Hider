# Hologram Hider

A client-side Minecraft mod that hides holograms on servers: the floating text of invisible armor stands, and optionally mob name tags and text displays. Everything happens on your client only, nothing is sent to the server.

## Controls

| Key | Action |
|---|---|
| H | Hide / show holograms |
| J | Hide / show mob names |
| K | Hide / show text displays (1.19.4+) |
| Left Alt (hold) | Show everything while held |

Keys can be changed in the Controls settings.

## Commands

| Command | What it does |
|---|---|
| `/hl toggle` | Same as H |
| `/hl mobs` | Same as J |
| `/hl displays` | Same as K |
| `/hl main mobs` | Make H hide mob names too |
| `/hl main displays` | Make H hide text displays too |
| `/hl info` | Current settings |
| `/hl nearby` | How many holograms are nearby |
| `/hl sound` | Turn the toggle sound on / off |
| `/hl help` | List of commands |

## Download

Get the mod from the official page: [modrinth.com/mod/hologram-hider](https://modrinth.com/mod/hologram-hider)

## Source code

Each folder is a separate Gradle project for its game version and mod loader.

| Minecraft | Fabric | Forge |
|---|---|---|
| 26.3 | [fabric/26.3](fabric/26.3) | [forge/26.3](forge/26.3) |
| 26.2 | [fabric/26.2](fabric/26.2) | [forge/26.2](forge/26.2) |
| 26.1.2 | [fabric/26.1.2](fabric/26.1.2) | [forge/26.1.2](forge/26.1.2) |
| 26.1.1 | [fabric/26.1.1](fabric/26.1.1) | [forge/26.1.1](forge/26.1.1) |
| 1.21.4 | [fabric/1.21.4](fabric/1.21.4) | [forge/1.21.4](forge/1.21.4) |
| 1.21.1 | [fabric/1.21.1](fabric/1.21.1) | [forge/1.21.1](forge/1.21.1) |
| 1.21 | [fabric/1.21](fabric/1.21) | [forge/1.21](forge/1.21) |
| 1.20.4 | [fabric/1.20.4](fabric/1.20.4) | [forge/1.20.4](forge/1.20.4) |
| 1.20.3 | [fabric/1.20.3](fabric/1.20.3) | [forge/1.20.3](forge/1.20.3) |
| 1.20.2 | [fabric/1.20.2](fabric/1.20.2) | [forge/1.20.2](forge/1.20.2) |
| 1.20.1 | [fabric/1.20.1](fabric/1.20.1) | [forge/1.20.1](forge/1.20.1) |
| 1.20 | [fabric/1.20](fabric/1.20) | [forge/1.20](forge/1.20) |
| 1.19.2 | [fabric/1.19.2](fabric/1.19.2) | [forge/1.19.2](forge/1.19.2) |
| 1.19.1 | [fabric/1.19.1](fabric/1.19.1) | [forge/1.19.1](forge/1.19.1) |
| 1.18.2 | [fabric/1.18.2](fabric/1.18.2) | [forge/1.18.2](forge/1.18.2) |
| 1.16.5 | [fabric/1.16.5](fabric/1.16.5) | [forge/1.16.5](forge/1.16.5) |
| 1.12.2 | [fabric/1.12.2](fabric/1.12.2) | [forge/1.12.2](forge/1.12.2) |

The 1.12.2 Fabric version is built on Legacy Fabric. Fabric versions require Fabric API.

To build, run `gradlew build` in the folder of the version you need. The jar appears in `build/libs`.

## License

All rights reserved. See [LICENSE](LICENSE).
