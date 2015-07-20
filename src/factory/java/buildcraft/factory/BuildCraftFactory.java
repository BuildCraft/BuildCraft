/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.factory;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLMissingMappingsEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.blueprints.BuilderAPI;
import buildcraft.api.blueprints.SchematicTile;
import buildcraft.core.BuildCraftCore;
import buildcraft.core.BuildCraftMod;
import buildcraft.core.CompatHooks;
import buildcraft.core.DefaultProps;
import buildcraft.core.InterModComms;
import buildcraft.core.Version;
import buildcraft.core.builders.schematics.SchematicFree;
import buildcraft.core.config.ConfigManager;
import buildcraft.core.lib.network.ChannelHandler;
import buildcraft.core.lib.network.PacketHandler;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.factory.block.BlockAutoWorkbench;
import buildcraft.factory.block.BlockChute;
import buildcraft.factory.block.BlockFloodGate;
import buildcraft.factory.block.BlockMiningWell;
import buildcraft.factory.block.BlockPlainPipe;
import buildcraft.factory.block.BlockPump;
import buildcraft.factory.block.BlockRefinery;
import buildcraft.factory.block.BlockTank;
import buildcraft.factory.render.ChuteRenderModel;
import buildcraft.factory.schematics.SchematicAutoWorkbench;
import buildcraft.factory.schematics.SchematicPump;
import buildcraft.factory.schematics.SchematicRefinery;
import buildcraft.factory.schematics.SchematicTileIgnoreState;
import buildcraft.factory.tile.TileAutoWorkbench;
import buildcraft.factory.tile.TileChute;
import buildcraft.factory.tile.TileFloodGate;
import buildcraft.factory.tile.TileMiningWell;
import buildcraft.factory.tile.TilePump;
import buildcraft.factory.tile.TileRefinery;
import buildcraft.factory.tile.TileTank;

@Mod(name = "BuildCraft Factory", version = Version.VERSION, useMetadata = false, modid = "BuildCraft|Factory",
        dependencies = DefaultProps.DEPENDENCY_CORE)
public class BuildCraftFactory extends BuildCraftMod {

    @Mod.Instance("BuildCraft|Factory")
    public static BuildCraftFactory instance;

    public static BlockMiningWell miningWellBlock;
    public static BlockAutoWorkbench autoWorkbenchBlock;
    public static BlockPlainPipe plainPipeBlock;
    public static BlockPump pumpBlock;
    public static BlockFloodGate floodGateBlock;
    public static BlockTank tankBlock;
    public static BlockRefinery refineryBlock;
    public static BlockChute chuteBlock;

    public static Achievement aLotOfCraftingAchievement;
    public static Achievement straightDownAchievement;
    public static Achievement refineAndRedefineAchievement;

    public static int miningDepth = 256;
    public static PumpDimensionList pumpDimensionList;

    @Mod.EventHandler
    public void load(FMLInitializationEvent evt) {
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new FactoryGuiHandler());

        // EntityRegistry.registerModEntity(EntityMechanicalArm.class, "bcMechanicalArm", EntityIds.MECHANICAL_ARM,
        // instance, 50, 1, true);

        CoreProxy.proxy.registerTileEntity(TileMiningWell.class, "buildcraft.factory.MiningWell", "MiningWell");
        CoreProxy.proxy.registerTileEntity(TileAutoWorkbench.class, "buildcraft.factory.AutoWorkbench", "AutoWorkbench");
        CoreProxy.proxy.registerTileEntity(TilePump.class, "buildcraft.factory.Pump", "net.minecraft.src.buildcraft.factory.TilePump");
        CoreProxy.proxy.registerTileEntity(TileFloodGate.class, "buildcraft.factory.FloodGate", "net.minecraft.src.buildcraft.factory.TileFloodGate");
        CoreProxy.proxy.registerTileEntity(TileTank.class, "buildcraft.factory.Tank", "net.minecraft.src.buildcraft.factory.TileTank");
        CoreProxy.proxy.registerTileEntity(TileRefinery.class, "buildcraft.factory.Refinery", "net.minecraft.src.buildcraft.factory.Refinery");
        CoreProxy.proxy.registerTileEntity(TileChute.class, "buildcraft.factory.Chute", "net.minecraft.src.buildcraft.factory.TileHopper");

