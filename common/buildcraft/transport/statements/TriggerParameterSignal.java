/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.statements;

import java.util.Locale;

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

public class TriggerParameterSignal implements IStatementParameter {

    @SideOnly(Side.CLIENT)
    private static TextureAtlasSprite[] icons;

    public boolean active = false;
    public PipeWire color = null;

    public TriggerParameterSignal() {

    }

    @Override
    public ItemStack getItemStack() {
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getIcon() {
        if (color == null) {
            return null;
        }

        return icons[color.ordinal() + (active ? 4 : 0)];
    }

    @Override
    public void onClick(IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse) {
        int maxColor = 4;
        if (source instanceof Gate) {
            maxColor = ((Gate) source).material.maxWireColor;
        }

        if (mouse.getButton() == 0) {
            if (color == null) {
                active = true;
                color = PipeWire.RED;
            } else if (active) {
                active = false;
            } else if (color == PipeWire.values()[maxColor - 1]) {
                color = null;
            } else {
                do {
                    color = PipeWire.values()[(color.ordinal() + 1) & 3];
                } while (color.ordinal() >= maxColor);
                active = true;
            }
        } else {
            if (color == null) {
                active = false;
                color = PipeWire.values()[maxColor - 1];
            } else if (!active) {
                active = true;
            } else if (color == PipeWire.RED) {
                color = null;
            } else {
                do {
                    color = PipeWire.values()[(color.ordinal() - 1) & 3];
                } while (color.ordinal() >= maxColor);
                active = false;
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setBoolean("active", active);

        if (color != null) {
            nbt.setByte("color", (byte) color.ordinal());
        }

    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        active = nbt.getBoolean("active");

        if (nbt.hasKey("color")) {
            color = PipeWire.values()[nbt.getByte("color")];
        }
    }

    @Override
    public String getDescription() {
        if (color == null) {
            return null;
        }
        return String.format(BCStringUtils.localize("gate.trigger.pipe.wire." + (active ? "active" : "inactive")), BCStringUtils.localize("color."
            + color.name().toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public String getUniqueTag() {
        return "buildcraft:pipeWireTrigger";
    }

    @SideOnly(Side.CLIENT)
    public static void registerIcons(TextureStitchEvent.Pre event) {
        icons = new TextureAtlasSprite[8];
        TextureMap map = event.map;
        icons[0] = getOrRegister(map, "buildcrafttransport:triggers/trigger_pipesignal_red_inactive");
        icons[1] = getOrRegister(map, "buildcrafttransport:triggers/trigger_pipesignal_blue_inactive");
        icons[2] = getOrRegister(map, "buildcrafttransport:triggers/trigger_pipesignal_green_inactive");
        icons[3] = getOrRegister(map, "buildcrafttransport:triggers/trigger_pipesignal_yellow_inactive");
        icons[4] = getOrRegister(map, "buildcrafttransport:triggers/trigger_pipesignal_red_active");
        icons[5] = getOrRegister(map, "buildcrafttransport:triggers/trigger_pipesignal_blue_active");
        icons[6] = getOrRegister(map, "buildcrafttransport:triggers/trigger_pipesignal_green_active");
        icons[7] = getOrRegister(map, "buildcrafttransport:triggers/trigger_pipesignal_yellow_active");
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
}
