package buildcraft.transport.api.impl;

import buildcraft.api.transport.pipe_bc8.IPipePropertyProvider.IPipePropertyImplicit;
import buildcraft.api.transport.pipe_bc8.IPipePropertyProvider.IPipePropertyValue;
import buildcraft.api.transport.pipe_bc8.IPropertyProvider;

public enum PropertyProvider implements IPropertyProvider {
    INSTANCE;

    @Override
    public <T> IPipePropertyValue<T> getValueProperty(String modId, String name) {
        return null;
    }

    @Override
    public <T> IPipePropertyValue<T> registerValueProperty(String name, Class<T> typeClass) {
        return null;
    }

    @Override
    public <T> IPipePropertyImplicit<T> getImplicitProperty(String modId, String uniqueName) {
        return null;
    }

    @Override
    public <T> IPipePropertyImplicit<T> registerSimpleImplicitProperty(String name, Class<T> typeClass) {
        return null;
    }

    @Override
    public <T> IPipePropertyImplicit<T> registerCutomImplicitProperty(String name, IPipePropertyImplicit<T> property) {
        return null;
    }
}
