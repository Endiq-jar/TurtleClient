# Control audit

The repair is not just replacement artwork. Source-level checks found missing handlers, incorrect module lookups, no input consumers and settings with no effect.

| Area | Finding | Change |
|---|---|---|
| Title account pill | Decorative only | Account screen, offline profiles, Microsoft device flow and disconnected session switching |
| Title navigation | No account-permission gate/error feedback | Multiplayer permission check; folder failures are visible |
| Cosmetics | Registry/equip flags only | Six textured player-layer meshes, 18 built-ins, persistent selection, bounded custom PNG uploads |
| Compact cosmetics menu | Folder/clear actions disappeared | Reload, folder and unequip-all in the persistent toolbar, with tooltips |
| FPS/CPS/ping/timer/auto-hide | Display-name mismatches silently returned null | Typed module lookup instead of string lookup in HUD |
| CPS | No click registration; stale counts | Mouse callback hook and one-second expiry, left/right tracking |
| FOV/zoom | New vanilla options rejected out-of-range values | Render-time FOV override, live zoom, scroll adjustment, sensitivity/cinematic controls; no option-file pollution |
| Full Bright | Gamma=100 was rejected | Lightmap-only gamma override; vanilla settings remain valid |
| Common text HUD | Position/color/scale controls ignored | Shared styled renderer, FPS graph and CPS/ping bars |
| Timer | No Start/Pause/Reset controls | Monotonic clock, real actions, count-up/countdown, completion notice and sound |
| Camera | Settings without capture implementation | Clean delayed PNG capture, optional flash/notice, open folder; removed fake JPEG/clipboard options |
| Waypoints | Empty list with no creation/rendering | Name editor, add current position, remove/clear, HUD distances; deliberately scoped to the current world session |
| Auto Text | Message not editable; delay/repeat unused | Text editor, join delay, optional repeat/whisper and Send now; live repeat starts/stops without reconnecting |
| Crosshair | Hardcoded white cross | Shape/size/gap/colors/opacity/outline/target/offset settings and vanilla-crosshair replacement |
| Colors | Six fixed presets only; tiny text alpha became opaque on old fonts | RGBA editor with opacity/preview; transparent text is skipped consistently |
| Clock/memory/speed/direction/server HUD | Setting-name mismatches and unconsumed format controls | Actual setting names, working date/units/labels/direction/custom address; velocity converted from blocks/tick to blocks/second |
| Popup Events | Nothing generated notifications; lifetime counted render frames | World-join and opt-in game-message events, test action, bounded queue, monotonic fade/lifetime and live style controls |
| Block Overlay | Depended on Block Indicator being enabled | Independent target sampling, offsets, scale, colors and coordinate layouts |
| Attack/PvP/team/combo HUD | Fixed drawing ignored available controls | Configurable cooldown bar, player filters/fields/sorting, styled team table, tick-based combo pop |
| Sprint/Auto GG | Unused modes and incorrectly hidden Auto GG checkbox | Forward/sneak/minimum-input controls; correctly exposed Auto /gg, restricted to Hypixel with duplicate suppression |
| Preferences | Toggles/favorites/settings lost on restart | Bounded JSON codec, atomic writes, saved module settings/favorites/keybinds |
| Action buttons | Inactive controls appeared clickable | Shared callback IDs, disabled-state reasons and visible errors |

## Honest limits, not fake working switches

Several **pre-existing prototypes** never had a usable implementation. They are now labelled **Unavailable** and cannot be enabled through clicks, keybinds or saved configuration: Animations, Freelook, Weather Changer, Time Changer, Chat, Nick Hider, Boss Bar, Scoreboard, Team Circles, Skyblock Addons, TabStat and Motion Blur. In particular, the old black-screen overlay is not described as real motion blur, and Nick Hider is not allowed to imply privacy protection.

Armor icons/bar, keystrokes, potion status and UHC currently use their basic fixed layouts; their unconsumed customization controls are not exposed. Other legacy modules remain basic implementations; this change is not a claim that every historical setting/prototype has become a complete feature. Unsupported circle/icon cooldown modes, alternate sprint modes and entity-tint controls are also not exposed as working controls. Toggle Sprint is explicitly a **status display**, not a movement keybinding. Auto Hide affects Turtle overlays, not the vanilla HUD. The UI presentation policy is centralized in `ModulePresentation.kt`.

## Runtime acceptance checklist

Automated tests cover action dispatch, scrolling, editable text, settings persistence/bounds, CPS expiry, clock behavior, PNG bounds, six mesh types, authentication transitions and version-specific bytecode contracts. **Minecraft has not been launched in the build sandbox.** Manually verify:

- [ ] All title actions and account restrictions on a normal and offline launcher account.
- [ ] Account cancel/expiry, app setup, approved Microsoft sign-in, switch/restore and signed multiplayer chat.
- [ ] Screen resize/GUI scale, fractional wheel input, dragging and keyboard navigation.
- [ ] Every cosmetic in third person/inventory; walking, crouching, slim skin, armor, elytra and invisibility.
- [ ] Reload/remove/invalid PNG, selection restart persistence and texture cleanup.
- [ ] FOV, zoom/scroll/sensitivity, brightness, crosshair replacement and disable/restore behavior.
- [ ] CPS decay, timer actions/completion, camera PNG/notification, waypoint lifetime, Auto Text opt-in and live repeat cancellation.
- [ ] Popup test/join/game events, fade/queue limits, transparent colors, clock/units, target overlay, cooldown bar and PvP/team/combo layouts.
