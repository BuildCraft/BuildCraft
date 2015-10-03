/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.tile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;

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
import buildcraft.core.BuildCraftCore;
import buildcraft.core.blueprints.BptBuilderTemplate;
import buildcraft.core.blueprints.BptContext;
import buildcraft.core.builders.TileAbstractBuilder;
import buildcraft.core.builders.patterns.FillerPattern;
import buildcraft.core.builders.patterns.PatternNone;
import buildcraft.core.lib.inventory.SimpleInventory;
import buildcraft.core.lib.network.command.CommandWriter;
import buildcraft.core.lib.network.command.ICommandReceiver;
import buildcraft.core.lib.network.command.PacketCommand;
import buildcraft.core.lib.utils.NetworkUtils;
import buildcraft.core.lib.utils.Utils;

import io.netty.buffer.ByteBuf;

public class TileFiller extends TileAbstractBuilder implements IHasWork, IControllable, ICommandReceiver, IStatementContainer {

    private static int POWER_ACTIVATION = 500;

    public FillerPattern currentPattern = PatternNone.INSTANCE;
    public IStatementParameter[] patternParameters;

    private BptBuilderTemplate currentTemplate;
    private BptContext context;

    private final Box box = new Box();
    private boolean done = false;
    private SimpleInventory inv = new SimpleInventory(27, "Filler", 64);

    private NBTTagCompound initNBT = null;

    public TileFiller() {
        inv.addListener(this);
        box.kind = Kind.STRIPES;
        initPatternParameters();
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

            if (a instanceof TileMarker) {
                a.removeFromWorld();
            }

            sendNetworkUpdate();
        }

        if (currentPattern != null && currentTemplate == null && box.isInitialized()) {
            currentTemplate = currentPattern.getTemplateBuilder(box, getWorld(), patternParameters);
            context = currentTemplate.getContext();
        }

        if (initNBT != null && currentTemplate != null) {
            currentTemplate.loadBuildStateToNBT(initNBT.getCompoundTag("builderState"), this);
        }

        initNBT = null;
    }

    @Override
    public void update() {
        super.update();

        if (worldObj.isRemote) {
            return;
        }

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

        if (currentPattern != null && currentTemplate == null) {
            currentTemplate = currentPattern.getTemplateBuilder(box, getWorld(), patternParameters);
            context = currentTemplate.getContext();
        }

        if (currentTemplate != null) {
            currentTemplate.buildNextSlot(worldObj, this, pos.getX(), pos.getY(), pos.getZ());

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
    public ItemStack getStackInSlotOnClosing(int slot) {
        return inv.getStackInSlotOnClosing(slot);
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

        setDone(nbt.getBoolean("done"));

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

        nbt.setBoolean("done", isDone());

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

    public void setPattern(FillerPattern pattern) {
        if (pattern != null && currentPattern != pattern) {
            currentPattern = pattern;
            currentTemplate = null;
            setDone(false);
            initPatternParameters();
            sendNetworkUpdate();
        }
    }

    private void writeParametersToNBT(NBTTagCompound nbt) {
        nbt.setByte("length", (byte) (patternParameters != null ? patternParameters.length : 0));
        if (patternParameters != null) {
            for (int i = 0; i < patternParameters.length; i++) {
                if (patternParameters[i] != null) {
                    NBTTagCompound patternData = new NBTTagCompound();
                    patternData.setString("kind", patternParameters[i].getUniqueTag());
                    patternParameters[i].writeToNBT(patternData);
                    nbt.setTag("p" + i, patternData);
                }
            }
        }
    }

    private void readParametersFromNBT(NBTTagCompound nbt) {
        patternParameters = new IStatementParameter[nbt.getByte("length")];
        for (int i = 0; i < patternParameters.length; i++) {
            if (nbt.hasKey("p" + i)) {
                NBTTagCompound patternData = nbt.getCompoundTag("p" + i);
                patternParameters[i] = StatementManager.createParameter(patternData.getString("kind"));
                patternParameters[i].readFromNBT(patternData);
            }
        }
    }

    @Override
    public void writeData(ByteBuf data) {
        super.writeData(data);
        box.writeData(data);
        data.writeBoolean(isDone());
        NetworkUtils.writeUTF(data, currentPattern.getUniqueTag());

        NBTTagCompound parameterData = new NBTTagCompound();
        writeParametersToNBT(parameterData);
        NetworkUtils.writeNBT(data, parameterData);
    }

    @Override
    public void readData(ByteBuf data) {
        super.readData(data);
        box.readData(data);
        setDone(data.readBoolean());
        FillerPattern pattern = (FillerPattern) FillerManager.registry.getPattern(NetworkUtils.readUTF(data));
        NBTTagCompound parameterData = NetworkUtils.readNBT(data);
        readParametersFromNBT(parameterData);
        setPattern(pattern);

        worldObj.markBlockForUpdate(pos);
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
            public void write(ByteBuf data) {
                NetworkUtils.writeUTF(data, name);
            }
        }));
    }

    @Override
    public void receiveCommand(String command, Side side, Object sender, ByteBuf stream) {
        super.receiveCommand(command, side, sender, stream);
        if (side.isServer() && "setPattern".equals(command)) {
            String name = NetworkUtils.readUTF(stream);
            setPattern((FillerPattern) FillerManager.registry.getPattern(name));
        } else if (side.isServer() && "setParameters".equals(command)) {
            NBTTagCompound patternData = NetworkUtils.readNBT(stream);
            readParametersFromNBT(patternData);
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
            public void write(ByteBuf data) {
                NBTTagCompound parameterData = new NBTTagCompound();
                writeParametersToNBT(parameterData);
                NetworkUtils.writeNBT(data, parameterData);
            }
        }));
    }

    @Deprecated
    public int getIconGlowLevel(int renderPass) {
        if (renderPass == 1) { // Red LED
            return isDone() ? 15 : 0;
        } else if (renderPass == 2) { // Green LED
            return 0;
        } else {
            return -1;
        }
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
}
