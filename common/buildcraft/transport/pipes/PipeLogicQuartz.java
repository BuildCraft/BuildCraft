/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.pipes;
import net.minecraft.util.EnumFacing;
import buildcraft.transport.Pipe;
import buildcraft.transport.TravelingItem;

public abstract class PipeLogicQuartz extends PipeLogicIron{
    public PipeLogicQuartz(Pipe<?> pipe) {
        super(pipe);
    }

    private EnumFacing getInverse(EnumFacing facing){
        switch(facing){
            case DOWN:
                return EnumFacing.UP;
            case UP:
                return EnumFacing.DOWN;
            case EAST:
                return EnumFacing.WEST;
            case WEST:
                return EnumFacing.EAST;
            case NORTH:
                return EnumFacing.SOUTH;
            case SOUTH:
                return EnumFacing.NORTH;
            default:
                return null;
        }
    }

    public void switchPosition(TravelingItem item) {
        if(item == null){
            super.switchPosition();
            return;
        }

        int meta = pipe.container.getBlockMetadata();
        EnumFacing bounceback = getInverse(item.input);

        for (int i = meta + 1; i <= meta + 6; ++i) {
            EnumFacing facing = EnumFacing.getFront(i % 6);
            if (!facing.equals(bounceback)) {
                if(setFacing(facing)){
                    return;
                }
            }
        }
    }

    @Override
    public void onBlockPlaced() {
        setFacing(null);
        switchPosition(null);
    }
}
