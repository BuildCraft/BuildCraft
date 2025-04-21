/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.entity;

import buildcraft.api.BCItems;
import buildcraft.api.boards.RedstoneBoardNBT;
import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.IZone;
import buildcraft.api.events.RobotEvent;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.net.IMessage;
import buildcraft.api.robots.*;
import buildcraft.api.statements.StatementSlot;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.tools.IToolWrench;
import buildcraft.lib.cap.CapabilityHelper;
import buildcraft.lib.fluid.Tank;
import buildcraft.lib.fluid.TankManager;
import buildcraft.lib.misc.*;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.net.*;
import buildcraft.lib.oredictionarytag.OreDictionaryTags;
import buildcraft.lib.tile.item.ItemHandlerManager;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import buildcraft.robotics.BCRoboticsParticleTypes;
import buildcraft.robotics.ai.AIRobotMain;
import buildcraft.robotics.ai.AIRobotShutdown;
import buildcraft.robotics.ai.AIRobotSleep;
import buildcraft.robotics.client.particle.EntityRobotEnergyParticle;
import buildcraft.robotics.item.ItemRobot;
import buildcraft.robotics.statements.ActionRobotWorkInArea;
import buildcraft.robotics.statements.ActionRobotWorkInArea.AreaType;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.netty.buffer.Unpooled;
import io.netty.util.internal.StringUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;

public class EntityRobot extends EntityRobotBase implements IEntityAdditionalSpawnData, IPayloadReceiver, IDebuggable {
    public static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.entity");

    protected static final IdAllocator IDS = new IdAllocator("robot");
    public static final int NET_REQUEST_INITIALIZATION = IDS.allocId("requestInitialization");
    public static final int NET_CLIENT_SET_ITEM_IN_USE = IDS.allocId("clientSetItemInUse");
    public static final int NET_CLIENT_SET_INVENTORY = IDS.allocId("clientSetInventory");
    public static final int NET_INITIALIZE = IDS.allocId("initialize");
    public static final int NET_SET_ITEM_ACTIVE = IDS.allocId("setItemActive");
    public static final int NET_SET_STEAM_DIRECTION = IDS.allocId("setSteamDirection");
    public static final int NET_SYNC_WEARABLES = IDS.allocId("syncWearables");

    // public static final ResourceLocation ROBOT_BASE = new ResourceLocation(DefaultProps.TEXTURE_PATH_ROBOTS + "/robot_base.png");
    // public static final ResourceLocation ROBOT_BASE = new ResourceLocation("buildcraftrobotics:textures/entities" + "/robot_base.png");
    public static final ResourceLocation ROBOT_BASE = new ResourceLocation("buildcraftrobotics:entities/robot_base");
    public static final ResourceLocation ROBOT_BASE_PNG = new ResourceLocation("buildcraftrobotics:entities/robot_base.png");

