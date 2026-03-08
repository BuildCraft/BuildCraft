package buildcraft.core.block;

import buildcraft.api.blocks.BlockConstants;
import buildcraft.api.blocks.ISpring;
import buildcraft.api.enums.EnumSpring;
import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.misc.data.XorShift128Random;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.Random;
import java.util.function.Supplier;

public class BlockSpring extends BlockBCBase_Neptune implements ITileEntityProvider, ISpring {
    // public static final Property<EnumSpring> SPRING_TYPE = BuildCraftProperties.SPRING_TYPE;
    public final EnumSpring springType;

    public static final XorShift128Random rand = new XorShift128Random();

    public BlockSpring(String idBC, AbstractBlock.Properties properties, EnumSpring springType) {
        super(idBC, properties);
//        setBlockUnbreakable();
//        setResistance(6000000.0F);
//        setSoundType(SoundType.STONE);

//        disableStats();
//        setTickRandomly(true);
//        setDefaultState(getDefaultState().withProperty(SPRING_TYPE, EnumSpring.WATER));
        this.springType = springType;
    }

    // 1.18.2: no meta

//    @Override
//     protected BlockStateContainer createBlockState() {
//         return new BlockStateContainer(this, SPRING_TYPE);
//     }

    // BlockState

//    @Override
//    protected BlockStateContainer createBlockState() {
//        return new BlockStateContainer(this, SPRING_TYPE);
//    }

//    @Override
//    public int getMetaFromState(BlockState state) {
//        return state.getValue(BuildCraftProperties.SPRING_TYPE).ordinal();
//    }

//    @Override
//    public BlockState getStateFromMeta(int meta) {
//        if (meta == EnumSpring.OIL.ordinal()) {
//            return defaultBlockState().setValue(BuildCraftProperties.SPRING_TYPE, EnumSpring.OIL);
//        } else {
//            return defaultBlockState().setValue(BuildCraftProperties.SPRING_TYPE, EnumSpring.WATER);
//        }
//    }

    // Other

//    @Override
//    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
//        for (EnumSpring type : EnumSpring.VALUES) {
//            list.add(new ItemStack(this, 1, type.ordinal()));
//        }
//    }

    // Calen: use datagen LootTable
//    @Override
//    public int damageDropped(BlockState state) {
//        return state.getValue(SPRING_TYPE).ordinal();
//    }

    @Override
//    public void updateTick(World world, BlockPos pos, IBlockState state, Random random)
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        generateSpringBlock(world, pos, state);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
//        return state.getValue(SPRING_TYPE).tileConstructor != null;
        return springType.tileConstructor != null;
    }

    @Override
//    public TileEntity getBlockEntity(IBlockReader world, BlockPos pos)
    public TileEntity newBlockEntity(IBlockReader world) {
//        Supplier<TileEntity> constructor = state.getValue(SPRING_TYPE).tileConstructor;
        Supplier<TileEntity> constructor = this.springType.tileConstructor;
        if (constructor != null) {
            return constructor.get();
        }
        return null;
    }

    // @Override
    // public void onNeighborBlockChange(World world, int x, int y, int z, int blockid) {
    // assertSpring(world, x, y, z);
    // }

    @Override
//    public void onBlockAdded(World world, BlockPos pos, BlockState state)
    public void setPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity p_52509_, ItemStack p_52510_) {
//        super.onBlockAdded(world, pos, state);
        super.setPlacedBy(world, pos, state, p_52509_, p_52510_);
//        world.scheduleUpdate(pos, this, state.getValue(SPRING_TYPE).tickRate);
        world.getBlockTicks().scheduleTick(pos, this, this.springType.tickRate);
    }

    private void generateSpringBlock(World world, BlockPos pos, BlockState state) {
//        EnumSpring spring = state.getValue(BuildCraftProperties.SPRING_TYPE);
        EnumSpring spring = this.springType;
        world.getBlockTicks().scheduleTick(pos, this, spring.tickRate);
        if (!spring.canGen || spring.liquidBlock == null) {
            return;
        }
        if (!world.isEmptyBlock(pos.above())) {
            return;
        }
        if (spring.chance != -1 && rand.nextInt(spring.chance) != 0) {
            return;
        }
        world.setBlock(pos.above(), spring.liquidBlock, BlockConstants.UPDATE_ALL);
    }

    // Prevents updates on chunk generation
    // @Override
    // public boolean func_149698_L() {
    // return false;
    // }

    // ISpring

    @Override
    public EnumSpring getType() {
        return springType;
    }
}
