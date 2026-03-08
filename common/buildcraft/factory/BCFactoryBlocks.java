package buildcraft.factory;

import buildcraft.factory.block.*;
import buildcraft.factory.tile.*;
import buildcraft.lib.block.BlockPropertiesCreator;
import buildcraft.lib.registry.RegistrationHelper;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;

public class BCFactoryBlocks {
    private static final RegistrationHelper HELPER = new RegistrationHelper(BCFactory.MODID);

    public static RegistryObject<BlockAutoWorkbenchItems> autoWorkbenchItems;
    // public static RegistryObject<BlockAutoWorkbenchFluids> autoWorkbenchFluids;
    public static RegistryObject<BlockMiningWell> miningWell;
    public static RegistryObject<BlockPump> pump;
    public static RegistryObject<BlockTube> tube;
    public static RegistryObject<BlockFloodGate> floodGate;
    public static RegistryObject<BlockTank> tank;
    public static RegistryObject<BlockChute> chute;
    public static RegistryObject<BlockDistiller> distiller;
    public static RegistryObject<BlockHeatExchange> heatExchange;

    public static RegistryObject<BlockWaterGel> waterGel;
//    public static RegistryObject<BlockPlastic> plastic;

    public static RegistryObject<TileEntityType<TileAutoWorkbenchItems>> autoWorkbenchItemsTile;
    public static RegistryObject<TileEntityType<TileAutoWorkbenchFluids>> autoWorkbenchFluidsTile;
    public static RegistryObject<TileEntityType<TileMiningWell>> miningWellTile;
    public static RegistryObject<TileEntityType<TilePump>> pumpTile;
    public static RegistryObject<TileEntityType<TileFloodGate>> floodGateTile;
    public static RegistryObject<TileEntityType<TileTank>> tankTile;
    public static RegistryObject<TileEntityType<TileChute>> chuteTile;
    public static RegistryObject<TileEntityType<TileDistiller_BC8>> distillerTile;
    public static RegistryObject<TileEntityType<TileHeatExchange>> heatExchangeTile;

    static {
        autoWorkbenchItems = HELPER.addBlockAndItem("block.autoworkbench.item", BlockPropertiesCreator.createDefaultProperties(Material.STONE), BlockAutoWorkbenchItems::new);
//        autoWorkbenchFluids = HELPER.addBlockAndItem("block.autoworkbench.fluid", BlockPropertiesCreater.createDefaultProperties(Material.STONE), BlockAutoWorkbenchFluids::new);
        miningWell = HELPER.addBlockAndItem("block.mining_well", BlockPropertiesCreator.createDefaultProperties(Material.METAL), BlockMiningWell::new);
        pump = HELPER.addBlockAndItem("block.pump", BlockPropertiesCreator.createDefaultProperties(Material.METAL), BlockPump::new);
        tube = HELPER.addBlock(
                "block.tube",
                AbstractBlock.Properties.of(Material.METAL)
                        .strength(-1.0F, 3600000.0F) // setBlockUnbreakable()
                        .noOcclusion()
                        .noDrops()
                        .isSuffocating((state, world, pos) -> false)
                ,
                BlockTube::new
        );
        floodGate = HELPER.addBlockAndItem("block.flood_gate", BlockPropertiesCreator.createDefaultProperties(Material.METAL), BlockFloodGate::new);
        tank = HELPER.addBlockAndItem(
                "block.tank",
                BlockPropertiesCreator.createDefaultProperties(Material.METAL)
                        .sound(SoundType.GLASS)
                        .lightLevel((state) -> 0)
                        .noOcclusion()
                        .isSuffocating((state, world, pos) -> false)
                ,
                BlockTank::new
        );
        chute = HELPER.addBlockAndItem("block.chute",
                BlockPropertiesCreator.createDefaultProperties(Material.METAL)
                        .lightLevel((state) -> 0)
                        .noOcclusion()
                        .isSuffocating((state, world, pos) -> false)
                ,
                BlockChute::new);
        distiller = HELPER.addBlockAndItem(
                "block.distiller",
                BlockPropertiesCreator.createDefaultProperties(Material.METAL)
                        .sound(SoundType.GLASS)
                        .lightLevel((state) -> 0)
                        .noOcclusion()
                        .isSuffocating((state, world, pos) -> false)
                ,
                BlockDistiller::new
        );
        heatExchange = HELPER.addBlockAndItem(
                "block.heat_exchange",
                BlockPropertiesCreator.createDefaultProperties(Material.METAL)
                        .sound(SoundType.GLASS)
                        .noOcclusion()
                        .isViewBlocking((state, world, pos) -> false)
                        .isSuffocating((state, world, pos) -> false)
                ,
                BlockHeatExchange::new
        );
        waterGel = HELPER.addBlock(
                "block.water_gel",
                BlockPropertiesCreator.createDefaultProperties(Material.CLAY)
                        .sound(SoundType.SLIME_BLOCK)
                        .randomTicks()
                ,
                BlockWaterGel::new
        );
//        plastic = HELPER.addBlockAndItem("block.plastic", BlockPropertiesCreator.createDefaultProperties(Material.METAL), BlockPlastic::new, ItemPlastic::new);

        autoWorkbenchItemsTile = HELPER.registerTile("tile.autoworkbench.item", TileAutoWorkbenchItems::new, autoWorkbenchItems);
//        autoWorkbenchFluidsTile = HELPER.registerTile("tile.autoworkbench.fluid", TileAutoWorkbenchFluids::new, autoWorkbenchFluids);
        miningWellTile = HELPER.registerTile("tile.mining_well", TileMiningWell::new, miningWell);
        pumpTile = HELPER.registerTile("tile.pump", TilePump::new, pump);
        floodGateTile = HELPER.registerTile("tile.flood_gate", TileFloodGate::new, floodGate);
        tankTile = HELPER.registerTile("tile.tank", TileTank::new, tank);
        chuteTile = HELPER.registerTile("tile.chute", TileChute::new, chute);
        distillerTile = HELPER.registerTile("tile.distiller", TileDistiller_BC8::new, distiller);
        heatExchangeTile = HELPER.registerTile("tile.heat_exchange", TileHeatExchange::new, heatExchange);
    }

    public static void fmlPreInit() {

    }

}
