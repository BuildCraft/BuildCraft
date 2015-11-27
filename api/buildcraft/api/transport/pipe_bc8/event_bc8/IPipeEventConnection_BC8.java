package buildcraft.api.transport.pipe_bc8.event_bc8;

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;

import buildcraft.api.transport.pipe_bc8.IConnection_BC8;
import buildcraft.api.transport.pipe_bc8.IPipe_BC8;

public interface IPipeEventConnection_BC8 extends IPipeEvent_BC8 {
    IConnection_BC8 getConnection();

    public interface AttemptCreate extends IPipeEventConnection_BC8 {
        void disallow();

        Object with();

        public interface Pipe extends AttemptCreate {
            @Override
            IPipe_BC8 with();
        }

        public interface Tile extends AttemptCreate {
            @Override
            TileEntity with();
        }

        public interface MovableEntity extends AttemptCreate {
            @Override
            Entity with();
        }
    }

    public interface Destroy extends IPipeEventConnection_BC8 {
        Object with();

        public interface Pipe extends Destroy {
            @Override
            IPipe_BC8 with();
        }

        public interface Tile extends Destroy {
            @Override
            TileEntity with();
        }

        public interface MovableEntity extends Destroy {
            @Override
            Entity with();
        }
    }
}