        FactoryProxy.proxy.initializeTileEntities();

        BuilderAPI.schematicRegistry.registerSchematicBlock(refineryBlock, SchematicRefinery.class);
        BuilderAPI.schematicRegistry.registerSchematicBlock(tankBlock, SchematicTileIgnoreState.class);
        BuilderAPI.schematicRegistry.registerSchematicBlock(pumpBlock, SchematicPump.class);
        BuilderAPI.schematicRegistry.registerSchematicBlock(miningWellBlock, SchematicTileIgnoreState.class);
        BuilderAPI.schematicRegistry.registerSchematicBlock(floodGateBlock, SchematicTileIgnoreState.class);
        BuilderAPI.schematicRegistry.registerSchematicBlock(autoWorkbenchBlock, SchematicAutoWorkbench.class);
        BuilderAPI.schematicRegistry.registerSchematicBlock(chuteBlock, SchematicTile.class);
        BuilderAPI.schematicRegistry.registerSchematicBlock(plainPipeBlock, SchematicFree.class);

        aLotOfCraftingAchievement = BuildCraftCore.achievementManager.registerAchievement(new Achievement("achievement.aLotOfCrafting",
                "aLotOfCraftingAchievement", 1, 2, autoWorkbenchBlock, BuildCraftCore.woodenGearAchievement));
        straightDownAchievement = BuildCraftCore.achievementManager.registerAchievement(new Achievement("achievement.straightDown",
                "straightDownAchievement", 5, 2, miningWellBlock, BuildCraftCore.ironGearAchievement));
        refineAndRedefineAchievement = BuildCraftCore.achievementManager.registerAchievement(new Achievement("achievement.refineAndRedefine",
                "refineAndRedefineAchievement", 10, 0, refineryBlock, BuildCraftCore.diamondGearAchievement));

