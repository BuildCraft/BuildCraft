/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.energy;

import buildcraft.BuildCraftCore;
import buildcraft.core.BlockBuildCraft;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.ICustomHighlight;
import buildcraft.core.IItemPipe;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;
import java.util.Random;

import static net.minecraft.util.AxisAlignedBB.getBoundingBox;

public class BlockEngine extends BlockBuildCraft implements ICustomHighlight {

	private static final AxisAlignedBB[][] boxes = {
			{getBoundingBox(0.0, 0.5, 0.0, 1.0, 1.0, 1.0), getBoundingBox(0.25, 0.0, 0.25, 0.75, 0.5, 0.75)},// -Y
			{getBoundingBox(0.0, 0.0, 0.0, 1.0, 0.5, 1.0), getBoundingBox(0.25, 0.5, 0.25, 0.75, 1.0, 0.75)},// +Y
			{getBoundingBox(0.0, 0.0, 0.5, 1.0, 1.0, 1.0), getBoundingBox(0.25, 0.25, 0.0, 0.75, 0.75, 0.5)},// -Z
			{getBoundingBox(0.0, 0.0, 0.0, 1.0, 1.0, 0.5), getBoundingBox(0.25, 0.25, 0.5, 0.75, 0.75, 1.0)},// +Z
			{getBoundingBox(0.5, 0.0, 0.0, 1.0, 1.0, 1.0), getBoundingBox(0.0, 0.25, 0.25, 0.5, 0.75, 0.75)},// -X
			{getBoundingBox(0.0, 0.0, 0.0, 0.5, 1.0, 1.0), getBoundingBox(0.5, 0.25, 0.25, 1.0, 0.75, 0.75)} // +X
	};

	private static IIcon woodTexture;
	private static IIcon stoneTexture;
	private static IIcon ironTexture;

	public BlockEngine(CreativeTabBuildCraft creativeTab) {
		super(Material.iron, creativeTab);
		setBlockName("engineBlock");
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		woodTexture = par1IconRegister.registerIcon("buildcraft:engineWoodBottom");
		stoneTexture = par1IconRegister.registerIcon("buildcraft:engineStoneBottom");
		ironTexture = par1IconRegister.registerIcon("buildcraft:engineIronBottom");
	}

	@Override
	public int getRenderType() {
		return BuildCraftCore.blockByEntityModel;
	}

	@Override
	public TileEntity createTileEntity(World world, int metadata) {
		switch (metadata) {
			case 1:
				return new TileEngineStone();
			case 2:
				return new TileEngineIron();
			case 3:
				return new TileEngineCreative();
			default:
				return new TileEngineWood();
		}
	}

