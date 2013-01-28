package de.seqan.knime.gasic;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "GASiCLasso" Node.
 * 
 * 
 * @author Stephan Aiche
 */
public class GASiCLassoNodeFactory extends NodeFactory<GASiCLassoNodeModel> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GASiCLassoNodeModel createNodeModel() {
		return new GASiCLassoNodeModel();
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
	public NodeView<GASiCLassoNodeModel> createNodeView(final int viewIndex,
			final GASiCLassoNodeModel nodeModel) {
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
