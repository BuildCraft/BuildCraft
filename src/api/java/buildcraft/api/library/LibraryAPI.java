package buildcraft.api.library;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class LibraryAPI {
    private static final Set<ILibraryTypeHandler> handlers = new HashSet<ILibraryTypeHandler>();
    private static final Map<String, ILibraryTypeHandler> handlersByExt = new HashMap<String, ILibraryTypeHandler>();

    private LibraryAPI() {

    }

    public static Set<ILibraryTypeHandler> getHandlerSet() {
        return handlers;
    }

    public static void registerHandler(ILibraryTypeHandler handler) {
        handlers.add(handler);
        handlersByExt.put(handler.getFileExtension(), handler);
    }

    public static ILibraryTypeHandler getHandler(String ext) {
        return handlersByExt.get(ext);
    }
}
