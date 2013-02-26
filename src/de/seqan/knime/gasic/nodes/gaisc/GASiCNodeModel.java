package de.seqan.knime.gasic.nodes.gaisc;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.ejml.simple.SimpleMatrix;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
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
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;

import de.seqan.knime.gasic.similarity_correction.LassoCorrection;

/**
 * This is the model implementation of GASiC. Implements the GASiC approach.
 * 
 * @author Stephan Aiche
 */
public class GASiCNodeModel extends NodeModel {
	/**
	 * The portion of the total reads that should be used in each bootstrap
	 * iteration.
	 */
	private static double SUBSET_QUANT = 0.6;

	static String CFG_RHO_BEG = "rho_beg";
	static double DEFAULT_RHO_BEG = 1.0;

	private final SettingsModelDoubleBounded m_rhobeg = new SettingsModelDoubleBounded(
			CFG_RHO_BEG, DEFAULT_RHO_BEG, Double.MIN_VALUE, 10.0);
	// //////////
	static String CFG_RHO_END = "rho_end";
	static double DEFAULT_RHO_END = 1.0e-10;

	private final SettingsModelDoubleBounded m_rhoend = new SettingsModelDoubleBounded(
			CFG_RHO_END, DEFAULT_RHO_END, Double.MIN_VALUE, 10.0);

	// //////////
	static String CFG_MAX_ITERATIONS = "max_iterations";
	static int DEFAULT_MAX_ITERATIONS = 10000;

	private final SettingsModelIntegerBounded m_max_iter = new SettingsModelIntegerBounded(
			CFG_MAX_ITERATIONS, DEFAULT_MAX_ITERATIONS, 1, Integer.MAX_VALUE);

	// //////////
	static String CFG_NUM_THREADS = "num_threads";
	// we want at least two threads (except when we have only one at max)
	static int DEFAULT_NUM_THREADS = (Runtime.getRuntime()
			.availableProcessors() == 1 ? 1 : 2);

	private final SettingsModelIntegerBounded m_num_threads = new SettingsModelIntegerBounded(
			CFG_NUM_THREADS, DEFAULT_NUM_THREADS, 1, Runtime.getRuntime()
					.availableProcessors());

	// //////////
	static final int DEFAULT_NUM_BOOSTRAP = 5;
	static final String CFG_NUM_BOOSTRAP = "num_boostrap";

	private final SettingsModelInteger m_num_boostrap = new SettingsModelInteger(
			CFG_NUM_BOOSTRAP, DEFAULT_NUM_BOOSTRAP);

	// //////////
	static final double DEFAULT_TEST_LEVEL = 0.1;
	static final String CFG_TEST_LEVEL = "num_boostrap";

	private final SettingsModelDouble m_test_level = new SettingsModelDouble(
			CFG_TEST_LEVEL, DEFAULT_TEST_LEVEL);

	// the logger instance
	private static final NodeLogger logger = NodeLogger
			.getLogger(GASiCNodeModel.class);

