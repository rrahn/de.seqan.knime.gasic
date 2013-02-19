package de.seqan.knime.gasic.nodes.listaggregator;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
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
 * This is the model implementation of ListAggregator. Aggregates a list of
 * GASiC mappings into a similarity matrix.
 * 
 * @author Stephan Aiche
 */
public class GASiCListAggregatorNodeModel extends NodeModel {

	// the logger instance
	private static final NodeLogger logger = NodeLogger
			.getLogger(GASiCListAggregatorNodeModel.class);

	private Map<Integer, String> m_colidx_genomes;
	private Map<String, Integer> m_genomes_idx;
	private Map<Integer, Integer> m_idx_colidx;

	/**
	 * Constructor for the node model.
	 */
	protected GASiCListAggregatorNodeModel() {
		super(1, 1);
		m_colidx_genomes = new HashMap<Integer, String>();
		m_genomes_idx = new HashMap<String, Integer>();
		m_idx_colidx = new HashMap<Integer, Integer>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {

		logger.info("Node Model Stub... this is not yet implemented !");

		DataTableSpec outputSpec = createDataTableSpec(inData[0]
				.getDataTableSpec());
		BufferedDataContainer container = exec.createDataContainer(outputSpec);

		int[][] counts = new int[m_colidx_genomes.size()][m_colidx_genomes
				.size()];

		int r = 0;
		for (DataRow row : inData[0]) {
			int genomeIndex = m_genomes_idx.get(((StringCell) row.getCell(0))
					.getStringValue());

			for (int j = 2; j < row.getNumCells(); ++j) {
				counts[genomeIndex][j - 2] += ((BooleanCell) row.getCell(j))
						.getIntValue();

			}

			// check if the execution monitor was canceled
			++r;
			exec.checkCanceled();
			exec.setProgress(r / (double) inData[0].getRowCount(),
					"Processing row " + r);
		}

		// fill the output
		for (int i = 0; i < counts.length; ++i) {
			DoubleCell[] cells = new DoubleCell[counts.length];
			for (int j = 0; j < counts.length; ++j) {
				if (j != i) {
					cells[j] = new DoubleCell((double) counts[i][j]
							/ (double) counts[i][i]);
				}
			}
			cells[i] = new DoubleCell(1.0);
			RowKey rowKey = new RowKey(outputSpec.getColumnNames()[i]);
			DataRow row = new DefaultRow(rowKey, cells);
			container.addRowToTable(row);
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
		if (inSpecs[0].getNumColumns() <= 2) {
			throw new InvalidSettingsException("Invalid input table.");
		}

		// we expect one source-genome column, one read column, and n bool
		// columns in the input spec
		if (inSpecs[0].getColumnSpec(0).getType() != StringCell.TYPE
				|| inSpecs[0].getColumnSpec(1).getType() != StringCell.TYPE) {
			throw new InvalidSettingsException(
					"First two columns are not string columns. Genome-Source and read name column are required.");
		}

		m_colidx_genomes.clear();
		m_genomes_idx.clear();
		m_idx_colidx.clear();

		int currentGenomeIndex = 0;

		for (int i = 2; i < inSpecs[0].getNumColumns(); ++i) {
			if (inSpecs[0].getColumnSpec(i).getType() == BooleanCell.TYPE) {
				m_colidx_genomes.put(i, inSpecs[0].getColumnSpec(i).getName());
				m_genomes_idx.put(inSpecs[0].getColumnSpec(i).getName(),
						currentGenomeIndex);
				m_idx_colidx.put(currentGenomeIndex, i);

				// increase genome index
				currentGenomeIndex++;
			}
		}
		return new DataTableSpec[] { createDataTableSpec(inSpecs[0]) };
	}

	/**
	 * 
	 * @return
	 */
	private DataTableSpec createDataTableSpec(DataTableSpec inSpec) {
		DataColumnSpec[] colSpecs = new DataColumnSpec[m_colidx_genomes.size()];
		int c = 0;
		for (int i = 2; i < inSpec.getNumColumns(); ++i) {
			if (inSpec.getColumnSpec(i).getType() == BooleanCell.TYPE) {
				colSpecs[c++] = new DataColumnSpecCreator(
						m_colidx_genomes.get(i), DoubleCell.TYPE).createSpec();
			}
		}
		DataTableSpec outputSpec = new DataTableSpec(colSpecs);
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
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
	}

}
