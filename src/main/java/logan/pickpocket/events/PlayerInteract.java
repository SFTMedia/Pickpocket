package logan.pickpocket.events;

import logan.pickpocket.main.Pickpocket;
import logan.pickpocket.main.Profile;
import logan.pickpocket.main.Profiles;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.FlagContext.FlagContextBuilder;
import com.sk89q.worldguard.protection.flags.StateFlag.State;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

/**
 * Created by Tre on 12/28/2015.
 */
public class PlayerInteract implements Listener {

    private Pickpocket pickpocket;

    public PlayerInteract(Pickpocket pickpocket) {
        this.pickpocket = pickpocket;
        pickpocket.getServer().getPluginManager().registerEvents(this, pickpocket);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Player)) return;
        Player player = event.getPlayer();
        if (!player.isSneaking()) return;
        Profile profile = Profiles.get(player, pickpocket.getProfiles());

        if(!WorldGuardPlugin.inst().getRegionManager(event.getPlayer().getWorld()).getApplicableRegions(event.getRightClicked().getLocation()).allows(DefaultFlag.PVP)){
            player.sendMessage(ChatColor.GREEN+"Saftey Samurai"+ChatColor.RESET+": NOPE");
            event.getRightClicked().sendMessage(ChatColor.GREEN+"Saftey Samurai"+ChatColor.RESET+": "+ player.getName()+" tried to pickpocket you, but I kept you safe while you are in my domain.");
            return;
        }

        if(event.getRightClicked().hasPermission("pickpocket.staff")) {
            player.sendMessage("They see you.");
            event.getRightClicked().sendMessage(player.getName()+" tried to pickpocket you, but you in your ninja ways saw them.");
            return;
        }

        if (!pickpocket.getCooldowns().containsKey(player) || player.hasPermission("pickpocket.bypass")) {
            Player entity = (Player) event.getRightClicked();
            player.openInventory(entity.getInventory());
            profile.setStealing(entity);
            pickpocket.addCooldown(player);
            return;
        }


        if (pickpocket.getCooldowns().containsKey(player) && !player.hasPermission("pickpocket.bypass")) {
            player.sendMessage(ChatColor.RED + "You must wait " + pickpocket.getCooldowns().get(player) + " seconds before attempting another pickpocket.");
            return;
        }


        Player entity = (Player) event.getRightClicked();
        player.openInventory(entity.getInventory());
        profile.setStealing(entity);
    }
}
