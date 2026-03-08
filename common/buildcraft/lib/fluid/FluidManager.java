/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.fluid;

@Deprecated()
public class FluidManager {

//    private static final RegistrationHelper HELPER = new RegistrationHelper();
//    private static final List<BCFluidBlock> fluidBlocks = new ArrayList<>();

//    /** Should only ever be called during pre-init */
//    public static <F extends BCFluid> F register(F fluid) {
//
//        if (!Loader.instance().isInState(LoaderState.PREINITIALIZATION)) {
//            throw new IllegalStateException("Can only call this during pre-init!");
//        }
//
//        FluidRegistry.registerFluid(fluid);
//
//        Material material = new BCMaterialFluid(fluid.getMapColour(), fluid.isFlammable());
//        BCFluidBlock block = new BCFluidBlock(fluid, material);
//        block.setRegistryName(Loader.instance().activeModContainer().getModId(), "fluid_block_" + fluid.getBlockName());
//        block.setUnlocalizedName("blockFluid_" + fluid.getBlockName());
//        block.setLightOpacity(fluid.getLightOpacity());
//        HELPER.addForcedBlock(block);
//        fluid.setBlock(block);
//        FluidRegistry.addBucketForFluid(fluid);
//        fluidBlocks.add(block);
//        return fluid;
//    }

//    @SubscribeEvent
//    @SideOnly(Side.CLIENT)
//    public static void onModelBake(ModelBakeEvent event) {
//        for (BCFluidBlock fluid : fluidBlocks) {
//            event.getModelManager().getBlockModelShapes().registerBlockWithStateMapper(fluid,
//                new StateMap.IBuilder().ignore(BlockFluidBase.LEVEL).build());
//        }
//    }
}