        if (BuildCraftCore.loadDefaultRecipes) {
            loadRecipes();
        }
    }

    @Mod.EventHandler
    public void initialize(FMLPreInitializationEvent evt) {
        channels = NetworkRegistry.INSTANCE.newChannel(DefaultProps.NET_CHANNEL_NAME + "-FACTORY", new ChannelHandler(), new PacketHandler());

        String plc = "Allows admins to whitelist or blacklist pumping of specific fluids in specific dimensions.\n"
            + "Eg. \"-/-1/Lava\" will disable lava in the nether. \"-/*/Lava\" will disable lava in any dimension. \"+/0/*\" will enable any fluid in the overworld.\n"
            + "Entries are comma seperated, banned fluids have precedence over allowed ones."
            + "Default is \"+/*/*,+/-1/Lava\" - the second redundant entry (\"+/-1/lava\") is there to show the format.";

        BuildCraftCore.mainConfigManager.register("general.miningDepth", 256, "Should the mining well only be usable once after placing?",
                ConfigManager.RestartRequirement.NONE);

        BuildCraftCore.mainConfigManager.get("general.miningDepth").setMinValue(2).setMaxValue(256);
        BuildCraftCore.mainConfigManager.register("general.pumpDimensionControl", DefaultProps.PUMP_DIMENSION_LIST, plc,
                ConfigManager.RestartRequirement.NONE);

        reloadConfig(ConfigManager.RestartRequirement.GAME);

        miningWellBlock = (BlockMiningWell) CompatHooks.INSTANCE.getBlock(BlockMiningWell.class);
        CoreProxy.proxy.registerBlock(miningWellBlock.setUnlocalizedName("miningWellBlock"));

        plainPipeBlock = new BlockPlainPipe();
        CoreProxy.proxy.registerBlock(plainPipeBlock.setUnlocalizedName("plainPipeBlock"));

        autoWorkbenchBlock = (BlockAutoWorkbench) CompatHooks.INSTANCE.getBlock(BlockAutoWorkbench.class);
        CoreProxy.proxy.registerBlock(autoWorkbenchBlock.setUnlocalizedName("autoWorkbenchBlock"));

        tankBlock = (BlockTank) CompatHooks.INSTANCE.getBlock(BlockTank.class);
        CoreProxy.proxy.registerBlock(tankBlock.setUnlocalizedName("tankBlock"));

        pumpBlock = (BlockPump) CompatHooks.INSTANCE.getBlock(BlockPump.class);
        CoreProxy.proxy.registerBlock(pumpBlock.setUnlocalizedName("pumpBlock"));

        floodGateBlock = (BlockFloodGate) CompatHooks.INSTANCE.getBlock(BlockFloodGate.class);
        CoreProxy.proxy.registerBlock(floodGateBlock.setUnlocalizedName("floodGateBlock"));

        refineryBlock = (BlockRefinery) CompatHooks.INSTANCE.getBlock(BlockRefinery.class);
        CoreProxy.proxy.registerBlock(refineryBlock.setUnlocalizedName("refineryBlock"));

        chuteBlock = (BlockChute) CompatHooks.INSTANCE.getBlock(BlockChute.class);
        CoreProxy.proxy.registerBlock(chuteBlock.setUnlocalizedName("chuteBlock"));

        FactoryProxy.proxy.initializeEntityRenders();

        FMLCommonHandler.instance().bus().register(this);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static void loadRecipes() {
        if (miningWellBlock != null) {
            CoreProxy.proxy.addCraftingRecipe(new ItemStack(miningWellBlock, 1), "ipi", "igi", "iPi", 'p', "dustRedstone", 'i', "ingotIron", 'g',
                    "gearIron", 'P', Items.iron_pickaxe);
        }

        if (pumpBlock != null) {
            CoreProxy.proxy.addCraftingRecipe(new ItemStack(pumpBlock), "ipi", "igi", "TBT", 'p', "dustRedstone", 'i', "ingotIron", 'T', tankBlock,
                    'g', "gearIron", 'B', Items.bucket);
        }

        if (autoWorkbenchBlock != null) {
            CoreProxy.proxy.addCraftingRecipe(new ItemStack(autoWorkbenchBlock), "gwg", 'w', Blocks.crafting_table, 'g', "gearStone");

            CoreProxy.proxy.addCraftingRecipe(new ItemStack(autoWorkbenchBlock), "g", "w", "g", 'w', Blocks.crafting_table, 'g', "gearStone");
        }

        if (tankBlock != null) {
            CoreProxy.proxy.addCraftingRecipe(new ItemStack(tankBlock), "ggg", "g g", "ggg", 'g', "blockGlass");
        }

        if (refineryBlock != null) {
            CoreProxy.proxy.addCraftingRecipe(new ItemStack(refineryBlock), "RTR", "TGT", 'T', tankBlock != null ? tankBlock : "blockGlass", 'G',
                    "gearDiamond", 'R', Blocks.redstone_torch);
        }

        if (chuteBlock != null) {
            CoreProxy.proxy.addCraftingRecipe(new ItemStack(chuteBlock), "ICI", " G ", 'I', "ingotIron", 'C', Blocks.chest, 'G', "gearStone");

            CoreProxy.proxy.addShapelessRecipe(new ItemStack(chuteBlock), Blocks.hopper, "gearStone");
        }

        if (floodGateBlock != null) {
            CoreProxy.proxy.addCraftingRecipe(new ItemStack(floodGateBlock), "IGI", "FTF", "IFI", 'I', "ingotIron", 'T', tankBlock != null ? tankBlock
                : "blockGlass", 'G', "gearIron", 'F', new ItemStack(Blocks.iron_bars));
        }
    }

    public void reloadConfig(ConfigManager.RestartRequirement restartType) {
        if (restartType == ConfigManager.RestartRequirement.GAME) {
            reloadConfig(ConfigManager.RestartRequirement.WORLD);
        } else if (restartType == ConfigManager.RestartRequirement.WORLD) {
            reloadConfig(ConfigManager.RestartRequirement.NONE);
        } else {
            miningDepth = BuildCraftCore.mainConfigManager.get("general.miningDepth").getInt();
            pumpDimensionList = new PumpDimensionList(BuildCraftCore.mainConfigManager.get("general.pumpDimensionControl").getString());

            if (BuildCraftCore.mainConfiguration.hasChanged()) {
                BuildCraftCore.mainConfiguration.save();
            }
        }
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if ("BuildCraft|Core".equals(event.modID)) {
            reloadConfig(event.isWorldRunning ? ConfigManager.RestartRequirement.NONE : ConfigManager.RestartRequirement.WORLD);
        }
    }

    @Mod.EventHandler
    public void processIMCRequests(FMLInterModComms.IMCEvent event) {
        InterModComms.processIMC(event);
    }

    @Mod.EventHandler
    public void whiteListAppliedEnergetics(FMLInitializationEvent event) {
        // FMLInterModComms.sendMessage("appliedenergistics2", "whitelist-spatial",
        // TileMiningWell.class.getCanonicalName());
        FMLInterModComms.sendMessage("appliedenergistics2", "whitelist-spatial", TileAutoWorkbench.class.getCanonicalName());
        // FMLInterModComms.sendMessage("appliedenergistics2", "whitelist-spatial",
        // TilePump.class.getCanonicalName());
        FMLInterModComms.sendMessage("appliedenergistics2", "whitelist-spatial", TileFloodGate.class.getCanonicalName());
        FMLInterModComms.sendMessage("appliedenergistics2", "whitelist-spatial", TileTank.class.getCanonicalName());
        FMLInterModComms.sendMessage("appliedenergistics2", "whitelist-spatial", TileRefinery.class.getCanonicalName());
        FMLInterModComms.sendMessage("appliedenergistics2", "whitelist-spatial", TileChute.class.getCanonicalName());
    }

    @Mod.EventHandler
    public void remap(FMLMissingMappingsEvent event) {
        for (FMLMissingMappingsEvent.MissingMapping mapping : event.get()) {
            if (mapping.name.equalsIgnoreCase("BuildCraft|Factory:machineBlock") || mapping.name.equalsIgnoreCase("BuildCraft|Factory:quarryBlock")) {
                if (Loader.isModLoaded("BuildCraft|Builders")) {
                    if (mapping.type == GameRegistry.Type.BLOCK) {
                        mapping.remap(Block.getBlockFromName("BuildCraft|Builders:quarryBlock"));
                    } else if (mapping.type == GameRegistry.Type.ITEM) {
                        mapping.remap(Item.getItemFromBlock(Block.getBlockFromName("BuildCraft|Builders:quarryBlock")));
                    }
                } else {
                    mapping.warn();
                }
            } else if (mapping.name.equalsIgnoreCase("BuildCraft|Factory:frameBlock")) {
                if (Loader.isModLoaded("BuildCraft|Builders")) {
                    if (mapping.type == GameRegistry.Type.BLOCK) {
                        mapping.remap(Block.getBlockFromName("BuildCraft|Builders:frameBlock"));
                    } else if (mapping.type == GameRegistry.Type.ITEM) {
                        mapping.remap(Item.getItemFromBlock(Block.getBlockFromName("BuildCraft|Builders:frameBlock")));
                    }
                } else {
                    mapping.ignore();
                }
            } else if (mapping.name.equalsIgnoreCase("BuildCraft|Factory:hopperBlock")) {
                mapping.remap(Block.getBlockFromName("BuildCraft|Factory:chuteBlock"));
            }
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void loadTextures(TextureStitchEvent.Pre evt) {
        TextureMap terrainTextures = evt.map;
        FactoryProxyClient.pumpTexture = terrainTextures.registerSprite(new ResourceLocation("buildcraftfactory:blocks/pump/tube"));
        ChuteRenderModel.sideTexture = terrainTextures.registerSprite(new ResourceLocation("buildcraftfactory:blocks/chute/side"));
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void registerModels(ModelBakeEvent event) {
        ModelResourceLocation mrl = new ModelResourceLocation("buildcraftfactory:chuteBlock");
        IBakedModel model = (IBakedModel) event.modelRegistry.getObject(mrl);
        event.modelRegistry.putObject(mrl, ChuteRenderModel.create(model));
    }
}
