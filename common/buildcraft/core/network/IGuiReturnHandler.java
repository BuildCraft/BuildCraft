package buildcraft.core.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

/**
 * 
 * @author CovertJaguar <railcraft.wikispaces.com>
 */
public abstract interface IGuiReturnHandler {
	public World getWorld();

	public void writeGuiData(DataOutputStream paramDataOutputStream) throws IOException;

	public void readGuiData(DataInputStream paramDataInputStream, EntityPlayer paramEntityPlayer) throws IOException;
}
