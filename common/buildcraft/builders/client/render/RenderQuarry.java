package buildcraft.builders.client.render;

import buildcraft.builders.tile.TileQuarry;
import buildcraft.lib.client.render.laser.LaserData_BC8;
import buildcraft.lib.client.render.laser.LaserRenderer_BC8;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.misc.CullTESR;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class RenderQuarry extends CullTESR<TileQuarry> {
    public static final LaserData_BC8.LaserType FRAME;

    static {
        SpriteHolderRegistry.SpriteHolder sprite = SpriteHolderRegistry.getHolder("buildcraftbuilders:blocks/frame/default");
        LaserData_BC8.LaserRow capStart = new LaserData_BC8.LaserRow(sprite, 0, 0, 0, 0);
        LaserData_BC8.LaserRow start = null;
        LaserData_BC8.LaserRow[] middle = {
                new LaserData_BC8.LaserRow(sprite, 0, 4, 16, 12)
        };
        LaserData_BC8.LaserRow end = null;
        LaserData_BC8.LaserRow capEnd = new LaserData_BC8.LaserRow(sprite, 0, 0, 0, 0);
        FRAME = new LaserData_BC8.LaserType(capStart, start, middle, end, capEnd);
    }

    public RenderQuarry() {}

    @Override
    public void renderTileEntityFast(TileQuarry tile, double x, double y, double z, float partialTicks, int destroyStage, VertexBuffer buffer) {
        if(tile.min != null && tile.max != null && tile.drillPos != null) {
            buffer.setTranslation(x - tile.getPos().getX(), y - tile.getPos().getY(), z - tile.getPos().getZ());

//            LaserRenderer_BC8.renderLaserBuffer(new LaserData_BC8(FRAME, new Vec3d(tile.getPos().getX() + 1, tile.getPos().getY() + 0.5, tile.getPos().getZ() + 0.5), new Vec3d(tile.getPos().getX() + 2, tile.getPos().getY() + 0.5, tile.getPos().getZ() + 0.5), 1 / 16D), buffer);
            LaserRenderer_BC8.renderLaserBuffer(new LaserData_BC8(FRAME, new Vec3d(tile.drillPos.xCoord + 0.5, tile.min.getY() + 0.5, tile.drillPos.zCoord), new Vec3d(tile.drillPos.xCoord + 0.5, tile.min.getY() + 0.5, tile.max.getZ()), 1 / 16D), buffer);
            LaserRenderer_BC8.renderLaserBuffer(new LaserData_BC8(FRAME, new Vec3d(tile.drillPos.xCoord + 0.5, tile.min.getY() + 0.5, tile.drillPos.zCoord), new Vec3d(tile.drillPos.xCoord + 0.5, tile.min.getY() + 0.5, tile.min.getZ() + 1), 1 / 16D), buffer);
            LaserRenderer_BC8.renderLaserBuffer(new LaserData_BC8(FRAME, new Vec3d(tile.drillPos.xCoord, tile.min.getY() + 0.5, tile.drillPos.zCoord + 0.5), new Vec3d(tile.max.getX(), tile.min.getY() + 0.5, tile.drillPos.zCoord + 0.5), 1 / 16D), buffer);
            LaserRenderer_BC8.renderLaserBuffer(new LaserData_BC8(FRAME, new Vec3d(tile.drillPos.xCoord, tile.min.getY() + 0.5, tile.drillPos.zCoord + 0.5), new Vec3d(tile.min.getX() + 1, tile.min.getY() + 0.5, tile.drillPos.zCoord + 0.5), 1 / 16D), buffer);

            buffer.setTranslation(0, 0, 0);
        }
    }
}
