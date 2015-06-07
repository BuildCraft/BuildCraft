/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.core.lib.render.ICustomHighlight;

public class BlockHighlightHandler {

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void handleBlockHighlight(DrawBlockHighlightEvent e) {
        if (e.target.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            int x = (int) e.target.hitVec.xCoord;
            int y = (int) e.target.hitVec.yCoord;
            int z = (int) e.target.hitVec.zCoord;
            BlockPos pos = new BlockPos(x, y, z);
            Block block = e.player.worldObj.getBlockState(pos).getBlock();
            if (block instanceof ICustomHighlight) {
                AxisAlignedBB[] aabbs = ((ICustomHighlight) block).getBoxes(e.player.worldObj, pos, e.player);

                pos = e.player.getPosition();
                GL11.glEnable(GL11.GL_BLEND);
                OpenGlHelper.glBlendFunc(770, 771, 1, 0);
                GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.4F);
                GL11.glLineWidth(2.0F);
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glDepthMask(false);
                double exp = ((ICustomHighlight) block).getExpansion();
                for (AxisAlignedBB aabb : aabbs) {
                    RenderGlobal
                        .drawOutlinedBoundingBox(aabb.expand(exp, exp, exp).offset(x, y, z).offset(-pos.getX(), -pos.getY(), -pos.getZ()), -1);
                }
                GL11.glDepthMask(true);
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glDisable(GL11.GL_BLEND);
                e.setCanceled(true);
            }
        }
    }
}
