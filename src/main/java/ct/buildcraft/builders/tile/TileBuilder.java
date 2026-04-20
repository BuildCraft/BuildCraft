/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package ct.buildcraft.builders.tile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.ImmutableList;

import ct.buildcraft.api.core.BCLog;
import ct.buildcraft.api.core.EnumPipePart;
import ct.buildcraft.api.core.IPathProvider;
import ct.buildcraft.api.enums.EnumOptionalSnapshotType;
import ct.buildcraft.api.enums.EnumSnapshotType;
import ct.buildcraft.api.inventory.IItemTransactor;
import ct.buildcraft.api.mj.MjAPI;
import ct.buildcraft.api.mj.MjBattery;
import ct.buildcraft.api.mj.MjCapabilityHelper;
import ct.buildcraft.api.tiles.IDebuggable;
import ct.buildcraft.builders.BCBuildersBlocks;
import ct.buildcraft.builders.item.ItemSnapshot;
import ct.buildcraft.builders.menu.ContainerBuilder;
import ct.buildcraft.builders.snapshot.Blueprint;
import ct.buildcraft.builders.snapshot.BlueprintBuilder;
import ct.buildcraft.builders.snapshot.GlobalSavedDataSnapshots;
import ct.buildcraft.builders.snapshot.ITileForBlueprintBuilder;
import ct.buildcraft.builders.snapshot.ITileForTemplateBuilder;
import ct.buildcraft.builders.snapshot.Snapshot;
import ct.buildcraft.builders.snapshot.SnapshotBuilder;
import ct.buildcraft.builders.snapshot.Template;
import ct.buildcraft.builders.snapshot.TemplateBuilder;
import ct.buildcraft.lib.block.BlockBCBase_Neptune;
import ct.buildcraft.lib.fluid.Tank;
import ct.buildcraft.lib.fluid.TankManager;
import ct.buildcraft.lib.gui.ItemProvider;
import ct.buildcraft.lib.misc.AdvancementUtil;
import ct.buildcraft.lib.misc.BoundingBoxUtil;
import ct.buildcraft.lib.misc.CapUtil;
import ct.buildcraft.lib.misc.MessageUtil;
import ct.buildcraft.lib.misc.NBTUtilBC;
import ct.buildcraft.lib.misc.PositionUtil;
import ct.buildcraft.lib.misc.data.Box;
import ct.buildcraft.lib.misc.data.IdAllocator;
import ct.buildcraft.lib.mj.MjBatteryReceiver;
import ct.buildcraft.lib.net.MessageManager;
import ct.buildcraft.lib.tile.TileBC_Neptune;
import ct.buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;
import ct.buildcraft.lib.tile.item.ItemHandlerSimple;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent.Message;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.network.NetworkEvent;

public class TileBuilder extends TileBC_Neptune implements IDebuggable, ITileForTemplateBuilder, ITileForBlueprintBuilder, MenuProvider {
    public static final IdAllocator IDS = TileBC_Neptune.IDS.makeChild("builder");
    public static final int NET_CAN_EXCAVATE = IDS.allocId("CAN_EXCAVATE");
    public static final int NET_SNAPSHOT_TYPE = IDS.allocId("SNAPSHOT_TYPE");
    private static final ResourceLocation ADVANCEMENT = new ResourceLocation("buildcraftbuilders:paving_the_way");

    public final ItemHandlerSimple invSnapshot =
        itemManager
            .addInvHandler("snapshot", 1,
                (slot, stack) -> stack.getItem() instanceof ItemSnapshot
                    && ItemSnapshot.EnumItemSnapshotType.getFromStack(stack).used,
                EnumAccess.BOTH, EnumPipePart.VALUES);
    public final ItemHandlerSimple invResources =
        itemManager.addInvHandler("resources", 27, EnumAccess.BOTH, EnumPipePart.VALUES);
    public final ItemProvider invRequire = new ItemProvider(this::getDisplay, 24);

    private final MjBattery battery = new MjBattery(16000 * MjAPI.MJ);

