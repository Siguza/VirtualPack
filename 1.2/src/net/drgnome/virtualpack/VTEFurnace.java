// Bukkit Plugin "VirtualPack" by Siguza
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack;

import java.util.*;

import net.minecraft.server.*;

import org.bukkit.inventory.InventoryHolder;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;

import static net.drgnome.virtualpack.Util.*;

// VirtualTileEntityFurnace is way too long, therefore VTE
public class VTEFurnace extends TileEntityFurnace
{
    // To access the chests
    private VPack vpack;
    public int link;
    private ItemStack[] contents = new ItemStack[3];
    // For custom stuff
    private double burnSpeed;
    private double meltSpeed;
    // I'm internally using "myCookTime" to not lose any precision, but for displaying the progress, I still have to use "cookTime"
    private double myCookTime;
    // Call me paranoid, but this has to be checked
    private int lastID;
    // Increases performance (or should at least)
    private long lastCheck;
    
    // New VTE
    public VTEFurnace(VPack vpack)
    {
        this.vpack = vpack;
        link = 0;
        burnSpeed = 1.0D;
        meltSpeed = 1.0D;
        myCookTime = 0.0D;
        cookTime = 0;
        burnTime = 0;
        ticksForCurrentFuel = 0;
        lastID = 0;
        lastCheck = 0;
    }
    
    // Read from save
    public VTEFurnace(VPack vpack, String data[]) throws Throwable
    {
        this(vpack, data, 0);
    }
    
    // Read from save
    public VTEFurnace(VPack vpack, String data[], int offset) throws Throwable
    {
        this(vpack);
        // Where to stop?
        int max = data.length - offset < contents.length ? data.length - offset : contents.length;
        for(int i = 0; i < max; i++)
        {
            contents[i] = stringToItemStack(data[i + offset]);
        }
        lastID = contents[0] == null ? 0 : contents[0].id;
        // If the data array is long enough, we try to parse its entry, and if it's too short or the parsing fails, we'll leave it as it is.
        burnTime = (data.length - offset > 3) ? tryParse(data[offset + 3], burnTime) : burnTime;
        ticksForCurrentFuel = (data.length - offset > 4) ? tryParse(data[offset + 4], ticksForCurrentFuel) : ticksForCurrentFuel;
        myCookTime = (data.length - offset > 5) ? tryParse(data[offset + 5], myCookTime) : myCookTime;
        cookTime = (int)Math.round(myCookTime);
        link = (data.length - offset > 6) ? tryParse(data[offset + 6], link) : link;
        // This has to be read from the save file
        burnSpeed = (data.length - offset > 7) ? tryParse(data[offset + 7], getBurnSpeed(contents[1])) : getBurnSpeed(contents[1]);
        // While this doesn't (this item doesn't get consumed before the progress is done)
        meltSpeed = getMeltSpeed(contents[0]);
    }
    
    public String[] save()
    {
        ArrayList<String> list = new ArrayList<String>();
        for(int i = 0; i < 3; i++)
        {
            list.add(itemStackToString(contents[i]));
        }
        list.add(Integer.toString(burnTime));
        list.add(Integer.toString(ticksForCurrentFuel));
        list.add(Double.toString(myCookTime));
        list.add(Integer.toString(link));
        // I save this now, because you could lose burn speed if it's the last fuel item and the server gets restartet
        list.add(Double.toString(burnSpeed));
        return list.toArray(new String[0]);
    }
    
    // For compatibility
    public void q_() // Derpnote
    {
        tick();
    }
    
    public void tick()
    {
        checkLink();
        int newID = contents[0] == null ? 0 : contents[0].id;
        // Has the item been changed?
        if(newID != lastID)
        {
            // Then reset the progress!
            myCookTime = 0.0D;
            lastID = newID;
            // And, most important: change the melt speed
            meltSpeed = getMeltSpeed(contents[0]);
        }
        // So, can we now finally burn?
        if(canBurn() && !isBurning() && (getFuelTime(contents[1]) > 0))
        {
            // I have no idea what "ticksForCurrentFuel" is good for, but it works fine like this
            burnTime = ticksForCurrentFuel = getFuelTime(contents[1]);
            // Before we remove the item: how fast does it burn?
            burnSpeed = getBurnSpeed(contents[1]);
            // If it's a container item (lava bucket), we only consume its contents (not like evil Notch!)
            if(Item.byId[contents[1].id].k()) // Derpnote
            {
                contents[1] = new ItemStack(Item.byId[contents[1].id].j());  // Derpnote
            }
            // If it's not a container, consume it! Om nom nom nom!
            else
            {
                contents[1].count--;
                // Let 0 be null
                if(contents[1].count <= 0)
                {
                    contents[1] = null;
                }
            }
        }
        // Now, burning?
        if(isBurning())
        {
            // Then move on
            burnTime--;
            // I'm using a double here because of the custom recipes.
            // The faster this fuel burns and the faster the recipe melts, the faster we're done
            myCookTime += burnSpeed * meltSpeed;
            // Finished burning?
            if(myCookTime >= 200.0D)
            {
                myCookTime -= 200.0D;
                burn();
            }
        }
        // If it's not burning, we reset the burning progress!
        else
        {
            myCookTime = 0.0D;
        }
        // And for the display (I'm using floor rather than round to not cause the client to do shit when we not really reached 200):
        cookTime = (int)Math.floor(myCookTime);
    }
    
