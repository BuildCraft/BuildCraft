package buildcraft.builders.blueprints;

import buildcraft.core.network.NetworkData;
import net.minecraft.nbt.NBTTagCompound;

public class BlueprintMeta {

	public final String version = "Blueprint-2.0";

	@NetworkData
	public BlueprintId id;

	@NetworkData
	public String creator = "";

	public BlueprintMeta() {
		id = new BlueprintId();
	}

	/*protected BlueprintMeta(BlueprintId id, NBTTagCompound nbt) {
		this.id = id;

		name = nbt.getString("name");
		creator = nbt.getString("creator");
	}*/

	protected BlueprintId getId() {
		return id;
	}

	protected void setId(BlueprintId id) {
		this.id = id;
	}

	protected String getName() {
		return id.name;
	}

	protected void setName(String name) {
		this.id.name = name;
	}

	/**
	 * @return the creator
	 */
	protected String getCreator() {
		return creator;
	}

	/**
	 * @param creator the creator to set
	 */
	protected void setCreator(String creator) {
		this.creator = creator;
	}

	protected void writeToNBT(NBTTagCompound nbt) {
		nbt.setString("version", version);
		nbt.setString("creator", creator);
	}
}