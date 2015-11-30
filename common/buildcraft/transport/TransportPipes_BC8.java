package buildcraft.transport;

import java.util.List;
import java.util.Locale;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.common.registry.GameRegistry;

import buildcraft.BuildCraftTransport;
import buildcraft.api.transport.pipe_bc8.IPipeBehaviourFactory;
import buildcraft.api.transport.pipe_bc8.PipeAPI_BC8;
import buildcraft.api.transport.pipe_bc8.PipeDefinition_BC8;
import buildcraft.core.BCCreativeTab;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.transport.api.impl.EnumPipeType;
import buildcraft.transport.pipes.bc8.EnumPipeMaterial;
import buildcraft.transport.pipes.bc8.behaviour.BehaviourFactoryBasic;
import buildcraft.transport.pipes.bc8.behaviour.BehaviourFactoryBasic.EnumListStatus;
import buildcraft.transport.pipes.bc8.behaviour.BehaviourFactoryPolishedStone;
import buildcraft.transport.pipes.bc8.behaviour.BehaviourFactoryWooden;

public class TransportPipes_BC8 {
    private static final Table<EnumPipeMaterial, EnumPipeType, PipeDefinition_BC8> pipes = HashBasedTable.create();

    public static void preInit() {
        // Register everything that applies to all different types
        for (EnumPipeType type : EnumPipeType.CONTENTS) {
            for (int i = 0; i < 4; i++) {
                // Don't set the factory as we will set it below
                EnumPipeMaterial material = EnumPipeMaterial.STONES[i][0];
                // Create the unpolished variant
                registerDefinition(material, type, createBasicDefinition(type, material, false));

                material = EnumPipeMaterial.STONES[i][1];
                // Create the polished variant
                registerDefinition(material, type, createPolishedStonesDefinition(type, material));
            }

            // Register blacklist for the unpolished stones
            {
                PipeDefinition_BC8[] unpolished = new PipeDefinition_BC8[4];
                BehaviourFactoryBasic[] unpolishedFactories = new BehaviourFactoryBasic[4];

                PipeDefinition_BC8[] polished = new PipeDefinition_BC8[4];
                BehaviourFactoryPolishedStone[] polishedFactories = new BehaviourFactoryPolishedStone[4];

                for (int i = 0; i < 4; i++) {
                    unpolished[i] = pipes.get(EnumPipeMaterial.STONES[i][0], type);
                    unpolishedFactories[i] = (BehaviourFactoryBasic) unpolished[i].behaviourFactory;

                    polished[i] = pipes.get(EnumPipeMaterial.STONES[i][1], type);
                    polishedFactories[i] = (BehaviourFactoryPolishedStone) polished[i].behaviourFactory;
                }

                for (int i = 0; i < 4; i++) {
                    List<PipeDefinition_BC8> otherUnpolished = Lists.newArrayList(unpolished);
                    otherUnpolished.remove(unpolished[i]);
                    unpolishedFactories[i].setDefinition(unpolished[i], EnumListStatus.BLACKLIST, otherUnpolished.toArray(new PipeDefinition_BC8[3]));

                    polishedFactories[i].setDefinition(polished[i], EnumListStatus.WHITELIST, unpolished[i], polished[i]);
                }
            }
            // Register wooden pipe as special
            registerDefinition(EnumPipeMaterial.WOOD, type, createWoodenDefinition(type));
        }

        PipeDefinition_BC8 definition = createBasicDefinition(EnumPipeType.STRUCTURE, EnumPipeMaterial.COBBLESTONE, true);
        registerDefinition(EnumPipeMaterial.COBBLESTONE, EnumPipeType.STRUCTURE, definition);
    }

