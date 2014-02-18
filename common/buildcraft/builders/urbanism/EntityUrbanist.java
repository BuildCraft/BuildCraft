/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.urbanism;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import org.lwjgl.input.Keyboard;

import buildcraft.BuildCraftCore;
import buildcraft.api.core.Position;
import buildcraft.core.EntityEnergyLaser;

public class EntityUrbanist extends EntityLivingBase {

	/**
	 * To be used only in debug sessions to adjust the mouse pointer parameters.
	 */
	private boolean debugPointer = true;
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

	static FloatBuffer modelviewF = GLAllocation.createDirectFloatBuffer(16);
	static FloatBuffer projectionF = GLAllocation.createDirectFloatBuffer(16);
	static DoubleBuffer modelviewD = ByteBuffer.allocateDirect(16 * 8).asDoubleBuffer();
	static DoubleBuffer projectionD = ByteBuffer.allocateDirect(16 * 8).asDoubleBuffer();
	static IntBuffer viewport = GLAllocation.createDirectIntBuffer(16);
	static FloatBuffer winZ = ByteBuffer.allocateDirect(1 * 4).asFloatBuffer();
    static FloatBuffer pos = ByteBuffer.allocateDirect(3 * 4).asFloatBuffer();

	public MovingObjectPosition rayTraceMouse() {
		double distance = 1000;

        Vec3 pos = this.getPosition(1.0F);
        Vec3 look = this.getLook(1.0F).normalize();

        pos.xCoord += BuildCraftCore.diffX;
        pos.yCoord += BuildCraftCore.diffY;
        pos.zCoord += BuildCraftCore.diffZ;

        Vec3 vec32 = pos.addVector(look.xCoord * distance, look.yCoord * distance, look.zCoord * distance);

        MovingObjectPosition result = this.worldObj.rayTraceBlocks(pos, vec32);

        // TODO: work on pos not on result!!!
        System.out.println ("POS = " + pos.xCoord + ", " + pos.yCoord + ", " + pos.zCoord);
        System.out.println ("RESULT = " + result.blockX + ", " + result.blockY + ", " + result.blockZ);

		if (debugPointer) {
			if (laser == null) {
				// note: as this is on the client, it will only work if the
				// server client update is deactivated in the server updateentity.
				laser = new EntityEnergyLaser(worldObj, new Position(posX,
						posY, posZ), new Position(posX, posY, posZ));

				worldObj.spawnEntityInWorld(laser);
			}

			pos = this.getPosition(1.0F);
			pos.xCoord += BuildCraftCore.diffX;
		    pos.yCoord += BuildCraftCore.diffY + 0.1F;
		    pos.zCoord += BuildCraftCore.diffZ;

		    look = this.getLook(1.0F).normalize();

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
