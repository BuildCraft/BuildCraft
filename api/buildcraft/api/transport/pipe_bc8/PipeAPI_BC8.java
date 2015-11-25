package buildcraft.api.transport.pipe_bc8;

import net.minecraft.item.EnumDyeColor;

import buildcraft.api.transport.pipe_bc8.IPipePropertyProvider.IPipeProperty;

public enum PipeAPI_BC8 {
    INSTANCE;

    public static final IPipeProperty<EnumDyeColor> ITEM_COLOUR;

    static {
        ITEM_COLOUR = INSTANCE.registerSimpleProperty("buildcraft:item_colour");
    }

    public <T> IPipeProperty<T> createSimpleProperty(String name) {
        return new SimplePipeProperty<T>(name);
    }

    public <T> IPipeProperty<T> register(IPipeProperty<T> property) {
        return property;
    }

    public <T> IPipeProperty<T> registerSimpleProperty(String name) {
        IPipeProperty<T> simple = new SimplePipeProperty<T>(name);
        return register(simple);
    }

    private static final class SimplePipeProperty<T> implements IPipeProperty<T> {
        private final String name;

        public SimplePipeProperty(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
