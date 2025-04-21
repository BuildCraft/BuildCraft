package buildcraft.silicon;

import buildcraft.api.BCModules;
import buildcraft.api.core.BCLog;
import buildcraft.api.facades.FacadeAPI;
import buildcraft.api.imc.BcImcMessage;
import buildcraft.core.BCCore;
import buildcraft.lib.recipe.assembly.AssemblyRecipeRegistry;
import buildcraft.lib.recipe.assembly.AssemblyRecipeSerializer;
import buildcraft.lib.recipe.integration.IntegrationRecipeSerializer;
import buildcraft.lib.recipe.programming.ProgrammingRecipeSerializer;
import buildcraft.lib.registry.CreativeTabManager;
import buildcraft.lib.registry.CreativeTabManager.CreativeTabBC;
import buildcraft.lib.registry.RegistryConfig;
import buildcraft.lib.registry.TagManager;
import buildcraft.lib.registry.TagManager.EnumTagType;
import buildcraft.lib.registry.TagManager.TagEntry;
import buildcraft.silicon.client.SiliconItemModelPredicates;
import buildcraft.silicon.gate.GateVariant;
import buildcraft.silicon.plug.FacadeBlockStateInfo;
import buildcraft.silicon.plug.FacadeInstance;
import buildcraft.silicon.plug.FacadeStateManager;
import buildcraft.silicon.recipe.FacadeAssemblyRecipes;
import buildcraft.silicon.recipe.FacadeSwapRecipeSerializer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.*;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.function.Consumer;

