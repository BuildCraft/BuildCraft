/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Function;

public class RegistryUtil {
    public static boolean isRegistered(Block block) {
        return ForgeRegistries.BLOCKS.containsValue(block);
    }

    public static boolean isRegistered(Item item) {
        return ForgeRegistries.ITEMS.containsValue(item);
    }

    @OnlyIn(Dist.CLIENT)
    public static <T extends TileEntity> void regTesrIfTilePresent(RegistryObject<TileEntityType<T>> tileReg, Function<? super TileEntityRendererDispatcher, ? extends TileEntityRenderer<T>> tesrConstructor) {
        if (tileReg != null && tileReg.isPresent()) {
            ClientRegistry.bindTileEntityRenderer(tileReg.get(), tesrConstructor);
        }
    }
}
