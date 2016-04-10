/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders;

import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.BuildCraftCore;
import buildcraft.api.core.IAreaProvider;
import buildcraft.api.filler.FillerManager;
import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementManager;
import buildcraft.api.tiles.IControllable;
import buildcraft.api.tiles.IHasWork;
import buildcraft.core.Box;
import buildcraft.core.Box.Kind;
import buildcraft.core.blueprints.BptBuilderTemplate;
import buildcraft.core.builders.TileAbstractBuilder;
import buildcraft.core.builders.patterns.FillerPattern;
import buildcraft.core.builders.patterns.PatternNone;
import buildcraft.core.lib.inventory.SimpleInventory;
import buildcraft.core.lib.network.command.CommandWriter;
import buildcraft.core.lib.network.command.ICommandReceiver;
import buildcraft.core.lib.network.command.PacketCommand;
import buildcraft.core.lib.utils.NetworkUtils;
import buildcraft.core.lib.utils.Utils;

public class TileFiller extends TileAbstractBuilder implements IHasWork, IControllable, ICommandReceiver, IStatementContainer {
    private static int POWER_ACTIVATION = 500;

    public FillerPattern currentPattern = PatternNone.INSTANCE;
    public IStatementParameter[] patternParameters;
    private int patternLocked;

    private BptBuilderTemplate currentTemplate;

    private final Box box = new Box();
    private boolean done = false;
    private boolean excavate = true;
    private SimpleInventory inv = new SimpleInventory(27, "Filler", 64);

    private NBTTagCompound initNBT = null;

    public TileFiller() {
        setControlMode(Mode.On);
        inv.addListener(this);
        box.kind = Kind.STRIPES;
        initPatternParameters();
    }

    public boolean isExcavate() {
        return excavate;
    }

    @Override
    public void initialize() {
        super.initialize();

        if (worldObj.isRemote) {
            return;
        }

        IAreaProvider a = Utils.getNearbyAreaProvider(worldObj, pos);

        if (a != null) {
            box.initialize(a);
            a.removeFromWorld();
            sendNetworkUpdate();
        }

        if (currentTemplate == null) {
            initTemplate();
        }

        if (initNBT != null && currentTemplate != null) {
            currentTemplate.loadBuildStateToNBT(initNBT.getCompoundTag("builderState"), this);
        }

        initNBT = null;
    }

    private void initTemplate() {
        if (currentPattern != null && box.size().distanceSq(BlockPos.ORIGIN) > 0) {
            currentTemplate = currentPattern.getTemplateBuilder(box, getWorld(), patternParameters);
            currentTemplate.blueprint.excavate = excavate;
        }
    }

    @Override
    public void update() {
        super.update();

        if (worldObj.isRemote) {
            return;
        }

        if (patternLocked > 0) patternLocked--;

        if (mode == Mode.Off) {
            return;
        }

        if (!box.isInitialized()) {
            return;
        }

        if (getBattery().getEnergyStored() < POWER_ACTIVATION) {
            return;
        }

        boolean oldDone = isDone();

        if (isDone()) {
            if (mode == Mode.Loop) {
                setDone(false);
            } else {
                return;
            }
        }

        if (currentTemplate == null) {
            initTemplate();
        }

        if (currentTemplate != null) {
            currentTemplate.buildNextSlot(worldObj, this);

            if (currentTemplate.isDone(this)) {
                setDone(true);
                currentTemplate = null;
            }
        }

        if (oldDone != isDone()) {
            sendNetworkUpdate();
        }
    }

    @Override
    public final int getSizeInventory() {
        return inv.getSizeInventory();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return inv.getStackInSlot(slot);
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        return inv.decrStackSize(slot, amount);
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        inv.setInventorySlotContents(slot, stack);
    }

    @Override
    public ItemStack removeStackFromSlot(int slot) {
        return inv.removeStackFromSlot(slot);
    }

