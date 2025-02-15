/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders;

import buildcraft.api.BCModules;
import buildcraft.builders.client.render.RenderArchitectTables;
import buildcraft.builders.client.render.RenderQuarry;
import buildcraft.builders.snapshot.MessageSnapshotRequest;
import buildcraft.builders.snapshot.MessageSnapshotResponse;
import buildcraft.lib.client.render.DetachedRenderer;
import buildcraft.lib.net.MessageManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLLoader;

//public abstract class BCBuildersProxy implements IGuiHandler
public abstract class BCBuildersProxy {
    // @SidedProxy
    private static BCBuildersProxy proxy;

    public static BCBuildersProxy getProxy() {
        if (proxy == null) {
            switch (FMLLoader.getDist()) {
                case CLIENT:
                    proxy = new BCBuildersProxy.ClientProxy();
                    break;
                case DEDICATED_SERVER:
                    proxy = new BCBuildersProxy.ServerProxy();
                    break;
            }
        }
        return proxy;
    }

//    @Override
//    public Object getServerGuiElement(int id, Player player, Level world, int x, int y, int z)
//    {
//        TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
//        if (id == BCBuildersGuis.LIBRARY.ordinal())
//        {
//            if (tile instanceof TileElectronicLibrary)
//            {
//                TileElectronicLibrary electronicLibrary = (TileElectronicLibrary) tile;
//                return new ContainerElectronicLibrary(player, electronicLibrary);
//            }
//        }
//        if (id == BCBuildersGuis.BUILDER.ordinal())
//        {
//            if (tile instanceof TileBuilder)
//            {
//                TileBuilder builder = (TileBuilder) tile;
//                return new ContainerBuilder(player, builder);
//            }
//        }
//        if (id == BCBuildersGuis.FILLER.ordinal())
//        {
//            if (tile instanceof TileFiller)
//            {
//                TileFiller filler = (TileFiller) tile;
//                return new ContainerFiller(player, filler);
//            }
//        }
//        if (id == BCBuildersGuis.ARCHITECT.ordinal())
//        {
//            if (tile instanceof TileArchitectTable)
//            {
//                TileArchitectTable architectTable = (TileArchitectTable) tile;
//                return new ContainerArchitectTable(player, architectTable);
//            }
//        }
//        if (id == BCBuildersGuis.REPLACER.ordinal())
//        {
//            if (tile instanceof TileReplacer)
//            {
//                TileReplacer replacer = (TileReplacer) tile;
//                return new ContainerReplacer(player, replacer);
//            }
//        }
//        if (id == BCBuildersGuis.FILLER_PLANNER.ordinal())
//        {
//            return new ContainerFillerPlanner(player);
//        }
//        return null;
//    }

//    @Override
//    public Object getClientGuiElement(int ID, Player player, World world, int x, int y, int z)
//    {
//        return null;
//    }

    public void fmlPreInit() {
        MessageManager.registerMessageClass(BCModules.BUILDERS, MessageSnapshotRequest.class, MessageSnapshotRequest.HANDLER, Dist.DEDICATED_SERVER);
        MessageManager.registerMessageClass(BCModules.BUILDERS, MessageSnapshotResponse.class, Dist.CLIENT);
    }

    public void fmlInit() {
    }

    public void fmlPostInit() {
    }

    @SuppressWarnings("unused")
//    @OnlyIn(Dist.DEDICATED_SERVER)
    public static class ServerProxy extends BCBuildersProxy {
    }

    @SuppressWarnings("unused")
//    @OnlyIn(Dist.CLIENT)
    public static class ClientProxy extends BCBuildersProxy {
//        @Override
//        public Object getClientGuiElement(int id, Player player, World world, int x, int y, int z)
//        {
//            TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
//            if (id == BCBuildersGuis.LIBRARY.ordinal())
//            {
//                if (tile instanceof TileElectronicLibrary)
//                {
//                    TileElectronicLibrary library = (TileElectronicLibrary) tile;
//                    return new GuiElectronicLibrary(new ContainerElectronicLibrary(player, library));
//                }
//            }
//            if (id == BCBuildersGuis.BUILDER.ordinal())
//            {
//                if (tile instanceof TileBuilder)
//                {
//                    TileBuilder builder = (TileBuilder) tile;
//                    return new GuiBuilder(new ContainerBuilder(player, builder));
//                }
//            }
//            if (id == BCBuildersGuis.FILLER.ordinal())
//            {
//                if (tile instanceof TileFiller)
//                {
//                    TileFiller filler = (TileFiller) tile;
//                    return new GuiFiller(new ContainerFiller(player, filler));
//                }
//            }
//            if (id == BCBuildersGuis.ARCHITECT.ordinal())
//            {
//                if (tile instanceof TileArchitectTable)
//                {
//                    TileArchitectTable architectTable = (TileArchitectTable) tile;
//                    return new GuiArchitectTable(new ContainerArchitectTable(player, architectTable));
//                }
//            }
//            if (id == BCBuildersGuis.REPLACER.ordinal())
//            {
//                if (tile instanceof TileReplacer)
//                {
//                    TileReplacer replacer = (TileReplacer) tile;
//                    return new GuiReplacer(new ContainerReplacer(player, replacer));
//                }
//            }
//            if (id == BCBuildersGuis.FILLER_PLANNER.ordinal())
//            {
//                return new GuiFillerPlanner(new ContainerFillerPlanner(player));
//            }
//            return null;
//        }

        @Override
        public void fmlPreInit() {
            super.fmlPreInit();
            // Calen: NullPointerException: Cannot set config value without assigned Config object present
            // ...
            // at net.minecraftforge.common.ForgeConfigSpec$ConfigValue.set(ForgeConfigSpec.java:873)
            // at buildcraft.builders.BCBuildersProxy$ClientProxy.fmlPreInit(BCBuildersProxy.java:171)
            // ...
//            if (BCBuildersConfig.enableStencil) {
//                if (BCBuildersConfig.internalStencilCrashTest.get()) {
//                    BCLog.logger.warn("[builders.architect] Not enabling stencils because they have been force-disabled!");
//                } else {
//                    BCBuildersConfig.internalStencilCrashTest.set(true);
////                    BCCoreConfig.saveConfigs();
//                    // TODO Calen Framebuffer???
////                    Framebuffer framebuffer = Minecraft.getMinecraft().getFramebuffer();
////                    if (!framebuffer.isStencilEnabled()) {
////                        framebuffer.enableStencil();
////                    }
//                    BCBuildersConfig.internalStencilCrashTest.set(false);
////                    BCCoreConfig.saveConfigs();
//                }
//            }
            BCBuildersSprites.fmlPreInit();
            RenderQuarry.init();

            MessageManager.setHandler(MessageSnapshotResponse.class, MessageSnapshotResponse.HANDLER, Dist.CLIENT);
        }

        @Override
        public void fmlInit() {
            super.fmlInit();
            // Calen: moved to BCBuilders
//            ClientRegistry.bindTileEntitySpecialRenderer(TileArchitectTable.class, new RenderArchitectTable());
//            ClientRegistry.bindTileEntitySpecialRenderer(TileBuilder.class, new RenderBuilder());
//            ClientRegistry.bindTileEntitySpecialRenderer(TileFiller.class, new RenderFiller());
//            ClientRegistry.bindTileEntitySpecialRenderer(TileQuarry.class, new RenderQuarry());
            DetachedRenderer.INSTANCE.addRenderer(DetachedRenderer.RenderMatrixType.FROM_WORLD_ORIGIN, RenderArchitectTables.INSTANCE);
        }
    }
}
