# Jade Client

**Jade Client** is a legitimate, performance-and-cosmetic-focused Minecraft client mod for **1.8.9**, built on **Forge**. Lunar/Feather-class polish: real FPS boosts, a clean centered UI, HUD customization, and opt-in cosmetics — with **zero cheat modules**, ever.

> If a feature could get a user banned, it doesn't ship. No Killaura, no ESP, no XRay, no reach, no auto-clicker, no movement cheats. Jade alters rendering and quality-of-life only.

---

## Install

### Official Minecraft Launcher (Forge 1.8.9)

1. Install [Forge 1.8.9 (11.15.1.2318)](https://files.minecraftforge.net/) (Java 8 required).
2. Drop `jade-client-….jar` into your `.minecraft/mods` folder.
3. Launch the `forge 1.8.9` profile. On first launch Jade opens a 3-step onboarding (theme → performance preset → keybinds).

### Prism Launcher / MultiMC

1. Edit the instance → *Install Loader* → **Forge** → 1.8.9-11.15.1.2318.
2. Open the instance's `mods` folder; add the `jade-client-….jar`.
3. (Optional) In *Settings → Java*, make sure Java 8 is selected and allocate 2–4 GB RAM.
4. Launch. Right Shift opens the Jade menu.

### Custom launcher / server pack authors

Point the launcher profile at Forge 1.8.9 (11.15.1.2318) with Java 8, then include the Jade jar in the mods directory. JVM tuning hints for the best Jade experience:

```
-Xms2G -Xmx4G -XX:+UseG1GC -XX:MaxGCPauseMillis=40
```

## Getting started in-game

| Key | Action |
| --- | --- |
| **Right Shift** | Open the Jade menu (centered ClickGUI) |
| **Right Shift ×2** (fast) | Open the HUD editor (drag, snap, scale) |
| **C** | Zoom (configurable) |

In the menu: pick a category on the left, toggle modules in the middle, tweak the selected module's settings on the right. The search bar fuzzy-filters every module.

## Features

### Performance (legit only)
- **Master FPS Boost** switchboard: entity/block-entity culling, smart frustum, occlusion assist, async chunk building, batched meshing, fast math/render, particle limiter, rain-splash removal, low-fire smoke.
- Sliders: particle density, chunk fade speed, entity render distance, smooth FPS cap, **dynamic unfocused FPS cap** (saves battery when alt-tabbed).
- **Presets:** Low / Balanced / High / Jade-Max.
- **Live benchmark:** snapshot your before/after FPS + 1% lows with one button.

### HUD modules
FPS (+1% lows + frametime graph) · Info (coords, biome, dimension, server, ping, direction) · Keystrokes + CPS · Armor status · Potion effects · Clock + session timer · Compass · Watermark + custom text · Scoreboard tweaks · Chat tweaks. All movable/resizable via the HUD editor with snapping + alignment guides.

### Visuals (cosmetic)
Motion blur · Fullbright (clearly labeled as server-rule-dependent) · Smooth zoom + cinematic camera · Time/Sky/Fog/Weather changer · No hurt cam / fire overlay / pumpkin blur / vignette · Crosshair editor · Hit markers (visual only) · Block overlay + break progress.

### QoL
Waypoints · Minimap (radar **singleplayer-only** — never reveals hidden info on servers) · Auto-reconnect · Auto-Tip/GG (opt-in, rate-limit aware) · Chat macros · Screenshot manager · Discord RPC (opt-in) · Config profiles · Inventory tweaks.

### Cosmetics (opt-in, client-side)
Capes, wings, bandanas, hats, kill effects, emotes, hit particles. Visible to other Jade users only when **both** players opt in. Purely client-rendered; nothing is sent to the server.

## Building from source

Requirements: **JDK 8** (`JAVA_HOME` must point at it). ForgeGradle 2.1 does not run on newer JDKs or newer Gradle versions — always build through the wrapper:

```bash
gradlew setupDecompWorkspace   # first time only (deobfuscates MC 1.8.9)
gradlew build
```

The mod jar lands in `build/libs/`. All former Fabric submodules (`jade-core`, `jade-config`, `jade-performance`, `jade-modules`, `jade-ui`, `jade-cosmetics`) are merged into this single Forge mod jar; their sources stay in place and are wired into the root sourceSet (see `build.gradle`).

## Project layout

| Sources | Purpose |
| --- | --- |
| `jade-core/.../core` | Module system, event bus, settings, FPS tracker |
| `jade-core/.../forge` | Forge `@Mod` entrypoint + central event hub |
| `jade-config` | JSON persistence, profiles, versioned migrations |
| `jade-performance` | FPS modules + Forge perf hooks (no mixins) |
| `jade-modules` | HUD / visual / QoL modules |
| `jade-ui` | Centered ClickGUI, themes, toasts, HUD editor, onboarding |
| `jade-cosmetics` | Crosshair renderer, opt-in cosmetics state |

## Config

Everything lives in `.minecraft/jade/`:

```
jade/
├── config.json            # active profile + module state
├── profiles/<name>.json   # named profiles
└── onboarded              # onboarding flag
```

Configs are schema-versioned with forward migrations — old configs never break. Import/export produces a single portable JSON file. Cloud sync is planned as opt-in + encrypted; telemetry is **off by default** (opt-in anonymous crash reports only).

## Accessibility & i18n

- Themes: Dark / Light / Jade, custom accent color picker.
- Reduced-motion mode disables all animations.
- Colorblind-safe modes, scalable UI, crisp at GUI scale 1–4.
- Languages: English, Português (BR), Español — pull requests for more are welcome.

## License

MIT — see [LICENSE](LICENSE).

Generated with Codebuff 🤖
