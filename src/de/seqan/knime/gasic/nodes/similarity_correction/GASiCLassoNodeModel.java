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
package de.seqan.knime.gasic.nodes.similarity_correction;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.ejml.simple.SimpleMatrix;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
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
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;

import de.seqan.knime.gasic.similarity_correction.LassoCorrection;

/**
 * This is the model implementation of GASiCLasso.
 * 
 * 
 * @author Stephan Aiche
 */
public class GASiCLassoNodeModel extends NodeModel {

	static String CFG_RHO_BEG = "rho_beg";
	static double DEFAULT_RHO_BEG = 1.0;

	private final SettingsModelDoubleBounded m_rhobeg = new SettingsModelDoubleBounded(
			CFG_RHO_BEG, DEFAULT_RHO_BEG, Double.MIN_VALUE, 10.0);

	static String CFG_RHO_END = "rho_end";
	static double DEFAULT_RHO_END = 1.0e-10;

	private final SettingsModelDoubleBounded m_rhoend = new SettingsModelDoubleBounded(
			CFG_RHO_END, DEFAULT_RHO_END, Double.MIN_VALUE, 10.0);

	static String CFG_MAX_ITERATIONS = "max_iterations";
	static int DEFAULT_MAX_ITERATIONS = 10000;

	private final SettingsModelIntegerBounded m_max_iter = new SettingsModelIntegerBounded(
			CFG_MAX_ITERATIONS, DEFAULT_MAX_ITERATIONS, 1, Integer.MAX_VALUE);

	static String CFG_NUM_THREADS = "num_threads";
	// we want at least two threads (except when we have only one at max)
	static int DEFAULT_NUM_THREADS = (Runtime.getRuntime()
			.availableProcessors() == 1 ? 1 : 2);

	private final SettingsModelIntegerBounded m_num_threads = new SettingsModelIntegerBounded(
			CFG_NUM_THREADS, DEFAULT_NUM_THREADS, 1, Runtime.getRuntime()
					.availableProcessors());

	// the logger instance
	private static final NodeLogger logger = NodeLogger
			.getLogger(GASiCLassoNodeModel.class);

	/**
	 * Constructor for the node model.
	 */
	protected GASiCLassoNodeModel() {
		super(2, 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {

		logger.info("Node Model Stub... this is not yet implemented !");

		// transfer input table 1 to matrix
		SimpleMatrix reads = new SimpleMatrix(inData[0].getRowCount(), 1);
		int r = 0;
		for (DataRow dataRow : inData[0]) {
			reads.set(r++, 0, ((DoubleCell) dataRow.getCell(0)).getRealValue());
		}

		// transfer input table 2 to matrix
		SimpleMatrix similarityMatrix = new SimpleMatrix(
				inData[1].getRowCount(), inData[1].getDataTableSpec()
						.getNumColumns());

		if (similarityMatrix.numCols() != similarityMatrix.numRows()) {
			throw new Exception("The similarity matrix must be quadratic.");
		}

		r = 0;
		for (DataRow dataRow : inData[1]) {
			int c = 0;
			for (DataCell cell : dataRow) {
				similarityMatrix
						.set(r, c++, ((DoubleCell) cell).getRealValue());
			}
			r++;
		}

		// correct lasso
		double[] abbundances = (new LassoCorrection(0,
				m_num_threads.getIntValue(), m_rhobeg.getDoubleValue(),
				m_rhoend.getDoubleValue(), m_max_iter.getIntValue()))
				.similarityCorrection(similarityMatrix, reads);

		logger.debug("Corrected abbundances: " + Arrays.toString(abbundances));

		// write to output table
		BufferedDataContainer container = exec
				.createDataContainer(createOutputSpec());

		for (int i = 0; i < abbundances.length; ++i) {
			RowKey key = new RowKey("Row " + i);
			DataCell[] cells = new DataCell[1];
			cells[0] = new DoubleCell(abbundances[i]);
			DataRow row = new DefaultRow(key, cells);
			container.addRowToTable(row);

			// check if the execution monitor was canceled
			exec.checkCanceled();
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

		// ensure that inSpecs[0] has only one column
		if (inSpecs[0].getNumColumns() != 1) {
			throw new InvalidSettingsException(
					"We currently support only a single abundance vector as input.");
		} else if (inSpecs[0].getColumnSpec(0).getType() != DoubleCell.TYPE) {
			throw new InvalidSettingsException(
					"The input column needs to be a double column.");
		}

		// check if all inSpecs[1] columns are double
		for (int i = 0; i < inSpecs[1].getNumColumns(); ++i) {
			if (inSpecs[1].getColumnSpec(i).getType() != DoubleCell.TYPE) {
				throw new InvalidSettingsException(
						"All columns of the similarity matrix must be of type double.");
			}
		}

		return new DataTableSpec[] { createOutputSpec() };
	}

	private DataTableSpec createOutputSpec() {

		DataColumnSpec[] allColSpecs = new DataColumnSpec[1];
		allColSpecs[0] = new DataColumnSpecCreator("Abbundance",
				DoubleCell.TYPE).createSpec();

		return new DataTableSpec(allColSpecs);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		m_max_iter.saveSettingsTo(settings);
		m_num_threads.saveSettingsTo(settings);
		m_rhobeg.saveSettingsTo(settings);
		m_rhoend.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_max_iter.loadSettingsFrom(settings);
		m_num_threads.loadSettingsFrom(settings);
		m_rhobeg.loadSettingsFrom(settings);
		m_rhoend.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_max_iter.validateSettings(settings);
		m_num_threads.validateSettings(settings);
		m_rhobeg.validateSettings(settings);
		m_rhoend.validateSettings(settings);
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
