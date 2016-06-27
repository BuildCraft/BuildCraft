package buildcraft.api.bpt;

import buildcraft.api.IBptWriter;

public interface IBptAction extends IBptWriter {
    void run(IBuilderAccessor builder);
}
