/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.statements;

import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementManager;

public abstract class BCStatement implements IStatement {

    protected final String uniqueTag;

    /** UniqueTag accepts multiple possible tags, use this feature to migrate to more standardised tags if needed,
     * otherwise just pass a single string. The first passed string will be the one used when saved to disk.
     *
     * @param uniqueTag */
    public BCStatement(String... uniqueTag) {
        this.uniqueTag = uniqueTag[0];
        for (String tag : uniqueTag) {
            StatementManager.statements.put(tag, this);
        }
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
    public IStatement[] getPossible() {
        return new IStatement[] { this };
    }

    @Override
    public IStatementParameter createParameter(int index) {
        return null;
    }

    protected static <P extends IStatementParameter> P getParam(int index, IStatementParameter[] params, P _default) {
        if (params.length <= index) {
            return _default;
        }
        IStatementParameter atIndex = params[index];
        if (atIndex.getClass() == _default.getClass()) {
            return (P) atIndex;
        }
        return _default;
    }
}
