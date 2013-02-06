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

import org.ejml.simple.SimpleMatrix;

import com.cureos.numerics.Calcfc;

/**
 * Objective funtion for the LassoCorrection optimization using Constrained
 * optimization by linear approximation.
 * 
 * @author aiche
 */
public final class CobylaObjective implements Calcfc {
	/**
	 * The similarity matrix
	 */
	private final SimpleMatrix similarityMatrix;
	private final SimpleMatrix reads;

	public CobylaObjective(SimpleMatrix sm, SimpleMatrix reads) {
		similarityMatrix = sm;
		this.reads = reads;
	}

	@Override
	public double Compute(int numVariables, int numConstraints, double[] x,
			double[] con) {

		// non-negative constraints for all variables
		for (int i = 0; i < numVariables; ++i) {
			con[i] = x[i];
		}

		// sum <= 1 constraint
		con[numVariables] = 1.0 - LassoCorrection.sum(x);

		return computeObjectiveValue(x);
	}

	/**
	 * Computes the value of the objective function given the solution x.
	 * 
	 * @param x
	 * @return
	 */
	public double computeObjectiveValue(double[] x) {
		// convert x to SimpleMatrix
		SimpleMatrix smX = new SimpleMatrix(similarityMatrix.numRows(), 1,
				true, x);

		// compute norm
		return norm(similarityMatrix.mult(smX).minus(reads));
	}

	private double norm(final SimpleMatrix sm) {
		// we assume that this is a vector
		double n = 0.0;

		for (int i = 0; i < sm.numRows(); ++i) {
			n += sm.get(i, 0) * sm.get(i, 0);
		}
		return n;
	}
}