package buildcraft.api.transport.pipe_bc8;

public interface IContentsFilter {
    boolean matches(IPipeContents contents);

    public interface Item {
        int maxItems();
    }
    
    public interface Fluid {
        int maxMilliBuckets();
    }
    
    public interface Power {
        int maxRF();
    }
}
