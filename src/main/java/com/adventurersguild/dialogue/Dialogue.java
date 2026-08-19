package com.adventurersguild.dialogue;

import java.util.LinkedHashMap;
import java.util.Map;

/** Data-driven dialogue definition (TASK-010). */
public class Dialogue {
    private final String id;
    private final String npc;
    private final String startNode;
    private final Map<String, DialogueNode> nodes;

    public Dialogue(String id, String npc, String startNode, Map<String, DialogueNode> nodes) {
        this.id = id;
        this.npc = npc;
        this.startNode = startNode;
        this.nodes = nodes;
    }

    public String getId() { return id; }
    public String getNpc() { return npc; }
    public String getStartNode() { return startNode; }
    public Map<String, DialogueNode> getNodes() { return nodes; }

    public DialogueNode getNode(String nodeId) {
        return nodes.get(nodeId);
    }

    public static Map<String, DialogueNode> newNodeMap() {
        return new LinkedHashMap<>();
    }
}