    protected void checkLink()
    {
        // If this furnace is linked, then we should see if there's a reason to interact
        if(isFine() || (link == 0) || (vpack == null) || (vpack.getInv(link) == null) || (lastCheck >= vpack.getInv(link).getLastUpdate()))
        {
            return;
        }
        VInv inv = vpack.getInv(link);
        // If we can't burn at the moment, we need different stuff
        if(!canBurn())
        {
            // Do we need a different ingredient?
            boolean get0 = false;
            // If there is none, then of course
            if(contents[0] == null)
            {
                get0 = true;
            }
            else
            {
                // Or if it can't be molten
                if(getBurnResult(contents[0]) == null)
                {
                    get0 = true;
                }
            }
            // So do we need a different ingredient?
            if(get0)
            {
                // Lets search for a meltable item
                ItemStack item;
                for(int i = 0; i < inv.getSize(); i++)
                {
                    item = inv.getItem(i);
                    if(getBurnResult(item) != null)
                    {
                        // We have to exchange the items, but we can't do it directly without messing everything up
                        item = copy(item);
                        ItemStack item1 = copy(contents[0]);
                        contents[0] = item;
                        inv.setItem(i, item1);
                        // And leave the loop
                        break;
                    }
                }
            }
            // Now, if there is any reason we can't burn, we're done and put the output item away (if there is any)
            if(!canBurn() && (contents[2] != null))
            {
                // Lets search for a place we can put our stuff
                ItemStack item;
                for(int i = 0; i < inv.getSize(); i++)
                {
                    item = inv.getItem(i);
                    // If there's no item: Lol, too easy ^^
                    if(item == null)
                    {
                        inv.setItem(i, copy(contents[2]));
                        contents[2] = null;
                        // And we can leave the loop
                        break;
                    }
                    // If there an item, then the materials have to match
                    else if(contents[2].doMaterialsMatch(item))
                    {
                        // Put away as much as possible
                        int max = min(contents[2].count, min(Item.byId[item.id].getMaxStackSize(), getMaxStackSize()) - item.count);
                        item.count += max;
                        contents[2].count -= max;
                        // If we've put everything away
                        if(contents[2].count <= 0)
                        {
                            contents[2] = null;
                            // Then let's go away from here
                            break;
                        }
                    }
                }
            }
        }
        // Now, if we finally can burn, but we don't have fuel, then go and get some!
        if(canBurn() && !isBurning() && (getFuelTime(contents[1]) <= 0))
        {
            // Search for fuel
            ItemStack item;
            for(int i = 0; i < inv.getSize(); i++)
            {
                item = inv.getItem(i);
                // Is it fuel?
                if(getFuelTime(item) > 0)
                {
                    // Then take it!
                    item = copy(item);
                    ItemStack item1 = copy(contents[1]);
                    contents[1] = item;
                    inv.setItem(i, item1);
                    // And goodbye
                    break;
                }
            }
        }
        // If we couldn't do anything, don't check again until the chest contents are changed
        if(isFine())
        {
            lastCheck = 0;
        }
        else
        {
            lastCheck = inv.getLastUpdate();
        }
    }
    
    public boolean isFine()
    {
        return ((myCookTime > 0.0D) || (getFuelTime(contents[1]) > 0)) && canBurn();
    }
    
    // This needs a little addition
    public boolean isBurning()
    {
        return super.isBurning() && (burnSpeed > 0.0D);
    }
    
