package buildcraft.api.bpt;

@Deprecated
public interface IBptAction extends IBptWriter {
    void run(IBuilderAccessor builder);
}
