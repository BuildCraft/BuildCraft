package buildcraft.lib.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.block.BlockStainedGlassPane;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumActionResult;

import buildcraft.api.blocks.CustomPaintHelper;
import buildcraft.api.blocks.ICustomPaintHandler;

public class VanillaPaintHandlers {

    public static void fmlInit() {
        registerDoubleTypedHandler(Blocks.GLASS, Blocks.STAINED_GLASS, BlockStainedGlass.COLOR);
        registerDoubleTypedHandler(Blocks.GLASS_PANE, Blocks.STAINED_GLASS_PANE, BlockStainedGlassPane.COLOR);
        registerDoubleTypedHandler(Blocks.HARDENED_CLAY, Blocks.STAINED_HARDENED_CLAY, BlockColored.COLOR);
    }

    private static void registerDoubleTypedHandler(Block clear, Block dyed, IProperty<EnumDyeColor> colourProp) {
        ICustomPaintHandler handler = createDoubleTypedPainter(clear, dyed, colourProp);
        CustomPaintHelper.INSTANCE.registerHandler(clear, handler);
        CustomPaintHelper.INSTANCE.registerHandler(dyed, handler);
    }

    public static ICustomPaintHandler createDoubleTypedPainter(Block clear, Block dyed, IProperty<EnumDyeColor> colourProp) {
        return (world, pos, state, hitPos, hitSide, to) -> {
            if (state.getBlock() == clear) {
                // We are currently clear
                if (to == null) {
                    return EnumActionResult.FAIL;
                }
                IBlockState painted = dyed.getDefaultState().withProperty(colourProp, to);
                world.setBlockState(pos, painted);
                return EnumActionResult.SUCCESS;
            } else if (state.getBlock() == dyed) {
                if (to == state.getValue(colourProp)) {
                    return EnumActionResult.FAIL;
                }
                if (to == null) {
                    state = clear.getDefaultState();
                } else {
                    state = state.withProperty(colourProp, to);
                }
                world.setBlockState(pos, state);
                return EnumActionResult.SUCCESS;
            }
            return EnumActionResult.PASS;
        };
    }
}
