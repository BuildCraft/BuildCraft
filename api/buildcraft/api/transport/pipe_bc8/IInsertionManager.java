package buildcraft.api.transport.pipe_bc8;

import net.minecraft.util.EnumFacing;

public interface IInsertionManager {
    /** Gets an {@link IInsertable_BC8} interface for a given tile or movable entity. */
    IInsertable_BC8 getInsertableFor(Object obj);

    /** Registers an {@link IInsertableFactory}. Note that if two insertables are registered and have an overlapping
     * class child, the most specific one is used. */
    <T> void registerInsertable(Class<T> clazz, IInsertableFactory<T> factory);

    public interface IInsertableFactory<T> {
        IInsertable_BC8 createNew(T obj);
    }

    public interface IInsertable_BC8 {
        /** @param contents The contents to try and insert
         * @param insertor The object that is doing the insertion.
         * @param direction The direction the contents is going.
         * @return True if the contents was inserted (And you should discard any contents that went in to it), false if
         *         it was not allowed in. */
        boolean tryInsert(IPipeContentsEditable contents, Object insertor, EnumFacing direction);

        /** @return A filter that will limit what can be inserted to just what is in the filter. This filter *may* be
         *         over-extensive in what it can accept though. */
        IContentsFilter getFilter();

        /** @param theoreticalContents The type that is to be tested.
         * @return true if {@link #tryInsert(IPipeContentsEditable, Object, EnumFacing)} MIGHT allow the type (Class
         *         type) to be inserted. */
        IContentsFilter getFilterForType(IPipeContents theoreticalContents);
    }
}
