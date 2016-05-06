package buildcraft.factory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

public enum FactoryGuis {
    AUTO_WORKBENCH_ITEMS,
    AUTO_WORKBENCH_FLUIDS;

    public void openGUI(EntityPlayer player) {
        player.openGui(BuildCraftFactory.INSTANCE, ordinal(), player.getEntityWorld(), 0, 0, 0);
    }

    public void openGUI(EntityPlayer player, BlockPos pos) {
        player.openGui(BuildCraftFactory.INSTANCE, ordinal(), player.getEntityWorld(), pos.getX(), pos.getY(), pos.getZ());
    }
}
