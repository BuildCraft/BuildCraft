package buildcraft.api.bpt;

import buildcraft.api.IUniqueWriter;

public interface IBptAction extends IUniqueWriter {
    void run(IBuilderAccessor builder);
}
