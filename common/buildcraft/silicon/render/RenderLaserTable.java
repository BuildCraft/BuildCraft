package buildcraft.silicon.render;

import buildcraft.core.render.BCSimpleBlockRenderingHandler;
import org.lwjgl.opengl.GL11;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;

import buildcraft.core.lib.block.BlockBuildCraft;
import buildcraft.core.lib.render.FakeBlock;
import buildcraft.core.lib.render.RenderUtils;
import buildcraft.core.lib.render.SubIcon;
import buildcraft.silicon.SiliconProxy;

public class RenderLaserTable extends BCSimpleBlockRenderingHandler {

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
		GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
		BlockBuildCraft bcBlock = (BlockBuildCraft) block;
		switch (metadata) {
			case 0:
				renderAssemblyTable(renderer, true, 0, 0, 0, bcBlock);
				break;
			case 1:
				renderAdvancedCraftingTable(renderer, true, 0, 0, 0, bcBlock);
				break;
			case 2:
				renderIntegrationTable(renderer, true, 0, 0, 0, bcBlock);
				break;
			case 3:
				renderChargingTable(renderer, true, 0, 0, 0, bcBlock);
				break;
			case 4:
				renderProgrammingTable(renderer, true, 0, 0, 0, bcBlock);
				break;
			case 5:
				renderStampingTable(renderer, true, 0, 0, 0, bcBlock);
				break;
		}
		GL11.glTranslatef(0.5F, 0.5F, 0.5F);
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
			case 2:
				renderIntegrationTable(renderer, false, x, y, z, bcBlock);
				break;
			case 3:
				renderChargingTable(renderer, false, x, y, z, bcBlock);
				break;
			case 4:
				renderProgrammingTable(renderer, false, x, y, z, bcBlock);
				break;
			case 5:
				renderStampingTable(renderer, false, x, y, z, bcBlock);
				break;
			default:
				fixEmptyAlphaPass(x, y, z);
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
		icons[0] = new SubIcon(base, topX + w - xI, topY - zI, 16, 16, 64);
		icons[1] = new SubIcon(base, topX - xI, topY - zI, 16, 16, 64);
		icons[2] = new SubIcon(base, topX - xI, topY + d - yI, 16, 16, 64);
		icons[3] = new SubIcon(base, topX + w + d - xI, topY + d - yI, 16, 16, 64);
		icons[4] = new SubIcon(base, topX - d - zI, topY + d - yI, 16, 16, 64);
		icons[5] = new SubIcon(base, topX + w - zI, topY + d - yI, 16, 16, 64);

