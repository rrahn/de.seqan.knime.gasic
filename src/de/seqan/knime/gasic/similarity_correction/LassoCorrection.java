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
package de.seqan.knime.gasic.similarity_correction;

import java.util.Arrays;

import org.ejml.simple.SimpleMatrix;

import com.cureos.numerics.Calcfc;
import com.cureos.numerics.Cobyla;

/**
 * Computes the corrected abundance matrix for a given similarity matrix and
 * read counts.
 * 
 * @author aiche
 */
public class LassoCorrection {

	// initial value taken from scipy version and GASiC source code
	private static final double rhobeg = 1.0;
	private static final double rhoend = 1.0e-10;
	private static final int iprint = 1;
	private static final int maxfun = 10000;

	/**
	 * Calculate corrected abundances given a similarity matrix and observations
	 * using optimization.
	 * 
	 * @param similarity
	 *            Matrix with pairwise similarities between species.
	 * @param normalizedReadAbundances
	 *            Vector of read counts per species (normalized).
	 * @return Estimated abundance of each species in the sample.
	 */
	public static double[] similarityCorrection(double[][] similarity,
			double[] normalizedReadAbundances) {

		final SimpleMatrix sm = new SimpleMatrix(similarity);
		final SimpleMatrix reads = new SimpleMatrix(sm.numRows(), 1, true,
				normalizedReadAbundances);

		// compute total number of reads
		final int numGenoms = reads.numRows();

		// 1 constraint for each read (non-negative) and total sum <= 1
		final int numConstraints = numGenoms + 1;

		// normalize reads by its total number

		// solve the lasso problem
		Calcfc calcfc = new CobylaObjective(sm, reads);

		// initial guess -> 0.5 for all
		double[] abbundanceValue = new double[numGenoms];
		double startParameter = 1 / numGenoms;
		Arrays.fill(abbundanceValue, startParameter);

		// do the actual optimization
		Cobyla.FindMinimum(calcfc, numGenoms, numConstraints, abbundanceValue,
				rhobeg, rhoend, iprint, maxfun);

		return abbundanceValue;
	}

}
