package ct.buildcraft.api.enums;

import ct.buildcraft.api.properties.BuildCraftProperties;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.BlockState;

public enum EnumMachineState implements StringRepresentable {
    OFF,
    ON,
    DONE;

    public static EnumMachineState getType(BlockState state) {
        return state.getValue(BuildCraftProperties.MACHINE_STATE);
    }

    @Override
    public String getSerializedName() {
        return name();
    }
}
