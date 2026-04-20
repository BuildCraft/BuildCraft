/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.builders.tile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;

import ct.buildcraft.api.core.EnumPipePart;
import ct.buildcraft.api.core.IAreaProvider;
import ct.buildcraft.api.core.IBox;
import ct.buildcraft.api.filler.IFillerPattern;
import ct.buildcraft.api.inventory.IItemTransactor;
import ct.buildcraft.api.mj.MjAPI;
import ct.buildcraft.api.mj.MjBattery;
import ct.buildcraft.api.mj.MjCapabilityHelper;
import ct.buildcraft.api.statements.IStatementParameter;
import ct.buildcraft.api.statements.containers.IFillerStatementContainer;
import ct.buildcraft.api.tiles.IControllable;
import ct.buildcraft.api.tiles.IDebuggable;
import ct.buildcraft.api.tiles.TilesAPI;
import ct.buildcraft.builders.BCBuildersBlocks;
import ct.buildcraft.builders.addon.AddonFillerPlanner;
import ct.buildcraft.builders.filler.FillerType;
import ct.buildcraft.builders.filler.FillerUtil;
import ct.buildcraft.builders.menu.ContainerFiller;
import ct.buildcraft.builders.snapshot.ITileForTemplateBuilder;
import ct.buildcraft.builders.snapshot.SnapshotBuilder;
import ct.buildcraft.builders.snapshot.Template;
import ct.buildcraft.builders.snapshot.Template.BuildingInfo;
import ct.buildcraft.builders.snapshot.TemplateBuilder;
import ct.buildcraft.core.marker.volume.ClientVolumeBoxes;
import ct.buildcraft.core.marker.volume.EnumAddonSlot;
import ct.buildcraft.core.marker.volume.Lock;
import ct.buildcraft.core.marker.volume.VolumeBox;
import ct.buildcraft.core.marker.volume.WorldSavedDataVolumeBoxes;
import ct.buildcraft.lib.block.BlockBCBase_Neptune;
import ct.buildcraft.lib.misc.BoundingBoxUtil;
import ct.buildcraft.lib.misc.NBTUtilBC;
import ct.buildcraft.lib.misc.data.Box;
import ct.buildcraft.lib.misc.data.IdAllocator;
import ct.buildcraft.lib.mj.MjBatteryReceiver;
import ct.buildcraft.lib.net.MessageManager;
import ct.buildcraft.lib.statement.FullStatement;
import ct.buildcraft.lib.tile.TileBC_Neptune;
import ct.buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;
import ct.buildcraft.lib.tile.item.ItemHandlerSimple;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class TileFiller extends TileBC_Neptune 
    implements IDebuggable, ITileForTemplateBuilder, IFillerStatementContainer, IControllable, MenuProvider {
    public static final IdAllocator IDS = TileBC_Neptune.IDS.makeChild("filler");
    public static final int NET_CAN_EXCAVATE = IDS.allocId("CAN_EXCAVATE");
    public static final int NET_INVERT = IDS.allocId("INVERT");
    public static final int NET_PATTERN = IDS.allocId("PATTERN");
    public static final int NET_BOX = IDS.allocId("BOX");

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    public final ItemHandlerSimple invResources =
        itemManager.addInvHandler(
            "resources",
            27,
            (slot, stack) -> ForgeRegistries.ITEMS.containsValue(stack.getItem()),
            EnumAccess.INSERT,
            EnumPipePart.VALUES
        );
    private final MjBattery battery = new MjBattery(16000 * MjAPI.MJ);
    private boolean canExcavate = true;
    public boolean inverted = false;
    private boolean finished = false;
    private byte lockedTicks = 0;
    private Mode mode = Mode.ON;

    public final Box box = new Box();
    public AddonFillerPlanner addon;
    public boolean markerBox = true;

    public final FullStatement<IFillerPattern> patternStatement = new FullStatement<>(
        FillerType.INSTANCE,
        4,
        (statement, paramIndex) -> onStatementChange()
    );
    private BuildingInfo buildingInfo;
    public TemplateBuilder builder = new TemplateBuilder(this);

    public TileFiller(BlockPos pos, BlockState state) {
    	super(BCBuildersBlocks.FILLER_TILE_BC8.get(), pos, state);
        caps.addProvider(new MjCapabilityHelper(new MjBatteryReceiver(battery)));
        caps.addCapabilityInstance(TilesAPI.CAP_CONTROLLABLE, this, EnumPipePart.VALUES);
    }

    @Override
    public void onPlacedBy(LivingEntity placer, ItemStack stack) {
        super.onPlacedBy(placer, stack);
        if (level.isClientSide) {
            return;
        }
        BlockState blockState = level.getBlockState(worldPosition);
        WorldSavedDataVolumeBoxes volumeBoxes = WorldSavedDataVolumeBoxes.get(level);
        BlockPos offsetPos = worldPosition.offset(blockState.getValue(BlockBCBase_Neptune.PROP_FACING).getOpposite().getNormal());
        VolumeBox volumeBox = volumeBoxes.getVolumeBoxAt(offsetPos);
        BlockEntity tile = level.getBlockEntity(offsetPos);
        if (volumeBox != null) {
            addon = (AddonFillerPlanner) volumeBox.addons
                .values()
                .stream()
                .filter(AddonFillerPlanner.class::isInstance)
                .findFirst()
                .orElse(null);
            if (addon != null) {
                volumeBox.locks.add(
                    new Lock(
                        new Lock.Cause.CauseBlock(worldPosition, blockState.getBlock()),
                        new Lock.Target.TargetAddon(addon.getSlot()),
                        new Lock.Target.TargetRemove(),
                        new Lock.Target.TargetResize(),
                        new Lock.Target.TargetUsedByMachine(
                            Lock.Target.TargetUsedByMachine.EnumType.STRIPES_WRITE
                        )
                    )
                );
                volumeBoxes.setDirty();
                addon.updateBuildingInfo();
                markerBox = false;
            } else {
                box.reset();
                box.setMin(volumeBox.box.min());
                box.setMax(volumeBox.box.max());
                volumeBox.locks.add(
                    new Lock(
                        new Lock.Cause.CauseBlock(worldPosition, blockState.getBlock()),
                        new Lock.Target.TargetRemove(),
                        new Lock.Target.TargetResize(),
                        new Lock.Target.TargetUsedByMachine(
                            Lock.Target.TargetUsedByMachine.EnumType.STRIPES_WRITE
                        )
                    )
                );
                volumeBoxes.setDirty();
                markerBox = false;
            }
        } else if (tile instanceof IAreaProvider) {
            IAreaProvider provider = (IAreaProvider) tile;
            box.reset();
            box.setMin(provider.min());
            box.setMax(provider.max());
            provider.removeFromWorld(placer instanceof Player player ? player : null);
        }
        updateBuildingInfo();
        sendNetworkUpdate(NET_RENDER_DATA);
    }

    @Override
    protected void onSlotChange(IItemHandlerModifiable handler,
                                int slot,
                                @Nonnull ItemStack before,
                                @Nonnull ItemStack after) {
        if (!level.isClientSide) {
            if (handler == invResources) {
                Optional.ofNullable(getBuilder()).ifPresent(SnapshotBuilder::resourcesChanged);
            }
        }
        super.onSlotChange(handler, slot, before, after);
    }

    @Override
    public void update() {
        if (level.isClientSide) {
            if (isValid()) {
                builder.tick();
            }
            patternStatement.canInteract = !isLocked();
            return;
        }
        sendNetworkUpdate(NET_RENDER_DATA);
        lockedTicks--;
        if (lockedTicks < 0) {
            lockedTicks = 0;
        }
        if (mode == Mode.OFF/* || (mode == Mode.ON && finished)*/) { // TODO: finished
            return;
        }
        Optional.ofNullable(getBuilder()).ifPresent(SnapshotBuilder::tick);
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        builder.validate();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        builder.invalidate();
    }

    @Override
    public void writePayload(int id, FriendlyByteBuf buffer, LogicalSide side) {
        super.writePayload(id, buffer, side);
        if (side == LogicalSide.SERVER) {
            if (id == NET_RENDER_DATA) {
                builder.writeToByteBuf(buffer);
                writePayload(NET_BOX, buffer, side);
            } else if (id == NET_GUI_DATA || id == NET_GUI_TICK) {
                writePayload(NET_CAN_EXCAVATE, buffer, side);
                writePayload(NET_INVERT, buffer, side);
                writePayload(NET_PATTERN, buffer, side);
                builder.writeToByteBuf(buffer);
                buffer.writeBoolean(finished);
                buffer.writeBoolean(lockedTicks > 0);
                buffer.writeEnum(mode);
            } else if (id == NET_BOX) {
                box.writeData(buffer);
                buffer.writeBoolean(markerBox);
                buffer.writeBoolean(addon != null);
                if (addon != null) {
                    buffer.writeUUID(addon.volumeBox.id);
                    buffer.writeEnum(addon.getSlot());
                }
            } else if (id == NET_CAN_EXCAVATE) {
                buffer.writeBoolean(canExcavate);
            } else if (id == NET_INVERT) {
                buffer.writeBoolean(inverted);
            } else if (id == NET_PATTERN) {
                patternStatement.writeToBuffer(buffer);
            }
        }
    }

    @Override
    public void readPayload(int id, FriendlyByteBuf buffer, LogicalSide side, NetworkEvent.Context ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == LogicalSide.CLIENT) {
            if (id == NET_RENDER_DATA) {
                builder.readFromByteBuf(buffer);
                readPayload(NET_BOX, buffer, side, ctx);
            } else if (id == NET_GUI_DATA || id == NET_GUI_TICK) {
                readPayload(NET_CAN_EXCAVATE, buffer, side, ctx);
                readPayload(NET_INVERT, buffer, side, ctx);
                readPayload(NET_PATTERN, buffer, side, ctx);
                builder.readFromByteBuf(buffer);
                finished = buffer.readBoolean();
                lockedTicks = buffer.readBoolean() ? (byte) 1 : (byte) 0;
                mode = buffer.readEnum(Mode.class);
            } else if (id == NET_BOX) {
                box.readData(buffer);
                markerBox = buffer.readBoolean();
                if (buffer.readBoolean()) {
                    UUID volumeBoxId = buffer.readUUID();
                    VolumeBox volumeBox = level.isClientSide
                        ?
                        ClientVolumeBoxes.INSTANCE.volumeBoxes.stream()
                            .filter(localVolumeBox -> localVolumeBox.id.equals(volumeBoxId))
                            .findFirst()
                            .orElseThrow(NullPointerException::new)
                        : WorldSavedDataVolumeBoxes.get(level).getVolumeBoxFromId(volumeBoxId);
                    addon = (AddonFillerPlanner) volumeBox
                        .addons
                        .get(buffer.readEnum(EnumAddonSlot.class));
                }
            } else if (id == NET_CAN_EXCAVATE) {
                canExcavate = buffer.readBoolean();
            } else if (id == NET_INVERT) {
                inverted = buffer.readBoolean();
            } else if (id == NET_PATTERN) {
                patternStatement.readFromBuffer(buffer);
            }
        }
        if (side == LogicalSide.SERVER) {
            if (id == NET_CAN_EXCAVATE) {
                canExcavate = buffer.readBoolean();
                sendNetworkGuiUpdate(NET_CAN_EXCAVATE);
            }
        }
    }

    private void updateBuildingInfo() {
        Optional.ofNullable(getBuilder()).ifPresent(SnapshotBuilder::cancel);
        buildingInfo = (hasBox() && addon == null) ? FillerUtil.createBuildingInfo(
            this,
            patternStatement,
            IntStream.range(0, patternStatement.maxParams)
                .mapToObj(patternStatement::get)
                .toArray(IStatementParameter[]::new),
            inverted
        ) : null;
        Optional.ofNullable(getBuilder()).ifPresent(SnapshotBuilder::updateSnapshot);
    }

    public void sendCanExcavate(boolean newValue) {
        MessageManager.sendToServer(createMessage(NET_CAN_EXCAVATE, buffer -> buffer.writeBoolean(newValue)));
    }

    public void onStatementChange() {
        if (!level.isClientSide) {
            createAndSendMessage(NET_PATTERN, patternStatement::writeToBuffer);
        }
        finished = false;
        updateBuildingInfo();
        setChanged();
    }

    // Read-write


	public void saveAdditional(CompoundTag nbt) {
		super.saveAdditional(nbt);
        nbt.put("battery", battery.serializeNBT());
        nbt.putBoolean("canExcavate", canExcavate);
        nbt.putBoolean("inverted", inverted);
        nbt.putBoolean("finished", finished);
        nbt.putByte("lockedTicks", lockedTicks);
        nbt.put("mode", NBTUtilBC.writeEnum(mode));
        nbt.put("box", box.writeToNBT());
        if (addon != null) {
            nbt.putUUID("addonVolumeBoxId", addon.volumeBox.id);
            nbt.put("addonSlot", NBTUtilBC.writeEnum(addon.getSlot()));
        }
        nbt.putBoolean("markerBox", markerBox);
        nbt.put("patternStatement", patternStatement.writeToNbt());
        Optional.ofNullable(getBuilder()).ifPresent(builder -> nbt.put("builder", builder.serializeNBT()));
	}

	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
        battery.deserializeNBT(nbt.getCompound("battery"));
        canExcavate = nbt.getBoolean("canExcavate");
        inverted = nbt.getBoolean("inverted");
        finished = nbt.getBoolean("finished");
        lockedTicks = nbt.getByte("lockedTicks");
        mode = Optional.ofNullable(NBTUtilBC.readEnum(nbt.get("mode"), Mode.class)).orElse(Mode.ON);
        box.initialize(nbt.getCompound("box"));
        if (nbt.contains("addonSlot")) {
            addon = (AddonFillerPlanner) WorldSavedDataVolumeBoxes.get(level)
                .getVolumeBoxFromId(nbt.getUUID("addonVolumeBoxId"))
                .addons
                .get(NBTUtilBC.readEnum(nbt.get("addonSlot"), EnumAddonSlot.class));
        }
        markerBox = nbt.getBoolean("markerBox");
        patternStatement.readFromNbt(nbt.getCompound("patternStatement"));
        if (nbt.contains("builder")) {
            Optional.ofNullable(getBuilder()).ifPresent(builder -> builder.deserializeNBT(nbt.getCompound("builder")));
        }
	}
	
    @Override
    public void onLoad() {
        super.onLoad();
        updateBuildingInfo();
    }

    // Rendering

    @Nonnull
    @Override
    @OnlyIn(Dist.CLIENT)
    public AABB getRenderBoundingBox() {
        return BoundingBoxUtil.makeFrom(worldPosition, addon != null ? addon.volumeBox.box : box);
    }
