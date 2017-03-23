package buildcraft.builders.snapshot;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
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
        for (int z = 0; z < blueprint.size.getZ(); z++) {
            for (int y = 0; y < blueprint.size.getY(); y++) {
                for (int x = 0; x < blueprint.size.getX(); x++) {
                    SchematicBlock schematicBlock = blueprint.data[x][y][z];
                    schematicBlock.buildWithoutChecks(this, new BlockPos(x, y, z));
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
