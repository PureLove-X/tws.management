# TWSManagement

This is a fork.
Originally by kylejuliandev 

## General

Plugin to provide management functionalities for Minecraft servers. Specifically, this plugin was created for the The Wooden Spoon Minecraft Server. You can check out their website @ [tws.gg](https://tws.gg/).

### Features

This plugin has a variety of bespoke features intended for the use of TWS Server. The features are listed below:

- Ability to go afk
  - Players automatically go afk after not triggering certain events [config.json](https://github.com/kylejulian98/tws.management/blob/master/src/main/resources/config.json#L7))
  - Player can manually go afk through:
    - `/afk`
  - AFK players are shown as AFK in the tab menu (if you have the TAB plugin installed, [see below](README.md#Requirements))
- Ability to kick an afk player after a configurable [config.json](https://github.com/kylejulian98/tws.management/blob/master/src/main/resources/config.json#L4) of time
- Ability to add Players to an AFK Kick exempt list, stored in a local SQLite database - The in-game commands for this feature are listed below:
  - `/afk exempt list`
  - `/afk exempt add <PlayerName>`
  - `/afk exempt remove <PlayerName>`
  - `/afk exempt clear`
- Automatically set night to day if the following conditions are met:
  - It is currently night time (in game)
  - All remaining players are AFK
  - At-least one player is asleep. The act of getting in to a Minecraft Bed triggers this code
  - Any players not AFK must be other worlds, such as the Nether
- Add a Heads up Display (HUD) which displays a players coordinates and world time
  - To activate the HUD players can use the following command:
    - `/hud`
  - Players who have enabled the HUD are stored in a local SQLite database

This plugin maintains a local SQLite database which persists information used by some of the provided features. This is found under: `/plugins/tws.management/tws-local.db`.

### Running the Plugin

This plugin runs on Minecraft [Purpur 1.20.1](https://purpurmc.org/docs/). You can download the latest version of the plugin jar through navigating the Packages section of this github repository, or using the following link [packages](https://github.com/kylejulian98/tws.management/packages).

#### Requirements

The following symbols are used to indicate if a dependency (plugin also running on the server) is essential or not:

- ❗ Essential
- ❓ Not Essential

To run this plugin you must have the following dependencies also running on your Minecraft Server:

- ❗ [TAB](https://www.mc-market.org/resources/20631/) - Displays the AFK suffix tag next to your name, in the tab leaderboard.
- ❓ [LuckPerms](https://www.spigotmc.org/resources/luckperms.28140/) - Used when checking if players have a permission node when automatically unwhitelisting.