    public static void addRecipies() {
        // Setup the glass types for later
        ItemStack[] glassTypes = new ItemStack[17];
        glassTypes[0] = new ItemStack(Blocks.glass);
        for (int i = 0; i < 16; i++) {
            glassTypes[i + 1] = new ItemStack(Blocks.stained_glass, 1, i);
        }

        // For every registered pipe
        for (Cell<EnumPipeMaterial, EnumPipeType, PipeDefinition_BC8> entry : pipes.cellSet()) {
            EnumPipeMaterial material = entry.getRowKey();
            EnumPipeType type = entry.getColumnKey();
            PipeDefinition_BC8 definition = entry.getValue();
            Item pipe = PipeAPI_BC8.PIPE_REGISTRY.getItem(definition);

            // 0 for unpainted and 1-16 for different glass colours
            for (int i = 0; i < 17; i++) {
                ItemStack output = new ItemStack(pipe, 1, i);

                if (type == EnumPipeType.ITEM) {
                    ItemStack in1 = material.ingredient1;
                    ItemStack in2 = material.ingredient2;
                    GameRegistry.addShapedRecipe(output, "1G2", '1', in1, 'G', glassTypes[i], '2', in2);
                } else {
                    // Fluid and power use modifiers on the item pipe
                    ItemStack modifier;
                    if (type == EnumPipeType.FLUID) {
                        modifier = new ItemStack(BuildCraftTransport.pipeWaterproof);
                    } else {
                        modifier = new ItemStack(Items.redstone);
                    }
                    Item pipeItem = null;
                    ItemStack inputPipe = new ItemStack(pipeItem, 1, i);
                    GameRegistry.addShapelessRecipe(output, modifier, inputPipe);

                    // Also allow uncrafting fluid + power pipes back down to item pipes, losing the modifier in the
                    // process

                    GameRegistry.addShapelessRecipe(inputPipe, output);
                }
            }
        }
    }

    public static PipeDefinition_BC8 getPipe(EnumPipeType type, EnumPipeMaterial material) {
        return pipes.get(material, type);
    }

    private static PipeDefinition_BC8 createBasicDefinition(EnumPipeType type, EnumPipeMaterial material, boolean defineFactoryFully) {
        BehaviourFactoryBasic factory = new BehaviourFactoryBasic();
        PipeDefinition_BC8 definition = createDefinition(type, material, factory);
        if (defineFactoryFully) {
            factory.setDefinition(definition, EnumListStatus.BLACKLIST);
        }
        return definition;
    }

    private static PipeDefinition_BC8 createPolishedStonesDefinition(EnumPipeType type, EnumPipeMaterial material) {
        BehaviourFactoryPolishedStone factory = new BehaviourFactoryPolishedStone();
        PipeDefinition_BC8 definition = createDefinition(type, material, factory);
        return definition;
    }

    private static PipeDefinition_BC8 createWoodenDefinition(EnumPipeType type) {
        BehaviourFactoryWooden factory = new BehaviourFactoryWooden();
        String[] suffixes = { "_clear", "_filled" };
        PipeDefinition_BC8 definition = createDefinition(type, EnumPipeMaterial.WOOD, suffixes, factory);
        factory.setDefinition(definition);
        return definition;
    }

    private static PipeDefinition_BC8 createDefinition(EnumPipeType type, EnumPipeMaterial material, IPipeBehaviourFactory factory) {
        return createDefinition(type, material, new String[] { "" }, factory);
    }

    private static PipeDefinition_BC8 createDefinition(EnumPipeType type, EnumPipeMaterial material, String[] suffixes,
            IPipeBehaviourFactory factory) {
        String name = material.name().toLowerCase(Locale.ROOT) + "_" + type.name().toLowerCase(Locale.ROOT);
        PipeDefinition_BC8 definition = new PipeDefinition_BC8(name, type, material.maxSprites, "buildcrafttransport:pipes/", suffixes, factory);
        return definition;
    }

    private static void registerDefinition(EnumPipeMaterial material, EnumPipeType type, PipeDefinition_BC8 definition) {
        Item item = PipeAPI_BC8.PIPE_REGISTRY.registerDefinition(definition);
        // item.setUnlocalizedName("buildcraft_" + item.getUnlocalizedName().replace("item.", ""));
        pipes.put(material, type, definition);
        CoreProxy.proxy.registerItem(item);
        item.setCreativeTab(BCCreativeTab.get("neptune"));
    }

    public static void init() {

    }

    public static void postInit() {

    }
}
