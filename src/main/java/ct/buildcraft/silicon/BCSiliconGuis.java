/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package ct.buildcraft.silicon;

import ct.buildcraft.lib.gui.BCContainerFactory;
import ct.buildcraft.silicon.container.ContainerAdvancedCraftingTable;
import ct.buildcraft.silicon.container.ContainerAssemblyTable;
import ct.buildcraft.silicon.container.ContainerGate;
import ct.buildcraft.silicon.container.ContainerIntegrationTable;
import ct.buildcraft.silicon.gui.GuiAdvancedCraftingTable;
import ct.buildcraft.silicon.gui.GuiAssemblyTable;
import ct.buildcraft.silicon.gui.GuiGate;
import ct.buildcraft.silicon.gui.GuiIntegrationTable;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.ParallelDispatchEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BCSiliconGuis {
	public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, BCSilicon.MODID);
    public static final RegistryObject<MenuType<ContainerAdvancedCraftingTable>> MENU_AD_CRAFTING_TABLE = MENUS.register("advanced_crafting_table_menu", () -> BCContainerFactory.create(ContainerAdvancedCraftingTable::new));
    public static final RegistryObject<MenuType<ContainerAssemblyTable>> MENU_ASSEMBLY_TABLE = MENUS.register("assembly_table_menu", () -> BCContainerFactory.create(ContainerAssemblyTable::new));
    public static final RegistryObject<MenuType<ContainerGate>> MENU_GATE = MENUS.register("gate_menu", () -> BCContainerFactory.create(ContainerGate::creatClientMenu));
    public static final RegistryObject<MenuType<ContainerIntegrationTable>> MENU_INTEGRATION_TABLE = MENUS.register("integration_table_menu", () -> BCContainerFactory.create(ContainerIntegrationTable::new));


    public static void clientInit(ParallelDispatchEvent event) {
        event.enqueueWork(
                () -> {
                	MenuScreens.register(MENU_AD_CRAFTING_TABLE.get(), GuiAdvancedCraftingTable::new);
                	MenuScreens.register(MENU_ASSEMBLY_TABLE.get(), GuiAssemblyTable::new);
                	MenuScreens.register(MENU_GATE.get(), GuiGate::new);
                	MenuScreens.register(MENU_INTEGRATION_TABLE.get(), GuiIntegrationTable::new);
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