    /** Stores the real path - just a few block positions. */
    public List<BlockPos> path = null;
    /** Stores the real path plus all possible block positions inbetween. */
    private List<BlockPos> basePoses = new ArrayList<>();
    private int currentBasePosIndex = 0;
    private Snapshot snapshot = null;
    public EnumSnapshotType snapshotType = null;
    private Template.BuildingInfo templateBuildingInfo = null;
    private Blueprint.BuildingInfo blueprintBuildingInfo = null;
    public TemplateBuilder templateBuilder = new TemplateBuilder(this);
    public BlueprintBuilder blueprintBuilder = new BlueprintBuilder(this);
    private Box currentBox = new Box();
    @NotNull
    private Rotation rotation = Rotation.NONE;
    
    private boolean isDone = false;
    
    private boolean shouldInit = false;
    
    //TODO
    private boolean needMaterial = true;
    private boolean canRotate = true;
    private boolean canExcavate = true;
    
    private DataSlot menuSetting = new DataSlot() {
		
		@Override
		public void set(int p) {
			needMaterial = (p&0b1) == 1;
			canRotate = (p&0b10) == 0b10;
			canExcavate = (p&0b100) == 0b100;
		}
		
		@Override
		public int get() {
			return (needMaterial ? 1 : 0) | (canRotate ? 0b10 : 0) | (canExcavate ? 0b100 : 0);
		}
	};
    
//    private final ContainerData blueprintData = new SingleProviderData(() -> snapshotType == null ? -1 : snapshotType.ordinal());
/*    private final ContainerData remainingDisplayRequiredData = 
    		new MutliProviderData((index) -> {
    			ItemStack item = blueprintBuilder.remainingDisplayRequired.get(index>>1);
    			return (index & 1) == 1 ? ((ForgeRegistry<Item>)ForgeRegistries.ITEMS).getID(item.getItem()) : item.getCount();
    		},
    			() -> blueprintBuilder.remainingDisplayRequired.size()*2) ;
    */
    public final GameEventListener worldEventListener = new GameEventListener() {
    	
    	GameEventListener blueprint = blueprintBuilder.getListener();
    	GameEventListener template = templateBuilder.getListener();
    	@Override
    	public boolean handleEventsImmediately() {
    		return true;
    	}
    	@Override
    	public PositionSource getListenerSource() {
    		return blueprint.getListenerSource();
    	}
    	@Override
    	public int getListenerRadius() {
    		return 64;
    	}
    	@Override
    	public boolean handleGameEvent(ServerLevel level, Message msg) {
    		return blueprint.handleGameEvent(level, msg) || template.handleGameEvent(level, msg);
    	}
    };

    public TileBuilder(BlockPos pos, BlockState state) {
    	super(BCBuildersBlocks.BUILDER_TILE_BC8.get(), pos, state);
    	Tank[] tanks = new Tank[4];
        for (int i = 0; i < 4; i++) {
            tanks[i] = new Tank(("tank" + (i+1)), FluidType.BUCKET_VOLUME * 8, this) {
                @Override
                protected void onContentsChanged() {
                    super.onContentsChanged();
                    Optional.ofNullable(getBuilder()).ifPresent(SnapshotBuilder::resourcesChanged);
                }
            };
            tankManager.add(tanks[i]);
        }
        caps.addProvider(new MjCapabilityHelper(new MjBatteryReceiver(battery)));
        caps.addCapabilityInstance(CapUtil.CAP_FLUIDS, tankManager, EnumPipePart.VALUES);
    }

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    @Override
    protected void onSlotChange(IItemHandlerModifiable handler, int slot, @Nonnull ItemStack before,
        @Nonnull ItemStack after) {
        if (!level.isClientSide) {
            if (handler == invSnapshot) {
                currentBasePosIndex = 0;
                snapshot = null;
                if (after.getItem() instanceof ItemSnapshot) {
                    Snapshot.Header header = ItemSnapshot.getHeader(after);
                    if (header != null) {
                        Snapshot newSnapshot = GlobalSavedDataSnapshots.get(level).getSnapshot(header.key);
                        if (newSnapshot != null) {
                            snapshot = newSnapshot;
                        }
                    }
                }
                updateSnapshot(true);
                sendNetworkUpdate(NET_SNAPSHOT_TYPE);
            }
            if (handler == invResources) {
                Optional.ofNullable(getBuilder()).ifPresent(SnapshotBuilder::resourcesChanged);
            }
        }
        super.onSlotChange(handler, slot, before, after);
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        templateBuilder.validate();
        blueprintBuilder.validate();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        templateBuilder.invalidate();
        blueprintBuilder.invalidate();
    }

