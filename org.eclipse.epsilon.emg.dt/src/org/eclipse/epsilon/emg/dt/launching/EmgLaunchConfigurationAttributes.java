/*******************************************************************************
 * Copyright (c) 2012 The University of York.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Dimitrios Kolovos - initial API and implementation
 *     Saheed Popoola - aditional functionality
 *     Horacio Hoyos - aditional functionality
 ******************************************************************************/
package org.eclipse.epsilon.emg.dt.launching;

/**
 * The configuration attributes for the EMG launch configration.
 * 
 * @author Horacio Hoyos
 *
 */
public class EmgLaunchConfigurationAttributes {
	
	/**
	 * The seed to use for the random generator
	 */
	public static final String SEED = "genModelSeed";
	
	/**
	 * If the seed should be used, or if the generation uses a system genereated seed.
	 */
	public static final String USE_SEED = "useSeed";
	
	/**
	 * The default value for the seed (in the configuration dialog)
	 */
	public static final String DEFAULT_SEED = "123456789";
	
	
}
