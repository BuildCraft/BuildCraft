package buildcraft.transport.pipe.behaviour;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.PipeEventHandler;
import buildcraft.api.transport.PipeEventItem;
import buildcraft.api.transport.neptune.IPipe;

import buildcraft.lib.misc.StackUtil;

public class PipeBehaviourDiamondItem extends PipeBehaviourDiamond {

    public PipeBehaviourDiamondItem(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourDiamondItem(IPipe pipe, NBTTagCompound nbt) {
        super(pipe, nbt);
    }

    @PipeEventHandler
    public void sideCheck(PipeEventItem.SideCheck sideCheck) {
        ItemStack toCompare = sideCheck.stack;
        for (EnumFacing face : EnumFacing.VALUES) {
            if (sideCheck.isAllowed(face) && pipe.isConnected(face)) {
                int offset = FILTERS_PER_SIDE * face.ordinal();
                boolean sideAllowed = false;
                boolean foundItem = false;
                for (int i = 0; i < FILTERS_PER_SIDE; i++) {
                    ItemStack compareTo = filters.getStackInSlot(offset + i);
                    if (compareTo.isEmpty()) continue;
                    foundItem = true;
                    if (StackUtil.isMatchingItemOrList(compareTo, toCompare)) {
                        sideAllowed = true;
                        break;
                    }
                }
                if (foundItem) {
                    if (sideAllowed) {
                        sideCheck.increasePrecedence(face, 12);
                    } else {
                        sideCheck.disallow(face);
                    }
                }
            }
        }
    }
}
