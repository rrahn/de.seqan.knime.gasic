package de.seqan.knime.gasic.nodes.aggregator;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "GASiCAggregator" Node. Aggregates the read
 * information into a single column with one line for each species containing
 * the corresponding read count.
 * 
 * @author Stephan Aiche
 */
public class GASiCAggregatorNodeFactory extends
		NodeFactory<GASiCAggregatorNodeModel> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GASiCAggregatorNodeModel createNodeModel() {
		return new GASiCAggregatorNodeModel();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getNrNodeViews() {
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodeView<GASiCAggregatorNodeModel> createNodeView(
			final int viewIndex, final GASiCAggregatorNodeModel nodeModel) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasDialog() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodeDialogPane createNodeDialogPane() {
		return null;
	}

}
