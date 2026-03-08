/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.block;

import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.lib.registry.TagManager;
import net.minecraft.block.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BlockBCBase_Neptune extends Block {
    public static final Property<Direction> PROP_FACING = BuildCraftProperties.BLOCK_FACING;
    public static final Property<Direction> BLOCK_FACING_6 = BuildCraftProperties.BLOCK_FACING_6;

    /** The tag used to identify this in the {@link TagManager}. Note that this may be empty if this block doesn't use
     * the tag system. */
    public final String idBC;

    /** @param idBC The ID that will be looked up in the {@link TagManager} when registering blocks. Pass null or the
     *            empty string to bypass the {@link TagManager} entirely. */
    public BlockBCBase_Neptune(String idBC, Properties props) {
        super(props);
        if (idBC == null) {
            idBC = "";
        }
        this.idBC = idBC;

//        // Sensible default block properties
//        setHardness(5.0F);
//        setResistance(10.0F);
//        setSoundType(SoundType.METAL);

        if (!idBC.isEmpty()) {
            // Init names from the tag manager
//            setUnlocalizedName("tile." + TagManager.getTag(id, TagManager.EnumTagType.UNLOCALIZED_NAME) + ".name");
            // Calen: for BCEnergy chocolate engine
            String unlocalizedName = TagManager.getTag(idBC, TagManager.EnumTagType.UNLOCALIZED_NAME);
            if (unlocalizedName.startsWith("buildcraft.christmas.")) {
                unlocalizedName = unlocalizedName.replace("buildcraft.christmas.", "buildcraft.christmas.tile.") + ".name";
            } else {
                unlocalizedName = "tile." + unlocalizedName + ".name";
            }
            setUnlocalizedName(unlocalizedName);
//            setRegistryName(TagManager.getTag(id, TagManager.EnumTagType.REGISTRY_NAME));
//            setCreativeTab(CreativeTabManager.getTab(TagManager.getTag(id, TagManager.EnumTagType.CREATIVE_TAB)));
        }

        if (this instanceof IBlockWithFacing) {
            Property<Direction> facingProp = ((IBlockWithFacing) this).getFacingProperty();
//            setDefaultState(defaultBlockState().setValue(facingProp, Direction.NORTH));
            registerDefaultState(
                    defaultBlockState()
                            .setValue(facingProp, Direction.NORTH)
            );
        }
    }

    // IBlockState

    protected void addProperties(List<Property<?>> properties) {
        if (this instanceof IBlockWithFacing) {
            properties.add(((IBlockWithFacing) this).getFacingProperty());
        }
    }

    @Override
//    protected BlockStateContainer createBlockState()
    protected void createBlockStateDefinition(@Nonnull StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        List<Property<?>> properties = new ArrayList<>();
        addProperties(properties);
//        return new BlockStateContainer(this, properties.toArray(new IProperty<?>[0]));
        builder.add(properties.toArray(new Property<?>[0]));
    }

//    @Override
//    public int getMetaFromState(IBlockState state) {
//        int meta = 0;
//        if (this instanceof IBlockWithFacing) {
//            if (((IBlockWithFacing) this).canFaceVertically()) {
//                meta |= state.getValue(((IBlockWithFacing) this).getFacingProperty()).getIndex();
//            } else {
//                meta |= state.getValue(((IBlockWithFacing) this).getFacingProperty()).getHorizontalIndex();
//            }
//        }
//        return meta;
//    }

//    @Override
//    public IBlockState getStateFromMeta(int meta) {
//        IBlockState state = getDefaultState();
//        if (this instanceof IBlockWithFacing) {
//            IBlockWithFacing b = (IBlockWithFacing) this;
//            IProperty<EnumFacing> prop = b.getFacingProperty();
//            if (b.canFaceVertically()) {
//                state = state.withProperty(prop, EnumFacing.getFront(meta & 7));
//            } else {
//                state = state.withProperty(prop, EnumFacing.getHorizontal(meta & 3));
//            }
//        }
//        return state;
//    }

    @Override
//    public IBlockState withRotation(IBlockState state, Rotation rot)
    public BlockState rotate(BlockState state, Rotation rot) {
        if (this instanceof IBlockWithFacing) {
            Property<Direction> prop = ((IBlockWithFacing) this).getFacingProperty();
            Direction facing = state.getValue(prop);
            state = state.setValue(prop, rot.rotate(facing));
        }
        return state;
    }

    @Override
//    public IBlockState withMirror(IBlockState state, Mirror mirror)
    public BlockState mirror(BlockState state, Mirror mirror) {
        if (this instanceof IBlockWithFacing) {
            Property<Direction> prop = ((IBlockWithFacing) this).getFacingProperty();
            Direction facing = state.getValue(prop);
            state = state.setValue(prop, mirror.mirror(facing));
        }
        return state;
    }

    @Override
//    public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis)
    public BlockState rotate(BlockState state, IWorld world, BlockPos pos, Rotation direction) {
        if (this instanceof IBlockWithFacing) {
            if (!((IBlockWithFacing) this).canBeRotated(world, pos, world.getBlockState(pos))) {
//                return false;
                return state;
            }
        }
//        return super.rotateBlock(world, pos, axis);
        return super.rotate(world.getBlockState(pos), world, pos, direction);
    }

    // Others

    // Calen: this is called when the block not been placed, to choose a state for place
    @Override
//    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand)
    public BlockState getStateForPlacement(@Nonnull BlockItemUseContext context) {
        BlockPos pos = context.getClickedPos();
        LivingEntity placer = context.getPlayer();
//        BlockState state = super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand);
        BlockState state = super.getStateForPlacement(context);
        if (this instanceof IBlockWithFacing) {
//            EnumFacing orientation = placer.getHorizontalFacing();
            Direction orientation = placer.getDirection();
            IBlockWithFacing b = (IBlockWithFacing) this;
            if (b.canFaceVertically()) {
//                if (MathHelper.abs((float) placer.getX() - pos.getX()) < 2.0F
                if (MathHelper.abs((float) placer.getX() - pos.getX()) < 2.0F
//                        && MathHelper.abs((float) placer.getZ() - pos.getZ()) < 2.0F)
                        && MathHelper.abs((float) placer.getZ() - pos.getZ()) < 2.0F)
                {
                    double y = placer.getY() + placer.getEyeHeight();

                    if (y - pos.getY() > 2.0D) {
                        orientation = Direction.DOWN;
                    }

                    if (pos.getY() - y > 0.0D) {
                        orientation = Direction.UP;
                    }
                }
            }
            state = state.setValue(b.getFacingProperty(), orientation.getOpposite());
        }
        return state;
    }

    public static boolean isExceptBlockForAttachWithPiston(Block attachBlock) {
//        return Block.isExceptBlockForAttachWithPiston(attachBlock);
        return isExceptionBlockForAttaching(attachBlock) || attachBlock == Blocks.PISTON || attachBlock == Blocks.STICKY_PISTON || attachBlock == Blocks.PISTON_HEAD;
    }

    protected static boolean isExceptionBlockForAttaching(Block attachBlock) {
        return attachBlock instanceof ShulkerBoxBlock || attachBlock instanceof LeavesBlock || attachBlock instanceof TrapDoorBlock || attachBlock == Blocks.BEACON || attachBlock == Blocks.CAULDRON || attachBlock == Blocks.GLASS || attachBlock == Blocks.GLOWSTONE || attachBlock == Blocks.ICE || attachBlock == Blocks.SEA_LANTERN || Tags.Blocks.STAINED_GLASS.contains(attachBlock);
    }

    // Calen:
    // in 1.18.2 setUnlocalizedName setRegistryName are unvailable
    @Override
    public String getDescriptionId() {
        return this.unlocalizedName;
    }

    private String unlocalizedName;

    public void setUnlocalizedName(String unlocalizedName) {
        this.unlocalizedName = unlocalizedName;
    }

    // Calen: from mc 1.12.2
    // should be called where we want, not by mc
    public BlockState getActualState(BlockState state, IWorld world, BlockPos pos, TileEntity tile) {
        return state;
    }

    // Calen

    /**
     * To call {@link #getActualState(BlockState, IWorld, BlockPos, TileEntity)} and update BlockState if required.
     * @param state
     * @param world
     * @param pos
     * @param tile Whether null is allowed depends on how {@link #getActualState(BlockState, IWorld, BlockPos, TileEntity)} overrides.
     */
    public void checkActualStateAndUpdate(BlockState state, World world, BlockPos pos, @Nullable TileEntity tile) {
        BlockState newState = getActualState(state, world, pos, tile);
        if (!newState.equals(state)) {
            world.setBlockAndUpdate(pos, newState);
        }
    }
}
