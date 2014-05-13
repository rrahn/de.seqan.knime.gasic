/**
 * Copyright (c) 2013-2014, Knut Reinert, Freie Uinversitaet Berlin
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
 *     * Neither the name of Knut Reinert or the Freie Universitaet Berlin nor 
 *       the names of its contributors may be used to endorse or promote 
 *       products derived from this software without specific prior written 
 *       permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL KNUT REINERT OR THE FREIE UNIVESITAET  
 * BERLIN BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, 
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 */
package de.seqan.knime.gasic.similarity_correction;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.ejml.simple.SimpleMatrix;
import org.junit.Test;

/**
 * @author aiche
 */
public class LassoCorrectionTest {

    private static final double EPSILON = 0.001;

    private double[][] simpleMatrixTo2DArray(SimpleMatrix sm) {
        double[][] data = new double[sm.numRows()][sm.numCols()];

        for (int i = 0; i < sm.numRows(); ++i) {
            for (int j = 0; j < sm.numCols(); ++j) {
                data[i][j] = sm.get(i, j);
            }
        }
        return data;
    }

    @Test
    public void testSimpleMatrixTo2DArray() {
        double[][] data = { { 1, 2, 3 }, { 4, 5, 6 }, { 7, 8, 9 } };
        SimpleMatrix sm = new SimpleMatrix(data);

        assertEquals(1, sm.get(0, 0), EPSILON);
        assertEquals(2, sm.get(0, 1), EPSILON);
        assertEquals(3, sm.get(0, 2), EPSILON);

        assertEquals(4, sm.get(1, 0), EPSILON);
        assertEquals(5, sm.get(1, 1), EPSILON);
        assertEquals(6, sm.get(1, 2), EPSILON);

        assertEquals(7, sm.get(2, 0), EPSILON);
        assertEquals(8, sm.get(2, 1), EPSILON);
        assertEquals(9, sm.get(2, 2), EPSILON);

        double[][] res = simpleMatrixTo2DArray(sm);

        assertEquals(1, res[0][0], EPSILON);
        assertEquals(2, res[0][1], EPSILON);
        assertEquals(3, res[0][2], EPSILON);

        assertEquals(4, res[1][0], EPSILON);
        assertEquals(5, res[1][1], EPSILON);
        assertEquals(6, res[1][2], EPSILON);

        assertEquals(7, res[2][0], EPSILON);
        assertEquals(8, res[2][1], EPSILON);
        assertEquals(9, res[2][2], EPSILON);

    }

    /**
     * Test method for
     * {@link de.seqan.knime.gasic.similarity_correction.LassoCorrection#similarityCorrection(double[][], double[])}
     * .
     * 
     * @throws IOException
     *             Thrown if loading of matrices fails.
     * @throws URISyntaxException
     *             Thrown if converting the URI to the test data fails.
     */
    @Test
    public void testSimilarityCorrection() throws IOException,
            URISyntaxException {

        final int numReads = 100000;

        String inputFilename = LassoCorrectionTest.class
                .getResource("input.txt").toURI().getPath();
        String smMatrixFilename = LassoCorrectionTest.class
                .getResource("matrix.txt").toURI().getPath();
        String outputFilename = LassoCorrectionTest.class
                .getResource("output.txt").toURI().getPath();

        SimpleMatrix input = SimpleMatrix.loadCSV(inputFilename);
        SimpleMatrix smMatrix = SimpleMatrix.loadCSV(smMatrixFilename);
        SimpleMatrix output = SimpleMatrix.loadCSV(outputFilename);

        input.print();
        output.print();

        smMatrix.print();

        for (int i = 0; i < input.numCols(); ++i) {
            System.out.println("Test iteration: " + i);

            SimpleMatrix currentInput = input.extractVector(false, i);
            SimpleMatrix currentOutput = output.extractVector(false, i);

            // currentInput.print();
            SimpleMatrix normalizedInput = currentInput.divide(numReads);
            // normalizedInput.print();

            // currentOutput.print();

            double[] corrected = (new LassoCorrection()).similarityCorrection(
                    simpleMatrixTo2DArray(smMatrix),
                    normalizedInput.getMatrix().data);

            // assertArrayEquals(currentOutput.getMatrix().getData(), corrected,
            // EPSILON);

            System.out.println("Start:     "
                    + Arrays.toString(normalizedInput.getMatrix().getData()));
            System.out.println("Result:    " + Arrays.toString(corrected));
            System.out.println("Expected:  "
                    + Arrays.toString(currentOutput.getMatrix().getData()));
            double[] diff = new double[corrected.length];
            for (int d = 0; d < corrected.length; ++d) {
                diff[d] = Math.abs(corrected[d]
                        - currentOutput.getMatrix().getData()[d]);
            }
            System.out.println("Diff:     " + Arrays.toString(diff));
            double ourObj = (new CobylaObjective(smMatrix, normalizedInput))
                    .computeObjectiveValue(corrected);
            double theirObj = (new CobylaObjective(smMatrix, normalizedInput))
                    .computeObjectiveValue(currentOutput.getMatrix().getData());
            System.out.println("Our Obj:   " + ourObj);
            System.out.println("Their Obj: " + theirObj);
            System.out.println("Obj-Diff:  " + (ourObj - theirObj));
            assertEquals(theirObj, ourObj, 0.0001);
        }
    }
}
