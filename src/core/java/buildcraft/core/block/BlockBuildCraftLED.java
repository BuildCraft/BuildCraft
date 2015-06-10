package buildcraft.core.block;

import net.minecraft.block.material.Material;

import buildcraft.core.lib.block.BlockBuildCraft;

// TODO (CHECK) Is this class required?
public abstract class BlockBuildCraftLED extends BlockBuildCraft {
    // private TextureAtlasSprite[] led;

    protected BlockBuildCraftLED(Material material) {
        super(material);
        setPassCount(3);
    }

    // @Override
    // public void registerBlockIcons(TextureAtlasSpriteRegister register) {
    // super.registerBlockIcons(register);
    // String base = ResourceUtils.getObjectPrefix(Block.blockRegistry.getNameForObject(this));
    // led = new TextureAtlasSprite[] { register.registerIcon(base + "/led_red"), register.registerIcon(base +
    // "/led_green") };
    // }

    // @Override
    // public TextureAtlasSprite getIconAbsolute(IBlockAccess access, BlockPos pos, int side, int meta) {
    // if (renderPass == 0) {
    // return super.getIconAbsolute(access, pos, side, meta);
    // } else {
    // if (isRotatable()) {
    // return side == 2 ? led[renderPass - 1] : null;
    // } else {
    // return side >= 2 ? led[renderPass - 1] : null;
    // }
    // }
    // }

    // @Override
    // public TextureAtlasSprite getIconAbsolute(int side, int meta) {
    // if (renderPass == 0) {
    // return super.getIconAbsolute(side, meta);
    // } else {
    // if (isRotatable()) {
    // return side == 2 ? led[renderPass - 1] : null;
    // } else {
    // return side >= 2 ? led[renderPass - 1] : null;
    // }
    // }
    // }

    // @Override
    // public boolean renderAsNormalBlock() {
    // return false;
    // }
}
