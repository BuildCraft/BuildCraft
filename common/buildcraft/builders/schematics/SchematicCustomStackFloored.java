package buildcraft.builders.schematics;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.core.builders.schematics.SchematicCustomStack;
import com.google.common.collect.ImmutableSet;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;

import java.util.Set;

/**
 * Created by asie on 3/14/16.
 */
public class SchematicCustomStackFloored extends SchematicCustomStack {
	public SchematicCustomStackFloored(ItemStack customStack) {
		super(customStack);
	}

	@Override
	public Set<BlockPos> getPrerequisiteBlocks(IBuilderContext context) {
		return ImmutableSet.of(new BlockPos(0, -1, 0));
	}
}
