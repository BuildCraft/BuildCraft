package buildcraft.builders.snapshot;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.SaveHandlerMP;
import net.minecraft.world.storage.WorldInfo;

public class FakeWorld extends World {
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

    public FakeWorld(Blueprint blueprint) {
        this();
        for (int z = 0; z < blueprint.size.getZ(); z++) {
            for (int y = 0; y < blueprint.size.getY(); y++) {
                for (int x = 0; x < blueprint.size.getX(); x++) {
                    SchematicBlock schematicBlock = blueprint.data[x][y][z];
                    schematicBlock.buildWithoutChecks(this, new BlockPos(x, y, z));
                }
            }
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
