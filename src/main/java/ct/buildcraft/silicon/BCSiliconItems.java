package ct.buildcraft.silicon;

import java.util.EnumMap;

import ct.buildcraft.api.enums.EnumRedstoneChipset;
import ct.buildcraft.core.BCCore;
import ct.buildcraft.lib.item.ItemByEnum;
import ct.buildcraft.lib.item.ItemPluggableSimple;
import ct.buildcraft.silicon.item.ItemGateCopier;
import ct.buildcraft.silicon.item.ItemPluggableFacade;
import ct.buildcraft.silicon.item.ItemPluggableGate;
import ct.buildcraft.silicon.item.ItemPluggableLens;
import ct.buildcraft.silicon.item.ItemRedstoneChipset;
import ct.buildcraft.silicon.plug.PluggablePulsar;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BCSiliconItems {
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, BCSilicon.MODID);
	public static final RegistryObject<Item> REDSTONE_CRYSTAL = ITEMS.register("redstone_crystal", () -> new Item(new Item.Properties().tab(BCCore.BUILDCRAFT_TAB)));//TODO check
	public static final EnumMap<EnumRedstoneChipset, ItemRedstoneChipset> REDSTONE_CHIPSET_ITEMS = ItemByEnum.creatItems(ItemRedstoneChipset::new
    		, new Item.Properties().stacksTo(16).tab(BCCore.BUILDCRAFT_TAB), EnumRedstoneChipset.values(), EnumRedstoneChipset.class, "redstone_chipset", ITEMS);
    public static final RegistryObject<ItemPluggableGate> PLUG_GATE_ITEM = ITEMS.register("plug/gate", ItemPluggableGate::new); 
    public static final RegistryObject<ItemPluggableFacade> PLUG_FACADE_ITEM = ITEMS.register("plug/facade", ItemPluggableFacade::new); 
    public static final RegistryObject<ItemPluggableLens> PLUG_LENS_ITEM = ITEMS.register("plug/lens", ItemPluggableLens::new); 
    public static final RegistryObject<ItemPluggableSimple> PLUG_LIGHT_SENSOR_ITEM = ITEMS.register("plug/light_sensor", () -> new ItemPluggableSimple(BCSiliconPlugs.lightSensor, new Item.Properties().tab(BCSilicon.tabPlugs))); 
    public static final RegistryObject<ItemPluggableSimple> PLUG_PULSAR_ITEM = ITEMS.register("plug/pulsar", () -> new ItemPluggableSimple(BCSiliconPlugs.pulsar, PluggablePulsar::new, ItemPluggableSimple.PIPE_BEHAVIOUR_ACCEPTS_RS_POWER, new Item.Properties().tab(BCSilicon.tabPlugs))); 
    public static final RegistryObject<ItemGateCopier> GATE_COPIER_ITEM = ITEMS.register("gate_copier", ItemGateCopier::new);
    
    public static final RegistryObject<BlockItem> LASER_BLOCK_ITEM = ITEMS.register("laser", () -> new BlockItem(BCSiliconBlocks.LASER_BLOCK.get(),new Item.Properties().tab(BCCore.BUILDCRAFT_TAB))); 
    public static final RegistryObject<BlockItem> ASSEMBLY_TABLE_ITEM = ITEMS.register("assembly_table", () -> new BlockItem(BCSiliconBlocks.ASSEMBLY_TABLE_BLOCK.get(),new Item.Properties().tab(BCCore.BUILDCRAFT_TAB))); 
    public static final RegistryObject<BlockItem> CHARGING_TABLE_ITEM = ITEMS.register("charging_table", () -> new BlockItem(BCSiliconBlocks.CHARGING_TABLE_BLOCK.get(),new Item.Properties().tab(BCCore.BUILDCRAFT_TAB))); 
    public static final RegistryObject<BlockItem> INTERGRATION_TABLE_ITEM = ITEMS.register("integration_table", () -> new BlockItem(BCSiliconBlocks.INTERGRATION_TABLE_BLOCK.get(),new Item.Properties().tab(BCCore.BUILDCRAFT_TAB))); 
    public static final RegistryObject<BlockItem> ADVANCED_CRAFTING_TABLE_ITEM = ITEMS.register("advanced_crafting_table", () -> new BlockItem(BCSiliconBlocks.ADVANCED_CRAFTING_TABLE_BLOCK.get(),new Item.Properties().tab(BCCore.BUILDCRAFT_TAB))); 
    public static final RegistryObject<BlockItem> PROGRAMMING_TABLE_ITEM = ITEMS.register("programming_table", () -> new BlockItem(BCSiliconBlocks.PROGRAMMING_TABLE_BLOCK.get(),new Item.Properties().tab(BCCore.BUILDCRAFT_TAB))); 
 
    public static void registry(IEventBus b) {
    	ITEMS.register(b);
    }
    
    public static void registerItemProperties() {
    	ResourceLocation label = new ResourceLocation("buildcraftsilicon","isempty");
    	ItemProperties.register(GATE_COPIER_ITEM.get(), label, (itemStack, ClientWorld, entity, p_174638_) -> 
    			(itemStack.getTag()!= null&&itemStack.getTag().contains(ItemGateCopier.NBT_DATA)) ? 0f : 1f);

/*    	ItemProperties.register(TEMPLATE.get(), label, (itemStack, ClientWorld, entity, p_174638_) -> {
			return itemStack.getDamageValue() == ItemSchematicSingle.DAMAGE_CLEAN ? 0.0F : 1.0F;
    	});*/
    }
}
