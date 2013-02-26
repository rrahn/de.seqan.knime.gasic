package de.seqan.knime.gasic.nodes.gaisc;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;

/**
 * <code>NodeDialog</code> for the "GASiC" Node. Implements the GASiC approach.
 * 
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Stephan Aiche
 */
public class GASiCNodeDialog extends DefaultNodeSettingsPane {

	/**
	 * New pane for configuring GASiC node dialog. This is just a suggestion to
	 * demonstrate possible default dialog components.
	 */
	protected GASiCNodeDialog() {
		super();
	}
}
