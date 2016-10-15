package buildcraft.transport.item;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.misc.SoundUtil;
import buildcraft.transport.api_move.IItemPluggable;
import buildcraft.transport.api_move.IPipeHolder;
import buildcraft.transport.api_move.PipePluggable;
import buildcraft.transport.api_move.PluggableDefinition;
import buildcraft.transport.api_move.PluggableDefinition.IPluggableCreator;

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
    public PipePluggable onPlace(ItemStack stack, IPipeHolder holder, EnumFacing side) {
        SoundUtil.playBlockPlace(holder.getPipeWorld(), holder.getPipePos());
        return creator.createSimplePluggable(definition, holder, side);
    }
}
