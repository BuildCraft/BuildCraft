package buildcraft.lib.registry;

import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.item.IItemBuildCraft;
import buildcraft.lib.item.ItemBlockBC_Neptune;
import buildcraft.lib.item.ItemPropertiesCreator;
import buildcraft.lib.registry.TagManager.EnumTagType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.javafmlmod.FMLModContainer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * Registration helper for static blocks and items: those which will always be registered. This is intended to simplify
 * item/block registry usage, as it looks like forge will start to support dynamically registered ones. (Perhaps we
 * could allow this to work dynamically by looking items up in the config on reload? Either way we need to see what
 * forge does in the future.)
 */
public final class RegistrationHelper {
//    private static final Map<String, Block> oredictBlocks = new HashMap<>();
//    private static final Map<String, Item> oredictItems = new HashMap<>();

    private final List<RegistryObject<? extends Block>> blocks = new ArrayList<>();
    private final List<RegistryObject<? extends Item>> items = new ArrayList<>();


    public final DeferredRegister<Block> BLOCKS;
    public final DeferredRegister<Item> ITEMS;
    public final DeferredRegister<BlockEntityType<?>> TILE_ENTITIES;

    private final String namespace;

    public RegistrationHelper(String namespace) {
        BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, namespace);
        ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, namespace);
        TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, namespace);
        IEventBus modEventBus = ((FMLModContainer) ModList.get().getModContainerById(namespace).get()).getEventBus(); // Calen: don't use FMLJavaModLoadingContext.get().getModEventBus()
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        TILE_ENTITIES.register(modEventBus);

        this.namespace = namespace;
    }

//    public static void registerOredictEntries() {
//        for (Entry<String, Item> entry : oredictItems.entrySet()) {
//            OreDictionary.registerOre(entry.getKey(), entry.getValue());
//        }
//        for (Entry<String, Block> entry : oredictBlocks.entrySet()) {
//            OreDictionary.registerOre(entry.getKey(), entry.getValue());
//        }
//    }

//    @SubscribeEvent
//    public final void onRegisterBlocks(RegistryEvent.Register<Block> event){
//        for (Block block : blocks){
//            event.getRegistry().register(block);
//        }
//    }

//    @SubscribeEvent
//    public final void onRegisterItems(RegistryEvent.Register<Item> event) {
//        for (Item item : items) {
//            event.getRegistry().register(item);
//        }
//    }

    // Calen: not still used in 1.18.2
//    @SubscribeEvent
//    @SideOnly(Side.CLIENT)
//    public final void onModelRegistry(ModelRegistryEvent event) {
//        for (Item item : items) {
//            if (item instanceof IItemBuildCraft) {
//                ((IItemBuildCraft) item).registerVariants();
//            }
//        }
//    }

    @Nullable
    public <I extends Item> RegistryObject<I> addItem(String id, Item.Properties properties, BiFunction<String, Item.Properties, I> item) {
        return addItem(id, properties, item, false);
    }

    @Nullable
    public <I extends Item> RegistryObject<I> addItem(String id, String registryId, Item.Properties properties, BiFunction<String, Item.Properties, I> item) {
        return addItem(id, registryId, properties, item, false);
    }

    @Nullable
    public <I extends Item> RegistryObject<I> addItem(String idBC, Item.Properties properties, BiFunction<String, Item.Properties, I> item, boolean force) {
        if (force || RegistryConfig.isEnabledItem(idBC)) {
            return addForcedItem(idBC, properties, item);
        } else {
            return null;
        }
    }

    @Nullable
    public <I extends Item> RegistryObject<I> addItem(String idBC, String registryId, Item.Properties properties, BiFunction<String, Item.Properties, I> item, boolean force) {
//        if (force || RegistryConfig.isEnabled(item))
        if (force || RegistryConfig.isEnabledItem(idBC)) {
            return addForcedItem(idBC, registryId, properties, item);
        } else {
            return null;
        }
    }

    public <I extends Item> RegistryObject<I> addForcedItem(String idBC, Item.Properties properties, BiFunction<String, Item.Properties, I> item) {
//        items.add(item);
//        if (item instanceof IItemBuildCraft) {
//            IItemBuildCraft itemBC = (IItemBuildCraft) item;
//            String id = itemBC.id();
//            if (!id.isEmpty()) {
//                String[] oldRegNames = TagManager.getMultiTag(id, EnumTagTypeMulti.OLD_REGISTRY_NAME);
//                MigrationManager.INSTANCE.addItemMigration(item, oldRegNames);
//                if (TagManager.hasTag(id, EnumTagType.OREDICT_NAME)) {
//                    oredictItems.put(TagManager.getTag(id, EnumTagType.OREDICT_NAME), item);
//                }
//            }
//        }
        String registryId = TagManager.getTag(idBC, EnumTagType.REGISTRY_NAME).replace(this.namespace + ":", "");
        RegistryObject<I> reg = ITEMS.register(registryId, () -> item.apply(idBC, properties));
        items.add(reg);
        return reg;
    }

    public <I extends Item> RegistryObject<I> addForcedItem(String idBC, String registryId, Item.Properties properties, BiFunction<String, Item.Properties, I> item) {
        RegistryObject<I> reg = ITEMS.register(registryId, () -> item.apply(idBC, properties));
        items.add(reg);
        return reg;
    }

    public <I extends Item> RegistryObject<I> addForcedItem(String registryId, Supplier<I> item) {
        RegistryObject<I> reg = ITEMS.register(registryId, item);
        items.add(reg);
        return reg;
    }

    public <I extends Item> RegistryObject<I> addForcedBlockItem(String idBC, Supplier<I> item) {
        String registryId = TagManager.getTag(idBC, EnumTagType.REGISTRY_NAME).replace(this.namespace + ":", "");
        RegistryObject<I> reg = ITEMS.register(registryId, item);
        items.add(reg);
        return reg;
    }

    public <I extends Item> RegistryObject<I> addForcedBlockItem(String idBC, String registryId, Supplier<I> item) {
        RegistryObject<I> reg = ITEMS.register(registryId, item);
        items.add(reg);
        return reg;
    }

    @Nullable
