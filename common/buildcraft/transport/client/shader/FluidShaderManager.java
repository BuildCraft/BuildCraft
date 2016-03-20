package buildcraft.transport.client.shader;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.shader.ShaderManager;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public enum FluidShaderManager {
    INSTANCE;

    private FluidShaderRenderer currentRenderer;
    private WorldClient currentWorld;
    private boolean hasInit = false;
    private ShaderManager shader = null;

    public ShaderManager getShader() {
        return shader;
    }

    public FluidShaderRenderer getRenderer(WorldClient world) {
        if (!hasInit) {
            bindShaders();
        }
        if (currentWorld != world) {
            currentWorld = world;
            if (currentRenderer != null) {
                currentRenderer.destroy();
            }
            currentRenderer = new FluidShaderRenderer(currentWorld);
        }
        return currentRenderer;
    }

    private void bindShaders() {
        hasInit = true;

        // String shaderSource = "buildcraft/pipe_fluid";
        // try {
        // shader = new ShaderManager(Minecraft.getMinecraft().getResourceManager(), shaderSource);
        // } catch (IOException e) {
        // shader = null;
        // throw new RuntimeException("Could not load the shader!", e);
        // }
    }

    // @SubscribeEvent
    public void clientTick(ClientTickEvent event) {
        if (event.phase == Phase.END) {
            if (Minecraft.getMinecraft().theWorld != null) {
                getRenderer(Minecraft.getMinecraft().theWorld).clientTick();
            }
        }
    }

    // @SubscribeEvent
    public void renderWorld(RenderWorldLastEvent event) {
        if (Minecraft.getMinecraft().theWorld != null) {
            getRenderer(Minecraft.getMinecraft().theWorld).renderAll(event.partialTicks);
        }
    }
}
