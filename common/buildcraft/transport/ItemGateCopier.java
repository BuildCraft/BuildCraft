package buildcraft.transport;

import net.minecraft.client.renderer.texture.IIconRegister;
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
import buildcraft.core.ItemBuildCraft;
import buildcraft.core.utils.NBTUtils;
import buildcraft.transport.gates.GateDefinition.GateMaterial;

public class ItemGateCopier extends ItemBuildCraft {

	private IIcon[] icons;

	public ItemGateCopier() {
		super();
		setMaxDamage(0);
		setMaxStackSize(1);
		setHasSubtypes(true);
		setUnlocalizedName("gateCopier");
	}
	
	@Override
	public IIcon getIconFromDamage(int damage) {
		return icons[damage];
	}
	
	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		if (world.isRemote) {
			return true;
		}
		
		boolean isCopying = !player.isSneaking();
		TileEntity tile = world.getTileEntity(x, y, z);
		NBTTagCompound data = NBTUtils.getItemData(stack);
		
		if (tile == null || !(tile instanceof TileGenericPipe)) {
			return false;
		}
		
		Gate gate = ((TileGenericPipe) tile).getGate(ForgeDirection.getOrientation(side));
		
		if (isCopying) {
			if (gate == null) {
				stack.setItemDamage(0);
				stack.setTagCompound(null);
				player.addChatMessage(new ChatComponentTranslation("chat.gateCopier.clear"));
				return true;
			}
			
			gate.writeStatementsToNBT(data);
			data.setByte("material", (byte) gate.material.ordinal());
			data.setByte("logic", (byte) gate.logic.ordinal());
			stack.setItemDamage(1);
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

			((TileGenericPipe) tile).sendUpdateToClient();
			player.addChatMessage(new ChatComponentTranslation("chat.gateCopier.gatePasted"));	
			return true;
		}
		
		return true;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister register) {
		icons = new IIcon[2];
		icons[0] = register.registerIcon("buildcraft:gate_copier_off");
		icons[1] = register.registerIcon("buildcraft:gate_copier_on");
	}
	
}
