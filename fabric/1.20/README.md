# Hologram Hider

Client-side Fabric mod for Minecraft 26.2 that hides armor-stand hologram text.

## Features

- Toggle hologram visibility with a configurable key (default: `H`).
- Temporarily show or hide holograms while holding a configurable key (default: `Left Alt`).
- Count hidden holograms and show the current status with `/hl info`.
- Show nearby holograms within 32 blocks with `/hl nearby`.
- Enable or disable toggle sounds with `/hl sound`.
- Show all commands in-game with `/hl help`.
- Russian and English localization, selected automatically from the Minecraft language setting.

## Commands

- `/hl toggle`
- `/hl info`
- `/hl nearby`
- `/hl sound`
- `/hl help`

## Building

Requirements: JDK 25.

```powershell
.\gradlew.bat build
```

The built mod JAR is created in `build/libs`.

## License

This project is licensed under the [MIT License](LICENSE).
