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

import net.lapismc.afkplus.api.AFKPlusAPI;
import net.lapismc.afkplus.api.AFKPlusPlayerAPI;
import net.lapismc.afkplus.commands.AFK;
import net.lapismc.afkplus.commands.AFKPlusCmd;
import net.lapismc.afkplus.playerdata.AFKPlusPlayer;
import net.lapismc.lapiscore.LapisCoreConfiguration;
import net.lapismc.lapiscore.LapisCorePlugin;
import net.lapismc.lapiscore.utils.LapisCoreFileWatcher;
import net.lapismc.lapiscore.utils.LapisUpdater;
import net.lapismc.lapiscore.utils.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitTask;
import org.ocpsoft.prettytime.Duration;
import org.ocpsoft.prettytime.PrettyTime;
import org.ocpsoft.prettytime.units.JustNow;
import org.ocpsoft.prettytime.units.Millisecond;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Level;
import net.lapismc.afkplus.commands.DisableAFK;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

public final class AFKPlus extends LapisCorePlugin {

    public PrettyTime prettyTime;
    public LapisUpdater updater;
    private LapisCoreFileWatcher fileWatcher;
    private final HashMap<UUID, AFKPlusPlayer> players = new HashMap<>();
    private BukkitTask repeatingTask;
    private AFKPlusListeners listeners;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        registerConfiguration(new LapisCoreConfiguration(this, 4, 2));
        registerPermissions(new AFKPlusPermissions(this));
        update();
        fileWatcher = new LapisCoreFileWatcher(this);
        Locale loc = new Locale(config.getMessage("PrettyTimeLocale"));
        prettyTime = new PrettyTime(loc);
        prettyTime.removeUnit(JustNow.class);
        prettyTime.removeUnit(Millisecond.class);
        new AFK(this);
        new AFKPlusCmd(this);
        new DisableAFK(this);
        listeners = new AFKPlusListeners(this);
        new AFKPlusAPI(this);
        new AFKPlusPlayerAPI(this);
        new Metrics(this);
        repeatingTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this, runRepeatingTasks(), 20, 20);
        getLogger().info(getName() + " v." + getDescription().getVersion() + " has been enabled!");
        
        
        createAFKTeamIfNotExists();
    }

    @Override
    public void onDisable() {
        disableAFKAllPlayers();
        
        fileWatcher.stop();
        //Stop the AFK repeating task
        repeatingTask.cancel();
        //Also stop the AFK Machine detection task
//        listeners.getAfkMachineDetectionTask().cancel();
        getLogger().info(getName() + " has been disabled!");
    }

    public AFKPlusPlayer getPlayer(UUID uuid) {
        if (!players.containsKey(uuid)) {
            players.put(uuid, new AFKPlusPlayer(this, uuid));
        }
        return players.get(uuid);
    }

    public AFKPlusPlayer getPlayer(OfflinePlayer op) {
        return getPlayer(op.getUniqueId());
    }

    private void update() {
        updater = new LapisUpdater(this, "AFKPlus", "SLAzurin", "SafeAFKPlus", "master");
        if (updater.checkUpdate()) {
            if (getConfig().getBoolean("UpdateDownload")) {
                updater.downloadUpdate();
            } else {
                getLogger().info(config.getMessage("Updater.UpdateFound"));
            }
        } else {
            getLogger().info(config.getMessage("Updater.NoUpdate"));
        }
    }

    private Runnable runRepeatingTasks() {
        return () -> {
            for (AFKPlusPlayer player : players.values()) {
                player.getRepeatingTask().run();
            }
        };
    }

    private void disableAFKAllPlayers() {
        this.players.entrySet().forEach((player) -> {
            AFKPlusPlayer afkp = player.getValue();
            if (afkp.isAFK()) {
                afkp.forceStopAFK();
            }
        });
    }

    private void createAFKTeamIfNotExists() {
        new BukkitRunnable() {
            @Override
            public void run() {
                ScoreboardManager manager = Bukkit.getScoreboardManager();
                if (manager == null) {
                    throw new IllegalStateException("No world loaded");
                }
                Scoreboard main = manager.getMainScoreboard();
                try {
                    
                    Team t = main.registerNewTeam("afk_plugin");
                    t.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
                    getLogger().log(Level.INFO, "{0} Created team afk_plugin", getName());
                } catch (IllegalArgumentException e) {
                    // Team already exists.
                }
                
//                try {
//                    Team t = main.registerNewTeam("not_afk_plugin");
//                    t.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.ALWAYS);
//                    getLogger().log(Level.INFO, "{0} Created team not_afk_plugin", getName());
//                } catch (IllegalArgumentException e) {
//                    // Team already exists.
//                }
            }
        }.runTaskLater(this, 1L);
    }

    public List<Duration> reduceDurationList(List<Duration> durationList) {
        while (durationList.size() > 2) {
            Duration smallest = null;
            for (Duration current : durationList) {
                if (smallest == null || smallest.getUnit().getMillisPerUnit() > current.getUnit().getMillisPerUnit()) {
                    smallest = current;
                }
            }
            durationList.remove(smallest);
        }
        return durationList;
    }

}
