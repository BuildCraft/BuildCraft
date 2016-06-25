package buildcraft.lib.client.guide;

@Deprecated
public interface IComparableLine {
    String getText();

    int compareToLine(IComparableLine other);
}
