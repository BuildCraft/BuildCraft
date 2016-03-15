package buildcraft.builders.json;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicTile;
import buildcraft.core.lib.utils.NBTUtils;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import java.util.*;

public class SchematicJSON extends SchematicTile {
	public final String entryName;
	private final BuilderSupportFile file;
	private BuilderSupportEntry entry;

	public SchematicJSON(BuilderSupportFile file, String entryName) {
		this.file = file;
		this.entryName = entryName;
	}

	@Override
	public void onNBTLoaded() {
		entry = file.getEntryForSchematic(this);
		defaultPermission = entry.buildingPermission;

		if (tileNBT != null && entry.nbt != null) {
			if (entry.nbt.blacklist != null) {
				for (String s : entry.nbt.blacklist) {
					tileNBT.removeTag(s);
				}
			} else if (entry.nbt.whitelist != null) {
				NBTTagCompound copyTag = new NBTTagCompound();

				for (String s : entry.nbt.whitelist) {
					if (tileNBT.hasKey(s)) {
						copyTag.setTag(s, tileNBT.getTag(s));
					}
				}

				tileNBT = copyTag;
			}
		}
	}

	@Override
	public void getRequirementsForPlacement(IBuilderContext context, List<ItemStack> requirements) {
		if (!entry.ignore) {
			super.getRequirementsForPlacement(context, requirements);
		}
	}

	@Override
	public void rotateLeft(IBuilderContext context) {
		for (BuilderRotation r : entry.getAllRotations()) {
			r.rotateLeft(this, context);
		}
	}

	@Override
	public void initializeFromObjectAt(IBuilderContext context, BlockPos pos) {
		super.initializeFromObjectAt(context, pos);
		entry = file.getEntryForSchematic(this);
	}

	@Override
	public void placeInWorld(IBuilderContext context, BlockPos pos, List<ItemStack> stacks) {
		if (!entry.ignore) {
			super.placeInWorld(context, pos, stacks);
		}
	}

	protected IBlockState getPlacedState() {
		if (entry.placedBlock != null) {
			BuilderSupportUtils.BlockItemPair bip = BuilderSupportUtils.parseBlockItemPair(entry.placedBlock);
			if (bip != null) {
				return bip.block.getStateFromMeta(bip.meta);
			}
		}
		return state;
	}

	@Override
	protected void setBlockInWorld(IBuilderContext context, BlockPos pos) {
		if (entry.placedBlock != null) {
			BuilderSupportUtils.BlockItemPair bip = BuilderSupportUtils.parseBlockItemPair(entry.placedBlock);
			if (bip != null) {
				context.world().setBlockState(pos, bip.block.getStateFromMeta(bip.meta), 3);
			}
			return;
		}

		context.world().setBlockState(pos, state, 3);
	}

	@Override
	public void storeRequirements(IBuilderContext context, BlockPos pos) {
		if (!entry.ignore) {
			List<ItemStack> req = null;

			if (entry.requirements != null) {
				req = new ArrayList<ItemStack>();
				for (String s : entry.requirements) {
					ItemStack is = BuilderSupportUtils.parseItemStack(s);
					if (is != null) {
						req.add(is);
					}
				}
			} else if (state != null) {
				World reqWorld = context.world();
				if (tileNBT != null) {
					tileNBT.setInteger("x", pos.getX());
					tileNBT.setInteger("y", pos.getY());
					tileNBT.setInteger("z", pos.getZ());
					reqWorld = new WorldWrapped(context.world(), pos, TileEntity.createAndLoadEntity(tileNBT));
				}

				if (entry.ignoreDrops) {
					req = new ArrayList<ItemStack>();
					req.add(new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state)));
				} else {
					req = state.getBlock().getDrops(reqWorld, pos, state, 0);
				}

				if (!entry.ignoreInventoryContents && state.getBlock().hasTileEntity(state)) {
					TileEntity tile = reqWorld.getTileEntity(pos);

					if (tile instanceof IInventory) {
						IInventory inv = (IInventory) tile;

						for (int i = 0; i < inv.getSizeInventory(); ++i) {
							if (inv.getStackInSlot(i) != null) {
								req.add(inv.getStackInSlot(i));
							}
						}
					}
				}
			}

			if (req != null) {
				storedRequirements = new ItemStack [req.size()];
				req.toArray(storedRequirements);
			} else {
				storedRequirements = new ItemStack[0];
			}
		}
	}

	@Override
	public Set<BlockPos> getPrerequisiteBlocks(IBuilderContext context) {
		Set<BlockPos> prerequisites = new HashSet<BlockPos>();

		if (entry.getPrerequisites() != null) {
			prerequisites.addAll(entry.getPrerequisites());
		} else {
			if (state.getBlock() instanceof BlockFalling) {
				prerequisites.add(BlockPos.ORIGIN.offset(EnumFacing.DOWN));
			}
		}

		Collection<BuilderRotation> rotations = entry.getAllRotations();
		if (rotations.size() > 0) {
			for (BuilderRotation r : rotations) {
				if (r.sticky) {
					EnumFacing dir = r.getOrientation(this);
					if (dir != null) {
						prerequisites.add(BlockPos.ORIGIN.offset(dir));
					}
				}
			}
		}

		return prerequisites;
	}

	@Override
	public boolean isAlreadyBuilt(IBuilderContext context, BlockPos pos) {
		IBlockState oState = context.world().getBlockState(pos);
		if (state.getBlock() == oState.getBlock()) {
			// TODO: equality checks
			for (BuilderRotation rotation : entry.getAllRotations()) {
				if (!rotation.isEqual(this, state, tileNBT)) {
					return false;
				}
			}

			if (entry.nbt != null && entry.nbt.equal != null) {
				TileEntity tileEntity = context.world().getTileEntity(pos);
				if (tileEntity != null && tileNBT != null) {
					NBTTagCompound targetNBT = new NBTTagCompound();
					tileEntity.writeToNBT(targetNBT);

					for (String s : entry.nbt.equal) {
						NBTBase srcTag = NBTUtils.getTag(tileNBT, s);
						NBTBase dstTag = NBTUtils.getTag(targetNBT, s);
						if (!srcTag.equals(dstTag)) {
							return false;
						}
					}
				} else {
					if (tileEntity != null || tileNBT != null) {
						return false;
					}
				}
			}

			return true;
		}
		return false;
	}

	@Override
	public boolean doNotBuild() {
		return entry != null ? entry.ignore : false;
	}
}
