package buildcraft.builders.schematics;

import buildcraft.core.builders.schematics.SchematicRotateMeta;

public class SchematicBuilderLike extends SchematicRotateMeta {
	public SchematicBuilderLike() {
		super(new int[]{2, 5, 3, 4}, true);
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
