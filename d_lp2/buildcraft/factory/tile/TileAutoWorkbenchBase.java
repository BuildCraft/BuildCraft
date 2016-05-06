package buildcraft.factory.tile;

import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.items.IItemHandlerModifiable;

import buildcraft.api.core.EnumPipePart;
import buildcraft.lib.tile.TileBCInventory_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;

public abstract class TileAutoWorkbenchBase extends TileBCInventory_Neptune {
    // protected final IItemHandlerModifiable invBlueprint;
    protected final IItemHandlerModifiable invMaterials;
    protected final IItemHandlerModifiable invResult;

    public TileAutoWorkbenchBase(int slots) {
        // invBlueprint = addInventory("blueprint", slots);
        invMaterials = addInventory("materials", slots, EnumAccess.INSERT, EnumPipePart.VALUES);
        invResult = addInventory("result", 1, EnumAccess.EXTRACT, EnumPipePart.VALUES);
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {

        super.writeToNBT(compound);
    }
}