    private void updateSnapshot(boolean canGetFacing) {
        Optional.ofNullable(getBuilder()).ifPresent(SnapshotBuilder::cancel);
        if (snapshot != null && getCurrentBasePos() != null) {
            snapshotType = snapshot.getType();
            if (canGetFacing) {
                rotation = Arrays.stream(Rotation.values()).filter(r -> r.rotate(snapshot.facing) == level
                    .getBlockState(worldPosition).getValue(BlockBCBase_Neptune.PROP_FACING)).findFirst().orElse(Rotation.NONE);
            }
            if (snapshot.getType() == EnumSnapshotType.TEMPLATE) {
                templateBuildingInfo = ((Template) snapshot).new BuildingInfo(getCurrentBasePos(), rotation);
            }
            if (snapshot.getType() == EnumSnapshotType.BLUEPRINT) {
                blueprintBuildingInfo = ((Blueprint) snapshot).new BuildingInfo(getCurrentBasePos(), rotation);
            }
            currentBox = Optional.ofNullable(getBuildingInfo()).map(buildingInfo -> buildingInfo.box).orElse(null);
            Optional.ofNullable(getBuilder()).ifPresent(SnapshotBuilder::updateSnapshot);
        } else {
            snapshotType = null;
            rotation = Rotation.NONE;
            templateBuildingInfo = null;
            blueprintBuildingInfo = null;
            currentBox = null;
        }
        if (currentBox == null) {
            currentBox = new Box();
        }
        BCLog.d(currentBox + "update");
    }

    private void updateBasePoses() {
        basePoses.clear();
        if (path != null) {
            int max = path.size() - 1;
            // Create a list of all the possible block positions on the path that could be used
            basePoses.add(path.get(0));
            for (int i = 1; i <= max; i++) {
                basePoses.addAll(PositionUtil.getAllOnPath(path.get(i - 1), path.get(i)));
            }
        } else {
            basePoses.add(worldPosition.offset(level.getBlockState(worldPosition).getValue(BlockBCBase_Neptune.PROP_FACING).getOpposite().getNormal()));
        }
    }

    private BlockPos getCurrentBasePos() {
        return currentBasePosIndex < basePoses.size() ? basePoses.get(currentBasePosIndex) : null;
    }
    
    @Override
    public void onPlacedBy(LivingEntity placer, ItemStack stack) {
        super.onPlacedBy(placer, stack);
        Direction facing = level.getBlockState(worldPosition).getValue(BlockBCBase_Neptune.PROP_FACING);
        BlockEntity inFront = level.getBlockEntity(worldPosition.offset(facing.getOpposite().getNormal()));
        if (inFront instanceof IPathProvider) {
            IPathProvider provider = (IPathProvider) inFront;
            ImmutableList<BlockPos> copiedPath = ImmutableList.copyOf(provider.getPath());
            if (copiedPath.size() >= 2) {
                path = copiedPath;
                provider.removeFromWorld(placer instanceof Player player ? player : null);
            }
        }
        updateBasePoses();
    }

