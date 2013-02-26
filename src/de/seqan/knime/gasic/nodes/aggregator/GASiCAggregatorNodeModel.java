/**
 * Copyright (c) 2006-2013, Knut Reinert, Freie Universitaet Berlin
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Freie Universitaet Berlin nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.seqan.knime.gasic.nodes.aggregator;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
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
		double[] normalizedCounts = new double[in.getDataTableSpec()
				.getNumColumns()];
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

		// normalize (if requested) by the number of reads/rows
		for (int c = 0; c < in.getDataTableSpec().getNumColumns(); ++c) {
			if (in.getDataTableSpec().getColumnSpec(c).getType() == BooleanCell.TYPE) {
				normalizedCounts[c] = ((double) counts[c])
						/ ((double) in.getRowCount());
			}
		}

		for (int c = 0; c < in.getDataTableSpec().getNumColumns(); ++c) {
			if (in.getDataTableSpec().getColumnSpec(c).getType() == BooleanCell.TYPE) {
				DataCell[] cells = new DataCell[2];
				cells[0] = new IntCell(counts[c]);
				cells[1] = new DoubleCell(normalizedCounts[c]);
				DataRow row = new DefaultRow(in.getDataTableSpec()
						.getColumnSpec(c).getName(), cells);
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
		DataColumnSpec[] allColSpecs = new DataColumnSpec[2];
		allColSpecs[0] = new DataColumnSpecCreator("Count", IntCell.TYPE)
				.createSpec();
		allColSpecs[1] = new DataColumnSpecCreator("Normalized count",
				DoubleCell.TYPE).createSpec();

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
