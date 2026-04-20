/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.factory.tile;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import ct.buildcraft.api.core.EnumPipePart;
import ct.buildcraft.api.core.IFluidFilter;
import ct.buildcraft.api.core.IFluidHandlerAdv;
import ct.buildcraft.api.items.FluidItemDrops;
import ct.buildcraft.api.tiles.IDebuggable;
import ct.buildcraft.factory.BCFactoryBlocks;
import ct.buildcraft.lib.fluid.FluidSmoother;
import ct.buildcraft.lib.fluid.FluidSmoother.FluidStackInterp;
import ct.buildcraft.lib.fluid.Tank;
import ct.buildcraft.lib.misc.AdvancementUtil;
import ct.buildcraft.lib.misc.CapUtil;
import ct.buildcraft.lib.misc.FluidUtilBC;
import ct.buildcraft.lib.misc.data.IdAllocator;
import ct.buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

public class TileTank extends TileBC_Neptune implements IDebuggable, IFluidHandlerAdv {
    public static final int NET_FLUID_DELTA = IDS.allocId("FLUID_DELTA");

    private static final ResourceLocation ADVANCEMENT_STORE_FLUIDS = new ResourceLocation(
        "buildcraftfactory:fluid_storage"
    );

    private static boolean isPlayerInteracting = false;

    public final Tank tank;
    public final FluidSmoother smoothedTank;
    
//    protected final TankManager tankManager = new TankManager();

    private int lastComparatorLevel;

    public TileTank(BlockPos pos, BlockState state) {
        this(16 * FluidType.BUCKET_VOLUME, pos, state);
    }

    protected TileTank(int capacity, BlockPos pos, BlockState state) {
        this(new Tank("tank", capacity, null), pos, state);
    }

    protected TileTank(Tank tank, BlockPos pos, BlockState state) {
    	super(BCFactoryBlocks.ENTITYBLOCKTANK.get(), pos, state);
        tank.setBlockEntity(this);
        this.tank = tank;
        tankManager.addLast(tank);
        caps.addCapabilityInstance(CapUtil.CAP_FLUIDS, this, EnumPipePart.VALUES);
        smoothedTank = new FluidSmoother(w -> createAndSendMessage(NET_FLUID_DELTA, w), tank);
    }

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    public int getComparatorLevel() {
        int amount = tank.getFluidAmount();
        int cap = tank.getCapacity();
        return amount * 14 / cap + (amount > 0 ? 1 : 0);
    }

    // ITickable

    public void update() {
        smoothedTank.tick(level);

        if (!level.isClientSide) {
            int compLevel = getComparatorLevel();
            if (compLevel != lastComparatorLevel) {
                lastComparatorLevel = compLevel;
                setChanged();
            }
        }
    }

    // BlockEntity

    @Override
    public void onPlacedBy(LivingEntity placer, ItemStack stack) {
        super.onPlacedBy(placer, stack);
        if (!placer.level.isClientSide) {
            isPlayerInteracting = true;
            balanceTankFluids();
            isPlayerInteracting = false;
        }
    }

    /** Moves fluids around to their preferred positions. (For gaseous fluids this will move everything as high as
     * possible, for liquid fluids this will move everything as low as possible.) */
    public void balanceTankFluids() {
        List<TileTank> tanks = i_getTanks();
        FluidStack fluid = FluidStack.EMPTY;
        for (TileTank tile : tanks) {
            FluidStack held = tile.tank.getFluid();
            if (held.isEmpty()) {
                continue;
            }
            if (fluid.isEmpty()) {
                fluid = held;
            } else if (!fluid.isFluidEqual(held)) {
                return;
            }
        }
        if (fluid.isEmpty()) {
            return;
        }
        if (fluid.getFluid().getFluidType().isLighterThanAir()) {
            Collections.reverse(tanks);
        }
        TileTank prev = null;
        for (TileTank tile : tanks) {
            if (prev != null) {
                FluidUtilBC.move(tile.tank, prev.tank);
            }
            prev = tile;
        }
    }

    
    
    @Override
	public InteractionResult onActivated(Player player, InteractionHand hand, BlockHitResult hit) {
        int amountBefore = tank.getFluidAmount();
        isPlayerInteracting = true;
        boolean didChange = FluidUtilBC.onTankActivated(player, worldPosition, hand, this);
        isPlayerInteracting = false;
        if (didChange && !player.level.isClientSide && amountBefore < tank.getFluidAmount()) {
            AdvancementUtil.unlockAdvancement(player, ADVANCEMENT_STORE_FLUIDS);
        }
        return didChange ? InteractionResult.SUCCESS : InteractionResult.PASS;
	}