		renderer.setRenderBounds(xB, yB, zB, xB + (w / 16.0F), yB + (h / 16.0F), zB + (d / 16.0F));
		if (isInventory) {
			RenderUtils.drawBlockItem(renderer, Tessellator.instance, block, 0);
		} else {
			renderer.renderStandardBlockWithColorMultiplier(block, xPos, yPos, zPos, 1.0f, 1.0f, 1.0f);
		}
		block.getTextureState().pushArray();
		block.setRenderMask(0x3F);
	}

	private void renderAssemblyTable(RenderBlocks renderer, boolean isInv, int x, int y, int z, BlockBuildCraft block) {
		if (!isInv && block.getCurrentRenderPass() != 0) {
			fixEmptyAlphaPass(x, y, z);
			return;
		}
		IIcon base = block.getIcon(0, 0);
		renderCube(renderer, isInv, x, y, z, 0, 0, 0, 16, 2, 16, 16, 21, base, 0x3f); // bottom
		renderCube(renderer, isInv, x, y, z, 0.0625F, 0.125F, 0.0625F, 14, 1, 14, 18, 39, base, 0x3c); // middle (no top/bottom rendered)
		renderCube(renderer, isInv, x, y, z, 0, 0.1875F, 0, 16, 5, 16, 16, 0, base, 0x3f); // top
	}

	private void renderChargingTable(RenderBlocks renderer, boolean isInv, int x, int y, int z, BlockBuildCraft block) {
		if (!isInv && block.getCurrentRenderPass() != 0) {
			fixEmptyAlphaPass(x, y, z);
			return;
		}
		IIcon base = block.getIcon(0, 3);
		renderCube(renderer, isInv, x, y, z, 0.0625F, 0, 0.0625F, 14, 5, 14, 14, 19, base, 0x3d); // bottom (no top)

		// sides (no top)
		renderCube(renderer, isInv, x, y, z, 0.8125F, 0, 0, 3, 5, 3, 3, 6, base, 0x3d);
		renderCube(renderer, isInv, x, y, z, 0.8125F, 0, 0.8125F, 3, 5, 3, 3, 6, base, 0x3d);
		renderCube(renderer, isInv, x, y, z, 0, 0, 0.8125F, 3, 5, 3, 3, 6, base, 0x3d);
		renderCube(renderer, isInv, x, y, z, 0, 0, 0, 3, 5, 3, 3, 6, base, 0x3d);

		renderCube(renderer, isInv, x, y, z, 0, 0.3125F, 0, 16, 3, 16, 16, 0, base, 0x3f); // top
	}

	private void renderProgrammingTable(RenderBlocks renderer, boolean isInv, int x, int y, int z, BlockBuildCraft block) {
		IIcon base = block.getIcon(0, 4);
		if (block.getCurrentRenderPass() != 0) {
			renderCube(renderer, isInv, x, y, z, 0.25F, 0.375F, 0.25F, 8, 2, 8, 8, 48, base, 0x02); // semitransparent view
			if (!isInv) {
				return;
			}
		}
		renderCube(renderer, isInv, x, y, z, 0.0625F, 0, 0.0625F, 14, 3, 14, 14, 23, base, 0x3f); // bottom (no top)

		// sides (no top)
		renderCube(renderer, isInv, x, y, z, 0, 0, 0, 4, 3, 4, 4, 0, base, 0x3d);
		renderCube(renderer, isInv, x, y, z, 0.75F, 0, 0, 4, 3, 4, 4, 0, base, 0x3d);
		renderCube(renderer, isInv, x, y, z, 0, 0, 0.75F, 4, 3, 4, 4, 0, base, 0x3d);
		renderCube(renderer, isInv, x, y, z, 0.75F, 0, 0.75F, 4, 3, 4, 4, 0, base, 0x3d);

		// sides (top)
		renderCube(renderer, isInv, x, y, z, 0, 0.1875F, 0, 4, 5, 16, 16, 2, base, 0x3e);
		renderCube(renderer, isInv, x, y, z, 0.25F, 0.1875F, 0.75F, 8, 5, 4, 28, 9, base, 0x0e);

		renderCube(renderer, isInv, x, y, z, 0.3125F, 0.1875F, 0.3125F, 6, 2, 6, 6, 40, base, 0x3e); // green inside

		renderCube(renderer, isInv, x, y, z, 0.25F, 0.1875F, 0, 8, 5, 4, 28, 0, base, 0x0e);
		renderCube(renderer, isInv, x, y, z, 0.75F, 0.1875F, 0, 4, 5, 16, 40, 43, base, 0x3e);

	}

	private void renderIntegrationTable(RenderBlocks renderer, boolean isInv, int x, int y, int z, BlockBuildCraft block) {
		IIcon base = block.getIcon(0, 2);
		if (!isInv && block.getCurrentRenderPass() != 0) {
			fixEmptyAlphaPass(x, y, z);
			return;
		}
		renderCube(renderer, isInv, x, y, z, 0, 0, 0, 16, 1, 16, 16, 21, base, 0x3f); // black bottom

		// sides (no top)
		renderCube(renderer, isInv, x, y, z, 0.0625F, 0.0625F, 0.0625F, 4, 2, 4, 4, 0, base, 0x3c);
		renderCube(renderer, isInv, x, y, z, 0.6875F, 0.0625F, 0.0625F, 4, 2, 4, 4, 0, base, 0x3c);
		renderCube(renderer, isInv, x, y, z, 0.0625F, 0.0625F, 0.6875F, 4, 2, 4, 4, 0, base, 0x3c);
		renderCube(renderer, isInv, x, y, z, 0.6875F, 0.0625F, 0.6875F, 4, 2, 4, 4, 0, base, 0x3c);

		// sides (top)
		renderCube(renderer, isInv, x, y, z, 0, 0.1875F, 0, 5, 5, 16, 16, 0, base, 0x3f);
		renderCube(renderer, isInv, x, y, z, 0.3125F, 0.1875F, 0.6875F, 6, 5, 5, 47, 10, base, 0x0f);

		renderCube(renderer, isInv, x, y, z, 0.3125F, 0.1875F, 0.3125F, 6, 4, 6, 6, 38, base, 0x3f); // yellow inside

		renderCube(renderer, isInv, x, y, z, 0.3125F, 0.1875F, 0, 6, 5, 5, 47, 0, base, 0x0f);
		renderCube(renderer, isInv, x, y, z, 0.6875F, 0.1875F, 0, 5, 5, 16, 38, 43, base, 0x3f);
	}

	private void renderAdvancedCraftingTable(RenderBlocks renderer, boolean isInv, int x, int y, int z, BlockBuildCraft block) {
		if (!isInv && block.getCurrentRenderPass() != 0) {
			fixEmptyAlphaPass(x, y, z);
			return;
		}
		IIcon base = block.getIcon(0, 1);
		renderCube(renderer, isInv, x, y, z, 0.125F, 0, 0.125F, 12, 3, 12, 12, 21, base, 0x3d); // bottom (no top)

		// sides (no top)
		renderCube(renderer, isInv, x, y, z, 0, 0, 0, 3, 3, 3, 3, 0, base, 0x3d);
		renderCube(renderer, isInv, x, y, z, 0, 0, 0.8125F, 3, 3, 3, 3, 0, base, 0x3d);
		renderCube(renderer, isInv, x, y, z, 0.8125F, 0, 0, 3, 3, 3, 3, 0, base, 0x3d);
		renderCube(renderer, isInv, x, y, z, 0.8125F, 0, 0.8125F, 3, 3, 3, 3, 0, base, 0x3d);

		renderCube(renderer, isInv, x, y, z, 0, 0.1875F, 0, 16, 5, 16, 16, 0, base, 0x3f); // top
	}

	private void renderStampingTable(RenderBlocks renderer, boolean isInv, int x, int y, int z, BlockBuildCraft block) {
		if (!isInv && block.getCurrentRenderPass() != 0) {
			fixEmptyAlphaPass(x, y, z);
			return;
		}
		IIcon base = block.getIcon(0, 5);
		renderCube(renderer, isInv, x, y, z, 0.125F, 0, 0.125F, 12, 3, 12, 12, 21, base, 0x3d); // bottom (no top)

		// sides (no top)
		renderCube(renderer, isInv, x, y, z, 0, 0, 0, 3, 3, 3, 3, 0, base, 0x3d);
		renderCube(renderer, isInv, x, y, z, 0, 0, 0.8125F, 3, 3, 3, 3, 0, base, 0x3d);
		renderCube(renderer, isInv, x, y, z, 0.8125F, 0, 0, 3, 3, 3, 3, 0, base, 0x3d);
		renderCube(renderer, isInv, x, y, z, 0.8125F, 0, 0.8125F, 3, 3, 3, 3, 0, base, 0x3d);

		renderCube(renderer, isInv, x, y, z, 0, 0.1875F, 0, 16, 5, 16, 16, 0, base, 0x3f); // top
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

