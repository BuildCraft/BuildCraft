/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.blueprints;

import net.minecraft.nbt.NBTTagCompound;
import buildcraft.core.network.NetworkData;

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