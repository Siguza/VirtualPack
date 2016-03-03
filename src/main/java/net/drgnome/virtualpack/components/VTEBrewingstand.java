// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.components;

import java.util.*;
import net.minecraft.server.v#MC_VERSION#.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.craftbukkit.v#MC_VERSION#.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v#MC_VERSION#.inventory.CraftInventoryBrewer;
import org.bukkit.entity.HumanEntity;
import net.drgnome.virtualpack.VPack;
import net.drgnome.virtualpack.util.*;

public class VTEBrewingstand extends TileEntityBrewingStand
{
    // To access the chests
    private VPack vpack;
    private CraftInventoryBrewer bukkitInv;
    public int link;
    private double myBrewTime;
    private double brewSpeed;
    private int lastID;
    private long lastCheck;
    private ItemStack[] items;

    public VTEBrewingstand(VPack vpack)
    {
        super();
        items = getContents();
        this.vpack = vpack;
        this.bukkitInv = new CraftInventoryBrewer(this);
        link = 0;
        myBrewTime = 0.0D;
        brewTime(0);
        brewSpeed = 1.0D;
        lastCheck = 0;
    }

    // Read from save
    public VTEBrewingstand(VPack vpack, String data[])
    {
        this(vpack);
        // Get our items
        for(int i = 0; i < Util.min(data.length, 4); i++)
        {
            items[i] = Util.stringToItemStack(data[i]);
        }
        // And the times
        myBrewTime = Util.tryParse(data[4], myBrewTime);
        brewTime(Util.floor(myBrewTime));
        link = Util.tryParse(data[5], link);
        brewSpeed = getBrewSpeed(items[3]);
        if(items.length >= 5 && data.length >= 7)
        {
            items[4] = Util.stringToItemStack(data[6]);
        }
    }

    public String[] save()
    {
        ArrayList<String> list = new ArrayList<String>();
        for(int i = 0; i < 4; i++)
        {
            list.add(Util.itemStackToString(items[i]));
        }
        list.add(Double.toString(myBrewTime));
        list.add(Integer.toString(link));
        if(items.length >= 5)
        {
            list.add(Util.itemStackToString(items[4]));
        }
        return list.toArray(new String[0]);
    }

    public void tick(int ticks)
    {
        checkLink();
        int newID = items[3] == null ? 0 : Item.#FIELD_ITEM_7#(items[3].getItem());
        // Item changed?
        if(newID != lastID)
        {
            // Then reset the brewing time
            myBrewTime = 400.0D;
            lastID = newID;
            // And most important: Reset the brewing speed
            brewSpeed = getBrewSpeed(items[3]);
        }
        // Are we brewing?
        boolean brewable = canBrew();
        if(brewable && (myBrewTime > 0.0D))
        {
            myBrewTime -= brewSpeed * ((double)ticks);
            // Are we done?
            if(myBrewTime <= 0.0D)
            {
                // Then brew!
                brew();
            }
        }
        // Nothing going on, but we could still brew?
        if(brewable)
        {
            // I'm using two if-clauses here because of the following else-clause.
            if(myBrewTime <= 0.0D)
            {
                myBrewTime = 400.0D;
            }
        }
        // If we can't brew, we should reset the brewing time
        else
        {
            myBrewTime = 0.0D;
        }
        // And here we set the display variable.
        brewTime(Util.floor(myBrewTime));
    }

