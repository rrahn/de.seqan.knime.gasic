package de.seqan.knime.gasic.nodes.aggregator;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/**
 * This is the model implementation of GASiCAggregator. Aggregates the read
 * information into a single column with one line for each species containing
 * the corresponding read count.
 * 
 * @author Stephan Aiche
 */
public class GASiCAggregatorNodeModel extends NodeModel {

	// the logger instance
	@SuppressWarnings("unused")
	private static final NodeLogger logger = NodeLogger
			.getLogger(GASiCAggregatorNodeModel.class);

	private Map<Integer, String> m_genomes;

	/**
	 * Constructor for the node model.
	 */
	protected GASiCAggregatorNodeModel() {
		super(1, 1);
		m_genomes = new HashMap<Integer, String>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {

		BufferedDataContainer container = exec
				.createDataContainer(createDataTableSpec());

		BufferedDataTable in = inData[0];

		int[] counts = new int[in.getDataTableSpec().getNumColumns()];
		int i = 0;
		for (DataRow row : in) {
			// we know which cells are boolean
			for (Integer c : m_genomes.keySet()) {
				counts[c] += ((BooleanCell) row.getCell(c)).getIntValue();
			}

			// check if the execution monitor was canceled
			exec.checkCanceled();
			exec.setProgress(i / (double) in.getRowCount(), "Processing row "
					+ i);
		}

		for (int c = 0; c < in.getDataTableSpec().getNumColumns(); ++c) {
			if (in.getDataTableSpec().getColumnSpec(c).getType() == BooleanCell.TYPE) {
				DataRow row = new DefaultRow(in.getDataTableSpec()
						.getColumnSpec(c).getName(), new IntCell(counts[c]));
				container.addRowToTable(row);
			}
		}

		// once we are done, we close the container and return its table
		container.close();
		BufferedDataTable out = container.getTable();
		return new BufferedDataTable[] { out };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
			throws InvalidSettingsException {

		// we expect one read column and n bool columns in the input spec
		if (inSpecs[0].getColumnSpec(0).getType() != StringCell.TYPE) {
			throw new InvalidSettingsException(
					"First column is not a string column.");
		}

		m_genomes.clear();

		for (int i = 1; i < inSpecs[0].getNumColumns(); ++i) {
			if (inSpecs[0].getColumnSpec(i).getType() == BooleanCell.TYPE) {
				m_genomes.put(i, inSpecs[0].getColumnSpec(i).getName());
			}
		}

		return new DataTableSpec[] { createDataTableSpec() };
	}

	/**
	 * 
	 * @return
	 */
	private DataTableSpec createDataTableSpec() {
		DataColumnSpec[] allColSpecs = new DataColumnSpec[1];
		allColSpecs[0] = new DataColumnSpecCreator("Count", IntCell.TYPE)
				.createSpec();
		DataTableSpec outputSpec = new DataTableSpec(allColSpecs);
		return outputSpec;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {

		// TODO load internal data.
		// Everything handed to output ports is loaded automatically (data
		// returned by the execute method, models loaded in loadModelContent,
		// and user settings set through loadSettingsFrom - is all taken care
		// of). Load here only the other internals that need to be restored
		// (e.g. data used by the views).

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {

		// TODO save internal models.
		// Everything written to output ports is saved automatically (data
		// returned by the execute method, models saved in the saveModelContent,
		// and user settings saved through saveSettingsTo - is all taken care
		// of). Save here only the other internals that need to be preserved
		// (e.g. data used by the views).

	}

}
