package buildcraft.api.bpt;

public interface IBptAction extends IBptWriter {
    void run(IBuilderAccessor builder);
}
