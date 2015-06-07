package buildcraft.core.lib.block;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IUnlistedProperty;

import buildcraft.api.core.BuildCraftProperties;
import buildcraft.api.enums.EnumColor;
import buildcraft.api.enums.EnumEngineType;
import buildcraft.api.enums.EnumSpring;
import buildcraft.api.properties.BuildCraftProperty;
import buildcraft.core.BCCreativeTab;
import buildcraft.core.lib.utils.Utils;

public abstract class BlockBuildCraftBase extends Block {

    public static final BuildCraftProperty<EnumFacing> FACING_PROP = BuildCraftProperties.BLOCK_FACING;
    public static final BuildCraftProperty<EnumFacing> FACING_6_PROP = BuildCraftProperties.BLOCK_FACING_6;

    public static final BuildCraftProperty<EnumEngineType> ENGINE_TYPE = BuildCraftProperties.ENGINE_TYPE;
    public static final BuildCraftProperty<EnumColor> COLOR_PROP = BuildCraftProperties.BLOCK_COLOR;
    public static final BuildCraftProperty<EnumSpring> SPRING_TYPE = BuildCraftProperties.SPRING_TYPE;

    public static final BuildCraftProperty<Boolean> JOINED_BELOW = BuildCraftProperties.JOINED_BELOW;

    protected final IProperty[] properties;
    protected final HashBiMap<Integer, IBlockState> validStates = HashBiMap.create();
    protected final BlockState myBlockState;

    private boolean rotatable = false;

    protected BlockBuildCraftBase(Material material) {
        this(material, BCCreativeTab.get("main"), new IProperty[0], new IUnlistedProperty[0]);
    }

    protected BlockBuildCraftBase(Material material, BCCreativeTab creativeTab) {
        this(material, creativeTab, new IProperty[0], new IUnlistedProperty[0]);
    }

    protected BlockBuildCraftBase(Material material, IProperty... properties) {
        this(material, BCCreativeTab.get("main"), properties, new IUnlistedProperty[0]);
    }

    protected BlockBuildCraftBase(Material material, IProperty[] properties, IUnlistedProperty<?>[] nonMetaProperties) {
        this(material, BCCreativeTab.get("main"), properties, nonMetaProperties);
    }

    protected BlockBuildCraftBase(Material material, BCCreativeTab bcCreativeTab, IProperty[] properties, IUnlistedProperty<?>[] nonMetaProperties) {
        super(material);
        setCreativeTab(bcCreativeTab);
        setHardness(5F);
        this.properties = properties;

        this.myBlockState = createBlockState();

        IBlockState defaultState = getBlockState().getBaseState();

        int total = 1;
        List<IBlockState> tempValidStates = Lists.newArrayList();
        tempValidStates.add(defaultState);
        for (IProperty prop : properties) {
            total *= prop.getAllowedValues().size();
            if (total > 16)
                throw new IllegalArgumentException("Cannot have more than 16 properties in a block!");

            Collection<Comparable<?>> allowedValues = prop.getAllowedValues();
            defaultState = defaultState.withProperty(prop, allowedValues.iterator().next());

            List<IBlockState> newValidStates = Lists.newArrayList();
            for (IBlockState state : tempValidStates) {
                for (Comparable<?> comp : allowedValues) {
                    newValidStates.add(state.withProperty(prop, comp));
                }
            }
            tempValidStates = newValidStates;
        }

        int i = 0;
        for (IBlockState state : tempValidStates) {
            validStates.put(i, state);
            i++;
        }

        setDefaultState(defaultState);
    }

    public boolean isRotatable() {
        return rotatable;
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

        return new BlockState(this, properties);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return validStates.inverse().get(state);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return validStates.get(meta);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity, ItemStack stack) {
        if (isRotatable()) {
            EnumFacing orientation = Utils.get2dOrientation(entity);
            world.setBlockState(pos, state.withProperty(FACING_PROP, orientation.getOpposite()));
        }
    }
}
