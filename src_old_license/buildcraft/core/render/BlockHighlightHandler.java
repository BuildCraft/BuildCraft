/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.render;

import org.lwjgl.opengl.GL11;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.util.math.*;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import buildcraft.core.lib.client.render.ICustomHighlight;

public class BlockHighlightHandler {
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void handleBlockHighlight(DrawBlockHighlightEvent e) {
        if (e.getTarget().typeOfHit == RayTraceResult.Type.BLOCK) {
            int x = MathHelper.floor_double(e.getTarget().hitVec.xCoord);
            int y = MathHelper.floor_double(e.getTarget().hitVec.yCoord);
            int z = MathHelper.floor_double(e.getTarget().hitVec.zCoord);

            BlockPos pos = new BlockPos(x, y, z);
            IBlockState state = e.getPlayer().worldObj.getBlockState(pos);
            Block block = state.getBlock();

            if (block instanceof ICustomHighlight) {
                AxisAlignedBB[] aabbs = ((ICustomHighlight) block).getBoxes(e.getPlayer().worldObj, pos, state);
                Vec3d nPos = e.getPlayer().getPositionEyes(e.getPartialTicks()).subtract(0, e.getPlayer().getEyeHeight(), 0);

                // Highlight "breathing"
                long millis = System.currentTimeMillis();
                float expansion = (millis % 5000) / 2500F - 1;
                expansion *= Math.PI * 2;
                expansion = (MathHelper.sin(expansion) + 1) / 2;
                expansion *= ((ICustomHighlight) block).getBreathingCoefficent();

                GL11.glEnable(GL11.GL_BLEND);
                OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
                GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.4F);
                GL11.glLineWidth(2F);
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glDepthMask(false);

                double exp = ((ICustomHighlight) block).getExpansion();
                exp += expansion / 32D;
                nPos = nPos.subtract(x, y, z);
                for (AxisAlignedBB aabb : aabbs) {
                    AxisAlignedBB changed = aabb.expand(exp, exp, exp).offset(-nPos.xCoord, -nPos.yCoord, -nPos.zCoord);
                    RenderGlobal.drawSelectionBoundingBox(changed);
                }

                GL11.glDepthMask(true);
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glDisable(GL11.GL_BLEND);
                e.setCanceled(true);
            }
        }
    }
}
