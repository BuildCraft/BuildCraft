package buildcraft.builders.schematics;

import buildcraft.api.blueprints.SchematicTile;

public class SchematicBuilderLike extends SchematicTile {
    public SchematicBuilderLike() {
        super();
    }

    @Override
    public void onNBTLoaded() {
        if (tileNBT != null) {
            tileNBT.removeTag("box");
            tileNBT.removeTag("bpt");
            tileNBT.removeTag("bptBuilder");
            tileNBT.removeTag("builderState");
            tileNBT.removeTag("done");
            tileNBT.removeTag("iterator");
            tileNBT.removeTag("path");
        }
    }
}
