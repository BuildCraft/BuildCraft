package buildcraft.core.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;

import buildcraft.core.internal.ICustomLEDBlock;
import buildcraft.core.internal.ILEDProvider;
import buildcraft.core.lib.block.BlockBuildCraft;
import buildcraft.core.lib.render.RenderEntityBlock;
import buildcraft.core.lib.utils.ResourceUtils;

public class RenderLEDTile extends TileEntitySpecialRenderer {
	private static final Map<Block, IIcon[]> iconMap = new HashMap<Block, IIcon[]>();
	private static final float Z_OFFSET = 2049 / 2048.0F;
	private final Block block;

	public RenderLEDTile(Block block) {
		iconMap.put(block, null);
		this.block = block;
	}

	public static void registerBlockIcons(IIconRegister register) {
		for (Block b : iconMap.keySet().toArray(new Block[iconMap.keySet().size()])) {
			String base = ResourceUtils.getObjectPrefix(Block.blockRegistry.getNameForObject(b));
			if (base != null) {
				List<IIcon> icons = new ArrayList<IIcon>();
				if (b instanceof ICustomLEDBlock) {
					for (String s : ((ICustomLEDBlock) b).getLEDSuffixes()) {
						icons.add(register.registerIcon(base + "/" + s));
					}
				} else {
					icons.add(register.registerIcon(base + "/led_red"));
					icons.add(register.registerIcon(base + "/led_green"));
				}

				iconMap.put(b, icons.toArray(new IIcon[icons.size()]));
			}
		}
	}

	@Override
	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float f) {
		if (!(tile instanceof ILEDProvider)) {
			return;
		}

		bindTexture(TextureMap.locationBlocksTexture);
		RenderEntityBlock.RenderInfo renderBox = new RenderEntityBlock.RenderInfo();

		ILEDProvider provider = (ILEDProvider) tile;

		GL11.glPushMatrix();
		GL11.glPushAttrib(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_ENABLE_BIT);

		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glColor3f(1, 1, 1);
		GL11.glTranslatef((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);
		GL11.glScalef(Z_OFFSET, Z_OFFSET, Z_OFFSET);
		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);

		IIcon[] icons = iconMap.get(block);

		for (int i = 0; i < icons.length; i++) {
			renderBox.light = provider.getLEDLevel(i);
			if (renderBox.light != 0) {
				renderBox.texture = icons[i];

				if (((BlockBuildCraft) block).isRotatable()) {
					renderBox.setRenderSingleSide(((BlockBuildCraft) block).getFrontSide(tile.getBlockMetadata()));
				} else {
					renderBox.renderSide[0] = false;
					renderBox.renderSide[1] = false;
					renderBox.renderSide[2] = true;
					renderBox.renderSide[3] = true;
					renderBox.renderSide[4] = true;
					renderBox.renderSide[5] = true;
				}
				RenderEntityBlock.INSTANCE.renderBlock(renderBox);
			}
		}

		GL11.glPopAttrib();
		GL11.glPopMatrix();
	}
}
