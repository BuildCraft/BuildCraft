package ct.buildcraft.robotics.statements;

import ct.buildcraft.api.core.IZone;
import ct.buildcraft.api.items.IMapLocation;
import ct.buildcraft.api.statements.IActionInternal;
import ct.buildcraft.api.statements.IStatement;
import ct.buildcraft.api.statements.IStatementContainer;
import ct.buildcraft.api.statements.IStatementParameter;
import ct.buildcraft.api.statements.StatementSlot;
import ct.buildcraft.core.statements.BCStatement;
import ct.buildcraft.api.core.render.ISprite;
import ct.buildcraft.robotics.BCRoboticsSprites;
import ct.buildcraft.robotics.BCRoboticsStatements;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ActionRobotWorkInArea extends BCStatement implements IActionInternal {

    public enum AreaType { WORK, LOAD_UNLOAD }

    public final boolean loadUnload;

    public ActionRobotWorkInArea(boolean loadUnload) {
        super(loadUnload ? "buildcraft:robot.load_unload_area" : "buildcraft:robot.work_in_area");
        this.loadUnload = loadUnload;
    }

    @Override
    public int maxParameters() { return 1; }
    @Override
    public int minParameters() { return 1; }

    @Override
    public IStatementParameter createParameter(int index) {
        return new StatementParameterMapLocation();
    }

    @Override
    public Component getDescription() {
        return Component.translatable(loadUnload ? "gate.action.robot.load_unload_area" : "gate.action.robot.work_in_area");
    }

    @Override
    public void actionActivate(IStatementContainer container, IStatementParameter[] parameters) {
        // The area constraint is read by the robot AI boards
    }

    public static IMapLocation getMapLocation(IStatementParameter[] parameters) {
        if (parameters == null || parameters.length == 0) return null;
        IStatementParameter p = parameters[0];
        if (!(p instanceof StatementParameterMapLocation spl)) return null;
        ItemStack stack = spl.getItemStack();
        if (stack.isEmpty() || !(stack.getItem() instanceof IMapLocation)) return null;
        return (IMapLocation) stack.getItem();
    }

    public static IZone getArea(StatementSlot slot) {
        if (slot == null || slot.parameters == null || slot.parameters.length == 0) {
            return null;
        }
        IStatementParameter parameter = slot.parameters[0];
        if (!(parameter instanceof StatementParameterMapLocation mapParam)) {
            return null;
        }
        ItemStack stack = mapParam.getItemStack();
        if (stack.isEmpty() || !(stack.getItem() instanceof IMapLocation map)) {
            return null;
        }

        return map.getZone(stack);
    }

    public AreaType getAreaType() {
        return loadUnload ? AreaType.LOAD_UNLOAD : AreaType.WORK;
    }

    @Override
    public IStatement[] getPossible() {
        return new IStatement[] {
            BCRoboticsStatements.ACTION_ROBOT_WORK_IN_AREA,
            BCRoboticsStatements.ACTION_ROBOT_LOAD_UNLOAD_AREA
        };
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ISprite getSprite() {
        return loadUnload ? BCRoboticsSprites.ACTION_ROBOT_LOAD_UNLOAD_AREA : BCRoboticsSprites.ACTION_ROBOT_WORK_IN_AREA;
    }
}
