/*******************************************************************************
 * Copyright (c) 2017 The University of York.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Horacio Hoyos Rodriguez - initial API and implementation
 ******************************************************************************/
package org.eclipse.epsilon.emg.engine.test;

import org.eclipse.epsilon.emg.operation.contributors.test.EmgOperationContributionTest;
import org.eclipse.epsilon.emg.random.test.EmgRandomGeneratorTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;

@RunWith(Suite.class)
@SuiteClasses({EmgTests.class, EmgRandomGeneratorTest.class, EmgOperationContributionTest.class})
public class EmgAcceptanceTestSuite {
	
	public static Test suite() {
		return new JUnit4TestAdapter(EmgAcceptanceTestSuite.class);
	}
	
	

}
