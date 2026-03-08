package buildcraft.energy.tile;

import buildcraft.api.enums.EnumPowerStage;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.mj.MjCapabilityHelper;
import buildcraft.api.tiles.IBCTileMenuProvider;
import buildcraft.api.tiles.ITickable;
import buildcraft.api.transport.pipe.IItemPipe;
import buildcraft.energy.BCEnergyBlocks;
import buildcraft.energy.BCEnergyMenuTypes;
import buildcraft.energy.container.ContainerDynamoMJ;
import buildcraft.lib.BCLibConfig;
import buildcraft.lib.block.VanillaRotationHandlers;
import buildcraft.lib.engine.IEngineLikeForLedger;
import buildcraft.lib.misc.EntityUtil;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.collect.OrderedEnumMap;
import buildcraft.lib.misc.data.ModelVariableData;
import buildcraft.lib.mj.MjBatteryReceiver;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import buildcraft.lib.tile.item.StackInsertionFunction;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Map;

public class TileDynamoMJ extends TileBC_Neptune implements ITickable, IEngineLikeForLedger, IBCTileMenuProvider {

    public static final int MAX_RF = 10_000;
    public static final long MAX_MJ = 1000 * MjAPI.MJ;

    public static final double HEAT_RATE = 0.06;
    public static final double COOLDOWN_RATE = 0.01;

    public static final double MIN_HEAT = 20;
    public static final double IDEAL_HEAT = 100;
    public static final double MAX_HEAT = 250;

    private final MjBattery mjBattery;
    private final MjBatteryReceiver mjConnector;
    private final MjCapabilityHelper mjCaps;
    private final Rf rf = new Rf();
    int currentRF;
    public final ItemHandlerSimple invUpgrades;

    protected double heat = MIN_HEAT;// TODO: sync gui data
    /** Increments from 0 to 1. Above 0.5 all of the held power is emitted. */
    private float progress, lastProgress;
    private int progressPart = 0;

    protected EnumPowerStage powerStage = EnumPowerStage.BLUE;
    protected Direction currentDirection = Direction.UP;

    public long currentOutput;// TODO: sync gui data
    public boolean isRedstonePowered = false;
    protected boolean isPumping = false;

    /** The model variables, used to keep track of the various state-based variables. */
    public final ModelVariableData clientModelData = new ModelVariableData();

    public TileDynamoMJ() {
        super(BCEnergyBlocks.mjDynamoTile.get());
        mjBattery = new MjBattery(MAX_MJ);
        mjConnector = new MjBatteryReceiver(mjBattery);
        mjCaps = new MjCapabilityHelper(mjConnector);
        invUpgrades = itemManager.addInvHandler(
                "upgrades", 4, this::isValidUpgrade, StackInsertionFunction.getInsertionFunction(1), EnumAccess.NONE
        );
    }

