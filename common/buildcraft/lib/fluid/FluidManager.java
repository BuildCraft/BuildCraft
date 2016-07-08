package buildcraft.lib.fluid;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.UniversalBucket;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class FluidManager {
    private static List<BCFluid> registeredFluids = new ArrayList<>();

    public static <F extends BCFluid> F register(F fluid) {
        return register(fluid, false);
    }

    public static <F extends BCFluid> F register(F fluid, boolean force) {
        if (!registeredFluids.contains(fluid) || force) {
            registeredFluids.add(fluid);
            FluidRegistry.registerFluid(fluid);

            Material material = new MaterialLiquid(fluid.getMapColor());
            Block block = new BCFluidBlock(fluid, material);
            block.setRegistryName(Loader.instance().activeModContainer().getModId(), "fluid_block_" + fluid.getName());
            block.setUnlocalizedName("blockFluid_" + fluid.getName());
            GameRegistry.register(block);
            GameRegistry.register(new ItemBlock(block).setRegistryName(block.getRegistryName()));
            fluid.setBlock(block);
            return fluid;
        }
        return null;
    }

    @SideOnly(Side.CLIENT)
    public static void fmlPreInitClient() {
        for (BCFluid fluid : registeredFluids) {
            ModelResourceLocation modelLocation = new ModelResourceLocation(Loader.instance().activeModContainer().getModId() + ":fluids", fluid.getName());
            Item item = ItemBlock.getItemFromBlock(fluid.getBlock());
            ModelBakery.registerItemVariants(item);
            ModelLoader.setCustomMeshDefinition(item, stack -> modelLocation);
            ModelLoader.setCustomStateMapper(fluid.getBlock(), new StateMapperBase() {
                @Override
                protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
                    return modelLocation;
                }
            });
            FluidRegistry.addBucketForFluid(fluid);
        }
    }
}
