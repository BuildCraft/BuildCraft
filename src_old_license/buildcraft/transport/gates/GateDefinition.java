/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.gates;

import java.util.Locale;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.core.lib.client.sprite.SpriteBuilder;
import buildcraft.core.lib.utils.BCStringUtils;

@Deprecated
public final class GateDefinition {

    private GateDefinition() {}

    public static String getLocalizedName(GateMaterial material, GateLogic logic) {
        if (material == GateMaterial.REDSTONE) {
            return BCStringUtils.localize("gate.name.basic");
        } else {
            return String.format(BCStringUtils.localize("gate.name"), BCStringUtils.localize("gate.material." + material.getTag()), BCStringUtils
                    .localize("gate.logic." + logic.getTag()));
        }
    }

    public enum GateMaterial {

        REDSTONE("gate_interface_1.png", 146, 1, 0, 0, 1),
        IRON("gate_interface_2.png", 164, 2, 0, 0, 2),
        GOLD("gate_interface_3.png", 200, 4, 1, 0, 3),
        DIAMOND("gate_interface_4.png", 200, 8, 1, 0, 4),
//        EMERALD("gate_interface_5.png", 200, 4, 3, 3, 4),
        QUARTZ("gate_interface_6.png", 164, 2, 1, 1, 3);

        public static final GateMaterial[] VALUES = values();
        public final ResourceLocation guiFile;
        public final int guiHeight;
        public final int numSlots;
        public final int numTriggerParameters;
        public final int numActionParameters;
        public final int maxWireColor;
        @SideOnly(Side.CLIENT)
        private TextureAtlasSprite iconBlock;
        @SideOnly(Side.CLIENT)
        private TextureAtlasSprite iconItem;

        GateMaterial(String guiFile, int guiHeight, int numSlots, int triggerParameterSlots, int actionParameterSlots, int maxWireColor) {
            this.guiFile = new ResourceLocation("buildcrafttransport:textures/gui/" + guiFile);
            this.guiHeight = guiHeight;
            this.numSlots = numSlots;
            this.numTriggerParameters = triggerParameterSlots;
            this.numActionParameters = actionParameterSlots;
            this.maxWireColor = maxWireColor;
        }

        @SideOnly(Side.CLIENT)
        public TextureAtlasSprite getIconBlock() {
            return iconBlock;
        }

        @SideOnly(Side.CLIENT)
        public TextureAtlasSprite getIconItem() {
            return iconItem;
        }

        public String getTag() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        @SideOnly(Side.CLIENT)
        public void registerBlockIcon(TextureMap iconRegister) {
            if (this != REDSTONE) {
                iconBlock = iconRegister.registerSprite(new ResourceLocation("buildcrafttransport:gates/gate_material_" + getTag()));
            }
        }

        @SideOnly(Side.CLIENT)
        public void registerItemIcon(TextureMap iconRegister) {
            if (this != REDSTONE) {
                iconItem = iconRegister.registerSprite(new ResourceLocation("buildcrafttransport:gates/gate_material_" + getTag()));
            }
        }

        public static GateMaterial fromOrdinal(int ordinal) {
            if (ordinal < 0 || ordinal >= VALUES.length) {
                return REDSTONE;
            }
            return VALUES[ordinal];
        }
    }

    public enum GateLogic {

        AND,
        OR;
        public static final GateLogic[] VALUES = values();

        @SideOnly(Side.CLIENT)
        private TextureAtlasSprite iconLit;

        @SideOnly(Side.CLIENT)
        private TextureAtlasSprite iconDark;

        @SideOnly(Side.CLIENT)
        private TextureAtlasSprite iconItem;

        @SideOnly(Side.CLIENT)
        private TextureAtlasSprite iconGate;

        @SideOnly(Side.CLIENT)
        public TextureAtlasSprite getIconLit() {
            return iconLit;
        }

        @SideOnly(Side.CLIENT)
        public TextureAtlasSprite getIconDark() {
            return iconDark;
        }

        @SideOnly(Side.CLIENT)
        public TextureAtlasSprite getGateIcon() {
            return iconGate;
        }

        @SideOnly(Side.CLIENT)
        public TextureAtlasSprite getIconItem() {
            return iconItem;
        }

        public String getTag() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        @SideOnly(Side.CLIENT)
        public void registerBlockIcon(TextureMap iconRegister) {
            String lit = "buildcrafttransport:gates/gate_" + getTag() + "_lit";
            String dark = "buildcrafttransport:gates/gate_" + getTag() + "_dark";
            String base = "buildcrafttransport:gates/gate_" + getTag();

            iconLit = new SpriteBuilder(lit + "_generated").addSprite(base, 0xFE).addSprite(lit, 0xFE).build();
            iconDark = new SpriteBuilder(dark + "_generated").addSprite(base, 0xFE).addSprite(dark, 0xFE).build();
            iconRegister.setTextureEntry(lit + "_generated", iconLit);
            iconRegister.setTextureEntry(dark + "_generated", iconDark);

            // iconLit = iconRegister.registerSprite(new ResourceLocation("buildcrafttransport:gates/gate_" + getTag() +
            // "_lit"));
            // iconDark = iconRegister.registerSprite(new ResourceLocation("buildcrafttransport:gates/gate_" + getTag()
            // + "_dark"));

            iconGate = iconRegister.registerSprite(new ResourceLocation("buildcrafttransport:gates/gate_" + getTag()));
        }

        @SideOnly(Side.CLIENT)
        public void registerItemIcon(TextureMap iconRegister) {
            iconItem = iconRegister.registerSprite(new ResourceLocation("buildcrafttransport:gates/gate_logic_" + getTag()));
        }

        public static GateLogic fromOrdinal(int ordinal) {
            if (ordinal < 0 || ordinal >= VALUES.length) {
                return AND;
            }
            return VALUES[ordinal];
        }
    }
}
