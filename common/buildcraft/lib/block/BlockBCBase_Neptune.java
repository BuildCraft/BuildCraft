/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. */
package buildcraft.lib.block;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.api.properties.BuildCraftProperty;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

import buildcraft.lib.CreativeTabManager;
import buildcraft.lib.MigrationManager;
import buildcraft.lib.RegistryHelper;
import buildcraft.lib.TagManager;
import buildcraft.lib.TagManager.EnumTagType;
import buildcraft.lib.TagManager.EnumTagTypeMulti;
import buildcraft.lib.item.ItemBlockBC_Neptune;
import buildcraft.lib.item.ItemManager;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockBCBase_Neptune extends Block {
    private static List<BlockBCBase_Neptune> registeredBlocks = new ArrayList<>();
    private static final BuildCraftProperty<EnumFacing> PROP_FACING = BuildCraftProperties.BLOCK_FACING;

    /** The tag used to identify this in the {@link TagManager} */
    public final String id;

    public BlockBCBase_Neptune(Material material, String id) {
        super(material);
        this.id = id;
        setUnlocalizedName(TagManager.getTag(id, EnumTagType.UNLOCALIZED_NAME));
        setRegistryName(TagManager.getTag(id, EnumTagType.REGISTRY_NAME));
        setCreativeTab(CreativeTabManager.getTab(TagManager.getTag(id, EnumTagType.CREATIVE_TAB)));
        if(this instanceof IBlockWithFacing) {
            setDefaultState(getDefaultState().withProperty(PROP_FACING, EnumFacing.NORTH));
        }
    }

    // IBlockState

    @Override
    protected BlockStateContainer createBlockState() {
        if(this instanceof IBlockWithFacing) {
            return new BlockStateContainer(this, PROP_FACING);
        } else {
            return new BlockStateContainer(this);
        }
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = 0;
        if(this instanceof IBlockWithFacing) {
            meta |= state.getValue(PROP_FACING).getHorizontalIndex() & 3;
        }
        return meta;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        IBlockState state = getDefaultState();
        if(this instanceof IBlockWithFacing) {
            state = state.withProperty(PROP_FACING, EnumFacing.getHorizontal(meta & 3));
        }
        return state;
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        if(this instanceof IBlockWithFacing) {
            EnumFacing facing = state.getValue(PROP_FACING);
            state = state.withProperty(PROP_FACING, rot.rotate(facing));
        }
        return state;
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirror) {
        if(this instanceof IBlockWithFacing) {
            EnumFacing facing = state.getValue(PROP_FACING);
            state = state.withProperty(PROP_FACING, mirror.mirror(facing));
        }
        return state;
    }

    // Others

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        if(this instanceof IBlockWithFacing) {
            EnumFacing orientation = placer.getHorizontalFacing();
            world.setBlockState(pos, state.withProperty(PROP_FACING, orientation.getOpposite()));
        }
        super.onBlockPlacedBy(world, pos, state, placer, stack);
    }

    public static <B extends BlockBCBase_Neptune> B register(B block) {
        return register(block, false, ItemBlockBC_Neptune::new);
    }

    public static <B extends BlockBCBase_Neptune> B register(B block, boolean force) {
        return register(block, force, ItemBlockBC_Neptune::new);
    }

    public static <B extends BlockBCBase_Neptune> B register(B block, Function<B, ItemBlockBC_Neptune> itemBlockConstructor) {
        return register(block, false, itemBlockConstructor);
    }

    public static <B extends BlockBCBase_Neptune> B register(B block, boolean force, Function<B, ItemBlockBC_Neptune> itemBlockConstructor) {
        if (RegistryHelper.registerBlock(block, force)) {
            registeredBlocks.add(block);
            MigrationManager.INSTANCE.addBlockMigration(block, TagManager.getMultiTag(block.id, EnumTagTypeMulti.OLD_REGISTRY_NAME));
            if (itemBlockConstructor != null) {
                ItemBlockBC_Neptune item = itemBlockConstructor.apply(block);
                if (item != null) {
                    ItemManager.register(item, true);
                }
            }
            return block;
        }
        return null;
    }
}
