/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.urbanism;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import buildcraft.api.core.Position;
import buildcraft.core.EntityEnergyLaser;

public class EntityUrbanist extends EntityLivingBase {

	/**
	 * To be used only in debug sessions to adjust the mouse pointer parameters.
	 */
	private boolean debugPointer = false;
	private EntityEnergyLaser laser = null;

	public EntityLivingBase player;
	public TileUrbanist tile;


	public EntityUrbanist(World par1World) {
		super(par1World);

		width = 0;
		height = 0;
	}

	@Override
	public void onUpdate() {
        Vec3 look = this.getLook(1.0F).normalize();

        Vec3 worldUp = worldObj.getWorldVec3Pool().getVecFromPool(0, 1, 0);
        Vec3 side = worldUp.crossProduct(look).normalize();
        Vec3 forward = side.crossProduct(worldUp).normalize();

		motionX = 0;
		motionY = 0;
		motionZ = 0;

		if (!Keyboard.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindSneak.getKeyCode())) {
			if (Keyboard.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindLeft.getKeyCode())) {
				motionX = side.xCoord * 0.5;
				motionZ = side.zCoord * 0.5;
			} else if (Keyboard.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindRight.getKeyCode())) {
				motionX = side.xCoord * -0.5;
				motionZ = side.zCoord * -0.5;
			}
		} else {
			if (Keyboard.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindLeft.getKeyCode())) {
				setAngles (-10, 0);
			} else if (Keyboard.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindRight.getKeyCode())) {
				setAngles (10, 0);
			}
		}

		if (!Keyboard.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindSneak.getKeyCode())) {
			if (Keyboard.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindForward.getKeyCode())) {
				motionX = forward.xCoord * 0.5;
				motionZ = forward.zCoord * 0.5;
			} else if (Keyboard.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindBack.getKeyCode())) {
				motionX = forward.xCoord * -0.5;
				motionZ = forward.zCoord * -0.5;
			}
		} else {
			if (Keyboard.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindForward.getKeyCode())) {
				setAngles(0, 10);
			} else if (Keyboard.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindBack.getKeyCode())) {
				setAngles(0, -10);
			}
		}


		if (Keyboard.isKeyDown(Keyboard.KEY_PRIOR)) {
			motionY = 0.2;
		} else if (Keyboard.isKeyDown(Keyboard.KEY_NEXT)) {
			motionY = -0.2;
		}

		super.onUpdate();
	}

	@Override
	public ItemStack getHeldItem() {
		return null;
	}

	@Override
	public void setCurrentItemOrArmor(int i, ItemStack itemstack) {

	}

	@Override
	public ItemStack[] getLastActiveItems() {
		return null;
	}

	public MovingObjectPosition rayTraceMouse() {
		// These three parameters have been determined experimentally. Proper
		// ray tracing would involve matrix inversion through e.g. gluUnProject.
		// This works just fine right now, and can be re-adjusted using the
		// debug pointer (a laser instance). Note: laser parameters are not yet
		// synchronized with the client, not needs to tweaks to be used
		// properly.
		double posAdjust = -1F;
		float diffScale = 3.67F;
		float diffLScale = 5.2F;

		double distance = 1000;

		float width = Minecraft.getMinecraft().displayWidth;
		float height = Minecraft.getMinecraft().displayHeight;

		float x = Mouse.getX();
		float y = Mouse.getY();

		// the height determines the view scale, hence / height in both cases
		float diffX = (x - (width  / 2F)) / height * 2F;
		float diffY = (y - (height  / 2F)) / height * 2F;

		diffX *= diffScale;
		diffY *= diffScale;

		float diffXL = diffX / diffLScale;
		float diffYL = diffY / diffLScale;

        Vec3 pos = this.getPosition(1.0F);
        Vec3 look = this.getLook(1.0F).normalize();

        Vec3 worldUp = worldObj.getWorldVec3Pool().getVecFromPool(0, 1, 0);
        Vec3 side = worldUp.crossProduct(look).normalize();
        Vec3 up = side.crossProduct(look).normalize();

        pos = pos.addVector(up.xCoord * posAdjust, up.yCoord * posAdjust, up.zCoord * posAdjust);
        pos = pos.addVector(side.xCoord * -diffX, side.yCoord * -diffX, side.zCoord * -diffX);
        pos = pos.addVector(up.xCoord * -diffY, up.yCoord * -diffY, up.zCoord * -diffY);

        look = look.addVector(side.xCoord * -diffXL, side.yCoord * -diffXL, side.zCoord * -diffXL);
        look = look.addVector(up.xCoord * -diffYL, up.yCoord * -diffYL, up.zCoord * -diffYL);

        Vec3 vec32 = pos.addVector(look.xCoord * distance, look.yCoord * distance, look.zCoord * distance);

        MovingObjectPosition result = this.worldObj.rayTraceBlocks(pos, vec32);

		if (debugPointer) {
			if (laser == null) {
				laser = new EntityEnergyLaser(worldObj, new Position(posX,
						posY, posZ), new Position(posX, posY, posZ));
				worldObj.spawnEntityInWorld(laser);
			}

			pos = this.getPosition(1.0F);
			pos = pos.addVector(up.xCoord * posAdjust, up.yCoord * posAdjust,
					up.zCoord * posAdjust);
			pos = pos.addVector(side.xCoord * -diffX, side.yCoord * -diffX,
					side.zCoord * -diffX);
			pos = pos.addVector(up.xCoord * -diffY, up.yCoord * -diffY,
					up.zCoord * -diffY);

			Vec3 aimed = worldObj.getWorldVec3Pool().getVecFromPool(
					pos.xCoord + look.xCoord * 200,
					pos.yCoord + look.yCoord * 200,
					pos.zCoord + look.zCoord * 200);

			laser.setPositions(
					new Position(pos.xCoord, pos.yCoord, pos.zCoord),
					new Position(aimed.xCoord, aimed.yCoord, aimed.zCoord));

			if (!laser.isVisible()) {
				laser.show();
			}
		}

        return result;
    }

	@Override
	public ItemStack getEquipmentInSlot(int var1) {
		return null;
	}

}
