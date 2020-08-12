# SafeAFKPlus

This is a spigot plugin for managing AFK players and in a way they remain safe.
This plugin is meant for personal use and for specific use cases.

My server needed an AFK Plugin which would make the players invulnerable while being AFK.

Major changes:
  - Removal of the afk detection background task. (Not needed for this use case)
  - Change gamemode of a player to creative only while afk.
  - Add anti-exploit for creative game mode while being invincible.
  - AFK users will no longer be able to be pushed.
  - These plugin changes break any servers that use vanilla teams.

This variant of the plugin itself will most likely never be publically released.

The original Spigot plugin can be found here:
Spigot: https://www.spigotmc.org/resources/afk.35065/
