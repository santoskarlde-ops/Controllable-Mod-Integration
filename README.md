# Controllable Integration 1.0.5 (Beta)

*Made with AI. The ideas and core concepts came from me.*

Controller support for Timeless and Classics Zero (TACZ) through Controllable. This is a client-side mod for Minecraft 1.20.1 on Forge. It is still experimental.

## Supported Mods
- **TACZ** (Timeless and Classics Zero)
- **MrCrayfish's Vehicle Mod** (tested against 0.45.3-1.20.1, the unofficial port)

## Requirements
- Minecraft 1.20.1 and Forge 47+
- TACZ 1.1.8 (hotfix2)
- Controllable 0.21.9 and Framework 0.8.0
- Optional: MrCrayfish's Vehicle Mod

*The mod does nothing if TACZ or Controllable is missing.*

## Features

### TACZ Bindings
- A "TACZ (Controller)" category in Controllable's bindings screen. All of them are rebindable.
- **Defaults:**
  - Shoot: `RT`
  - Aim: `LT`
  - Reload: `X`
  - Melee: `B`
  - Fire mode: `D-pad Left`
  - Attachments: `D-pad Up`
  - Inspect: `Unbound`
- The Attachments binding opens TACZ's refit screen, which TACZ has no controller button for.

### Aim Fix
- Controllable's triggers flicker around the halfway point, which made aiming spam on and off. The triggers now have a press and release threshold, defaulting to 0.60 and 0.40, so aiming engages once and stays.

### Aim Assist
- Works with any TACZ gun, including gun-pack guns.
- It pulls the view smoothly toward the target, per frame, with stick slowdown near targets. It is stronger while aiming down sights.
- By default it targets every mob (hostiles and everything else) except your own pets. Players are off.
- A debug option shows on the action bar why it is or isn't acting.

### On-Screen Hints
- While you hold a gun, Controllable's "Attack" and "Use Item" hints become Shoot, Aim and Reload. An option adds Fire Mode, Attachments, Melee and Inspect.

### Vehicle Mod Support
- **GTA V-style driving:** `RT` is gas, `LT` is brake/reverse, the left stick steers and `RB` is the handbrake. The mod switches on the vehicle mod's `useTriggers` option while you ride.
- New rebindable bindings: Vehicle: Horn (`L3`) and Vehicle: Exit (`Y`).
- Vehicle hints for accelerate, brake, handbrake, horn and exit.
- While you ride a vehicle, gun bindings and aim assist are off.

### Config Screen
- Mods list → TACZ Controllable Compat → Config. It has two pages: toggles, then sliders.
- Older aim-assist settings are reset once to the new defaults.

## Known Limitations
- The vehicle mod hard-codes the handbrake on `RB` and its own horn on `R3`. `R3` still honks as well as sneaking.
- TACZ's own "TACZ" binding category now mirrors this mod's bindings. Rebind the controls in the new category instead.
- TACZ's zoom (`X`) and crawl bindings are untouched. Reload now takes `X`, so rebind zoom if you use it.
- Hint editing and vehicle support work by reflection, so a different Controllable or Vehicle Mod version may need a patch.
- Not yet tested across other TACZ versions, controllers or multiplayer servers.

## Installation
Put the jar in `mods/` next to the required mods.

## Build Instructions
Reuse an existing Forge 1.20.1 project's wrapper and libs (no copying of jars, no wrapper generation):

1. Copy `gradlew`, `gradlew.bat` and the `gradle/` folder from your other project into this folder.

2. Build, pointing at the folder that already holds your jars:

        ./gradlew build -PlibsDir=/absolute/path/to/that/project/libs

   (Requires JDK 17.) The jars in that folder must be named exactly:
   `controllable-forge-1.20.1-0.21.9.jar`, `framework-forge-1.20.1-0.8.0.jar`, `tacz-1.20.1-1.1.8-hotfix2.jar`

3. **Result:** `build/libs/tacz_controllable_compat-1.0.5.jar`

## Reporting Problems
Turn on debug under `[aimAssist]` in the config (or "Debug (action bar)" in the config screen) and attach `logs/latest.log`.
