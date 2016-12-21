package buildcraft.energy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

public enum BCEnergyGuis {
    ENGINE_STONE,
    ENGINE_IRON;

    public static final BCEnergyGuis[] VALUES = values();

    public static BCEnergyGuis get(int id) {
        if (id < 0 || id >= VALUES.length) return null;
        return VALUES[id];
    }

    public void openGUI(EntityPlayer player) {
        player.openGui(BCEnergy.INSTANCE, ordinal(), player.getEntityWorld(), 0, 0, 0);
    }

    public void openGUI(EntityPlayer player, BlockPos pos) {
        player.openGui(BCEnergy.INSTANCE, ordinal(), player.getEntityWorld(), pos.getX(), pos.getY(), pos.getZ());
    }

}
