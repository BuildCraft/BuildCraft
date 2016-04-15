package buildcraft.lib.guide;

import java.util.Map;

import com.google.common.collect.Maps;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.core.lib.utils.Utils;
import buildcraft.lib.guide.block.IBlockGuidePageMapper;
import buildcraft.lib.guide.parts.GuideImage;
import buildcraft.lib.guide.parts.GuidePageBase;
import buildcraft.lib.guide.parts.GuidePart;
import buildcraft.lib.guide.parts.GuidePartFactory;

@SideOnly(Side.CLIENT)
public class GuideManager {
    static final Map<ModContainer, GuideManager> managers = Maps.newHashMap();

    /** A cache of what has been loaded so far by this guide. */
    static private final Map<ResourceLocation, GuidePartFactory<?>> guideMap = Maps.newHashMap();
    /** All of the guide pages that have been registered to appear in this guide manager */
    static final Map<ResourceLocation, GuidePartFactory<GuidePageBase>> registeredPages = Maps.newHashMap();
    static final Map<ResourceLocation, PageMeta> pageMetas = Maps.newHashMap();
    /** Base locations for generic chapters */
    private final String locationBase, locationBlock, locationItem, locationPipe, locationRobot;

    final Map<Block, IBlockGuidePageMapper> customMappers = Maps.newHashMap();

    public GuideManager(String assetBase) {
        locationBase = assetBase + ":guide/";
        locationBlock = locationBase + "block/";
        locationItem = locationBase + "item/";
        locationPipe = locationBase + "pipe/";
        locationRobot = locationBase + "robot/";
    }

    public static void registerManager(GuideManager manager) {
        if (manager == null) {
            throw new IllegalArgumentException("Tried to register a null manager!");
        }
        ModContainer container = Loader.instance().activeModContainer();
        if (container == null) {
            throw new IllegalStateException("Tried to register a manager outside of a correct event!");
        }
        if (managers.containsKey(container)) {
            throw new IllegalStateException("Tried to register a manager twice for one mod!");
        }
        managers.put(container, manager);
    }

    // Page Registration

    public void registerCustomPage(ResourceLocation location, GuidePartFactory<GuidePageBase> page) {
        registeredPages.put(location, page);
        guideMap.put(location, page);
    }

    public void registerPage(ResourceLocation location) {
        registerCustomPage(location, (GuidePartFactory<GuidePageBase>) getPartFactory(location));
    }

    // Registration

    public void registerPageWithTitle(ResourceLocation location, PageMeta meta) {
        registerPage(location);
        pageMetas.put(location, meta);
    }

    public void registerBlock(Block block) {
        ResourceLocation location = new ResourceLocation(locationBlock + Utils.getModSpecificNameForBlock(block) + ".md");
        PageMeta meta = new PageMeta(block.getLocalizedName(), "", "");
        registerPageWithTitle(location, meta);
    }

    public void registerCustomBlock(Block block, IBlockGuidePageMapper mapper) {
        customMappers.put(block, mapper);
        for (String page : mapper.getAllPossiblePages()) {
            registerPage(new ResourceLocation(locationBlock + page + ".md"));
        }
    }

    /** Automatically registers all blocks that the calling mod has registered. Use {@link #unregisterBlock(Block)} to
     * remove specific blocks you don't want added to the guide. */
    public void registerAllBlocks() {
        ModContainer container = Loader.instance().activeModContainer();
        if (container == null) {
            // This is a definite coding error, so crash in the dev environment rather than putting up a warning
            throw new IllegalStateException("Was called outside the scope of an active mod! This is not how this is meant to be used!");
        }
        String prefix = container.getModId();
        for (ResourceLocation location : Block.REGISTRY.getKeys()) {
            String domain = location.getResourceDomain();
            if (domain.equalsIgnoreCase(prefix)) {
                registerBlock(Block.REGISTRY.getObject(location));
            }
        }
    }

