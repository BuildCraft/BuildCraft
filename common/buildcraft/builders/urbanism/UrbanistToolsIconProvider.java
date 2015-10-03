/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.urbanism;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

// TODO (PASS 1): Register Urbanist textures
public final class UrbanistToolsIconProvider {

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

    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getIcon(int iconIndex) {
        return icons[iconIndex];
    }

    @SideOnly(Side.CLIENT)
    public void registerSprites(TextureMap terrainTextures) {
        icons[UrbanistToolsIconProvider.Tool_Block_Place] = terrainTextures.registerSprite(new ResourceLocation(
                "buildcraftbuilders:icons/urbanist_block"));
        icons[UrbanistToolsIconProvider.Tool_Block_Erase] = terrainTextures.registerSprite(new ResourceLocation(
                "buildcraftbuilders:icons/urbanist_erase"));
        icons[UrbanistToolsIconProvider.Tool_Area] = terrainTextures.registerSprite(new ResourceLocation("buildcraftbuilders:icons/urbanist_area"));
        icons[UrbanistToolsIconProvider.Tool_Path] = terrainTextures.registerSprite(new ResourceLocation("buildcraftbuilders:icons/urbanist_path"));
        icons[UrbanistToolsIconProvider.Tool_Filler] = terrainTextures.registerSprite(new ResourceLocation(
                "buildcraftbuilders:icons/urbanist_filler"));
        icons[UrbanistToolsIconProvider.Tool_Blueprint] = terrainTextures.registerSprite(new ResourceLocation(
                "buildcraftbuilders:icons/urbanist_blueprint"));
    }
}
