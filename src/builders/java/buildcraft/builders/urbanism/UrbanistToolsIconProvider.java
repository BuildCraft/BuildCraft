/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.urbanism;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class UrbanistToolsIconProvider implements TextureAtlasSpriteProvider {

    public static UrbanistToolsIconProvider INSTANCE = new UrbanistToolsIconProvider();
    public static final int Tool_Block_Place = 0;
    public static final int Tool_Block_Erase = 1;
    public static final int Tool_Area = 2;
    public static final int Tool_Path = 3;
    public static final int Tool_Filler = 4;
    public static final int Tool_Blueprint = 5;

    public static final int MAX = 6;
    @SideOnly(Side.CLIENT)
    private final TextureAtlasSprite[] icons = new TextureAtlasSprite[MAX];

    private UrbanistToolsIconProvider() {}

    @Override
    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getIcon(int iconIndex) {
        return icons[iconIndex];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(TextureAtlasSpriteRegister iconRegister) {
        icons[UrbanistToolsIconProvider.Tool_Block_Place] = iconRegister.registerIcon("buildcraftbuilders:icons/urbanist_block");
        icons[UrbanistToolsIconProvider.Tool_Block_Erase] = iconRegister.registerIcon("buildcraftbuilders:icons/urbanist_erase");
        icons[UrbanistToolsIconProvider.Tool_Area] = iconRegister.registerIcon("buildcraftbuilders:icons/urbanist_area");
        icons[UrbanistToolsIconProvider.Tool_Path] = iconRegister.registerIcon("buildcraftbuilders:icons/urbanist_path");
        icons[UrbanistToolsIconProvider.Tool_Filler] = iconRegister.registerIcon("buildcraftbuilders:icons/urbanist_filler");
        icons[UrbanistToolsIconProvider.Tool_Blueprint] = iconRegister.registerIcon("buildcraftbuilders:icons/urbanist_blueprint");
    }
}
