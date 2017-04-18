package buildcraft.transport.item;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.IItemPluggable;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import buildcraft.api.transport.pluggable.PluggableDefinition.IPluggableCreator;

import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.misc.SoundUtil;
import net.minecraft.util.EnumHand;

public class ItemPluggableSimple extends ItemBC_Neptune implements IItemPluggable {

    private final PluggableDefinition definition;

    @Nonnull
    private final IPluggableCreator creator;

    public ItemPluggableSimple(String id, PluggableDefinition definition, IPluggableCreator creator) {
        super(id);
        this.definition = definition;
        if (creator == null) {
            throw new NullPointerException("Null creator! (Was given " + definition.identifier + ")");
        }
        this.creator = creator;
    }

    public ItemPluggableSimple(String id, PluggableDefinition definition) {
        this(id, definition, definition.creator);
    }

    @Override
    public PipePluggable onPlace(ItemStack stack, IPipeHolder holder, EnumFacing side, EntityPlayer player, EnumHand hand) {
        SoundUtil.playBlockPlace(holder.getPipeWorld(), holder.getPipePos());
        return creator.createSimplePluggable(definition, holder, side);
    }
}
