package ct.buildcraft.energy;


import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import ct.buildcraft.core.BCCore;
import ct.buildcraft.energy.tile.TileSpringOil;
import ct.buildcraft.lib.misc.AdvancementUtil;
import ct.buildcraft.lib.misc.FluidUtilBC;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@Mod(BCEnergy.MODID)
public class BCEnergy {
	public static final String MODID = "buildcraftenergy";
	static final Logger LOGGER = LogUtils.getLogger();

    private static final ResourceLocation ADVANCEMENT_FIND_OIL_SPOT = new ResourceLocation(MODID, "fine_riches");
    private static final int OIL_SPOT_CHECK_INTERVAL = 40;
    private static final int OIL_SPOT_CHECK_RADIUS_XZ = 12;
    private static final int OIL_SPOT_CHECK_RADIUS_Y = 8;
	
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);
    
    
    public BCEnergy() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        //TEST_CODE_START
        try {
//			test();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			ct.buildcraft.api.core.BCLog.logger.warn("Failed to initialise BuildCraft Energy integration", e);
		} 
        //TEST_CODE_END
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::gatherData);
        BCEnergyFluids.registry(modEventBus);
        BCEnergyBlocks.init(modEventBus);
        BCEnergyGuis.init();
        BCEnergyWorldGen.preInit(modEventBus);
        BCEnergyConfig.preInit();
        ModLoadingContext.get().registerConfig(Type.COMMON, BCEnergyConfig.config);
        MinecraftForge.EVENT_BUS.register(this);
        // Register the Deferred Register to the mod event bus so blocks get registered
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        MENUS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in

 //       MinecraftForge.EVENT_BUS.register(EntityBlockPump::new);
    }
    private void commonSetup(final FMLCommonSetupEvent event)
    {
    	BCCore.tabFluids.setItem(BCEnergyFluids.OIL_BUCKET.get(0).get());
    	BCEnergyFluids.init();
    	BCEnergyRecipes.init();
    	BCEnergyConfig.reloadConfig(MODID);
//    	event.enqueueWork(() -> 
//    	if(BCEnergyWorldGen.isTerraBlenderLoaded) {
//	    	Regions.register(new BCOverWorldRegion(40));
//  	  	SurfaceRuleManager.addSurfaceRules(SurfaceRuleManager.RuleCategory.OVERWORLD, MODID, BCSurfaceRuleData.oilDesertRule());
//    	});
    	//event.enqueueWork(BCEnergyWorldGen::registryFeature);
        
    }
    public void gatherData(GatherDataEvent event) {
        event.getGenerator().addProvider(event.includeServer(),
            new BCEnergyRecipes.BCEnergyRecipeProvider(event.getGenerator()));
        event.getGenerator().addProvider(event.includeClient(), 
        	new BCEnergyProvider.BlockModel(event.getGenerator(), event.getExistingFileHelper()));
        event.getGenerator().addProvider(event.includeClient(), 
            new BCEnergyProvider.BlockState(event.getGenerator(), event.getExistingFileHelper()));
        event.getGenerator().addProvider(event.includeClient(), 
                new BCEnergyProvider.ItemModel(event.getGenerator(), event.getExistingFileHelper()));
    }
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level.isClientSide) {
            return;
        }
        if (!(event.player instanceof ServerPlayer player)) {
            return;
        }
        if (player.tickCount % OIL_SPOT_CHECK_INTERVAL != 0) {
            return;
        }
        if (hasAdvancement(player, ADVANCEMENT_FIND_OIL_SPOT)) {
            return;
        }
        if (isNearOilSpot(player)) {
            AdvancementUtil.unlockAdvancement(player, ADVANCEMENT_FIND_OIL_SPOT);
        }
    }

    private static boolean hasAdvancement(ServerPlayer player, ResourceLocation advancementName) {
        Advancement advancement = player.getServer().getAdvancements().getAdvancement(advancementName);
        return advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone();
    }

    private static boolean isNearOilSpot(ServerPlayer player) {
        Level level = player.level;
        BlockPos center = player.blockPosition();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int y = -OIL_SPOT_CHECK_RADIUS_Y; y <= OIL_SPOT_CHECK_RADIUS_Y; y++) {
            for (int x = -OIL_SPOT_CHECK_RADIUS_XZ; x <= OIL_SPOT_CHECK_RADIUS_XZ; x++) {
                for (int z = -OIL_SPOT_CHECK_RADIUS_XZ; z <= OIL_SPOT_CHECK_RADIUS_XZ; z++) {
                    pos.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                    if (isOilSpotBlock(level, pos)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean isOilSpotBlock(Level level, BlockPos pos) {
        if (!level.isLoaded(pos)) {
            return false;
        }

        Fluid fluid = level.getFluidState(pos).getType();
        if (fluid != Fluids.EMPTY && FluidUtilBC.areFluidsEqual(fluid, BCEnergyFluids.crudeOil[0])) {
            return true;
        }

        if (level.getBlockState(pos).hasBlockEntity()) {
            BlockEntity tile = level.getBlockEntity(pos);
            return tile instanceof TileSpringOil;
        }
        return false;
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
    }
    







}
