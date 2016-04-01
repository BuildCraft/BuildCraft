package buildcraft.core.lib;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IWorldAccess;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import buildcraft.api.core.BCLog;
import buildcraft.core.lib.config.DetailedConfigOption;

public class DebuggingTools {
    public static final DetailedConfigOption OPTION_PRINT_RENDER_UPDATES = new DetailedConfigOption("debug.tool.render.update", "false");

    public static void init() {
        EventHook hook = new EventHook();
        boolean use = false;
        if (OPTION_PRINT_RENDER_UPDATES.getAsBoolean()) {
            hook.renderUpdate = true;
            use = true;
        }
        if (use) {
            MinecraftForge.EVENT_BUS.register(hook);
        }
    }

    private static class EventHook {
        private boolean renderUpdate;

        @SubscribeEvent
        public void worldLoadEvent(WorldEvent.Load load) {
            load.world.addWorldAccess(new WorldListener(this));
        }
    }

    private static class WorldListener implements IWorldAccess {
        private final boolean renderUpdate;

        public WorldListener(EventHook hook) {
            this.renderUpdate = hook.renderUpdate;
        }

        @Override
        public void markBlockForUpdate(BlockPos pos) {}

        @Override
        public void notifyLightSet(BlockPos pos) {}

        @Override
        public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
            if (renderUpdate) {
                StackTraceElement[] elements = new Throwable().getStackTrace();
                String[] bc = new String[elements.length];
                int bcIndex = 0;
                for (int i = 1; i < elements.length; i++) {
                    StackTraceElement ste = elements[i];
                    if (!ste.getClassName().startsWith("buildcraft")) continue;
                    bc[bcIndex++] = ste.getClassName() + " # " + ste.getMethodName() + " : " + ste.getLineNumber();
                }
                if (bcIndex > 0) {
                    BCLog.logger.info("markBlockRangeForRenderUpdate(" + x1 + ", " + y1 + ", " + z1 + ", " + x2 + ", " + y2 + ", " + z2 + ")");
                    for (int i = 0; i < bcIndex; i++) {
                        BCLog.logger.info("  at " + bc[i]);
                    }
                }
            }
        }

        @Override
        public void playSound(String soundName, double x, double y, double z, float volume, float pitch) {}

        @Override
        public void playSoundToNearExcept(EntityPlayer except, String soundName, double x, double y, double z, float volume, float pitch) {}

        @Override
        public void spawnParticle(int particleID, boolean ignoreRange, double xCoord, double yCoord, double zCoord, double xOffset, double yOffset, double zOffset, int... parameters) {}

        @Override
        public void onEntityAdded(Entity entityIn) {}

        @Override
        public void onEntityRemoved(Entity entityIn) {}

        @Override
        public void playRecord(String recordName, BlockPos blockPosIn) {}

        @Override
        public void broadcastSound(int soundID, BlockPos pos, int data) {}

        @Override
        public void playAuxSFX(EntityPlayer player, int sfxType, BlockPos blockPosIn, int data) {}

        @Override
        public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {}
    }
}
