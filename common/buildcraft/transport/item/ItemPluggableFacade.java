package buildcraft.transport.item;

import buildcraft.api.transport.IItemPluggable;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.transport.BCTransportPlugs;
import buildcraft.transport.plug.PluggableFacade;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;

public class ItemPluggableFacade extends ItemBC_Neptune implements IItemPluggable {
    public ItemPluggableFacade(String id) {
        super(id);
        setMaxDamage(0);
        setHasSubtypes(true);
    }

    @Override
    public PipePluggable onPlace(@Nonnull ItemStack stack, IPipeHolder holder, EnumFacing side) {
        return new PluggableFacade(BCTransportPlugs.facade, holder, side);
    }
}
