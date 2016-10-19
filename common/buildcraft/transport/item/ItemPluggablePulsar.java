package buildcraft.transport.item;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import buildcraft.api.mj.IMjRedstoneReceiver;
import buildcraft.api.transport.neptune.*;

import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.misc.SoundUtil;
import buildcraft.transport.BCTransportPlugs;
import buildcraft.transport.plug.PluggablePulsar;

public class ItemPluggablePulsar extends ItemBC_Neptune implements IItemPluggable {
    public ItemPluggablePulsar(String id) {
        super(id);
    }

    @Override
    public PipePluggable onPlace(ItemStack stack, IPipeHolder holder, EnumFacing side) {
        IPipe pipe = holder.getPipe();
        if (pipe == null) {
            return null;
        }
        PipeBehaviour behaviour = pipe.getBehaviour();
        if (behaviour instanceof IMjRedstoneReceiver) {
            SoundUtil.playBlockPlace(holder.getPipeWorld(), holder.getPipePos());
            return new PluggablePulsar(BCTransportPlugs.pulsar, holder, side);
        } else {
            return null;
        }
    }
}
