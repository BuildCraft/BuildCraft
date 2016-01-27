package buildcraft.core.lib.network.base;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import buildcraft.core.lib.network.base.NetworkStatRecorder.EnumOpType;
import buildcraft.core.lib.network.base.NetworkStatRecorder.PacketSource;
import buildcraft.core.lib.network.base.NetworkStatRecorder.PacketStats;

public class StatisticsFrame extends JFrame implements TreeSelectionListener {
    private static final long serialVersionUID = -6012472823876988075L;

    // If it ever uses more than MB... something has probably gone wrong
    private static final String[] sizes = { "", "K", "M", "G", "T" };

    private static final Object INSTANCE_SYNC_OBJ = new Object();
    private static final Object STAT_SYNC_OBJ = new Object();

    static volatile StatisticsFrame instance = null;

    private JPanel contentPane;
    private JTree packetType;
    private JPanel packetGraph;

    private DefaultTreeModel packetTreeModel;

    private final Map<Class<? extends Packet>, PacketNode> nodes = Maps.newHashMap();
    private final Map<PacketSource, InfoNode> infoNodes = Maps.newHashMap();
    private final PacketNode rootNode;

    public static void destroyStatisticsFrame() {
        if (instance == null) return;
        synchronized (INSTANCE_SYNC_OBJ) {
            if (instance == null) return;
            instance.dispose();
            instance = null;
        }
    }

