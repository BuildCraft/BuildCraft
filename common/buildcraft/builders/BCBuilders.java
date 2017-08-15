/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

import javax.annotation.Nonnull;

import net.minecraft.block.BlockBanner;
import net.minecraft.block.BlockVine;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemBanner;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.schematics.SchematicBlockFactoryRegistry;
import buildcraft.api.schematics.SchematicEntityFactoryRegistry;

import buildcraft.lib.BCLib;
import buildcraft.lib.net.MessageManager;
import buildcraft.lib.registry.RegistryHelper;
import buildcraft.lib.registry.TagManager;
import buildcraft.lib.registry.TagManager.EnumTagType;
import buildcraft.lib.registry.TagManager.TagEntry;

import buildcraft.builders.addon.AddonFillingPlanner;
import buildcraft.builders.snapshot.GlobalSavedDataSnapshots;
import buildcraft.builders.snapshot.MessageSnapshotRequest;
import buildcraft.builders.snapshot.MessageSnapshotResponse;
import buildcraft.builders.snapshot.RulesLoader;
import buildcraft.builders.snapshot.SchematicBlockAir;
import buildcraft.builders.snapshot.SchematicBlockDefault;
import buildcraft.builders.snapshot.SchematicBlockFluid;
import buildcraft.builders.snapshot.SchematicEntityDefault;
import buildcraft.core.BCCore;
import buildcraft.core.marker.volume.AddonsRegistry;

//@formatter:off
@Mod(
    modid = BCBuilders.MODID,
    name = "BuildCraft Builders",
    version = BCLib.VERSION,
    dependencies = "required-after:buildcraftcore@[" + BCLib.VERSION + "]"
)
//@formatter:on
public class BCBuilders {
    public static final String MODID = "buildcraftbuilders";

    @Mod.Instance(MODID)
    public static BCBuilders INSTANCE = null;

    @Mod.EventHandler
    public static void preInit(FMLPreInitializationEvent evt) {
        RegistryHelper.useOtherModConfigFor(MODID, BCCore.MODID);

        BCBuildersRegistries.preInit();
        BCBuildersItems.preInit();
        BCBuildersBlocks.preInit();

        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, BCBuildersProxy.getProxy());
        AddonsRegistry.INSTANCE.register(new ResourceLocation("buildcraftbuilders", "filling_planner"), AddonFillingPlanner.class);

        SchematicBlockFactoryRegistry.registerFactory(
            "air",
            0,
            SchematicBlockAir::predicate,
            SchematicBlockAir::new
        );
        SchematicBlockFactoryRegistry.registerFactory(
            "default",
            100,
            SchematicBlockDefault::predicate,
            SchematicBlockDefault::new
        );
        SchematicBlockFactoryRegistry.registerFactory(
            "fluid",
            200,
            SchematicBlockFluid::predicate,
            SchematicBlockFluid::new
        );
        SchematicBlockFactoryRegistry.registerFactory(
            "banner",
            300,
            context -> context.block instanceof BlockBanner,
            new Supplier<SchematicBlockDefault>() {
                @Override
                public SchematicBlockDefault get() {
                    return new SchematicBlockDefault() {
                        @Nonnull
                        @Override
                        public List<ItemStack> computeRequiredItems() {
                            return Collections.singletonList(
                                ItemBanner.makeBanner(
                                    EnumDyeColor.byDyeDamage(tileNbt.getInteger("Base")),
                                    tileNbt.getTagList("Patterns", 10)
                                )
                            );
                        }
                    };
                }
            }
        );
        SchematicBlockFactoryRegistry.registerFactory(
            "vine",
            300,
            context -> context.block instanceof BlockVine,
            new Supplier<SchematicBlockDefault>() {
                @Override
                public SchematicBlockDefault get() {
                    return new SchematicBlockDefault() {
                        @Override
                        public boolean isReadyToBuild(World world, BlockPos blockPos) {
                            return super.isReadyToBuild(world, blockPos) &&
                                (world.getBlockState(blockPos.up()).getBlock() instanceof BlockVine ||
                                    StreamSupport.stream(EnumFacing.Plane.HORIZONTAL.spliterator(), false)
                                        .map(blockPos::offset)
                                        .map(world::getBlockState)
                                        .anyMatch(state -> state.isFullCube() && state.getMaterial().blocksMovement()));
                        }
                    };
                }
            }
        );

