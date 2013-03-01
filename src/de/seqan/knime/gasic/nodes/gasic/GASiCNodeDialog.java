package de.seqan.knime.gasic.nodes.gasic;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;

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

		createNewGroup("Cobyla Options:");
		addDialogComponent(new DialogComponentNumber(
				new SettingsModelDoubleBounded(GASiCNodeModel.CFG_RHO_BEG,
						GASiCNodeModel.DEFAULT_RHO_BEG, Double.MIN_VALUE, 10.0),
				"Rho Begin:", /* step */1, /* componentwidth */
				5));

		addDialogComponent(new DialogComponentNumber(
				new SettingsModelDoubleBounded(GASiCNodeModel.CFG_RHO_END,
						GASiCNodeModel.DEFAULT_RHO_END, Double.MIN_VALUE, 10.0),
				"Rho End:", /* step */0.0001, /* componentwidth */
				5));

		addDialogComponent(new DialogComponentNumber(
				new SettingsModelIntegerBounded(
						GASiCNodeModel.CFG_MAX_ITERATIONS,
						GASiCNodeModel.DEFAULT_MAX_ITERATIONS, 1,
						Integer.MAX_VALUE), "Max. Iterations:", /* step */1, /* componentwidth */
				5));

		createNewGroup("Bootstrapping Options:");
		addDialogComponent(new DialogComponentNumber(new SettingsModelInteger(
				GASiCNodeModel.CFG_NUM_BOOSTRAP,
				GASiCNodeModel.DEFAULT_NUM_BOOSTRAP),
				"Number of bootstrap samples:", 100));

		createNewGroup("p-Value options:");
		addDialogComponent(new DialogComponentNumber(new SettingsModelDouble(
				GASiCNodeModel.CFG_TEST_LEVEL,
				GASiCNodeModel.DEFAULT_TEST_LEVEL),
				"Min. abundance to count species as present", 0.01));

		createNewGroup("Mutlithreading Options:");
		addDialogComponent(new DialogComponentNumber(
				new SettingsModelIntegerBounded(GASiCNodeModel.CFG_NUM_THREADS,
						GASiCNodeModel.DEFAULT_NUM_THREADS, 1, Runtime
								.getRuntime().availableProcessors()),
				"Number of Threads:", /* step */1, /* componentwidth */
				5));
	}
}