//    public <B extends Block> RegistryObject<B> addBlock(B block)
    public <B extends Block> RegistryObject<B> addBlock(String idBC, BlockBehaviour.Properties properties, BiFunction<String, BlockBehaviour.Properties, B> block) {
        return addBlock(idBC, properties, block, false);
    }

    @Nullable
//    public <B extends Block> RegistryObject<B> addBlock(B block, boolean force)
    public <B extends Block> RegistryObject<B> addBlock(String idBC, BlockBehaviour.Properties properties, BiFunction<String, BlockBehaviour.Properties, B> block, boolean force) {
//        if (force || RegistryConfig.isEnabled(block))
        if (force || RegistryConfig.isEnabledBlock(idBC)) {
            return addForcedBlock(idBC, properties, block);
        } else {
            return null;
        }
    }

    public <B extends Block> RegistryObject<B> addBlock(String idBC, String regId, BlockBehaviour.Properties properties, BiFunction<String, BlockBehaviour.Properties, B> block, boolean force) {
//        if (force || RegistryConfig.isEnabled(block))
        if (force || RegistryConfig.isEnabledBlock(idBC)) {
            return addForcedBlock(idBC, regId, properties, block);
        } else {
            return null;
        }
    }

    // public <B extends Block> RegistryObject<B> addForcedBlock(B block)
    public <B extends Block> RegistryObject<B> addForcedBlock(String idBC, BlockBehaviour.Properties properties, BiFunction<String, BlockBehaviour.Properties, B> block) {
//        blocks.add(block);
//        if (block instanceof BlockBCBase_Neptune) {
//            String id = ((BlockBCBase_Neptune) block).id;
//            if (!id.isEmpty()) {
//                String[] oldRegNames = TagManager.getMultiTag(id, EnumTagTypeMulti.OLD_REGISTRY_NAME);
//                MigrationManager.INSTANCE.addBlockMigration(block, oldRegNames);
//                if (TagManager.hasTag(id, EnumTagType.OREDICT_NAME)) {
//                    oredictBlocks.put(TagManager.getTag(id, EnumTagType.OREDICT_NAME), block);
//                }
//            }
//        }
//        return block;
        String registryId = TagManager.getTag(idBC, EnumTagType.REGISTRY_NAME).replace(this.namespace + ":", "");
        RegistryObject<B> reg = BLOCKS.register(registryId, () -> block.apply(idBC, properties));
        blocks.add(reg);
        return reg;
    }

    public <B extends Block> RegistryObject<B> addForcedBlock(String idBC, String registryId, BlockBehaviour.Properties properties, BiFunction<String, BlockBehaviour.Properties, B> block) {
        RegistryObject<B> reg = BLOCKS.register(registryId, () -> block.apply(idBC, properties));
        blocks.add(reg);
        return reg;
    }

    @Nullable
//    public <B extends BlockBCBase_Neptune> RegistryObject<B> addBlockAndItem(B block)
    public <B extends BlockBCBase_Neptune> RegistryObject<B> addBlockAndItem(String idBCBlock, BlockBehaviour.Properties properties, BiFunction<String, BlockBehaviour.Properties, B> block) {
        return addBlockAndItem(idBCBlock, properties, block, false, ItemBlockBC_Neptune::new);
    }

    @Nullable