        SchematicEntityFactoryRegistry.registerFactory(
            "default",
            100,
            SchematicEntityDefault::predicate,
            SchematicEntityDefault::new
        );

        BCBuildersProxy.getProxy().fmlPreInit();

        MinecraftForge.EVENT_BUS.register(BCBuildersEventDist.INSTANCE);

        MessageManager.addMessageType(MessageSnapshotRequest.class, MessageSnapshotRequest.HANDLER, Side.SERVER);
        MessageManager.addMessageType(MessageSnapshotResponse.class, MessageSnapshotResponse.HANDLER, Side.CLIENT);
    }

    @Mod.EventHandler
    public static void init(FMLInitializationEvent evt) {
        BCBuildersProxy.getProxy().fmlInit();
        BCBuildersRegistries.init();
        BCBuildersRecipes.init();
    }

    @Mod.EventHandler
    public static void postInit(FMLPostInitializationEvent evt) {
        RulesLoader.loadAll();
    }

    @Mod.EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        GlobalSavedDataSnapshots.reInit(Side.SERVER);
    }

    static {
        startBatch();
        // Items
        registerTag("item.schematic.single").reg("schematic_single").locale("schematicSingle").model("schematic_single/");
        registerTag("item.snapshot").reg("snapshot").locale("snapshot").model("snapshot/");
        registerTag("item.filling_planner").reg("filling_planner").locale("fillingPlannerItem").model("filling_planner");
        // Item Blocks
        registerTag("item.block.architect").reg("architect").locale("architectBlock").model("architect");
        registerTag("item.block.builder").reg("builder").locale("builderBlock").model("builder");
        registerTag("item.block.filler").reg("filler").locale("fillerBlock").model("filler");
        registerTag("item.block.library").reg("library").locale("libraryBlock").model("library");
        registerTag("item.block.replacer").reg("replacer").locale("replacerBlock").model("replacer");
        registerTag("item.block.frame").reg("frame").locale("frameBlock").model("frame");
        registerTag("item.block.quarry").reg("quarry").locale("quarryBlock").model("quarry");
        // Blocks
        registerTag("block.architect").reg("architect").locale("architectBlock").model("architect");
        registerTag("block.builder").reg("builder").locale("builderBlock").model("builder");
        registerTag("block.filler").reg("filler").locale("fillerBlock").model("filler");
        registerTag("block.library").reg("library").locale("libraryBlock").model("library");
        registerTag("block.replacer").reg("replacer").locale("replacerBlock").model("replacer");
        registerTag("block.frame").reg("frame").locale("frameBlock").model("frame");
        registerTag("block.quarry").reg("quarry").locale("quarryBlock").model("quarry");
        // Tiles
        registerTag("tile.architect").reg("architect");
        registerTag("tile.builder").reg("builder");
        registerTag("tile.library").reg("library");
        registerTag("tile.replacer").reg("replacer");
        registerTag("tile.filler").reg("filler");
        registerTag("tile.quarry").reg("quarry");

        endBatch(TagManager.prependTags("buildcraftbuilders:", EnumTagType.REGISTRY_NAME, EnumTagType.MODEL_LOCATION).andThen(TagManager.setTab("buildcraft.main")));
    }

    private static TagEntry registerTag(String id) {
        return TagManager.registerTag(id);
    }

    private static void startBatch() {
        TagManager.startBatch();
    }

    private static void endBatch(Consumer<TagEntry> consumer) {
        TagManager.endBatch(consumer);
    }
}
