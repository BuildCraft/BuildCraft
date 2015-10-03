/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport;

import java.util.Locale;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.transport.PipeWire;

public class WireIconProvider {

    public enum Type {
        RED_DARK(PipeWire.RED, false),
        RED_LIT(PipeWire.RED, true),
        BLUE_DARK(PipeWire.BLUE, false),
        BLUE_LIT(PipeWire.BLUE, true),
        GREEN_DARK(PipeWire.GREEN, false),
        GREEN_LIT(PipeWire.GREEN, true),
        YELLOW_DARK(PipeWire.YELLOW, false),
        YELLOW_LIT(PipeWire.YELLOW, true);

        private final ResourceLocation location;

        Type(PipeWire type, boolean lit) {
            if (lit) {
                litMap.put(type, this);
            } else {
                darkMap.put(type, this);
            }
            location = new ResourceLocation("buildcraftcore:blocks/misc/texture_" + name().toLowerCase(Locale.ENGLISH));
        }
    }

    private static final Map<PipeWire, Type> darkMap = Maps.newEnumMap(PipeWire.class);
    private static final Map<PipeWire, Type> litMap = Maps.newEnumMap(PipeWire.class);
    private static Map<Type, TextureAtlasSprite> icons = Maps.newEnumMap(Type.class);

    public TextureAtlasSprite getIcon(PipeWire wire, boolean lit) {
        return icons.get((lit ? litMap : darkMap).get(wire));
    }

    public static void registerIcons(TextureMap iconRegister) {
        for (Type type : Type.values()) {
            icons.put(type, iconRegister.registerSprite(type.location));
        }
    }

}
