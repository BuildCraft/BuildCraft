/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.energy;

import ct.buildcraft.energy.menu.ContainerEngineIron_BC8;
import ct.buildcraft.energy.menu.ContainerEngineStone_BC8;
import ct.buildcraft.lib.gui.BCContainerFactory;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.RegistryObject;

public class BCEnergyGuis {

	public static final RegistryObject<MenuType<ContainerEngineStone_BC8>> MENU_STONE = BCEnergy.MENUS.register("engine_stone_menu", () -> BCContainerFactory.create(ContainerEngineStone_BC8::new));
	public static final RegistryObject<MenuType<ContainerEngineIron_BC8>> MENU_IRON = BCEnergy.MENUS.register("engine_iron_menu", () -> BCContainerFactory.create(ContainerEngineIron_BC8::new));
    
    static void init() {}

}