    @Override
    public void update() {//TODO dont update every tick
//    	if(true)return;
    	if(shouldInit) {
    		updateBasePoses();
    		shouldInit = false;
    	}

        level.getProfiler().push("main");
        level.getProfiler().push("power");
        battery.tick(getLevel(), getBlockPos());
        level.getProfiler().popPush("builder");
        SnapshotBuilder<?> builder = getBuilder();
        if (builder != null) {
            isDone = builder.tick();
            if (isDone) {
                if (currentBasePosIndex < basePoses.size() - 1) {
                    currentBasePosIndex++;
                    if (currentBasePosIndex == basePoses.size() && currentBasePosIndex > 1)
                        AdvancementUtil.unlockAdvancement(getOwner().getId(), ADVANCEMENT);
                    if (currentBasePosIndex >= basePoses.size()) {
                        currentBasePosIndex = basePoses.size() - 1;
                    }
                    updateSnapshot(true);
                }
            }
        }
        sendNetworkUpdate(NET_RENDER_DATA); // FIXME
        level.getProfiler().pop();
        level.getProfiler().pop();
    }

    // Networking

    @Override
    public void writePayload(int id, FriendlyByteBuf buffer, LogicalSide side) {
        super.writePayload(id, buffer, side);
        if (side == LogicalSide.SERVER) {
            if (id == NET_RENDER_DATA) {
                buffer.writeInt(path == null ? 0 : path.size());
                if (path != null) {
                    path.forEach((p) -> MessageUtil.writeBlockPos(buffer, p));
                }
                buffer.writeBoolean(snapshotType != null);
                if (snapshotType != null) {
                    buffer.writeEnum(snapshotType);
                    // noinspection ConstantConditions
                    getBuilder().writeToByteBuf(buffer);
                }
                currentBox.writeData(buffer);
                writePayload(NET_CAN_EXCAVATE, buffer, side);
                writePayload(NET_SNAPSHOT_TYPE, buffer, side);
            }
            if (id == NET_GUI_DATA || id == NET_GUI_TICK) {
                tankManager.writeData(buffer);
            }
            if (id == NET_CAN_EXCAVATE) {
                buffer.writeBoolean(canExcavate);
            }
            if (id == NET_SNAPSHOT_TYPE) {
                buffer.writeEnum(EnumOptionalSnapshotType.fromNullable(snapshotType));
            }
        }
    }

    @Override
    public void readPayload(int id, FriendlyByteBuf buffer, LogicalSide side, NetworkEvent.Context ctx) throws IOException {
    	super.readPayload(id, buffer, side, ctx);
        if (side == LogicalSide.CLIENT) {
            if (id == NET_RENDER_DATA) {
                path = new ArrayList<>();
                int pathSize = buffer.readInt();
                if (pathSize != 0) {
                    for (int i = 0; i < pathSize; i++) {
                        path.add(MessageUtil.readBlockPos(buffer));
                    }
                } else {
                    path = null;
                }
                //updateBasePoses();
                shouldInit = true;
                if (buffer.readBoolean()) {
                    snapshotType = buffer.readEnum(EnumSnapshotType.class);
                    getBuilder().readFromByteBuf(buffer);
                } else {
                    snapshotType = null;
                }
                currentBox.readData(buffer);
                readPayload(NET_CAN_EXCAVATE, buffer, side, ctx);
                readPayload(NET_SNAPSHOT_TYPE, buffer, side, ctx);
            }
            if (id == NET_GUI_DATA || id == NET_GUI_TICK) {
                tankManager.readData(buffer);
            }
            if (id == NET_CAN_EXCAVATE) {
                canExcavate = buffer.readBoolean();
            }
            if (id == NET_SNAPSHOT_TYPE) {
                EnumSnapshotType old = snapshotType;
                snapshotType = buffer.readEnum(EnumOptionalSnapshotType.class).type;
                if (old != snapshotType) {
                    redrawBlock();
                }
            }
        }
        if (side == LogicalSide.SERVER) {
            if (id == NET_CAN_EXCAVATE) {
                canExcavate = buffer.readBoolean();
                sendNetworkUpdate(NET_CAN_EXCAVATE);
            }
        }
    }

    public void sendCanExcavate(boolean newValue) {
        MessageManager.sendToServer(createMessage(NET_CAN_EXCAVATE, buffer -> buffer.writeBoolean(newValue)));
    }

    // Read-write
    
    

