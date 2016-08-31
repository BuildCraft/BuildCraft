package codechicken.nei;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

import java.util.List;

/**
 * If this is implemented on a gui, it will be automatically registered
 */
public interface INEIGuiHandler
{
    public VisiblityData modifyVisiblity(GuiContainer gui, VisiblityData currentVisibility);
    
    /**
     * NEI will give the specified item to the InventoryRange returned if the player's inventory is full.
     * return null for no range
     */
    public Iterable<Integer> getItemSpawnSlots(GuiContainer gui, ItemStack item);
    
    /**
     * @return A list of TaggedInventoryAreas that will be used with the savestates.
     */
    public List<TaggedInventoryArea> getInventoryAreas(GuiContainer gui);
    
    /**
     * Handles clicks while an itemstack has been dragged from the item panel. Use this to set configurable slots and the like. 
     * Changes made to the stackSize of the dragged stack will be kept
     * @param gui The current gui instance
     * @param mousex The x position of the mouse
     * @param mousey The y position of the mouse
     * @param draggedStack The stack being dragged from the item panel
     * @param button The button presed
     * @return True if the drag n drop was handled. False to resume processing through other routes. The held stack will be deleted if draggedStack.stackSize == 0
     */
    public boolean handleDragNDrop(GuiContainer gui, int mousex, int mousey, ItemStack draggedStack, int button);
    
    /**
     * Used to prevent the item panel from drawing on top of other gui elements.
     * This function will also be called with a 1x1 size rectangle on the mouse position for determining if the given coordinate should override item panel functions such as scrolling
     * @param x The x coordinate of the rectangle bounding the slot
     * @param y The y coordinate of the rectangle bounding the slot
     * @param w The w coordinate of the rectangle bounding the slot
     * @param h The h coordinate of the rectangle bounding the slot
     * @return true if the item panel slot within the specified rectangle should not be rendered.
     */
    public boolean hideItemPanelSlot(GuiContainer gui, int x, int y, int w, int h);
}