/*
    @Override
    @OnlyIn(Dist.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return Double.MAX_VALUE;
    }*/

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        left.add("battery = " + battery.getDebugString());
        left.add("box = " + box);
        left.add("pattern = " + patternStatement.get());
        left.add("mode = " + mode);
        left.add("is_finished = " + finished);
        left.add("lockedTicks = " + lockedTicks);
        left.add("addon = " + addon);
        left.add("markerBox = " + markerBox);
    }

    @Override
    public Level getWorldBC() {
        return level;
    }

    public int getCountToPlace() {
        return builder == null ? 0 : builder.leftToPlace;
    }

    public int getCountToBreak() {
        return builder == null ? 0 : builder.leftToBreak;
    }

    @Override
    public MjBattery getBattery() {
        return battery;
    }

    @Override
    public BlockPos getBuilderPos() {
        return worldPosition;
    }

    @Override
    public boolean canExcavate() {
        return canExcavate;
    }

    public boolean isFinished() {
        return mode != Mode.LOOP && this.finished;
    }

    public boolean isLocked() {
        return lockedTicks > 0;
    }

    @Override
    public TemplateBuilder getBuilder() {
        return isValid() ? builder : null;
    }

    @Override
    public Template.BuildingInfo getTemplateBuildingInfo() {
        return isValid()
            ? addon != null ? addon.buildingInfo : buildingInfo
            : null;
    }

    @Override
    public IItemTransactor getInvResources() {
        return invResources;
    }

    // IFillerStatementContainer

    @Override
    public BlockEntity getTile() {
        return this;
    }

    @Override
    public Level getFillerWorld() {
        return level;
    }

    @Override
    public boolean hasBox() {
        return addon != null || box.isInitialized();
    }

    public boolean isValid() {
        return hasBox() && ((level != null &&level.isClientSide) || (addon != null ? addon.buildingInfo : buildingInfo) != null);
    }

    @Override
    public IBox getBox() {
        if (!hasBox()) {
            throw new IllegalStateException("Called getBox() when hasBox() returned false!");
        }
        return addon != null ? addon.volumeBox.box : box;
    }

    @Override
    public void setPattern(IFillerPattern pattern, IStatementParameter[] params) {
        patternStatement.set(pattern, params);
        finished = false;
        lockedTicks = 3;
    }

    // IControllable

    @Override
    public Mode getControlMode() {
        return mode;
    }

    @Override
    public void setControlMode(Mode mode) {
        if (this.mode == Mode.OFF && mode != Mode.OFF) {
            finished = false;
        }
        this.mode = mode;
    }

	@Override
	public ContainerFiller createMenu(int id, Inventory inv, Player player) {
		return new ContainerFiller(id, inv, invResources, ContainerLevelAccess.create(level, worldPosition));
	}

	@Override
	public Component getDisplayName() {
		return this.getBlockState().getBlock().getName();
	}
}
