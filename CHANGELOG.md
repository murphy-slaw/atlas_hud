# Changelog for [Dead Reckoning](https://github.com/murphy-slaw/dead-reckoning)
(Formerly __Antique Atlas Compass HUD__)

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.6.0] 2025-12-26

### Changed
- Switched config from Cloth Config to Kaleido/McQoy/YACL. Working color pickers! Thanks to sisby-folk for the PR.
- Option to display only the four cardinal directions or all 8 (cardinal + intercardinal).
- Lots of non-user-visible code shuffling under the hood.

### Fixed
- Fixed a crash when accessing Surveyor landmarks without a POS component. Thanks to vgskye for the patch.
- Fixed caching of landmarks to reduce Surveyor server access in the case where the active set is unchanged.

## [1.5.2] 2025-12-22

### Changed
- Big refactor to de-spaghettify renderMarkers(). Fixes some minor rendering issues and some issues with incorrect 
  z-ordering of markers.

### Added
- Added a configurable keybind to show/hide the HUD (Might come in handy for ALWAYS or COMPASS_INVENTORY display 
  modes).  Defaults to '.'
- COMPASS_INVENTORY mode now checks Trinkets slots for items with the 'dead_reckoning:shows_compass_ribbon' tag if 
  Trinkets is installed (and configured to allow such items in slots).


## [1.5.1] 2025-12-21

### Fixed
- Fixed text color rendering.
- Updated jar name and a few other straggling mentions of **Antique Atlas Compass HUD**
### Added
- Now the offset for markers can be set independently from the background offset (for better handling of custom 
  background textures).

## [1.5.0] 2025-12-20

### Changed
- Now should work with any __Surveyor__ based map mod, including __Antique Atlas 4__ and __Hoofprint__ (or even standalone Surveyor if you only want waypoints.)
- Renamed! Now it's __Dead Reckoning__, since it doesn't strictly require AA4.
- Draws Surveyor landmarks with a STACK component as items.
- Uses Antique Atlas markers if it's installed.
- Falls back to simple colored banner markers if no other markers are applicable.

### Fixed
- Now only reloads the list of landmarks if there's a Surveyor update event.
- Handles overlapping markers more gracefully.

## [1.4.1] 2025-12-17

### Fixed
- Add missing translation strings.
- Fixed marker scale correction not being applied to width.

### Added
- Configurable Y offset for compass decorations.

## [1.4.0] 2025-12-17

### Fixed
- HUD now moves out of the way of boss bars.

### Added
- New config value: MinMarkerScale. If MinMarkerScale is smaller than MarkerScale, markers will get smaller the further
away they are.
- Drop shadow for direction text can be toggled off.
- Compass decorations are now textures. To customize, create a resource pack that adds the following textures:
  - `dead_reckoning/textures/left.png`
  - `dead_reckoning/textures/decoration.png`
  - `dead_reckoning/textures/right.png`

## [1.3.0] 2025-12-15

### Changed
- Updated for compatibility with Antique Atlas 4 3.0.0 / Surveyor 1.0.0

## [1.2.0] 2025-11-26

### Fixed
- Configuration options work again. 😅

### Changed
- Minimum width of the ribbon is now 10% of screen width.

## [1.1.0] 2025-09-19

### Changed
- Now the items which cause the HUD to appear are controlled by the `shows_compass_ribbon` item tag (which defaults to `minecraft:compass`)
- Now available for Minecraft 1.21.1

## [1.0.1] 2025-08-19

### Fixed
- Default config no longer supplies invalid RGBA colors. Fixes [#3](https://github.com/murphy-slaw/dead-reckoning/issues/3)

## [1.0.0] 2025-02-25

Initial public release.
