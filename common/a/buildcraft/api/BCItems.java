package a.buildcraft.api;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;

import buildcraft.api.core.BCLog;

/** Stores all of BuildCraft's items, from all of its modules. If any of them have been disabled by the user (or it the
 * module is not installed) then they will be null. This is the equivalent of {@link Items} */
public class BCItems {
    // BC Core
    public static final Item coreWrench;
    public static final Item coreList;
    public static final Item coreMapLocation;
    public static final Item corePaintbrush;
    public static final Item coreGearWood;
    public static final Item coreGearStone;
    public static final Item coreGearIron;
    public static final Item coreGearGold;
    public static final Item coreGearDiamond;

    // BC Builders
    public static final Item buildersBlueprint;
    public static final Item buildersTemplate;

    // BC Factory
    public static final Item factoryPlasticSheet;

    // BC Robotics
    public static final Item roboticsRedstoneBoard;
    public static final Item roboticsRobot;
    public static final Item roboticsRobotStation;
    public static final Item roboticsRobotGoggles;

    // BC Silicon
    public static final Item siliconRedstoneChipset;

    // BC Transport
    public static final Item transportWaterproof;
    public static final Item transportGateCopier;
    public static final Item transportPluggableGate;
    public static final Item transportPluggableWire;
    public static final Item transportPluggablePlug;
    public static final Item transportPluggableLens;
    public static final Item transportPluggablePowerAdapter;
    public static final Item transportPluggableFacade;
    // TODO: Pipes

    // Initialiser
    static {
        if (!Loader.instance().hasReachedState(LoaderState.INITIALIZATION)) {
            throw new RuntimeException("Accessed BC items too early! You can only use them from init onwards!");
        }
        // core
        final String core = "core";
        coreWrench = getRegisteredItem(core, "wrench");
        coreList = getRegisteredItem(core, "list");
        coreMapLocation = getRegisteredItem(core, "map_location");
        corePaintbrush = getRegisteredItem(core, "paintbrush");
        coreGearWood = getRegisteredItem(core, "gear_wood");
        coreGearStone = getRegisteredItem(core, "gear_stone");
        coreGearIron = getRegisteredItem(core, "gear_iron");
        coreGearGold = getRegisteredItem(core, "gear_gold");
        coreGearDiamond = getRegisteredItem(core, "gear_diamond");
        // builders
        final String builders = "builders";
        buildersBlueprint = getRegisteredItem(builders, "blueprint");
        buildersTemplate = getRegisteredItem(builders, "template");
        // factory
        final String factory = "factory";
        factoryPlasticSheet = getRegisteredItem(factory, "plastic_sheet");
        // robotics
        final String robotics = "robotics";
        roboticsRedstoneBoard = getRegisteredItem(robotics, "redstone_board");
        roboticsRobot = getRegisteredItem(robotics, "robot");
        roboticsRobotStation = getRegisteredItem(robotics, "robot_station");
        roboticsRobotGoggles = getRegisteredItem(robotics, "robot_goggles");
        // silicon
        final String silicon = "silicon";
        siliconRedstoneChipset = getRegisteredItem(silicon, "redstone_chipset");
        // transport
        final String transport = "transport";
        transportWaterproof = getRegisteredItem(transport, "waterproof");
        transportGateCopier = getRegisteredItem(transport, "gate_copier");
        transportPluggableGate = getRegisteredItem(transport, "pluggable_gate");
        transportPluggableWire = getRegisteredItem(transport, "pluggable_wire");
        transportPluggablePlug = getRegisteredItem(transport, "pluggable_plug");
        transportPluggableLens = getRegisteredItem(transport, "pluggable_lens");
        transportPluggableFacade = getRegisteredItem(transport, "pluggable_facade");
        transportPluggablePowerAdapter = getRegisteredItem(transport, "pluggable_power_adapter");
    }

    private static Item getRegisteredItem(String module, String regName) {
        String modid = "buildcraft" + module;
        Item item = Item.itemRegistry.getObject(new ResourceLocation(modid, regName));
        if (item != null) {
            return item;
        }
        if (Loader.isModLoaded(modid)) {
            // Only info because the item might have been disabled by the user
            BCLog.logger.info("[item-api] Did not find the item " + regName + " dispite the appropriate mod being loaded (" + modid + ")");
        }
        return null;
    }
}