    private ItemStack getBurnResult(ItemStack item)
    {
        if(item == null)
        {
            return null;
        }
        int i = item.id;
        // CUSTOM RECIPE HERE
        return FurnaceRecipes.getInstance().getResult(i); // Derpnote "RecipesFurnace"
    }
    
    private double getMeltSpeed(ItemStack item)
    {
        if(item == null)
        {
            return 0.0D;
        }
        // CUSTOM RECIPE HERE
        return 1.0D;
    }
    
    private int getFuelTime(ItemStack item)
    {
        if(item == null)
        {
            return 0;
        }
        int i = item.id;
        Item it = item.getItem();
        // CUSTOM FUEL HERE
        if((i == Item.STICK.id) || (i == Block.SAPLING.id))
        {
            return 100;
        }
        /*else if(i == Block.WOOD_STEP.id)
        {
            return 150;
        }
        else if(((it instanceof ItemHoe) && (((ItemHoe)it).f().equals("WOOD"))) || ((it instanceof ItemSword) && (((ItemSword)it).f().equals("WOOD"))) || ((it instanceof ItemTool) && (((ItemTool)it).e().equals("WOOD")))) // Derpnote
        {
            return 200;
        }*/
        else if((i < 256) && (Block.byId[i].material == Material.WOOD))
        {
            return 300;
        }
        else if(i == Item.COAL.id)
        {
            return 1600;
        }
        else if(i == Item.BLAZE_ROD.id)
        {
            return 2400;
        }
        else if(i == Item.LAVA_BUCKET.id)
        {
            return 25600;
        }
        return 0;
    }
    
    private double getBurnSpeed(ItemStack item)
    {
        if(item == null)
        {
            return 0.0D;
        }
        // CUSTOM FUEL HERE
        return 1.0D;
    }
    
    private boolean canBurn()
    {
        // No ingredient, no recipe
        if(contents[0] == null)
        {
            return false;
        }
        ItemStack itemstack = getBurnResult(contents[0]);
        // No recipe, no burning
        if(itemstack == null)
        {
            return false;
        }
        // Free space? Let's burn!
        else if(contents[2] == null)
        {
            return true;
        }
        // Materials don't match? Too bad.
        else if(!contents[2].doMaterialsMatch(itemstack))
        {
            return false;
        }
        // As long as there is space, we can burn
        else if((contents[2].count + itemstack.count <= getMaxStackSize()) && (contents[2].count + itemstack.count <= contents[2].getMaxStackSize()))
        {
            return true;
        }
        return false;
    }
    
    public void burn()
    {
        // Can't burn? Goodbye
        if(!canBurn())
        {
            return;
        }
        ItemStack itemstack = getBurnResult(contents[0]);
        // Nothing in there? Then put something there.
        if(contents[2] == null)
        {
            contents[2] = copy(itemstack);
        }
        // Burn ahead
        else if(contents[2].doMaterialsMatch(itemstack))
        {
            contents[2].count += itemstack.count;
        }
        // And consume the ingredient item
        // Goddamn, you have container functions, use them! Notch!
        if(Item.byId[contents[0].id].k()) // Derpnote
        {
            contents[0] = new ItemStack(Item.byId[contents[0].id].j()); // Derpnote
        }
        else
        {
            contents[0].count--;
            // Let 0 be null
            if(contents[0].count <= 0)
            {
                contents[0] = null;
            }
        }
    }
    
    /***** The following methods are only here because they interact with the contents array, which is private *****/
    
    public ItemStack[] getContents()
    {
        return contents;
    }
    
    public int getSize()
    {
        return contents.length;
    }
    
    public ItemStack getItem(int i)
    {
        return contents[i];
    }
    
    public ItemStack splitStack(int i, int j)
    {
        if(contents[i] != null)
        {
            ItemStack itemstack;
            if(contents[i].count <= j)
            {
                itemstack = contents[i];
                contents[i] = null;
                return itemstack;
            }
            else
            {
                itemstack = contents[i].a(j); // Derpnote
                if(contents[i].count == 0)
                {
                    contents[i] = null;
                }
                return itemstack;
            }
        }
        else
        {
            return null;
        }
    }
    
    public ItemStack splitWithoutUpdate(int i)
    {
        if(contents[i] != null)
        {
            ItemStack itemstack = contents[i];
            contents[i] = null;
            return itemstack;
        }
        else
        {
            return null;
        }
    }
    
    public void setItem(int i, ItemStack itemstack)
    {
        contents[i] = itemstack;
        if(itemstack != null && itemstack.count > getMaxStackSize())
        {
            itemstack.count = getMaxStackSize();
        }
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