	@Override
	public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
		TileEntity tile = world.getTileEntity(x, y, z);
		return tile instanceof TileEngine && ((TileEngine) tile).orientation.getOpposite() == side;
	}

	@Override
	public boolean rotateBlock(World world, int x, int y, int z, ForgeDirection axis) {
		TileEntity tile = world.getTileEntity(x, y, z);
		return tile instanceof TileEngine && ((TileEngine) tile).switchOrientation(false);
	}

	@Override
	public boolean recolourBlock(World wrd, int x, int y, int z, ForgeDirection side, int colour){
		if (wrd.isRemote) { // Easter egg, as requested. --anti344
			EntityPlayer player = Minecraft.getMinecraft().thePlayer;
			player.addChatMessage(new ChatComponentTranslation("egg.recolour_engine"));
			player.addChatMessage(new ChatComponentTranslation("egg.insane"));
		}
		return false;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float par7, float par8, float par9) {
		TileEntity tile = world.getTileEntity(x, y, z);

		// REMOVED DUE TO CREATIVE ENGINE REQUIREMENTS - dmillerw
		// Drop through if the player is sneaking
//		if (player.isSneaking()) {
//			return false;
//		}
		if (tile instanceof TileEngine){
			// Do not open guis when having a pipe in hand
			if(player.getCurrentEquippedItem() != null){
				if(player.getCurrentEquippedItem().getItem() instanceof IItemPipe){
					return false;
				}
			}
			return ((TileEngine) tile).onBlockActivated(player, ForgeDirection.getOrientation(side));
		}
		return false;
	}

	public void addDescription(NBTTagCompound nbt, List<String> lines, boolean f3) {
		if (nbt.hasKey("tankFuel", 10) && nbt.hasKey("tankCoolant", 10)) {
			FluidStack fuel = FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("tankFuel"));
			FluidStack coolant = FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("tankCoolant"));
			if (fuel != null && fuel.getFluid() != null) {
				lines.add(I18n.format("tip.nbt.engine.fuel", I18n.format("tip.fluid.format", fuel.getFluid().getLocalizedName(), fuel.amount)));
			} else {
				lines.add(I18n.format("tip.nbt.engine.fuel", I18n.format("tip.fluid.empty")));
			}
			if (coolant != null && coolant.getFluid() != null) {
				lines.add(I18n.format("tip.nbt.engine.coolant", I18n.format("tip.fluid.format", coolant.getFluid().getLocalizedName(), coolant.amount)));
			} else {
				lines.add(I18n.format("tip.nbt.engine.coolant", I18n.format("tip.fluid.empty")));
			}
		} else {
			super.addDescription(nbt, lines, f3);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void addCollisionBoxesToList(World wrd, int x, int y, int z, AxisAlignedBB mask, List list, Entity ent) {
		TileEntity tile = wrd.getTileEntity(x, y, z);
		if (tile instanceof TileEngine) {
			AxisAlignedBB[] aabbs = boxes[((TileEngine) tile).orientation.ordinal()];
			for (AxisAlignedBB aabb : aabbs) {
				aabb = aabb.getOffsetBoundingBox(x, y, z);
				if (mask.intersectsWith(aabb)) {
					list.add(aabb);
				}
			}
		} else {
			super.addCollisionBoxesToList(wrd, x, y, z, mask, list, ent);
		}
	}

	@Override
	public AxisAlignedBB[] getBoxes(World wrd, int x, int y, int z, EntityPlayer player) {
		TileEntity tile = wrd.getTileEntity(x, y, z);
		if (tile instanceof TileEngine) {
			return boxes[((TileEngine) tile).orientation.ordinal()];
		} else {
			return new AxisAlignedBB[]{AxisAlignedBB.getAABBPool().getAABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0)};
		}
	}

	@Override
	public double getExpansion() {
		return 0.0075;
	}

	@Override
	public MovingObjectPosition collisionRayTrace(World wrd, int x, int y, int z, Vec3 origin, Vec3 direction) {
		TileEntity tile = wrd.getTileEntity(x, y, z);
		if (tile instanceof TileEngine) {
			AxisAlignedBB[] aabbs = boxes[((TileEngine) tile).orientation.ordinal()];
			MovingObjectPosition closest = null;
			for (AxisAlignedBB aabb : aabbs) {
				MovingObjectPosition mop = aabb.getOffsetBoundingBox(x, y, z).calculateIntercept(origin, direction);
				if (mop != null) {
					if (closest != null && mop.hitVec.distanceTo(origin) < closest.hitVec.distanceTo(origin)) {
						closest = mop;
					} else {
						closest = mop;
					}
				}
			}
			if (closest != null) {
				return new MovingObjectPosition(x, y, z, closest.sideHit, closest.hitVec);
			}
			return null;
		} else {
			return super.collisionRayTrace(wrd, x, y, z, origin, direction);
		}
	}

	@Override
	public void onPostBlockPlaced(World world, int x, int y, int z, int par5) {
		TileEntity tile = world.getTileEntity(x, y, z);
		if (tile instanceof TileEngine) {
			TileEngine engine = (TileEngine) tile;
			engine.orientation = ForgeDirection.UP;

			if (!engine.isOrientationValid()) {
				engine.switchOrientation(true);
			}
		}
	}

	@Override
	public int damageDropped(int i) {
		return i;
	}

	@SuppressWarnings({"all"})
	@Override
	public void randomDisplayTick(World world, int x, int y, int z, Random random) {
		TileEntity tile = world.getTileEntity(x, y, z);
		if (tile instanceof TileEngine && !((TileEngine) tile).isBurning()) {
			return;
		}

		float f = x + 0.5F;
		float f1 = y + 0.0F + (random.nextFloat() * 6F) / 16F;
		float f2 = z + 0.5F;
		float f3 = 0.52F;
		float f4 = random.nextFloat() * 0.6F - 0.3F;

		world.spawnParticle("reddust", f - f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
		world.spawnParticle("reddust", f + f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
		world.spawnParticle("reddust", f + f4, f1, f2 - f3, 0.0D, 0.0D, 0.0D);
		world.spawnParticle("reddust", f + f4, f1, f2 + f3, 0.0D, 0.0D, 0.0D);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public void getSubBlocks(Item item, CreativeTabs par2CreativeTabs, List itemList) {
		if (par2CreativeTabs == CreativeTabBuildCraft.TIER_1.get()) {
			itemList.add(new ItemStack(this, 1, 0)); // WOOD
			itemList.add(new ItemStack(this, 1, 1)); // STONE
		} else {
			itemList.add(new ItemStack(this, 1, 2)); // IRON
			itemList.add(new ItemStack(this, 1, 3)); // CREATIVE
		}
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
		TileEntity tile = world.getTileEntity(x, y, z);

		if (tile instanceof TileEngine) {
			((TileEngine) tile).checkRedstonePower();
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		switch (meta) {
			case 0:
				return woodTexture;
			case 1:
				return stoneTexture;
			case 2:
				return ironTexture;
			default:
				return null;
		}
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return null;
	}
}
