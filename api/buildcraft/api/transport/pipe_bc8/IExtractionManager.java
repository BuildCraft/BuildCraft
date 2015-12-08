package buildcraft.api.transport.pipe_bc8;

import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.pipe_bc8.IInsertionManager.IInsertableFactory;

public interface IExtractionManager {
    /** Gets an {@link IExtractable_BC8} interface for a given tile or movable entity. */
    IExtractable_BC8 getExtractableFor(Object obj);

    /** Registers an {@link IInsertableFactory}. Note that if two insertables are registered and have an overlapping
     * class child, the most specific one is used. */
    <T> void registerInsertable(Class<T> clazz, IExtractableFactory<T> factory);

    public interface IExtractableFactory<T> {
        IExtractable_BC8 createNew(T obj);
    }

    public interface IExtractable_BC8 {
        /** @param filter The filter to use when determining what can be extracted
         * @param extractor The object the the contents are being extracted by
         * @param direction The direction the contents will be going in
         * @return A pipe contents object if it was successfully extracted, or null if nothing was extracted. */
        IPipeContentsEditable tryExtract(IContentsFilter filter, Object extractor, EnumFacing direction);

        /** @param theoreticalContents The type that is to be tested.
         * @return True if {@link #tryExtract(IContentsFilter, Object, EnumFacing)} MIGHT allow the type (Class type) to
         *         be extracted. */
        boolean givesType(IPipeContents theoreticalContents);
    }
}
