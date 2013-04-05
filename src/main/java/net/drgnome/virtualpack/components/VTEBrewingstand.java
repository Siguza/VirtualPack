// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.components;

import java.util.*;
import net.minecraft.server.v1_5_R2.*;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.craftbukkit.v1_5_R2.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import net.drgnome.virtualpack.VPack;
import net.drgnome.virtualpack.util.*;

public class VTEBrewingstand extends TileEntityBrewingStand
{
    // To access the chests
    private VPack vpack;
    public int link;
    private double myBrewTime;
    private double brewSpeed;
    private int lastID;
    private long lastCheck;
    
    public VTEBrewingstand(VPack vpack)
    {
        this.vpack = vpack;
        link = 0;
        myBrewTime = 0.0D;
        brewTime = 0;
        brewSpeed = 1.0D;
        lastCheck = 0;
    }
    
    // Read from save
    public VTEBrewingstand(VPack vpack, String data[])
    {
        this(vpack);
        // Get our items
        for(int i = 0; i < Util.min(data.length, items.length); i++)
        {
            items[i] = Util.stringToItemStack(data[i]);
        }
        // And the times
        myBrewTime = Util.tryParse(data[4], myBrewTime);
        brewTime = Util.floor(myBrewTime);
        link = Util.tryParse(data[5], link);
        brewSpeed = getBrewSpeed(items[3]);
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
        return list.toArray(new String[0]);
    }
    
    // For compatibility
    public void g() // Derpnote
    {
        tick(1);
    }
    
    public void tick(int ticks)
    {
        checkLink();
        int newID = items[3] == null ? 0 : items[3].id;
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
        if(canBrew() && (myBrewTime > 0.0D))
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
        if(canBrew())
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
        brewTime = Util.floor(myBrewTime);
    }
    
    protected void checkLink()
    {
        // If there's a link, check what's to do
        if(canBrew() || (link == 0) || (vpack == null) && (vpack.getInv(link) == null) || (lastCheck >= vpack.getInv(link).getLastUpdate()))
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
                if(!isBrewable(items[i], items[3]))
                {
                    ItemStack item;
                    for(int j = 0; j < inv.getSize(); j++)
                    {
                        item = inv.getItem(j);
                        // Did we find something brewable?
                        if(isBrewable(item, items[3]))
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
                    match[i] += isBrewable(items[j], item) ? 1 : 0;
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
                        if(isBrewable(inv.getItem(j), item))
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
        if(canBrew())
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
            // If we don't have an item or it's equal to it's result... NEXT!
            if(!isBrewable(items[i], items[3]))
            {
                continue;
            }
            // So then, is it a potion?
            if(items[i].id == Item.POTION.id)
            {
                // Make sure we don't get negative data values
                int meta = items[i].getData();
                int result = getPotionMeta(meta, items[3]);
                // I have no idea what this does
                if((!ItemPotion.f(meta)) && (ItemPotion.f(result))) // Derpnote
                {
                    return true;
                }
                // Lists of effects
                List list = Item.POTION.c(meta); // Derpnote
                List list1 = Item.POTION.c(result); // Derpnote
                // Just copied this part from TileEntityBrewingStand.o()
                if(((meta <= 0) || (list != list1)) && ((list == null) || ((!list.equals(list1)) && (list1 != null))) && (meta != result))
                {
                    return true;
                }
            }
            // If it's not a potion (we already checked if it even can brew), then go ahead!
            else
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
        ItemStack potion;
        // Go through all the potions and see what has to be done
        for(int i = 0; i < 3; i++)
        {
            // No potion or equal to the result... NEXT!
            if(!isBrewable(items[i], items[3]))
            {
                continue;
            }
            // Is it a potion then?
            if(items[i].id == Item.POTION.id)
            {
                int meta = items[i].getData() < 0 ? 0 : items[i].getData();
                int result = getPotionMeta(meta, items[3]) < 0 ? 0 : getPotionMeta(meta, items[3]);
                // Dafuq is this good for?
                if((!ItemPotion.f(meta)) && (ItemPotion.f(result))) // Derpnote
                {
                    items[i].setData(result);
                    continue;
                }
                // Lists of effects
                List list = Item.POTION.c(meta); // Derpnote
                List list1 = Item.POTION.c(result); // Derpnote
                if(((meta <= 0) || (list != list1)) && ((list == null) || ((!list.equals(list1)) && (list1 != null))) && (meta != result))
                {
                    items[i].setData(result);
                    continue;
                }
            }
            // Much easier if it's not a potion
            else
            {
                items[i] = getBrewResult(items[i], items[3]);
            }
        }
        // Is the ingredient a container?
        if(Item.byId[items[3].id].t()) // Derpnote
        {
            items[3] = new ItemStack(Item.byId[items[3].id].s()); // Derpnote
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
        return Item.byId[item.id].w(); // Derpnote
    }
    
    private boolean isBrewable(ItemStack item, ItemStack ingredient)
    {
        return (item != null) && !Util.areEqual(item, getBrewResult(item, ingredient)); // Derpnote
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
    
    private ItemStack getBrewResult(ItemStack potion, ItemStack ingredient)
    {
        if((potion == null) || (ingredient == null))
        {
            return potion;
        }
        // CUSTOM RESULTS HERE
        if((Item.byId[ingredient.id].w()) && (potion.id == Item.POTION.id)) // Derpnote
        {
            return new ItemStack(potion.id, potion.count, PotionBrewer.a(potion.getData(), Item.byId[ingredient.id].v())); // Derpnote
        }
        return potion;
    }
    
    private int getPotionMeta(int i, ItemStack item)
    {
        if(item == null)
        {
            return i;
        }
        // Cryptic names, Y U NO MAKE SENSE?
        if(Item.byId[item.id].w()) // Derpnote
        {
            return PotionBrewer.a(i, Item.byId[item.id].v()); // Derpnote
        }
        return i;
    }

    // Compatibility
    public InventoryHolder getOwner()
    {
        return null;
    }
    
    public boolean a(EntityHuman entityhuman) // Derpnote
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

}