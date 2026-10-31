# Controllable Integration 1.0.5 (Beta)

*Made with AI. The ideas and core concepts came from me.*

## Supported Mods
- **TACZ** (Timeless and Classics Zero)
- **MrCrayfish's Vehicle Mod**

## Build Instructions

Reuse an existing Forge 1.20.1 project's wrapper and libs (no copying of jars, no wrapper generation):

1. Copy `gradlew`, `gradlew.bat` and the `gradle/` folder from your other project into this folder.

2. Build, pointing at the folder that already holds your jars:

    ./gradlew build -PlibsDir=/absolute/path/to/that/project/libs

   (Requires JDK 17.) The jars in that folder must be named exactly:
   `controllable-forge-1.20.1-0.21.9.jar`, `framework-forge-1.20.1-0.8.0.jar`, `tacz-1.20.1-1.1.8-hotfix2.jar`

3. **Result:** `build/libs/tacz_controllable_compat-1.0.5.jar`
