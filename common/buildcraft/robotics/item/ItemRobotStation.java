/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.item;

import buildcraft.api.transport.IItemPluggable;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.misc.SoundUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;

import javax.annotation.Nonnull;

public class ItemRobotStation extends ItemBC_Neptune implements IItemPluggable {
    private final PluggableDefinition definition;
    private final PluggableDefinition.IPluggableCreator creator;

    public ItemRobotStation(String idBC, Properties properties, PluggableDefinition definition, PluggableDefinition.IPluggableCreator creator) {
        // super(BCCreativeTab.get("boards"));
        super(idBC, properties);
        this.definition = definition;
        this.creator = creator;
        if (creator == null) {
            throw new NullPointerException("Creator was null!");
        }
    }

//    @Override
//    public String getDescriptionId(ItemStack stack) {
//        return "item.PipeRobotStation";
//    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader world, BlockPos pos, Player player) {
        return true;
    }

    @Override
    public PipePluggable onPlace(@Nonnull ItemStack stack, IPipeHolder holder, Direction side, Player player, InteractionHand hand) {
        // return new PluggableRobotStation();
        SoundUtil.playBlockPlace(holder.getPipeWorld(), holder.getPipePos());
        return creator.createSimplePluggable(definition, holder, side);
    }
}
