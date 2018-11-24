package buildcraft.lib.client.guide.entry;

@FunctionalInterface
public interface IEntryIterable {
    void iterateAllDefault(IEntryLinkConsumer consumer);
}
