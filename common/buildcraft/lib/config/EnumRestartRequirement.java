package buildcraft.lib.config;

import net.minecraftforge.common.config.Property;

public enum EnumRestartRequirement {
    NONE(false, false),
    WORLD(true, false),
    GAME(true, true);

    private final boolean restartWorld, restartGame;

    EnumRestartRequirement(boolean restartWorld, boolean restartGame) {
        this.restartWorld = restartWorld;
        this.restartGame = restartGame;
    }

    public void setTo(Property prop) {
        prop.setRequiresWorldRestart(restartWorld);
        prop.setRequiresMcRestart(restartGame);
    }

    public boolean hasBeenRestarted(EnumRestartRequirement requirement) {
        if (restartGame && !requirement.restartGame) return false;
        if (restartWorld && !requirement.restartWorld) return false;
        return true;
    }
}