    // Networking

    @Override
    public void writePayload(int id, FriendlyByteBuf buffer, LogicalSide side) {
        super.writePayload(id, buffer, side);
        if (side == LogicalSide.SERVER) {
            if (id == NET_RENDER_DATA) {
                writePayload(NET_FLUID_DELTA, buffer, side);
            } else if (id == NET_FLUID_DELTA) {
                smoothedTank.writeInit(buffer);
            }
        }
    }

    @Override
    public void readPayload(int id, FriendlyByteBuf buffer, LogicalSide side, NetworkEvent.Context ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == LogicalSide.CLIENT) {
            if (id == NET_RENDER_DATA) {
                readPayload(NET_FLUID_DELTA, buffer, side, ctx);
                smoothedTank.resetSmoothing(level);
            } else if (id == NET_FLUID_DELTA) {
                smoothedTank.handleMessage(level, buffer);
            }
        }
    }

    // IDebuggable

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        left.add("fluid = " + tank.getDebugString());
        smoothedTank.getDebugInfo(left, right, side);
    }

    // Rendering

    @OnlyIn(Dist.CLIENT)
    public FluidStackInterp getFluidForRender(float partialTicks) {
        return smoothedTank.getFluidForRender(partialTicks);
    }

    // Tank helper methods

    /** Tests to see if this tank can connect to the other one, in the given direction. BuildCraft itself only calls
     * with {@link Direction#UP} or {@link Direction#DOWN}, however addons are free to call with any of the other 4
     * non-null faces. (Although an addon calling from other faces must provide some way of transferring fluids around).
     * 
     * @param other The other tank.
     * @param direction The direction that the other tank is, from this tank.
     * @return True if this can connect, false otherwise. */
    public boolean canConnectTo(TileTank other, Direction direction) {
        return true;
    }

    /** Helper for {@link #canConnectTo(TileTank, Direction)} that only returns true if both tanks can connect to each
     * other.
     * 
     * @param from
     * @param to
     * @param direction The direction from the "from" tank, to the "to" tank, such that
     *            {@link Objects#equals(Object, Object) Objects.equals(}{@link TileTank#getPos()
     *            from.getPos()}.{@link BlockPos#offset(Direction) offset(direction)}, {@link TileTank#getPos()
     *            to.getPos()}) returns true.
     * @return True if both could connect, false otherwise. */
    public static boolean canTanksConnect(TileTank from, TileTank to, Direction direction) {
        return from.canConnectTo(to, direction) && to.canConnectTo(from, direction.getOpposite());
    }

    /** @return A list of all connected tanks around this block, ordered by position from bottom to top. */
    private List<TileTank> i_getTanks() {
        // double-ended queue rather than array list to avoid
        // the copy operation when we search downwards
        Deque<TileTank> tanks = new ArrayDeque<>();
        tanks.add(this);
        TileTank prevTank = this;
        while (true) {
            BlockEntity tileAbove = prevTank.getNeighbourTile(Direction.UP);
            if (!(tileAbove instanceof TileTank)) {
                break;
            }
            TileTank tankUp = (TileTank) tileAbove;
            if (tankUp != null && canTanksConnect(prevTank, tankUp, Direction.UP)) {
                tanks.addLast(tankUp);
            } else {
                break;
            }
            prevTank = tankUp;
        }
        prevTank = this;
        while (true) {
            BlockEntity tileBelow = prevTank.getNeighbourTile(Direction.DOWN);
            if (!(tileBelow instanceof TileTank)) {
                break;
            }
            TileTank tankBelow = (TileTank) tileBelow;
            if (tankBelow != null && canTanksConnect(prevTank, tankBelow, Direction.DOWN)) {
                tanks.addFirst(tankBelow);
            } else {
                break;
            }
            prevTank = tankBelow;
        }
        return new ArrayList<>(tanks);
    }

    // IFluidHandler

