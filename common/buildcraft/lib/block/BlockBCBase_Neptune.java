/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.block;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import buildcraft.api.properties.BuildCraftProperties;

import buildcraft.lib.registry.CreativeTabManager;
import buildcraft.lib.registry.TagManager;
import buildcraft.lib.registry.TagManager.EnumTagType;

public class BlockBCBase_Neptune extends Block {
    public static final IProperty<EnumFacing> PROP_FACING = BuildCraftProperties.BLOCK_FACING;
    public static final IProperty<EnumFacing> BLOCK_FACING_6 = BuildCraftProperties.BLOCK_FACING_6;

    /** The tag used to identify this in the {@link TagManager}. Note that this may be empty if this block doesn't use
     * the tag system. */
    public final String id;

    /** @param id The ID that will be looked up in the {@link TagManager} when registering blocks. Pass null or the
     *            empty string to bypass the {@link TagManager} entirely. */
    public BlockBCBase_Neptune(Material material, String id) {
        super(material);
        if (id == null) {
            id = "";
        }
        this.id = id;

        // Sensible default block properties
        setHardness(5.0F);
        setResistance(10.0F);
        setSoundType(SoundType.METAL);

        if (!id.isEmpty()) {
            // Init names from the tag manager
            setUnlocalizedName(TagManager.getTag(id, EnumTagType.UNLOCALIZED_NAME));
            setRegistryName(TagManager.getTag(id, EnumTagType.REGISTRY_NAME));
            setCreativeTab(CreativeTabManager.getTab(TagManager.getTag(id, EnumTagType.CREATIVE_TAB)));
        }

        if (this instanceof IBlockWithFacing) {
            IProperty<EnumFacing> facingProp = ((IBlockWithFacing) this).getFacingProperty();
            setDefaultState(getDefaultState().withProperty(facingProp, EnumFacing.NORTH));
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
            if (((IBlockWithFacing) this).canFaceVertically()) {
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
            IBlockWithFacing b = (IBlockWithFacing) this;
            IProperty<EnumFacing> prop = b.getFacingProperty();
            if (b.canFaceVertically()) {
                state = state.withProperty(prop, EnumFacing.getFront(meta & 7));
            } else {
                state = state.withProperty(prop, EnumFacing.getHorizontal(meta & 3));
            }
        }
        return state;
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        if (this instanceof IBlockWithFacing) {
            IProperty<EnumFacing> prop = ((IBlockWithFacing) this).getFacingProperty();
            EnumFacing facing = state.getValue(prop);
            state = state.withProperty(prop, rot.rotate(facing));
        }
        return state;
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirror) {
        if (this instanceof IBlockWithFacing) {
            IProperty<EnumFacing> prop = ((IBlockWithFacing) this).getFacingProperty();
            EnumFacing facing = state.getValue(prop);
            state = state.withProperty(prop, mirror.mirror(facing));
        }
        return state;
    }

    // Others

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY,
        float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        IBlockState state = super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand);
        if (this instanceof IBlockWithFacing) {
            EnumFacing orientation = placer.getHorizontalFacing();
            IBlockWithFacing b = (IBlockWithFacing) this;
            if (b.canFaceVertically()) {
                if (MathHelper.abs((float) placer.posX - pos.getX()) < 2.0F
                    && MathHelper.abs((float) placer.posZ - pos.getZ()) < 2.0F) {
                    double y = placer.posY + placer.getEyeHeight();

                    if (y - pos.getY() > 2.0D) {
                        orientation = EnumFacing.DOWN;
                    }

                    if (pos.getY() - y > 0.0D) {
                        orientation = EnumFacing.UP;
                    }
                }
            }
            state = state.withProperty(b.getFacingProperty(), orientation.getOpposite());
        }
        return state;
    }

    @Override
    public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis) {
        if (this instanceof IBlockWithFacing) {
            if (!((IBlockWithFacing) this).canBeRotated(world, pos, world.getBlockState(pos))) {
                return false;
            }
        }
        return super.rotateBlock(world, pos, axis);
    }

    public static boolean isExceptBlockForAttachWithPiston(Block attachBlock) {
        return Block.isExceptBlockForAttachWithPiston(attachBlock);
    }
}
