package ct.buildcraft.builders;

import ct.buildcraft.api.enums.EnumSnapshotType;
import ct.buildcraft.builders.item.ItemFillerPlanner;
import ct.buildcraft.builders.item.ItemSchematicSingle;
import ct.buildcraft.builders.item.ItemSnapshot;
import ct.buildcraft.builders.item.ItemSnapshot.EnumItemSnapshotType;
import ct.buildcraft.core.BCCore;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BCBuildersItems {
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, BCBuilders.MODID);
	
    public static final RegistryObject<ItemSnapshot> BLUEPRINT = ITEMS.register("blueprint", () -> new ItemSnapshot(new Item.Properties().tab(BCCore.BUILDCRAFT_TAB), EnumSnapshotType.BLUEPRINT));
    public static final RegistryObject<ItemSnapshot> TEMPLATE = ITEMS.register("template", () -> new ItemSnapshot(new Item.Properties().tab(BCCore.BUILDCRAFT_TAB), EnumSnapshotType.TEMPLATE));
    public static final RegistryObject<ItemSchematicSingle> SCHEMATIC_SINGLE = ITEMS.register("schematic_single", () -> new ItemSchematicSingle(new Item.Properties().tab(BCCore.BUILDCRAFT_TAB).stacksTo(1)));
    public static final RegistryObject<ItemFillerPlanner> FILLER_PLANNER = ITEMS.register("filler_planner", () -> new ItemFillerPlanner(new Item.Properties().tab(BCCore.BUILDCRAFT_TAB)));


    public static final RegistryObject<BlockItem> FILLER_BLOCK_ITEM = ITEMS.register("filler", () -> new BlockItem(BCBuildersBlocks.FILLER.get(),new Item.Properties().tab(BCCore.BUILDCRAFT_TAB)));
    public static final RegistryObject<BlockItem> BUILDER_BLOCK_ITEM = ITEMS.register("builder", () -> new BlockItem(BCBuildersBlocks.BUILDER.get(),new Item.Properties().tab(BCCore.BUILDCRAFT_TAB)));
    public static final RegistryObject<BlockItem> ARCHITECT_BLOCK_ITEM = ITEMS.register("architect", () -> new BlockItem(BCBuildersBlocks.ARCHITECT.get(),new Item.Properties().tab(BCCore.BUILDCRAFT_TAB)));
    public static final RegistryObject<BlockItem> LIBRARY_BLOCK_ITEM = ITEMS.register("library", () -> new BlockItem(BCBuildersBlocks.LIBRARY.get(),new Item.Properties().tab(BCCore.BUILDCRAFT_TAB)));
    public static final RegistryObject<BlockItem> REPLACER_BLOCK_ITEM = ITEMS.register("replacer", () -> new BlockItem(BCBuildersBlocks.REPLACER.get(),new Item.Properties().tab(BCCore.BUILDCRAFT_TAB)));
    public static final RegistryObject<BlockItem> FRAME_BLOCK_ITEM = ITEMS.register("frame", () -> new BlockItem(BCBuildersBlocks.FRAME.get(),new Item.Properties().tab(BCCore.BUILDCRAFT_TAB)));
    public static final RegistryObject<BlockItem> QUARRY_BLOCK_ITEM = ITEMS.register("quarry", () -> new BlockItem(BCBuildersBlocks.QUARRY.get(),new Item.Properties().tab(BCCore.BUILDCRAFT_TAB)));
    
    
    	

    public static void registry(IEventBus b) {
    	ITEMS.register(b);
    }
    
    public static void registerItemProperties() {
    	ResourceLocation label = new ResourceLocation("buildcraftbuilder","used");
    	ItemProperties.register(BLUEPRINT.get(), label, (itemStack, ClientWorld, entity, p_174638_) -> {
    			return EnumItemSnapshotType.getFromStack(itemStack).used ? 1.1F : 0.1F;
    	});
    	ItemProperties.register(TEMPLATE.get(), label, (itemStack, ClientWorld, entity, p_174638_) -> {
			return EnumItemSnapshotType.getFromStack(itemStack).used ? 1.0F : 0.0F;
    	});
/*    	ItemProperties.register(TEMPLATE.get(), label, (itemStack, ClientWorld, entity, p_174638_) -> {
			return itemStack.getDamageValue() == ItemSchematicSingle.DAMAGE_CLEAN ? 0.0F : 1.0F;
    	});*/
    }
}
