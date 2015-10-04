package buildcraft.builders.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicTile;
import buildcraft.api.core.BlockIndex;
import buildcraft.core.lib.utils.NBTUtils;

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
	public void getRequirementsForPlacement(IBuilderContext context, LinkedList<ItemStack> requirements) {
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
	public void initializeFromObjectAt(IBuilderContext context, int x, int y, int z) {
		super.initializeFromObjectAt(context, x, y, z);
		entry = file.getEntryForSchematic(this);
	}

	@Override
	public void placeInWorld(IBuilderContext context, int x, int y, int z, LinkedList<ItemStack> stacks) {
		if (!entry.ignore) {
			super.placeInWorld(context, x, y, z, stacks);
		}
	}

	protected Block getPlacedBlock() {
		if (entry.placedBlock != null) {
			BuilderSupportUtils.BlockItemPair bip = BuilderSupportUtils.parseBlockItemPair(entry.placedBlock);
			if (bip != null) {
				return bip.block;
			}
		}
		return block;
	}

	protected int getPlacedMeta() {
		if (entry.placedBlock != null && entry.placedBlock.contains("@")) {
			BuilderSupportUtils.BlockItemPair bip = BuilderSupportUtils.parseBlockItemPair(entry.placedBlock);
			if (bip != null) {
				return bip.meta;
			}
		}
		return meta;
	}

	@Override
	protected void setBlockInWorld(IBuilderContext context, int x, int y, int z) {
		if (entry.placedBlock != null) {
			BuilderSupportUtils.BlockItemPair bip = BuilderSupportUtils.parseBlockItemPair(entry.placedBlock);
			if (bip != null) {
				Block rblock = bip.block;
				int rmeta = entry.placedBlock.contains("@") ? bip.meta : meta;

				rmeta &= entry.metadataMask;

				context.world().setBlock(x, y, z, rblock, rmeta, 3);
				if (entry.notifyBlockTwice) {
					context.world().setBlockMetadataWithNotify(x, y, z, rmeta, 3);
				}
			}
			return;
		}

		context.world().setBlock(x, y, z, block, meta & entry.metadataMask, 3);
		if (entry.notifyBlockTwice) {
			context.world().setBlockMetadataWithNotify(x, y, z, meta & entry.metadataMask, 3);
		}
	}

	@Override
	public void storeRequirements(IBuilderContext context, int x, int y, int z) {
		if (!entry.ignore) {
			ArrayList<ItemStack> req = null;

			if (entry.requirements != null) {
				req = new ArrayList<ItemStack>();
				for (String s : entry.requirements) {
					ItemStack is = BuilderSupportUtils.parseItemStack(s);
					if (is != null) {
						req.add(is);
					}
				}
			} else if (block != null) {
				if (entry.ignoreDrops) {
					req = new ArrayList<ItemStack>();
					req.add(new ItemStack(block, 1, meta));
				} else {
					req = block.getDrops(context.world(), x,
							y, z, context.world().getBlockMetadata(x, y, z), 0);
				}

				if (!entry.ignoreInventoryContents && block.hasTileEntity(meta)) {
					TileEntity tile = context.world().getTileEntity(x, y, z);

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
	public Set<BlockIndex> getPrerequisiteBlocks(IBuilderContext context) {
		Set<BlockIndex> prerequisites = new HashSet<BlockIndex>();

		if (entry.getPrerequisites() != null) {
			prerequisites.addAll(entry.getPrerequisites());
		} else {
			if (block instanceof BlockFalling) {
				prerequisites.add(RELATIVE_INDEXES[ForgeDirection.DOWN.ordinal()]);
			}
		}

		Collection<BuilderRotation> rotations = entry.getAllRotations();
		if (rotations.size() > 0) {
			for (BuilderRotation r : rotations) {
				if (r.sticksToWall) {
					ForgeDirection dir = r.getOrientation(this);
					if (dir != ForgeDirection.UNKNOWN) {
						prerequisites.add(new BlockIndex(dir.offsetX, dir.offsetY, dir.offsetZ));
					}
				}
			}
		}

		return prerequisites;
	}

	@Override
	public boolean isAlreadyBuilt(IBuilderContext context, int x, int y, int z) {
		if (getPlacedBlock() == context.world().getBlock(x, y, z)) {
			if (entry.metadataEqualityMask != 0) {
				if ((getPlacedMeta() & entry.metadataEqualityMask) != (context.world().getBlockMetadata(x, y, z) & entry.metadataEqualityMask)) {
					return false;
				}
			}

			if (entry.nbt != null && entry.nbt.equality != null) {
				TileEntity tileEntity = context.world().getTileEntity(x, y, z);
				if (tileEntity != null && tileNBT != null) {
					NBTTagCompound targetNBT = new NBTTagCompound();
					tileEntity.writeToNBT(targetNBT);

					for (String s : entry.nbt.equality) {
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
