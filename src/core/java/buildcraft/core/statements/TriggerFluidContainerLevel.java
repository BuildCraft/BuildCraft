/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.statements;

import java.util.Locale;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerExternal;
import buildcraft.api.statements.StatementParameterItemStack;
import buildcraft.core.lib.utils.StringUtils;

public class TriggerFluidContainerLevel extends BCStatement implements ITriggerExternal {

    public enum TriggerType {

        BELOW25(0.25F),
        BELOW50(0.5F),
        BELOW75(0.75F);

        public final float level;

        private TriggerType(float level) {
            this.level = level;
        }
    }

    public TriggerType type;

    public TriggerFluidContainerLevel(TriggerType type) {
        super("buildcraft:fluid." + type.name().toLowerCase(Locale.ENGLISH), "buildcraft.fluid." + type.name().toLowerCase(Locale.ENGLISH));
        this.type = type;
    }

    @Override
    public int maxParameters() {
        return 1;
    }

    @Override
    public String getDescription() {
        return String.format(StringUtils.localize("gate.trigger.fluidlevel.below"), (int) (type.level * 100));
    }

    @Override
    public boolean isTriggerActive(TileEntity tile, EnumFacing side, IStatementContainer statementContainer, IStatementParameter[] parameters) {
        if (tile instanceof IFluidHandler) {
            IFluidHandler container = (IFluidHandler) tile;

            FluidStack searchedFluid = null;

            if (parameters != null && parameters.length >= 1 && parameters[0] != null && parameters[0].getItemStack() != null) {
                searchedFluid = FluidContainerRegistry.getFluidForFilledItem(parameters[0].getItemStack());
            }

            if (searchedFluid != null) {
                searchedFluid.amount = 1;
            }

            FluidTankInfo[] liquids = container.getTankInfo(side);
            if (liquids == null || liquids.length == 0) {
                return false;
            }

            for (FluidTankInfo c : liquids) {
                if (c == null) {
                    continue;
                }
                if (c.fluid == null) {
                    if (searchedFluid == null) {
                        return true;
                    }
                    return container.fill(side, searchedFluid, false) > 0;
                }

                if (searchedFluid == null || searchedFluid.isFluidEqual(c.fluid)) {
                    float percentage = (float) c.fluid.amount / (float) c.capacity;
                    return percentage < type.level;
                }
            }
        }

        return false;
    }

    @Override
    public IStatementParameter createParameter(int index) {
        return new StatementParameterItemStack();
    }
}