//@formatter:off
//@Mod(
//        modid = BCSilicon.MODID,
//        name = "BuildCraft Silicon",
//        version = BCLib.VERSION,
//        dependencies = "required-after:buildcraftcore@[" + BCLib.VERSION + "];"
//                // Pluggable registration needs to happen *after* the transport registries have been set
//                + "after:buildcrafttransport"
//)
//@formatter:on
@Mod(BCSilicon.MODID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class BCSilicon {
    public static final String MODID = "buildcraftsilicon";

    // @Mod.Instance(MODID)
    public static BCSilicon INSTANCE = null;

    private static CreativeTabBC tabPlugs;
    private static CreativeTabBC tabFacades;

    public BCSilicon() {
        INSTANCE = this;
    }

    @SubscribeEvent
    public static void preInit(FMLConstructModEvent evt) {
        RegistryConfig.useOtherModConfigFor(MODID, BCCore.MODID);

        tabPlugs = CreativeTabManager.createTab("buildcraft.plugs");
        tabFacades = CreativeTabManager.createTab("buildcraft.facades");
        FacadeAPI.registry = FacadeStateManager.INSTANCE;

        BCSiliconConfig.preInit();
        BCSiliconBlocks.preInit();
        BCSiliconPlugs.preInit();
        BCSiliconItems.preInit();
        BCSiliconStatements.preInit();

//        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, BCSiliconProxy.getProxy());
//
        BCSiliconProxy.getProxy().fmlPreInit();
    }

    @SubscribeEvent
    public static void init(FMLCommonSetupEvent evt) {
        BCSiliconProxy.getProxy().fmlInit();
        FacadeStateManager.init();
    }

    @SubscribeEvent
    public static void postInit(FMLLoadCompleteEvent evt) {
        BCSiliconProxy.getProxy().fmlPostInit();
        if (BCSiliconItems.plugFacade != null) {
            FacadeBlockStateInfo state = FacadeStateManager.previewState;
            FacadeInstance inst = FacadeInstance.createSingle(state, false);
//            tabFacades.setItem(BCSiliconItems.plugFacade.createItemStack(inst));
            tabFacades.setItemStack(() -> BCSiliconItems.plugFacade.get().createItemStack(inst));
        }

        if (!BCModules.TRANSPORT.isLoaded()) {
//            tabPlugs.setItem(BCSiliconItems.plugGate);
            tabPlugs.setItem(BCSiliconItems.variantGateMap.get(new GateVariant(new CompoundNBT())));
        }
    }

    @SubscribeEvent
    public static void registerGui(RegistryEvent.Register<ContainerType<?>> event) {
        BCSiliconMenuTypes.registerAll(event);
    }

    @SubscribeEvent
//    public static void onImcEvent(IMCEvent imc)
    public static void onImcEvent(InterModProcessEvent imc) {

//        for (InterModComms.IMCMessage message : imc.getMessages())
        InterModComms.getMessages(MODID).forEach(message ->
        {
            Object inner = message.getMessageSupplier().get();
            if (inner instanceof BcImcMessage) {
                BcImcMessage bcImcMessage = (BcImcMessage) inner;
                FacadeStateManager.receiveInterModComms(message, bcImcMessage);
            } else {
                BCLog.logger.error("[silicon.imc] Unknown IMC message type: " + inner.getClass().getName());
            }
        });
    }

    @SubscribeEvent
    public static void registerRecipeSerializers(RegistryEvent.Register<IRecipeSerializer<?>> event) {
        IForgeRegistry<IRecipeSerializer<?>> registry = event.getRegistry();
        registry.register(AssemblyRecipeSerializer.INSTANCE);
        registry.register(FacadeSwapRecipeSerializer.INSTANCE);
        registry.register(ProgrammingRecipeSerializer.INSTANCE);
        registry.register(IntegrationRecipeSerializer.INSTANCE);

        AssemblyRecipeRegistry.FACADE_ASSEMBLY_RECIPE = FacadeAssemblyRecipes.INSTANCE;
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        RenderTypeLookup.setRenderLayer(BCSiliconBlocks.assemblyTable.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(BCSiliconBlocks.advancedCraftingTable.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(BCSiliconBlocks.integrationTable.get(), RenderType.cutout());
        // Calen: 1.12.2 not impl……
        RenderTypeLookup.setRenderLayer(BCSiliconBlocks.chargingTable.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(BCSiliconBlocks.programmingTable.get(), RenderType.translucent());

        SiliconItemModelPredicates.register(event);
    }

    private static final TagManager tagManager = new TagManager();

    static {
        startBatch();
        // Items
        registerTag("item.chipset.redstone").reg("chipset_redstone").locale("chipset_redstone");
//                .model("chipset_redstone/");
        registerTag("item.chipset.iron").reg("chipset_iron").locale("chipset_iron");
//                .model("chipset_redstone/");
        registerTag("item.chipset.gold").reg("chipset_gold").locale("chipset_gold");
//                .model("chipset_redstone/");
        registerTag("item.chipset.quartz").reg("chipset_quartz").locale("chipset_quartz");
//                .model("chipset_redstone/");
        registerTag("item.chipset.diamond").reg("chipset_diamond").locale("chipset_diamond");
//                .model("chipset_redstone/");
        registerTag("item.redstone_crystal").reg("redstone_crystal").locale("redstoneCrystal");
        registerTag("item.gate_copier").reg("gate_copier").locale("gateCopier");
//                .model("gatecopier_");
        registerTag("item.plug.gate").reg("plug_gate").locale("gate")
//                .model("pluggable/gate")
                .tab("buildcraft.plugs");
//                .oldReg("plug_gate");
        registerTag("item.plug.lens").reg("plug_lens").locale("lens")
//                .model("pluggable/lens")
                .tab("buildcraft.plugs");
//                .oldReg("plug_lens");
        registerTag("item.plug.pulsar").reg("plug_pulsar").locale("pulsar")
//                .model("plug_pulsar")
                .tab("buildcraft.plugs");
//                .oldReg("plug_pulsar");
        registerTag("item.plug.light_sensor").reg("plug_light_sensor").locale("light_sensor")
//                .model("plug_light_sensor")
                .tab("buildcraft.plugs");
//                .oldReg("plug_light_sensor");
        registerTag("item.plug.facade").reg("plug_facade").locale("Facade")
//                .model("plug_facade")
                .tab("buildcraft.facades");
//                .oldReg("plug_facade");
        // Item Blocks
        registerTag("item.block.laser").reg("laser").locale("laserBlock");
//                .model("laser");
        registerTag("item.block.assembly_table").reg("assembly_table").locale("assemblyTableBlock");
//                .model("assembly_table");
        registerTag("item.block.advanced_crafting_table").reg("advanced_crafting_table").locale("assemblyWorkbenchBlock");
//                .model("advanced_crafting_table");
        registerTag("item.block.integration_table").reg("integration_table").locale("integrationTableBlock");
//                .model("integration_table");
        registerTag("item.block.charging_table").reg("charging_table").locale("chargingTableBlock");
//                .model("charging_table");
        registerTag("item.block.programming_table").reg("programming_table").locale("programmingTableBlock");
//                .model("programming_table");
        // Blocks
//        registerTag("block.laser").reg("laser").oldReg("laserBlock").locale("laserBlock").model("laser");
        registerTag("block.laser").reg("laser").locale("laserBlock");
//        registerTag("block.assembly_table").reg("assembly_table").oldReg("assemblyTableBlock").locale("assemblyTableBlock").model("assembly_table");
        registerTag("block.assembly_table").reg("assembly_table").locale("assemblyTableBlock");
//        registerTag("block.advanced_crafting_table").reg("advanced_crafting_table").oldReg("advancedCraftingTableBlock").locale("assemblyWorkbenchBlock").model("advanced_crafting_table");
        registerTag("block.advanced_crafting_table").reg("advanced_crafting_table").locale("assemblyWorkbenchBlock");
//        registerTag("block.integration_table").reg("integration_table").oldReg("integrationTableBlock").locale("integrationTableBlock").model("integration_table");
        registerTag("block.integration_table").reg("integration_table").locale("integrationTableBlock");
//        registerTag("block.charging_table").reg("charging_table").oldReg("chargingTableBlock").locale("chargingTableBlock").model("charging_table");
        registerTag("block.charging_table").reg("charging_table").locale("chargingTableBlock");
//        registerTag("block.programming_table").reg("programming_table").oldReg("programmingTableBlock").locale("programmingTableBlock").model("programming_table");
        registerTag("block.programming_table").reg("programming_table").locale("programmingTableBlock");
        // Tiles
        registerTag("tile.laser").reg("laser");
        registerTag("tile.assembly_table").reg("assembly_table");
        registerTag("tile.advanced_crafting_table").reg("advanced_crafting_table");
        registerTag("tile.integration_table").reg("integration_table");
        registerTag("tile.charging_table").reg("charging_table");
        registerTag("tile.programming_table").reg("programming_table");

//        endBatch(TagManager.prependTags("buildcraftsilicon:", EnumTagType.REGISTRY_NAME, EnumTagType.MODEL_LOCATION)
        endBatch(TagManager.prependTags("buildcraftsilicon:", EnumTagType.REGISTRY_NAME)
                .andThen(TagManager.setTab("buildcraft.main"))
        );
    }


    private static TagEntry registerTag(String id) {
//        return TagManager.registerTag(id);
        return tagManager.registerTag(id);
    }

    private static void startBatch() {
//        TagManager.startBatch();
        tagManager.startBatch();
    }

    // Calen: the batch often causes Exception...
    private static void endBatch(Consumer<TagEntry> consumer) {
//        TagManager.endBatch(consumer);
        tagManager.endBatch(consumer);
    }
}