    protected void checkLink()
    {
        boolean brewable = canBrew();
        // If there's a link, check what's to do
        if(brewable || (link == 0) || (vpack == null) && (vpack.getInv(link) == null) || (lastCheck >= vpack.getInv(link).getLastUpdate()))
        {
            return;
        }
        VInv inv = vpack.getInv(link);
        // If there is a breeing ingredient, let's check all the potions
        if(isIngredient(items[3]))
        {
            for(int i = 0; i < 3; i++)
            {
                // If one of them is not brewable, we might find one that is
                if(!matchesResult(items[i], items[3]))
                {
                    ItemStack item;
                    for(int j = 0; j < inv.getSize(); j++)
                    {
                        item = inv.getItem(j);
                        // Did we find something brewable?
                        if(matchesResult(item, items[3]))
                        {
                            // Then exchange the items
                            item = Util.copy_old(item);
                            ItemStack item1 = Util.copy_old(items[i]);
                            items[i] = item;
                            inv.setItem(j, item1);
                            // And leave the loop
                            break;
                        }
                    }
                }
            }
        }
        // If the current item is not an ingredient, then we'll try to get one
        else
        {
            // First we'll try to get an ingredient that matches with as many potions as possible
            int[] match = new int[inv.getSize()];
            int max = -1;
            ItemStack item;
            // Go through all the items
            for(int i = 0; i < inv.getSize(); i++)
            {
                item = inv.getItem(i);
                // If it's not even an ingredient, forget it
                if(!isIngredient(item))
                {
                    continue;
                }
                // Now, is it compatible with our potions?
                for(int j = 0; j < 3; j++)
                {
                    match[i] += matchesResult(items[j], item) ? 1 : 0;
                }
                // If all potions match, we don't need to go further
                if(match[i] >= 3)
                {
                    break;
                }
                // If at least one potion matches, we may have a new temporary maximum
                else if((match[i] > 0) && ((max == -1) || (match[i] > match[max])))
                {
                    max = i;
                }
            }
            // If we found anything matching, let's exchange
            if(max != -1)
            {
                item = Util.copy_old(inv.getItem(max));
                ItemStack item1 = Util.copy_old(items[3]);
                items[3] = item;
                inv.setItem(max, item1);
            }
            // If we did not find anything matching, we'll have to grab whatever we can get
            else
            {
                int[][] matching = new int[inv.getSize()][];
                // Of course, more potions, higher priority
                for(int i = 0; i < inv.getSize(); i++)
                {
                    matching[i] = new int[]{-1, -1, -1, 0}; // Amount + 3 indices
                    item = inv.getItem(i);
                    // Go away if you're not an ingredient!
                    if(isIngredient(item))
                    {
                        continue;
                    }
                    for(int j = 0; j < inv.getSize(); j++)
                    {
                        // One does not simply brew itself
                        if(i == j)
                        {
                            continue;
                        }
                        // Is it brewable?
                        if(matchesResult(inv.getItem(j), item))
                        {
                            // Count one up
                            matching[i][0]++;
                            // And add mapping
                            matching[i][matching[i][0]] = j;
                            // All potions?
                            if(matching[i][0] >= 3)
                            {
                                // Then goodbye
                                break;
                            }
                        }
                    }
                    // All potions? Then also break here
                    if(matching[i][0] >= 3)
                    {
                        break;
                    }
                    // New highscore?
                    else if((matching[i][0] > 0) && ((max == -1) || (matching[i][0] > matching[max][0])))
                    {
                        max = i;
                    }
                }
                // So, do we now have anything matching?
                if(max != -1)
                {
                    int mapping;
                    ItemStack item1;
                    for(int i = 0; i < 4; i++)
                    {
                        // 3 = ingredient, other = potion indices
                        mapping = i == 3 ? max : matching[max][i + 1];
                        // Nothing to do if we have no mapping
                        if(mapping == -1)
                        {
                            item = Util.copy_old(inv.getItem(mapping));
                            item1 = Util.copy_old(items[i]);
                            items[i] = item;
                            inv.setItem(mapping, item1);
                        }
                    }
                }
            }
        }
        // If we couldn't do anything, don't check again until the chest contents are changed
        if(brewable)
        {
            lastCheck = 0;
        }
        else
        {
            lastCheck = inv.getLastUpdate();
        }
    }

    private boolean canBrew()
    {
        // If there is no ingredient, what are we gonna brew?
        if((items[3] == null) || (items[3].count <= 0))
        {
            return false;
        }
        // Can one drink this?
        if(!isIngredient(items[3]))
        {
            return false;
        }
        // As long as we find a potion we can put our ingredient in, we'll be fine.
        for(int i = 0; i < 3; i++)
        {
            if(isBrewable(items[i], items[3]))
            {
                return true;
            }
        }
        // Didn't find anything matching
        return false;
    }

