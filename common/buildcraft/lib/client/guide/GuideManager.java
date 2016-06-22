package buildcraft.lib.client.guide;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import buildcraft.lib.client.guide.block.IBlockGuidePageMapper;
import buildcraft.lib.client.guide.parts.*;
import buildcraft.lib.client.resource.MarkdownResourceHolder;
import buildcraft.lib.client.resource.ResourceRegistry;

@SideOnly(Side.CLIENT)
public class GuideManager {
    static final Map<ModContainer, GuideManager> managers = Maps.newHashMap();

    /** A cache of what has been loaded so far by this guide. */
    static private final Map<ResourceLocation, GuidePartFactory<?>> guideMap = new HashMap<>();
    /** All of the guide pages that have been registered to appear in this guide manager */
    static final Map<ResourceLocation, GuidePartFactory<? extends GuidePageBase>> registeredPages = new HashMap<>();
    static final Map<ResourceLocation, PageMeta> pageMetas = new HashMap<>();
    static final Map<ItemStack, ResourceLocation> itemToPages = new HashMap<>();

    private static final Map<ItemStack, GuidePartFactory<? extends GuidePageBase>> generatedPages = new HashMap<>();

    /** Base locations for generic chapters */
    private final String locationBase;

    final Map<Block, IBlockGuidePageMapper> customMappers = Maps.newHashMap();

    public GuideManager(String assetBase) {
        locationBase = assetBase + ":guide/";
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

    @Nonnull
    public static GuidePartFactory<? extends GuidePageBase> getPageFor(ItemStack stack) {
        GuidePartFactory<? extends GuidePageBase> existing = null;
        for (Entry<ItemStack, ResourceLocation> entry : itemToPages.entrySet()) {
            if (OreDictionary.itemMatches(entry.getKey(), stack, false)) {
                existing = registeredPages.get(entry.getValue());
                if (existing != null) {
                    return existing;
                }
            }
        }
        // Create a dummy page for the stack
        existing = generatedPages.get(stack);
        if (existing == null) {
            existing = GuidePageStandInRecipes.createFactory(stack);
            generatedPages.put(stack, existing);
        }
        return existing;
    }

    // Page Registration

    public static void registerCustomPage(ResourceLocation location, GuidePartFactory<? extends GuidePageBase> page) {
        registeredPages.put(location, page);
        guideMap.put(location, page);
        PageMeta meta = getPageMeta(location);
        pageMetas.put(location, meta);
        if (!StringUtils.isNullOrEmpty(meta.itemStack)) {
            ItemStack stack = MarkdownResourceHolder.loadItemStack(meta.itemStack);
            if (stack != null) {
                for (ItemStack key : itemToPages.keySet()) {
                    if (OreDictionary.itemMatches(key, stack, false)) {
                        return;
                    }
                }
                itemToPages.put(stack, location);
            }
        }
    }

    public static void registerPage(ResourceLocation location) {
        registerCustomPage(location, (GuidePartFactory<? extends GuidePageBase>) getPartFactory(location));
    }

    public void registerPage(String subFolder) {
        registerPage(new ResourceLocation(locationBase + subFolder));
    }

    // Part getters

    static GuidePartFactory<?> getPartFactory(ResourceLocation location) {
        if (guideMap.containsKey(location)) {
            return guideMap.get(location);
        }
        PageMeta meta = getPageMeta(location);
        GuidePartFactory<?> part = null;
        if (location.getResourcePath().endsWith(".md")) {// Wiki info page (Markdown)
            MarkdownResourceHolder holder = new MarkdownResourceHolder(location, meta.title == null ? "null" : meta.title);
            part = ResourceRegistry.INSTANCE.register(holder, MarkdownResourceHolder.class);
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

    /** Gets an image for display that location */
    public GuideImage getImage(String imageLocation, GuiGuide gui) {
        return (GuideImage) getPart(new ResourceLocation(locationBase, imageLocation + ".png"), gui);
    }
}