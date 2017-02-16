package buildcraft.transport;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemMinecart;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.IStripesRegistry.HandlerPriority;
import buildcraft.api.transport.pipe.ICustomPipeConnection;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeConnectionAPI;
import buildcraft.api.transport.pipe.PipeFlowType;

import buildcraft.transport.pipe.PipeRegistry;
import buildcraft.transport.pipe.StripesRegistry;
import buildcraft.transport.pipe.flow.PipeFlowFluids;
import buildcraft.transport.pipe.flow.PipeFlowItems;
import buildcraft.transport.pipe.flow.PipeFlowPower;
import buildcraft.transport.pipe.flow.PipeFlowStructure;
import buildcraft.transport.plug.PluggableRegistry;
import buildcraft.transport.stripes.StripesHandlerDispenser;
import buildcraft.transport.stripes.StripesHandlerMinecartDestroy;

public class BCTransportRegistries {
    public static void preInit() {
        PipeApi.pipeRegistry = PipeRegistry.INSTANCE;
        PipeApi.pluggableRegistry = PluggableRegistry.INSTANCE;
        PipeApi.stripeRegistry = StripesRegistry.INSTANCE;

        PipeApi.flowItems = new PipeFlowType(PipeFlowItems::new, PipeFlowItems::new);
        PipeApi.flowFluids = new PipeFlowType(PipeFlowFluids::new, PipeFlowFluids::new);
        PipeApi.flowPower = new PipeFlowType(PipeFlowPower::new, PipeFlowPower::new);
        PipeApi.flowStructure = new PipeFlowType(PipeFlowStructure::new, PipeFlowStructure::new);
    }

    public static void init() {
        ICustomPipeConnection smallerBlockConnection = (world, pos, face, state) -> face == EnumFacing.UP ? 0 : 2 / 16f;
        PipeConnectionAPI.registerConnection(Blocks.CHEST, smallerBlockConnection);
        PipeConnectionAPI.registerConnection(Blocks.TRAPPED_CHEST, smallerBlockConnection);
        PipeConnectionAPI.registerConnection(Blocks.HOPPER, smallerBlockConnection);

        // Item use stripes handlers
        // PipeAPI.stripeRegistry.addHandler(new StripesHandlerPlant());
        // PipeAPI.stripeRegistry.addHandler(new StripesHandlerBucket());
        // PipeAPI.stripeRegistry.addHandler(new StripesHandlerArrow());
        // PipeAPI.stripeRegistry.addHandler(new StripesHandlerShears());
        // PipeAPI.stripeRegistry.addHandler(new StripesHandlerPipes());
        // PipeAPI.stripeRegistry.addHandler(new StripesHandlerPipeWires());
        // PipeAPI.stripeRegistry.addHandler(new StripesHandlerEntityInteract());
        // PipeAPI.stripeRegistry.addHandler(new StripesHandlerHoe(), HandlerPriority.HIGH);
        // PipeAPI.stripeRegistry.addHandler(new StripesHandlerRightClick(), HandlerPriority.LOW);
        PipeApi.stripeRegistry.addHandler(StripesHandlerDispenser.INSTANCE, HandlerPriority.LOW);
        // PipeAPI.stripeRegistry.addHandler(new StripesHandlerPlaceBlock(), HandlerPriority.LOW);
        // PipeAPI.stripeRegistry.addHandler(new StripesHandlerUse(), HandlerPriority.LOW);

        StripesHandlerDispenser.itemClasses.add(ItemBucket.class);
        StripesHandlerDispenser.itemClasses.add(ItemMinecart.class);
        // StripesHandlerRightClick.items.add(Items.EGG);
        // StripesHandlerRightClick.items.add(Items.SNOWBALL);
        // StripesHandlerRightClick.items.add(Items.EXPERIENCE_BOTTLE);
        // StripesHandlerUse.items.add(Items.FIREWORKS);

        // Block breaking stripes handlers
        PipeApi.stripeRegistry.addHandler(StripesHandlerMinecartDestroy.INSTANCE);
    }
}
