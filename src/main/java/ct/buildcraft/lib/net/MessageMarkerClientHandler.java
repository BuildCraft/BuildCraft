package ct.buildcraft.lib.net;

import java.util.ArrayDeque;
import java.util.Queue;

import ct.buildcraft.api.core.BCLog;
import ct.buildcraft.lib.marker.MarkerCache;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MessageMarkerClientHandler {
    private static final Queue<MessageMarker> QUEUED_MESSAGES = new ArrayDeque<>();

    public static Level getClientLevel() {
        return Minecraft.getInstance().level;
    }

    public static void clearQueuedMessages() {
        QUEUED_MESSAGES.clear();
    }

    public static boolean handleOrQueue(MessageMarker message) {
        Level world = getClientLevel();
        if (world == null) {
            QUEUED_MESSAGES.add(message.copy());
            return false;
        }
        handle(message, world);
        return true;
    }

    public static void flushQueuedMessages() {
        Level world = getClientLevel();
        if (world == null || QUEUED_MESSAGES.isEmpty()) {
            return;
        }
        while (!QUEUED_MESSAGES.isEmpty()) {
            handle(QUEUED_MESSAGES.poll(), world);
        }
    }

    private static void handle(MessageMarker message, Level world) {
        if (message.cacheId < 0 || message.cacheId >= MarkerCache.CACHES.size()) {
            if (MessageManager.DEBUG) {
                BCLog.logger.warn("[lib.messages][marker] The cache ID " + message.cacheId + " was invalid!");
            }
            return;
        }
        MarkerCache<?> cache = MarkerCache.CACHES.get(message.cacheId);
        cache.getSubCache(world).handleMessageMain(message);
    }
}
