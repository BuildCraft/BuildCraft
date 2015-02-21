/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.energy;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import buildcraft.BuildCraftCore;
import buildcraft.api.core.BuildCraftProperties;
import buildcraft.api.enums.EnumEngineType;
import buildcraft.api.events.BlockInteractionEvent;
import buildcraft.core.BlockBuildCraft;
import buildcraft.core.ICustomHighlight;
import buildcraft.core.IItemPipe;
import buildcraft.core.utils.IModelRegister;
import buildcraft.core.utils.ModelHelper;

public class BlockEngine extends BlockBuildCraft implements ICustomHighlight, IModelRegister {
	public static final PropertyEnum TYPE = BuildCraftProperties.ENGINE_TYPE;

	private static final AxisAlignedBB[][] boxes = {
			{AxisAlignedBB.fromBounds(0.0, 0.5, 0.0, 1.0, 1.0, 1.0), AxisAlignedBB.fromBounds(0.25, 0.0, 0.25, 0.75, 0.5, 0.75)}, // -Y
			{AxisAlignedBB.fromBounds(0.0, 0.0, 0.0, 1.0, 0.5, 1.0), AxisAlignedBB.fromBounds(0.25, 0.5, 0.25, 0.75, 1.0, 0.75)}, // +Y
			{AxisAlignedBB.fromBounds(0.0, 0.0, 0.5, 1.0, 1.0, 1.0), AxisAlignedBB.fromBounds(0.25, 0.25, 0.0, 0.75, 0.75, 0.5)}, // -Z
			{AxisAlignedBB.fromBounds(0.0, 0.0, 0.0, 1.0, 1.0, 0.5), AxisAlignedBB.fromBounds(0.25, 0.25, 0.5, 0.75, 0.75, 1.0)}, // +Z
			{AxisAlignedBB.fromBounds(0.5, 0.0, 0.0, 1.0, 1.0, 1.0), AxisAlignedBB.fromBounds(0.0, 0.25, 0.25, 0.5, 0.75, 0.75)}, // -X
			{AxisAlignedBB.fromBounds(0.0, 0.0, 0.0, 0.5, 1.0, 1.0), AxisAlignedBB.fromBounds(0.5, 0.25, 0.25, 1.0, 0.75, 0.75)} // +X
	};

	/*private static IIcon woodTexture;
	private static IIcon stoneTexture;
	private static IIcon ironTexture;*/

	public BlockEngine() {
		super(Material.iron, new PropertyEnum[]{TYPE});
		setUnlocalizedName("engineBlock");
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}


	/*@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		woodTexture = par1IconRegister.registerIcon("buildcraft:engineWoodBottom");
		stoneTexture = par1IconRegister.registerIcon("buildcraft:engineStoneBottom");
		ironTexture = par1IconRegister.registerIcon("buildcraft:engineIronBottom");
	}*/

