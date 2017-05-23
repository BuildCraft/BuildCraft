/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.fluid;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;

import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.BCLibProxy;

public class FluidManager {
    public static <F extends BCFluid> F register(F fluid) {
        return register(fluid, false);
    }

    public static <F extends BCFluid> F register(F fluid, boolean force) {
        if (force || true) {// TODO: Replace with config check for registration
            FluidRegistry.registerFluid(fluid);

            Material material = new BCMaterialFluid(fluid.getMapColour(), fluid.isFlammable());
            Block block = new BCFluidBlock(fluid, material);
            block.setRegistryName(Loader.instance().activeModContainer().getModId(), "fluid_block_" + fluid.getBlockName());
            block.setUnlocalizedName("blockFluid_" + fluid.getBlockName());
            block.setLightOpacity(fluid.getLightOpacity());
            GameRegistry.register(block);
            fluid.setBlock(block);
            FluidRegistry.addBucketForFluid(fluid);
            BCLibProxy.getProxy().postRegisterFluid(fluid);
            return fluid;
        }
        return null;
    }

    @SideOnly(Side.CLIENT)
    public static void postRegisterFluid(BCFluid fluid) {
        ModelResourceLocation modelLocation = new ModelResourceLocation(Loader.instance().activeModContainer().getModId() + ":fluid_" + fluid.getName());
        Item item = ItemBlock.getItemFromBlock(fluid.getBlock());
        ModelBakery.registerItemVariants(item);
        ModelLoader.setCustomMeshDefinition(item, stack -> modelLocation);
        ModelLoader.setCustomStateMapper(fluid.getBlock(), new StateMap.Builder().ignore(BlockFluidBase.LEVEL).build());
    }
}
