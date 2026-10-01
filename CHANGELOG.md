# Changelog

All notable changes to Jade Client are documented here. Versions follow the target Minecraft version: `26.1.x`.

## 26.1.0-beta.1 — 2026-09-28

First public beta.

### Added
- **Core**: modular architecture (`jade-core`, `jade-config`, `jade-performance`, `jade-modules`, `jade-ui`, `jade-cosmetics`), ServiceLoader-based module discovery, graceful per-module failure handling.
- **Performance**: Master FPS Boost with entity/block-entity culling, smart frustum + occlusion assist, async chunk building, batched meshing, fast math/render toggles, particle density limiter, rain-splash removal, low fire/smoke, chunk fade speed, entity render distance, smooth FPS cap, dynamic unfocused FPS cap. Presets: Low / Balanced / High / Jade-Max. Live before/after benchmark.
- **UI**: centered Lunar/Feather-style ClickGUI (Right Shift), three-pane layout (categories / modules / settings), fuzzy search, animated toggle pills, jade shimmer top edge, toast notifications, click-outside-to-close. Themes: Dark / Light / Jade + accent picker, reduced motion, 120–200 ms eased animations.
- **HUD**: FPS + 1% lows + frametime graph, info readout (coords/biome/dimension/server/ping/direction), keystrokes + CPS, armor status, potion effects, clock + session timer, compass, watermark + custom text, scoreboard tweaks, chat tweaks. Drag-and-drop HUD editor with snapping, alignment guides and scroll-to-scale.
- **Visuals**: motion blur, fullbright (labeled server-rule-dependent), smooth zoom + cinematic camera, time/sky/fog/weather changer, overlay tweaks (no hurt cam / fire / pumpkin / vignette), crosshair editor (6 styles), visual-only hit markers, block overlay + break progress.
- **QoL**: waypoints, minimap (radar singleplayer-only), auto-reconnect, auto-tip/GG (opt-in, anti-spam guard), chat macros, screenshot manager, Discord RPC stub (opt-in), inventory tweaks, config profiles.
- **Cosmetics**: opt-in client-side capes/wings/bandanas/hats, kill effects, emotes, hit particles; mutual opt-in visibility between Jade users.
- **Config**: JSON in `.minecraft/jade/`, per-module state, named profiles, import/export, schema versioning with migrations, atomic saves.
- **Accessibility/i18n**: EN, PT-BR, ES language files; reduced motion; colorblind modes; GUI scale 1–4 parity.
- **Branding**: jade gemstone icon (512×512 PNG + SVG), `brand/icon.py` generator.

### Compliance
- No combat, movement, or information-revealing modules. No ESP/XRay/radar on multiplayer. No auto-clicker. Jade alters rendering and QoL only — everything else is explicitly out of scope.
