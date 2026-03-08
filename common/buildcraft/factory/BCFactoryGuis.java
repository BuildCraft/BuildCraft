/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.factory;

@Deprecated()
public enum BCFactoryGuis {
    AUTO_WORKBENCH_ITEMS,
    AUTO_WORKBENCH_FLUIDS,
    CHUTE;

//    public void openGUI(PlayerEntity player) {
//        player.openGui(BCFactory.INSTANCE, ordinal(), player.getEntityWorld(), 0, 0, 0);
//    }

//    public void openGUI(PlayerEntity player, BlockPos pos) {
////        player.openGui(BCFactory.INSTANCE, ordinal(), player.getEntityWorld(), pos.getX(), pos.getY(), pos.getZ());
//        if (player instanceof ServerPlayerEntity serverPlayer) {
////            player.openMenu(state.getMenuProvider(player.level, pos));
//            if (player.level.getBlockEntity(pos) instanceof INamedContainerProvider tile) {
//                NetworkHooks.openGui(serverPlayer, tile, pos);
//            }
//        }
//    }
}
