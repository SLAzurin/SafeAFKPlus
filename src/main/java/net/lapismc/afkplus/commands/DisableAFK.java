package net.lapismc.afkplus.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import net.lapismc.afkplus.AFKPlus;
import net.lapismc.afkplus.playerdata.AFKPlusPlayer;
import net.lapismc.afkplus.util.AFKPlusCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DisableAFK extends AFKPlusCommand{
    
    public DisableAFK(AFKPlus plugin) {
        super(plugin, "dafk", "disables afk", new ArrayList<String>());
    }
    
    @Override
    protected void onCommand(CommandSender cs, String[] args) {
        if (cs == null) {
            return;
        }
        if (!(cs instanceof Player)) {
            cs.sendMessage("Only players may use this command!");
            return;
        }
        
        AFKPlusPlayer afkp = this.getPlayer((Player) cs);
        
        afkp.setDisabled(!afkp.isDisabled());
        if (!afkp.isDisabled()) {
            cs.sendMessage("You will now be protected while AFK.");
        } else {
            cs.sendMessage("You will NOT be protected while AFK.");
        }
        
    }
    
}
