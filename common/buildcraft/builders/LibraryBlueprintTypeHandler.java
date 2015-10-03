package buildcraft.builders;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import buildcraft.BuildCraftBuilders;
import buildcraft.api.library.LibraryTypeHandlerNBT;
import buildcraft.core.blueprints.BlueprintBase;
import buildcraft.core.blueprints.LibraryId;

public class LibraryBlueprintTypeHandler extends LibraryTypeHandlerNBT {
	private final boolean isBlueprint;

	public LibraryBlueprintTypeHandler(boolean isBlueprint) {
		super(isBlueprint ? "bpt" : "tpl");
		this.isBlueprint = isBlueprint;
	}

	@Override
	public boolean isHandler(ItemStack stack, HandlerType type) {
		if (isBlueprint) {
			return stack.getItem() instanceof ItemBlueprintStandard && (type == HandlerType.LOAD || ItemBlueprint.isContentReadable(stack));
		} else {
			return stack.getItem() instanceof ItemBlueprintTemplate && (type == HandlerType.LOAD || ItemBlueprint.isContentReadable(stack));
		}
	}

	@Override
	public int getTextColor() {
		return isBlueprint ? 0x305080 : 0;
	}

	@Override
	public String getName(ItemStack stack) {
		LibraryId id = ItemBlueprint.getId(stack);
		return id != null ? id.name : "<<CORRUPT>>";
	}

	@Override
	public ItemStack load(ItemStack stack, NBTTagCompound compound) {
		BlueprintBase blueprint = BlueprintBase.loadBluePrint((NBTTagCompound) compound.copy());
		blueprint.id.name = compound.getString("__filename");
		blueprint.id.extension = getOutputExtension();
		BuildCraftBuilders.serverDB.add(blueprint.id, compound);
		return blueprint.getStack();
	}

	@Override
	public boolean store(ItemStack stack, NBTTagCompound compound) {
		LibraryId id = ItemBlueprint.getId(stack);
		if (id == null) {
			return false;
		}

		NBTTagCompound nbt = BuildCraftBuilders.serverDB.load(id);
		if (nbt == null) {
			return false;
		}

		for (Object o : nbt.func_150296_c()) {
			compound.setTag((String) o, nbt.getTag((String) o));
		}
		id.write(compound);
		return true;
	}
}
