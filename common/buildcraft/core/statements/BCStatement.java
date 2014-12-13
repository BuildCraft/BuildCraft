/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.statements;

import net.minecraft.util.ResourceLocation;
import buildcraft.api.core.SheetIcon;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementManager;

public abstract class BCStatement implements IStatement {
	protected final String uniqueTag;
	protected SheetIcon icon;

	private static final ResourceLocation STATEMENT_ICONS = new ResourceLocation("buildcraft", "textures/gui/statements.png");

	/**
	 * UniqueTag accepts multiple possible tags, use this feature to migrate to
	 * more standardized tags if needed, otherwise just pass a single string.
	 * The first passed string will be the one used when saved to disk.
	 *
	 * @param uniqueTag
	 */
	public BCStatement(String... uniqueTag) {
		this.uniqueTag = uniqueTag[0];
		for (String tag : uniqueTag) {
			StatementManager.statements.put(tag, this);
		}
		this.icon = new SheetIcon(STATEMENT_ICONS, getSheetLocation() & 15, getSheetLocation() >> 4);
	}

	@Override
	public String getUniqueTag() {
		return uniqueTag;
	}


	@Override
	public int maxParameters() {
		return 0;
	}

	@Override
	public int minParameters() {
		return 0;
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public IStatement rotateLeft() {
		return this;
	}

	@Override
	public IStatementParameter createParameter(int index) {
		return null;
	}

	public abstract int getSheetLocation();
}
