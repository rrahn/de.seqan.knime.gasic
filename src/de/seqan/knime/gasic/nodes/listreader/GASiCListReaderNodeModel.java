package de.seqan.knime.gasic.nodes.listreader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.uri.IURIPortObject;
import org.knime.core.data.uri.URIContent;
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
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;

/**
 * This is the model implementation of ListReader. Reads a list of GASiC
 * mappings into a corresponding table.
 * 
 * @author Stephan Aiche
 */
public class GASiCListReaderNodeModel extends NodeModel {

    private static final String GENOME_PREFIX = ">";

    /**
     * The TSV separator.
     */
    private static final String SEPARATOR = "\t";

    // the logger instance
    private static final NodeLogger logger = NodeLogger
            .getLogger(GASiCListReaderNodeModel.class);

    /**
     * Static method that provides the incoming {@link PortType}s.
     * 
     * @return The incoming {@link PortType}s of this node.
     */
    private static PortType[] getIncomingPorts() {
        return new PortType[] { IURIPortObject.TYPE };
    }

    /**
     * Static method that provides the outgoing {@link PortType}s.
     * 
     * @return The outgoing {@link PortType}s of this node.
     */
    private static PortType[] getOutgoingPorts() {
        return new PortType[] { new PortType(BufferedDataTable.class) };
    }

    /**
     * Constructor for the node model.
     */
    protected GASiCListReaderNodeModel() {
        super(getIncomingPorts(), getOutgoingPorts());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {
        BufferedReader brReader = null;
        BufferedDataContainer container = null;
        try {
            // check if we do this the first time, to initialize the table only
            // once
            boolean firstRun = true;
            int currentGenome = 0;
            int rowIdx = 1;

            for (URIContent uriContent : ((IURIPortObject) inData[0])
                    .getURIContents()) {

                List<String> genomes = new ArrayList<String>();
                File masicFile = new File(uriContent.getURI());

                // read the data and fill the table
                brReader = new BufferedReader(new InputStreamReader(
                        new FileInputStream(masicFile)));

                // 1st line numGenomes\tnumReads
                String line = brReader.readLine();
                String[] tokens = line.trim().split(SEPARATOR, -1);
                if (tokens.length != 2)
                    throw new Exception(
                            "Invalid masic file. First line should be #Genomes\t#Reads.");

                int numGenomes = Integer.parseInt(tokens[0]);
                int numReads = Integer.parseInt(tokens[1]);

                // read header
                while ((line = brReader.readLine()) != null) {
                    if (!line.startsWith(GENOME_PREFIX))
                        break;
                    genomes.add(line.substring(1).trim());
                }

                if (genomes.size() != numGenomes)
                    throw new Exception("Invalid masic file header. "
                            + numGenomes + " were announced but we found "
                            + genomes.size());

                if (firstRun) {
                    // create table spec and container
                    DataTableSpec outputSpec = new DataTableSpec(
                            createTableSpec(genomes));
                    container = exec.createDataContainer(outputSpec);
                    firstRun = false;
                }

                // put first line into container
                fillRowFromLine(rowIdx++, line, numGenomes, container,
                        genomes.get(currentGenome));

                // fill container
                while ((line = brReader.readLine()) != null) {
                    fillRowFromLine(rowIdx++, line, numGenomes, container,
                            genomes.get(currentGenome));

                    // we update only every 100th read
                    if (rowIdx % 100 == 0) {
                        exec.checkCanceled();
                        exec.setProgress(rowIdx / (double) numReads,
                                "Adding read " + rowIdx);
                    }
                }
                ++currentGenome;
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            throw ex;
        } finally {
            if (brReader != null)
                brReader.close();
            if (container != null)
                container.close();
        }

        BufferedDataTable out = container.getTable();
        return new BufferedDataTable[] { out };
    }

    private void fillRowFromLine(int rowIdx, String line, int numGenomes,
            BufferedDataContainer container, final String sourceGenome) {
        String[] tokens = line.trim().split(SEPARATOR, -1);
        // we skip empty lines
        if (tokens.length == 0)
            return;

        RowKey key = new RowKey("Row " + rowIdx);

        DataCell[] cells = new DataCell[2 + numGenomes];

        // the first is always the read name
        cells[0] = new StringCell(sourceGenome);
        cells[1] = new StringCell(tokens[0]);

        // initialize the row
        for (int i = 0; i < numGenomes; ++i) {
            cells[i + 2] = BooleanCell.FALSE;
        }

        // update those genomes that were mapped
        for (int i = 1; i < tokens.length; ++i) {
            cells[Integer.parseInt(tokens[i]) + 2] = BooleanCell.TRUE;
        }

        DataRow row = new DefaultRow(key, cells);
        container.addRowToTable(row);
    }

    private DataColumnSpec[] createTableSpec(List<String> genomes) {
        DataColumnSpec[] columnsSpecs = new DataColumnSpec[2 + genomes.size()];

        columnsSpecs[0] = new DataColumnSpecCreator("SourceGenome",
                StringCell.TYPE).createSpec();
        columnsSpecs[1] = new DataColumnSpecCreator("Read-Id", StringCell.TYPE)
                .createSpec();

        int i = 2;
        for (String genome : genomes) {
            columnsSpecs[i++] = new DataColumnSpecCreator(genome,
                    BooleanCell.TYPE).createSpec();
        }

        return columnsSpecs;
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
    protected DataTableSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {
        return new DataTableSpec[] { null };
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
