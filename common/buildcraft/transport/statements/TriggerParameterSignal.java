/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.statements;

import buildcraft.api.gates.IGate;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementMouseClick;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.StackUtil;
import buildcraft.transport.BCTransportSprites;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class TriggerParameterSignal implements IStatementParameter {

    public boolean active = false;
    public EnumDyeColor colour = null;

    public TriggerParameterSignal() {}

    public TriggerParameterSignal(boolean active, EnumDyeColor colour) {
        this.active = active;
        this.colour = colour;
    }

    @Nonnull
    @Override
    public ItemStack getItemStack() {
        return StackUtil.EMPTY;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getGuiSprite() {
        if (colour == null) {
            return null;
        }
        return BCTransportSprites.getPipeSignal(active, colour).getSprite();
    }

    @Override
    public boolean onClick(IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse) {
        return false;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setBoolean("active", active);

        if (colour != null) {
            nbt.setByte("color", (byte) colour.getMetadata());
        }

    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        active = nbt.getBoolean("active");

        if (nbt.hasKey("color")) {
            colour = EnumDyeColor.byMetadata(nbt.getByte("color"));
        }
    }

    @Override
    public String getDescription() {
        if (colour == null) {
            return null;
        }
        return String.format(LocaleUtil.localize("gate.trigger.pipe.wire." + (active ? "active" : "inactive")), ColourUtil.getTextFullTooltip(colour));
    }

    @Override
    public String getUniqueTag() {
        return "buildcraft:pipeWireTrigger";
    }

    @Override
    public IStatementParameter rotateLeft() {
        return this;
    }

    @Override
    public TriggerParameterSignal[] getPossible(IStatementContainer source, IStatement stmt) {
        if (!(source instanceof IGate)) {
            return null;
        }
        IGate gate = (IGate) source;
        List<TriggerParameterSignal> poss = new ArrayList<>(ColourUtil.COLOURS.length * 2 + 1);
        poss.add(new TriggerParameterSignal());
        for (EnumDyeColor c : ColourUtil.COLOURS) {
            if (TriggerPipeSignal.doesGateHaveColour(gate, c)) {
                poss.add(new TriggerParameterSignal(true, c));
                poss.add(new TriggerParameterSignal(false, c));
            }
        }
        return poss.toArray(new TriggerParameterSignal[poss.size()]);
    }
}
