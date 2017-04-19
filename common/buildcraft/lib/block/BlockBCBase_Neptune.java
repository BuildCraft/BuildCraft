/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.block;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import buildcraft.api.properties.BuildCraftProperties;

import buildcraft.lib.item.IItemBuildCraft;
import buildcraft.lib.item.ItemBlockBC_Neptune;
import buildcraft.lib.item.ItemManager;
import buildcraft.lib.registry.CreativeTabManager;
import buildcraft.lib.registry.MigrationManager;
import buildcraft.lib.registry.RegistryHelper;
import buildcraft.lib.registry.TagManager;
import buildcraft.lib.registry.TagManager.EnumTagType;
import buildcraft.lib.registry.TagManager.EnumTagTypeMulti;

public class BlockBCBase_Neptune extends Block {
    private static List<BlockBCBase_Neptune> registeredBlocks = new ArrayList<>();
    public static final IProperty<EnumFacing> PROP_FACING = BuildCraftProperties.BLOCK_FACING;
    public static final IProperty<EnumFacing> BLOCK_FACING_6 = BuildCraftProperties.BLOCK_FACING_6;

    /** The tag used to identify this in the {@link TagManager} */
    public final String id;

    public BlockBCBase_Neptune(Material material, String id) {
        super(material);
        this.id = id;

        // Sensible default block properties
        setHardness(5.0F);
        setResistance(10.0F);
        setSoundType(SoundType.METAL);

        // Init names from the tag manager
        setUnlocalizedName(TagManager.getTag(id, EnumTagType.UNLOCALIZED_NAME));
        setRegistryName(TagManager.getTag(id, EnumTagType.REGISTRY_NAME));
        setCreativeTab(CreativeTabManager.getTab(TagManager.getTag(id, EnumTagType.CREATIVE_TAB)));

        if (this instanceof IBlockWithFacing) {
            setDefaultState(getDefaultState().withProperty(((IBlockWithFacing) this).getFacingProperty(), EnumFacing.NORTH));
        }
    }

    // IBlockState

    protected void addProperties(List<IProperty<?>> properties) {
        if (this instanceof IBlockWithFacing) {
            properties.add(((IBlockWithFacing) this).getFacingProperty());
        }
    }

    @Override
    protected BlockStateContainer createBlockState() {
        List<IProperty<?>> properties = new ArrayList<>();
        addProperties(properties);
        return new BlockStateContainer(this, properties.toArray(new IProperty<?>[0]));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = 0;
        if (this instanceof IBlockWithFacing) {
            if (((IBlockWithFacing) this).canPlacedVertical()) {
                meta |= state.getValue(((IBlockWithFacing) this).getFacingProperty()).getIndex();
            } else {
                meta |= state.getValue(((IBlockWithFacing) this).getFacingProperty()).getHorizontalIndex();
            }
        }
        return meta;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        IBlockState state = getDefaultState();
        if (this instanceof IBlockWithFacing) {
            if (((IBlockWithFacing) this).canPlacedVertical()) {
                state = state.withProperty(((IBlockWithFacing) this).getFacingProperty(), EnumFacing.getFront(meta & 7));
            } else {
                state = state.withProperty(((IBlockWithFacing) this).getFacingProperty(), EnumFacing.getHorizontal(meta & 3));
            }
        }
        return state;
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        if (this instanceof IBlockWithFacing) {
            EnumFacing facing = state.getValue(((IBlockWithFacing) this).getFacingProperty());
            state = state.withProperty(((IBlockWithFacing) this).getFacingProperty(), rot.rotate(facing));
        }
        return state;
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirror) {
        if (this instanceof IBlockWithFacing) {
            EnumFacing facing = state.getValue(((IBlockWithFacing) this).getFacingProperty());
            state = state.withProperty(((IBlockWithFacing) this).getFacingProperty(), mirror.mirror(facing));
        }
        return state;
    }

    // Others

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        IBlockState state = super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand);
        if (this instanceof IBlockWithFacing) {
            EnumFacing orientation = placer.getHorizontalFacing();
            if (((IBlockWithFacing) this).canPlacedVertical()) {
                if (MathHelper.abs((float) placer.posX - pos.getX()) < 2.0F && MathHelper.abs((float) placer.posZ - pos.getZ()) < 2.0F) {
                    double y = placer.posY + placer.getEyeHeight();

                    if (y - pos.getY() > 2.0D) {
                        orientation = EnumFacing.DOWN;
                    }

                    if (pos.getY() - y > 0.0D) {
                        orientation = EnumFacing.UP;
                    }
                }
            }
            state = state.withProperty(((IBlockWithFacing) this).getFacingProperty(), orientation.getOpposite());
        }
        return state;
    }

    public static <B extends BlockBCBase_Neptune> B register(B block) {
        return register(block, false, ItemBlockBC_Neptune::new);
    }

    public static <B extends BlockBCBase_Neptune> B register(B block, boolean force) {
        return register(block, force, ItemBlockBC_Neptune::new);
    }

    public static <B extends BlockBCBase_Neptune, I extends Item & IItemBuildCraft> B register(B block, Function<B, I> itemBlockConstructor) {
        return register(block, false, itemBlockConstructor);
    }

    public static <B extends BlockBCBase_Neptune, I extends Item & IItemBuildCraft> B register(B block, boolean force, Function<B, I> itemBlockConstructor) {
        if (RegistryHelper.registerBlock(block, force)) {
            registeredBlocks.add(block);
            MigrationManager.INSTANCE.addBlockMigration(block, TagManager.getMultiTag(block.id, EnumTagTypeMulti.OLD_REGISTRY_NAME));
            if (itemBlockConstructor != null) {
                I item = itemBlockConstructor.apply(block);
                if (item != null) {
                    ItemManager.register(item, true);
                }
            }
            return block;
        }
        return null;
    }
}
