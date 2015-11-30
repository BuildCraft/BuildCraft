package buildcraft.transport.pipes.bc8.filter;

import buildcraft.api.transport.pipe_bc8.IContentsFilter;
import buildcraft.api.transport.pipe_bc8.IPipeContents;

public abstract class MaximumContentsFilter implements IContentsFilter {
    public static class Item extends MaximumContentsFilter implements IContentsFilter.Item {
        private final int maxItems;

        public Item(int maxItems) {
            this.maxItems = maxItems;
        }

        @Override
        public boolean matches(IPipeContents contents) {
            return contents instanceof IPipeContents.IPipeContentsItem;
        }

        @Override
        public int maxItems() {
            return maxItems;
        }
    }

    public static class Fluid extends MaximumContentsFilter implements IContentsFilter.Fluid {
        private final int maxMB;

        public Fluid(int maxMB) {
            this.maxMB = maxMB;
        }

        @Override
        public boolean matches(IPipeContents contents) {
            return contents instanceof IPipeContents.IPipeContentsItem;
        }

        @Override
        public int maxMilliBuckets() {
            return maxMB;
        }
    }

    public static class Power extends MaximumContentsFilter implements IContentsFilter.Power {
        private final int maxPower;

        public Power(int maxPower) {
            this.maxPower = maxPower;
        }

        @Override
        public boolean matches(IPipeContents contents) {
            return contents instanceof IPipeContents.IPipeContentsItem;
        }

        @Override
        public int maxRF() {
            return maxPower;
        }
    }
}
