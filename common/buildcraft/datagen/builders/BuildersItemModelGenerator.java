package buildcraft.datagen.builders;

import buildcraft.builders.BCBuilders;
import buildcraft.builders.BCBuildersBlocks;
import buildcraft.builders.BCBuildersItems;
import buildcraft.builders.client.BuildersItemModelPredicates;
import buildcraft.datagen.base.BCBaseItemModelGenerator;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;

public class BuildersItemModelGenerator extends BCBaseItemModelGenerator {
    public BuildersItemModelGenerator(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, BCBuilders.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        // BlockItems

        // builder
        getBuilder(BCBuildersBlocks.builder.get().getRegistryName().toString())
                .element()
                .from(0, 0, 0)
                .to(16, 16, 16)
                .face(Direction.DOWN).texture("#down").cullface(Direction.DOWN).end()
                .face(Direction.UP).texture("#up").cullface(Direction.UP).end()
                .face(Direction.NORTH).texture("#north").cullface(Direction.NORTH).end()
                .face(Direction.SOUTH).texture("#south").cullface(Direction.SOUTH).end()
                .face(Direction.WEST).texture("#west").cullface(Direction.WEST).end()
                .face(Direction.EAST).texture("#east").cullface(Direction.EAST).end()
                .end()
                .element()
                .from(3, 4, -2)
                .to(13, 6, 8)
                .face(Direction.DOWN).texture("#slot").uvs(3, 3, 13, 13).end()
                .face(Direction.UP).texture("#slot").uvs(3, 3, 13, 13).end()
                .face(Direction.NORTH).texture("#slot").uvs(3, 0, 13, 2).end()
                .face(Direction.SOUTH).texture("#slot").uvs(3, 0, 13, 2).end()
                .face(Direction.WEST).texture("#slot").uvs(3, 0, 13, 2).end()
                .face(Direction.EAST).texture("#slot").uvs(3, 0, 13, 2).end()
                .end()
                .texture("particle", "buildcraftbuilders:block/builder/side")
                .texture("down", "buildcraftbuilders:block/builder/bottom")
                .texture("up", "buildcraftbuilders:block/builder/top")
                .texture("north", "buildcraftbuilders:block/builder/front")
                .texture("east", "buildcraftbuilders:block/builder/side")
                .texture("south", "buildcraftbuilders:block/builder/back")
                .texture("west", "buildcraftbuilders:block/builder/side")
                .texture("slot", "buildcraftbuilders:block/builder/slot_blueprint")
                .guiLight(BlockModel.GuiLight.SIDE)
                .transforms()
                .transform(ItemDisplayContext.GUI).rotation(30, 225, 0).translation(0, 0, 0).scale(0.625F, 0.625F, 0.625F).end()
                .transform(ItemDisplayContext.GROUND).rotation(0, 0, 0).translation(0, 3, 0).scale(0.25F, 0.25F, 0.25F).end()
                .transform(ItemDisplayContext.FIXED).rotation(0, 0, 0).translation(0, 0, 0).scale(0.5F, 0.5F, 0.5F).end()
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND).rotation(75, 45, 0).translation(0, 2.5F, 0).scale(0.375F, 0.375F, 0.375F).end()
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND).rotation(0, 45, 0).translation(0, 0, 0).scale(0.40F, 0.40F, 0.40F).end()
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND).rotation(0, 225, 0).translation(0, 0, 0).scale(0.40F, 0.40F, 0.40F).end()
                .end()
        ;
        // filler
        withExistingParent(BCBuildersBlocks.filler.get().getRegistryName().toString(), new ResourceLocation("buildcraftbuilders:block/filler/main"));
        // frame
        withExistingParent(BCBuildersBlocks.frame.get().getRegistryName().toString(), CUBE_ALL)
                .element()
                .from(4, 0, 4)
                .to(12, 16, 12)
                .face(Direction.DOWN).texture("#all").cullface(Direction.DOWN).end()
                .face(Direction.UP).texture("#all").cullface(Direction.UP).end()
                .face(Direction.NORTH).texture("#all").cullface(Direction.NORTH).end()
                .face(Direction.SOUTH).texture("#all").cullface(Direction.SOUTH).end()
                .face(Direction.WEST).texture("#all").cullface(Direction.WEST).end()
                .face(Direction.EAST).texture("#all").cullface(Direction.EAST).end()
                .end()
                .texture("all", new ResourceLocation("buildcraftbuilders:block/frame/default"))
        ;
        // quarry
        withExistingParent(BCBuildersBlocks.quarry.get().getRegistryName().toString(), new ResourceLocation("buildcraftbuilders:block/quarry/false_false_false_false_false_false"));
        // replacer
        withExistingParent(BCBuildersBlocks.replacer.get().getRegistryName().toString(), new ResourceLocation("buildcraftbuilders:block/replacer"));

        // Calen: these were declared in blockstates in 1.12.2, no single file
        // library
        withExistingParent(BCBuildersBlocks.library.get().getRegistryName().toString(), new ResourceLocation("buildcraftbuilders:block/library"));
        // architect
        withExistingParent(BCBuildersBlocks.architect.get().getRegistryName().toString(), new ResourceLocation("buildcraftbuilders:block/architect_off"));

        // Items

        // addonFillerPlanner
        withExistingParent(BCBuildersItems.addonFillerPlanner.get().getRegistryName().toString(), CUBE_ALL)
                .texture("all", new ResourceLocation("buildcraftbuilders:addons/filler_planner"));

        // schematicSingle
        ResourceLocation schematicSingle = BCBuildersItems.schematicSingle.get().getRegistryName();
        withExistingParent(schematicSingle.toString(), GENERATED)
                .override()
                .model(
                        withExistingParent(schematicSingle.getNamespace() + ":item/" + schematicSingle.getPath() + "/clean", GENERATED)
                                .texture("layer0", "buildcraftbuilders:item/schematic_single/clean")
                )
                .predicate(BuildersItemModelPredicates.PREDICATE_USED, 0)
                .end()
                .override()
                .model(
                        withExistingParent(schematicSingle.getNamespace() + ":item/" + schematicSingle.getPath() + "/used", GENERATED)
                                .texture("layer0", "buildcraftbuilders:item/schematic_single/used")
                )
                .predicate(BuildersItemModelPredicates.PREDICATE_USED, 1)
                .end();

        // snapshotBLUEPRINT
        ResourceLocation snapshotBLUEPRINT = BCBuildersItems.snapshotBLUEPRINT.get().getRegistryName();
        withExistingParent(snapshotBLUEPRINT.toString(), GENERATED)
                .override()
                .model(
                        withExistingParent(snapshotBLUEPRINT.getNamespace() + ":item/" + snapshotBLUEPRINT.getPath() + "/clean", GENERATED)
                                .texture("layer0", "buildcraftbuilders:item/blueprint/clean")
                )
                .predicate(BuildersItemModelPredicates.PREDICATE_USED, 0)
                .end()
                .override()
                .model(
                        withExistingParent(snapshotBLUEPRINT.getNamespace() + ":item/" + snapshotBLUEPRINT.getPath() + "/used", GENERATED)
                                .texture("layer0", "buildcraftbuilders:item/blueprint/used")
                )
                .predicate(BuildersItemModelPredicates.PREDICATE_USED, 1)
                .end();

        // snapshotTEMPLATE
        ResourceLocation snapshotTEMPLATE = BCBuildersItems.snapshotTEMPLATE.get().getRegistryName();
        withExistingParent(snapshotTEMPLATE.toString(), GENERATED)
                .override()
                .model(
                        withExistingParent(snapshotTEMPLATE.getNamespace() + ":item/" + snapshotTEMPLATE.getPath() + "/clean", GENERATED)
                                .texture("layer0", "buildcraftbuilders:item/template/clean")
                )
                .predicate(BuildersItemModelPredicates.PREDICATE_USED, 0)
                .end()
                .override()
                .model(
                        withExistingParent(snapshotTEMPLATE.getNamespace() + ":item/" + snapshotTEMPLATE.getPath() + "/used", GENERATED)
                                .texture("layer0", "buildcraftbuilders:item/template/used")
                )
                .predicate(BuildersItemModelPredicates.PREDICATE_USED, 1)
                .end();

    }

    @Nonnull
    @Override
    public String getName() {
        return "BuildCraft Builders Item Model Generator";
    }
}
