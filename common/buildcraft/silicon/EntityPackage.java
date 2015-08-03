package buildcraft.silicon;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

import buildcraft.BuildCraftSilicon;

public class EntityPackage extends EntityThrowable {
	private ItemStack pkg;

	public EntityPackage(World world) {
		super(world);
		this.pkg = new ItemStack(BuildCraftSilicon.packageItem);
	}

	public EntityPackage(World world, EntityPlayer player, ItemStack stack) {
		super(world, player);
		this.pkg = stack;
	}

	public EntityPackage(World world, double x, double y, double z, ItemStack stack) {
		super(world, x, y, z);
		this.pkg = stack;
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		NBTTagCompound subTag = new NBTTagCompound();
		pkg.writeToNBT(subTag);
		compound.setTag("stack", subTag);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		if (compound.hasKey("stack")) {
			pkg = ItemStack.loadItemStackFromNBT(compound.getCompoundTag("stack"));
		}
	}

	@Override
	protected void onImpact(MovingObjectPosition target) {
		double x, y, z;
		if (target.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
			x = target.entityHit.posX;
			y = target.entityHit.posY;
			z = target.entityHit.posZ;
		} else {
			x = target.blockX;
			y = target.blockY;
			z = target.blockZ;
		}

		float hitPoints = 0.0F;
		for (int i = 0; i < 9; i++) {
			ItemStack stack = ItemPackage.getStack(pkg, i);
			if (stack != null) {
				if (stack.getItem() instanceof ItemBlock) {
					hitPoints += 0.28F;
				} else {
					hitPoints += 0.14F;
				}
				float var = 0.7F;
				World world = this.worldObj;
				double dx = world.rand.nextFloat() * var + (1.0F - var) * 0.5D;
				double dy = world.rand.nextFloat() * var + (1.0F - var) * 0.5D;
				double dz = world.rand.nextFloat() * var + (1.0F - var) * 0.5D;
				EntityItem entityitem = new EntityItem(world, x + dx, y + dy, z + dz, stack);
				entityitem.delayBeforeCanPickup = 10;

				world.spawnEntityInWorld(entityitem);
			}
		}

		if (target.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
			target.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this), hitPoints);
		}

		setDead();
	}
}
