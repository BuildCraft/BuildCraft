/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package ct.buildcraft.energy.tile;

import java.util.List;

import javax.annotation.Nonnull;

import ct.buildcraft.api.core.EnumPipePart;
import ct.buildcraft.api.enums.EnumPowerStage;
import ct.buildcraft.api.mj.IMjConnector;
import ct.buildcraft.api.mj.MjAPI;
import ct.buildcraft.api.properties.BuildCraftProperties;
import ct.buildcraft.core.client.render.RenderEngine_BC8;
import ct.buildcraft.energy.BCEnergyBlocks;
import ct.buildcraft.energy.menu.ContainerEngineStone_BC8;
import ct.buildcraft.lib.delta.DeltaInt;
import ct.buildcraft.lib.delta.DeltaManager.EnumNetworkVisibility;
import ct.buildcraft.lib.engine.EngineConnector;
import ct.buildcraft.lib.engine.TileEngineBase_BC8;
import ct.buildcraft.lib.misc.InventoryUtil;
import ct.buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;
import ct.buildcraft.lib.tile.item.ItemHandlerSimple;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.network.NetworkHooks;

public class TileEngineStone_BC8 extends TileEngineBase_BC8 implements MenuProvider{
    private static final long MAX_OUTPUT = MjAPI.MJ;
    private static final long MIN_OUTPUT = MAX_OUTPUT / 3;
    // private static final long TARGET_OUTPUT = 0.375f;
    private static final float kp = 1f;
    private static final float ki = 0.05f;
    private static final long eLimit = (MAX_OUTPUT - MIN_OUTPUT) * 20;

    public final DeltaInt deltaFuelLeft = deltaManager.addDelta("fuel_left", EnumNetworkVisibility.GUI_ONLY);
    public final ItemHandlerSimple invFuel;

    int burnTime = 0;
    int totalBurnTime = 0;
    long esum = 0;

    private boolean isForceInserting = false;

    public TileEngineStone_BC8(BlockPos pos, BlockState state) {
    	super(BCEnergyBlocks.ENGINE_STONE_TILE_BC8.get(), pos, state);
        invFuel = itemManager.addInvHandler("fuel", 1, this::isValidFuel, EnumAccess.BOTH, EnumPipePart.VALUES).setChecker((a,b) -> ForgeHooks.getBurnTime(b,RecipeType.SMELTING)>0);
        caps.addProvider(itemManager);
    }

    
     // Actually this should be called isVaildFuel, but if i change it, there will be a bug in Eclipse202306
    private boolean isValidFuel(int slot, ItemStack stack) {
        // Always allow inserting container items if they aren't fuel
        return isForceInserting || getItemBurnTime(stack) > 0;
    }

