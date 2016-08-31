package codechicken.nei;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import java.util.HashSet;

public class TaggedInventoryArea
{
    public HashSet<Integer> slots = new HashSet<Integer>();
    public String tagName;
    /**
     * Only for player inventories.
     */
    private IInventory inventory;
    /**
     * Other inventories
     */
    private Container container;
    
    public TaggedInventoryArea(InventoryPlayer invPlayer)
    {
        this("InventoryPlayer", 0, 39, null);
        inventory = invPlayer;
    }
    
    public TaggedInventoryArea(String name, int first, int last, Container container)
    {
        this.container = container;
        tagName = name;
        for(int i = first; i <= last; i++)
            slots.add(i);
    }

    public ItemStack getStackInSlot(int i)
    {
        if(inventory != null)
            return inventory.getStackInSlot(i);
        return container.getSlot(i).getStack();
    }
    
    public boolean isContainer()
    {
        return inventory == null;
    }
}