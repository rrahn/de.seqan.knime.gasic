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
package de.seqan.knime.gasic.nodes.gasic;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;

/**
 * <code>NodeDialog</code> for the "GASiC" Node. Implements the GASiC approach.
 * 
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Stephan Aiche
 */
public class GASiCNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring GASiC node dialog. This is just a suggestion to
     * demonstrate possible default dialog components.
     */
    protected GASiCNodeDialog() {
        super();

        createNewGroup("Cobyla Options:");
        addDialogComponent(new DialogComponentNumber(
                new SettingsModelDoubleBounded(GASiCNodeModel.CFG_RHO_BEG,
                        GASiCNodeModel.DEFAULT_RHO_BEG, Double.MIN_VALUE, 10.0),
                "Rho Begin:", /* step */1, /* componentwidth */
                5));

        addDialogComponent(new DialogComponentNumber(
                new SettingsModelDoubleBounded(GASiCNodeModel.CFG_RHO_END,
                        GASiCNodeModel.DEFAULT_RHO_END, Double.MIN_VALUE, 10.0),
                "Rho End:", /* step */0.0001, /* componentwidth */
                5));

        addDialogComponent(new DialogComponentNumber(
                new SettingsModelIntegerBounded(
                        GASiCNodeModel.CFG_MAX_ITERATIONS,
                        GASiCNodeModel.DEFAULT_MAX_ITERATIONS, 1,
                        Integer.MAX_VALUE), "Max. Iterations:", /* step */1, /* componentwidth */
                5));

        createNewGroup("Bootstrapping Options:");
        addDialogComponent(new DialogComponentNumber(new SettingsModelInteger(
                GASiCNodeModel.CFG_NUM_BOOSTRAP,
                GASiCNodeModel.DEFAULT_NUM_BOOSTRAP),
                "Number of bootstrap samples:", 100));

        createNewGroup("p-Value options:");
        addDialogComponent(new DialogComponentNumber(new SettingsModelDouble(
                GASiCNodeModel.CFG_TEST_LEVEL,
                GASiCNodeModel.DEFAULT_TEST_LEVEL),
                "Min. abundance to count species as present", 0.01));

        createNewGroup("Mutlithreading Options:");
        addDialogComponent(new DialogComponentNumber(
                new SettingsModelIntegerBounded(GASiCNodeModel.CFG_NUM_THREADS,
                        GASiCNodeModel.DEFAULT_NUM_THREADS, 1, Runtime
                                .getRuntime().availableProcessors()),
                "Number of Threads:", /* step */1, /* componentwidth */
                5));
    }
}
