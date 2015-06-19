package buildcraft.core.lib.block;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.BuildCraftProperties;
import buildcraft.api.enums.EnumColor;
import buildcraft.api.enums.EnumEnergyStage;
import buildcraft.api.enums.EnumEngineType;
import buildcraft.api.enums.EnumFillerPattern;
import buildcraft.api.enums.EnumSpring;
import buildcraft.api.properties.BuildCraftProperty;
import buildcraft.core.BCCreativeTab;

public abstract class BlockBuildCraftBase extends Block {

    public static final BuildCraftProperty<EnumFacing> FACING_PROP = BuildCraftProperties.BLOCK_FACING;
    public static final BuildCraftProperty<EnumFacing> FACING_6_PROP = BuildCraftProperties.BLOCK_FACING_6;

    public static final BuildCraftProperty<EnumEngineType> ENGINE_TYPE = BuildCraftProperties.ENGINE_TYPE;
    public static final BuildCraftProperty<EnumColor> COLOR_PROP = BuildCraftProperties.BLOCK_COLOR;
    public static final BuildCraftProperty<EnumSpring> SPRING_TYPE = BuildCraftProperties.SPRING_TYPE;
    public static final BuildCraftProperty<EnumEnergyStage> ENERGY_STAGE = BuildCraftProperties.ENERGY_STAGE;
    public static final BuildCraftProperty<EnumFillerPattern> FILLER_PATTERN = BuildCraftProperties.FILLER_PATTERN;

    public static final BuildCraftProperty<Boolean> JOINED_BELOW = BuildCraftProperties.JOINED_BELOW;
    public static final BuildCraftProperty<Boolean> MOVING = BuildCraftProperties.MOVING;
    public static final BuildCraftProperty<Boolean> LED_POWER = BuildCraftProperties.LED_POWER;
    public static final BuildCraftProperty<Boolean> LED_ACTIVE = BuildCraftProperties.LED_ACTIVE;

    protected final BuildCraftProperty<?>[] properties;
    protected final BuildCraftProperty<?>[] nonMetaProperties;
    protected final Map<Integer, IBlockState> intToState = Maps.newHashMap();
    protected final Map<IBlockState, Integer> stateToInt = Maps.newHashMap();
    protected final BlockState myBlockState;

    /** True if this block can rotate in any of the horizontal directions */
    public final boolean horizontallyRotatable;
    /** True if this block can rotate in any of the six facing directions */
    public final boolean allRotatable;

    protected BlockBuildCraftBase(Material material) {
        this(material, BCCreativeTab.get("main"), new BuildCraftProperty<?>[0]);
    }

    protected BlockBuildCraftBase(Material material, BCCreativeTab creativeTab) {
        this(material, creativeTab, new BuildCraftProperty<?>[0]);
    }

    protected BlockBuildCraftBase(Material material, BuildCraftProperty<?>... properties) {
        this(material, BCCreativeTab.get("main"), properties);
    }

    protected BlockBuildCraftBase(Material material, BCCreativeTab bcCreativeTab, BuildCraftProperty<?>... properties) {
        super(material);
        setCreativeTab(bcCreativeTab);
        setHardness(5F);
        List<BuildCraftProperty<?>> metas = Lists.newArrayList();
        List<BuildCraftProperty<?>> nonMetas = Lists.newArrayList();

        int total = 1;
        for (BuildCraftProperty<?> prop : properties) {
            total *= prop.getAllowedValues().size();

            if (total > 16) {
                nonMetas.add(prop);
            } else {
                metas.add(prop);
            }
        }

        this.properties = metas.toArray(new BuildCraftProperty<?>[0]);
        this.nonMetaProperties = nonMetas.toArray(new BuildCraftProperty<?>[0]);

        this.myBlockState = createBlockState();

        IBlockState defaultState = getBlockState().getBaseState();

        Map<IBlockState, Integer> tempValidStates = Maps.newHashMap();
        tempValidStates.put(defaultState, 0);
        boolean canRotate = false;
        boolean canSixRotate = false;

        for (BuildCraftProperty<?> prop : properties) {
            if (prop == FACING_PROP) {
                canRotate = true;
            }
            if (prop == FACING_6_PROP) {
                canRotate = true;
                canSixRotate = true;
            }

            Collection<? extends Comparable<?>> allowedValues = prop.getAllowedValues();
            defaultState = defaultState.withProperty(prop, allowedValues.iterator().next());

            Map<IBlockState, Integer> newValidStates = Maps.newHashMap();
            int mul = metas.contains(prop) ? allowedValues.size() : 1;
            for (Entry<IBlockState, Integer> entry : tempValidStates.entrySet()) {
                int index = 0;
                Collections.sort((List) allowedValues);
                for (Comparable<?> comp : allowedValues) {
                    int pos = entry.getValue() * mul + index;
                    newValidStates.put(entry.getKey().withProperty(prop, comp), pos);
                    if (mul > 1) {
                        index++;
                    }
                }
            }
            tempValidStates = newValidStates;
        }

        horizontallyRotatable = canRotate;
        allRotatable = canSixRotate;

        for (Entry<IBlockState, Integer> entry : tempValidStates.entrySet()) {
            int i = entry.getValue();
            stateToInt.put(entry.getKey(), i);
            if (!intToState.containsKey(i)) {
                intToState.put(i, entry.getKey());
            }
        }

        // Temporary. Used for debugging early on.
        BCLog.logger.info("Created the block " + getUnlocalizedName() + " as " + getClass().getSimpleName());

        BCLog.logger.info("Int -> State: ");
        for (Entry<Integer, IBlockState> entry : intToState.entrySet()) {
            BCLog.logger.info("  " + entry.getKey() + " -> " + entry.getValue());
        }

        BCLog.logger.info("State -> Int: ");
        for (Entry<IBlockState, Integer> entry : stateToInt.entrySet()) {
            BCLog.logger.info("  " + entry.getKey() + " -> " + entry.getValue());
        }

        setDefaultState(defaultState);

        for (BuildCraftProperty<?> prop : properties) {
            if (metas.contains(prop)) {
                BCLog.logger.info("  The value of " + prop.getName() + " is saved to disk");
            } else {
                BCLog.logger.info("  The value of " + prop.getName() + " is implied by surroundings and is temparary");
            }
        }
    }