    // TileEngineBase_BC8

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        super.save(nbt);
        nbt.put("currentDirection", NBTUtilBC.writeEnum(currentDirection));
        nbt.putBoolean("isRedstonePowered", isRedstonePowered);
        nbt.putDouble("heat", heat);
        nbt.putFloat("progress", progress);
        nbt.putInt("progressPart", progressPart);
        nbt.putInt("currentRF", currentRF);
        nbt.put("mj", mjBattery.serializeNBT());
        return nbt;
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        super.load(state, nbt);
        currentDirection = NBTUtilBC.readEnum(nbt.get("currentDirection"), Direction.class);
        if (currentDirection == null) {
            currentDirection = Direction.UP;
        }
        isRedstonePowered = nbt.getBoolean("isRedstonePowered");
        heat = nbt.getDouble("heat");
        progress = nbt.getFloat("progress");
        progressPart = nbt.getInt("progressPart");
        currentRF = nbt.getInt("currentRF");
        mjBattery.deserializeNBT(nbt.getCompound("mj"));
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, NetworkDirection side, NetworkEvent.Context ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == NetworkDirection.PLAY_TO_CLIENT) {
            if (id == NET_RENDER_DATA) {
                isPumping = buffer.readBoolean();
                currentDirection = buffer.readEnum(Direction.class);
                powerStage = buffer.readEnum(EnumPowerStage.class);
                progress = buffer.readFloat();
            } else if (id == NET_GUI_DATA || id == NET_GUI_TICK) {
                heat = buffer.readFloat();
                currentOutput = buffer.readLong();
                currentRF = buffer.readInt();
                mjBattery.readFromBuffer(buffer);
            }
        }
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Dist side) {
        super.writePayload(id, buffer, side);
        if (side == Dist.DEDICATED_SERVER) {
            if (id == NET_RENDER_DATA) {
                buffer.writeBoolean(isPumping);
                buffer.writeEnum(currentDirection);
                buffer.writeEnum(powerStage);
                buffer.writeFloat(progress);
            } else if (id == NET_GUI_DATA || id == NET_GUI_TICK) {
                buffer.writeFloat((float) heat);
                buffer.writeLong(currentOutput);
                buffer.writeInt(currentRF);
                mjBattery.writeToBuffer(buffer);
            }
        }
    }

    public ActionResultType attemptRotation() {
        OrderedEnumMap<Direction> possible = VanillaRotationHandlers.ROTATE_FACING;
        Direction current = currentDirection;
        for (int i = 0; i < 6; i++) {
            current = possible.next(current);
            if (isFacingReceiver(current)) {
                if (currentDirection != current) {
                    currentDirection = current;
                    // makeTileCache();
                    sendNetworkUpdate(NET_RENDER_DATA);
                    redrawBlock();
                    // TODO Calen notifyNeighborsRespectDebug???
                    // world.notifyNeighborsRespectDebug(getPos(), getBlockType(), true);
                    return ActionResultType.SUCCESS;
                }
                return ActionResultType.FAIL;
            }
        }
        return ActionResultType.FAIL;
    }

    private boolean isFacingReceiver(Direction dir) {
        return getReceiverToPower(dir) != null;
    }

    protected final boolean canChain() {
        return getMaxChainLength() > 0;
    }

    /** @return The number of additional engines that this engine can send power through. */
    protected int getMaxChainLength() {
        return 3;
    }

    public void rotateIfInvalid() {
        if (currentDirection != null && isFacingReceiver(currentDirection)) {
            return;
        }
        attemptRotation();
        if (currentDirection == null) {
            currentDirection = Direction.UP;
        }
    }

    @Override
    public void onPlacedBy(LivingEntity placer, ItemStack stack) {
        super.onPlacedBy(placer, stack);
        currentDirection = null;// Force rotateIfInvalid to always attempt to rotate
        rotateIfInvalid();
    }

    protected Biome getBiome() {
        // TODO: Cache this!
        return level.getBiome(getBlockPos());
    }

    /** @return The heat of the current biome, in celsius. */
    protected float getBiomeHeat() {
        Biome biome = getBiome();
        float temp = biome.getTemperature(getBlockPos());
        return Math.max(0, Math.min(30, temp * 15f));
    }

    public double getPowerLevel() {
        return currentRF / (double) MAX_RF;
    }

    protected EnumPowerStage computePowerStage() {
        double heatLevel = getHeatLevel();
        if (heatLevel < 0.25f) return EnumPowerStage.BLUE;
        else if (heatLevel < 0.5f) return EnumPowerStage.GREEN;
        else if (heatLevel < 0.75f) return EnumPowerStage.YELLOW;
        else if (heatLevel < 0.85f) return EnumPowerStage.RED;
        else return EnumPowerStage.OVERHEAT;
    }

    @Override
    public final EnumPowerStage getPowerStage() {
        if (!level.isClientSide) {
            EnumPowerStage newStage = computePowerStage();

            if (powerStage != newStage) {
                powerStage = newStage;
                sendNetworkUpdate(NET_RENDER_DATA);
            }
        }

        return powerStage;
    }

    public double getHeatLevel() {
        return (heat - MIN_HEAT) / (MAX_HEAT - MIN_HEAT);
    }

    public double getIdealHeatLevel() {
        return heat / IDEAL_HEAT;
    }

    @Override
    public double getHeat() {
        return heat;
    }

    public double getPistonSpeed() {
        switch (getPowerStage()) {
            case BLUE:
                return 0.04;
            case GREEN:
                return 0.05;
            case YELLOW:
                return 0.06;
            case RED:
                return 0.07;
            default:
                return 0;
        }
    }

    @Override
    public void onNeighbourBlockChanged(Block block, BlockPos nehighbour) {
        super.onNeighbourBlockChanged(block, nehighbour);
        // isRedstonePowered = world.isBlockIndirectlyGettingPowered(getPos()) > 0;
        isRedstonePowered = level.hasNeighborSignal(getBlockPos());
    }

    @Override
    public void update() {
        deltaManager.tick();
        if (cannotUpdate()) return;

        boolean overheat = getPowerStage() == EnumPowerStage.OVERHEAT;

        if (level.isClientSide) {
            lastProgress = progress;

            if (isPumping) {
                progress += getPistonSpeed();

                if (progress >= 1) {
                    progress = 0;
                }
            } else if (progress > 0) {
                progress -= 0.01f;
            }
            clientModelData.tick();
            return;
        }

        if (!isRedstonePowered) {
            if (currentRF > 0) {
                currentRF--;
            }
            if (currentRF < 0) {
                currentRF = 0;
            }
        }

        updateHeatLevel();
        getPowerStage();

        IEnergyStorage receiver = getReceiverToPower(currentDirection);
        if (progressPart != 0) {
            progress += getPistonSpeed();

            if (progress > 0.5 && progressPart == 1) {
                progressPart = 2;
            } else if (progress >= 1) {
                progress = 0;
                progressPart = 0;
            }
        } else if (isRedstonePowered && isActive()) {
            if (getPowerToExtract(false) > 0) {
                progressPart = 1;
                setPumping(true);
            } else {
                setPumping(false);
            }
        } else {
            setPumping(false);
        }

        if (isRedstonePowered && isActive()) {
            sendPower(receiver);
        } else {
            currentOutput = 0;
        }

        if (!overheat) {
            burn();
        }

        markChunkDirty();
    }

    private int getPowerToExtract(boolean doExtract) {
        IEnergyStorage receiver = getReceiverToPower(currentDirection);
        if (receiver == null) {
            return 0;
        }

        return extractPower(0, receiver.getMaxEnergyStored() - receiver.getEnergyStored(), doExtract);
    }

    private void sendPower(IEnergyStorage receiver) {
        if (receiver != null) {
            int extracted = getPowerToExtract(false);
            if (extracted > 0) {
                int received = receiver.receiveEnergy(extracted, false);
                extractPower(received, received, true);
            }
        }
    }

    // Uncomment out for constant power
    // public float getActualOutput() {
    // float heatLevel = getIdealHeatLevel();
    // return getCurrentOutput() * heatLevel;
    // }

    public boolean isActive() {
        return true;
    }

    protected final void setPumping(boolean isActive) {
        if (this.isPumping == isActive) {
            return;
        }

        this.isPumping = isActive;
        sendNetworkUpdate(NET_RENDER_DATA);
    }

    public void addPower(int rf) {
        currentRF += rf;

        if (getPowerStage() == EnumPowerStage.OVERHEAT) {
            // TODO: turn engine off
            // worldObj.createExplosion(null, xCoord, yCoord, zCoord, explosionRange(), true);
            // worldObj.setBlockToAir(xCoord, yCoord, zCoord);
        }

        if (currentRF > MAX_RF) {
            currentRF = MAX_RF;
        }
    }

    public int extractPower(int min, int max, boolean doExtract) {
        if (currentRF < min) {
            return 0;
        }

        int actualMax;

        if (max > maxPowerExtracted()) {
            actualMax = maxPowerExtracted();
        } else {
            actualMax = max;
        }

        if (actualMax < min) {
            return 0;
        }

        int extracted;

        if (currentRF >= actualMax) {
            extracted = actualMax;

            if (doExtract) {
                currentRF -= actualMax;
            }
        } else {
            extracted = currentRF;

            if (doExtract) {
                currentRF = 0;
            }
        }

        return extracted;
    }

    public final boolean isPoweredTile(TileEntity tile, Direction side) {
        if (tile == null) return false;
        if (tile.getClass() == getClass()) {
            TileDynamoMJ other = (TileDynamoMJ) tile;
            return other.currentDirection == currentDirection;
        }
        return getReceiverToPower(tile, side) != null;
    }

    /** @deprecated Replaced with {@link #getReceiverToPower(Direction)}. */
    @Deprecated
    public IEnergyStorage getReceiverToPower(TileEntity tile, Direction side) {
        if (tile == null) return null;
        IEnergyStorage rec = tile.getCapability(CapabilityEnergy.ENERGY, side.getOpposite()).orElse(null);
        if (rec != null && rec.canReceive()) {
            return rec;
        } else {
            return null;
        }
    }

    public IEnergyStorage getReceiverToPower(Direction side) {
        TileDynamoMJ engine = this;
        TileEntity next = null;

        for (int len = 0; len <= getMaxChainLength(); len++) {
            next = engine.getNeighbourTile(side);

            if (next == null) {
                return null;
            }

            if (next.getClass() == getClass()) {
                if (side != ((TileDynamoMJ) next).currentDirection) {
                    return null;
                }
            }

            if (next instanceof TileDynamoMJ) {
                if (next.getClass() != getClass()) {
                    return null;
                }
                engine = (TileDynamoMJ) next;
            } else {
                break;
            }
        }

        if (next == null || next instanceof TileDynamoMJ) {
            return null;
        }

        IEnergyStorage recv = next.getCapability(CapabilityEnergy.ENERGY, side.getOpposite()).orElse(null);
        if (recv != null && recv.canReceive()) {
            return recv;
        } else {
            return null;
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing) {
        if (facing == currentDirection) {
            if (CapabilityEnergy.ENERGY == capability) {
                return LazyOptional.of(() -> rf).cast();
            } else {
                return super.getCapability(capability, facing);
            }
        } else {
            LazyOptional<T> cap = mjCaps.getCapability(capability, facing);
            return cap.isPresent() ? cap : super.getCapability(capability, facing);
        }
    }

    public int maxPowerExtracted() {
        return MAX_RF / 10;
    }

    public int getRf() {
        return currentRF;
    }

    @Override
    public boolean isEngineOn() {
        return isPumping;
    }

    @Override
    public long getCurrentMjOutput() {
        if (currentRF > 0) {
            return getMjPerTick();
        } else {
            return 0;
        }
    }

    @Override
    public long getMjStored() {
        return mjBattery.getStored();
    }

    // DynamoRF specific

    protected boolean isValidUpgrade(int slot, ItemStack stack) {
        Item item = stack.getItem();
        // return TileEngineRF.RF_UPGRADE.containsKey(item);
        return TileEngineRF.RF_UPGRADE.keySet().stream().anyMatch(reg -> reg.get() == item);
    }

    @Override
    public ActionResultType onActivated(
            PlayerEntity player, Hand hand, Direction side, float hitX, float hitY, float hitZ
    ) {
        ItemStack current = player.getItemInHand(hand).copy();
        if (super.onActivated(player, hand, side, hitX, hitY, hitZ) == ActionResultType.SUCCESS) {
            return ActionResultType.SUCCESS;
        }
        if (!current.isEmpty()) {
            if (EntityUtil.getWrenchHand(player) != null) {
                // return false;
                return ActionResultType.PASS;
            }
            if (current.getItem() instanceof IItemPipe) {
                // return false;
                return ActionResultType.PASS;
            }
        }
        if (!level.isClientSide) {
            // BCEnergyGuis.DYNAMO_MJ.openGUI(player, getPos());
            MessageUtil.serverOpenTileGui(player, this);
        }
        // return true;
        return ActionResultType.SUCCESS;
    }

    public long getMjPerTick() {
        long value = MjAPI.MJ * 4;
        for (int slot = 0; slot < invUpgrades.getSlots(); slot++) {
            ItemStack stack = invUpgrades.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            // Long add = TileEngineRF.RF_UPGRADE.get(stack.getItem());
            Long add = TileEngineRF.RF_UPGRADE.entrySet().stream().filter(entry -> entry.getKey().get() == stack.getItem()).map(Map.Entry::getValue).findFirst().orElse(null);
            if (add != null) {
                value += add;
            }
        }
        return value;
    }

    public int getRfGenerationRate() {

        final long mjPerTick = getMjPerTick();
        long mjPerRf = BCLibConfig.mjRfConversion.mjPerRf;

        return (int) (mjPerTick / mjPerRf);
    }

    protected void burn() {
        long mjStored = mjBattery.getStored();
        if (mjStored <= 0) {
            return;
        }

        if (isRedstonePowered) {

            long mjPerRf = BCLibConfig.mjRfConversion.mjPerRf;
            int genRf = getRfGenerationRate();

            int maxRf = (int) Math.min(genRf, mjStored / mjPerRf);

            if (maxRf <= 0) {
                return;
            }

            if (currentRF + maxRf >= MAX_RF) {
                return;
            }

            if (mjBattery.extractPower(maxRf * mjPerRf)) {
                currentOutput = maxRf;
                addPower(maxRf);
                heat += HEAT_RATE;
                if (heat >= 200) {
                    heat = 200;
                }
            }
        }
    }

    public void updateHeatLevel() {

        if (heat > MIN_HEAT) {
            heat -= COOLDOWN_RATE;
        }

        if (heat <= MIN_HEAT) {
            heat = MIN_HEAT;
        }

        getPowerStage();
    }

    public int getCurrentOutput() {
        if (currentRF > 0) {
            return (int) (getMjPerTick() / BCLibConfig.mjRfConversion.mjPerRf);
        } else {
            return 0;
        }
    }

    public int getCurrentRF() {
        return currentRF;
    }

    public Direction getCurrentDirection() {
        return currentDirection;
    }

    private class Rf implements IEnergyStorage {

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            maxExtract = Math.min(maxExtract, currentRF);
            if (maxExtract <= 0) {
                return 0;
            }
            if (!simulate) {
                currentRF -= maxExtract;
            }
            return maxExtract;
        }

        @Override
        public int getEnergyStored() {
            return currentRF;
        }

        @Override
        public int getMaxEnergyStored() {
            return MAX_RF;
        }

        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public boolean canReceive() {
            return false;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public float getProgressClient(float partialTicks) {
        float last = lastProgress;
        float now = progress;
        if (last > 0.5 && now < 0.5) {
            // we just returned
            now += 1;
        }
        float interp = last * (1 - partialTicks) + now * partialTicks;
        return interp % 1;
    }

    // MenuProvider


    @Override
    public ITextComponent getDisplayName() {
        return this.getBlockState().getBlock().getName();
    }

    @Nullable
    @Override
    public Container createMenu(int id, PlayerInventory inventory, PlayerEntity player) {
        return new ContainerDynamoMJ(BCEnergyMenuTypes.DYNAMO_MJ, id, player, this);
    }
}
