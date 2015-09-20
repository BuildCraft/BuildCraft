package buildcraft.core.tablet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import cpw.mods.fml.relauncher.Side;

import buildcraft.BuildCraftCore;
import buildcraft.api.tablet.TabletBitmap;

public class TabletServer extends TabletBase {
	protected final EntityPlayer player;

	public TabletServer(EntityPlayer player) {
		super();
		this.player = player;
	}

	@Override
	public void tick(float time) {
		synchronized (programs) {
			while (programs.size() > 0 && programs.getLast().hasEnded()) {
				closeProgram();
			}

			if (programs.size() == 0) {
				launchProgram("menu");
			}

			super.tick(time);
		}
	}

	@Override
	public Side getSide() {
		return Side.SERVER;
	}

	@Override
	public void refreshScreen(TabletBitmap newDisplay) {
		// noop
	}

	@Override
	public void receiveMessage(NBTTagCompound compound) {
		if (!receiveMessageInternal(compound)) {
			if (compound.hasKey("doRemoveProgram")) {
				synchronized (programs) {
					programs.removeLast();
				}
			}
		}
	}

	@Override
	public void launchProgram(String name) {
		if (launchProgramInternal(name)) {
			NBTTagCompound compound = new NBTTagCompound();
			compound.setString("programToLaunch", name);
			BuildCraftCore.instance.sendToPlayer(player, new PacketTabletMessage(compound));
		}
	}

	protected void closeProgram() {
		programs.removeLast();
		NBTTagCompound compound = new NBTTagCompound();
		compound.setBoolean("doRemoveProgram", true);
		BuildCraftCore.instance.sendToPlayer(player, new PacketTabletMessage(compound));
	}

	@Override
	public void sendMessage(NBTTagCompound compound) {
		compound.setBoolean("__program", true);
		BuildCraftCore.instance.sendToPlayer(player, new PacketTabletMessage(compound));
	}
}
