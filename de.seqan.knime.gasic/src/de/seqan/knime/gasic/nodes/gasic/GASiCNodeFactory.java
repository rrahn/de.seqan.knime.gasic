package de.seqan.knime.gasic.nodes.gasic;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "GASiC" Node. Implements the GASiC approach.
 * 
 * @author Stephan Aiche
 */
public class GASiCNodeFactory extends NodeFactory<GASiCNodeModel> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GASiCNodeModel createNodeModel() {
		return new GASiCNodeModel();
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
	public NodeView<GASiCNodeModel> createNodeView(final int viewIndex,
			final GASiCNodeModel nodeModel) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasDialog() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodeDialogPane createNodeDialogPane() {
		return new GASiCNodeDialog();
	}

}
