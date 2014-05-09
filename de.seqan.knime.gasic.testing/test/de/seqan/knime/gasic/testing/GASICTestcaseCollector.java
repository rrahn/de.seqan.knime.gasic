/**
 * Copyright (c) 2012, Stephan Aiche.
 */
package de.seqan.knime.gasic.testing;

import org.knime.testing.core.AbstractTestcaseCollector;

/**
 * KNIME TestCaseCollector.
 * 
 * @author aiche
 */
public class GASICTestcaseCollector extends AbstractTestcaseCollector {

    /**
     * C'tor
     */
    public GASICTestcaseCollector() {
    }

    /**
     * C'tor.
     * 
     * @param excludedTestcases
     */
    public GASICTestcaseCollector(Class<?>... excludedTestcases) {
        super(excludedTestcases);
    }

}
