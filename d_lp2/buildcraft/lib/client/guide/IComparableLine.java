package buildcraft.lib.client.guide;

public interface IComparableLine {
    String getText();

    int compareToLine(IComparableLine other);
}
