package buildcraft.builders;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

public enum BuildersGuis {
    ARCHITECT,
    BUILDER,
    LIBRARY;

    public void openGUI(EntityPlayer player) {
        player.openGui(BuildCraftBuilders.INSTANCE, ordinal(), player.getEntityWorld(), 0, 0, 0);
    }

    public void openGUI(EntityPlayer player, BlockPos pos) {
        player.openGui(BuildCraftBuilders.INSTANCE, ordinal(), player.getEntityWorld(), pos.getX(), pos.getY(), pos.getZ());
    }
}