    public static void createStatisticsFrame() {
        if (instance != null) return;
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                synchronized (INSTANCE_SYNC_OBJ) {
                    if (instance != null) return;
                    try {
                        instance = new StatisticsFrame();
                        instance.updateGraph();
                        instance.setVisible(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                        instance = null;
                    } catch (Throwable t) {
                        t.printStackTrace();
                        throw new IllegalStateException(t);
                    }
                }
            }
        });
    }

    public static void update() {
        if (instance == null) return;
        synchronized (INSTANCE_SYNC_OBJ) {
            if (instance == null) return;
            instance.updateGraph();
        }
    }

    static String formatLong(long bytes, boolean useByteSize) {
        double tb = bytes;
        int type = 0;
        while (tb > 800) {
            // 1024 is a simpler division than 1000, and we keep accuracy to.
            tb /= 1024;
            type++;
        }
        String size = Double.toString(tb);
        if (size.length() > 5) {
            size = size.substring(0, 5);
        }
        return size + " " + sizes[type] + (useByteSize ? "B" : "");
    }

    /** Create the frame. */
    private StatisticsFrame() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                destroyStatisticsFrame();
            }
        });

        setTitle("BuildCraft network traffic");

        setBounds(100, 100, 450, 300);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        rootNode = new PacketNode(Packet.class, null);

        packetTreeModel = new DefaultTreeModel(rootNode, true);
        packetType = new JTree(packetTreeModel);
        packetType.addTreeSelectionListener(this);
        contentPane.add(packetType, BorderLayout.CENTER);

        // packetGraph = new JPanel();// Temp!
        // contentPane.add(packetGraph, BorderLayout.CENTER);
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        packetType.repaint();
    }

    private PacketNode getPacketNode(Class<? extends Packet> clazz) {
        if (!nodes.containsKey(clazz)) {
            if (clazz == Packet.class) {
                nodes.put(clazz, rootNode);
                return rootNode;
            }
            Class<?> parentClass = clazz.getSuperclass();
            PacketNode parentNode = getPacketNode((Class<? extends Packet>) parentClass);
            nodes.put(clazz, new PacketNode(clazz, parentNode));
        }
        return nodes.get(clazz);
    }

    private InfoNode getInfoNode(PacketSource source) {
        if (!infoNodes.containsKey(source)) {
            infoNodes.put(source, new InfoNode(source.extraInfo, getPacketNode(source.clazz)));
            packetType.revalidate();
            packetType.repaint();
        }
        return infoNodes.get(source);
    }

    private void updateGraph() {
        synchronized (STAT_SYNC_OBJ) {
            for (Entry<PacketSource, EnumMap<EnumOpType, PacketStats>> entry : NetworkStatRecorder.packetMap.entrySet()) {
                getInfoNode(entry.getKey()).update(entry.getValue());
            }
            rootNode.update();
        }
        packetType.revalidate();
        packetType.repaint();
    }

    private static abstract class BaseNode implements TreeNode {
        public static final int HISTORY_SIZE = NetworkStatRecorder.PacketStats.HISTORY_SIZE;
        static int index = 0;

        final boolean leaf;
        final BaseNode parent;
        final List<BaseNode> children;
        final long[] totalBytes = new long[2];
        final int[] totalPackets = new int[2];
        volatile EnumMap<EnumOpType, long[]> bytes = Maps.newEnumMap(EnumOpType.class);
        volatile EnumMap<EnumOpType, int[]> packets = Maps.newEnumMap(EnumOpType.class);

        protected BaseNode(boolean leaf, BaseNode parent) {
            this.leaf = leaf;
            this.parent = parent;
            this.children = Lists.newArrayList();
            for (EnumOpType type : EnumOpType.values()) {
                bytes.put(type, new long[HISTORY_SIZE]);
                packets.put(type, new int[HISTORY_SIZE]);
            }
            if (parent != null) {
                parent.children.add(this);
                instance.packetTreeModel.reload(parent);
            }
        }

        @Override
        public TreeNode getChildAt(int childIndex) {
            return children.get(childIndex);
        }

        @Override
        public int getChildCount() {
            return children.size();
        }

        @Override
        public TreeNode getParent() {
            return parent;
        }

        @Override
        public int getIndex(TreeNode node) {
            return children.indexOf(node);
        }

        @Override
        public boolean getAllowsChildren() {
            return !leaf;
        }

        @Override
        public boolean isLeaf() {
            return leaf;
        }

        @Override
        @SuppressWarnings("rawtypes")
        public Enumeration children() {
            return Collections.enumeration(children);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            synchronized (STAT_SYNC_OBJ) {
                int written = 0;
                for (EnumOpType type : EnumOpType.values()) {
                    if (totalBytes[type.ordinal()] == 0) continue;
                    builder.append(' ');
                    builder.append(formatLong(totalBytes[type.ordinal()], true));
                    builder.append(' ').append(type.operation).append(" over ");
                    builder.append(formatLong(totalPackets[type.ordinal()], false));
                    builder.append(" packets");
                    if (written == 0) {
                        builder.append(',');
                    }
                    written++;
                }
            }
            return builder.toString();
        }
    }

    private static class PacketNode extends BaseNode {
        final String clazz;

        protected PacketNode(Class<? extends Packet> clazz, PacketNode parent) {
            super(false, parent);
            this.clazz = clazz.getName();
        }

        @Override
        public String toString() {
            return clazz + super.toString();
        }

        public void update() {
            EnumMap<EnumOpType, long[]> totalBytes = Maps.newEnumMap(EnumOpType.class);
            EnumMap<EnumOpType, int[]> totalPackets = Maps.newEnumMap(EnumOpType.class);
            for (EnumOpType type : EnumOpType.values()) {
                totalBytes.put(type, new long[HISTORY_SIZE]);
                totalPackets.put(type, new int[HISTORY_SIZE]);
            }

            this.totalBytes[0] = this.totalBytes[1] = 0;
            this.totalPackets[0] = this.totalPackets[1] = 0;

            for (BaseNode child : children) {
                if (child instanceof PacketNode) {
                    ((PacketNode) child).update();
                }
                for (EnumOpType type : EnumOpType.values()) {
                    for (int i = 0; i < HISTORY_SIZE; i++) {
                        totalBytes.get(type)[i] += child.bytes.get(type)[i];
                        totalPackets.get(type)[i] += child.packets.get(type)[i];
                    }
                    this.totalBytes[type.ordinal()] += child.totalBytes[type.ordinal()];
                    this.totalPackets[type.ordinal()] += child.totalPackets[type.ordinal()];
                }
            }
            bytes = totalBytes;
            packets = totalPackets;
        }
    }

    private static class InfoNode extends BaseNode {
        final String info;

        protected InfoNode(String info, PacketNode parent) {
            super(true, parent);
            this.info = info;
        }

        @Override
        public String toString() {
            return info + super.toString();
        }

        void update(EnumMap<EnumOpType, PacketStats> map) {
            for (Entry<EnumOpType, PacketStats> entry : map.entrySet()) {
                EnumOpType type = entry.getKey();
                int[] packetsTo = packets.get(type);
                long[] bytesTo = bytes.get(type);
                PacketStats stats = entry.getValue();
                for (int i = 0; i < HISTORY_SIZE; i++) {
                    int from = (i + stats.currentIndex) % HISTORY_SIZE;
                    int to = (i + index) % HISTORY_SIZE;
                    packetsTo[to] = stats.packets[from];
                    bytesTo[to] = stats.bytes[from];
                }
                totalBytes[type.ordinal()] += stats.bytes[stats.getLastIndex()];
                totalPackets[type.ordinal()] += stats.packets[stats.getLastIndex()];
            }
        }
    }
}
