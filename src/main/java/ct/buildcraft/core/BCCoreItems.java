package ct.buildcraft.core;

import java.util.EnumMap;

import ct.buildcraft.api.enums.EnumEngineType;
import ct.buildcraft.api.enums.EnumSpring;
import ct.buildcraft.api.items.IMapLocation.MapLocationType;
import ct.buildcraft.core.item.ItemFragileFluidContainer;
import ct.buildcraft.core.item.ItemList_BC8;
import ct.buildcraft.core.item.ItemMapLocation;
import ct.buildcraft.core.item.ItemMarkerConnector;
import ct.buildcraft.core.item.ItemPaintbrush_BC8;
import ct.buildcraft.core.item.ItemVolumeBox;
import ct.buildcraft.core.item.ItemWrench;
import ct.buildcraft.lib.item.ItemByEnum;
import ct.buildcraft.lib.item.MultiBlockItem;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BCCoreItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, BCCore.MODID);
    public static final RegistryObject<Item> WRENCH = ITEMS.register("wrench", ItemWrench::new);
    public static final RegistryObject<Item> GEAR_WOOD = ITEMS.register("gears/gear_wood", () -> new Item(new Item.Properties().tab(BCCore.BUILDCRAFT_TAB))); 
    public static final RegistryObject<Item> GEAR_STONE = ITEMS.register("gears/gear_stone", () -> new Item(new Item.Properties().tab(BCCore.BUILDCRAFT_TAB))); 
    public static final RegistryObject<Item> GEAR_IRON = ITEMS.register("gears/gear_iron", () -> new Item(new Item.Properties().tab(BCCore.BUILDCRAFT_TAB))); 
    public static final RegistryObject<Item> GEAR_GOLD = ITEMS.register("gears/gear_gold", () -> new Item(new Item.Properties().tab(BCCore.BUILDCRAFT_TAB))); 
    public static final RegistryObject<Item> GEAR_DIAMOND = ITEMS.register("gears/gear_diamond", () -> new Item(new Item.Properties().tab(BCCore.BUILDCRAFT_TAB))); 
    public static final RegistryObject<ItemPaintbrush_BC8> PAINT_BRUSH = ITEMS.register("paintbrush/clean", () -> new ItemPaintbrush_BC8(new Item.Properties().tab(BCCore.BUILDCRAFT_TAB), null));
    public static final RegistryObject<ItemMarkerConnector> MARKER_CONNECTOR = ITEMS.register("marker_connector", () -> new ItemMarkerConnector(new Item.Properties().tab(BCCore.BUILDCRAFT_TAB)));
    public static final RegistryObject<ItemVolumeBox> VOLUME_BOX = ITEMS.register("volume_box", () -> new ItemVolumeBox(new Item.Properties().tab(BCCore.BUILDCRAFT_TAB)));
    public static final RegistryObject<ItemMapLocation> MAP_LOCATION = ITEMS.register("map_location", () -> new ItemMapLocation(new Item.Properties().tab(BCCore.BUILDCRAFT_TAB)));
    public static final RegistryObject<ItemList_BC8> LIST = ITEMS.register("list", () -> new ItemList_BC8(new Item.Properties().tab(BCCore.BUILDCRAFT_TAB).stacksTo(1)));

    
    
    public static final RegistryObject<ItemFragileFluidContainer> FRAGILE_FLUID_SHARD = ITEMS.register("fragile_fluid_shard", ItemFragileFluidContainer::new);
    
    
    
    public static final EnumMap<DyeColor, ItemPaintbrush_BC8> PAINT_BRUSHS = ItemByEnum.creatItems(ItemPaintbrush_BC8::new, new Item.Properties().durability(64).tab(BCCore.BUILDCRAFT_TAB),
    		DyeColor.values(), DyeColor.class, "paintbrush", ITEMS);;
    public static final EnumMap<EnumEngineType, MultiBlockItem<EnumEngineType>> ENGINE_ITEM_MAP = new EnumMap<EnumEngineType, MultiBlockItem<EnumEngineType>>(EnumEngineType.class);
    public static final EnumMap<EnumSpring, MultiBlockItem<EnumSpring>> SPRING_ITEM_MAP = new EnumMap<EnumSpring, MultiBlockItem<EnumSpring>>(EnumSpring.class);
    
    public static final RegistryObject<MultiBlockItem<EnumEngineType>> ENGINE_RESTONE_ITEM_BC8 = ITEMS.register("engine_redstone", () -> new MultiBlockItem<EnumEngineType>(BCCoreBlocks.ENGINE_BC8.get(),new Item.Properties().tab(BCCore.BUILDCRAFT_TAB),EnumEngineType.WOOD, ENGINE_ITEM_MAP));
    public static final RegistryObject<MultiBlockItem<EnumEngineType>> ENGINE_CREATIVE_ITEM_BC8 = ITEMS.register("engine_creative", () -> new MultiBlockItem<EnumEngineType>(BCCoreBlocks.ENGINE_BC8.get(),new Item.Properties().tab(BCCore.BUILDCRAFT_TAB),EnumEngineType.CREATIVE, ENGINE_ITEM_MAP));
    
    public static final RegistryObject<MultiBlockItem<EnumSpring>> SPRING_WATER = ITEMS.register("spring_water", () -> new MultiBlockItem<EnumSpring>(BCCoreBlocks.SPRING.get(),new Item.Properties().tab(BCCore.BUILDCRAFT_TAB), EnumSpring.WATER, SPRING_ITEM_MAP));
    public static final RegistryObject<MultiBlockItem<EnumSpring>> SPRING_OIL = ITEMS.register("spring_oil", () -> new MultiBlockItem<EnumSpring>(BCCoreBlocks.SPRING.get(),new Item.Properties().tab(BCCore.BUILDCRAFT_TAB), EnumSpring.OIL, SPRING_ITEM_MAP));

    public static final RegistryObject<BlockItem> MARKER_PATH = ITEMS.register("marker_path", () -> new BlockItem(BCCoreBlocks.MARKER_PATH.get(), new Item.Properties().tab(BCCore.BUILDCRAFT_TAB)));
    public static final RegistryObject<BlockItem> MARKER_VOLUME = ITEMS.register("marker_volume", () -> new BlockItem(BCCoreBlocks.MARKER_VOLUME.get(), new Item.Properties().tab(BCCore.BUILDCRAFT_TAB)));

    static void registry(IEventBus m) {
        ITEMS.register(m);
    }
    
    public static void registerItemProperties() {
    	ResourceLocation label = new ResourceLocation("buildcraftcore","map_type");
    	ItemProperties.register(MAP_LOCATION.get(), label, (itemStack, ClientWorld, entity, p_174638_) -> {
    		return (MapLocationType.getFromStack(itemStack).meta + 2)/8f;
    	});
/*    	ItemProperties.register(TEMPLATE.get(), label, (itemStack, ClientWorld, entity, p_174638_) -> {
			return itemStack.getDamageValue() == ItemSchematicSingle.DAMAGE_CLEAN ? 0.0F : 1.0F;
    	});*/
    }
}
