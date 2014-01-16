package buildcraft.builders.urbanism;

import org.lwjgl.input.Mouse;

import buildcraft.api.core.Position;
import buildcraft.core.EntityEnergyLaser;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class Urbanist extends EntityLivingBase {


	private EntityEnergyLaser laser = null;

	public Urbanist(World par1World) {
		super(par1World);
	}

	@Override
	public ItemStack getHeldItem() {
		return null;
	}

	@Override
	public ItemStack getCurrentItemOrArmor(int i) {
		return null;
	}

	@Override
	public void setCurrentItemOrArmor(int i, ItemStack itemstack) {

	}

	@Override
	public ItemStack[] getLastActiveItems() {
		return null;
	}

	protected void createLaser() {
		if (laser == null) {
			laser = new EntityEnergyLaser(worldObj, new Position(posX, posY, posZ), new Position(posX, posY, posZ));
			worldObj.spawnEntityInWorld(laser);
		}
	}

	public void setLaser () {
		createLaser();
	}

	public MovingObjectPosition rayTraceMouse()
    {
		createLaser();
		//double posAdjust = -1F;
		double posAdjust = 0F;

		double distance = 1000;

		float width = Minecraft.getMinecraft().displayWidth;
		float height = Minecraft.getMinecraft().displayHeight;

		float diffX = ((float) Mouse.getX() / width) * 2F - 1F;
		float diffY = ((float) Mouse.getY() / height) * 2F - 1F;

		diffX *= 1.70F;
		diffY *= 0.89F; // < 0.90
		//diffY = (diffY + 0.2F);

        Vec3 pos = this.getPosition(1.0F);
        Vec3 look = this.getLook(1.0F).normalize();

        /*float f1;
        float f2;
        float f3;
        float f4;

        f1 = MathHelper.cos(-this.rotationYaw - (float)Math.PI);
        f2 = MathHelper.sin(-this.rotationYaw - (float)Math.PI);
        f3 = -MathHelper.cos(-this.rotationPitch);
        f4 = MathHelper.sin(-this.rotationPitch);
        Vec3 look = this.worldObj.getWorldVec3Pool().getVecFromPool((double)(f2 * f3), (double)f4, (double)(f1 * f3));*/


        Vec3 worldUp = worldObj.getWorldVec3Pool().getVecFromPool(0, 1, 0);
        Vec3 side = worldUp.crossProduct(look).normalize();
        Vec3 up = side.crossProduct(look).normalize();

        pos = pos.addVector(up.xCoord * posAdjust, up.yCoord * posAdjust, up.zCoord * posAdjust);

        look = look.addVector(side.xCoord * -diffX, side.yCoord * -diffX, side.zCoord * -diffX);
        look = look.addVector(up.xCoord * -diffY, up.yCoord * -diffY, up.zCoord * -diffY);

        Vec3 vec32 = pos.addVector(look.xCoord * distance, look.yCoord * distance, look.zCoord * distance);

        //laser.setPositions (new Position(pos.xCoord, pos.yCoord, pos.zCoord), new Position(aimed.xCoord, aimed.yCoord, aimed.zCoord));

        //Debug.log (aimed.xCoord + ", " + aimed.yCoord + ", " + aimed.zCoord);

        //laser.setPositions (new Position(pos.xCoord, pos.yCoord, pos.zCoord), new Position(pos.xCoord + 1, pos.yCoord, pos.zCoord + 1));

        MovingObjectPosition result = this.worldObj.clip(pos, vec32);

        pos = this.getPosition(1.0F);
        pos = pos.addVector(up.xCoord * posAdjust, up.yCoord * posAdjust, up.zCoord * posAdjust);
        Vec3 aimed = pos.addVector (look.xCoord * 200, look.yCoord * 200, look.zCoord * 200);
        pos = this.getPosition(1.0F);
        pos = pos.addVector(up.xCoord * posAdjust, up.yCoord * posAdjust, up.zCoord * posAdjust);
        laser.setPositions (new Position(pos.xCoord, pos.yCoord, pos.zCoord), new Position(aimed.xCoord, aimed.yCoord, aimed.zCoord));
        //laser.setPositions (new Position(pos.xCoord, pos.yCoord, pos.zCoord), new Position(result.blockX + 0.5F, result.blockY + 0.5F, result.blockZ + 0.5F));

        if (!laser.isVisible()) {
			laser.show();
        }

        return result;
    }

}
