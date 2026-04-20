/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package ct.buildcraft.core.block;

import ct.buildcraft.api.enums.EnumEngineType;
import ct.buildcraft.api.properties.BuildCraftProperties;
import ct.buildcraft.core.BCCoreItems;
import ct.buildcraft.lib.engine.BlockEngineBase_BC8;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.HitResult;

public class BlockEngine_BC8 extends BlockEngineBase_BC8<EnumEngineType> {
	private static final EnumProperty<EnumEngineType> TYPE = BuildCraftProperties.ENGINE_TYPE;
    public BlockEngine_BC8(Properties material) {
        super(material);
        this.registerDefaultState(
        		this.stateDefinition.any()
        		.setValue(TYPE, EnumEngineType.WOOD));
    }
    
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> bs) {
		bs.add(TYPE);
		super.createBlockStateDefinition(bs);
	}
	
	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos,
			Player player) {
		return new ItemStack(BCCoreItems.ENGINE_ITEM_MAP.get(state.getValue(getEngineProperty())));
	}

	@Override
	public Property<EnumEngineType> getEngineProperty() {
		return BuildCraftProperties.ENGINE_TYPE;
	}
	
	@Override
    public String getUnlocalizedName(EnumEngineType engine) {
        return "block.engine.bc." + engine.unlocalizedTag;
    }

	@Override
	public boolean canHarvestBlock(BlockState state, BlockGetter level, BlockPos pos, Player player) {
		return super.canHarvestBlock(state.getValue(BuildCraftProperties.ENGINE_TYPE) == EnumEngineType.WOOD ?
				Blocks.STONE.defaultBlockState() : state, level, pos, player);
	}
    

}
