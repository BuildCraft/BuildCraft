package buildcraft.core.builders.patterns;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementMouseClick;

import buildcraft.lib.BCLibSprites;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.StackUtil;

public enum PatternParameterAxis implements IStatementParameter {
    X(Axis.X),
    Y(Axis.Y),
    Z(Axis.Z);

    public final EnumFacing.Axis axis;

    private PatternParameterAxis(Axis axis) {
        this.axis = axis;
    }

    public static PatternParameterAxis readFromNbt(NBTTagCompound nbt) {
        byte ord = nbt.getByte("a");
        if (ord <= 0) {
            return X;
        }
        if (ord >= 2) {
            return Z;
        }
        return Y;
    }

    @Override
    public String getUniqueTag() {
        return "buildcraft:filler_parameter_axis";
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("filler_axis_this_is_temp_" + name());
    }

    @Override
    public ISprite getSprite() {
        switch (this) {
            case X:
                return BCLibSprites.ENGINE_INACTIVE;
            case Y:
                return BCLibSprites.ENGINE_ACTIVE;
            case Z:
                return BCLibSprites.ENGINE_WARM;
            default:
                throw new IllegalStateException("Unknown axis " + this);
        }
    }

    @Override
    public ItemStack getItemStack() {
        return StackUtil.EMPTY;
    }

    @Override
    public IStatementParameter onClick(IStatementContainer source, IStatement stmt, ItemStack stack,
        StatementMouseClick mouse) {
        return null;
    }

    @Override
    public void writeToNbt(NBTTagCompound nbt) {
        nbt.setByte("a", (byte) ordinal());
    }

    @Override
    public IStatementParameter rotateLeft() {
        switch (this) {
            case X:
                return Z;
            case Y:
                return Y;
            case Z:
                return X;
            default:
                throw new IllegalStateException("Unknown axis " + this);
        }
    }

    @Override
    public IStatementParameter[] getPossible(IStatementContainer source) {
        return values();
    }
}
