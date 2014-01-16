package buildcraft.core;

import org.lwjgl.input.Mouse;

import buildcraft.builders.urbanism.Urbanist;
import buildcraft.core.network.RPC;
import buildcraft.core.network.RPCHandler;
import buildcraft.core.network.RPCSide;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MouseHelper;
import net.minecraft.util.MovingObjectPosition;

public class TilePingPong extends TileBuildCraft {

	static class UrbanistMouseHelper extends MouseHelper {

		@Override
	    public void grabMouseCursor() {

	    }
	}


	Urbanist urbanist;
	EntityLivingBase player;


	double posX, posY, posZ;
	float yaw;

	boolean buttonDown = true;

	@RPC (RPCSide.SERVER)
	public void setBlock (int x, int y, int z) {
		worldObj.setBlock(x, y + 1, z, Block.brick.blockID);
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (worldObj.isRemote) {
			if (urbanist == null) {
				urbanist = new Urbanist(worldObj);
				player = Minecraft.getMinecraft().renderViewEntity;

				urbanist.copyLocationAndAnglesFrom(player);


				urbanist.rotationYaw = 0;
				urbanist.rotationPitch = 0;

				Minecraft.getMinecraft().renderViewEntity = urbanist;
				Minecraft.getMinecraft().gameSettings.thirdPersonView = 8;
				Minecraft.getMinecraft().mouseHelper = new UrbanistMouseHelper();
				Minecraft.getMinecraft().setIngameNotInFocus();

				posX = urbanist.posX;
				posY = urbanist.posY + 10;
				posZ = urbanist.posZ;

				yaw = 0;
			}

			urbanist.setPositionAndRotation(posX, posY, posZ, yaw, 50);
			urbanist.setPositionAndUpdate(posX, posY, posZ);


			float width = Minecraft.getMinecraft().displayWidth;
			float height = Minecraft.getMinecraft().displayHeight;

			/*System.out.println (Mouse.getX() + ", " + Mouse.getX());

			yaw += Minecraft.getMinecraft().mouseHelper.deltaX;

			while (yaw > 360) {
				yaw -= 360;
			}

			while (yaw < -360) {
				yaw += 360;
			}*/

			MovingObjectPosition pos = urbanist.rayTraceMouse();

			if (Mouse.getEventButton() == 0) {
				if (buttonDown) {
					if (pos != null) {
						RPCHandler.rpcServer(this, "setBlock", pos.blockX, pos.blockY, pos.blockZ);
					}

					buttonDown = false;
				}
			} else {
				buttonDown = true;
			}
		}

	}

}
