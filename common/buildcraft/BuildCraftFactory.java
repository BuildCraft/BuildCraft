/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraft.util.RegistrySimple;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
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
import buildcraft.api.core.BCLog;
import buildcraft.core.BCRegistry;
import buildcraft.core.CompatHooks;
import buildcraft.core.DefaultProps;
import buildcraft.core.InterModComms;
import buildcraft.core.builders.schematics.SchematicFree;
import buildcraft.core.config.ConfigManager;
import buildcraft.core.lib.network.base.ChannelHandler;
import buildcraft.core.lib.network.base.PacketHandler;
import buildcraft.factory.*;
import buildcraft.factory.render.ChuteRenderModel;
import buildcraft.factory.schematics.SchematicAutoWorkbench;
import buildcraft.factory.schematics.SchematicPump;
import buildcraft.factory.schematics.SchematicRefinery;
import buildcraft.factory.schematics.SchematicTileIgnoreState;

@Mod(name = "BuildCraft Factory", version = DefaultProps.VERSION, useMetadata = false, modid = "BuildCraft|Factory",
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
    public static boolean pumpsNeedRealPower = false;
    public static PumpDimensionList pumpDimensionList;

    @Mod.EventHandler
    public void fmlPreInit(FMLPreInitializationEvent evt) {
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
        BuildCraftCore.mainConfigManager.register("general.pumpsNeedRealPower", false, "Do pumps need real (non-redstone) power?",
                ConfigManager.RestartRequirement.WORLD);

        reloadConfig(ConfigManager.RestartRequirement.GAME);

        miningWellBlock = (BlockMiningWell) CompatHooks.INSTANCE.getBlock(BlockMiningWell.class);
        if (BCRegistry.INSTANCE.registerBlock(miningWellBlock.setUnlocalizedName("miningWellBlock"), false)) {
            plainPipeBlock = new BlockPlainPipe();
            BCRegistry.INSTANCE.registerBlock(plainPipeBlock.setUnlocalizedName("plainPipeBlock"), true);
        }

        autoWorkbenchBlock = (BlockAutoWorkbench) CompatHooks.INSTANCE.getBlock(BlockAutoWorkbench.class);
        BCRegistry.INSTANCE.registerBlock(autoWorkbenchBlock.setUnlocalizedName("autoWorkbenchBlock"), false);

        tankBlock = (BlockTank) CompatHooks.INSTANCE.getBlock(BlockTank.class);
        BCRegistry.INSTANCE.registerBlock(tankBlock.setUnlocalizedName("tankBlock"), false);

        pumpBlock = (BlockPump) CompatHooks.INSTANCE.getBlock(BlockPump.class);
        BCRegistry.INSTANCE.registerBlock(pumpBlock.setUnlocalizedName("pumpBlock"), false);

        floodGateBlock = (BlockFloodGate) CompatHooks.INSTANCE.getBlock(BlockFloodGate.class);
        BCRegistry.INSTANCE.registerBlock(floodGateBlock.setUnlocalizedName("floodGateBlock"), false);

        refineryBlock = (BlockRefinery) CompatHooks.INSTANCE.getBlock(BlockRefinery.class);
        BCRegistry.INSTANCE.registerBlock(refineryBlock.setUnlocalizedName("refineryBlock"), false);

        chuteBlock = (BlockChute) CompatHooks.INSTANCE.getBlock(BlockChute.class);
        BCRegistry.INSTANCE.registerBlock(chuteBlock.setUnlocalizedName("blockChute"), false);

        FactoryProxy.proxy.initializeEntityRenders();

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static void loadRecipes() {
        if (miningWellBlock != null) {
            BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(miningWellBlock, 1), "ipi", "igi", "iPi", 'p', "dustRedstone", 'i', "ingotIron", 'g',
                    "gearIron", 'P', Items.iron_pickaxe);
        }

        if (pumpBlock != null) {
            BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(pumpBlock), "ipi", "igi", "TBT", 'p', "dustRedstone", 'i', "ingotIron", 'T',
                    tankBlock, 'g', "gearIron", 'B', Items.bucket);
        }

        if (autoWorkbenchBlock != null) {
            BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(autoWorkbenchBlock), "gwg", 'w', "craftingTableWood", 'g', "gearStone");

            BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(autoWorkbenchBlock), "g", "w", "g", 'w', "craftingTableWood", 'g', "gearStone");
        }

        if (tankBlock != null) {
            BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(tankBlock), "ggg", "g g", "ggg", 'g', "blockGlass");
        }

        if (refineryBlock != null) {
            BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(refineryBlock), "RTR", "TGT", 'T', tankBlock != null ? tankBlock : "blockGlass", 'G',
                    "gearDiamond", 'R', Blocks.redstone_torch);
        }

        if (chuteBlock != null) {
            BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(chuteBlock), "ICI", " G ", 'I', "ingotIron", 'C', "chestWood", 'G', "gearStone");

            BCRegistry.INSTANCE.addShapelessRecipe(new ItemStack(chuteBlock), Blocks.hopper, "gearStone");
        }

        if (floodGateBlock != null) {
            BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(floodGateBlock), "IGI", "FTF", "IFI", 'I', "ingotIron", 'T', tankBlock != null
                ? tankBlock : "blockGlass", 'G', "gearIron", 'F', new ItemStack(Blocks.iron_bars));
        }

    }

    @Mod.EventHandler
    public void fmlInit(FMLInitializationEvent evt) {
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new FactoryGuiHandler());

        BCRegistry.INSTANCE.registerTileEntity(TileMiningWell.class, "buildcraft.factory.MiningWell", "MiningWell");
        BCRegistry.INSTANCE.registerTileEntity(TileAutoWorkbench.class, "buildcraft.factory.AutoWorkbench", "AutoWorkbench");
        BCRegistry.INSTANCE.registerTileEntity(TilePump.class, "buildcraft.factory.Pump", "net.minecraft.src.buildcraft.factory.TilePump");
        BCRegistry.INSTANCE.registerTileEntity(TileFloodGate.class, "buildcraft.factory.FloodGate",
                "net.minecraft.src.buildcraft.factory.TileFloodGate");
        BCRegistry.INSTANCE.registerTileEntity(TileTank.class, "buildcraft.factory.Tank", "net.minecraft.src.buildcraft.factory.TileTank");
        BCRegistry.INSTANCE.registerTileEntity(TileRefinery.class, "buildcraft.factory.Refinery", "net.minecraft.src.buildcraft.factory.Refinery");
        BCRegistry.INSTANCE.registerTileEntity(TileChute.class, "buildcraft.factory.Chute", "net.minecraft.src.buildcraft.factory.TileHopper");

        FactoryProxy.proxy.fmlInit();

        BuilderAPI.schematicRegistry.registerSchematicBlock(refineryBlock, SchematicRefinery.class);
        BuilderAPI.schematicRegistry.registerSchematicBlock(tankBlock, SchematicTileIgnoreState.class);
        BuilderAPI.schematicRegistry.registerSchematicBlock(pumpBlock, SchematicPump.class);
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

    public void reloadConfig(ConfigManager.RestartRequirement restartType) {
        if (restartType == ConfigManager.RestartRequirement.GAME) {
            reloadConfig(ConfigManager.RestartRequirement.WORLD);
        } else if (restartType == ConfigManager.RestartRequirement.WORLD) {
            reloadConfig(ConfigManager.RestartRequirement.NONE);
        } else {
            miningDepth = BuildCraftCore.mainConfigManager.get("general.miningDepth").getInt();
            pumpsNeedRealPower = BuildCraftCore.mainConfigManager.get("general.pumpsNeedRealPower").getBoolean();
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
        for (ModelResourceLocation entry : ((RegistrySimple<ModelResourceLocation, IBakedModel>) event.modelRegistry).getKeys()) {
            String str = entry.toString();
            if (str.contains("buildcraftfactory")) {
                BCLog.logger.info(str);
            }
        }

        IBakedModel model = event.modelRegistry.getObject(mrl);
        if (model != null) {
            event.modelRegistry.putObject(mrl, ChuteRenderModel.create(model));
        }
    }
}
