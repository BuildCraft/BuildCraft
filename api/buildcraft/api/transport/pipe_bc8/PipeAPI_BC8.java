package buildcraft.api.transport.pipe_bc8;

import net.minecraft.item.EnumDyeColor;

import buildcraft.api.APIHelper;
import buildcraft.api.transport.pipe_bc8.IPipePropertyProvider.IPipePropertyImplicit;
import buildcraft.api.transport.pipe_bc8.IPipePropertyProvider.IPipePropertyValue;

public enum PipeAPI_BC8 {
    INSTANCE;

    public static final IPropertyProvider PROPERTY_PROVIDER;
    public static final IInsertionManager INSERTION_MANAGER;
    public static final IExtractionManager EXTRACTION_MANAGER;

    public static final IPipeRegistry PIPE_REGISTRY;

    public static final IPipeType PIPE_TYPE_STRUCTURE;
    public static final IPipeType PIPE_TYPE_POWER;
    public static final IPipeType PIPE_TYPE_FLUID;
    public static final IPipeType PIPE_TYPE_ITEM;

    public static final IPipePropertyValue<EnumDyeColor> ITEM_COLOUR;
    public static final IPipePropertyValue<Boolean> ITEM_PAUSED;

    public static final IPipePropertyImplicit<Integer> ITEM_COUNT;
    public static final IPipePropertyImplicit<Integer> STACK_COUNT;

    static {
        PROPERTY_PROVIDER = APIHelper.getInstance("buildcraft.transport.api.impl.PropertyProvider", IPropertyProvider.class);
        INSERTION_MANAGER = APIHelper.getInstance("buildcraft.transport.api.impl.InsertionManager", IInsertionManager.class);
        EXTRACTION_MANAGER = APIHelper.getInstance("buildcraft.transport.api.impl.ExtractionManager", IExtractionManager.class);

        PIPE_REGISTRY = APIHelper.getInstance("buildcraft.transport.api.impl.PipeRegistry", IPipeRegistry.class);

        PIPE_TYPE_STRUCTURE = APIHelper.getNamedInstance("buildcraft.transport.api.impl.EnumPipeType", "STRUCTURE", IPipeType.class);
        PIPE_TYPE_POWER = APIHelper.getNamedInstance("buildcraft.transport.api.impl.EnumPipeType", "POWER", IPipeType.class);
        PIPE_TYPE_FLUID = APIHelper.getNamedInstance("buildcraft.transport.api.impl.EnumPipeType", "FLUID", IPipeType.class);
        PIPE_TYPE_ITEM = APIHelper.getNamedInstance("buildcraft.transport.api.impl.EnumPipeType", "ITEM", IPipeType.class);

        ITEM_COLOUR = PROPERTY_PROVIDER.getValueProperty("BuildCraft|Transport", "item_colour");
        ITEM_PAUSED = PROPERTY_PROVIDER.getValueProperty("BuildCraft|Transport", "item_paused");

        ITEM_COUNT = PROPERTY_PROVIDER.getImplicitProperty("BuildCraft|Transport", "item_count");
        STACK_COUNT = PROPERTY_PROVIDER.getImplicitProperty("BuildCraft|Transport", "stack_count");
    }
}
