package buildcraft.core.block;

import net.minecraft.block.material.Material;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;

import buildcraft.core.lib.block.BlockBuildCraft;

// TODO (CHECK) Is this class required?
public abstract class BlockLEDHatchBase extends BlockBuildCraft {
    // private TextureAtlasSprite[] led;
    // private TextureAtlasSprite itemHatch;

    protected BlockLEDHatchBase(Material material) {
        super(material, FACING_PROP);

        setPassCount(4);
    }

    @Override
    public int getLightValue(IBlockAccess world, BlockPos pos) {
        return 1;
    }

    // @Override
    // public void registerBlockIcons(TextureAtlasSpriteRegister register) {
    // super.registerBlockIcons(register);
    // String base = ResourceUtils.getObjectPrefix(Block.blockRegistry.getNameForObject(this));
    // led = new TextureAtlasSprite[] { register.registerIcon(base + "/led_red"), register.registerIcon(base +
    // "/led_green") };
    // itemHatch = register.registerIcon(base + "/item_hatch");
    // }

    // @Override
    // public TextureAtlasSprite getIcon(IBlockAccess access, BlockPos pos, int side) {
    // //The quarry's pipe connection method has no idea about "sides".
    // if (renderPass == 1) {
    // return Utils.isPipeConnected(access, pos, EnumFacing.getOrientation(side), IPipeTile.PipeType.ITEM) ? itemHatch
    // : BuildCraftCore.transparentTexture;
    // } else {
    // return super.getIcon(access, pos, side);
    // }
    // }

    // @Override
    // public TextureAtlasSprite getIconAbsolute(IBlockAccess access, BlockPos pos, int side, int meta) {
    // if (renderPass == 0) {
    // return super.getIconAbsolute(access, pos, side, meta);
    // } else if (renderPass == 1) {
    // return null;
    // } else {
    // return side == 2 ? led[renderPass - 2] : null;
    // }
    // }
    //
    // @Override
    // public TextureAtlasSprite getIconAbsolute(int side, int meta) {
    // if (renderPass == 0) {
    // return super.getIconAbsolute(side, meta);
    // } else if (renderPass == 1) {
    // return side == 1 ? itemHatch : null;
    // } else {
    // return side == 2 ? led[renderPass - 2] : null;
    // }
    // }
    //
    // @Override
    // public boolean renderAsNormalBlock() {
    // return false;
    // }
}
