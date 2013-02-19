package de.seqan.knime.gasic.nodes.listreader;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "ListReader" Node. Reads a list of GASiC
 * mappings into a corresponding table.
 * 
 * @author Stephan Aiche
 */
public class GASiCListReaderNodeFactory extends NodeFactory<GASiCListReaderNodeModel> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GASiCListReaderNodeModel createNodeModel() {
		return new GASiCListReaderNodeModel();
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
	public NodeView<GASiCListReaderNodeModel> createNodeView(final int viewIndex,
			final GASiCListReaderNodeModel nodeModel) {
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
