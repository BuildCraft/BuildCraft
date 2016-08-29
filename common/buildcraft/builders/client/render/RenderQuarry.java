package buildcraft.builders.client.render;

import buildcraft.builders.tile.TileQuarry;
import buildcraft.lib.client.render.laser.LaserData_BC8;
import buildcraft.lib.client.render.laser.LaserRenderer_BC8;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.misc.CullTESR;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.util.math.Vec3d;

public class RenderQuarry extends CullTESR<TileQuarry> {
    public static final LaserData_BC8.LaserType FRAME;
    public static final LaserData_BC8.LaserType FRAME_BOTTOM;
    public static final LaserData_BC8.LaserType DRILL;

    static {
        {
            SpriteHolderRegistry.SpriteHolder sprite = SpriteHolderRegistry.getHolder("buildcraftbuilders:blocks/frame/default");
            LaserData_BC8.LaserRow capStart = new LaserData_BC8.LaserRow(sprite, 0, 0, 0, 0);
            LaserData_BC8.LaserRow start = null;
            LaserData_BC8.LaserRow[] middle = {
                    new LaserData_BC8.LaserRow(sprite, 0, 4, 16, 12)
            };
            LaserData_BC8.LaserRow end = new LaserData_BC8.LaserRow(sprite, 0, 4, 16, 12);
            LaserData_BC8.LaserRow capEnd = new LaserData_BC8.LaserRow(sprite, 0, 0, 0, 0);
            FRAME = new LaserData_BC8.LaserType(capStart, start, middle, end, capEnd);
        }
        {
            SpriteHolderRegistry.SpriteHolder sprite = SpriteHolderRegistry.getHolder("buildcraftbuilders:blocks/frame/default");
            LaserData_BC8.LaserRow capStart = new LaserData_BC8.LaserRow(sprite, 0, 0, 0, 0);
            LaserData_BC8.LaserRow start = null;
            LaserData_BC8.LaserRow[] middle = {
                    new LaserData_BC8.LaserRow(sprite, 0, 4, 16, 12)
            };
            LaserData_BC8.LaserRow end = new LaserData_BC8.LaserRow(sprite, 0, 4, 16, 12);
            LaserData_BC8.LaserRow capEnd = new LaserData_BC8.LaserRow(sprite, 4, 4, 12, 12);
            FRAME_BOTTOM = new LaserData_BC8.LaserType(capStart, start, middle, end, capEnd);
        }
        {
            SpriteHolderRegistry.SpriteHolder sprite = SpriteHolderRegistry.getHolder("buildcraftbuilders:blocks/quarry/drill");
            LaserData_BC8.LaserRow capStart = new LaserData_BC8.LaserRow(sprite, 6, 0, 10, 4);
            LaserData_BC8.LaserRow start = null;
            LaserData_BC8.LaserRow[] middle = {
                    new LaserData_BC8.LaserRow(sprite, 0, 0, 16, 4)
            };
            LaserData_BC8.LaserRow end = null;
            LaserData_BC8.LaserRow capEnd = new LaserData_BC8.LaserRow(sprite, 6, 0, 10, 4);
            DRILL = new LaserData_BC8.LaserType(capStart, start, middle, end, capEnd);
        }
    }

    public RenderQuarry() {}

    @Override
    public void renderTileEntityFast(TileQuarry tile, double x, double y, double z, float partialTicks, int destroyStage, VertexBuffer buffer) {
        if(tile.min != null && tile.max != null && tile.clientDrillPos != null && tile.prevClientDrillPos != null) {
            // this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks;
            buffer.setTranslation(x - tile.getPos().getX(), y - tile.getPos().getY(), z - tile.getPos().getZ());
            Vec3d interpolatedPos = tile.prevClientDrillPos.add(tile.clientDrillPos.subtract(tile.prevClientDrillPos).scale(partialTicks));

//            LaserRenderer_BC8.renderLaserBuffer(new LaserData_BC8(FRAME, new Vec3d(tile.getPos().getX() + 1, tile.getPos().getY() + 0.5, tile.getPos().getZ() + 0.5), new Vec3d(tile.getPos().getX() + 2, tile.getPos().getY() + 0.5, tile.getPos().getZ() + 0.5), 1 / 16D), buffer);
            LaserRenderer_BC8.renderLaserBuffer(new LaserData_BC8(FRAME, new Vec3d(interpolatedPos.xCoord + 0.5, tile.min.getY() + 0.5, interpolatedPos.zCoord), new Vec3d(interpolatedPos.xCoord + 0.5, tile.min.getY() + 0.5, tile.max.getZ() + 12 / 16D), 1 / 16D), buffer);
            LaserRenderer_BC8.renderLaserBuffer(new LaserData_BC8(FRAME, new Vec3d(interpolatedPos.xCoord + 0.5, tile.min.getY() + 0.5, interpolatedPos.zCoord), new Vec3d(interpolatedPos.xCoord + 0.5, tile.min.getY() + 0.5, tile.min.getZ() + 4 / 16D), 1 / 16D), buffer);
            LaserRenderer_BC8.renderLaserBuffer(new LaserData_BC8(FRAME, new Vec3d(interpolatedPos.xCoord, tile.min.getY() + 0.5, interpolatedPos.zCoord + 0.5), new Vec3d(tile.max.getX() + 12 / 16D, tile.min.getY() + 0.5, interpolatedPos.zCoord + 0.5), 1 / 16D), buffer);
            LaserRenderer_BC8.renderLaserBuffer(new LaserData_BC8(FRAME, new Vec3d(interpolatedPos.xCoord, tile.min.getY() + 0.5, interpolatedPos.zCoord + 0.5), new Vec3d(tile.min.getX() + 4 / 16D, tile.min.getY() + 0.5, interpolatedPos.zCoord + 0.5), 1 / 16D), buffer);
            LaserRenderer_BC8.renderLaserBuffer(new LaserData_BC8(FRAME_BOTTOM, new Vec3d(interpolatedPos.xCoord + 0.5, tile.min.getY() + 0.5, interpolatedPos.zCoord + 0.5), new Vec3d(interpolatedPos.xCoord + 0.5, interpolatedPos.yCoord + 1, interpolatedPos.zCoord + 0.5), 1 / 16D), buffer);
            LaserRenderer_BC8.renderLaserBuffer(new LaserData_BC8(DRILL, new Vec3d(interpolatedPos.xCoord + 0.5, interpolatedPos.yCoord + 1, interpolatedPos.zCoord + 0.5), new Vec3d(interpolatedPos.xCoord + 0.5, interpolatedPos.yCoord, interpolatedPos.zCoord + 0.5), 1 / 16D), buffer);

            buffer.setTranslation(0, 0, 0);
        }
    }

    @Override
    public boolean isGlobalRenderer(TileQuarry tile) {
        return true;
    }
}