    @Override
    public BlockState getBlockState() {
        return this.myBlockState;
    }

    @Override
    protected BlockState createBlockState() {
        if (properties == null) {
            // Will be overridden later
            return new BlockState(this, new IProperty[] {});
        }

        IProperty[] props = new IProperty[properties.length + nonMetaProperties.length];
        for (int i = 0; i < properties.length; i++) {
            props[i] = properties[i];
        }
        for (int i = 0; i < nonMetaProperties.length; i++) {
            props[properties.length + i] = nonMetaProperties[i];
        }
        return new BlockState(this, props);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return stateToInt.get(state);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return intToState.get(meta);
    }

    @Override
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta,
            EntityLivingBase placer) {
        if (allRotatable) {// TODO (CHECK): Do we want to do this for all blocks that have 6 facing directions
            return getStateFromMeta(meta).withProperty(FACING_6_PROP, facing);
        } else {
            return getStateFromMeta(meta);
        }
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity, ItemStack stack) {
        // If it was allRotatable the vertical direction would have been taken into account above
        if (horizontallyRotatable && !allRotatable) {
            EnumFacing orientation = entity.getHorizontalFacing();
            world.setBlockState(pos, state.withProperty(FACING_PROP, orientation.getOpposite()));
        }
    }

    public AxisAlignedBB getBox(IBlockAccess world, BlockPos pos, IBlockState state) {
        return new AxisAlignedBB(0, 0, 0, 1, 1, 1);
    }

    public AxisAlignedBB[] getBoxes(IBlockAccess world, BlockPos pos, IBlockState state) {
        return new AxisAlignedBB[] { getBox(world, pos, state) };
    }

    @Override
    public MovingObjectPosition collisionRayTrace(World world, BlockPos pos, Vec3 origin, Vec3 direction) {
        IBlockState state = world.getBlockState(pos);
        AxisAlignedBB[] aabbs = getBoxes(world, pos, state);
        MovingObjectPosition closest = null;
        for (AxisAlignedBB aabb : aabbs) {
            aabb = aabb.offset(pos.getX(), pos.getY(), pos.getZ()).expand(-0.01, -0.01, -0.01);

            MovingObjectPosition mop = aabb.calculateIntercept(origin, direction);
            if (mop != null) {
                if (closest != null && mop.hitVec.distanceTo(origin) < closest.hitVec.distanceTo(origin)) {
                    closest = mop;
                } else {
                    closest = mop;
                }
            }
        }
        if (closest == null) {
            return null;
        } else {
            return new MovingObjectPosition(closest.hitVec, closest.sideHit, pos);
        }
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void addCollisionBoxesToList(World world, BlockPos pos, IBlockState state, AxisAlignedBB mask, List list, Entity par7Entity) {
        if (!isCollidable()) {
            return;
        } else {
            for (AxisAlignedBB bb : getBoxes(world, pos, state)) {
                bb = bb.offset(pos.getX(), pos.getY(), pos.getZ());
                if (mask.intersectsWith(bb)) {
                    list.add(bb);
                }
            }
        }
    }

    public boolean canCollideCheck(IBlockState state, boolean hitIfLiquid) {
        return true;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(World world, BlockPos pos, IBlockState state) {
        if (isCollidable()) {
            return getBox(world, pos, state).offset(pos.getX(), pos.getY(), pos.getZ());
        } else {
            return null;
        }
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBox(World world, BlockPos pos) {
        return getBox(world, pos, world.getBlockState(pos)).offset(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, BlockPos pos) {
        AxisAlignedBB[] bbs = getBoxes(world, pos, world.getBlockState(pos));
        AxisAlignedBB bb = bbs[0];
        for (int i = 1; i < bbs.length; i++) {
            bb = bb.union(bbs[i]);
        }

        minX = bb.minX;
        minY = bb.minY;
        minZ = bb.minZ;

        maxX = bb.maxX;
        maxY = bb.maxY;
        maxZ = bb.maxZ;
    }
}
