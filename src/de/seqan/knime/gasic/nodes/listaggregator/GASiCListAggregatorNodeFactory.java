package de.seqan.knime.gasic.nodes.listaggregator;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "ListAggregator" Node. Aggregates a list of
 * GASiC mappings into a similarity matrix.
 * 
 * @author Stephan Aiche
 */
public class GASiCListAggregatorNodeFactory extends
		NodeFactory<GASiCListAggregatorNodeModel> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GASiCListAggregatorNodeModel createNodeModel() {
		return new GASiCListAggregatorNodeModel();
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
	public NodeView<GASiCListAggregatorNodeModel> createNodeView(
			final int viewIndex, final GASiCListAggregatorNodeModel nodeModel) {
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
