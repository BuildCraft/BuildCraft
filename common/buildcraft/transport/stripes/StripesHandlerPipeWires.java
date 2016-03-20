package buildcraft.transport.stripes;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandler;
import buildcraft.core.lib.utils.Utils;
import buildcraft.transport.ItemPipeWire;
import buildcraft.transport.TileGenericPipe;

public class StripesHandlerPipeWires implements IStripesHandler {
    @Override
    public StripesHandlerType getType() {
        return StripesHandlerType.ITEM_USE;
    }

    @Override
    public boolean shouldHandle(ItemStack stack) {
        return stack.getItem() instanceof ItemPipeWire;
    }

    @Override
    public boolean handle(World world, BlockPos pos, EnumFacing direction, ItemStack stack, EntityPlayer player, IStripesActivator activator) {
        int pipesToTry = 8;
        int pipeWireColor = stack.getItemDamage();

        Vec3 p = Utils.convert(pos);

        while (pipesToTry > 0) {
            p = p.add(Utils.convert(direction, -1));

            TileEntity tile = world.getTileEntity(Utils.convertFloor(p));
            if (tile instanceof TileGenericPipe) {
                TileGenericPipe pipeTile = (TileGenericPipe) tile;

                if (!pipeTile.pipe.wireSet[pipeWireColor]) {
                    pipeTile.pipe.wireSet[pipeWireColor] = true;
                    pipeTile.pipe.signalStrength[pipeWireColor] = 0;

                    pipeTile.pipe.updateSignalState();
                    pipeTile.scheduleRenderUpdate();
                    world.notifyNeighborsOfStateChange(pipeTile.getPos(), pipeTile.getBlock());
                    return true;
                } else {
                    pipesToTry--;
                    continue;
                }
            } else {
                // Not a pipe, don't follow chain
                break;
            }
        }

        return false;
    }
}
