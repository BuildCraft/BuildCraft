/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

import buildcraft.builders.render.RenderArchitect;
import buildcraft.builders.render.RenderConstructionMarker;
import buildcraft.builders.render.RenderPathMarker;
import buildcraft.builders.tile.TileArchitect;
import buildcraft.builders.tile.TileBuilder;
import buildcraft.builders.tile.TileConstructionMarker;
import buildcraft.builders.tile.TileFiller;
import buildcraft.builders.tile.TilePathMarker;
import buildcraft.builders.tile.TileQuarry;
import buildcraft.builders.urbanism.TileUrbanist;
import buildcraft.core.lib.EntityResizableCube;
import buildcraft.core.lib.render.RenderVoid;
import buildcraft.core.render.RenderBoxProvider;
import buildcraft.core.render.RenderBuilder;

public class BuilderProxyClient extends BuilderProxy {
    public static TextureAtlasSprite drillTexture;
    public static TextureAtlasSprite drillHeadTexture;

    @Override
    public void registerClientHook() {

    }

    @Override
    public void registerBlockRenderers() {
        super.registerBlockRenderers();

        ClientRegistry.bindTileEntitySpecialRenderer(TileQuarry.class, new RenderBuilder());
        ClientRegistry.bindTileEntitySpecialRenderer(TileUrbanist.class, new RenderBoxProvider());
        ClientRegistry.bindTileEntitySpecialRenderer(TileArchitect.class, new RenderArchitect());
        ClientRegistry.bindTileEntitySpecialRenderer(TileFiller.class, new RenderBuilder());
        ClientRegistry.bindTileEntitySpecialRenderer(TileBuilder.class, new RenderBuilder());
        ClientRegistry.bindTileEntitySpecialRenderer(TilePathMarker.class, new RenderPathMarker());
        ClientRegistry.bindTileEntitySpecialRenderer(TileConstructionMarker.class, new RenderConstructionMarker());

        RenderingRegistry.registerEntityRenderingHandler(EntityMechanicalArm.class, new RenderVoid());
    }

    @Override
    public EntityResizableCube newDrill(World w, double i, double j, double k, double l, double d, double e) {
        EntityResizableCube eb = super.newDrill(w, i, j, k, l, d, e);
        eb.texture = drillTexture;
        return eb;
    }

    @Override
    public EntityResizableCube newDrillHead(World w, double i, double j, double k, double l, double d, double e) {
        EntityResizableCube eb = super.newDrillHead(w, i, j, k, l, d, e);
        eb.texture = drillHeadTexture;
        return eb;
    }
}
