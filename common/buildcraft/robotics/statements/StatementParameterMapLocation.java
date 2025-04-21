package buildcraft.robotics.statements;

import buildcraft.api.items.IMapLocation;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.StatementMouseClick;
import buildcraft.api.statements.StatementParameterItemStack;
import buildcraft.lib.misc.StackUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class StatementParameterMapLocation extends StatementParameterItemStack {
    // Calen 1.18.2
    public static final StatementParameterMapLocation EMPTY;

    static {
        EMPTY = new StatementParameterMapLocation();
    }

    public StatementParameterMapLocation() {
        super();
    }

    public StatementParameterMapLocation(ItemStack itemStack) {
        super(itemStack);
    }

    public StatementParameterMapLocation(CompoundTag nbt) {
        super(nbt);
    }

    @Override
    public String getUniqueTag() {
        return "buildcraft:maplocation";
    }

    @Override
    // public void onClick(IStatementContainer source, IStatement stmt, ItemStack stackIn, StatementMouseClick mouse)
    public StatementParameterMapLocation onClick(IStatementContainer source, IStatement stmt, @Nonnull ItemStack stackIn, StatementMouseClick mouse) {
        ItemStack stack = stackIn;
        // if (stack != null && !(stack.getItem() instanceof IMapLocation))
        if (!stack.isEmpty() && !(stack.getItem() instanceof IMapLocation)) {
            // stack = null;
            stack = StackUtil.EMPTY;
        }
        // return super.onClick(source, stmt, stack, mouse);
        if (stack.isEmpty()) {
            return EMPTY;
        } else {
            ItemStack newStack = stack.copy();
            newStack.setCount(1);
            return new StatementParameterMapLocation(newStack);
        }
    }
}
