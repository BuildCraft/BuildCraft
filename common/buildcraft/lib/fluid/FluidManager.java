/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.fluid;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.block.statemap.StateMap;

import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.BCLibProxy;
import buildcraft.lib.registry.RegistrationHelper;

public class FluidManager {

    private static final RegistrationHelper HELPER = new RegistrationHelper();

    /** Should only ever be called during pre-init */
    public static <F extends BCFluid> F register(F fluid) {

        if (!Loader.instance().isInState(LoaderState.PREINITIALIZATION)) {
            throw new IllegalStateException("Can only call this during pre-init!");
        }

        FluidRegistry.registerFluid(fluid);

        Material material = new BCMaterialFluid(fluid.getMapColour(), fluid.isFlammable());
        BCFluidBlock block = new BCFluidBlock(fluid, material);
        block.setRegistryName(Loader.instance().activeModContainer().getModId(), "fluid_block_" + fluid.getBlockName());
        block.setUnlocalizedName("blockFluid_" + fluid.getBlockName());
        block.setLightOpacity(fluid.getLightOpacity());
        HELPER.addForcedBlock(block);
        fluid.setBlock(block);
        FluidRegistry.addBucketForFluid(fluid);
        BCLibProxy.getProxy().postRegisterFluid(fluid);
        return fluid;
    }

    @SideOnly(Side.CLIENT)
    public static void postRegisterFluid(BCFluid fluid) {
        ModelLoader.setCustomStateMapper(fluid.getBlock(), new StateMap.Builder().ignore(BlockFluidBase.LEVEL).build());
    }
}