/*    @Override
    public IFluidTankProperties[] getTankProperties() {
        List<TileTank> tanks = i_getTanks();
        TileTank bottom = tanks.get(0);
        TileTank top = tanks.get(tanks.size() - 1);
        FluidStack total = bottom.tank.getFluid();
        if (total == null) {
            total = top.tank.getFluid();
        }
        int capacity = 0;
        if (total == null) {
            for (TileTank t : tanks) {
                capacity += t.tank.getCapacity();
            }
        } else {
            total = total.copy();
            total.setAmount(0);
            for (TileTank t : tanks) {
                FluidStack other = t.tank.getFluid();
                if (other != null) {
                    total.amount += other.amount;
                }
                capacity += t.tank.getCapacity();
            }
        }
        return new IFluidTankProperties[] { new FluidTankProperties(total, capacity) };
    }*/
    
    public FluidStack getFluidInTank(int tank) {
        List<TileTank> tanks = i_getTanks();
        TileTank bottom = tanks.get(0);
        TileTank top = tanks.get(tanks.size() - 1);
        FluidStack total = bottom.tank.getFluid();
        if (total.isEmpty()) {
            return top.tank.getFluid();
        }
        total = total.copy();
        total.setAmount(0);
        for (TileTank t : tanks) {
        	FluidStack other = t.tank.getFluid();
        	if (other != FluidStack.EMPTY) {
        		total.grow(other.getAmount());
        	}
        }
        return total;
    }
    
    
    

    @Override
	public int getTankCapacity(int tank) {
        List<TileTank> tanks = i_getTanks();
        int capacity = 0;
        for (TileTank t : tanks) {
            capacity += t.tank.getCapacity();
        }
        return capacity;
	}

	@Override
    public int fill(FluidStack resource, FluidAction doFill) {
        if (resource.isEmpty() || resource.getAmount() <= 0) {
            return 0;
        }
        int filled = 0;
        List<TileTank> tanks = i_getTanks();
        for (TileTank t : tanks) {
            FluidStack current = t.tank.getFluid();
            if (!current.isEmpty() && !current.isFluidEqual(resource)) {
                return 0;
            }
        }
        boolean gas = resource.getFluid().getFluidType().isLighterThanAir();
        if (gas) {
            Collections.reverse(tanks);
        }
        resource = resource.copy();
        for (TileTank t : tanks) {
            int tankFilled = t.tank.fill(resource, doFill);
            if (tankFilled > 0) {
                if (isPlayerInteracting & doFill == FluidAction.EXECUTE) {
                    t.sendNetworkUpdate(NET_RENDER_DATA);
                }
                resource.shrink(tankFilled);
                filled += tankFilled;
                if (resource.getAmount() == 0) {
                    break;
                }
            }
        }
        return filled;
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction doDrain) {
        return drain((a) -> true, maxDrain, doDrain);
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction doDrain) {
        if (resource.isEmpty()) {
            return FluidStack.EMPTY;
        }
        return drain(resource::isFluidEqual, resource.getAmount(), doDrain);
    }

    // IFluidHandlerAdv

    @Override
    public FluidStack drain(IFluidFilter filter, int maxDrain, FluidAction doDrain) {
        if (maxDrain <= 0) {
            return FluidStack.EMPTY;
        }
        List<TileTank> tanks = i_getTanks();
        boolean gas = false;
        for (TileTank tile : tanks) {
            FluidStack fluid = tile.tank.getFluid();
            if (!fluid.isEmpty()) {
                gas = fluid.getFluid().getFluidType().isLighterThanAir();
                break;
            }
        }
        if (!gas) {
            Collections.reverse(tanks);
        }
        FluidStack total = FluidStack.EMPTY;
        for (TileTank t : tanks) {
            int realMax = maxDrain - (total.isEmpty() ? 0 : total.getAmount());
            if (realMax <= 0) {
                break;
            }
            FluidStack drained = t.tank.drain(filter, realMax, doDrain);
            if (drained.isEmpty()) continue;
            if (isPlayerInteracting & doDrain == FluidAction.EXECUTE) {
                t.sendNetworkUpdate(NET_RENDER_DATA);
            }
            if (total.isEmpty()) {
                total = drained.copy();
                if(total.isEmpty()) return FluidStack.EMPTY;
                total.setAmount(0);
            }
            total.grow(drained.getAmount());
        }
        return total;
    }

	@Override
	public void addDrops(NonNullList<ItemStack> toDrop, int fortune) {
		FluidItemDrops.addFluidDrops(toDrop, tank);
		super.addDrops(toDrop, fortune);
	}

	@Override
	public void saveAdditional(CompoundTag nbt) {
		super.saveAdditional(nbt);
		nbt.put("tanks", tank.serializeNBT());
	}

	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		tank.readFromNBT(nbt.getCompound("tanks"));
	}

	@Override
	public int getTanks() {
		return i_getTanks().size();
	}

	@Override
	public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
		return this.tank.isFluidValid(stack);
	}

}
