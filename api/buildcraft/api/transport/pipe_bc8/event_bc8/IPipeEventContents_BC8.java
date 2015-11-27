package buildcraft.api.transport.pipe_bc8.event_bc8;

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.pipe_bc8.IPipeContents;
import buildcraft.api.transport.pipe_bc8.IPipeContentsEditable;
import buildcraft.api.transport.pipe_bc8.IPipe_BC8;

/** In all events, the contents have had the appropriate variables set up as if they were in the pipe at the time the
 * event is fired. */
public interface IPipeEventContents_BC8 extends IPipeEvent_BC8 {
    IPipeContents getContents();

    // Attempts to put in some contents

    public interface AttemptEnter extends IPipeEventContents_BC8 {
        void disallow();

        /** Gets the direction that the contents came from. So if this was a pipe below, and the item was moving
         * upwards, this would be {@link EnumFacing#DOWN} */
        EnumFacing getFrom();

        /** Gets the *thing* that tried to insert to this pipe. Most of the time this will be a tile entity, however it
         * might be a robot or */
        Object getInserter();

        public interface Pipe extends AttemptEnter {
            @Override
            IPipe_BC8 getInserter();
        }

        /** Fired whenever a tile entity attempts to insert an item. NOTE: this will NEVER call with a pipe as the
         * argument. */
        public interface Tile extends AttemptEnter {
            @Override
            TileEntity getInserter();
        }

        public interface MovableEntity extends AttemptEnter {
            @Override
            Entity getInserter();
        }
    }

    // Actually puts in the contents

    /** Fired after {@link AttemptEnter}, but before the contents has been added to the pipe. */
    public interface Enter extends IPipeEventContents_BC8 {
        @Override
        IPipeContentsEditable getContents();
    }

    // Actual

    /** Fired after the contents have been removed from the pipe, but before they have been dropped onto the ground or
     * added to a different pipe or inventory. */
    public interface Exit extends IPipeEventContents_BC8 {
        @Override
        IPipeContentsEditable getContents();
    }
}
