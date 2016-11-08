/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.statements;

import java.util.Locale;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerExternal;
import buildcraft.api.statements.StatementParameterItemStack;

import buildcraft.core.BCCoreSprites;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import buildcraft.lib.misc.StringUtilBC;

public class TriggerFluidContainer extends BCStatement implements ITriggerExternal {

    public enum State {

        Empty,
        Contains,
        Space,
        Full;

        public static final State[] VALUES = values();
    }

    public State state;

    public TriggerFluidContainer(State state) {
        super("buildcraft:fluid." + state.name().toLowerCase(Locale.ROOT), "buildcraft.fluid." + state.name().toLowerCase(Locale.ROOT));
        this.state = state;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SpriteHolder getSpriteHolder() {
        return BCCoreSprites.TRIGGER_FLUID.get(state);
    }

    @Override
    public int maxParameters() {
        return state == State.Contains || state == State.Space ? 1 : 0;
    }

    @Override
    public String getDescription() {
        return StringUtilBC.localize("gate.trigger.fluid." + state.name().toLowerCase(Locale.ROOT));
    }

    @Override
    public boolean isTriggerActive(TileEntity tile, EnumFacing side, IStatementContainer statementContainer, IStatementParameter[] parameters) {
        IFluidHandler handler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.getOpposite());

        if (handler != null) {
            FluidStack searchedFluid = null;

            if (parameters != null && parameters.length >= 1 && parameters[0] != null && parameters[0].getItemStack() != null) {
                searchedFluid = FluidContainerRegistry.getFluidForFilledItem(parameters[0].getItemStack());
            }

            if (searchedFluid != null) {
                searchedFluid.amount = 1;
            }

            FluidTankInfo[] liquids = handler.getTankInfo(side);
            if (liquids == null || liquids.length == 0) {
                return false;
            }

            switch (state) {
                case Empty:
                    for (FluidTankInfo c : liquids) {
                        if (c != null && c.fluid != null && c.fluid.amount > 0 && (searchedFluid == null || searchedFluid.isFluidEqual(c.fluid))) {
                            return false;
                        }
                    }
                    return true;
                case Contains:
                    for (FluidTankInfo c : liquids) {
                        if (c != null && c.fluid != null && c.fluid.amount > 0 && (searchedFluid == null || searchedFluid.isFluidEqual(c.fluid))) {
                            return true;
                        }
                    }
                    return false;
                case Space:
                    if (searchedFluid == null) {
                        for (FluidTankInfo c : liquids) {
                            if (c != null && (c.fluid == null || c.fluid.amount < c.capacity)) {
                                return true;
                            }
                        }
                        return false;
                    }
                    return handler.fill(side, searchedFluid, false) > 0;
                case Full:
                    if (searchedFluid == null) {
                        for (FluidTankInfo c : liquids) {
                            if (c != null && (c.fluid == null || c.fluid.amount < c.capacity)) {
                                return false;
                            }
                        }
                        return true;
                    }
                    return handler.fill(side, searchedFluid, false) <= 0;
            }
        }

        return false;
    }

    @Override
    public IStatementParameter createParameter(int index) {
        return new StatementParameterItemStack();
    }
}
