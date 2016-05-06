package buildcraft.lib.guide;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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

    // Page Registration

    public static void registerCustomPage(ResourceLocation location, GuidePartFactory<GuidePageBase> page) {
        registeredPages.put(location, page);
        guideMap.put(location, page);
        pageMetas.put(location, getPageMeta(location));
    }

    public static void registerPage(ResourceLocation location) {
        registerCustomPage(location, (GuidePartFactory<GuidePageBase>) getPartFactory(location));
    }

    public void registerPage(String subFolder) {
        registerPage(new ResourceLocation(locationBase + subFolder));
    }

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

    /** Gets an image for display that location */
    public GuideImage getImage(String imageLocation, GuiGuide gui) {
        return (GuideImage) getPart(new ResourceLocation(locationBase, imageLocation + ".png"), gui);
    }
}
