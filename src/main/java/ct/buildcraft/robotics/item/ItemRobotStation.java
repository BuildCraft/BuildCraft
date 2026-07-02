package ct.buildcraft.robotics.item;

import javax.annotation.Nonnull;

import ct.buildcraft.api.transport.IItemPluggable;
import ct.buildcraft.api.transport.pipe.IPipeHolder;
import ct.buildcraft.api.transport.pluggable.PipePluggable;
import ct.buildcraft.robotics.BCRoboticsPlugs;
import ct.buildcraft.robotics.plug.RobotStationPluggable;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemRobotStation extends Item implements IItemPluggable {
    public ItemRobotStation(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.buildcraftrobotics.robot_station");
    }

    @Override
    public boolean doesSneakBypassUse(net.minecraft.world.item.ItemStack stack, net.minecraft.world.level.LevelReader world, net.minecraft.core.BlockPos pos, Player player) {
        return true;
    }

    @Override
    public @Nonnull PipePluggable onPlace(@Nonnull ItemStack stack, IPipeHolder holder, Direction side, Player player, InteractionHand hand) {
        if (side == null || BCRoboticsPlugs.robotStation == null) {
            return PipePluggable.EMPTY;
        }
        return new RobotStationPluggable(BCRoboticsPlugs.robotStation, holder, side);
    }
}
