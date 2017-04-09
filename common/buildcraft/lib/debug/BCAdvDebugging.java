package buildcraft.lib.debug;

/** Holds the current {@link IAdvDebugTarget}. Use {@link DebugRenderHelper} for helpers when rendering debuggables. */
public enum BCAdvDebugging {
    INSTANCE;

    private IAdvDebugTarget target = null;
    IAdvDebugTarget targetClient = null;

    public static boolean isBeingDebugged(IAdvDebugTarget target) {
        return INSTANCE.target == target;
    }

    public static void setCurrentDebugTarget(IAdvDebugTarget target) {
        if (INSTANCE.target != null) {
            INSTANCE.target.disableDebugging();
        }
        INSTANCE.target = target;
    }

    public static void setClientDebugTarget(IAdvDebugTarget target) {
        INSTANCE.targetClient = target;
    }

    public void onServerPostTick() {
        if (target != null) {
            if (!target.doesExistInWorld()) {
                target.disableDebugging();
                target = null;
            } else {
                target.sendDebugState();
            }
        }
    }
}
