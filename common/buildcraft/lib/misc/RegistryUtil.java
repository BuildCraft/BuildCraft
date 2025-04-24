/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class RegistryUtil {
    public static boolean isRegistered(Block block) {
        return ForgeRegistries.BLOCKS.containsValue(block);
    }

    public static boolean isRegistered(Item item) {
        return ForgeRegistries.ITEMS.containsValue(item);
    }

    @OnlyIn(Dist.CLIENT)
    public static <T extends BlockEntity> void regTesrIfTilePresent(RegistryObject<BlockEntityType<T>> tileReg, BlockEntityRendererProvider<T> tesrConstructor) {
        if (tileReg != null && tileReg.isPresent()) {
            BlockEntityRenderers.register(tileReg.get(), tesrConstructor);
        }
    }
}
