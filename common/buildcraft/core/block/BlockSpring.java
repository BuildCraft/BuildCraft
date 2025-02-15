package buildcraft.core.block;

import buildcraft.api.blocks.ISpring;
import buildcraft.api.enums.EnumSpring;
import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.misc.data.XorShift128Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiFunction;

public class BlockSpring extends BlockBCBase_Neptune implements EntityBlock, ISpring {
    // public static final Property<EnumSpring> SPRING_TYPE = BuildCraftProperties.SPRING_TYPE;
    public final EnumSpring springType;

    public static final XorShift128Random rand = new XorShift128Random();

    public BlockSpring(String idBC, BlockBehaviour.Properties properties, EnumSpring springType) {
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
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        generateSpringBlock(world, pos, state);
    }

//    @Override
//    public boolean hasTileEntity(BlockState state) {
//        return state.getValue(SPRING_TYPE).tileConstructor != null;
//    }

    @Override
//    public BlockEntity getBlockEntity(BlockGetter world, BlockPos pos)
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
//        Supplier<TileEntity> constructor = state.getValue(SPRING_TYPE).tileConstructor;
        BiFunction<BlockPos, BlockState, BlockEntity> constructor = this.springType.tileConstructor;
        if (constructor != null) {
            return constructor.apply(pos, state);
        }
        return null;
    }

    // @Override
    // public void onNeighborBlockChange(Level world, int x, int y, int z, int blockid) {
    // assertSpring(world, x, y, z);
    // }

    @Override
//    public void onBlockAdded(Level world, BlockPos pos, BlockState state)
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity p_52509_, ItemStack p_52510_) {
//        super.onBlockAdded(world, pos, state);
        super.setPlacedBy(world, pos, state, p_52509_, p_52510_);
//        world.scheduleUpdate(pos, this, state.getValue(SPRING_TYPE).tickRate);
        world.scheduleTick(pos, this, this.springType.tickRate);
    }

    private void generateSpringBlock(Level world, BlockPos pos, BlockState state) {
//        EnumSpring spring = state.getValue(BuildCraftProperties.SPRING_TYPE);
        EnumSpring spring = this.springType;
        world.scheduleTick(pos, this, spring.tickRate);
        if (!spring.canGen || spring.liquidBlock == null) {
            return;
        }
        if (!world.isEmptyBlock(pos.above())) {
            return;
        }
        if (spring.chance != -1 && rand.nextInt(spring.chance) != 0) {
            return;
        }
        world.setBlock(pos.above(), spring.liquidBlock, Block.UPDATE_ALL);
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
