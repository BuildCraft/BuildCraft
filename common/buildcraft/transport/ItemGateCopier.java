package buildcraft.transport;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.core.lib.items.ItemBuildCraft;
import buildcraft.core.lib.utils.NBTUtils;
import buildcraft.transport.BlockGenericPipe.Part;
import buildcraft.transport.BlockGenericPipe.RaytraceResult;
import buildcraft.transport.gates.GateDefinition.GateMaterial;
import buildcraft.transport.gates.GatePluggable;

public class ItemGateCopier extends ItemBuildCraft {
	public ItemGateCopier() {
		super();
		setMaxStackSize(1);
		setUnlocalizedName("gateCopier");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconIndex(ItemStack i) {
		NBTTagCompound cpt = NBTUtils.getItemData(i);
		this.itemIcon = cpt.hasKey("logic") ? icons[1] : icons[0];
		return this.itemIcon;
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		if (world.isRemote) {
			return true;
		}

		boolean isCopying = !player.isSneaking();
		TileEntity tile = world.getTileEntity(x, y, z);
		NBTTagCompound data = NBTUtils.getItemData(stack);
		PipePluggable pluggable = null;
		Gate gate = null;

		if (tile == null || !(tile instanceof IPipeTile)) {
			isCopying = true;
		} else {
			Block block = world.getBlock(x, y, z);

			if (tile instanceof TileGenericPipe && block instanceof BlockGenericPipe) {
				RaytraceResult rayTraceResult = ((BlockGenericPipe) block).doRayTrace(world, x, y, z, player);

				if (rayTraceResult != null && rayTraceResult.boundingBox != null && rayTraceResult.hitPart == Part.Pluggable) {
					pluggable = ((TileGenericPipe) tile).getPipePluggable(rayTraceResult.sideHit);
				}
			} else {
				pluggable = ((IPipeTile) tile).getPipePluggable(ForgeDirection.getOrientation(side));
			}
		}

		if (pluggable instanceof GatePluggable) {
			gate = ((GatePluggable) pluggable).realGate;
		}

		if (isCopying) {
			if (gate == null) {
				stack.setTagCompound(new NBTTagCompound());
				player.addChatMessage(new ChatComponentTranslation("chat.gateCopier.clear"));
				return true;
			}

			data = new NBTTagCompound();
			stack.setTagCompound(data);

			gate.writeStatementsToNBT(data);
			data.setByte("material", (byte) gate.material.ordinal());
			data.setByte("logic", (byte) gate.logic.ordinal());
			player.addChatMessage(new ChatComponentTranslation("chat.gateCopier.gateCopied"));
		} else {
			if (!data.hasKey("logic")) {
				player.addChatMessage(new ChatComponentTranslation("chat.gateCopier.noInformation"));
				return true;
			} else if (gate == null) {
				player.addChatMessage(new ChatComponentTranslation("chat.gateCopier.noGate"));
				return true;
			}

			GateMaterial dataMaterial = GateMaterial.fromOrdinal(data.getByte("material"));
			GateMaterial gateMaterial = gate.material;

			if (gateMaterial.numSlots < dataMaterial.numSlots) {
				player.addChatMessage(new ChatComponentTranslation("chat.gateCopier.warning.slots"));
			}
			if (gateMaterial.numActionParameters < dataMaterial.numActionParameters) {
				player.addChatMessage(new ChatComponentTranslation("chat.gateCopier.warning.actionParameters"));
			}
			if (gateMaterial.numTriggerParameters < dataMaterial.numTriggerParameters) {
				player.addChatMessage(new ChatComponentTranslation("chat.gateCopier.warning.triggerParameters"));
			}
			if (data.getByte("logic") != gate.logic.ordinal()) {
				player.addChatMessage(new ChatComponentTranslation("chat.gateCopier.warning.logic"));
			}

			gate.readStatementsFromNBT(data);
			if (!gate.verifyGateStatements()) {
				player.addChatMessage(new ChatComponentTranslation("chat.gateCopier.warning.load"));
			}

			if (tile instanceof TileGenericPipe) {
				((TileGenericPipe) tile).sendUpdateToClient();
			}
			player.addChatMessage(new ChatComponentTranslation("chat.gateCopier.gatePasted"));
			return true;
		}

		return true;
	}

	public String[] getIconNames() {
		return new String[]{"gateCopier/empty", "gateCopier/full"};
	}
}
