package org.nem.ncc.model.graph;

import org.nem.core.node.*;

/**
 * Graph that contains information about nodes and connections between nodes.
 */
public class GraphBuilder {
	private final Graph graph;
	private final GraphMetaData metaData;

	/**
	 * Creates a new graph builder.
	 */
	public GraphBuilder() {
		this.graph = new Graph();
		this.metaData = new GraphMetaData();
	}

	/**
	 * Creates the graph.
	 *
	 * @return The graph.
	 */
	public GraphViewModel create() {
		return new GraphViewModel(this.graph, this.metaData);
	}

	/**
	 * Adds the neighborhood of a node to the graph.
	 *
	 * @param node The node.
	 * @param neighbors The node's neighbors (null if the node is inactive).
	 */
	public void addToGraph(final Node node, final NodeCollection neighbors) {
		final boolean isActive = null != neighbors;
		this.addNode(node, isActive);
		if (!isActive) {
			return;
		}

		neighbors.getActiveNodes().stream()
				.forEach(peerNode -> {
					this.addNode(peerNode, true);
					this.addEdge(node, peerNode);
				});

		// TODO 20141110 G-B: I've changed it to busy nodes, but I guess that might not be what we want
		neighbors.getBusyNodes().stream()
				.forEach(peerNode -> {
					this.addNode(peerNode, false);
					this.addEdge(node, peerNode);
				});
	}

	private void addNode(final Node node, final boolean isActive) {
		this.graph.addNode(new GraphNode(node));
		this.metaData.add(new GraphNodeMetaData(node, isActive));
	}

	private void addEdge(final Node lhs, final Node rhs) {
		this.graph.addEdge(new GraphEdge(new GraphNode(lhs), new GraphNode(rhs)));
	}
}
