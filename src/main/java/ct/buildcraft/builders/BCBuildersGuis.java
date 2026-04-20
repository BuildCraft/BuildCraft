/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package ct.buildcraft.builders;

import ct.buildcraft.builders.gui.GuiArchitectTable;
import ct.buildcraft.builders.gui.GuiBuilder;
import ct.buildcraft.builders.gui.GuiElectronicLibrary;
import ct.buildcraft.builders.gui.GuiFiller;
import ct.buildcraft.builders.gui.MenuFiller;
import ct.buildcraft.builders.gui.MenuReplacer;
import ct.buildcraft.builders.gui.ScreenFiller;
import ct.buildcraft.builders.gui.ScreenReplacer;
import ct.buildcraft.builders.menu.ContainerArchitectTable;
import ct.buildcraft.builders.menu.ContainerBuilder;
import ct.buildcraft.builders.menu.ContainerElectronicLibrary;
import ct.buildcraft.builders.menu.ContainerFiller;
import ct.buildcraft.lib.gui.BCContainerFactory;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BCBuildersGuis {
	public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, BCBuilders.MODID);
    public static final RegistryObject<MenuType<ContainerArchitectTable>> MENU_ARCHITECT_TABLE = MENUS.register("architect_menu", () -> BCContainerFactory.create(ContainerArchitectTable::new));
    public static final RegistryObject<MenuType<ContainerBuilder>> MENU_BUILDER = MENUS.register("builder_menu", () -> BCContainerFactory.create(ContainerBuilder::new));
    public static final RegistryObject<MenuType<ContainerElectronicLibrary>> MENU_ELIBRARY = MENUS.register("elibrary_menu", () -> BCContainerFactory.create(ContainerElectronicLibrary::new));
    public static final RegistryObject<MenuType<ContainerFiller>> MENU_FILLER = MENUS.register("filler_menu", () -> BCContainerFactory.create(ContainerFiller::new));
    public static final RegistryObject<MenuType<MenuReplacer>> MENU_REPLACER = MENUS.register("replacer_menu", () -> new MenuType<>(MenuReplacer::new));


    public static void clientInit(FMLClientSetupEvent event) {
        event.enqueueWork(
                () -> {
                	MenuScreens.register(MENU_ARCHITECT_TABLE.get(), GuiArchitectTable::new);
                	MenuScreens.register(MENU_BUILDER.get(), GuiBuilder::new);
                	MenuScreens.register(MENU_ELIBRARY.get(), GuiElectronicLibrary::new);
                	MenuScreens.register(MENU_FILLER.get(), GuiFiller::new);
                	MenuScreens.register(MENU_REPLACER.get(), ScreenReplacer::new);
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