    // private static final int DATA_LASER_TAIL_X = 12;
    // private static final int DATA_LASER_TAIL_Y = 13;
    // private static final int DATA_LASER_TAIL_Z = 14;
    // // 15 is used by entity living base to see if the AI is active or not
    // private static final int DATA_LASER_VISIBLE = 16;
    // private static final int DATA_BOARD_ID = 17;
    // private static final int DATA_ITEM_AIM_YAW = 18;
    // private static final int DATA_ITEM_AIM_PITCH = 19;
    // private static final int DATA_ENERGY_SPEND_PER_CYCLE = 20;
    // private static final int DATA_ACTIVE_CLIENT = 21;
    // private static final int DATA_BATTERY_ENERGY = 22;
    private static final DataParameter<Float> DATA_LASER_TAIL_X = EntityDataManager.defineId(EntityRobot.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> DATA_LASER_TAIL_Y = EntityDataManager.defineId(EntityRobot.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> DATA_LASER_TAIL_Z = EntityDataManager.defineId(EntityRobot.class, DataSerializers.FLOAT);
    // 15 is used by entity living base to see if the AI is active or not
    private static final DataParameter<Byte> DATA_LASER_VISIBLE = EntityDataManager.defineId(EntityRobot.class, DataSerializers.BYTE);
    private static final DataParameter<String> DATA_BOARD_ID = EntityDataManager.defineId(EntityRobot.class, DataSerializers.STRING);
    private static final DataParameter<Float> DATA_ITEM_AIM_YAW = EntityDataManager.defineId(EntityRobot.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> DATA_ITEM_AIM_PITCH = EntityDataManager.defineId(EntityRobot.class, DataSerializers.FLOAT);
    private static final DataParameter<CompoundNBT> DATA_ENERGY_SPEND_PER_CYCLE = EntityDataManager.defineId(EntityRobot.class, DataSerializers.COMPOUND_TAG);
    private static final DataParameter<Byte> DATA_ACTIVE_CLIENT = EntityDataManager.defineId(EntityRobot.class, DataSerializers.BYTE);
    private static final DataParameter<CompoundNBT> DATA_BATTERY_ENERGY = EntityDataManager.defineId(EntityRobot.class, DataSerializers.COMPOUND_TAG);

    public static final int MAX_WEARABLES = 8;

    private static Set<ResourceLocation> blacklistedItemsForUpdate = Sets.newHashSet();
    // public LaserData_BC8 laser = new LaserData_BC8(LASER_TYPE, new Vector3d(0, 0, 0), new Vector3d(0, 0, 0), 1 / 16D);
    public float laserEndX, laserEndY, laserEndZ;
    public DockingStation linkedDockingStation;
    public BlockPos linkedDockingStationIndex;
    public Direction linkedDockingStationSide;

    public BlockPos currentDockingStationIndex;
    public Direction currentDockingStationSide;

    public boolean isDocked = false;

    public RedstoneBoardRobot board;
    public AIRobotMain mainAI;

    @Nonnull
    // public ItemStack itemInUse;
    public ItemStack itemInUse = StackUtil.EMPTY;
    public float itemAimYaw = 0;
    public float renderItemAimYaw = 0;
    public float itemAimPitch = 0;
    public boolean itemActive = false;
    public float itemActiveStage = 0;
    public long lastUpdateTime = 0;

    private DockingStation currentDockingStation;
    private List<ItemStack> wearables = new ArrayList<ItemStack>();

    private boolean needsUpdate = false;

    // Calen 1.18.2
    protected final CapabilityHelper caps = new CapabilityHelper();
    protected final ItemHandlerManager itemManager = new ItemHandlerManager(this::onSlotChange);
    public final TankManager tankManager = new TankManager();

    // private ItemStack[] inv = new ItemStack[4];
    public ItemHandlerSimple inv = itemManager.addInvHandler(
            "inv",
            4,
            this::canPlaceItem,
            ItemHandlerManager.EnumAccess.BOTH,
            EnumPipePart.VALUES
    );
    // private FluidStack tank;
    // private int maxFluid = FluidAttributes.BUCKET_VOLUME * 4;

    public final Tank tank = new Tank("robotTank", FluidAttributes.BUCKET_VOLUME * 4, this);
    private ResourceLocation texture;

    private WeakHashMap<Entity, Long> unreachableEntities = new WeakHashMap<Entity, Long>();

    private ListNBT stackRequestNBT;

    private MjBattery battery = new MjBattery(MAX_POWER);

    private boolean firstUpdateDone = false;

    private boolean isActiveClient = false;

    private long robotId = EntityRobotBase.NULL_ROBOT_ID;

    private long energySpendPerCycle = 0;
    private int ticksCharging = 0;
    private float energyFX = 0;
    private Vector3d steamDirection = new Vector3d(0, -1, 0);

    // Calen 1.18.2: to replace LaserData#isVisible
    public boolean isLaserVisible;

    public EntityRobot(EntityType<EntityRobot> entityType, World world, RedstoneBoardRobotNBT boardNBT) {
        this(entityType, world);

        caps.addProvider(itemManager);
        this.tankManager.add(tank);
        caps.addCapabilityInstance(CapUtil.CAP_FLUIDS, tank, EnumPipePart.VALUES);

        board = boardNBT.create(this);
        entityData.set(DATA_BOARD_ID, board.getNBTHandler().getID().toString());

        if (!world.isClientSide) {
            mainAI = new AIRobotMain(this);
            mainAI.start();
        }
    }

    private EntityRobot(EntityType<EntityRobot> entityType, World world) {
        super(entityType, world);

        this.setNoGravity(true); // Calen 1.18.2: to avoid falling

//        motionX = 0;
//        motionY = 0;
//        motionZ = 0;
        setDeltaMovement(0, 0, 0);

        // ignoreFrustumCheck = true;
        noCulling = true;
        // laser.isVisible = false;
        this.isLaserVisible = false;
        // entityCollisionReduction = 1F; // Calen 1.18.2: no this field

        // width = 0.25F;
        // height = 0.25F;
    }

    // Calen 1.16.5: to move smoothly without friction
    @Override
    public Vector3d handleRelativeFrictionAndCalculateMovement(Vector3d p_233633_1_, float friction) {
        this.moveRelative(0, p_233633_1_);
        this.move(MoverType.SELF, this.getDeltaMovement());
        return this.getDeltaMovement();
    }

    // Calen 1.16.5: to move smoothly without friction
    @Override
    public void travel(Vector3d p_213352_1_) {
        Vector3d deltaMovement = this.handleRelativeFrictionAndCalculateMovement(p_213352_1_, 0);
        this.setDeltaMovement(deltaMovement.x, deltaMovement.y, deltaMovement.z);
        this.calculateEntityAnimation(this, false);
    }

    @Override
    // protected void entityInit()
    protected void defineSynchedData() {
        // super.entityInit();
        super.defineSynchedData();

        // setNullBoundingBox();

        // preventEntitySpawning = false;
        blocksBuilding = false;
        // noClip = true;
        noPhysics = true;
        // isImmuneToFire = true; // Calen 1.18.2: moved to EntityType
        // this.enablePersistence(); // TODO Calen

//        dataWatcher.addObject(DATA_LASER_TAIL_X, Float.valueOf(0));
//        dataWatcher.addObject(DATA_LASER_TAIL_Y, Float.valueOf(0));
//        dataWatcher.addObject(DATA_LASER_TAIL_Z, Float.valueOf(0));
//        dataWatcher.addObject(DATA_LASER_VISIBLE, Byte.valueOf((byte) 0));
//        dataWatcher.addObject(DATA_BOARD_ID, "");
//        dataWatcher.addObject(DATA_ITEM_AIM_YAW, Float.valueOf(0));
//        dataWatcher.addObject(DATA_ITEM_AIM_PITCH, Float.valueOf(0));
//        dataWatcher.addObject(DATA_ENERGY_SPEND_PER_CYCLE, Integer.valueOf(0));
//        dataWatcher.addObject(DATA_ACTIVE_CLIENT, Byte.valueOf((byte) 0));
//        dataWatcher.addObject(DATA_BATTERY_ENERGY, Integer.valueOf(0));
        entityData.define(DATA_LASER_TAIL_X, Float.valueOf(0));
        entityData.define(DATA_LASER_TAIL_Y, Float.valueOf(0));
        entityData.define(DATA_LASER_TAIL_Z, Float.valueOf(0));
        entityData.define(DATA_LASER_VISIBLE, Byte.valueOf((byte) 0));
        entityData.define(DATA_BOARD_ID, "");
        entityData.define(DATA_ITEM_AIM_YAW, Float.valueOf(0));
        entityData.define(DATA_ITEM_AIM_PITCH, Float.valueOf(0));
        entityData.define(DATA_ENERGY_SPEND_PER_CYCLE, writeLongToNbt(0));
        entityData.define(DATA_ACTIVE_CLIENT, Byte.valueOf((byte) 0));
        entityData.define(DATA_BATTERY_ENERGY, writeLongToNbt(0));
    }

    protected void updateDataClient() {
//        float x = dataWatcher.getWatchableObjectFloat(DATA_LASER_TAIL_X);
//        float y = dataWatcher.getWatchableObjectFloat(DATA_LASER_TAIL_Y);
//        float z = dataWatcher.getWatchableObjectFloat(DATA_LASER_TAIL_Z);
        float x = entityData.get(DATA_LASER_TAIL_X);
        float y = entityData.get(DATA_LASER_TAIL_Y);
        float z = entityData.get(DATA_LASER_TAIL_Z);
        // laser.end = new Vec3(x, y, z);
        laserEndX = x;
        laserEndY = y;
        laserEndZ = z;
//        laser.isVisible = entityData.get(DATA_LASER_VISIBLE) == 1;
        this.isLaserVisible = entityData.get(DATA_LASER_VISIBLE) == 1;

//        RedstoneBoardNBT<?> boardNBT = RedstoneBoardRegistry.instance.getRedstoneBoard(dataWatcher.getWatchableObjectString(DATA_BOARD_ID));
        RedstoneBoardNBT<?> boardNBT = RedstoneBoardRegistry.instance.getRedstoneBoard(new ResourceLocation(entityData.get(DATA_BOARD_ID)));

        if (boardNBT != null) {
            // texture = ((RedstoneBoardRobotNBT) boardNBT).getRobotTexture();
            texture = ((RedstoneBoardRobotNBT) boardNBT).getRobotTextureFullLocation();
        }

//        itemAimYaw = dataWatcher.getWatchableObjectFloat(DATA_ITEM_AIM_YAW);
//        itemAimPitch = dataWatcher.getWatchableObjectFloat(DATA_ITEM_AIM_PITCH);
//        energySpendPerCycle = dataWatcher.getWatchableObjectInt(DATA_ENERGY_SPEND_PER_CYCLE);
//        isActiveClient = dataWatcher.getWatchableObjectByte(DATA_ACTIVE_CLIENT) == 1;
//        battery.setEnergy(dataWatcher.getWatchableObjectInt(DATA_BATTERY_ENERGY));
        itemAimYaw = entityData.get(DATA_ITEM_AIM_YAW);
        itemAimPitch = entityData.get(DATA_ITEM_AIM_PITCH);
        energySpendPerCycle = readLongFromNbt(entityData.get(DATA_ENERGY_SPEND_PER_CYCLE));
        isActiveClient = entityData.get(DATA_ACTIVE_CLIENT) == 1;
        battery.extractAll();
        battery.addPower(readLongFromNbt(entityData.get(DATA_BATTERY_ENERGY)), false);
    }

    protected void updateDataServer() {
        entityData.set(DATA_LASER_TAIL_X, Float.valueOf(laserEndX));
        entityData.set(DATA_LASER_TAIL_Y, Float.valueOf(laserEndY));
        entityData.set(DATA_LASER_TAIL_Z, Float.valueOf(laserEndZ));
//        entityData.set(DATA_LASER_VISIBLE, Byte.valueOf((byte) (laser.isVisible ? 1 : 0)));
        entityData.set(DATA_LASER_VISIBLE, Byte.valueOf((byte) (this.isLaserVisible ? 1 : 0)));
        entityData.set(DATA_ITEM_AIM_YAW, Float.valueOf(itemAimYaw));
        entityData.set(DATA_ITEM_AIM_PITCH, Float.valueOf(itemAimPitch));
    }

    public boolean isActive() {
        if (level.isClientSide) {
            return isActiveClient;
        } else {
            return mainAI.getActiveAI() instanceof AIRobotSleep || mainAI.getActiveAI() instanceof AIRobotShutdown;
        }
    }

    protected void init() {
        if (level.isClientSide) {
            MessageManager.sendToServer(createMessage(NET_REQUEST_INITIALIZATION, (buf) -> {}));
        }
    }

    public void setLaserDestination(float x, float y, float z) {
        if (x != laserEndX || y != laserEndY || z != laserEndZ) {
            // laser.end = new Vec3(x, y, z);
            needsUpdate = true;
        }
    }

    public void showLaser() {
        // if (!laser.isVisible)
        if (!this.isLaserVisible) {
            // laser.isVisible = true;
            this.isLaserVisible = true;
            needsUpdate = true;
        }
    }

    public void hideLaser() {
        // if (laser.isVisible)
        if (this.isLaserVisible) {
            // laser.isVisible = false;
            this.isLaserVisible = false;
            needsUpdate = true;
        }
    }

    protected void firstUpdate() {
        if (stackRequestNBT != null) {

        }

        if (!level.isClientSide) {
            getRegistry().registerRobot(this);
        }
    }

    @Override
    public ITextComponent getName() {
        if (this.hasCustomName()) {
            // return this.getCustomNameTag();
            return this.getCustomName();
        } else {
            return this.board.getNBTHandler().getDisplayNameComponent();
        }
    }

    @Override
    // public void onEntityUpdate()
    public void tick() {
        this.level.getProfiler().push("bcEntityRobot");
        if (!firstUpdateDone) {
            firstUpdate();
            firstUpdateDone = true;
        }

        if (ticksCharging > 0) {
            ticksCharging--;
        }

        if (!level.isClientSide) {
            // The client-side sleep indicator should also display if the robot is charging.
            // To not break gates and other things checking for sleep, this is done here.
            entityData.set(DATA_ACTIVE_CLIENT, Byte.valueOf((byte) ((isActive() && ticksCharging == 0) ? 1 : 0)));
            entityData.set(DATA_BATTERY_ENERGY, writeLongToNbt(getPower()));

            if (needsUpdate) {
                updateDataServer();
                needsUpdate = false;
            }
        }

        if (level.isClientSide) {
            updateDataClient();
            updateRotationYaw(60.0f);
            updateEnergyFX();
        }

        if (currentDockingStation != null) {
//            motionX = 0;
//            motionY = 0;
//            motionZ = 0;
            setDeltaMovement(0, 0, 0);

            Vector3d pos = VecUtil.convertCenter(currentDockingStation.getPos()).add(VecUtil.convert(currentDockingStation.side(), 0.5));
//            posX = pos.x();
//            posY = pos.y();
//            posZ = pos.z();
            this.setPos(pos.x(), pos.y(), pos.z());
        }

        if (!level.isClientSide) {
            if (linkedDockingStation == null) {
                if (linkedDockingStationIndex != null) {
                    linkedDockingStation = getRegistry().getStation(linkedDockingStationIndex, linkedDockingStationSide);
                }

                if (linkedDockingStation == null) {
                    shutdown("no docking station");
                } else {
                    if (linkedDockingStation.robotTaking() != this) {
                        if (linkedDockingStation.robotIdTaking() == robotId) {
                            BCLog.logger.warn("A robot entity was not properly unloaded");
                            linkedDockingStation.invalidateRobotTakingEntity();
                        }
                        if (linkedDockingStation.robotTaking() != this) {
                            shutdown("wrong docking station");
                        }
                    }
                }
            }

            if (currentDockingStationIndex != null && currentDockingStation == null) {
                currentDockingStation = getRegistry().getStation(currentDockingStationIndex, currentDockingStationSide);
            }

            if (getY() < -128) {
                dead = true;
                forceRemove();

                BCLog.logger.info("Destroying robot " + this.toString() + " - Fallen into Void");
                getRegistry().killRobot(this);
            }

            if (linkedDockingStation == null || linkedDockingStation.isInitialized()) {
                this.level.getProfiler().push("bcRobotAI");
                mainAI.cycle();
                this.level.getProfiler().pop();

                if (energySpendPerCycle != mainAI.getActiveAI().getPowerCost()) {
                    energySpendPerCycle = mainAI.getActiveAI().getPowerCost();
                    entityData.set(DATA_ENERGY_SPEND_PER_CYCLE, writeLongToNbt(energySpendPerCycle));
                }
            }
        }

        // tick all carried itemstacks
        // for (int i = 0; i < inv.length; i++)
        for (int i = 0; i < inv.getSlots(); i++) {
            // updateItem(inv[i], i, false);
            updateItem(inv.getStackInSlot(i), i, false);
        }

        // tick the item the robot is currently holding
        updateItem(itemInUse, 0, true);

        // do not tick wearables or equipment from EntityLiving

        // super.onEntityUpdate();
        super.tick();
        this.level.getProfiler().pop();
    }

    // @Override
    // protected void updateEntityActionState() {}

    @Override
    // public boolean handleWaterMovement()
    public boolean updateInWaterStateAndDoFluidPushing() {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    private void updateEnergyFX() {
        energyFX += energySpendPerCycle;

        // if (energyFX >= (100 << (2 * Minecraft.getInstance().options.particles.getId())))
        if (energyFX >= ((10 * MjAPI.MJ) << (2 * Minecraft.getInstance().options.particles.getId()))) {
            energyFX = 0;
            spawnEnergyFX();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnEnergyFX() {
//        Minecraft.getInstance().effectRenderer.addEffect(
//                new EntityRobotEnergyParticle(
//                        worldObj,
//                        getX() + steamDirection.x() * 0.25,
//                        getY() + steamDirection.y() * 0.25,
//                        getZ() + steamDirection.z() * 0.25,
//                        steamDirection.x() * 0.05,
//                        steamDirection.y() * 0.05,
//                        steamDirection.z() * 0.05,
//                        energySpendPerCycle * 0.075F < 1 ? 1 : energySpendPerCycle * 0.075F
//                )
//        );
        EntityRobotEnergyParticle particle = (EntityRobotEnergyParticle) Minecraft.getInstance().particleEngine.createParticle(
                BCRoboticsParticleTypes.robot.get(),
                getX() + steamDirection.x() * 0.25,
                getY() + steamDirection.y() * 0.25,
                getZ() + steamDirection.z() * 0.25,
                steamDirection.x() * 0.05,
                steamDirection.y() * 0.05,
                steamDirection.z() * 0.05
        );
        // particle.setSize(energySpendPerCycle * 0.075F < 1 ? 1 : energySpendPerCycle * 0.075F);
        particle.setSize(energySpendPerCycle * 10 * 0.075F / MjAPI.MJ < 1 ? 1 : energySpendPerCycle * 10 * 0.075F / MjAPI.MJ);
    }

    @Override
    // public AxisAlignedBB getEntityBoundingBox()
    public AxisAlignedBB getBoundingBox() {
        // return new AxisAlignedBB(posX - 0.25F, posY - 0.25F, posZ - 0.25F, posX + 0.25F, posY + 0.25F, posZ + 0.25F);
        return new AxisAlignedBB(this.getX() - 0.25F, this.getY() - 0.25F, this.getZ() - 0.25F, this.getX() + 0.25F, this.getY() + 0.25F, this.getZ() + 0.25F);
    }

    @Override
    public void setPos(double x, double y, double z) {
        this.setPosRaw(x, y, z);
    }

    // Calen 1.16.5 to avoid calling Entity#setLocationFromBoundingbox in super method
    @Override
    public void move(MoverType type, Vector3d dir) {
        Vector3d pos = this.position();
        Vector3d newPos = pos.add(dir);
        this.setPosRaw(newPos.x, newPos.y, newPos.z);
    }

    @Override
    public float getEyeHeight(Pose pose) {
        return 0;
    }

//    public void setNullBoundingBox() {
//        width = 0F;
//        height = 0F;
//
//        setEntityBoundingBox(new AxisAlignedBB(posX, posY, posZ, posX, posY, posZ));
//    }

    private void shutdown(String reason) {
        if (!(mainAI.getDelegateAI() instanceof AIRobotShutdown)) {
            BCLog.logger.info("Shutting down robot " + this.toString() + " - " + reason);
            mainAI.startDelegateAI(new AIRobotShutdown(this));
        }
    }

    @Override
    public void writeSpawnData(PacketBuffer data) {
        data.writeByte(wearables.size());
        for (ItemStack s : wearables) {
//            NetworkUtils.writeStack(data, s);
            data.writeItemStack(s, false);
        }
    }

    @Override
    public void readSpawnData(PacketBuffer data) {
        int amount = data.readUnsignedByte();
        while (amount > 0) {
//            wearables.add(NetworkUtils.readStack(data));
            wearables.add(data.readItem());
            amount--;
        }
        init();
    }

    @Nonnull
    @Override
    // public ItemStack getHeldItem()
    public ItemStack getItemInHand(Hand hand) {
        return itemInUse;
    }

    @Override
    public ItemStack getMainHandItem() {
        return itemInUse;
    }

    @Override
    public ItemStack getOffhandItem() {
        return itemInUse;
    }

    @Override
    public void setItemInHand(Hand hand, ItemStack itemStack) {
        this.itemInUse = itemStack;
    }

    // 1.18.2 forced
    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return Lists.newArrayList();
    }

    // 1.18.2 forced
    @Override
    public ItemStack getItemBySlot(EquipmentSlotType equipmentSlot) {
        return StackUtil.EMPTY;
    }

    // 1.18.2 forced
    @Override
    public void setItemSlot(EquipmentSlotType equipmentSlot, ItemStack itemStack) {

    }

//    @Override
//    public void setCurrentItemOrArmor(int i, ItemStack itemstack) {}

//    @Override
//    public void moveEntityWithHeading(float par1, float par2) {
//        this.setPos(getX() + getDeltaMovement().x(), getY() + getDeltaMovement().y(), getZ() + getDeltaMovement().z());
//    }

    @Override
    // public boolean isOnLadder()
    public boolean onClimbable() {
        return false;
    }

    public ResourceLocation getTexture() {
        return texture;
    }

    @Override
    // public void writeEntityToNBT(NBTTagCompound nbt)
    public void addAdditionalSaveData(CompoundNBT nbt) {
        super.addAdditionalSaveData(nbt);

        if (linkedDockingStationIndex != null) {
            CompoundNBT linkedStationNBT = new CompoundNBT();
            linkedStationNBT.put("index", NBTUtilBC.writeBlockPos(linkedDockingStationIndex));
            linkedStationNBT.putByte("side", (byte) linkedDockingStationSide.ordinal());
            nbt.put("linkedStation", linkedStationNBT);
        }

        if (currentDockingStationIndex != null) {
            CompoundNBT currentStationNBT = new CompoundNBT();
            currentStationNBT.put("index", NBTUtilBC.writeBlockPos(currentDockingStationIndex));
            currentStationNBT.putByte("side", (byte) currentDockingStationSide.ordinal());
            nbt.put("currentStation", currentStationNBT);
        }

        CompoundNBT nbtLaser = new CompoundNBT();
        nbtLaser.putFloat("endX", laserEndX);
        nbtLaser.putFloat("endY", laserEndY);
        nbtLaser.putFloat("endZ", laserEndZ);
        nbt.put("laser", nbtLaser);

        CompoundNBT batteryNBT = battery.serializeNBT();
        nbt.put("battery", batteryNBT);

        // if (itemInUse != null)
        if (!itemInUse.isEmpty()) {
            CompoundNBT itemNBT = new CompoundNBT();
            itemInUse.save(itemNBT);
            nbt.put("itemInUse", itemNBT);
            nbt.putBoolean("itemActive", itemActive);
        }

        // for (int i = 0; i < inv.length; ++i) {
        //     CompoundTag stackNbt = new CompoundTag();
        //
        //     if (inv[i] != null) {
        //         nbt.put("inv[" + i + "]", inv[i].save(stackNbt));
        //     }
        // }
        CompoundNBT items = itemManager.serializeNBT();
        if (!items.isEmpty()) {
            nbt.put("items", items);
        }

        if (wearables.size() > 0) {
            ListNBT wearableList = new ListNBT();

            for (ItemStack wearable : wearables) {
                CompoundNBT item = new CompoundNBT();
                wearable.save(item);
                wearableList.add(item);
            }

            nbt.put("wearables", wearableList);
        }

        CompoundNBT ai = new CompoundNBT();
        mainAI.writeToNBT(ai);
        nbt.put("mainAI", ai);

        if (mainAI.getDelegateAI() != board) {
            CompoundNBT boardNBT = new CompoundNBT();
            board.writeToNBT(boardNBT);
            nbt.put("board", boardNBT);
        }

        nbt.putLong("robotId", robotId);

//        if (tank != null) {
//            NBTTagCompound tankNBT = new NBTTagCompound();
//
//            tank.writeToNBT(tankNBT);
//
//            nbt.setTag("tank", tankNBT);
//        }
        CompoundNBT tanks = tankManager.serializeNBT();
        if (!tanks.isEmpty()) {
            nbt.put("tanks", tanks);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT nbt) {
        super.readAdditionalSaveData(nbt);

        if (nbt.contains("linkedStation")) {
            CompoundNBT linkedStationNBT = nbt.getCompound("linkedStation");
            linkedDockingStationIndex = NBTUtilBC.readBlockPos(linkedStationNBT.get("index"));
            linkedDockingStationSide = Direction.values()[linkedStationNBT.getByte("side")];
        }

        if (nbt.contains("currentStation")) {
            CompoundNBT currentStationNBT = nbt.getCompound("currentStation");
            currentDockingStationIndex = NBTUtilBC.readBlockPos(currentStationNBT.get("index"));
            currentDockingStationSide = Direction.values()[currentStationNBT.getByte("side")];

        }

        // laser.readFromNBT(nbt.getCompound("laser"));
        CompoundNBT laserNbt = nbt.getCompound("laser");
        this.laserEndX = laserNbt.getFloat("endX");
        this.laserEndY = laserNbt.getFloat("endY");
        this.laserEndZ = laserNbt.getFloat("endZ");

        battery.deserializeNBT(nbt.getCompound("battery"));

        wearables.clear();
        if (nbt.contains("wearables")) {
            ListNBT list = nbt.getList("wearables", 10);
            for (int i = 0; i < list.size(); i++) {
                ItemStack stack = ItemStack.of(list.getCompound(i));
//                if (stack != null)
                if (stack != null && !stack.isEmpty()) {
                    wearables.add(stack);
                }
            }
        }

        if (nbt.contains("itemInUse")) {
            itemInUse = ItemStack.of(nbt.getCompound("itemInUse"));
            itemActive = nbt.getBoolean("itemActive");
        }

//        for (int i = 0; i < inv.length; ++i) {
//            inv[i] = ItemStack.of(nbt.getCompound("inv[" + i + "]"));
//        }
        if (nbt.contains("items", Constants.NBT.TAG_COMPOUND)) {
            itemManager.deserializeNBT(nbt.getCompound("items"));
        }

        CompoundNBT ai = nbt.getCompound("mainAI");
        mainAI = (AIRobotMain) AIRobot.loadAI(ai, this);

        if (nbt.contains("board")) {
            board = (RedstoneBoardRobot) AIRobot.loadAI(nbt.getCompound("board"), this);
        } else {
            board = (RedstoneBoardRobot) mainAI.getDelegateAI();
        }

        if (board == null) {
            board = RedstoneBoardRegistry.instance.getEmptyRobotBoard().create(this);
        }

        entityData.set(DATA_BOARD_ID, board.getNBTHandler().getID().toString());

        stackRequestNBT = nbt.getList("stackRequests", Constants.NBT.TAG_COMPOUND);

        if (nbt.contains("robotId")) {
            robotId = nbt.getLong("robotId");
        }

//        if (nbt.hasKey("tank")) {
//            tank = FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("tank"));
//        } else {
//            tank = null;
//        }
        if (nbt.contains("tanks", Constants.NBT.TAG_COMPOUND)) {
            tankManager.deserializeNBT(nbt.getCompound("tanks"));
        }

        // Calen 1.18.2: only Mob has this method
//        // Restore robot persistence on pre-6.1.9 robotics
//        this.enablePersistence(); // Calen 1.18.2: EntityType#noSave() -> no persistence
//        // this.func_110163_bv(); TODO (PASS 1): Check to make sure this is really the correct method!

        init();
    }

    @Override
    public void dock(DockingStation station) {
        currentDockingStation = station;

        setSteamDirection(VecUtil.convert(currentDockingStation.side));

        currentDockingStationIndex = currentDockingStation.index();
        currentDockingStationSide = currentDockingStation.side();
    }

    @Override
    public void undock() {
        if (currentDockingStation != null) {
            currentDockingStation.release(this);
            currentDockingStation = null;

            setSteamDirection(new Vector3d(0, -1, 0));

            currentDockingStationIndex = null;
            currentDockingStationSide = null;
        }
    }

    @Override
    public DockingStation getDockingStation() {
        return currentDockingStation;
    }

    @Override
    public void setMainStation(DockingStation station) {
        if (linkedDockingStation != null && linkedDockingStation != station) {
            linkedDockingStation.unsafeRelease(this);
        }

        linkedDockingStation = station;
        if (station != null) {
            linkedDockingStationIndex = linkedDockingStation.index();
            linkedDockingStationSide = linkedDockingStation.side();
        } else {
            linkedDockingStationIndex = null;
            linkedDockingStationSide = null;
        }
    }

//    @Override
//    public ItemStack getEquipmentInSlot(int var1) {
//        return null;
//    }

    // @Override
    // public int getSizeInventory() {
    //     return inv.length;
    // }

    // @Override
    // public ItemStack getStackInSlot(int var1) {
    //     return inv[var1];
    // }

//    @Override
//    public ItemStack decrStackSize(int var1, int var2) {
//        ItemStack result = inv[var1].splitStack(var2);
//
//        if (inv[var1].stackSize == 0) {
//            inv[var1] = null;
//        }
//
//        updateClientSlot(var1);
//
//        return result;
//    }

//    @Override
//    public ItemStack removeStackFromSlot(int var1) {
//        ItemStack stack = inv[var1];
//        inv[var1] = null;
//        return stack;
//    }

//    @Override
//    public void setInventorySlotContents(int var1, ItemStack var2) {
//        inv[var1] = var2;
//
//        updateClientSlot(var1);
//    }

    // @Override
    // public int getInventoryStackLimit()

    // @Override
    // public void markDirty() {}

    // @Override
    // public boolean isUseableByPlayer(Player var1)

    // @Override
    // public void openInventory(Player player) {}

    // @Override
    // public void closeInventory(Player player) {}

    // @Override
    // public boolean isItemValidForSlot(int var1, ItemStack var2)
    public boolean canPlaceItem(int slot, ItemStack var2) {
        // return inv[var1] == null || (
        return inv.getStackInSlot(slot).isEmpty()
                ||
                (
//                        inv[var1].isItemEqual(var2) &&
                        StackUtil.isSameItemSameDamage(inv.getStackInSlot(slot), var2)
                                &&
                                inv.getStackInSlot(slot).isStackable()
//                                &&
//                                // inv.[slot].getCount() + var2.getCount() <= inv[slot].getItem().getItemStackLimit(inv.getStackInSlot(slot))
//                                inv.getStackInSlot(slot).getCount() + var2.getCount() <= inv.getStackInSlot(slot).getItem().getItemStackLimit(inv.getStackInSlot(slot))
                );
    }

    @Override
    public ITextComponent getDisplayName() {
        // return null;
        return this.board.getNBTHandler().getDisplayNameComponent();
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    // Calen 1.18.2 from TileBC_Neptune
    protected void onSlotChange(IItemHandlerModifiable handler, int slot, @Nonnull ItemStack before, @Nonnull ItemStack after) {
        if (!this.level.isClientSide) {
            this.updateClientSlot(slot);
        }
    }

    public void updateClientSlot(final int slot) {
        IMessage message = createMessage(NET_CLIENT_SET_INVENTORY, (data) ->
        {
            data.writeShort(slot);
            // data.writeItem(inv[slot]);
            data.writeItem(inv.getStackInSlot(slot));
        });
        MessageManager.sendToEntity(message, this);
    }

    @Override
    public boolean isMoving() {
//        return motionX != 0 || motionY != 0 || motionZ != 0;
        return !Vector3d.ZERO.equals(getDeltaMovement());
    }

    @Override
    public void setItemInUse(@Nonnull ItemStack stack) {
        itemInUse = stack;
        IMessage message = createMessage(NET_CLIENT_SET_ITEM_IN_USE, (data) ->
        {
            data.writeItem(itemInUse);
        });
        MessageManager.sendToEntity(message, this);
    }

    private void setSteamDirection(final Vector3d direction) {
        if (!level.isClientSide) {
            IMessage message = createMessage(NET_SET_STEAM_DIRECTION, (data) ->
            {
                data.writeDouble(direction.x);
                data.writeDouble(direction.y);
                data.writeDouble(direction.z);
            });
            MessageManager.sendToEntity(message, this);
        } else {
            steamDirection = direction.normalize();
        }
    }

    public void readPayload(int id, PacketBufferBC buffer, NetworkDirection side, NetworkEvent.Context ctx) throws IOException {
        if (side == NetworkDirection.PLAY_TO_CLIENT) {
            if (NET_CLIENT_SET_ITEM_IN_USE == id) {
                itemInUse = buffer.readItem();
            } else if (NET_CLIENT_SET_INVENTORY == id) {
                int slot = buffer.readUnsignedShort();
                // inv[slot] = buffer.readItem();
                inv.setStackInSlot(slot, buffer.readItem());
            } else if (NET_INITIALIZE == id) {
                itemInUse = buffer.readItem();
                itemActive = buffer.readBoolean();
            } else if (NET_SET_ITEM_ACTIVE == id) {
                itemActive = buffer.readBoolean();
                itemActiveStage = 0;
                lastUpdateTime = new Date().getTime();

                if (!itemActive) {
                    setSteamDirection(new Vector3d(0, -1, 0));
                }
            } else if (NET_SET_STEAM_DIRECTION == id) {
                setSteamDirection(new Vector3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()));
            } else if (NET_SYNC_WEARABLES == id) {
                wearables.clear();

                int amount = buffer.readUnsignedByte();
                while (amount > 0) {
                    wearables.add(buffer.readItem());
                    amount--;
                }
            }
        } else if (side == NetworkDirection.PLAY_TO_SERVER) {
            ServerPlayerEntity p = ctx.getSender();
            if (NET_REQUEST_INITIALIZATION == id) {
                IMessage message0 = createMessage(NET_INITIALIZE, (data) ->
                {
                    data.writeItem(itemInUse);
                    data.writeBoolean(itemActive);
                });
                MessageManager.sendTo(message0, p);

                // for (int i = 0; i < inv.length; ++i)
                for (int i = 0; i < inv.getSlots(); ++i) {
                    final int j = i;
                    IMessage message1 = createMessage(NET_CLIENT_SET_INVENTORY, (data) ->
                    {
                        data.writeShort(j);
                        // data.writeItem(inv[j]);
                        data.writeItem(inv.getStackInSlot(j));
                    });
                    MessageManager.sendTo(message1, p);
                }

                syncWearablesToClient();

                if (currentDockingStation != null) {
                    setSteamDirection(VecUtil.convert(currentDockingStation.side()));
                } else {
                    setSteamDirection(new Vector3d(0, -1, 0));
                }
            }
        }
    }

    public final MessageUpdateEntity createMessage(int id, IPayloadWriter writer) {
        PacketBufferBC buffer = new PacketBufferBC(Unpooled.buffer());
        buffer.writeShort(id);
        writer.write(buffer);
        return new MessageUpdateEntity(this, buffer);
    }

    @Override
    public void setHealth(float par1) {
        // deactivate health management
    }

    @Override
    // public boolean attackEntityFrom(DamageSource source, float f)
    public boolean hurt(DamageSource source, float f) {
        // Ignore hits from mobs or when docked.
        Entity src = source.getEntity();
        if (src != null && !(src instanceof FallingBlockEntity) && !(src instanceof MobEntity) && currentDockingStation == null) {
            if (!ForgeHooks.onLivingAttack(this, source, f)) {
                return false;
            }

            if (!level.isClientSide) {
                // hurtTime = maxHurtTime = 10;
                hurtTime = hurtDuration = 10;

                long mul = 2600;
                for (ItemStack s : wearables) {
                    if (s.getItem() instanceof ArmorItem) {
                        // mul = mul * 2 / (2 + ((ArmorItem) s.getItem()).damageReduceAmount);
                        mul = mul * 2 / (2 + ((ArmorItem) s.getItem()).getDefense());
                    } else {
                        mul *= 0.7;
                    }
                }

                long energy = Math.round(f * mul);
                if (battery.getStored() - energy > 0) {
//                    battery.setEnergy(battery.getEnergyStored() - energy);
                    battery.extractPower(energy);
                    return true;
                } else {
                    onRobotHit(true);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public float getAimYaw() {
        return itemAimYaw;
    }

    @Override
    public float getAimPitch() {
        return itemAimPitch;
    }

    @Override
    public void aimItemAt(float yaw, float pitch) {
        itemAimYaw = yaw;
        itemAimPitch = pitch;

        updateDataServer();
    }

    @Override
    public void aimItemAt(BlockPos pos) {
        Vector3d delta = VecUtil.convert(pos).subtract(VecUtil.getVec(this));
        if (delta.x() != 0 || delta.z() != 0) {
            itemAimYaw = (float) (Math.atan2(delta.x(), delta.z()) * 180f / Math.PI) + 180f;
        }

//        double d3 = MathHelper.sqrt_double(delta.xCoord * delta.xCoord + delta.zCoord * delta.zCoord);
        double d3 = MathHelper.sqrt((float) (delta.x() * delta.x() + delta.z() * delta.z()));
        itemAimPitch = (float) (-(Math.atan2(delta.y(), d3) * 180.0D / Math.PI));

        setSteamDirection(delta);

        updateDataServer();
    }

    private void updateRotationYaw(float maxStep) {
//        float step = MathHelper.wrapAngleTo180_float(itemAimYaw - rotationYaw);
        float step = (itemAimYaw - this.xRot);

        if (step > maxStep) {
            step = maxStep;
        }

        if (step < -maxStep) {
            step = -maxStep;
        }

//        rotationYaw = rotationYaw + step;
        this.xRot = this.xRot + step;
    }

    @Override
    // protected float updateDistance(float targetYaw, float dist)
    protected float tickHeadTurn(float targetYaw, float dist) {
        if (level.isClientSide) {
            // float f2 = Mth.RAD_TO_DEG * (this.getXRot() - this.renderYawOffset);
            float f2 = (this.xRot - this.yBodyRot);
            this.yBodyRot += f2 * 0.5F;
            float f3 = (this.xRot - this.yBodyRot);
            boolean flag = f3 < -90.0F || f3 >= 90.0F;

            this.yBodyRot = this.xRot - f3;

            if (f3 * f3 > 2500.0F) {
                this.yBodyRot += f3 * 0.2F;
            }

            float newDist = dist;
            if (flag) {
                newDist *= -1.0F;
            }

            return newDist;
        }
        return 0;
    }

    @Override
    public void setItemActive(final boolean isActive) {
        if (isActive != itemActive) {
            itemActive = isActive;
            MessageManager.sendToEntity(createMessage(NET_SET_ITEM_ACTIVE, (data) -> {
                data.writeBoolean(isActive);
            }), this);
        }
    }

    @Override
    public RedstoneBoardRobot getBoard() {
        return board;
    }

    @Override
    public DockingStation getLinkedStation() {
        return linkedDockingStation;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    // public boolean isInRangeToRenderDist(double par1)
    public boolean shouldRenderAtSqrDistance(double par1) {
        return true;
    }

    @Override
    public long getPower() {
        return battery.getStored();
    }

    @Override
    public MjBattery getBattery() {
        return battery;
    }

    // Calen 1.18.2: only Mob has this method
//    @Override
//    protected boolean canDespawn() {
//        return false;
//    }

    @Override
    public boolean isAlive() {
        return super.isAlive();
    }

    public AIRobot getOverridingAI() {
        return mainAI.getOverridingAI();
    }

    public void overrideAI(AIRobot ai) {
        mainAI.setOverridingAI(ai);
    }

    public void attackTargetEntityWithCurrentItem(Entity par1Entity) {
        BlockPos entPos = VecUtil.convertFloor(VecUtil.getVec(par1Entity));
        if (MinecraftForge.EVENT_BUS.post(new AttackEntityEvent(FakePlayerProvider.INSTANCE.getFakePlayer((ServerWorld) level, FakePlayerProvider.NULL_PROFILE, entPos), par1Entity))) {
            return;
        }
        // if (par1Entity.canAttackWithItem())
        if (par1Entity.isAttackable()) {
            // if (!par1Entity.hitByEntity(this))
            if (!par1Entity.skipAttackInteraction(this)) {
                float attackDamage = 2.0F;
                int knockback = 0;

                // if (attributes != null)
                if (this.getAttributes() != null) {
                    // for (AttributeModifier modifier : attributes.get(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName()))
                    for (AttributeModifier modifier : this.getAttributes().getInstance(Attributes.ATTACK_DAMAGE).getModifiers()) {
                        switch (modifier.getOperation()) {
                            case ADDITION:
                                attackDamage += modifier.getAmount();
                                break;
                            case MULTIPLY_BASE:
                                attackDamage *= modifier.getAmount();
                                break;
                            case MULTIPLY_TOTAL:
                                attackDamage *= 1.0F + modifier.getAmount();
                                break;
                        }
                    }
                }

                if (par1Entity instanceof LivingEntity) {
                    // FIXME: This was probably meant to do something at some point
                    // attackDamage += EnchantmentHelper.getEnchantmentModifierDamage(this, (EntityLivingBase)
                    // par1Entity);
                    // knockback += EnchantmentHelper.getKnockbackModifier(this);
                    knockback += EnchantmentHelper.getKnockbackBonus(this);
                }

                if (attackDamage > 0.0F) {
                    // int fireAspect = EnchantmentHelper.getFireAspectModifier(this);
                    int fireAspect = EnchantmentHelper.getFireAspect(this);

                    if (par1Entity instanceof LivingEntity && fireAspect > 0 && !par1Entity.isOnFire()) {
//                        par1Entity.setFire(fireAspect * 4);
                        par1Entity.setSecondsOnFire(fireAspect * 4);
                    }

                    if (par1Entity.hurt(new EntityDamageSource("robot", this), attackDamage)) {
                        this.setLastHurtMob(par1Entity);

                        if (knockback > 0) {
                            // par1Entity.addVelocity(
                            par1Entity.push(
                                    (double) (-MathHelper.sin(this.xRot * (float) Math.PI / 180.0F) * (float) knockback * 0.5F),
                                    0.1D,
                                    (double) (MathHelper.cos(this.xRot * (float) Math.PI / 180.0F) * (float) knockback * 0.5F)
                            );
//                            this.motionX *= 0.6D;
//                            this.motionZ *= 0.6D;
                            this.setDeltaMovement(this.getDeltaMovement().x * 0.6D, 1, this.getDeltaMovement().z * 0.6D);
                            this.setSprinting(false);
                        }
                        // FIXME: This was probably meant to do something at some point...

                        // if (par1Entity instanceof EntityLivingBase) {
                        // EnchantmentHelper.((EntityLivingBase) par1Entity, this);
                        // }
                        //
                        // EnchantmentHelper.func_151385_b(this, par1Entity);

                        ItemStack itemstack = itemInUse;

                        // if (itemstack != null && par1Entity instanceof LivingEntity)
                        if (!itemstack.isEmpty() && par1Entity instanceof LivingEntity) {
                            itemstack.getItem().hurtEnemy(itemstack, (LivingEntity) par1Entity, this);
                        }
                    }
                }
            }
        }
    }

    @Override
    public IZone getZoneToWork() {
        return getZone(AreaType.WORK);
    }

    @Override
    public IZone getZoneToLoadUnload() {
        IZone zone = getZone(AreaType.LOAD_UNLOAD);
        if (zone == null) {
            zone = getZoneToWork();
        }
        return zone;
    }

    private IZone getZone(AreaType areaType) {
        if (linkedDockingStation != null) {
            for (StatementSlot s : linkedDockingStation.getActiveActions()) {
                if (s.statement instanceof ActionRobotWorkInArea && ((ActionRobotWorkInArea) s.statement).getAreaType() == areaType) {
                    IZone zone = ActionRobotWorkInArea.getArea(s);

                    if (zone != null) {
                        return zone;
                    }
                }
            }
        }

        return null;
    }

    @Override
    public boolean containsItems() {
        // for (ItemStack element : inv)
        for (ItemStack element : inv.stacks) {
            // if (element != null)
            if (!element.isEmpty()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean hasFreeSlot() {
        // for (ItemStack element : inv)
        for (ItemStack element : inv.stacks) {
            if (element == null) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void unreachableEntityDetected(Entity entity) {
        unreachableEntities.put(entity, level.getGameTime() + 1200);
    }

    @Override
    public boolean isKnownUnreachable(Entity entity) {
        if (unreachableEntities.containsKey(entity)) {
            if (unreachableEntities.get(entity) >= level.getGameTime()) {
                return true;
            } else {
                unreachableEntities.remove(entity);
                return false;
            }
        } else {
            return false;
        }
    }

    protected void onRobotHit(boolean attacked) {
        if (!level.isClientSide) {
            if (attacked) {
                convertToItems();
            } else {
                if (wearables.size() > 0) {
//                    entityDropItem(wearables.remove(wearables.size() - 1), 0);
                    spawnAtLocation(wearables.remove(wearables.size() - 1), 0);
                    syncWearablesToClient();
                }
                // else if (itemInUse != null)
                else if (!itemInUse.isEmpty()) {
//                    entityDropItem(itemInUse, 0);
                    spawnAtLocation(itemInUse, 0);
                    // itemInUse = null;
                    setItemInUse(StackUtil.EMPTY);
                } else {
                    convertToItems();
                }
            }
        }
    }

    @Override
    // protected boolean interact(Player player)
    public ActionResultType interact(PlayerEntity player, Hand hand) {
        // ItemStack stack = player.getCurrentEquippedItem();
        ItemStack stack = player.getItemInHand(hand);
        // if (stack == null || stack.getItem() == null)
        if (stack.isEmpty()) {
            // return false;
            return ActionResultType.PASS;
        }

        RobotEvent.Interact robotInteractEvent = new RobotEvent.Interact(this, player, stack);
        MinecraftForge.EVENT_BUS.post(robotInteractEvent);
        if (robotInteractEvent.isCanceled()) {
            // return false;
            return ActionResultType.PASS;
        }

        if (player.isShiftKeyDown() && stack.getItem().is(OreDictionaryTags.WRENCH)) {
            RobotEvent.Dismantle robotDismantleEvent = new RobotEvent.Dismantle(this, player);
            MinecraftForge.EVENT_BUS.post(robotDismantleEvent);
            if (robotDismantleEvent.isCanceled()) {
                // return false;
                return ActionResultType.PASS;
            }

            onRobotHit(false);

            // if (level.isClientSide) {
            //     ((ItemWrench_Neptune) stack.getItem()).wrenchUsed(player, this);
            if (stack.getItem() == BCItems.Core.WRENCH) {
                ((IToolWrench) stack.getItem()).wrenchUsed(player, hand, stack, new EntityRayTraceResult(this));
            }
            // }
            return ActionResultType.SUCCESS;
        }
        // else if (wearables.size() < MAX_WEARABLES && stack.getItem().isValidArmor(stack, 0, this))
        else if (wearables.size() < MAX_WEARABLES && stack.getItem().canEquip(stack, EquipmentSlotType.HEAD, this)) {
            if (!level.isClientSide) {
                wearables.add(stack.split(1));
                syncWearablesToClient();
            } else {
//                player.swingItem();
                player.swing(Hand.MAIN_HAND);
            }
            return ActionResultType.SUCCESS;
        } else if (wearables.size() < MAX_WEARABLES && stack.getItem() instanceof IRobotOverlayItem && ((IRobotOverlayItem) stack.getItem())
                .isValidRobotOverlay(stack)) {
            if (!level.isClientSide) {
                wearables.add(stack.split(1));
                syncWearablesToClient();
            } else {
//                player.swingItem();
                player.swing(Hand.MAIN_HAND);
            }
            return ActionResultType.SUCCESS;
        }
        // else if (wearables.size() < MAX_WEARABLES && stack.getItem() instanceof SkullItem)
        else if (wearables.size() < MAX_WEARABLES && stack.getItem().is(Tags.Items.HEADS)) {
            if (!level.isClientSide) {
                ItemStack skullStack = stack.split(1);
                initSkullItem(skullStack);
                wearables.add(skullStack);
                syncWearablesToClient();
            } else {
                // player.swingItem();
                player.swing(hand);
            }
            return ActionResultType.SUCCESS;
        } else {
            return super.interact(player, hand);
        }
    }

    private void initSkullItem(ItemStack skullStack) {
        if (skullStack.hasTag()) {
            CompoundNBT nbttagcompound = skullStack.getTag();
            GameProfile gameProfile = null;

            if (nbttagcompound.contains("SkullOwner", Constants.NBT.TAG_COMPOUND)) {
                gameProfile = NBTUtil.readGameProfile(nbttagcompound.getCompound("SkullOwner"));
            } else if (nbttagcompound.contains("SkullOwner", Constants.NBT.TAG_STRING) && !StringUtil.isNullOrEmpty(nbttagcompound.getString("SkullOwner"))) {
                gameProfile = new GameProfile(null, nbttagcompound.getString("SkullOwner"));
            }
            if (gameProfile != null && !StringUtils.isNullOrEmpty(gameProfile.getName())) {
                if (!gameProfile.isComplete() || !gameProfile.getProperties().containsKey("textures")) {
                    // // TODO: FIND OUT HOW SKULLS LOAD GAME PROFILES
                    // gameProfile = MinecraftServer.getServer().getGameProfileRepository().(gameProfile.getName());
                    gameProfile = this.getServer().getProfileCache().get(gameProfile.getName());

                    if (gameProfile != null) {
                        Property property = (Property) Iterables.getFirst(gameProfile.getProperties().get("textures"), (Object) null);

                        if (property == null) {
                            // gameProfile =
                            // MinecraftServer.getServer().func_147130_as().fillProfileProperties(gameProfile, true);
                        }
                    }
                }
            }
            if (gameProfile != null && gameProfile.isComplete() && gameProfile.getProperties().containsKey("textures")) {
                CompoundNBT profileNBT = new CompoundNBT();
                NBTUtil.writeGameProfile(profileNBT, gameProfile);
                nbttagcompound.put("SkullOwner", profileNBT);
            } else {
                nbttagcompound.remove("SkullOwner");
            }
        }
    }

    private void syncWearablesToClient() {
//        MessageManager.sendToEntity(new PacketCommand(this, "syncWearables", new CommandWriter() {
//            public void write(FriendlyByteBuf data) {
//                data.writeByte(wearables.size());
//                for (ItemStack s : wearables) {
//                    data.writeItem(s);
//                }
//            }
//        }), this);
        IMessage message = createMessage(NET_SYNC_WEARABLES, (data) ->
        {
            data.writeByte(wearables.size());
            for (ItemStack s : wearables) {
                data.writeItem(s);
            }
        });
        MessageManager.sendToEntity(message, this);
    }

    private List<ItemStack> getDrops() {
        List<ItemStack> drops = new ArrayList<ItemStack>();
        drops.add(ItemRobot.createRobotStack(board.getNBTHandler(), battery.getStored()));
        // if (itemInUse != null)
        if (!itemInUse.isEmpty()) {
            drops.add(itemInUse);
        }
        // for (ItemStack element : inv)
        for (ItemStack element : inv.stacks) {
            if (element != null) {
                drops.add(element);
            }
        }
        drops.addAll(wearables);
        return drops;
    }

    private void convertToItems() {
        if (!level.isClientSide && !dead) {
            if (mainAI != null) {
                mainAI.abort();
            }
            List<ItemStack> drops = getDrops();
            for (ItemStack stack : drops) {
                spawnAtLocation(stack, 0);
            }
            dead = true;
            forceRemove();
        }

        getRegistry().killRobot(this);
    }

    @Override
    public void kill() {
        if (level.isClientSide) {
            super.kill();
        }
    }

    // Calen 1.18.2
    private void forceRemove() {
        super.kill();
        dead = true;
        remove();
        setHealth(0);
    }

    @Override
    public void onChunkUnload() {
        getRegistry().unloadRobot(this);
    }

    @Override
    // public boolean canBePushed()
    public boolean isPushable() {
        return false;
    }

    // 1.18.2 forced
    @Override
    public HandSide getMainArm() {
        return HandSide.RIGHT;
    }

    @Override
    // protected void collideWithEntity(Entity par1Entity)
    public void doPush(Entity par1Entity) {

    }

    @Override
    // public void applyEntityCollision(Entity par1Entity)
    public void push(Entity par1Entity) {

    }

    public void setUniqueRobotId(long iRobotId) {
        robotId = iRobotId;
    }

    @Override
    public long getRobotId() {
        return robotId;
    }

    @Override
    public IRobotRegistry getRegistry() {
        return RobotManager.registryProvider.getRegistry(level);
    }

    @Override
    public void releaseResources() {
        getRegistry().releaseResources(this);
    }

    /** Tries to receive items in parameters, return items that are left after the operation. */
    @Override
    public ItemStack receiveItem(TileEntity tile, ItemStack stack) {
        // if (currentDockingStation != null && currentDockingStation.index().subtract(tile.getBlockPos()).distSqr(BlockPos.ZERO) == 1 && mainAI != null)
        if (currentDockingStation != null && VecUtil.distanceSq(currentDockingStation.index().subtract(tile.getBlockPos()), BlockPos.ZERO) == 1 && mainAI != null) {

            return mainAI.getActiveAI().receiveItem(stack);
        } else {
            return stack;
        }
    }

//    @Override
//    public int fill(EnumFacing from, FluidStack resource, boolean doFill) {
//        int result = 0;
//
//        if (tank != null && !tank.isFluidEqual(resource)) {
//            return 0;
//        }
//
//        if (tank == null) {
//            tank = new FluidStack(resource.getFluid(), 0);
//        }
//
//        if (tank.amount + resource.amount <= maxFluid) {
//            result = resource.amount;
//
//            if (doFill) {
//                tank.amount += resource.amount;
//            }
//        } else {
//            result = maxFluid - tank.amount;
//
//            if (doFill) {
//                tank.amount = maxFluid;
//            }
//        }
//
//        if (tank != null && tank.amount == 0) {
//            tank = null;
//        }
//
//        return result;
//    }

//    @Override
//    public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain) {
//        if (tank != null && tank.isFluidEqual(resource)) {
//            return drain(from, resource.amount, doDrain);
//        } else {
//            return null;
//        }
//    }

//    @Override
//    public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain) {
//        FluidStack result = null;
//
//        if (tank == null) {
//            result = null;
//        } else if (tank.amount <= maxDrain) {
//            result = tank.copy();
//
//            if (doDrain) {
//                tank = null;
//            }
//        } else {
//            result = tank.copy();
//            result.amount = maxDrain;
//
//            if (doDrain) {
//                tank.amount -= maxDrain;
//            }
//        }
//
//        if (tank != null && tank.amount == 0) {
//            tank = null;
//        }
//
//        return result;
//    }

//    @Override
//    public boolean canFill(EnumFacing from, Fluid fluid) {
//        return tank == null || tank.amount == 0 || (tank.amount < maxFluid && tank.getFluid().getID() == fluid.getID());
//    }

//    @Override
//    public boolean canDrain(Direction from, Fluid fluid) {
//        return tank != null && tank.getAmount() != 0 && tank.getRawFluid() == fluid;
//    }

//    @Override
//    public FluidTankInfo[] getTankInfo(EnumFacing from) {
//        return new FluidTankInfo[] { new FluidTankInfo(tank, maxFluid) };
//    }

    @Override
    public void getDebugInfo(List<ITextComponent> left, List<ITextComponent> right, Direction side) {
        left.add(new StringTextComponent("Robot " + board.getNBTHandler().getID() + " (" + MjAPI.formatMj(getBattery().getStored()) + "/").append(LocaleUtil.localizeMjComponent(getBattery().getCapacity())).append(new StringTextComponent(")")));
        left.add(new StringTextComponent(String.format("Position: %.2f, %.2f, %.2f", getX(), getY(), getZ())));
        left.add(new StringTextComponent("AI tree:"));
        AIRobot aiRobot = mainAI;
        while (aiRobot != null) {
            left.add(new StringTextComponent("- " + RobotManager.getAIRobotName(aiRobot.getClass()) + " (").append(LocaleUtil.localizeMjFlow((aiRobot.getPowerCost()))).append(new StringTextComponent(")")));
            if (aiRobot instanceof IDebuggable) {
                ((IDebuggable) aiRobot).getDebugInfo(left, right, side);
            }
            aiRobot = aiRobot.getDelegateAI();
        }
    }

    public long receiveEnergy(long maxReceive, boolean simulate) {
        // long energyReceived = getBattery().addPower(maxReceive, simulate);
        long energyExcess = getBattery().addPower(maxReceive, simulate);
        long energyReceived = maxReceive - energyExcess;

        // 5 RF/t is set as the "sleep threshold" for detecting charging.
        if (!simulate && energyReceived > 500 * MjAPI.MJ && ticksCharging <= 25) {
            ticksCharging += 5;
        }

        // return energyReceived;
        return energyExcess;
    }

    public List<ItemStack> getWearables() {
        return wearables;
    }

    // Something to do with IInventory

//    @Override
//    public int getField(int id) {
//        return 0;
//    }

//    @Override
//    public void setField(int id, int value) {}

//    @Override
//    public int getFieldCount() {
//        return 0;
//    }

//    @Override
//    public void clear() {}

    private void updateItem(@Nonnull ItemStack stack, int i, boolean held) {
        // if (stack != null && stack.getItem() != null)
        if (!stack.isEmpty()) {
            ResourceLocation id = stack.getItem().getRegistryName();
            // did this item not throw an exception before?
            if (!blacklistedItemsForUpdate.contains(id)) {
                try {
                    // stack.getItem().onUpdate(stack, level, this, i, held);
                    stack.getItem().inventoryTick(stack, level, this, i, held);
                } catch (Exception e) {
                    // the item threw an exception, print it and do not let it update once more
                    // e.printStackTrace();
                    BCLog.logger.error("[robotics.robot.updateItem] Failed to update item: [" + stack + "]", e);
                    blacklistedItemsForUpdate.add(id);
                }
            }
        }
    }

    // Calen 1.18.2
    @Override
    public final IMessage receivePayload(NetworkEvent.Context ctx, PacketBufferBC buffer) throws IOException {
        int id = buffer.readUnsignedShort();
        readPayload(id, buffer, ctx.getDirection(), ctx);

        // Make sure that we actually read the entire message rather than just discarding it
        MessageUtil.ensureEmpty(buffer, level.isClientSide, getClass() + ", id = " + getIdAllocator().getNameFor(id));

        if (ctx.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            spawnReceiveParticles(id);
        }
        return null;
    }

    private CompoundNBT writeLongToNbt(long value) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putLong("v", value);
        return nbt;
    }

    private long readLongFromNbt(CompoundNBT nbt) {
        return nbt.getLong("v");
    }

    // Calen 1.18.2 from TileBC_Neptune

    public IdAllocator getIdAllocator() {
        return IDS;
    }

    private void spawnReceiveParticles(int id) {
        if (DEBUG) {
            String name = getIdAllocator().getNameFor(id);

            if (level != null) {
                double x = blockPosition().getX() + 0.5;
                double y = blockPosition().getY() + 0.5;
                double z = blockPosition().getZ() + 0.5;
                double r = 0.01 + (id & 3) / 4.0;
                double g = 0.01 + ((id / 4) & 3) / 4.0;
                double b = 0.01 + ((id / 16) & 3) / 4.0;
                level.addParticle(RedstoneParticleData.REDSTONE, x, y, z, r, g, b);
            }
        }
    }

    // Calen 1.18.2
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing) {
        LazyOptional<T> obj = caps.getCapability(capability, facing);
        if (!obj.isPresent()) {
            obj = super.getCapability(capability, facing);
        }
        return obj;
    }

    public static AttributeModifierMap createAttributes() {
        return AttributeModifierMap.builder()
                .add(Attributes.MAX_HEALTH, 1)
                .add(Attributes.KNOCKBACK_RESISTANCE)
                .add(Attributes.MOVEMENT_SPEED)
                .add(Attributes.ARMOR)
                .add(Attributes.ARMOR_TOUGHNESS)
                .add(ForgeMod.SWIM_SPEED.get())
                .add(ForgeMod.NAMETAG_DISTANCE.get())
                .add(ForgeMod.ENTITY_GRAVITY.get(), 0)
                .build();
    }
}
