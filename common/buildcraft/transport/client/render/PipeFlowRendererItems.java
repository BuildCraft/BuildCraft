package buildcraft.transport.client.render;

import java.util.List;

import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import buildcraft.api.transport.neptune.IPipeFlowRenderer;

import buildcraft.core.lib.client.model.BCModelHelper;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.render.ItemRenderUtil;
import buildcraft.lib.client.sprite.ISprite;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.transport.BCTransportSprites;
import buildcraft.transport.pipe.flow.PipeFlowItems;
import buildcraft.transport.pipe.flow.TravellingItem;

public enum PipeFlowRendererItems implements IPipeFlowRenderer<PipeFlowItems> {
    INSTANCE;

    private static final MutableQuad[] COLOURED_QUADS = new MutableQuad[6];

    public static void onModelBake() {
        Tuple3f center = new Point3f();
        Tuple3f radius = new Vector3f(0.2f, 0.2f, 0.2f);

        ISprite sprite = BCTransportSprites.COLOUR_ITEM_BOX;
        float[] uvs = {//
            (float) sprite.getInterpU(0),//
            (float) sprite.getInterpU(1),//
            (float) sprite.getInterpV(0),//
            (float) sprite.getInterpV(1) //
        };

        for (EnumFacing face : EnumFacing.VALUES) {
            MutableQuad q = BCModelHelper.createFace(face, center, radius, uvs);
            q.setCalculatedDiffuse();
            COLOURED_QUADS[face.ordinal()] = q;
        }
    }

    @Override
    public void render(PipeFlowItems flow, double x, double y, double z, float partialTicks, VertexBuffer vb) {
        World world = flow.pipe.getHolder().getPipeWorld();
        long now = world.getTotalWorldTime();
        int lightc = world.getCombinedLight(flow.pipe.getHolder().getPipePos(), 0);

        List<TravellingItem> toRender = flow.getAllItemsForRender();

        for (TravellingItem item : toRender) {
            Vec3d pos = item.getRenderPosition(BlockPos.ORIGIN, now, partialTicks);

            ItemRenderUtil.renderItem(x + pos.xCoord, y + pos.yCoord, z + pos.zCoord, item.stack);

            if (item.colour != null) {
                vb.setTranslation(x + pos.xCoord, y + pos.yCoord, z + pos.zCoord);
                int col = ColourUtil.getLightHex(item.colour);
                int r = (col >> 16) & 0xFF;
                int g = (col >> 8) & 0xFF;
                int b = col & 0xFF;
                for (MutableQuad q : COLOURED_QUADS) {
                    MutableQuad q2 = new MutableQuad(q);
                    q2.lighti(lightc);
                    q2.multColourd(r / 255.0, g / 255.0, b / 255.0, 1);
                    q2.render(vb);
                }
                vb.setTranslation(0, 0, 0);
            }
        }
    }
}
