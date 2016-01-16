package buildcraft.core.lib.world;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.SaveHandlerMP;
import net.minecraft.world.storage.WorldInfo;

import buildcraft.BuildCraftCore;
import buildcraft.api.blueprints.SchematicBlockBase;
import buildcraft.api.enums.EnumDecoratedBlock;
import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.builders.blueprints.RealBlueprintDeployer;
import buildcraft.core.blueprints.Blueprint;
import buildcraft.core.blueprints.Template;
import buildcraft.core.lib.utils.Utils;

public class FakeWorld extends World {
    public boolean isDirty = true;
    public volatile boolean hasDeployed = false;

    private FakeWorld(EnumDecoratedBlock type) {
        super(new SaveHandlerMP(), new WorldInfo(new NBTTagCompound()), new FakeWorldProvider(), Minecraft.getMinecraft().mcProfiler, false);
        chunkProvider = new FakeChunkProvider(this, type);
        provider.registerWorld(this);
    }

    public FakeWorld(final Blueprint blueprint) {
        this(EnumDecoratedBlock.TEMPLATE);
        BlockPos start = blueprint.getBoxForPos(BlockPos.ORIGIN).center();
        start = start.add(0, -start.getY() + 1, 0);

        final BlockPos deployPos = start.add(blueprint.size).subtract(Utils.POS_ONE);
        final FakeWorld thisWorld = this;
        new Thread("blueprint-deployer") {
            @Override
            public void run() {
                RealBlueprintDeployer.realInstance.deployBlueprint(thisWorld, deployPos, EnumFacing.EAST, blueprint);
                hasDeployed = true;
            }
        }.start();

        start = start.down();
        BlockPos end = start.add(blueprint.size.getX() - 1, 0, blueprint.size.getZ() - 1);

        IBlockState state = BuildCraftCore.decoratedBlock.getDefaultState();
        state = state.withProperty(BuildCraftProperties.DECORATED_BLOCK, EnumDecoratedBlock.BLUEPRINT);

        IBlockState roofState = Blocks.dirt.getDefaultState();

        for (BlockPos pos : BlockPos.getAllInBox(start, end)) {
            setBlockState(pos, state);
            setBlockState(pos.up(255), roofState);
        }
    }

    public FakeWorld(Template template, IBlockState filledBlock) {
        this(EnumDecoratedBlock.BLUEPRINT);
        BlockPos start = template.getBoxForPos(BlockPos.ORIGIN).center();
        BlockPos end = start.add(template.size).subtract(Utils.POS_ONE);

        IBlockState state = BuildCraftCore.decoratedBlock.getDefaultState();
        state = state.withProperty(BuildCraftProperties.DECORATED_BLOCK, EnumDecoratedBlock.TEMPLATE);

        IBlockState roofState = Blocks.dirt.getDefaultState();

        for (BlockPos pos : BlockPos.getAllInBox(start, end)) {
            BlockPos array = pos.subtract(start);
            SchematicBlockBase block = template.get(array);

            if (block != null) {
                setBlockState(pos, filledBlock);
            }
            if (pos.getY() == 1) {
                setBlockState(pos.down(), state);
                setBlockState(pos.up(254), roofState);
            }
        }
    }

    @Override
    protected IChunkProvider createChunkProvider() {
        return chunkProvider;
    }

    @Override
    protected int getRenderDistanceChunks() {
        return 10;
    }
}
