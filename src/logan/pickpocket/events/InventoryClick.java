package logan.pickpocket.events;

import logan.pickpocket.main.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Tre on 12/28/2015.
 */
public class InventoryClick implements Listener {

    private Pickpocket pickpocket;

    public InventoryClick(Pickpocket pickpocket) {
        this.pickpocket = pickpocket;
        pickpocket.getServer().getPluginManager().registerEvents(this, pickpocket);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Profile profile = Profiles.get(player, pickpocket.getProfiles());
        PickpocketItemInventory pickpocketItemInventory = profile.getPickpocketItemInventory();
        Inventory inventory = event.getClickedInventory();
        ItemStack currentItem = event.getCurrentItem();
        try {
            if (inventory.getItem(event.getSlot()) == null) return;
        } catch (NullPointerException e) {
            return;
        }
        if (inventory.getName().contains(PickpocketItemInventory.NAME)) {
            if (currentItem.getItemMeta().getDisplayName().equals(pickpocketItemInventory.getNextButtonName()))
                pickpocketItemInventory.nextPage();
            if (currentItem.getItemMeta().getDisplayName().equals(pickpocketItemInventory.getBackButtonName()))
                pickpocketItemInventory.previousPage();
            event.setCancelled(true);
        } else {
            if (!profile.isStealing()) return;
            if (Profiles.get(profile.getVictim(), pickpocket.getProfiles()).isStealExempt()) {
                event.setCancelled(true);
                profile.getPlayer().sendMessage(ChatColor.GRAY + "This person cannot be stolen from.");
                return;
            }


            for (PickpocketItem pickpocketItem : PickpocketItem.values()) {
                if (currentItem.getType().equals(pickpocketItem.getMaterial())) {
                    boolean shouldCancel = !testChance(profile, pickpocketItem);
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (Profiles.get(p, pickpocket.getProfiles()).isAdmin()) {
                            p.sendMessage(profile.getPlayer().getName() + " attempted to steal from " + profile.getVictim().getName() + ".");
                        }
                    }
                    event.setCancelled(shouldCancel);
                    if (!event.isCancelled()) {
                        player.getInventory().addItem(currentItem);
                        inventory.remove(currentItem);
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (Profiles.get(p, pickpocket.getProfiles()).isAdmin()) {
                                p.sendMessage(profile.getPlayer().getName() + " stole from " + profile.getVictim().getName() + ".");
                            }
                        }
                    } else event.setCancelled(true);
                    return;
                }
            }

            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot steal this item.");
        }
    }

    public boolean testChance(Profile profile, PickpocketItem pickpocketItem) {
        if (Math.random() < pickpocketItem.calculateStolenBasedChance(profile.getTimesStolenOf(pickpocketItem))) {
            if (!profile.hasPickpocketItem(pickpocketItem)) {
                profile.givePickpocketItem(pickpocketItem);
                profile.getVictim().sendMessage(ChatColor.GRAY + profile.getPlayer().getName() + ChatColor.WHITE + " just stole your " + pickpocketItem.getName() + "!");
                profile.getPlayer().sendMessage(ChatColor.GRAY + " you stole their " + pickpocketItem.getName() + "!");
            }
        } else {
            profile.getPlayer().sendMessage(ChatColor.RED + "Theft unsuccessful.");
            profile.getVictim().sendMessage(ChatColor.GRAY + profile.getPlayer().getName() + ChatColor.RED + " has attempted to steal from you.");
            profile.setStealing(null);
            return false;
        }

        return true;
    }
}