    @Override
	public void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        if (path != null) {
            nbt.put("path", NBTUtilBC.writeObjectList(path.stream().map(NbtUtils::writeBlockPos)));
        }
        nbt.put("basePoses", NBTUtilBC.writeObjectList(basePoses.stream().map(NbtUtils::writeBlockPos)));
        nbt.putBoolean("canExcavate", canExcavate);
        nbt.put("rotation", NBTUtilBC.writeEnum(rotation));
        Optional.ofNullable(getBuilder()).ifPresent(builder -> nbt.put("builder", builder.serializeNBT()));
	}

	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
        if (nbt.contains("path")) {
            path =
                NBTUtilBC.readCompoundList(nbt.get("path")).map(NbtUtils::readBlockPos).collect(Collectors.toList());
        }
        basePoses = NBTUtilBC.readCompoundList(nbt.get("basePoses")).map(NbtUtils::readBlockPos)
            .collect(Collectors.toList());
        canExcavate = nbt.getBoolean("canExcavate");
        rotation = NBTUtilBC.readEnum(nbt.get("rotation"), Rotation.class);
        if (nbt.contains("builder")) {
            updateSnapshot(false);
            Optional.ofNullable(getBuilder())
                .ifPresent(builder -> builder.deserializeNBT(nbt.getCompound("builder")));
        }
	}
	
    @Override
	public void onLoad() {
		super.onLoad();
		this.onSlotChange(invSnapshot, 0, invSnapshot.getStackInSlot(0), invSnapshot.getStackInSlot(0));//TODO:find a better way
	}

    // Rendering

	@OnlyIn(Dist.CLIENT)
    public Box getBox() {
        return currentBox;
    }
/*
    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean hasFastRenderer() {
        return true;
    }
*//*
    @Nonnull
    @Override
    @OnlyIn(Dist.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return BoundingBoxUtil.makeFrom(getBlockPos(), getBox(), path);
    }
   
    

    @Override
    @OnlyIn(Dist.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return Double.MAX_VALUE;
    } *///TODO
    
    @Nonnull
    @Override
    @OnlyIn(Dist.CLIENT)
	public AABB getRenderBoundingBox() {
    	 return BoundingBoxUtil.makeFrom(getBlockPos(), getBox(), path);
	}
    

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        left.add("battery = " + battery.getDebugString());
        left.add("basePoses = " + (basePoses == null ? "null" : basePoses.size()));
        left.add("currentBasePosIndex = " + currentBasePosIndex);
        left.add("isDone = " + isDone);
    }

	@Override
	public Level getWorldBC() {
		return level;
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

    @Override
    public SnapshotBuilder<?> getBuilder() {
        if (snapshotType == EnumSnapshotType.TEMPLATE) {
            return templateBuilder;
        }
        if (snapshotType == EnumSnapshotType.BLUEPRINT) {
            return blueprintBuilder;
        }
        return null;
    }

    private Snapshot.BuildingInfo getBuildingInfo() {
        if (snapshotType == EnumSnapshotType.TEMPLATE) {
            return templateBuildingInfo;
        }
        if (snapshotType == EnumSnapshotType.BLUEPRINT) {
            return blueprintBuildingInfo;
        }
        return null;
    }

    @Override
    public Template.BuildingInfo getTemplateBuildingInfo() {
        return templateBuildingInfo;
    }

    @Override
    public Blueprint.BuildingInfo getBlueprintBuildingInfo() {
        return blueprintBuildingInfo;
    }

    @Override
    public IItemTransactor getInvResources() {
        return invResources;
    }

    @Override
    public TankManager getTankManager() {
        return tankManager;
    }
    
	
    private ItemStack getDisplay(int index) {
        return snapshotType == EnumSnapshotType.BLUEPRINT &&
                index < blueprintBuilder.remainingDisplayRequired.size()
                ? blueprintBuilder.remainingDisplayRequired.get(index)
                : ItemStack.EMPTY;
    }

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerBuilder(id, inv, invSnapshot, invResources, invRequire, menuSetting, ContainerLevelAccess.create(level, worldPosition));
	}

	@Override
	public Component getDisplayName() {
		return this.getBlockState().getBlock().getName();
	}

}
