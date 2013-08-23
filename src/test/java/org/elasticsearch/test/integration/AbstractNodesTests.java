package org.elasticsearch.test.integration;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.network.NetworkUtils;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.junit.Before;
import org.junit.BeforeClass;

import java.util.Map;

import static org.elasticsearch.common.collect.Maps.newHashMap;
import static org.elasticsearch.common.settings.ImmutableSettings.Builder.EMPTY_SETTINGS;
import static org.elasticsearch.common.settings.ImmutableSettings.settingsBuilder;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;


public abstract class AbstractNodesTests {
    protected final ESLogger logger = Loggers.getLogger(getClass());

    private static Map<String, Node> nodes = newHashMap();

    private static Map<String, Client> clients = newHashMap();

    private static Settings defaultSettings = ImmutableSettings
            .settingsBuilder()
            .put("cluster.name", "test-cluster-" + NetworkUtils.getLocalAddress().getHostName())
            .build();


    public Node startNode(String id) {
        return buildNode(id).start();
    }

    public Node startNode(String id, Settings.Builder settings) {
        return startNode(id, settings.build());
    }

    public Node startNode(String id, Settings settings) {
        return buildNode(id, settings).start();
    }

    public Node buildNode(String id) {
        return buildNode(id, EMPTY_SETTINGS);
    }

    public Node buildNode(String id, Settings.Builder settings) {
        return buildNode(id, settings.build());
    }

    public Node buildNode(String id, Settings settings) {
        String settingsSource = getClass().getName().replace('.', '/') + ".yml";
        Settings finalSettings = settingsBuilder()
                .loadFromClasspath(settingsSource)
                .put(defaultSettings)
                .put(settings)
                .put("name", id)
                .build();

        if (finalSettings.get("gateway.type") == null) {
            // default to non gateway
            finalSettings = settingsBuilder().put(finalSettings).put("gateway.type", "none").build();
        }
        if (finalSettings.get("cluster.routing.schedule") != null) {
            // decrease the routing schedule so new nodes will be added quickly
            finalSettings = settingsBuilder().put(finalSettings).put("cluster.routing.schedule", "50ms").build();
        }

        Node node = nodeBuilder()
                .settings(finalSettings)
                .build();
        nodes.put(id, node);
        clients.put(id, node.client());
        return node;
    }

    public void closeNode(String id) {
        Client client = clients.remove(id);
        if (client != null) {
            client.close();
        }
        Node node = nodes.remove(id);
        if (node != null) {
            node.close();
        }
    }

    public Node node(String id) {
        return nodes.get(id);
    }

    public Client client(String id) {
        return clients.get(id);
    }

    public void closeAllNodes() {
        for (Client client : clients.values()) {
            client.close();
        }
        clients.clear();
        for (Node node : nodes.values()) {
            node.close();
        }
        nodes.clear();
    }

    private static volatile AbstractNodesTests testInstance; // this test class only works once per JVM

    @BeforeClass
    public static void tearDownOnce() throws Exception {
        synchronized (AbstractNodesTests.class) {
            if (testInstance != null) {
                testInstance.afterClass();
                testInstance.closeAllNodes();
                testInstance = null;
            }
        }
    }

    @Before
    public final void setUp() throws Exception {
        synchronized (AbstractNodesTests.class) {
            if (testInstance == null) {
                testInstance = this;
                testInstance.beforeClass();

            } else {
                assert testInstance.getClass() == this.getClass();
            }
        }
    }

    public Client client() {
        synchronized (AbstractNodesTests.class) {
            if (clients.isEmpty()) {
                return null;
            }
            return clients.values().iterator().next();
        }
    }

    protected void afterClass() throws Exception {
    }

    protected Settings getClassDefaultSettings() {
        return ImmutableSettings.EMPTY;
    }

    protected void beforeClass() throws Exception {
    }
}