    public void registerItem(Item item) {
        ResourceLocation location = new ResourceLocation(locationItem + Utils.getModSpecificNameForItem(item) + ".md");
        PageMeta meta = getPageMeta(location);
        if (meta == null) {
            meta = new PageMeta(new ItemStack(item).getDisplayName(), "", "");
        } else if (StringUtils.isEmpty(meta.title) || meta.title.contains(location.toString())) {
            meta = new PageMeta(new ItemStack(item).getDisplayName(), meta.customLocation, meta.customImageLocation);
        }
        registerPageWithTitle(location, meta);
    }

    public void registerAllItems(boolean andItemBlocks) {
        ModContainer container = Loader.instance().activeModContainer();
        if (container == null) {
            // This is a definite coding error, so crash in the dev environment rather than putting up a warning
            throw new IllegalStateException("Was called outside the scope of an active mod! This is not how this is meant to be used!");
        }
        String prefix = container.getModId();
        for (ResourceLocation location : Item.REGISTRY.getKeys()) {
            if (Block.getBlockFromItem(Item.REGISTRY.getObject(location)) != null && !andItemBlocks) {
                continue;
            }
            String domain = location.getResourceDomain();
            if (domain.equalsIgnoreCase(prefix)) {
                registerItem(Item.REGISTRY.getObject(location));
            }
        }
    }

    // Unregistering

    public void unregister(ResourceLocation location) {
        registeredPages.remove(location);
        guideMap.remove(location);
    }

    public void unregisterBlock(Block block) {
        unregister(new ResourceLocation(locationBlock + Utils.getModSpecificNameForBlock(block)));
    }

    public void unregisterItem(Item item) {
        unregister(new ResourceLocation(locationItem + Utils.getNameForItem(item)));
    }

    // public void unregisterPipe(PipeDefinition definition) {
    // unregister(new ResourceLocation(locationPipe + definition.modUniqueTag));
    // }

    // TODO (PASS 1): Add robots the the in game manual
    // public void unregisterRobot(buildcraft.api.boards.RedstoneBoardRobot robot) {
    // unregister(new ResourceLocation(locationEntity + mechanic));
    // }

    // Part getters

    static GuidePartFactory<?> getPartFactory(ResourceLocation location) {
        if (guideMap.containsKey(location)) {
            return guideMap.get(location);
        }
        GuidePartFactory<?> part = null;
        if (location.getResourcePath().endsWith("md")) {// Wiki info page (Markdown)
            part = MarkdownLoader.loadMarkdown(location);
        } else {
            throw new IllegalArgumentException("Recieved an unknown filetype! " + location);
        }
        guideMap.put(location, part);
        return part;
    }

    public static PageMeta getPageMeta(ResourceLocation location) {
        if (pageMetas.containsKey(location)) {
            return pageMetas.get(location);
        }
        ResourceLocation metaLoc = new ResourceLocation(location + ".json");
        PageMeta meta = PageMetaLoader.load(metaLoc);
        pageMetas.put(location, meta);
        return meta;
    }

    private static GuidePart getPart(ResourceLocation location, GuiGuide gui) {
        return getPartFactory(location).createNew(gui);
    }

    public static GuidePageBase getPage(ResourceLocation location, GuiGuide gui) {
        return (GuidePageBase) getPart(location, gui);
    }

    private GuidePageBase getPage(String locationBase, GuiGuide gui) {
        return (GuidePageBase) getPart(new ResourceLocation(locationBase + ".md"), gui);
    }

    public GuidePageBase getItemPage(Item item, GuiGuide gui) {
        return getPage(locationItem + Utils.getNameForItem(item), gui);
    }

    public GuidePageBase getBlockPage(Block block, GuiGuide gui) {
        return getPage(locationBlock + Utils.getNameForBlock(block), gui);
    }

    // public GuidePageBase getPipePage(PipeDefinition definition, GuiGuide gui) {
    // return getPage(locationPipe + definition.modUniqueTag, gui);
    // }

    // TODO (PASS 1): Add robots to the in-game manual
    // public GuidePageBase getMechanicPage(String mechanic, GuiGuide gui) {
    // return getPage(locationMechanic + mechanic, gui);
    // }

    /** Gets an image for display that location */
    public GuideImage getImage(String imageLocation, GuiGuide gui) {
        return (GuideImage) getPart(new ResourceLocation(locationBase, imageLocation + ".png"), gui);
    }
}
