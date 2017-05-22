/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.statements;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.common.util.Constants;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.gates.IGate;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementMouseClick;

import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.StackUtil;

import buildcraft.transport.BCTransportSprites;

public class ActionParameterSignal implements IStatementParameter {

    public static final ActionParameterSignal EMPTY = new ActionParameterSignal(null);
    private static final Map<EnumDyeColor, ActionParameterSignal> SIGNALS;

    static {
        SIGNALS = new EnumMap<>(EnumDyeColor.class);
        for (EnumDyeColor colour : ColourUtil.COLOURS) {
            SIGNALS.put(colour, new ActionParameterSignal(colour));
        }
    }

    @Nullable
    public final EnumDyeColor colour;

    private ActionParameterSignal(EnumDyeColor colour) {
        this.colour = colour;
    }

    public static ActionParameterSignal get(EnumDyeColor colour) {
        return colour == null ? EMPTY : SIGNALS.get(colour);
    }

    public static ActionParameterSignal readFromNbt(NBTTagCompound nbt) {
        if (nbt.hasKey("color", Constants.NBT.TAG_ANY_NUMERIC)) {
            return get(EnumDyeColor.byMetadata(nbt.getByte("color")));
        }
        return EMPTY;
    }

    @Override
    public void writeToNbt(NBTTagCompound nbt) {
        EnumDyeColor c = colour;
        if (c != null) {
            nbt.setByte("color", (byte) c.getMetadata());
        }
    }

    @Nullable
    public EnumDyeColor getColor() {
        return colour;
    }

    @Override
    public ISprite getGuiSprite() {
        EnumDyeColor c = colour;
        if (c == null) {
            return null;
        } else {
            return BCTransportSprites.getPipeSignal(true, c);
        }
    }

    @Override
    public ActionParameterSignal onClick(IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse) {
        return null;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof ActionParameterSignal) {
            ActionParameterSignal param = (ActionParameterSignal) object;

            return param.getColor() == getColor();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(getColor());
    }

    @Override
    public String getDescription() {
        EnumDyeColor c = colour;
        if (c == null) {
            return null;
        }
        String format = LocaleUtil.localize("gate.action.pipe.wire");
        Object[] args = { ColourUtil.getTextFullTooltip(c) };
        return String.format(format, args);
    }

    @Override
    public String getUniqueTag() {
        return "buildcraft:pipeWireAction";
    }

    @Override
    public IStatementParameter rotateLeft() {
        return this;
    }

    @Override
    public ItemStack getItemStack() {
        return StackUtil.EMPTY;
    }

    @Override
    public IStatementParameter[] getPossible(IStatementContainer source, IStatement stmt) {
        if (!(source instanceof IGate)) {
            return null;
        }
        IGate gate = (IGate) source;
        List<IStatementParameter> poss = new ArrayList<>(1 + ColourUtil.COLOURS.length);
        poss.add(EMPTY);
        for (EnumDyeColor c : ColourUtil.COLOURS) {
            if (TriggerPipeSignal.doesGateHaveColour(gate, c)) {
                poss.add(get(c));
            }
        }
        return poss.toArray(new IStatementParameter[poss.size()]);
    }
}
