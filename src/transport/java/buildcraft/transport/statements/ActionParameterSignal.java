/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.statements;

import java.util.Locale;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementMouseClick;
import buildcraft.api.transport.PipeWire;
import buildcraft.core.lib.utils.StringUtils;
import buildcraft.transport.Gate;

public class ActionParameterSignal implements IStatementParameter {

    @SideOnly(Side.CLIENT)
    private static TextureAtlasSprite[] icons;

    public PipeWire color = null;

    public ActionParameterSignal() {

    }

    @Override
    public TextureAtlasSprite getIcon() {
        if (color == null) {
            return null;
        } else {
            return icons[color.ordinal()];
        }
    }

    @Override
    public void onClick(IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse) {
        int maxColor = 4;
        if (source instanceof Gate) {
            maxColor = ((Gate) source).material.maxWireColor;
        }

        if (color == null) {
            color = mouse.getButton() == 0 ? PipeWire.RED : PipeWire.values()[maxColor - 1];
        } else if (color == (mouse.getButton() == 0 ? PipeWire.values()[maxColor - 1] : PipeWire.RED)) {
            color = null;
        } else {
            do {
                color = PipeWire.values()[(mouse.getButton() == 0 ? color.ordinal() + 1 : color.ordinal() - 1) & 3];
            } while (color.ordinal() >= maxColor);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        if (color != null) {
            nbt.setByte("color", (byte) color.ordinal());
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("color")) {
            color = PipeWire.values()[nbt.getByte("color")];
        }
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof ActionParameterSignal) {
            ActionParameterSignal param = (ActionParameterSignal) object;

            return param.color == color;
        } else {
            return false;
        }
    }

    @Override
    public String getDescription() {
        if (color == null) {
            return null;
        }
        return String.format(StringUtils.localize("gate.action.pipe.wire"), StringUtils.localize("color." + color.name().toLowerCase(
                Locale.ENGLISH)));
    }

    @Override
    public String getUniqueTag() {
        return "buildcraft:pipeWireAction";
    }

    public static void registerIcons(TextureStitchEvent.Pre event) {
        icons = new TextureAtlasSprite[4];
        icons[0] = event.map.registerSprite(new ResourceLocation("buildcrafttransport:triggers/trigger_pipesignal_red_active"));
        icons[1] = event.map.registerSprite(new ResourceLocation("buildcrafttransport:triggers/trigger_pipesignal_blue_active"));
        icons[2] = event.map.registerSprite(new ResourceLocation("buildcrafttransport:triggers/trigger_pipesignal_green_active"));
        icons[3] = event.map.registerSprite(new ResourceLocation("buildcrafttransport:triggers/trigger_pipesignal_yellow_active"));
    }

    @Override
    public IStatementParameter rotateLeft() {
        return this;
    }

    @Override
    public ItemStack getItemStack() {
        return null;
    }
}