	@Override
	public int getRenderType() {
		return BuildCraftCore.blockByEntityModel;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		switch (metadata) {
			case 0:
				return new TileEngineWood();
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
	public boolean isSideSolid(IBlockAccess world, BlockPos pos, EnumFacing side) {
		TileEntity tile = world.getTileEntity(pos);

		if (tile instanceof TileEngine) {
			return ((TileEngine) tile).orientation.getOpposite() == side;
		} else {
			return false;
		}
	}

	@Override
	public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis) {
		TileEntity tile = world.getTileEntity(pos);

		if (tile instanceof TileEngine) {
			return ((TileEngine) tile).switchOrientation(false);
		} else {
			return false;
		}
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ) {

		TileEntity tile = world.getTileEntity(pos);

		// REMOVED DUE TO CREATIVE ENGINE REQUIREMENTS - dmillerw
		// Drop through if the player is sneaking
//		if (player.isSneaking()) {
//			return false;
//		}

		BlockInteractionEvent event = new BlockInteractionEvent(player, pos, state);
		FMLCommonHandler.instance().bus().post(event);
		if (event.isCanceled()) {
			return false;
		}

		// Do not open guis when having a pipe in hand
		if (player.getCurrentEquippedItem() != null) {
			if (player.getCurrentEquippedItem().getItem() instanceof IItemPipe) {
				return false;
			}
		}

		if (tile instanceof TileEngine) {
			return ((TileEngine) tile).onBlockActivated(player, side);
		}

		return false;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void addCollisionBoxesToList(World wrd, BlockPos pos, IBlockState state, AxisAlignedBB mask, List list, Entity ent) {
		TileEntity tile = wrd.getTileEntity(pos);
		if (tile instanceof TileEngine) {
			AxisAlignedBB[] aabbs = boxes[((TileEngine) tile).orientation.ordinal()];
			for (AxisAlignedBB aabb : aabbs) {
				AxisAlignedBB aabbTmp = aabb.offset(pos.getX(), pos.getY(), pos.getZ());
				if (mask.intersectsWith(aabbTmp)) {
					list.add(aabbTmp);
				}
			}
		} else {
			super.addCollisionBoxesToList(wrd, pos, state, mask, list, ent);
		}
	}

	@Override
	public AxisAlignedBB[] getBoxes(World wrd, BlockPos pos, EntityPlayer player) {
		TileEntity tile = wrd.getTileEntity(pos);
		if (tile instanceof TileEngine) {
			return boxes[((TileEngine) tile).orientation.ordinal()];
		} else {
			return new AxisAlignedBB[]{AxisAlignedBB.fromBounds(0.0, 0.0, 0.0, 1.0, 1.0, 1.0)};
		}
	}

	@Override
	public double getExpansion() {
		return 0.0075;
	}

	@Override
	public MovingObjectPosition collisionRayTrace(World wrd, BlockPos pos, Vec3 origin, Vec3 direction) {
		TileEntity tile = wrd.getTileEntity(pos);
		if (tile instanceof TileEngine) {
			AxisAlignedBB[] aabbs = boxes[((TileEngine) tile).orientation.ordinal()];
			MovingObjectPosition closest = null;
			for (AxisAlignedBB aabb : aabbs) {
				MovingObjectPosition mop = aabb.offset(pos.getX(), pos.getY(), pos.getZ()).calculateIntercept(origin, direction);
				if (mop != null) {
					if (closest != null && mop.hitVec.distanceTo(origin) < closest.hitVec.distanceTo(origin)) {
						closest = mop;
					} else {
						closest = mop;
					}
				}
			}
			if (closest != null) 
				closest = new MovingObjectPosition(new Vec3(pos.getX(), pos.getY(), pos.getZ()), closest.sideHit, pos);
			
			return closest;
		} else {
			return super.collisionRayTrace(wrd, pos, origin, direction);
		}
	}

	@Override
	public IBlockState onBlockPlaced(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		IBlockState result = super.onBlockPlaced(world, pos, facing, hitX, hitY, hitZ, meta, placer);
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof TileEngine) {
			TileEngine engine = (TileEngine) tile;
			engine.orientation = EnumFacing.UP;
			if (!engine.isOrientationValid()) {
				engine.switchOrientation(true);
			}
		}
		return result;
	}

	@Override
	public int damageDropped(IBlockState state) {
		return EnumEngineType.getType(state).ordinal();
	}

	@SuppressWarnings({"all"})
	@Override
	public void randomDisplayTick(World world, BlockPos pos, IBlockState state, Random random) {
		TileEntity tile = world.getTileEntity(pos);

		if (!(tile instanceof TileEngine)) {
			return;
		}

		if (((TileEngine) tile).getEnergyStage() == TileEngine.EnergyStage.OVERHEAT) {
			for (int f = 0; f < 16; f++) {
				world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, pos.getX() + 0.4F + (random.nextFloat() * 0.2F),
						pos.getY() + (random.nextFloat() * 0.5F),
						pos.getZ() + 0.4F + (random.nextFloat() * 0.2F),
						random.nextFloat() * 0.04F - 0.02F,
						random.nextFloat() * 0.05F + 0.02F,
						random.nextFloat() * 0.04F - 0.02F);
			}
		} else if (((TileEngine) tile).isBurning()) {
			float f = pos.getX() + 0.5F;
			float f1 = pos.getY() + 0.0F + (random.nextFloat() * 6F) / 16F;
			float f2 = pos.getZ()  + 0.5F;
			float f3 = 0.52F;
			float f4 = random.nextFloat() * 0.6F - 0.3F;

			world.spawnParticle(EnumParticleTypes.REDSTONE, f - f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
			world.spawnParticle(EnumParticleTypes.REDSTONE, f + f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
			world.spawnParticle(EnumParticleTypes.REDSTONE, f + f4, f1, f2 - f3, 0.0D, 0.0D, 0.0D);
			world.spawnParticle(EnumParticleTypes.REDSTONE, f + f4, f1, f2 + f3, 0.0D, 0.0D, 0.0D);
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public void getSubBlocks(Item item, CreativeTabs par2CreativeTabs, List itemList) {
		itemList.add(new ItemStack(this, 1, 0)); // WOOD
		itemList.add(new ItemStack(this, 1, 1)); // STONE
		itemList.add(new ItemStack(this, 1, 2)); // IRON
		itemList.add(new ItemStack(this, 1, 3)); // CREATIVE
	}

	@Override
	public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock) {
		TileEntity tile = world.getTileEntity(pos);

		if (tile instanceof TileEngine) {
			((TileEngine) tile).onNeighborUpdate();
		}
	}

	@Override
	public void registerModels() {
		Item item = ItemBlock.getItemFromBlock(this);
		ModelHelper.registerItemModel(item, 0, "wood");
		ModelHelper.registerItemModel(item, 1, "stone");
		ModelHelper.registerItemModel(item, 2, "iron");
		ModelHelper.registerItemModel(item, 3, "creative");
	}

	/*@Override
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
	}*/

}
