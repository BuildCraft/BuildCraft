package buildcraft.transport.pipes.bc8;

import java.util.Collections;
import java.util.Map;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import buildcraft.api.transport.pipe_bc8.BCPipeEventHandler;
import buildcraft.api.transport.pipe_bc8.IConnection_BC8;
import buildcraft.api.transport.pipe_bc8.IPipeHolder_BC8;
import buildcraft.api.transport.pipe_bc8.IPipeListener;
import buildcraft.api.transport.pipe_bc8.IPipePropertyProvider;
import buildcraft.api.transport.pipe_bc8.IPipePropertyProvider.IPipePropertyProviderEditable;
import buildcraft.api.transport.pipe_bc8.IPipe_BC8;
import buildcraft.api.transport.pipe_bc8.PipeAPI_BC8;
import buildcraft.api.transport.pipe_bc8.PipeBehaviour_BC8;
import buildcraft.api.transport.pipe_bc8.PipeDefinition_BC8;
import buildcraft.api.transport.pipe_bc8.event_bc8.IPipeEventConnection_BC8;
import buildcraft.api.transport.pipe_bc8.event_bc8.IPipeEventContents_BC8;
import buildcraft.api.transport.pipe_bc8.event_bc8.IPipeEvent_BC8;
import buildcraft.core.lib.event.EventBusProviderASM;
import buildcraft.core.lib.event.IEventBus;
import buildcraft.core.lib.event.IEventBusProvider;

public class Pipe_BC8 implements IPipe_BC8 {
    private static final IEventBusProvider<IPipeEvent_BC8> eventBusProvider = new EventBusProviderASM<IPipeEvent_BC8>(IPipeEvent_BC8.class,
            BCPipeEventHandler.class);

    private final PipeDefinition_BC8 definition;
    private final PipeBehaviour_BC8 behaviour;
    private final IPipeHolder_BC8 holder;
    private final World world;
    private final IPipePropertyProviderEditable propProvider = new PipePropertyProviderEditable(this);
    private final IEventBus<IPipeEvent_BC8> bus = eventBusProvider.newBus();
    private final BiMap<Integer, IPipeListener> listenerMap = HashBiMap.create();
    private int nextListenerId = 1;

    private final Map<EnumFacing, PipeConnection> connectionMap = Maps.newHashMap();
    private final Map<EnumFacing, PipeConnection> readOnlyConntionMap = Collections.unmodifiableMap(connectionMap);

    private NBTTagCompound initNBT = null;

    public Pipe_BC8(IPipeHolder_BC8 holder, PipeDefinition_BC8 definition, World world) {
        if (holder == null) throw new NullPointerException("holder");
        if (definition == null) throw new NullPointerException("definition");
        if (world == null) throw new NullPointerException("world");
        this.holder = holder;
        this.definition = definition;
        this.behaviour = definition.behaviourFactory.createNew(this);
        bus.registerHandler(behaviour);
        this.world = world;
    }

    private Pipe_BC8(IPipeHolder_BC8 holder, PipeBehaviour_BC8 behaviour, World world) {
        this.holder = holder;
        this.definition = behaviour.definition;
        this.behaviour = behaviour;
        this.world = world;
    }

    @Override
    public Pipe_BC8 readFromNBT(NBTBase base) {
        NBTTagCompound nbt = (NBTTagCompound) base;
        PipeBehaviour_BC8 behaviour = this.behaviour.readFromNBT(nbt.getTag("behaviour"));
        Pipe_BC8 pipe = new Pipe_BC8(holder, behaviour, getWorld());

        NBTTagList list = nbt.getTagList("listeners", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound comp = list.getCompoundTagAt(i);
            String type = comp.getString("type");
            IPipeListener listener = PipeAPI_BC8.PIPE_LISTENER_REGISTRY.getFactory(type).createNewListener(pipe);
            pipe.bus.registerHandler(listener);
            pipe.listenerMap.put(pipe.nextListenerId++, listener);
        }

        initNBT = (NBTTagCompound) nbt.copy();
        return pipe;
    }

    @Override
    public NBTBase writeToNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("behaviour", behaviour.writeToNBT());

        NBTTagList list = new NBTTagList();
        for (IPipeListener listener : listenerMap.values()) {
            NBTTagCompound comp = new NBTTagCompound();
            comp.setString("type", PipeAPI_BC8.PIPE_LISTENER_REGISTRY.getGlobalUniqueTag(listener));
            comp.setTag("data", listener.writeToNBT());
            list.appendTag(comp);
        }
        nbt.setTag("listeners", list);

        NBTTagCompound connections = new NBTTagCompound();
        for (EnumFacing face : EnumFacing.values()) {
            if (connectionMap.containsKey(face)) {
                PipeConnection connection = connectionMap.get(face);
                NBTTagCompound compound = connection.saveConnection();
                connections.setTag(face.name(), compound);
            }
        }
        nbt.setTag("connections", connections);

        return nbt;
    }

    void initialize() {
        if (initNBT != null) {
            NBTTagCompound connections = initNBT.getCompoundTag("connections");
            for (EnumFacing face : EnumFacing.values()) {
                if (!connections.hasKey(face.name())) continue;
                NBTTagCompound connNBT = connections.getCompoundTag(face.name());
                PipeConnection conn = PipeConnection.loadConnection(connNBT, world);
                if (conn != null) connectionMap.put(face, conn);
            }
            initNBT = null;
        }
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public IPipeHolder_BC8 getHolder() {
        return holder;
    }

    @Override
    public IPipePropertyProvider getProperties() {
        return propProvider.asReadOnly();
    }

    @Override
    public PipeBehaviour_BC8 getBehaviour() {
        return behaviour;
    }

    @Override
    public Map<EnumFacing, ? extends IConnection_BC8> getConnections() {
        return readOnlyConntionMap;
    }

    @Override
    public void fireEvent(IPipeEvent_BC8 event) {
        if (event instanceof IPipeEventConnection_BC8.Create) throw new IllegalArgumentException("Cannot directly fire a connection creation event!");
        if (event instanceof IPipeEventContents_BC8.Enter) throw new IllegalArgumentException("Cannot directly fire a contents enter method!");
        bus.handleEvent(event);
    }

    @Override
    public boolean addEventListener(IPipeListener list) {
        if (list == null) throw new NullPointerException("listener");
        if (list == behaviour) throw new IllegalArgumentException("Cannot re-add the behaviour!");
        if (listenerMap.inverse().containsKey(list)) throw new IllegalArgumentException("Cannot add something twice!");
        // Fire an event
        // If event was denied then return false

        int id = nextListenerId++;
        listenerMap.put(id, list);
        bus.registerHandler(list);
        return true;
    }

    @Override
    public void removeEventListener(IPipeListener list) {
        listenerMap.inverse().remove(list);
    }

    @Override
    public void sendClientUpdate(IPipeListener listener) {
        int id;
        if (listener == behaviour) id = 0;
        else id = listenerMap.inverse().get(listener);
        // Create a packet
        // Send the packet
    }

    @Override
    public void sendRenderUpdate() {

    }
}