    private void brew()
    {
        // Can't brew? Goodbye
        if(!canBrew())
        {
            return;
        }
        ItemStack ingredient = items[3];
        // Go through all the potions and see what has to be done
        for(int i = 0; i < 3; i++)
        {
            // CraftBukkit events :D
            int mode = Config.getInt("brewevent.mode");
            if(mode > 0)
            {
                BrewEvent event = new BrewEvent(mode > 1 ? world.getWorld().getBlockAt(0, 0, 0) : null, this.bukkitInv);
                Bukkit.getPluginManager().callEvent(event);
                if(event.isCancelled() && !Config.bool("brewevent.ignorecancelled"))
                {
                    return;
                }
            }
            items[i] = processItem(items[i], ingredient);
        }
        // Is the ingredient a container?
        if(ingredient.getItem().#FIELD_ITEM_1#()) // Derpnote
        {
            items[3] = new ItemStack(ingredient.getItem().#FIELD_ITEM_2#()); // Derpnote
        }
        // Or not?
        else
        {
            items[3].count--;
            // Let 0 be null
            if(items[3].count <= 0)
            {
                items[3] = null;
            }
        }
    }

    private boolean isIngredient(ItemStack item)
    {
        if(item == null)
        {
            return false;
        }
        // CUSTOM INGREDIENTS HERE
        return checkWhatever(item);
    }

    private boolean matchesResult(ItemStack item, ItemStack ingredient)
    {
        return (item != null) && !Util.areEqual(item, processItem(item, ingredient));
    }

    private boolean isBrewable(ItemStack item, ItemStack ingredient)
    {
        ---------- PRE 1.9 START ----------
        // If we don't have an item or it's equal to it's result... NEXT!
        if(!matchesResult(item, ingredient))
        {
            return false;
        }
        // So then, is it a potion?
        if(Item.#FIELD_ITEM_7#(item.getItem()) == Material.POTION.getId())
        {
            // Make sure we don't get negative data values
            int meta = item.getData();
            int result = getPotionMeta(meta, ingredient);
            // I have no idea what this does
            if((!ItemPotion.#FIELD_ITEMPOTION_1#(meta)) && (ItemPotion.#FIELD_ITEMPOTION_1#(result))) // Derpnote
            {
                return true;
            }
            // Lists of effects
            List list = ((ItemPotion)Item.#FIELD_ITEM_8#(Material.POTION.getId())).#FIELD_ITEMPOTION_2#(meta); // Derpnote
            List list1 = ((ItemPotion)Item.#FIELD_ITEM_8#(Material.POTION.getId())).#FIELD_ITEMPOTION_2#(result); // Derpnote
            // Just copied this part from TileEntityBrewingStand.o()
            if(((meta <= 0) || (list != list1)) && ((list == null) || ((!list.equals(list1)) && (list1 != null))) && (meta != result))
            {
                return true;
            }
            return false;
        }
        // If it's not a potion (we already checked if it even can brew), then go ahead!
        return true;
        ---------- PRE 1.9 END ----------
        ---------- SINCE 1.9 START ----------
        return item == null ? false : PotionBrewer.#FIELD_POTIONBREWER_3#(item, ingredient);
        ---------- SINCE 1.9 END ----------
    }

    private double getBrewSpeed(ItemStack item)
    {
        if(item == null)
        {
            return 0.0D;
        }
        // CUSTOM INGREDIENTS HERE
        return 1.0D;
    }

    ---------- PRE 1.9 START ----------
    private ItemStack getBrewResult(ItemStack potion, ItemStack ingredient)
    {
        if((potion == null) || (ingredient == null))
        {
            return potion;
        }
        // CUSTOM RESULTS HERE
        if(checkWhatever(ingredient) && (Item.#FIELD_ITEM_7#(potion.getItem()) == Material.POTION.getId())) // Derpnote
        {
            return new ItemStack(potion.getItem(), potion.count,
            PotionBrewer.#FIELD_POTIONBREWER_1#(potion.getData(), ingredient.getItem().#FIELD_ITEM_4#(ingredient))); // Derpnote
        }
        return potion;
    }
    ---------- PRE 1.9 END ----------

    private ItemStack processItem(ItemStack potion, ItemStack ingredient)
    {
        ---------- PRE 1.9 START ----------
        // No potion or equal to the result... NEXT!
        if(!matchesResult(potion, ingredient))
        {
            return potion;
        }
        // Is it a potion then?
        if(Item.#FIELD_ITEM_7#(potion.getItem()) == Material.POTION.getId())
        {
            int meta = potion.getData() < 0 ? 0 : potion.getData();
            int result = getPotionMeta(meta, ingredient) & 0xffffffff;
            // Dafuq is this good for?
            if((!ItemPotion.#FIELD_ITEMPOTION_1#(meta)) && (ItemPotion.#FIELD_ITEMPOTION_1#(result))) // Derpnote
            {
                potion.setData(result);
                return potion;
            }
            // Lists of effects
            List list = ((ItemPotion)Item.#FIELD_ITEM_8#(Material.POTION.getId())).#FIELD_ITEMPOTION_2#(meta); // Derpnote
            List list1 = ((ItemPotion)Item.#FIELD_ITEM_8#(Material.POTION.getId())).#FIELD_ITEMPOTION_2#(result); // Derpnote
            if(((meta <= 0) || (list != list1)) && ((list == null) || ((!list.equals(list1)) && (list1 != null))) && (meta != result))
            {
                potion.setData(result);
            }
            return potion;
        }
        // Much easier if it's not a potion
        return getBrewResult(potion, ingredient);
        ---------- PRE 1.9 END ----------
        ---------- SINCE 1.9 START ----------
        return PotionBrewer.#FIELD_POTIONBREWER_4#(ingredient, potion);
        ---------- SINCE 1.9 END ----------
    }

    ---------- PRE 1.9 START ----------
    private int getPotionMeta(int i, ItemStack item)
    {
        if(item == null)
        {
            return i;
        }
        if(checkWhatever(item))
        {
            return PotionBrewer.#FIELD_POTIONBREWER_1#(i, item.getItem().#FIELD_ITEM_4#(item)); // Derpnote
        }
        return i;
    }
    ---------- PRE 1.9 END ----------

    private void brewTime(int i)
    {
        #F_SETPROPERTY#(0, i);
    }

    // Compatibility
    public InventoryHolder getOwner()
    {
        return null;
    }

    public boolean #FIELD_IINVENTORY_1#(EntityHuman entityhuman) // Derpnote
    {
        return true;
    }

    public void onOpen(CraftHumanEntity who)
    {
    }

    public void onClose(CraftHumanEntity who)
    {
    }

    public List<HumanEntity> getViewers()
    {
        return new ArrayList<HumanEntity>();
    }

    private boolean checkWhatever(ItemStack item)
    {
        ---------- PRE 1.9 START ----------
        return item.getItem().#FIELD_ITEM_3#(item);
        ---------- PRE 1.9 END ----------
        ---------- SINCE 1.9 START ----------
        return PotionBrewer.#FIELD_POTIONBREWER_2#(item);
        ---------- SINCE 1.9 END ----------
    }
}
