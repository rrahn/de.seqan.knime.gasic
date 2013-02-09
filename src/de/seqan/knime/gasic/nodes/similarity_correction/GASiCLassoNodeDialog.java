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

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;

/**
 * Dialog exposing the optimization settings.
 * 
 * @author aiche
 */
public class GASiCLassoNodeDialog extends DefaultNodeSettingsPane {

	protected GASiCLassoNodeDialog() {
		super();

		createNewGroup("Cobyla Options:");
		addDialogComponent(new DialogComponentNumber(
				new SettingsModelDoubleBounded(GASiCLassoNodeModel.CFG_RHO_BEG,
						GASiCLassoNodeModel.DEFAULT_RHO_BEG, Double.MIN_VALUE,
						10.0), "Rho Begin:", /* step */1, /* componentwidth */
				5));

		addDialogComponent(new DialogComponentNumber(
				new SettingsModelDoubleBounded(GASiCLassoNodeModel.CFG_RHO_END,
						GASiCLassoNodeModel.DEFAULT_RHO_END, Double.MIN_VALUE,
						10.0), "Rho End:", /* step */0.000001, /* componentwidth */
				5));

		addDialogComponent(new DialogComponentNumber(
				new SettingsModelIntegerBounded(
						GASiCLassoNodeModel.CFG_MAX_ITERATIONS,
						GASiCLassoNodeModel.DEFAULT_MAX_ITERATIONS, 1,
						Integer.MAX_VALUE), "Max. Iterations:", /* step */1, /* componentwidth */
				5));

		createNewGroup("Mutlithreading Options:");
		addDialogComponent(new DialogComponentNumber(
				new SettingsModelIntegerBounded(
						GASiCLassoNodeModel.CFG_NUM_THREADS,
						GASiCLassoNodeModel.DEFAULT_NUM_THREADS, 1, Runtime
								.getRuntime().availableProcessors()),
				"Number of Threads:", /* step */1, /* componentwidth */
				5));
	}
}