	/**
	 * Constructor for the node model.
	 */
	protected GASiCNodeModel() {
		super(2, 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {

		final int numGenomes = getNumberOfGenomes(new DataTableSpec[] {
				inData[0].getDataTableSpec(), inData[1].getDataTableSpec() });

		final int numReads = inData[0].getRowCount();
		final int numSimReads = inData[1].getRowCount() / numGenomes;

		String[] names = getGenomeNames(inData[0].getDataTableSpec(),
				numGenomes);

		double[][] correct = new double[m_num_boostrap.getIntValue()][numGenomes];
		double[][] fail = new double[m_num_boostrap.getIntValue()][numGenomes];

		LassoCorrection lc = new LassoCorrection(0,
				m_num_threads.getIntValue(), m_rhobeg.getDoubleValue(),
				m_rhoend.getDoubleValue(), m_max_iter.getIntValue());

		for (int i = 0; i < m_num_boostrap.getIntValue(); ++i) {

			SimpleMatrix reads = sampleNormalizedReadVector(inData[0],
					numReads, numGenomes);
			SimpleMatrix sm = sampleSimilarityMatrix(inData[1], numSimReads,
					numGenomes);
			// sm.transpose();

			logger.info("Similartiy matrix in iteration " + i + ": "
					+ sm.toString());

			correct[i] = lc.similarityCorrection(sm, reads);
			fail[i] = new double[numGenomes];
			Arrays.fill(fail[i], 0.0);

			exec.setProgress((double) i / m_num_boostrap.getIntValue());
			exec.checkCanceled();
		}

		// write to output table
		BufferedDataContainer container = exec
				.createDataContainer(createOutputSpec());
		exec.setMessage("Creating output");

		int[] mapped_reads = getMappedReads(inData[0], numGenomes);
		double[] avg_correct = mean(correct);
		double[] avg_fails = mean(fail);
		double[] var_correct = var(correct);

		logger.info("Correction values: " + Arrays.toString(avg_correct));

		for (int i = 0; i < numGenomes; ++i) {
			RowKey key = new RowKey("Row " + i);
			DataCell[] cells = new DataCell[5];
			// name
			cells[0] = new StringCell(names[i]);
			// mapped reads
			cells[1] = new IntCell(mapped_reads[i]);
			// corrected
			cells[2] = new DoubleCell(avg_correct[i] * numReads);
			// error
			cells[3] = new DoubleCell(var_correct[i] * numReads);
			// pval
			cells[4] = new DoubleCell(avg_fails[i]);

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

	private String[] getGenomeNames(DataTableSpec dataTableSpec,
			final int numGenomes) {
		String[] names = new String[numGenomes];

		int[] indices = getMappingCols(dataTableSpec, numGenomes);

		for (int i = 0; i < numGenomes; ++i) {
			names[i] = dataTableSpec.getColumnSpec(indices[i]).getName();
		}

		return names;
	}

	private int getNumberOfGenomes(DataTableSpec[] inData)
			throws InvalidSettingsException {

		int num1Genomes = numberOfBoolCols(inData[0]);
		int num2Genomes = numberOfBoolCols(inData[1]);

		if (num1Genomes != num2Genomes) {
			throw new InvalidSettingsException(
					"The number of genomes in the two input tables are not equal.");
		}

		return num1Genomes;
	}

	public int numberOfBoolCols(DataTableSpec spec) {
		int numBoolCols = 0;
		for (DataColumnSpec colSpec : spec) {
			if (colSpec.getType() == BooleanCell.TYPE) {
				numBoolCols++;
			}
		}
		return numBoolCols;
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
		// checks if the number of genomes is correct
		getNumberOfGenomes(inSpecs);
		return new DataTableSpec[] { createOutputSpec() };
	}

	private DataTableSpec createOutputSpec() {
		// {name}\t{mapped}\t{corr}\t{error}\t{pval}

		DataColumnSpec[] allColSpecs = new DataColumnSpec[5];
		allColSpecs[0] = new DataColumnSpecCreator("name", StringCell.TYPE)
				.createSpec();
		allColSpecs[1] = new DataColumnSpecCreator("mapped reads", IntCell.TYPE)
				.createSpec();
		allColSpecs[2] = new DataColumnSpecCreator("corrected", DoubleCell.TYPE)
				.createSpec();
		allColSpecs[3] = new DataColumnSpecCreator("error", DoubleCell.TYPE)
				.createSpec();
		allColSpecs[4] = new DataColumnSpecCreator("pval", DoubleCell.TYPE)
				.createSpec();

		return new DataTableSpec(allColSpecs);
	}

	/**
	 * 
	 * @param source
	 * @param numReads
	 * @return
	 */
	private SimpleMatrix sampleNormalizedReadVector(
			final BufferedDataTable source, final int numReads,
			final int numGenomes) {

		int[] counts = getMappedReads(source, numGenomes, true);

		SimpleMatrix normalizedReads = new SimpleMatrix(numGenomes, 1);

		for (int i = 0; i < numGenomes; ++i) {
			normalizedReads.set(i, 0, (double) counts[i] / (double) numReads);
		}

		return normalizedReads;
	}

	public int[] getMappedReads(final BufferedDataTable source,
			final int numGenomes, boolean bootstrap) {
		int[] boolCols = getMappingCols(source.getDataTableSpec(), numGenomes);
		int[] counts = new int[numGenomes];
		Arrays.fill(counts, 0);

		Set<Integer> rndSet = getRandomSet(source.getRowCount(),
				(int) (source.getRowCount() * SUBSET_QUANT));

		int r = 0;
		for (DataRow row : source) {
			if (!bootstrap || rndSet.contains(r)) {
				for (int i = 0; i < numGenomes; ++i) {
					counts[i] += ((BooleanCell) row.getCell(boolCols[i]))
							.getIntValue();
				}
			}
			++r;
		}
		return counts;
	}

	public int[] getMappedReads(final BufferedDataTable source,
			final int numGenomes) {
		return getMappedReads(source, numGenomes, false);
	}

	private int[] getMappingCols(DataTableSpec dataTableSpec,
			final int numGenomes) {
		int[] boolCols = new int[numGenomes];
		int c = 0;
		for (int i = 0; i < dataTableSpec.getNumColumns(); ++i) {
			if (dataTableSpec.getColumnSpec(i).getType() == BooleanCell.TYPE) {
				boolCols[c++] = i;
			}
		}
		return boolCols;
	}

	/**
	 * 
	 * @param N
	 * @param M
	 * @return
	 */
	private Set<Integer> getRandomSet(final int N, final int M) {
		logger.debug(String.format(
				"Generating random subset of size M=%d in the interval [0,%d)",
				M, N));
		Set<Integer> rndSet = new TreeSet<Integer>();
		Random rndGen = new Random();
		while (rndSet.size() < M) {
			rndSet.add(rndGen.nextInt(N));
		}
		return rndSet;
	}

	/**
	 * 
	 * @param source
	 * @param numReads
	 * @return
	 * @throws Exception
	 */
	private SimpleMatrix sampleSimilarityMatrix(final BufferedDataTable source,
			final int numReads, final int numGenomes) throws Exception {
		int[] boolCols = getMappingCols(source.getDataTableSpec(), numGenomes);

		int[][] counts = getSimilartiyCountMatrix(source, numGenomes, boolCols);

		// normalize
		SimpleMatrix sm = new SimpleMatrix(numGenomes, numGenomes);

		for (int i = 0; i < numGenomes; ++i) {
			for (int j = 0; j < numGenomes; ++j) {
				sm.set(i, j, (double) counts[i][j] / (double) counts[i][i]);
			}
		}

		return sm;
	}

	public int[][] getSimilartiyCountMatrix(final BufferedDataTable source,
			final int numGenomes, int[] boolCols) throws Exception {
		Iterator<DataRow> rowIterator = source.iterator();
		String currentGenome = "";
		int[][] counts = new int[numGenomes][numGenomes];
		int currentGenomeIdx = -1;

		int numReadsPerGenome = source.getRowCount() / numGenomes;
		Set<Integer> subSet = new TreeSet<Integer>();
		int r = 0;
		Iterator<Integer> selectedRowIt = subSet.iterator();
		int nextRow = -1;

		while (currentGenomeIdx < numGenomes) {
			if (!rowIterator.hasNext())
				break;

			DataRow row = rowIterator.next();

			// we have a new genome
			if (!((StringCell) row.getCell(0)).getStringValue().equals(
					currentGenome)) {
				++currentGenomeIdx;
				currentGenome = ((StringCell) row.getCell(0)).getStringValue();

				// reset the sampling variables
				r = 0;
				subSet = getRandomSet(numReadsPerGenome,
						(int) (numReadsPerGenome * SUBSET_QUANT));
				selectedRowIt = subSet.iterator();
				nextRow = selectedRowIt.next();
			}

			if (currentGenomeIdx >= numGenomes) {
				throw new Exception(
						"Invalid input data: The input data contains more genomes in its rows then in its columns.");
			}

			if (r == nextRow) {
				// logger.info(String.format(
				// "Adding row %d of genome %d to similarity matrix",
				// nextRow, currentGenomeIdx));
				for (int g = 0; g < numGenomes; ++g) {
					counts[currentGenomeIdx][g] += ((BooleanCell) row
							.getCell(boolCols[g])).getIntValue();
				}

				if (selectedRowIt.hasNext()) {
					nextRow = selectedRowIt.next();
				} else {
					nextRow = -1;
				}
			}
			++r;
		}
		return counts;
	}

	private double[] mean(double[][] input) {
		double[] mean = new double[input[0].length];

		for (int i = 0; i < input.length; ++i) {
			for (int j = 0; j < mean.length; ++j) {
				mean[j] += input[i][j];
			}
		}

		for (int j = 0; j < mean.length; ++j) {
			mean[j] /= input.length;
		}

		return mean;
	}

	private double[] var(double[][] input) {
		double[] mean = mean(input);
		double[] var = new double[mean.length];

		for (int i = 0; i < input.length; ++i) {
			for (int j = 0; j < var.length; ++j) {
				var[j] += Math.pow((input[i][j] - mean[j]), 2);
			}
		}

		for (int j = 0; j < var.length; ++j) {
			var[j] /= (input.length - 1);
		}

		return var;
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
