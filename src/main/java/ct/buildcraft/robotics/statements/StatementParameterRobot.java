package ct.buildcraft.robotics.statements;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import ct.buildcraft.api.robots.EntityRobotBase;
import ct.buildcraft.api.statements.IStatementParameter;
import ct.buildcraft.api.statements.StatementParameterItemStack;

public class StatementParameterRobot extends StatementParameterItemStack {

    public static final String TAG = "buildcraft:robot";

    public StatementParameterRobot() { super(); }
    public StatementParameterRobot(ItemStack stack) { super(stack); }
    public StatementParameterRobot(CompoundTag nbt) { super(nbt); }

    @Override
    public String getUniqueTag() { return TAG; }

    @Override
    public StatementParameterRobot onClick(
            ct.buildcraft.api.statements.IStatementContainer source,
            ct.buildcraft.api.statements.IStatement stmt,
            ItemStack clicked,
            ct.buildcraft.api.statements.StatementMouseClick mouse) {
        if (clicked.isEmpty()) return new StatementParameterRobot();
        // Accept any robot item (check via capability or item type is flexible)
        ItemStack copy = clicked.copy();
        copy.setCount(1);
        return new StatementParameterRobot(copy);
    }

    public static boolean matches(@Nullable IStatementParameter param, EntityRobotBase robot) {
        if (!(param instanceof StatementParameterRobot r)) return true;
        ItemStack held = r.getItemStack();
        if (held.isEmpty()) return true;
        // Simple match: if a filter is set, check if the robot has a board with the matching item
        return true; // TODO: match by robot board type
    }

    public static StatementParameterRobot readFromNbt(CompoundTag nbt) {
        return new StatementParameterRobot(nbt);
    }
}
