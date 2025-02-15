/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.tile;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.IFluidFilter;
import buildcraft.api.core.IFluidHandlerAdv;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.tiles.ITickable;
import buildcraft.factory.BCFactoryBlocks;
import buildcraft.lib.fluid.FluidSmoother;
import buildcraft.lib.fluid.Tank;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.misc.FluidUtilBC;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;

public class TileTank extends TileBC_Neptune implements IDebuggable, IFluidHandlerAdv, ITickable {
    public static final IdAllocator IDS = TileBC_Neptune.IDS.makeChild("tank");
    public static final int NET_FLUID_DELTA = IDS.allocId("FLUID_DELTA");

    private static final ResourceLocation ADVANCEMENT_STORE_FLUIDS = new ResourceLocation(
            "buildcraftfactory:fluid_storage"
    );

    private static boolean isPlayerInteracting = false;

    public final Tank tank;
    public final FluidSmoother smoothedTank;

    private int lastComparatorLevel;

    public TileTank(BlockPos pos, BlockState blockState) {
        this(pos, blockState, 16 * FluidType.BUCKET_VOLUME);
    }

    public TileTank(BlockPos pos, BlockState blockState, int capacity) {
        this(pos, blockState, new Tank("tank", capacity, null));
    }

    public TileTank(BlockPos pos, BlockState blockState, Tank tank) {
        super(BCFactoryBlocks.tankTile.get(), pos, blockState);
        tank.setTileEntity(this);
        this.tank = tank;
        tankManager.add(tank);
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

    @Override
    public void update() {
        ITickable.super.update();
        smoothedTank.tick(level);

        if (!level.isClientSide) {
            int compLevel = getComparatorLevel();
            if (compLevel != lastComparatorLevel) {
                lastComparatorLevel = compLevel;
//                markDirty();
                setChanged();
            }
        }
    }

    // TileEntity

    @Override
    public void onPlacedBy(LivingEntity placer, ItemStack stack) {
        super.onPlacedBy(placer, stack);
        if (!placer.level().isClientSide) {
            isPlayerInteracting = true;
            balanceTankFluids();
            isPlayerInteracting = false;
        }
    }

    /**
     * Moves fluids around to their preferred positions. (For gaseous fluids this will move everything as high as
     * possible, for liquid fluids this will move everything as low as possible.)
     */
    public void balanceTankFluids() {
        List<TileTank> tanks = getAllTanks();
//        FluidStack fluid = null;
        FluidStack fluid = StackUtil.EMPTY_FLUID;
        for (TileTank tile : tanks) {
            FluidStack held = tile.tank.getFluid();
//            if (held == null)
            if (held.isEmpty()) {
                continue;
            }
//            if (fluid == null)
            if (fluid.isEmpty()) {
                fluid = held;
            } else if (!fluid.isFluidEqual(held)) {
                return;
            }
        }
//        if (fluid == null)
        if (fluid.isEmpty()) {
            return;
        }
        if (fluid.getRawFluid().getFluidType().isLighterThanAir()) {
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
    public InteractionResult onActivated(Player player, InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ) {
        int amountBefore = tank.getFluidAmount();
        isPlayerInteracting = true;
        InteractionResult didChange = FluidUtilBC.onTankActivated(player, getBlockPos(), hand, this);
        isPlayerInteracting = false;
        if (didChange == InteractionResult.SUCCESS && !player.level().isClientSide && amountBefore < tank.getFluidAmount()) {
            AdvancementUtil.unlockAdvancement(player, ADVANCEMENT_STORE_FLUIDS);
        }
        return didChange;
    }

    // Networking

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Dist side) {
        super.writePayload(id, buffer, side);
        if (side == Dist.DEDICATED_SERVER) {
            if (id == NET_RENDER_DATA) {
                writePayload(NET_FLUID_DELTA, buffer, side);
            } else if (id == NET_FLUID_DELTA) {
                smoothedTank.writeInit(buffer);
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, NetworkDirection side, NetworkEvent.Context ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == NetworkDirection.PLAY_TO_CLIENT) {
            if (id == NET_RENDER_DATA) {
                readPayload(NET_FLUID_DELTA, buffer, side, ctx);
                smoothedTank.resetSmoothing(getLevel());
            } else if (id == NET_FLUID_DELTA) {
                smoothedTank.handleMessage(getLevel(), buffer);
            }
        }
    }

    // IDebuggable

    @Override
//    public void getDebugInfo(List<String> left, List<String> right, Direction side)
    public void getDebugInfo(List<Component> left, List<Component> right, Direction side) {
//        left.add("fluid = " + tank.getDebugString());
        left.add(Component.literal("fluid = " + tank.getDebugString()));
        smoothedTank.getDebugInfo(left, right, side);
    }

    // Rendering

    @OnlyIn(Dist.CLIENT)
    public FluidSmoother.FluidStackInterp getFluidForRender(float partialTicks) {
        return smoothedTank.getFluidForRender(partialTicks);
    }

    // Tank helper methods

    /**
     * Tests to see if this tank can connect to the other one, in the given direction. BuildCraft itself only calls
     * with {@link Direction#UP} or {@link Direction#DOWN}, however addons are free to call with any of the other 4
     * non-null faces. (Although an addon calling from other faces must provide some way of transferring fluids around).
     *
     * @param other The other tank.
     * @param direction The direction that the other tank is, from this tank.
     * @return True if this can connect, false otherwise.
     */
    public boolean canConnectTo(TileTank other, Direction direction) {
        return true;
    }

    /**
     * Helper for {@link #canConnectTo(TileTank, Direction)} that only returns true if both tanks can connect to each
     * other.
     *
     * @param from
     * @param to
     * @param direction The direction from the "from" tank, to the "to" tank, such that
     * {@link Objects#equals(Object, Object) Objects.equals(}{@link TileTank#getBlockPos()}
     * from.getPos()}.{@link BlockPos#relative(Direction)}  offset(direction)}, {@link TileTank#getBlockPos()
     * to.getPos()}) returns true.
     * @return True if both could connect, false otherwise.
     */
    public static boolean canTanksConnect(TileTank from, TileTank to, Direction direction) {
        return from.canConnectTo(to, direction) && to.canConnectTo(from, direction.getOpposite());
    }

    /** @return A list of all connected tanks around this block, ordered by position from bottom to top. */
    public List<TileTank> getAllTanks() {
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

    // Calen: divided into 3 methods
//    @Override
//    public IFluidTankProperties[] getTankProperties() {
//        List<TileTank> tanks = getTanks();
//        TileTank bottom = tanks.get(0);
//        TileTank top = tanks.get(tanks.size() - 1);
//        FluidStack total = bottom.tank.getFluid();
//        if (total == null) {
//            total = top.tank.getFluid();
//        }
//        int capacity = 0;
//        if (total == null) {
//            for (TileTank t : tanks) {
//                capacity += t.tank.getCapacity();
//            }
//        }
//        else {
//            total = total.copy();
//            total.setAmount(0);
//            for (TileTank t : tanks) {
//                FluidStack other = t.tank.getFluid();
//                if (other != null) {
//                    total.setAmount(total.getAmount() + other.getAmount());
//                }
//                capacity += t.tank.getCapacity();
//            }
//        }
//        return new IFluidTankProperties[]{new FluidTankProperties(total, capacity)};
//    }

    @Override
    public int getTanks() {
        return 1;
    }

    @NotNull
    @Override
    public FluidStack getFluidInTank(int tank) {
        List<TileTank> tanks = getAllTanks();
        TileTank bottom = tanks.get(0);
        TileTank top = tanks.get(tanks.size() - 1);
        FluidStack total = bottom.tank.getFluid();
        if (total.isEmpty()) {
            total = top.tank.getFluid();
        }
        if (total.isEmpty()) {
        } else {
            total = total.copy();
            total.setAmount(0);
            for (TileTank t : tanks) {
                FluidStack other = t.tank.getFluid();
                if (!other.isEmpty()) {
                    total.setAmount(total.getAmount() + other.getAmount());
                }
            }
        }
        return total;
    }

    @Override
    public int getTankCapacity(int tank) {
        List<TileTank> tanks = getAllTanks();
        int capacity = 0;
        for (TileTank t : tanks) {
            capacity += t.tank.getCapacity();
        }
        return capacity;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return true;
    }

    @Override
    public int fill(FluidStack resource, FluidAction doFill) {
        if (resource == null || resource.getAmount() <= 0) {
            return 0;
        }
        int filled = 0;
        List<TileTank> tanks = getAllTanks();
        for (TileTank t : tanks) {
            FluidStack current = t.tank.getFluid();
            if (!current.isEmpty() && !current.isFluidEqual(resource)) {
                return 0;
            }
        }
        boolean gas = resource.getRawFluid().getFluidType().isLighterThanAir();
        if (gas) {
            Collections.reverse(tanks);
        }
        resource = resource.copy();
        for (TileTank t : tanks) {
            int tankFilled = t.tank.fill(resource, doFill);
            if (tankFilled > 0) {
                if (isPlayerInteracting & doFill.execute()) {
                    t.sendNetworkUpdate(NET_RENDER_DATA);
                }
                resource.setAmount(resource.getAmount() - tankFilled);
                filled += tankFilled;
                if (resource.getAmount() == 0) {
                    break;
                }
            }
        }
        return filled;
    }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction doDrain) {
        return drain((fluid) -> true, maxDrain, doDrain);
    }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction doDrain) {
//        if (resource == null)
        if (resource.isEmpty()) {
//            return null;
            return StackUtil.EMPTY_FLUID;
        }
        return drain(resource::isFluidEqual, resource.getAmount(), doDrain);
    }

    // IFluidHandlerAdv

    @Nonnull
    @Override
    public FluidStack drain(IFluidFilter filter, int maxDrain, FluidAction doDrain) {
        if (maxDrain <= 0) {
//            return null;
            return StackUtil.EMPTY_FLUID;
        }
        List<TileTank> tanks = getAllTanks();
        boolean gas = false;
        for (TileTank tile : tanks) {
            FluidStack fluid = tile.tank.getFluid();
//            if (fluid != null)
            if (!fluid.isEmpty()) {
                gas = fluid.getRawFluid().getFluidType().isLighterThanAir();
                break;
            }
        }
        if (!gas) {
            Collections.reverse(tanks);
        }
//        FluidStack total = null;
        FluidStack total = StackUtil.EMPTY_FLUID;
        for (TileTank t : tanks) {
//            int realMax = maxDrain - (total == null ? 0 : total.getAmount());
            int realMax = maxDrain - (total.isEmpty() ? 0 : total.getAmount());
            if (realMax <= 0) {
                break;
            }
            FluidStack drained = t.tank.drain(filter, realMax, doDrain);
//            if (drained == null) continue;
            if (drained.isEmpty()) continue;
            if (isPlayerInteracting & doDrain.execute()) {
                t.sendNetworkUpdate(NET_RENDER_DATA);
            }
//            if (total == null)
            if (total.isEmpty()) {
                total = drained.copy();
                total.setAmount(0);
            }
            total.setAmount(total.getAmount() + drained.getAmount());
        }
//        return total == null ? StackUtil.EMPTY_FLUID : total;
        return total.isEmpty() ? StackUtil.EMPTY_FLUID : total;
    }
}
