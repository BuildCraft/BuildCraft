package buildcraft.lib.client.render.tile;

import java.util.function.BiConsumer;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import buildcraft.lib.client.model.MutableVertex;

public class RenderMachineWave<T extends TileEntity> implements ITileRenderPart<T> {
    private static final double SIZE = 1 / 16.0;

    public final MutableVertex centerStart = new MutableVertex();
    public double height = 4 / 16.0;
    public int length = 6;
    public EnumFacing direction = EnumFacing.NORTH;
    private final BiConsumer<T, RenderMachineWave<T>> resolver;

    public RenderMachineWave(BiConsumer<T, RenderMachineWave<T>> resolver) {
        this.resolver = resolver;
    }

    @Override
    public void render(T tile, VertexBuffer buffer) {
        // TODO: Sine wave (Make the tile return something?)
        RenderPartCube.renderElement(buffer, centerStart, SIZE, SIZE, SIZE, new RenderPartCube(null)); // FIXME: new instance creation and method should be private
    }
}
