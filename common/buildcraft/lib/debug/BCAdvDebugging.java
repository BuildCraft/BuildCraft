package buildcraft.lib.debug;

import net.minecraft.entity.player.EntityPlayer;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.client.render.DetatchedRenderer.IDetachedRenderer;

public enum BCAdvDebugging implements IDetachedRenderer {
    INSTANCE;

    private IAdvDebugTarget target = null;

    public static boolean isBeingDebugged(IAdvDebugTarget target) {
        return INSTANCE.target == target;
    }

    public static void setCurrentDebugTarget(IAdvDebugTarget target) {
        if (INSTANCE.target != null) {
            INSTANCE.target.disableDebugging();
        }
        INSTANCE.target = target;
    }

    public void onServerPostTick() {
        if (target != null) {
            target.sendDebugState();
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void render(EntityPlayer player, float partialTicks) {
        IDetachedRenderer renderer = target == null ? null : target.getDebugRenderer();
        if (renderer != null) {
            renderer.render(player, partialTicks);
        }
    }
}
