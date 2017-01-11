package buildcraft.factory.client.render;

import java.util.Deque;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

import buildcraft.factory.tile.TileFloodGate;
import buildcraft.lib.client.render.DetatchedRenderer.IDetachedRenderer;
import buildcraft.lib.debug.DebugRenderHelper;

public class AdvDebuggerFloodGate implements IDetachedRenderer {
    public final TileFloodGate target;

    public AdvDebuggerFloodGate(TileFloodGate target) {
        this.target = target;
    }

    @Override
    public void render(EntityPlayer player, float partialTicks) {
        VertexBuffer vb = Tessellator.getInstance().getBuffer();
        vb.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        TreeMap<Integer, Deque<BlockPos>> queues = target.clientLayerQueues;

        int r = 255;
        int g = 255;
        int b = 255;
        for (Entry<Integer, Deque<BlockPos>> entry : queues.entrySet()) {
            Deque<BlockPos> positions = entry.getValue();
            for (BlockPos p : positions) {
                int colour = 0xFF_00_00_00 | (r << 16) | (g << 8) | b;
                DebugRenderHelper.renderSmallCuboid(vb, p, colour);
                r -= 16;
                if (r < 0) {
                    r = 256;
                    g -= 16;
                    if (g < 0) {
                        g = 256;
                        b -= 16;
                        if (b < 0) {
                            b = 256;
                        }
                    }
                }
            }
        }

        Tessellator.getInstance().draw();
    }
}
