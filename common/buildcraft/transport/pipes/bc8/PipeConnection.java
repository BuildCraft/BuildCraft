package buildcraft.transport.pipes.bc8;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.core.BCLog;
import buildcraft.api.transport.pipe_bc8.IConnection_BC8;
import buildcraft.api.transport.pipe_bc8.IExtractionManager.IExtractable_BC8;
import buildcraft.api.transport.pipe_bc8.IInsertionManager.IInsertable_BC8;
import buildcraft.api.transport.pipe_bc8.IPipeHolder_BC8;
import buildcraft.api.transport.pipe_bc8.IPipe_BC8;
import buildcraft.api.transport.pipe_bc8.PipeAPI_BC8;
import buildcraft.core.lib.utils.NBTUtils;

public abstract class PipeConnection implements IConnection_BC8 {
    private final double length;
    private final IExtractable_BC8 extractable;
    private final IInsertable_BC8 insertable;

    public PipeConnection(double length, Object obj) {
        this.length = length;
        extractable = PipeAPI_BC8.EXTRACTION_MANAGER.getExtractableFor(obj);
        insertable = PipeAPI_BC8.INSERTION_MANAGER.getInsertableFor(obj);
    }

    @Override
    public double getLength() {
        return length;
    }

    @Override
    public IExtractable_BC8 getExtractor() {
        return extractable;
    }

    @Override
    public IInsertable_BC8 getInserter() {
        return insertable;
    }

    abstract NBTTagCompound saveConnection();

    public static PipeConnection loadConnection(NBTTagCompound nbt, World world) {
        PipeConnectionType type = NBTUtils.readEnum(nbt.getTag("type"), PipeConnectionType.class);
        if (type == PipeConnectionType.TILE) {
            BlockPos pos = NBTUtils.readBlockPos(nbt.getTag("pos"));
            TileEntity tile = world.getTileEntity(pos);
            double length = nbt.getDouble("length");
            if (tile == null) return null;
            return new Tile(length, tile);
        } else if (type == PipeConnectionType.PIPE_TILE) {
            BlockPos pos = NBTUtils.readBlockPos(nbt.getTag("pos"));
            TileEntity tile = world.getTileEntity(pos);
            if (tile == null) return null;
            if (!(tile instanceof IPipeHolder_BC8)) return null;
            double length = nbt.getDouble("length");
            return new Pipe(length, ((IPipeHolder_BC8) tile).getPipe());
        } else if (type == PipeConnectionType.PIPE_ENTITY) {
            int entId = nbt.getInteger("id");
            Entity ent = world.getEntityByID(entId);
            if (ent == null) return null;
            if (!(ent instanceof IPipeHolder_BC8)) return null;
            double length = nbt.getDouble("length");
            return new Pipe(length, ((IPipeHolder_BC8) ent).getPipe());
        } else if (type == PipeConnectionType.ENTITY) {
            // TODO :)
        }

        BCLog.logger.warn("Tried to load a connection with an unknown type! " + type);
        return null;
    }

    private enum PipeConnectionType {
        TILE,
        PIPE_TILE,
        PIPE_ENTITY,
        ENTITY
    }

    public static class Tile extends PipeConnection implements IConnection_BC8.Tile {
        private final TileEntity tile;

        public Tile(double length, TileEntity tile) {
            super(length, tile);
            this.tile = tile;
        }

        @Override
        public TileEntity getOther() {
            return tile;
        }

        @Override
        NBTTagCompound saveConnection() {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setTag("type", NBTUtils.writeEnum(PipeConnectionType.TILE));
            tag.setTag("pos", NBTUtils.writeBlockPos(tile.getPos()));
            tag.setDouble("length", getLength());
            return tag;
        }
    }

    public static class Pipe extends PipeConnection implements IConnection_BC8.Pipe {
        private final IPipe_BC8 pipe;

        public Pipe(double length, IPipe_BC8 pipe) {
            super(length, pipe);
            this.pipe = pipe;
        }

        @Override
        public IPipe_BC8 getOther() {
            return pipe;
        }

        @Override
        NBTTagCompound saveConnection() {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setDouble("length", getLength());
            if (pipe.getHolder() instanceof TileEntity) {
                tag.setTag("type", NBTUtils.writeEnum(PipeConnectionType.PIPE_TILE));
                tag.setTag("pos", NBTUtils.writeBlockPos(((TileEntity) pipe.getHolder()).getPos()));
            } else if (pipe.getHolder() instanceof Entity) {
                tag.setTag("type", NBTUtils.writeEnum(PipeConnectionType.PIPE_ENTITY));
                tag.setInteger("id", ((Entity) pipe.getHolder()).getEntityId());
            } else {
                throw new IllegalStateException("Held a pipe object that had an unknow holder! Make a request if this is needed for " + pipe
                        .getHolder().getClass());
            }
            return tag;
        }
    }
}
