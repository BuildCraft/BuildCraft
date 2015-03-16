package buildcraft.silicon.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import buildcraft.core.lib.block.BlockBuildCraft;
import buildcraft.core.lib.render.RenderUtils;
import buildcraft.core.lib.render.SubIcon;
import buildcraft.silicon.SiliconProxy;
import buildcraft.transport.render.FakeBlock;

/**
 * Created by asie on 3/15/15.
 */
public class RenderLaserTable implements ISimpleBlockRenderingHandler {
	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
		BlockBuildCraft bcBlock = (BlockBuildCraft) block;
		switch (metadata) {
			case 0:
				renderAssemblyTable(renderer, true, 0, 0, 0, bcBlock);
				break;
			case 1:
				renderAdvancedCraftingTable(renderer, true, 0, 0, 0, bcBlock);
				break;
			case 3:
				renderChargingTable(renderer, true, 0, 0, 0, bcBlock);
				break;
		}
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		BlockBuildCraft bcBlock = (BlockBuildCraft) block;
		switch (world.getBlockMetadata(x, y, z)) {
			case 0:
				renderAssemblyTable(renderer, false, x, y, z, bcBlock);
				break;
			case 1:
				renderAdvancedCraftingTable(renderer, false, x, y, z, bcBlock);
				break;
			case 3:
				renderChargingTable(renderer, false, x, y, z, bcBlock);
				break;
		}
		return true;
	}

	private void renderCube(RenderBlocks renderer, boolean isInventory, int xPos, int yPos, int zPos, float xB, float yB, float zB, int w, int h, int d, int topX, int topY, IIcon base, int mask) {
		int xI = (int) (xB * 16.0F);
		int yI = 16 - (int) (yB * 16.0F) - h;
		int zI = (int) (zB * 16.0F);
		FakeBlock block = FakeBlock.INSTANCE;
		block.setRenderMask(mask);
		block.setColor(0xFFFFFF);
		IIcon[] icons = block.getTextureState().popArray();
		icons[0] = new SubIcon(base, topX + w - xI, topY - zI, 16, 16);
		icons[1] = new SubIcon(base, topX - xI, topY - zI, 16, 16);
		icons[2] = new SubIcon(base, topX - xI, topY + d - yI, 16, 16);
		icons[3] = new SubIcon(base, topX + w + d - xI, topY + d - yI, 16, 16);
		icons[4] = new SubIcon(base, topX - d - zI, topY + d - yI, 16, 16);
		icons[5] = new SubIcon(base, topX + w - zI, topY + d - yI, 16, 16);
		renderer.setRenderBounds(xB, yB, zB, xB + (w / 16.0F), yB + (h / 16.0F), zB + (d / 16.0F));
		if (isInventory) {
			RenderUtils.drawBlockItem(renderer, Tessellator.instance, block, 0);
		} else {
			renderer.renderStandardBlock(block, xPos, yPos, zPos);
		}
	}

	private void renderAssemblyTable(RenderBlocks renderer, boolean isInv, int x, int y, int z, BlockBuildCraft block) {
		IIcon base = block.getIcon(0, 0);
		renderCube(renderer, isInv, x, y, z, 0, 0, 0, 16, 2, 16, 16, 21, base, 0x3f); // bottom
		renderCube(renderer, isInv, x, y, z, 0.0625F, 0.125F, 0.0625F, 14, 1, 14, 18, 39, base, 0x3c); // middle (no top/bottom rendered)
		renderCube(renderer, isInv, x, y, z, 0, 0.1875F, 0, 16, 5, 16, 16, 0, base, 0x3f); // top
	}

	private void renderChargingTable(RenderBlocks renderer, boolean isInv, int x, int y, int z, BlockBuildCraft block) {
		IIcon base = block.getIcon(0, 3);
		renderCube(renderer, isInv, x, y, z, 0.0625F, 0, 0.0625F, 14, 5, 14, 14, 19, base, 0x3d); // bottom (no top)
		renderCube(renderer, isInv, x, y, z, 0, 0.3125F, 0, 16, 3, 16, 16, 0, base, 0x3f); // top

		// sides (no top)
		renderCube(renderer, isInv, x, y, z, 0, 0, 0, 3, 5, 3, 3, 6, base, 0x3d);
		renderCube(renderer, isInv, x, y, z, 0.8125F, 0, 0, 3, 5, 3, 3, 6, base, 0x3d);
		renderCube(renderer, isInv, x, y, z, 0, 0, 0.8125F, 3, 5, 3, 3, 6, base, 0x3d);
		renderCube(renderer, isInv, x, y, z, 0.8125F, 0, 0.8125F, 3, 5, 3, 3, 6, base, 0x3d);
	}

	private void renderAdvancedCraftingTable(RenderBlocks renderer, boolean isInv, int x, int y, int z, BlockBuildCraft block) {
		IIcon base = block.getIcon(0, 1);
		renderCube(renderer, isInv, x, y, z, 0.125F, 0, 0.125F, 12, 3, 12, 12, 21, base, 0x3d); // bottom (no top)
		renderCube(renderer, isInv, x, y, z, 0, 0.1875F, 0, 16, 5, 16, 16, 0, base, 0x3f); // top

		// sides (no top)
		renderCube(renderer, isInv, x, y, z, 0, 0, 0, 3, 3, 3, 3, 0, base, 0x3d);
		renderCube(renderer, isInv, x, y, z, 0.8125F, 0, 0, 3, 3, 3, 3, 0, base, 0x3d);
		renderCube(renderer, isInv, x, y, z, 0, 0, 0.8125F, 3, 3, 3, 3, 0, base, 0x3d);
		renderCube(renderer, isInv, x, y, z, 0.8125F, 0, 0.8125F, 3, 3, 3, 3, 0, base, 0x3d);
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId) {
		return true;
	}

	@Override
	public int getRenderId() {
		return SiliconProxy.laserTableModel;
	}
}