    @Override
    public String getInventoryName() {
        return "Filler";
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        inv.readFromNBT(nbt);

        if (nbt.hasKey("pattern")) {
            currentPattern = (FillerPattern) FillerManager.registry.getPattern(nbt.getString("pattern"));
        }

        if (currentPattern == null) {
            currentPattern = PatternNone.INSTANCE;
        }

        if (nbt.hasKey("pp")) {
            readParametersFromNBT(nbt.getCompoundTag("pp"));
        } else {
            initPatternParameters();
        }

        if (nbt.hasKey("box")) {
            box.initialize(nbt.getCompoundTag("box"));
        }

        done = nbt.getBoolean("done");
        excavate = nbt.hasKey("excavate") ? nbt.getBoolean("excavate") : true;

        // The rest of load has to be done upon initialize.
        initNBT = (NBTTagCompound) nbt.getCompoundTag("bpt").copy();
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        inv.writeToNBT(nbt);

        if (currentPattern != null) {
            nbt.setString("pattern", currentPattern.getUniqueTag());
        }

        NBTTagCompound boxStore = new NBTTagCompound();
        box.writeToNBT(boxStore);
        nbt.setTag("box", boxStore);

        nbt.setBoolean("done", done);
        nbt.setBoolean("excavate", excavate);

        NBTTagCompound bptNBT = new NBTTagCompound();

        if (currentTemplate != null) {
            NBTTagCompound builderCpt = new NBTTagCompound();
            currentTemplate.saveBuildStateToNBT(builderCpt, this);
            bptNBT.setTag("builderState", builderCpt);
        }

        nbt.setTag("bpt", bptNBT);

        NBTTagCompound ppNBT = new NBTTagCompound();
        writeParametersToNBT(ppNBT);
        nbt.setTag("pp", ppNBT);
    }

    @Override
    public int getInventoryStackLimit() {
        return inv.getInventoryStackLimit();
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer entityplayer) {
        if (worldObj.getTileEntity(pos) != this) {
            return false;
        }

        return entityplayer.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64D;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        destroy();
    }

    private void initPatternParameters() {
        patternParameters = new IStatementParameter[currentPattern.maxParameters()];
        for (int i = 0; i < currentPattern.minParameters(); i++) {
            patternParameters[i] = currentPattern.createParameter(i);
        }
    }

    public boolean setPattern(FillerPattern pattern, boolean lock) {
        if (pattern != null && currentPattern != pattern) {
            currentPattern = pattern;
            currentTemplate = null;
            if (lock) patternLocked = 2;
            else patternLocked = 0;
            setDone(false);
            initPatternParameters();
            sendNetworkUpdate();
            return true;
        } else if (pattern != null && lock) {
            patternLocked = 2;
        }
        return false;
    }

    public boolean isPatternLocked() {
        return patternLocked > 0;
    }

    private void writeParametersToNBT(NBTTagCompound nbt) {
        IStatementParameter[] patternParameters = this.patternParameters;
        nbt.setByte("length", (byte) (patternParameters != null ? patternParameters.length : 0));
        if (patternParameters != null) {
            for (int i = 0; i < patternParameters.length; i++) {
                if (patternParameters[i] != null) {
                    NBTTagCompound patternData = new NBTTagCompound();
                    patternData.setString("kind", patternParameters[i].getUniqueTag());
                    patternParameters[i].writeToNBT(patternData);// ArrayIndexOutOfBounds
                    nbt.setTag("p" + i, patternData);
                }
            }
        }
    }

    private void readParametersFromNBT(NBTTagCompound nbt) {
        IStatementParameter[] patternParameters = new IStatementParameter[nbt.getByte("length")];
        for (int i = 0; i < patternParameters.length; i++) {
            if (nbt.hasKey("p" + i)) {
                NBTTagCompound patternData = nbt.getCompoundTag("p" + i);
                patternParameters[i] = StatementManager.createParameter(patternData.getString("kind"));
                patternParameters[i].readFromNBT(patternData);
            }
        }
        this.patternParameters = patternParameters;
    }

