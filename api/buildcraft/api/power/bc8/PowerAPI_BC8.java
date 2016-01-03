package buildcraft.api.power.bc8;

import java.util.function.Predicate;

import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.APIHelper;
import buildcraft.api.power.bc8.IPowerConnection.IPowerConsumer;
import buildcraft.api.power.bc8.IPowerTunnel.IPowerTunnelHalfLoaded;

public class PowerAPI_BC8 {

    public static final IPowerNetwork NETWORK;

    static {
        NETWORK = APIHelper.getInstance("buildcraft.core.power_bc8.PowerNetwork", IPowerNetwork.class);
    }

    public interface IPowerNetwork {
        /** Adds the given connection to the internal list of active connection objects.
         * 
         * @param connection */
        void addConnection(IPowerConnection connection);

        /** Removes the given connection and disconnects all {@link IPowerTunnel} that use it.
         * 
         * @param connection */
        void removeConnection(IPowerConnection connection);

        /** Identical to {@link #requestTunnel(IPowerConsumer, EnumPowerBar, int, Predicate)} except that all
         * connections are allowed.
         * <p>
         * This MAY be cheaper to call than {@link #requestTunnel(IPowerConsumer, EnumPowerBar, int, Predicate)} if you
         * don't mind what connections are gone over, as this might provide optimisations that cannot be applied to
         * {@link #requestTunnel(IPowerConsumer, EnumPowerBar, int, Predicate)}.
         * 
         * @return A tunnel that can satisfy the request, and will either be */
        default IPowerTunnel requestTunnel(IPowerConsumer consumer, EnumPowerBar type, int units) {
            return requestTunnel(consumer, type, units, c -> true);
        }

        /** Requests a tunnel to supply the given consumer the number of units given, checking each node along the way
         * to make sure that it satisfies the filter.
         * 
         * <p>
         * <b>NOTE:</b> This may return an {@link IPowerTunnelHalfLoaded} if this cannot search the graph within a
         * single tick. If this is the case then the resulting tunnel will return a non-null value from
         * {@link IPowerTunnelHalfLoaded#loadedTunnel()}, which will be the loaded tunnel you can use.
         * 
         * @param type The type of power that will be given.
         * @param units The number of power units to request. The resulting tunnel will supply up to the given number of
         *            units without fail, disconnecting automatically if nothing can fulfil the request.
         * @param connectionFilter The filter to check each possible connection against. Each connection will
         *            automatically be checked against the type and unit count for you. */
        IPowerTunnel requestTunnel(IPowerConsumer consumer, EnumPowerBar type, int units, Predicate<IPowerConnection> connectionFilter);

        /** Loads the tunnel from NBT, given the consumer that saved it. */
        IPowerTunnel loadTunnel(IPowerConsumer consumer, NBTTagCompound compound);

        public enum Void implements IPowerNetwork {
            INSTANCE;

            @Override
            public void addConnection(IPowerConnection connection) {}

            @Override
            public void removeConnection(IPowerConnection connection) {}

            @Override
            public IPowerTunnel requestTunnel(IPowerConsumer consumer, EnumPowerBar type, int units, Predicate<IPowerConnection> connectionFilter) {
                return null;
            }

            @Override
            public IPowerTunnel loadTunnel(IPowerConsumer consumer, NBTTagCompound compound) {
                return null;
            }
        }
    }
}
