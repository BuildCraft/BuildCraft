package buildcraft.api.transport.pipe_bc8;

import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.core.INBTStoreable;
import buildcraft.api.core.ISerializable;

public interface IPipePropertyProvider {
    <T> T getValue(IPipeProperty<T> property);

    boolean hasProperty(IPipeProperty<?> property);

    /** Defines a property key- this should be stored in a publicly accessible static variable somewhere */
    public interface IPipeProperty<T> {
        String getName();
    }

    /** Defines a pipe property that has its value queried every time it is asked for its value. */
    public interface IPipePropertyImplicit<T> extends IPipeProperty<T> {
        T getValue(IPipe_BC8 pipe);
    }

    /** Defines a pipe property that has a value explicitly set */
    public interface IPipePropertyValue<T> extends ISerializable, INBTStoreable {
        T getValue();

        /** This should read all of its data from a sub-tag in this {@link #readFromNBT(NBTTagCompound)}, preferably
         * with the name of the property as the key to avoid confusion. */
        @Override
        void readFromNBT(NBTTagCompound tag);

        /** This should write all of its data to a sub-tag in this {@link NBTTagCompound}, preferably with the name of
         * the property as the key to avoid confusion */
        @Override
        void writeToNBT(NBTTagCompound tag);
    }

    /** Defines a provider that can have value properties changed and added. */
    public interface IPipePropertyProviderEditable extends IPipePropertyProvider {
        <T> void addProperty(IPipePropertyValue<T> property);

        <T> void removeProperty(IPipeProperty<T> property);

        <T> IPipePropertyValue<T> getProperty(IPipeProperty<T> property);
    }
}
