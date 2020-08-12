/*
 * Copyright 2020 Benjamin Martin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.lapismc.afkplus;

import net.lapismc.afkplus.playerdata.AFKPlusPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryCreativeEvent;

class AFKPlusListeners implements Listener {

    private final AFKPlus plugin;
//    private final HashMap<UUID, Location> playerLocations = new HashMap<>();
//    private BukkitTask AfkMachineDetectionTask;

    AFKPlusListeners(AFKPlus plugin) {
        this.plugin = plugin;
//        startRunnable();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        plugin.getPlayer(e.getPlayer()).forceStopAFK();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        plugin.getPlayer(e.getPlayer()).forceStopAFK();
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        if (plugin.getConfig().getBoolean("EnabledDetections.Chat")) {
            Bukkit.getScheduler().runTask(plugin, () -> plugin.getPlayer(e.getPlayer()).interact());
        }
    }
    
    @EventHandler
    public void onInventoryCreativeEvent(InventoryCreativeEvent e) {
//        getLogger().info("onInventoryMoveItemEvent");
        HumanEntity hE = e.getWhoClicked();
        if (!(hE instanceof Player)) {
            return;
        }
        AFKPlusPlayer afkp = plugin.getPlayer((Player) hE);
        if (afkp.isAFK()) {
            e.setCancelled(true);
            afkp.interact();
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (plugin.getConfig().getBoolean("EnabledDetections.Move")) {
            plugin.getPlayer(e.getPlayer()).interact();
        }
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent e) {
        if (plugin.getConfig().getBoolean("EnabledDetections.Attack")) {
            if (e.getDamager() instanceof Player) {
                Player p = (Player) e.getDamager();
                plugin.getPlayer(p).interact();
            }
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent e) {
        if (!e.getMessage().contains("/afk") && plugin.getConfig().getBoolean("EnabledDetections.Command")) {
            plugin.getPlayer(e.getPlayer()).interact();
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (plugin.getConfig().getBoolean("EnabledDetections.Interact")) {
            AFKPlusPlayer afkp = plugin.getPlayer(e.getPlayer());
            if (afkp.isAFK()) {
                e.setCancelled(true);
            }
            afkp.interact();
        }
    }

    @EventHandler
    public void onPlayerBlockPlace(BlockPlaceEvent e) {
        if (plugin.getConfig().getBoolean("EnabledDetections.BlockPlace")) {
            plugin.getPlayer(e.getPlayer()).interact();
        }
    }

    @EventHandler
    public void onPlayerBlockBreak(BlockBreakEvent e) {
        if (plugin.getConfig().getBoolean("EnabledDetections.BlockBreak")) {
            plugin.getPlayer(e.getPlayer()).interact();
        }
    }

    /*
    AFK Machine detection
     */

    /**
     * This task should be canceled on disable, this is the task that attempts to stop AFK machines from working
     *
     * @return The AFK machine detection task being run by Bukkit
     */
//    public BukkitTask getAfkMachineDetectionTask() {
//        return AfkMachineDetectionTask;
//    }

//    private void startRunnable() {
//        AfkMachineDetectionTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
//            playerLocations.clear();
//            //Save all players current locations
//            for (Player p : Bukkit.getOnlinePlayers()) {
//                playerLocations.put(p.getUniqueId(), p.getLocation());
//            }
//            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
//                //Go through each saved location and see if the player is moving
//                for (UUID uuid : playerLocations.keySet()) {
//                    if (Bukkit.getOfflinePlayer(uuid).isOnline()) {
//                        Location savedLoc = playerLocations.get(uuid);
//                        Location loc = Bukkit.getPlayer(uuid).getLocation();
//                        //Check if the player is moving in both rotation and transform
//                        boolean inactive = false;
//                        if (plugin.getConfig().getBoolean("AggressiveAFKDetection")) {
//                            //If aggressive is enabled we want to check if the player isn't moving in one or both
//                            if (checkRotation(savedLoc, loc))
//                                inactive = true;
//                            if (checkTransform(savedLoc, loc))
//                                inactive = true;
//                        } else {
//                            //Without aggressive enabled we only want one to be true,
//                            //if both are false then inactive will be false
//                            //This is achieved by converting true to 1 and false to 0 and summing them
//                            //Inactive is only true when only one of the booleans is true
//                            if ((checkRotation(savedLoc, loc) ? 1 : 0) + (checkTransform(savedLoc, loc) ? 1 : 0) == 1) {
//                                inactive = true;
//                            }
//                        }
//                        //This is sent to the player object, if the player is deemed to not be moving they will not
//                        //be able to reset their interact timer. This wil force them into AFK even
//                        //if they are triggering move events
//                        plugin.getPlayer(uuid).setInactive(inactive);
//
//                    }
//                }
//            }, 20 * 2);
//        }, 20 * 5, 20 * 5);
//    }

    private boolean checkRotation(Location oldLoc, Location newLoc) {
        boolean yaw = oldLoc.getYaw() == newLoc.getYaw();
        boolean pitch = oldLoc.getPitch() == newLoc.getPitch();
        return yaw && pitch;
    }


    private boolean checkTransform(Location oldLoc, Location newLoc) {
        boolean x = oldLoc.getX() == newLoc.getX();
        boolean y = oldLoc.getY() == newLoc.getY();
        boolean z = oldLoc.getZ() == newLoc.getZ();
        return x && y && z;
    }
}