    // BlockEntity overrides

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        burnTime = nbt.getInt("burnTime");
        totalBurnTime = nbt.getInt("totalBurnTime");
        esum = nbt.getLong("esum");
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.putInt("burnTime", burnTime);
        nbt.putInt("totalBurnTime", totalBurnTime);
        nbt.putLong("esum", esum);
    }

    @Override
    protected void onSlotChange(IItemHandlerModifiable handler, int slot, @Nonnull ItemStack before,
        @Nonnull ItemStack after) {
        if (handler == invFuel) {
            if (isForceInserting && after.isEmpty()) {
                isForceInserting = false;
            }
        }
    }

    // Engine overrides
    
    @Override
	public InteractionResult onActivated(Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
        	NetworkHooks.openScreen(((net.minecraft.server.level.ServerPlayer)player), this, worldPosition);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.SUCCESS;
	}

    
    
	@Nonnull
    @Override
    protected IMjConnector createConnector() {
        return new EngineConnector(false);
    }

    @Override
    public boolean isBurning() {
        return burnTime > 0;
    }

    @Override
    protected void engineUpdate() {
        super.engineUpdate();
        if (burnTime > 0) {
            burnTime--;
            if (getPowerStage() != EnumPowerStage.OVERHEAT) {
                // this seems wrong...
                long output = getCurrentOutput();
                currentOutput = output; // Comment out for constant power
                addPower(output);
            }
        }
    }

    @Override
    public void burn() {
        if (burnTime == 0 && isRedstonePowered) {
            burnTime = totalBurnTime = getItemBurnTime(invFuel.getStackInSlot(0));

            if (burnTime > 0) {
                deltaFuelLeft.setValue(100);
                deltaFuelLeft.addDelta(0, totalBurnTime, -100);

                ItemStack fuel = invFuel.extractItem(0, 1, false);
                ItemStack container = fuel.getItem().getCraftingRemainingItem(fuel);
                if (!container.isEmpty()) {
                    if (invFuel.getStackInSlot(0).isEmpty()) {
                        isForceInserting = false;
                        ItemStack leftover = invFuel.insert(container, false, false);
                        if (!leftover.isEmpty()) {
                            isForceInserting = true;
                            invFuel.setStackInSlot(0, leftover);
                        }
                    } else {
                        // Not good!
                        InventoryUtil.addToBestAcceptor(getLevel(), getBlockPos(), null, container);
                    }
                }
            }
        }
    }

    private static int getItemBurnTime(ItemStack itemstack) {
        return ForgeHooks.getBurnTime(itemstack, RecipeType.SMELTING);
    }

    @Override
    public long maxPowerReceived() {
        return 200 * MjAPI.MJ;
    }

    @Override
    public long maxPowerExtracted() {
        return 100 * MjAPI.MJ;
    }

    @Override
    public long getMaxPower() {
        return 1000 * MjAPI.MJ;
    }

    @Override
    public float explosionRange() {
        return 2;
    }

    @Override
    public long getCurrentOutput() {
        // double e = 0.375 * getMaxEnergy() - energy;
        // esum = MathUtils.clamp(esum + e, -eLimit, eLimit);
        // return MathUtils.clamp(e * 1 + esum * 0.05, MIN_OUTPUT, MAX_OUTPUT);

        long e = 3 * getMaxPower() / 8 - power;
        esum = clamp(esum + e, -eLimit, eLimit);
        return clamp(e + esum / 20, MIN_OUTPUT, MAX_OUTPUT);
    }

    private static long clamp(long val, long min, long max) {
        return Math.max(min, Math.min(max, val));
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        super.getDebugInfo(left, right, side);
        left.add("esum = " + MjAPI.formatMj(esum) + " M");
        long e = 3 * getMaxPower() / 8 - power;
        left.add("output = " + MjAPI.formatMj(clamp(e + esum / 20, MIN_OUTPUT, MAX_OUTPUT)) + " MJ");
        left.add("burnTime = " + burnTime);
        left.add("delta = " + deltaFuelLeft.getDynamic(0));
    }

	@Override
	public TextureAtlasSprite getTextureBack() {
		return RenderEngine_BC8.STONE_BACK;
	}

	@Override
	public TextureAtlasSprite getTextureSide() {
		return RenderEngine_BC8.STONE_SIDE;
	}


	@Override
	public AbstractContainerMenu createMenu(int id, Inventory playerIncentory, Player p_39956_) {
		return new ContainerEngineStone_BC8(id, playerIncentory, invFuel, ContainerLevelAccess.create(level, worldPosition));
	}


	@Override
	public Component getDisplayName() {
		return Component.translatable("block.buildcraftcore.engine_"+this.getBlockState().getValue(BuildCraftProperties.ENGINE_TYPE).getSerializedName());
	}
	
/*	//Container

	@Override
	public void clearContent() {
		invFuel.setStackInSlot(0, ItemStack.EMPTY);
	}


	@Override
	public int getContainerSize() {
		return 1;
	}


	@Override
	public boolean isEmpty() {
		return invFuel.isEmpty();
	}


	@Override
	public ItemStack getItem(int index) {
		return invFuel.getStackInSlot(index);
	}


	@Override
	public ItemStack removeItem(int index, int num) {
		return invFuel.;
	}


	@Override
	public ItemStack removeItemNoUpdate(int p_18951_) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void setItem(int p_18944_, ItemStack p_18945_) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public boolean stillValid(Player p_18946_) {
		// TODO Auto-generated method stub
		return false;
	}*/
}
