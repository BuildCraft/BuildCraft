/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 * Please check the contents of the license, which should be located
 * as "LICENSE.API" in the BuildCraft source code distribution.
 */
package buildcraft.api.statements;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import buildcraft.api.core.SheetIcon;

public interface IStatement {

	/**
	 * Every trigger needs a unique tag, it should be in the format of
	 * "<modid>:<name>".
	 *
	 * @return the unique id
	 */
	String getUniqueTag();

	@SideOnly(Side.CLIENT)
	SheetIcon getIcon();

	/**
	 * Return the maximum number of parameter this trigger can have, 0 if none.
	 */
	int maxParameters();

	/**
	 * Return the minimum number of parameter this trigger can have, 0 if none.
	 */
	int minParameters();

	/**
	 * Return the trigger description in the UI
	 */
	String getDescription();

	/**
	 * Create parameters for the trigger.
	 */
	IStatementParameter createParameter(int index);

	/**
	 * This returns the trigger after a left rotation. Used in particular in
	 * blueprints orientation.
	 */
	IStatement rotateLeft();
}
