/**
 * BuildCraft is open-source. It is distributed under the terms of the
 * BuildCraft Open Source License. It grants rights to read, modify, compile or
 * run the code. It does *NOT* grant the right to redistribute this software or
 * its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */
package buildcraft.transport.pipes;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.tools.IToolWrench;
import buildcraft.core.utils.StringUtils;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportPower;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.ChatMessageComponent;
import net.minecraftforge.common.ForgeDirection;

public class PipePowerIron extends Pipe<PipeTransportPower> {

	public static enum PowerMode {

		M2(2), M4(4), M8(8), M16(16), M32(32), M64(64), M128(128);
		public static final PowerMode[] VALUES = values();
		private final int maxPower;

		private PowerMode(int max) {
			this.maxPower = max;
		}

		public PowerMode getNext() {
			PowerMode next = VALUES[(ordinal() + 1) % (VALUES.length - 1)];
			return next;
		}

		public PowerMode getPrevious() {
			PowerMode previous = VALUES[(ordinal() + VALUES.length - 2) % (VALUES.length - 1)];
			return previous;
		}

		public static PowerMode fromId(int id) {
			if (id < 0 || id >= VALUES.length) {
				return M128;
			}
			return VALUES[id];
		}
	}

	public PipePowerIron(int itemID) {
		super(new PipeTransportPower(), itemID);
		transport.initFromPipe(getClass());
	}

	@Override
	public int getIconIndex(ForgeDirection direction) {
		if (container == null)
			return PipeIconProvider.TYPE.PipePowerIronM128.ordinal();
		return PipeIconProvider.TYPE.PipePowerIronM2.ordinal() + container.getBlockMetadata();
	}

	@Override
	public boolean blockActivated(EntityPlayer player) {
		Item equipped = player.getCurrentEquippedItem() != null ? player.getCurrentEquippedItem().getItem() : null;
		if (equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(player, container.xCoord, container.yCoord, container.zCoord)) {
			if (player.isSneaking()) {
				setMode(getMode().getPrevious());
			} else {
				setMode(getMode().getNext());
			}
			player.addChatMessage(String.format(StringUtils.localize("chat.pipe.power.iron.mode"), getMode().maxPower));

			((IToolWrench) equipped).wrenchUsed(player, container.xCoord, container.yCoord, container.zCoord);
			return true;
		}

		return false;
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		transport.maxPower = getMode().maxPower;
	}

	public PowerMode getMode() {
		return PowerMode.fromId(container.getBlockMetadata());
	}

	public void setMode(PowerMode mode) {
		if (mode.ordinal() != container.getBlockMetadata()) {
			container.worldObj.setBlockMetadataWithNotify(container.xCoord, container.yCoord, container.zCoord, mode.ordinal(), 3);
			container.scheduleRenderUpdate();
			container.markBlockForUpdate();
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return BuildCraftTransport.instance.pipeIconProvider;
	}
}
