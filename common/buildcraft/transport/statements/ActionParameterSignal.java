/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.statements;

import java.util.Locale;
import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
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
import buildcraft.core.lib.utils.BCStringUtils;
import buildcraft.transport.Gate;

public class ActionParameterSignal implements IStatementParameter {

    @SideOnly(Side.CLIENT)
    private static TextureAtlasSprite[] icons;

    @Nullable
    private PipeWire color = null;

    public ActionParameterSignal() {

    }

    @Nullable
    public PipeWire getColor() {
        return color;
    }

    @Override
    public TextureAtlasSprite getIcon() {
        PipeWire colour = getColor();
        if (colour == null) {
            return null;
        } else {
            return icons[colour.ordinal()];
        }
    }

    @Override
    public void onClick(IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse) {
        int maxColor = 4;
        if (source instanceof Gate) {
            maxColor = ((Gate) source).material.maxWireColor;
        }
        PipeWire colour = getColor();

        if (colour == null) {
            colour = mouse.getButton() == 0 ? PipeWire.RED : PipeWire.values()[maxColor - 1];
        } else if (colour == (mouse.getButton() == 0 ? PipeWire.values()[maxColor - 1] : PipeWire.RED)) {
            colour = null;
        } else {
            do {
                colour = PipeWire.values()[(mouse.getButton() == 0 ? colour.ordinal() + 1 : colour.ordinal() - 1) & 3];
            } while (colour.ordinal() >= maxColor);
        }
        this.color = colour;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        PipeWire colour = getColor();
        if (colour != null) {
            nbt.setByte("color", (byte) colour.ordinal());
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("color")) {
            this.color = PipeWire.values()[nbt.getByte("color")];
        }
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
        PipeWire colour = getColor();
        if (colour == null) {
            return null;
        }
        String format = BCStringUtils.localize("gate.action.pipe.wire");
        Object[] args = { BCStringUtils.localize("color." + colour.name().toLowerCase(Locale.ENGLISH)) };
        return String.format(format, args);
    }

    @Override
    public String getUniqueTag() {
        return "buildcraft:pipeWireAction";
    }

    public static void registerIcons(TextureStitchEvent.Pre event) {
        icons = new TextureAtlasSprite[4];
        icons[0] = getOrRegister(event.map, "buildcrafttransport:triggers/trigger_pipesignal_red_active");
        icons[1] = getOrRegister(event.map, "buildcrafttransport:triggers/trigger_pipesignal_blue_active");
        icons[2] = getOrRegister(event.map, "buildcrafttransport:triggers/trigger_pipesignal_green_active");
        icons[3] = getOrRegister(event.map, "buildcrafttransport:triggers/trigger_pipesignal_yellow_active");
    }

    private static TextureAtlasSprite getOrRegister(TextureMap map, String location) {
        TextureAtlasSprite sprite = map.getTextureExtry(location);
        if (sprite == null) sprite = map.registerSprite(new ResourceLocation(location));
        return sprite;
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
