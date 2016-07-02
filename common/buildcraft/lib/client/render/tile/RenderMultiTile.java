package buildcraft.lib.client.render.tile;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.client.model.animation.FastTESR;

/** Designates a tile entity that is made up of multiple {@link ITileRenderPart}. Although it might also have other
 * components. */
public abstract class RenderMultiTile<T extends TileEntity> extends FastTESR<T> {
    protected final List<ITileRenderPart<? super T>> parts = new ArrayList<>();

    @Override
    public void renderTileEntityFast(T tile, double x, double y, double z, float partialTicks, int destroyStage, VertexBuffer buffer) {
        buffer.setTranslation(x, y, z);
        for (ITileRenderPart<? super T> part : parts) {
            part.render(tile, buffer);
        }
        buffer.setTranslation(0, 0, 0);
    }
}
