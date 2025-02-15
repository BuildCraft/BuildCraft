package buildcraft.lib.config;

import buildcraft.api.BCModules;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLModContainer;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// TODO Calen: In 1.18.2 config file is loaded after item/block/fluid/... registry, so the registries like propOilIsSticky/enableOilBurn/... is always default value.
public class BCConfig {
    private static Map<BCModules, List<Runnable>> reloadListeners = new ConcurrentHashMap<>();

    public static void registerReloadListener(BCModules module, Runnable reload) {
        reloadListeners.computeIfAbsent(module, m -> new LinkedList<>()).add(reload);
        // 1.18.2: ModConfigEvent is IModBusEvent
        ((FMLModContainer) ModList.get().getModContainerById(module.getModId()).get()).getEventBus().register(BCConfig.class);
    }

    @SubscribeEvent
//    public static void onConfigChange(OnConfigChangedEvent cce)
    public static void onConfigChange(ModConfigEvent cce) {
//        if (BCModules.isBcMod(cce.getModID()))
        if (BCModules.isBcMod(cce.getConfig().getModId())) {
//            EnumRestartRequirement req = EnumRestartRequirement.NONE;
//            if (Loader.instance().isInState(LoaderState.AVAILABLE)) {
//                // The loaders state will be LoaderState.SERVER_STARTED when we are in a world
//                req = EnumRestartRequirement.WORLD;
//            }
//            for (Consumer<EnumRestartRequirement> listener : reloadListeners) {
//                listener.accept(req);
//            }

            reloadListeners.getOrDefault(BCModules.getBcMod(cce.getConfig().getModId()), new LinkedList<>()).forEach(Runnable::run);
        }
    }
}
