package buildcraft.builders;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import buildcraft.BuildCraftBuilders;
import buildcraft.api.library.ILibraryTypeHandler;
import buildcraft.core.blueprints.BlueprintBase;

public class LibraryBlueprintTypeHandler implements ILibraryTypeHandler {
	private final boolean isBlueprint;

	public LibraryBlueprintTypeHandler(boolean isBlueprint) {
		this.isBlueprint = isBlueprint;
	}

	@Override
	public boolean isHandler(ItemStack stack, boolean store) {
		if (isBlueprint) {
			return stack.getItem() instanceof ItemBlueprintStandard;
		} else {
			return stack.getItem() instanceof ItemBlueprintTemplate;
		}
	}

	@Override
	public String getFileExtension() {
		return isBlueprint ? "bpt" : "tpl";
	}

	@Override
	public int getTextColor() {
		return isBlueprint ? 0x305080 : 0;
	}

	@Override
	public String getName(ItemStack stack) {
		return ItemBlueprint.getId(stack).name;
	}

	@Override
	public ItemStack load(ItemStack stack, NBTTagCompound compound) {
		BlueprintBase blueprint = BlueprintBase.loadBluePrint(compound);
		blueprint.id.name = compound.getString("__filename");
		blueprint.id.extension = getFileExtension();
		BuildCraftBuilders.serverDB.add(blueprint.id, compound);
		return blueprint.getStack();
	}

	@Override
	public boolean store(ItemStack stack, NBTTagCompound compound) {
		BlueprintBase blueprint = ItemBlueprint.loadBlueprint(stack);
		if (blueprint != null) {
			NBTTagCompound nbt = blueprint.getNBT();
			for (Object o : nbt.func_150296_c()) {
				compound.setTag((String) o, nbt.getTag((String) o));
			}
			blueprint.id.write(compound);
			return true;
		}
		return false;
	}
}
