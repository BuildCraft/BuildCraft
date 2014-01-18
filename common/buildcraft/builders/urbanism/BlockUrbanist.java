package buildcraft.builders.urbanism;

import buildcraft.BuildCraftBuilders;
import buildcraft.core.BlockBuildCraft;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.GuiIds;
import buildcraft.core.proxy.CoreProxy;
import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockUrbanist extends BlockBuildCraft {

	public static final Random rand = new Random();

	@Override
	public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer entityplayer, int par6, float par7, float par8, float par9) {
		if (!CoreProxy.proxy.isRenderWorld(world)) {
			entityplayer.openGui(BuildCraftBuilders.instance, GuiIds.URBANIST,
					world, i, j, k);
		}

		return true;

	}

	public BlockUrbanist(int id) {
		super(id, Material.rock);
		setBlockUnbreakable();
		setResistance(6000000.0F);
		setStepSound(soundStoneFootstep);
		disableStats();
		setTickRandomly(true);
		setCreativeTab(CreativeTabBuildCraft.MACHINES.get());
	}

	@Override
	public TileEntity createNewTileEntity(World var1) {
		return new TileUrbanist();
	}
}
