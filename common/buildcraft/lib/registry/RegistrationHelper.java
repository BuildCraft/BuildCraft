package buildcraft.lib.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.item.IItemBuildCraft;
import buildcraft.lib.item.ItemBlockBC_Neptune;
import buildcraft.lib.registry.TagManager.EnumTagType;
import buildcraft.lib.registry.TagManager.EnumTagTypeMulti;

/** Registration helper for static blocks and items: those which will always be registered. This is intended to simplify
 * item/block registry usage, as it looks like forge will start to support dynamically registered ones. (Perhaps we
 * could allow this to work dynamically by looking items up in the config on reload? Either way we need to see what
 * forge does in the future.) */
public final class RegistrationHelper {

    private static final Map<String, Block> oredictBlocks = new HashMap<>();
    private static final Map<String, Item> oredictItems = new HashMap<>();

    private final List<Block> blocks = new ArrayList<>();
    private final List<Item> items = new ArrayList<>();

    public RegistrationHelper() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static void registerOredictEntries() {
        for (Entry<String, Item> entry : oredictItems.entrySet()) {
            OreDictionary.registerOre(entry.getKey(), entry.getValue());
        }
        for (Entry<String, Block> entry : oredictBlocks.entrySet()) {
            OreDictionary.registerOre(entry.getKey(), entry.getValue());
        }
    }

    @SubscribeEvent
    public final void onRegisterBlocks(RegistryEvent.Register<Block> event) {
        for (Block block : blocks) {
            event.getRegistry().register(block);
        }
    }

    @SubscribeEvent
    public final void onRegisterItems(RegistryEvent.Register<Item> event) {
        for (Item item : items) {
            event.getRegistry().register(item);
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public final void onRegisterModels(ModelRegistryEvent event) {
        for (Item item : items) {
            if (item instanceof IItemBuildCraft) {
                ((IItemBuildCraft) item).registerVariants();
            }
        }
    }

    @Nullable
    public <I extends Item> I addItem(I item) {
        return addItem(item, false);
    }

    @Nullable
    public <I extends Item> I addItem(I item, boolean force) {
        if (force || RegistryConfig.isEnabled(item)) {
            return addForcedItem(item);
        } else {
            return null;
        }
    }

    public <I extends Item> I addForcedItem(I item) {
        items.add(item);
        if (item instanceof IItemBuildCraft) {
            IItemBuildCraft itemBC = (IItemBuildCraft) item;
            String id = itemBC.id();
            if (!id.isEmpty()) {
                String[] oldRegNames = TagManager.getMultiTag(id, EnumTagTypeMulti.OLD_REGISTRY_NAME);
                MigrationManager.INSTANCE.addItemMigration(item, oldRegNames);
                if (TagManager.hasTag(id, EnumTagType.OREDICT_NAME)) {
                    oredictItems.put(TagManager.getTag(id, EnumTagType.OREDICT_NAME), item);
                }
            }
        }
        return item;
    }

    @Nullable
    public <B extends Block> B addBlock(B block) {
        return addBlock(block, false);
    }

    @Nullable
    public <B extends Block> B addBlock(B block, boolean force) {
        if (force || RegistryConfig.isEnabled(block)) {
            return addForcedBlock(block);
        } else {
            return null;
        }
    }

    public <B extends Block> B addForcedBlock(B block) {
        blocks.add(block);
        if (block instanceof BlockBCBase_Neptune) {
            String id = ((BlockBCBase_Neptune) block).id;
            if (!id.isEmpty()) {
                String[] oldRegNames = TagManager.getMultiTag(id, EnumTagTypeMulti.OLD_REGISTRY_NAME);
                MigrationManager.INSTANCE.addBlockMigration(block, oldRegNames);
                if (TagManager.hasTag(id, EnumTagType.OREDICT_NAME)) {
                    oredictBlocks.put(TagManager.getTag(id, EnumTagType.OREDICT_NAME), block);
                }
            }
        }
        return block;
    }

    @Nullable
    public <B extends BlockBCBase_Neptune> B addBlockAndItem(B block) {
        return addBlockAndItem(block, false, ItemBlockBC_Neptune::new);
    }

    @Nullable
    public <B extends BlockBCBase_Neptune> B addBlockAndItem(B block, boolean force) {
        return addBlockAndItem(block, force, ItemBlockBC_Neptune::new);
    }

    @Nullable
    public <B extends BlockBCBase_Neptune, I extends Item & IItemBuildCraft> B addBlockAndItem(B block,
        Function<B, I> itemBlockConstructor) {
        return addBlockAndItem(block, false, itemBlockConstructor);
    }

    public <B extends BlockBCBase_Neptune, I extends Item & IItemBuildCraft> B addBlockAndItem(B block, boolean force,
        Function<B, I> itemBlockConstructor) {
        B added = addBlock(block, force);
        if (added != null) {
            addForcedItem(itemBlockConstructor.apply(added));
        }
        return added;
    }

    public void registerTile(Class<? extends TileEntity> clazz, String id) {
        String regName = TagManager.getTag(id, EnumTagType.REGISTRY_NAME);
        String[] alternatives = TagManager.getMultiTag(id, EnumTagTypeMulti.OLD_REGISTRY_NAME);
        GameRegistry.registerTileEntity(clazz, regName);
    }
}
