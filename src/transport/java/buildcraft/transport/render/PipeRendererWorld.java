/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.render;

// TODO (PASS 0): Rewrite this into a block model thingy, like how the chute works
public class PipeRendererWorld {// implements ISimpleBlockRenderingHandler {
    //
    // public static int renderPass = -1;
    // public static float zFightOffset = 1F / 4096F;
    //
    // public void renderPipe(RenderBlocks renderblocks, IBlockAccess iblockaccess, TileGenericPipe tile, BlockPos pos)
    // {
    // PipeRenderState state = tile.renderState;
    // IIconProvider icons = tile.getPipeIcons();
    // FakeBlock fakeBlock = FakeBlock.INSTANCE;
    // int glassColor = tile.getPipeColor();
    //
    // if (icons == null) {
    // return;
    // }
    //
    // if (renderPass == 0 || glassColor >= 0) {
    // // Pass 0 handles the pipe texture, pass 1 handles the transparent stained glass
    // int connectivity = state.pipeConnectionMatrix.getMask();
    // float[] dim = new float[6];
    //
    // if (renderPass == 1) {
    // fakeBlock.setColor(ColorUtils.getRGBColor(glassColor));
    // }
    //
    // // render the unconnected pipe faces of the center block (if any)
    //
    // if (connectivity != 0x3f) { // note: 0x3f = 0x111111 = all sides
    // resetToCenterDimensions(dim);
    //
    // if (renderPass == 0) {
    // fakeBlock.getTextureState().setToStack(icons.getIcon(state.textureMatrix.getTextureIndex(null)));
    // } else {
    // fakeBlock.getTextureState().set(PipeIconProvider.TYPE.PipeStainedOverlay.getIcon());
    // }
    //
    // fixForRenderPass(dim);
    //
    // renderTwoWayBlock(renderblocks, fakeBlock, pos, dim, connectivity ^ 0x3f);
    // }
    //
    // // render the connecting pipe faces
    // for (int dir = 0; dir < 6; dir++) {
    // int mask = 1 << dir;
    //
    // if ((connectivity & mask) == 0) {
    // continue; // no connection towards dir
    // }
    //
    // // center piece offsets
    // resetToCenterDimensions(dim);
    //
    // // extend block towards dir as it's connected to there
    // dim[dir / 2] = dir % 2 == 0 ? 0 : CoreConstants.PIPE_MAX_POS;
    // dim[dir / 2 + 3] = dir % 2 == 0 ? CoreConstants.PIPE_MIN_POS : 1;
    //
    // // the mask points to all faces perpendicular to dir, i.e. dirs 0+1 -> mask 111100, 1+2 -> 110011, 3+5
    // // -> 001111
    // int renderMask = (3 << (dir & 0x6)) ^ 0x3f;
    //
    // fixForRenderPass(dim);
    //
    // // render sub block
    // if (renderPass == 0) {
    // fakeBlock.getTextureState().setToStack(icons.getIcon(state.textureMatrix.getTextureIndex(EnumFacing.VALUES[dir])));
    // } else {
    // fakeBlock.getTextureState().set(PipeIconProvider.TYPE.PipeStainedOverlay.getIcon());
    // }
    //
    // renderTwoWayBlock(renderblocks, fakeBlock, pos, dim, renderMask);
    //
    // // Render connecting block
    // if (Minecraft.getMinecraft().gameSettings.fancyGraphics) {
    // EnumFacing side = EnumFacing.getFront(dir);
    // int px = x + side.offsetX;
    // int py = y + side.offsetY;
    // int pz = z + side.offsetZ;
    // Block block = iblockaccess.getBlock(px, py, pz);
    // if (!(block instanceof BlockGenericPipe) && !block.isOpaqueCube()) {
    //
    // double[] blockBB;
    // if (block instanceof BlockChest) {
    // // work around what sems to be a vanilla bug?
    // blockBB = new double[] { 0, 0.0625F, 0.0625F, 0.875F, 0.9375F, 0.9375F };
    // } else {
    // block.setBlockBoundsBasedOnState(iblockaccess, px, py, pz);
    //
    // blockBB =
    // new double[] { block.getBlockBoundsMinY(), block.getBlockBoundsMinX(), block.getBlockBoundsMinZ(),
    // block.getBlockBoundsMaxY(), block.getBlockBoundsMaxX(), block.getBlockBoundsMaxZ() };
    // }
    //
    // if ((dir % 2 == 1 && blockBB[dir / 2] != 0) || (dir % 2 == 0 && blockBB[dir / 2 + 3] != 1)) {
    // resetToCenterDimensions(dim);
    //
    // if (dir % 2 == 1) {
    // dim[dir / 2] = 0;
    // dim[dir / 2 + 3] = (float) blockBB[dir / 2];
    // } else {
    // dim[dir / 2] = (float) blockBB[dir / 2 + 3];
    // dim[dir / 2 + 3] = 1;
    // }
    //
    // fixForRenderPass(dim);
    //
    // renderTwoWayBlock(renderblocks, fakeBlock, x + side.offsetX, y + side.offsetY, z + side.offsetZ, dim,
    // renderMask);
    // }
    // }
    // }
    // }
    //
    // fakeBlock.setColor(0xFFFFFF);
    // }
    //
    // renderblocks.setRenderBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    //
    // for (EnumFacing dir : EnumFacing.VALUES) {
    // if (tile.hasPipePluggable(dir)) {
    // PipePluggable p = tile.getPipePluggable(dir);
    // IPipePluggableRenderer r = p.getRenderer();
    // if (r != null) {
    // r.renderPluggable(renderblocks, tile.getPipe(), dir, p, fakeBlock, renderPass, pos);
    // }
    // }
    // }
    // }
    //
    // private void fixForRenderPass(float[] dim) {
    // if (renderPass == 1) {
    // for (int i = 0; i < 3; i++) {
    // dim[i] += zFightOffset;
    // }
    //
    // for (int i = 3; i < 6; i++) {
    // dim[i] -= zFightOffset;
    // }
    // }
    // }
    //
    // private void resetToCenterDimensions(float[] dim) {
    // for (int i = 0; i < 3; i++) {
    // dim[i] = CoreConstants.PIPE_MIN_POS;
    // }
    //
    // for (int i = 3; i < 6; i++) {
    // dim[i] = CoreConstants.PIPE_MAX_POS;
    // }
    // }
    //
    // /** Render a block with normal and inverted vertex order so back face culling doesn't have any effect. */
    // private void renderTwoWayBlock(RenderBlocks renderblocks, FakeBlock stateHost, BlockPos pos, float[] dim, int
    // mask) {
    // assert mask != 0;
    //
    // stateHost.setRenderMask(mask);
    // renderblocks.setRenderBounds(dim[2], dim[0], dim[1], dim[5], dim[3], dim[4]);
    // renderblocks.renderStandardBlock(stateHost, pos);
    // stateHost.setRenderMask((mask & 0x15) << 1 | (mask & 0x2a) >> 1); // pairwise swapped mask
    // renderblocks.setRenderBounds(dim[5], dim[3], dim[4], dim[2], dim[0], dim[1]);
    // renderblocks.renderStandardBlock(stateHost, pos);
    // stateHost.setRenderAllSides();
    // }
    //
    // @Override
    // public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {
    // // Done with a special item renderer
    // }
    //
    // @Override
    // public boolean renderWorldBlock(IBlockAccess world, BlockPos pos, Block block, int modelId, RenderBlocks
    // renderer) {
    // TileEntity tile = world.getTileEntity(pos);
    //
    // // Here to prevent Minecraft from crashing when nothing renders on a render pass
    // // (rarely in pass 0, often in pass 1)
    // // This is a 1.7 bug.
    // Tessellator.instance.addVertexWithUV(pos, 0, 0);
    // Tessellator.instance.addVertexWithUV(pos, 0, 0);
    // Tessellator.instance.addVertexWithUV(pos, 0, 0);
    // Tessellator.instance.addVertexWithUV(pos, 0, 0);
    //
    // if (tile instanceof TileGenericPipe) {
    // TileGenericPipe pipeTile = (TileGenericPipe) tile;
    // renderPipe(renderer, world, pipeTile, pos);
    // }
    //
    // return true;
    // }
    //
    // @Override
    // public boolean shouldRender3DInInventory(int modelId) {
    // return false;
    // }
    //
    // @Override
    // public int getRenderId() {
    // return TransportProxy.pipeModel;
    // }
}
