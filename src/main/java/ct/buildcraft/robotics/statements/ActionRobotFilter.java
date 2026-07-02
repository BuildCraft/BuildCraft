package ct.buildcraft.robotics.statements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import ct.buildcraft.api.core.IFluidFilter;
import ct.buildcraft.api.core.IStackFilter;
import ct.buildcraft.api.robots.DockingStation;
import ct.buildcraft.api.robots.EntityRobotBase;
import ct.buildcraft.api.statements.IActionInternal;
import ct.buildcraft.api.statements.IStatement;
import ct.buildcraft.api.statements.IStatementContainer;
import ct.buildcraft.api.statements.IStatementParameter;
import ct.buildcraft.api.statements.StatementParameterItemStack;
import ct.buildcraft.api.statements.StatementSlot;
import ct.buildcraft.core.statements.BCStatement;
import ct.buildcraft.lib.inventory.filter.ArrayFluidFilter;
import ct.buildcraft.lib.inventory.filter.ArrayStackOrListFilter;
import ct.buildcraft.lib.inventory.filter.PassThroughFluidFilter;
import ct.buildcraft.lib.inventory.filter.PassThroughStackFilter;
import ct.buildcraft.api.core.render.ISprite;
import ct.buildcraft.robotics.BCRoboticsSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ActionRobotFilter extends BCStatement implements IActionInternal {

    public ActionRobotFilter() {
        super("buildcraft:robot.work_filter");
    }

    @Override
    public int maxParameters() { return 3; }
    @Override
    public int minParameters() { return 1; }

    @Override
    public IStatementParameter createParameter(int index) {
        return StatementParameterItemStack.EMPTY;
    }

    @Override
    public Component getDescription() {
        return Component.translatable("gate.action.robot.filter");
    }

    @Override
    public void actionActivate(IStatementContainer container, IStatementParameter[] parameters) {
        // Filter is used passively by the robot AI, not actively
    }

    /** Check if any parameter slot matches the given stack. */
    public static boolean matches(IStatementParameter[] parameters, ItemStack stack) {
        if (parameters == null) return true;
        boolean hasFilter = false;
        for (IStatementParameter p : parameters) {
            if (p == null) continue;
            ItemStack filterStack = p.getItemStack();
            if (!filterStack.isEmpty()) {
                hasFilter = true;
                if (ItemStack.isSameItemSameTags(filterStack, stack)) return true;
            }
        }
        return !hasFilter;
    }

    public static Collection<ItemStack> getGateFilterStacks(DockingStation station) {
        Collection<ItemStack> result = new ArrayList<>();
        if (station == null) {
            return result;
        }

        for (StatementSlot slot : station.getActiveActions()) {
            if (slot.statement instanceof ActionRobotFilter && slot.parameters != null) {
                for (IStatementParameter parameter : slot.parameters) {
                    if (parameter == null) continue;
                    ItemStack stack = parameter.getItemStack();
                    if (!stack.isEmpty()) {
                        result.add(stack.copy());
                    }
                }
            }
        }
        return result;
    }

    public static IStackFilter getGateFilter(DockingStation station) {
        Collection<ItemStack> stacks = getGateFilterStacks(station);
        return stacks.isEmpty()
                ? new PassThroughStackFilter()
                : new ArrayStackOrListFilter(stacks.toArray(new ItemStack[0]));
    }

    public static Collection<FluidStack> getGateFilterFluids(DockingStation station) {
        List<FluidStack> result = new ArrayList<>();
        if (station == null) {
            return result;
        }

        for (StatementSlot slot : station.getActiveActions()) {
            if (slot.statement instanceof ActionRobotFilter && slot.parameters != null) {
                for (IStatementParameter parameter : slot.parameters) {
                    if (parameter == null) continue;
                    Optional<FluidStack> contained = FluidUtil.getFluidContained(parameter.getItemStack());
                    contained.ifPresent(fluid -> {
                        if (!fluid.isEmpty()) {
                            result.add(fluid.copy());
                        }
                    });
                }
            }
        }
        return result;
    }

    public static IFluidFilter getGateFluidFilter(DockingStation station) {
        Collection<FluidStack> fluids = getGateFilterFluids(station);
        return fluids.isEmpty()
                ? new PassThroughFluidFilter()
                : new ArrayFluidFilter(fluids.toArray(new FluidStack[0]));
    }

    public static boolean canInteractWithFluid(DockingStation station, IFluidFilter filter, Class<?> actionClass) {
        if (station == null) {
            return false;
        }

        for (StatementSlot slot : station.getActiveActions()) {
            IStatement statement = slot.statement;
            if (statement != null && actionClass.isAssignableFrom(statement.getClass()) && parametersAllowFluid(slot.parameters, filter)) {
                return true;
            }
        }
        return false;
    }

    public static boolean canInteractWithItem(DockingStation station, IStackFilter filter, Class<?> actionClass) {
        if (station == null) {
            return false;
        }

        for (StatementSlot slot : station.getActiveActions()) {
            IStatement statement = slot.statement;
            if (statement != null && actionClass.isAssignableFrom(statement.getClass()) && parametersAllow(slot.parameters, filter)) {
                return true;
            }
        }
        return false;
    }

    private static boolean parametersAllow(IStatementParameter[] parameters, IStackFilter filter) {
        if (parameters == null || parameters.length == 0) {
            return true;
        }

        boolean hasFilter = false;
        for (IStatementParameter parameter : parameters) {
            if (parameter == null) continue;
            ItemStack stack = parameter.getItemStack();
            if (!stack.isEmpty()) {
                hasFilter = true;
                if (filter == null || filter.matches(stack)) {
                    return true;
                }
            }
        }
        return !hasFilter;
    }

    private static boolean parametersAllowFluid(IStatementParameter[] parameters, IFluidFilter filter) {
        if (parameters == null || parameters.length == 0) {
            return true;
        }

        boolean hasFilter = false;
        for (IStatementParameter parameter : parameters) {
            if (parameter == null) continue;
            Optional<FluidStack> contained = FluidUtil.getFluidContained(parameter.getItemStack());
            if (contained.isPresent() && !contained.get().isEmpty()) {
                hasFilter = true;
                if (filter == null || filter.matches(contained.get())) {
                    return true;
                }
            }
        }
        return !hasFilter;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ISprite getSprite() {
        return BCRoboticsSprites.ACTION_ROBOT_FILTER;
    }
}
