package buildcraft.core;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.BuildCraftCore;
import buildcraft.api.transport.IPipeTile;
import buildcraft.core.lib.block.BlockBuildCraft;
import buildcraft.core.lib.utils.ResourceUtils;
import buildcraft.core.lib.utils.Utils;

public abstract class BlockBuildCraftLED extends BlockBuildCraft {
	private IIcon[] led;

	protected BlockBuildCraftLED(Material material) {
		super(material);
		setPassCount(3);
	}

	@Override
	public void registerBlockIcons(IIconRegister register) {
		super.registerBlockIcons(register);
		String base = ResourceUtils.getObjectPrefix(Block.blockRegistry.getNameForObject(this));
		led = new IIcon[] {
				register.registerIcon(base + "/led_red"),
				register.registerIcon(base + "/led_green")
		};
	}

	@Override
	public IIcon getIconAbsolute(IBlockAccess access, int x, int y, int z, int side, int meta) {
		if (renderPass == 0) {
			return super.getIconAbsolute(access, x, y, z, side, meta);
		} else {
			if (isRotatable()) {
				return side == 2 ? led[renderPass - 1] : null;
			} else {
				return side >= 2 ? led[renderPass - 1] : null;
			}
		}
	}

	@Override
	public IIcon getIconAbsolute(int side, int meta) {
		if (renderPass == 0) {
			return super.getIconAbsolute(side, meta);
		} else {
			if (isRotatable()) {
				return side == 2 ? led[renderPass - 1] : null;
			} else {
				return side >= 2 ? led[renderPass - 1] : null;
			}
		}
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}
}
