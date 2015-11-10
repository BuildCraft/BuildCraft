package buildcraft.core.lib.network.base;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

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

import buildcraft.core.lib.network.base.ChannelHandlerStats.PacketSource;

public class StatisticsFrame extends JFrame implements TreeSelectionListener {
    private static final long serialVersionUID = -6012472823876988075L;

    private static final Object SYNC_OBJ = new Object();
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
        synchronized (SYNC_OBJ) {
            if (instance == null) return;
            instance.dispose();
            instance = null;
        }
    }

    public static void createStatisticsFrame() {
        if (instance != null) return;
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                synchronized (SYNC_OBJ) {
                    if (instance != null) return;
                    try {
                        instance = new StatisticsFrame();
                        instance.updateGraph();
                        instance.setVisible(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                        instance = null;
                    }
                }
            }
        });
    }

    public static void update() {
        if (instance == null) return;
        synchronized (SYNC_OBJ) {
            if (instance == null) return;
            instance.updateGraph();
        }
    }

    /** Create the frame. */
    private StatisticsFrame() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 450, 300);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        rootNode = new PacketNode(Packet.class, null);

        packetTreeModel = new DefaultTreeModel(rootNode, true);

        packetType = new JTree();
        packetType.addTreeSelectionListener(this);
        contentPane.add(packetType, BorderLayout.WEST);

        packetGraph = new JPanel();// Temp!
        contentPane.add(packetGraph, BorderLayout.CENTER);
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {

    }

    private PacketNode getPacketNode(Class<? extends Packet> clazz) {
        if (!nodes.containsKey(clazz)) {
            PacketNode parentNode = null;
            if (clazz == Packet.class) {
                return rootNode;
            } else {
                Class<?> parentClass = clazz.getSuperclass();
                parentNode = getPacketNode((Class<? extends Packet>) parentClass);
            }
            nodes.put(clazz, new PacketNode(clazz, parentNode));
        }
        return nodes.get(clazz);
    }

    private InfoNode getInfoNode(PacketSource source) {
        if (!infoNodes.containsKey(source)) {
            infoNodes.put(source, new InfoNode(source.extraInfo, getPacketNode(source.clazz)));
        }
        return infoNodes.get(source);
    }

    private void updateGraph() {
        for (PacketSource source : ChannelHandlerStats.packetMap.keySet()) {
            Class<? extends Packet> clazz = source.clazz;
            PacketNode node = getPacketNode(clazz);
            InfoNode info = getInfoNode(source);
        }
    }

    private static abstract class BaseNode implements TreeNode {
        private final boolean leaf;
        private final BaseNode parent;
        private final List<BaseNode> children;

        protected BaseNode(boolean leaf, BaseNode parent) {
            this.leaf = leaf;
            this.parent = parent;
            this.children = Lists.newArrayList();
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
            return leaf;
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
    }

    private static class PacketNode extends BaseNode {
        final String clazz;

        protected PacketNode(Class<? extends Packet> clazz, PacketNode parent) {
            super(false, parent);
            this.clazz = clazz.getName();
        }

        @Override
        public String toString() {
            return clazz;
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
            return info;
        }
    }
}
