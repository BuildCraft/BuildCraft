package buildcraft.energy.container;

import net.minecraft.entity.player.EntityPlayer;

import buildcraft.energy.tile.TileEngineStone_BC8;
import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;

public class ContainerEngineStone_BC8 extends ContainerBCTile<TileEngineStone_BC8> {
    public ContainerEngineStone_BC8(EntityPlayer player, TileEngineStone_BC8 engine) {
        super(player, engine);

        addFullPlayerInventory(84);
        addSlotToContainer(new SlotBase(engine.invFuel, 0, 80, 41) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return TileEntityFurnace.getItemBurnTime(stack) > 0;
            }
        });
    }
    
    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return tile.canInteractWith(player);
    }
}
