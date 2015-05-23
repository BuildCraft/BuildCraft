package buildcraft.robotics.boards;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.EntityRobot;


public class RedstoneBoardRobotEmptyNBT extends RedstoneBoardRobotNBT {

	public static RedstoneBoardRobotEmptyNBT instance = new RedstoneBoardRobotEmptyNBT();
	private IIcon icon;

	@Override
	public RedstoneBoardRobot create(EntityRobotBase robot) {
		return new BoardRobotEmpty(robot);
	}

	@Override
	public ResourceLocation getRobotTexture() {
		return EntityRobot.ROBOT_BASE;
	}

	@Override
	public String getID() {
		return "buildcraft:boardRobotEmpty";
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List<?> list, boolean advanced) {
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister iconRegister) {
		icon = iconRegister.registerIcon("buildcraftrobotics:board/clean");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(NBTTagCompound nbt) {
		return icon;
	}

}
