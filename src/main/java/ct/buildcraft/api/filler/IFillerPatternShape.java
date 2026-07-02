/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package ct.buildcraft.api.filler;

import javax.annotation.Nullable;

import ct.buildcraft.api.statements.IStatementParameter;
import ct.buildcraft.api.statements.containers.IFillerStatementContainer;

/** {@code IFillerPattern} independent from {@link World} */
public interface IFillerPatternShape extends IFillerPattern {
    /**
     * @param filledTemplate empty template
     * @return {@code true} if the template filled, or {@code false} if this shouldn't make a template for the given params.
     */
    boolean fillTemplate(IFilledTemplate filledTemplate, IStatementParameter[] params);

    @Nullable
    @Override
    default IFilledTemplate createTemplate(IFillerStatementContainer filler, IStatementParameter[] params) {
        IFilledTemplate template = FillerManager.registry.createFilledTemplate(
            filler.getBox().min(),
            filler.getBox().size()
        );
        if (!fillTemplate(template, params)) {
            return null;
        }
        return template;
    }
}
