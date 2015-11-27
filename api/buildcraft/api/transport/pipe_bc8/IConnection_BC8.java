package buildcraft.api.transport.pipe_bc8;

import net.minecraft.tileentity.TileEntity;

public interface IConnection_BC8 {
    Object getOther();

//    IExtractable_BC8 getExtractor();

//    IInsertable_BC8 getInserter();

    interface Pipe extends IConnection_BC8 {
        @Override
        IPipe_BC8 getOther();
    }

    interface Entity extends IConnection_BC8 {
        @Override
        Entity getOther();
    }

    interface Tile extends IConnection_BC8 {
        @Override
        TileEntity getOther();
    }
}
