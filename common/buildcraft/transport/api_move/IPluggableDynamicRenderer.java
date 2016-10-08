package buildcraft.transport.api_move;

import net.minecraft.client.renderer.VertexBuffer;

public interface IPluggableDynamicRenderer {
    void render(double x, double y, double z, VertexBuffer vb);
}
