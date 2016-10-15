package buildcraft.transport.pipe.behaviour;

import java.util.EnumSet;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.transport.PipeEventHandler;
import buildcraft.api.transport.PipeEventItem;

import buildcraft.transport.api_move.IPipe;

public class PipeBehaviourIron extends PipeBehaviourDirectional {
    public PipeBehaviourIron(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourIron(IPipe pipe, NBTTagCompound nbt) {
        super(pipe, nbt);
    }

    @Override
    public int getTextureIndex(EnumFacing face) {
        return face == currentDir.face ? 0 : 1;
    }

    @Override
    protected boolean canFaceDirection(EnumFacing dir) {
        return pipe.isConnected(dir);
    }

    @PipeEventHandler
    public void sideCheck(PipeEventItem.SideCheck sideCheck) {
        if (currentDir == EnumPipePart.CENTER) {
            sideCheck.possible.clear();
        } else {
            boolean contains = false;
            for (EnumSet<EnumFacing> set : sideCheck.possible) {
                if (set.contains(currentDir.face)) {
                    contains = true;
                    break;
                }
            }
            sideCheck.possible.clear();
            if (contains) {
                sideCheck.possible.add(EnumSet.of(currentDir.face));
            }
        }
    }

    @PipeEventHandler
    public void tryBounce(PipeEventItem.TryBounce tryBounce) {
        tryBounce.canBounce = true;
    }
}
