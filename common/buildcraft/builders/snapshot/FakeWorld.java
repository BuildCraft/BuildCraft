package buildcraft.builders.snapshot;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.SaveHandlerMP;
import net.minecraft.world.storage.WorldInfo;

import java.util.ArrayList;
import java.util.List;

public class FakeWorld extends World {
    public static final BlockPos BLUEPRINT_OFFSET = new BlockPos(0, 128, 0);
    private final List<ItemStack> drops = new ArrayList<>();

    public FakeWorld() {
        super(
                new SaveHandlerMP(),
                new WorldInfo(new NBTTagCompound()),
                new WorldProvider() {
                    @Override
                    public DimensionType getDimensionType() {
                        return DimensionType.OVERWORLD;
                    }
                },
                Minecraft.getMinecraft().mcProfiler,
                false
        );
        chunkProvider = new FakeChunkProvider(this);
    }

    public void clear() {
        ((FakeChunkProvider) chunkProvider).chunks.clear();
    }

    public void uploadBlueprint(Blueprint blueprint) {
        for (int z = -1; z <= blueprint.size.getZ(); z++) {
            for (int y = -1; y <= blueprint.size.getY(); y++) {
                for (int x = -1; x <= blueprint.size.getX(); x++) {
                    BlockPos pos = new BlockPos(x, y, z).add(BLUEPRINT_OFFSET);
                    if (x == -1 || y == -1 || z == -1 ||
                            x == blueprint.size.getX() ||
                            y == blueprint.size.getY() ||
                            z == blueprint.size.getZ()) {
                        setBlockState(pos, Blocks.STONE.getDefaultState());
                    } else {
                        SchematicBlock schematicBlock = blueprint.data[x][y][z];
                        schematicBlock.buildWithoutChecks(this, pos);
                    }
                }
            }
        }
    }

    public List<ItemStack> breakBlockAndGetDrops(BlockPos pos) {
        getBlockState(pos).getBlock().breakBlock(this, pos, getBlockState(pos));
        List<ItemStack> dropsCopy = new ArrayList<>(drops);
        drops.clear();
        return dropsCopy;
    }

    @Override
    public boolean setBlockState(BlockPos pos, IBlockState newState, int flags) {
        captureBlockSnapshots = true;
        return super.setBlockState(pos, newState, flags);
    }

    @Override
    public boolean spawnEntity(Entity entity) {
        if (entity instanceof EntityItem) {
            drops.add(((EntityItem) entity).getEntityItem());
            return true;
        } else {
            return super.spawnEntity(entity);
        }
    }

    @Override
    protected IChunkProvider createChunkProvider() {
        return chunkProvider;
    }

    @Override
    protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
        return true;
    }
}
