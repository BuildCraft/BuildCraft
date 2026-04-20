package ct.buildcraft.factory;

import ct.buildcraft.core.BCCore;
import ct.buildcraft.factory.item.ItemWaterGel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BCFactoryItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, BCFactory.MODID);

    public static final RegistryObject<BlockItem> PUMP_BLOCK_ITEM = ITEMS.register("pump", () -> new BlockItem(BCFactoryBlocks.PUMP_BLOCK.get(),new Item.Properties().tab(BCCore.BUILDCRAFT_TAB)));
    public static final RegistryObject<BlockItem> TANK_BLOCK_ITEM = ITEMS.register("tank", () -> new BlockItem(BCFactoryBlocks.TANK_BLOCK.get(),new Item.Properties().tab(BCCore.BUILDCRAFT_TAB)));
    public static final RegistryObject<BlockItem> CHUTE_BLOCK_ITEM = ITEMS.register("chute", () -> new BlockItem(BCFactoryBlocks.CHUTE_BLOCK.get(),new Item.Properties().tab(BCCore.BUILDCRAFT_TAB)));
    public static final RegistryObject<BlockItem> FLOOD_GATE_BLOCK_ITEM = ITEMS.register("flood_gate", () -> new BlockItem(BCFactoryBlocks.FLOOD_GATE_BLOCK.get(),new Item.Properties().tab(BCCore.BUILDCRAFT_TAB)));
    public static final RegistryObject<BlockItem> MINING_WELL_BLOCK_ITEM = ITEMS.register("mining_well", () -> new BlockItem(BCFactoryBlocks.MINING_WELL_BLOCK.get(),new Item.Properties().tab(BCCore.BUILDCRAFT_TAB)));
    public static final RegistryObject<BlockItem> DISTILLER_BLOCK_ITEM = ITEMS.register("distiller", () -> new BlockItem(BCFactoryBlocks.DISTILLER_BLOCK.get(),new Item.Properties().tab(BCCore.BUILDCRAFT_TAB)));
    public static final RegistryObject<BlockItem> HEAT_EXCHANGE_BLOCK_ITEM = ITEMS.register("heat_exchange", () -> new BlockItem(BCFactoryBlocks.HEATEXCHANGE_BLOCK.get(),new Item.Properties().tab(BCCore.BUILDCRAFT_TAB)));
    public static final RegistryObject<BlockItem> AUTO_BENCH_ITEM = ITEMS.register("autoworkbench_item", () -> new BlockItem(BCFactoryBlocks.AUTO_BENCH_BLOCK.get(), new Item.Properties().tab(BCCore.BUILDCRAFT_TAB)));
    
    public static final RegistryObject<ItemWaterGel> WATER_GEL_SPAWN = ITEMS.register("water_gel", ItemWaterGel::new);
    public static final RegistryObject<Item> GEL = ITEMS.register("gel", () -> new Item(new Item.Properties().tab(BCCore.BUILDCRAFT_TAB)));
    
    
    static void registry(IEventBus bus) {
    	ITEMS.register(bus);
    }
}
