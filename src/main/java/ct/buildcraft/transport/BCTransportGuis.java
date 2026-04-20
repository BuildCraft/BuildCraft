/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package ct.buildcraft.transport;

import ct.buildcraft.lib.gui.BCContainerFactory;
import ct.buildcraft.transport.container.ContainerDiamondPipe;
import ct.buildcraft.transport.container.ContainerDiamondWoodPipe;
import ct.buildcraft.transport.container.ContainerEmzuliPipe_BC8;
import ct.buildcraft.transport.container.ContainerFilteredBuffer_BC8;
import ct.buildcraft.transport.gui.GuiDiamondPipe;
import ct.buildcraft.transport.gui.GuiDiamondWoodPipe;
import ct.buildcraft.transport.gui.GuiEmzuliPipe_BC8;
import ct.buildcraft.transport.gui.GuiFilteredBuffer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.ParallelDispatchEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BCTransportGuis {
/*    FILTERED_BUFFER,
    PIPE_DIAMOND,
    ,
    PIPE_EMZULI;*/
	public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, BCTransport.MODID);
    public static final RegistryObject<MenuType<ContainerDiamondWoodPipe>> MENU_PIPE_DIAMOND_WOOD = MENUS.register("pipe_diawood_menu", () -> BCContainerFactory.create(ContainerDiamondWoodPipe::create));
    public static final RegistryObject<MenuType<ContainerDiamondPipe>> MENU_PIPE_DIAMOND = MENUS.register("pipe_diamond_menu",() -> BCContainerFactory.create(ContainerDiamondPipe::create));
    public static final RegistryObject<MenuType<ContainerFilteredBuffer_BC8>> MENU_FILTERED_BUFFER = MENUS.register("pipe_filtered_buffer", () -> BCContainerFactory.create(ContainerFilteredBuffer_BC8::new));
    public static final RegistryObject<MenuType<ContainerEmzuliPipe_BC8>> MENU_PIPE_EMZULI = MENUS.register("pipe_emzuli_menu", () -> BCContainerFactory.create(ContainerEmzuliPipe_BC8::create));


    public static void clientInit(ParallelDispatchEvent event) {
        event.enqueueWork(
                () -> {
                	MenuScreens.register(MENU_PIPE_DIAMOND_WOOD.get(), GuiDiamondWoodPipe::new);
                	MenuScreens.register(MENU_PIPE_DIAMOND.get(), GuiDiamondPipe::new);
                	MenuScreens.register(MENU_FILTERED_BUFFER.get(), GuiFilteredBuffer::new);
                	MenuScreens.register(MENU_PIPE_EMZULI.get(), GuiEmzuliPipe_BC8::new);
                }
        );
    }
    

    public void openGui(Player player) {
        openGui(player, 0, -1, 0);
    }

    public void openGui(Player player, BlockPos pos) {
        openGui(player, pos.getX(), pos.getY(), pos.getZ());
    }

    public void openGui(Player player, int x, int y, int z) {
//    	player.openMenu(null);
//        player.openGui(BCTransport, ordinal(), player.getLevel(), x, y, z);
    }
    static void preInit(IEventBus modEventBus) {
    	MENUS.register(modEventBus);
    }
}