//    public <B extends BlockBCBase_Neptune> RegistryObject<B> addBlockAndItem(B block, boolean force)
    public <B extends BlockBCBase_Neptune> RegistryObject<B> addBlockAndItem(String idBCBlock, BlockBehaviour.Properties properties, BiFunction<String, BlockBehaviour.Properties, B> block, boolean force) {
        return addBlockAndItem(idBCBlock, properties, block, force, ItemBlockBC_Neptune::new);
    }

    @Nullable
//    public <B extends BlockBCBase_Neptune, I extends Item & IItemBuildCraft> RegistryObject<B> addBlockAndItem(B block, Function<B, I> itemBlockConstructor)
    public <B extends BlockBCBase_Neptune, I extends Item & IItemBuildCraft> RegistryObject<B> addBlockAndItem(String idBCBlock, BlockBehaviour.Properties properties, BiFunction<String, BlockBehaviour.Properties, B> block, BiFunction<B, Item.Properties, I> itemBlockConstructor) {
        return addBlockAndItem(idBCBlock, properties, block, false, itemBlockConstructor);
    }

    public <B extends BlockBCBase_Neptune, I extends Item & IItemBuildCraft> RegistryObject<B> addBlockAndItem(String idBCBlock, String regId, BlockBehaviour.Properties properties, BiFunction<String, BlockBehaviour.Properties, B> block, BiFunction<B, Item.Properties, I> itemBlockConstructor) {
        return addBlockAndItem(idBCBlock, regId, properties, block, false, itemBlockConstructor);
    }

    // public <B extends BlockBCBase_Neptune, I extends Item & IItemBuildCraft> RegistryObject<B> addBlockAndItem(B block, boolean force, Function<B, I> itemBlockConstructor)
    public <B extends BlockBCBase_Neptune, I extends Item & IItemBuildCraft> RegistryObject<B> addBlockAndItem(String idBCBlock, BlockBehaviour.Properties properties, BiFunction<String, BlockBehaviour.Properties, B> block, boolean force, BiFunction<B, Item.Properties, I> itemBlockConstructor) {
        RegistryObject<B> added = addBlock(idBCBlock, properties, block, force);
        if (added != null) {
            String idBCItem = "item." + idBCBlock;
//            addForcedItem(itemBlockConstructor.apply(added));
            addForcedBlockItem(idBCItem, () -> itemBlockConstructor.apply(added.get(), ItemPropertiesCreator.blockItem()));
        } else {
            // FIXME: This won't work if the item has a different reg name to the block!
//            RegistryConfig.setDisabled("items", block.getRegistryName().getResourcePath());
            RegistryConfig.setDisabled("items", idBCBlock);
        }
        return added;
    }

    public <B extends BlockBCBase_Neptune, I extends Item & IItemBuildCraft> RegistryObject<B> addBlockAndItem(String idBCBlock, String regId, BlockBehaviour.Properties properties, BiFunction<String, BlockBehaviour.Properties, B> block, boolean force, BiFunction<B, Item.Properties, I> itemBlockConstructor) {
        RegistryObject<B> added = addBlock(idBCBlock, regId, properties, block, force);
        if (added != null) {
            String idBCItem = "item." + idBCBlock;
//            addForcedItem(itemBlockConstructor.apply(added));
            addForcedBlockItem(idBCItem, regId, () -> itemBlockConstructor.apply(added.get(), ItemPropertiesCreator.blockItem()));
        } else {
            // FIXME: This won't work if the item has a different reg name to the block!
//            RegistryConfig.setDisabled("items", block.getRegistryName().getResourcePath());
            RegistryConfig.setDisabled("items", idBCBlock);
        }
        return added;
    }

    public <T extends BlockEntity> RegistryObject<BlockEntityType<T>> registerTile(String idBC, BlockEntityType.BlockEntitySupplier<T> blockEntityConstructor, RegistryObject<? extends Block> block) {
        String regName = TagManager.getTag(idBC, EnumTagType.REGISTRY_NAME).replace(this.namespace + ":", "");
//        String[] alternatives = TagManager.getMultiTag(id, EnumTagTypeMulti.OLD_REGISTRY_NAME);
//        GameRegistry.registerTileEntity(clazz, regName);
//        return register.register(
//                regName,
//                () -> BlockEntityType.Builder.of(
//                        blockEntityConstructor,
//                        block.get()
//                ).build(null)
//        );
        return TILE_ENTITIES.register(
                regName,
                () -> BlockEntityType.Builder.of(
                        blockEntityConstructor,
                        block.get()
                ).build(null));
    }
}
