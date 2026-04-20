/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.factory.tile;

import java.util.List;

import ct.buildcraft.api.core.EnumPipePart;
import ct.buildcraft.api.tiles.IDebuggable;
import ct.buildcraft.lib.fluid.Tank;
import ct.buildcraft.lib.fluid.TankManager;
import ct.buildcraft.lib.misc.CapUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidType;

public class TileAutoWorkbenchFluids extends TileAutoWorkbenchBase implements MenuProvider, IDebuggable {
    private final Tank tank1 = new Tank("tank1", FluidType.BUCKET_VOLUME * 6, this);
    private final Tank tank2 = new Tank("tank2", FluidType.BUCKET_VOLUME * 6, this);
    
    protected TankManager tankManager = new TankManager();

    public TileAutoWorkbenchFluids(BlockPos pos, BlockState state) {
        super(null, pos, state, 2, 2);
        tankManager.addAll(tank1, tank2);
        caps.addCapabilityInstance(CapUtil.CAP_FLUIDS, tankManager, EnumPipePart.CENTER);
        caps.addCapabilityInstance(CapUtil.CAP_FLUIDS, tank1, EnumPipePart.DOWN, EnumPipePart.NORTH, EnumPipePart.WEST);
        caps.addCapabilityInstance(CapUtil.CAP_FLUIDS, tank2, EnumPipePart.UP, EnumPipePart.SOUTH, EnumPipePart.EAST);
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        left.add("Tanks:");
        left.add("  " + tank1.getContentsString());
        left.add("  " + tank2.getContentsString());
    }
    

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
		return null;//new MenuAutoWorkbenchItems(id, inventory, crafting, ContainerLevelAccess.create(level, worldPosition));
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(this.getBlockState().getBlock().getDescriptionId());
	}

}