    @Override
    public void writeData(ByteBuf data) {
        super.writeData(data);
        box.writeData(data);
        data.writeByte((done ? 1 : 0) | (excavate ? 2 : 0) | (isPatternLocked() ? 4 : 0));
        NetworkUtils.writeUTF(data, currentPattern.getUniqueTag());

        NBTTagCompound parameterData = new NBTTagCompound();
        writeParametersToNBT(parameterData);
        NetworkUtils.writeNBT(data, parameterData);
    }

    @Override
    public void readData(ByteBuf data) {
        super.readData(data);
        box.readData(data);
        int flags = data.readUnsignedByte();
        done = (flags & 1) > 0;
        excavate = (flags & 2) > 0;
        patternLocked = (flags & 4) > 0 ? 2 : 0;
        FillerPattern pattern = (FillerPattern) FillerManager.registry.getPattern(NetworkUtils.readUTF(data));
        NBTTagCompound parameterData = NetworkUtils.readNBT(data);
        readParametersFromNBT(parameterData);
        if (setPattern(pattern, isPatternLocked())) {
            worldObj.markBlockForUpdate(pos);
        }
    }

    @Override
    public boolean hasWork() {
        return !isDone() && mode != Mode.Off;
    }

    @Override
    public void openInventory(EntityPlayer player) {}

    @Override
    public void closeInventory(EntityPlayer player) {}

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return true;
    }

    public void rpcSetPatternFromString(final String name) {
        BuildCraftCore.instance.sendToServer(new PacketCommand(this, "setPattern", new CommandWriter() {
            @Override
            public void write(ByteBuf data) {
                NetworkUtils.writeUTF(data, name);
            }
        }));
    }

    @Override
    public void receiveCommand(String command, Side side, Object sender, ByteBuf stream) {
        super.receiveCommand(command, side, sender, stream);
        if (side.isServer()) {
            if ("setPattern".equals(command)) {
                // You cannot set the pattern if it is locked
                if (isPatternLocked()) return;
                String name = NetworkUtils.readUTF(stream);
                setPattern((FillerPattern) FillerManager.registry.getPattern(name), false);

                done = false;
            } else if ("setParameters".equals(command)) {
                // You cannot set the pattern if it is locked
                if (isPatternLocked()) return;
                NBTTagCompound patternData = NetworkUtils.readNBT(stream);
                readParametersFromNBT(patternData);

                currentTemplate = null;
                done = false;
            } else if ("setFlags".equals(command)) {
                excavate = stream.readBoolean();
                currentTemplate = null;

                sendNetworkUpdate();
                done = false;
            }
        }
    }

    @Override
    public Box getBox() {
        return box;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new Box(this).extendToEncompass(box).expand(50).getBoundingBox();
    }

    @Override
    public boolean isBuildingMaterialSlot(int i) {
        return true;
    }

    @Override
    public boolean acceptsControlMode(Mode mode) {
        return mode == IControllable.Mode.On || mode == IControllable.Mode.Off || mode == IControllable.Mode.Loop;
    }

    @Override
    public TileEntity getTile() {
        return this;
    }

    public void rpcSetParameter(int i, IStatementParameter patternParameter) {
        BuildCraftCore.instance.sendToServer(new PacketCommand(this, "setParameters", new CommandWriter() {
            @Override
            public void write(ByteBuf data) {
                NBTTagCompound parameterData = new NBTTagCompound();
                writeParametersToNBT(parameterData);
                NetworkUtils.writeNBT(data, parameterData);
            }
        }));
    }

    private boolean isDone() {
        return done;
    }

    private void setDone(boolean done) {
        this.done = done;
        if (worldObj != null) {
            worldObj.setBlockState(pos, worldObj.getBlockState(pos).withProperty(BuildCraftProperties.LED_DONE, done));
        }
    }

    public void setExcavate(boolean excavate) {
        this.excavate = excavate;
    }
}
