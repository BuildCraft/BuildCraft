package buildcraft.transport.pipe.behaviour;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjRedstoneReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.pipe.*;
import buildcraft.api.transport.pipe.IPipeHolder.PipeMessageReceiver;

import buildcraft.lib.misc.*;

public class PipeBehaviourStripes extends PipeBehaviour implements IStripesActivator, IMjRedstoneReceiver {
    private final MjBattery mjBattery = new MjBattery(2 * MjAPI.MJ);

    @Nullable
    protected EnumFacing currentDir = null;

    public PipeBehaviourStripes(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourStripes(IPipe pipe, NBTTagCompound nbt) {
        super(pipe, nbt);
        mjBattery.deserializeNBT(nbt.getCompoundTag("mjBattery"));
        setCurrentDir(NBTUtilBC.readEnum(nbt.getTag("currentDir"), EnumFacing.class));
    }

    @Override
    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = super.writeToNbt();
        nbt.setTag("mjBattery", mjBattery.serializeNBT());
        nbt.setTag("currentDir", NBTUtilBC.writeEnum(getCurrentDir()));
        return nbt;
    }

    @Override
    public void readPayload(PacketBuffer buffer, Side side, MessageContext ctx) {
        super.readPayload(buffer, side, ctx);
        currentDir = MessageUtil.readEnumOrNull(buffer, EnumFacing.class);
    }

    @Override
    public void writePayload(PacketBuffer buffer, Side side) {
        super.writePayload(buffer, side);
        MessageUtil.writeEnumOrNull(buffer, currentDir);
    }

    // Sides

    @Nullable
    public EnumFacing getCurrentDir() {
        return currentDir;
    }

    protected void setCurrentDir(@Nullable EnumFacing setTo) {
        if (currentDir != setTo) {
            this.currentDir = setTo;
            if (!pipe.getHolder().getPipeWorld().isRemote) {
                pipe.getHolder().scheduleNetworkUpdate(PipeMessageReceiver.BEHAVIOUR);
            }
        }
    }

    // IMjRedstoneReceiver

    @Override
    public boolean canConnect(IMjConnector other) {
        return true;
    }

    @Override
    public long getPowerRequested() {
        return MjAPI.MJ;
    }

    @Override
    public long receivePower(long microJoules, boolean simulate) {
        if (simulate) {
            return mjBattery.isFull() ? microJoules : 0;
        } else {
            return mjBattery.addPowerChecking(microJoules);
        }
    }

    // Stripes

    @Override
    public void onTick() {
        if (pipe.getHolder().getPipeWorld().isRemote) {
            return;
        }
        int sides = 0;
        EnumFacing dir = null;
        for (EnumFacing face : EnumFacing.VALUES) {
            if (pipe.isConnected(face)) {
                sides++;
                dir = face;
            }
        }
        if (sides == 1 && dir != null) {
            setCurrentDir(dir.getOpposite());
        } else {
            setCurrentDir(null);
        }

        mjBattery.tick(pipe.getHolder().getPipeWorld(), pipe.getHolder().getPipePos());
        long potential = mjBattery.getStored();
        if (potential > 0) {
            // TODO: Break the block!
        }
    }

    @PipeEventHandler
    public void onDrop(PipeEventItem.Drop event) {
        if (currentDir == null) {
            return;
        }
        IPipeHolder holder = pipe.getHolder();
        World world = holder.getPipeWorld();
        BlockPos pos = holder.getPipePos();
        FakePlayer player = FakePlayerUtil.INSTANCE.getFakePlayer((WorldServer) world, pos, holder.getOwner());
        player.inventory.clear();
        player.setHeldItem(EnumHand.MAIN_HAND, event.getStack());
        if (PipeApi.stripeRegistry.handleItem(world, pos, currentDir, event.getStack(), player, this)) {
            event.setStack(StackUtil.EMPTY);
        }
    }

    @Override
    public void dropItem(ItemStack stack, EnumFacing direction) {
        InventoryUtil.drop(pipe.getHolder().getPipeWorld(), pipe.getHolder().getPipePos(), stack);
    }

    @Override
    public boolean sendItem(ItemStack itemStack, EnumFacing from) {
        PipeFlow flow = pipe.getFlow();
        if (flow instanceof IFlowItems) {
            ((IFlowItems) flow).insertItemsForce(itemStack, from, null, 0.02);
            return true;
        } else {
            return false;
        }
    }
}
