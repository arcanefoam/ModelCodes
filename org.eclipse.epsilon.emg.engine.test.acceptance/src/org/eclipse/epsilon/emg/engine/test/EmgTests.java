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

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.epsilon.emc.emf.EmfMetaModel;
import org.eclipse.epsilon.emc.emf.EmfModel;
import org.eclipse.epsilon.emg.EmgModule;
import org.eclipse.epsilon.epl.execute.PatternMatchModel;
import org.junit.Test;


public class EmgTests {
	
	protected EmgModule module = null;
	protected PatternMatchModel patternMatchModel = null;
	
	@Test
	public void testEvl() throws Exception {
		module = new EmgModule();
		module.parse(EmgTests.class.getResource("EmfEcoreTest.emg").toURI());
		
		ResourceSet rs = new ResourceSetImpl();
		
		
		
		EmfModel genModel = new EmfModel();
		genModel.setReadOnLoad(false);
		genModel.setStoredOnDisposal(true);
		genModel.setMetamodelUri("http://www.eclipse.org/enf/2002/Ecore");
		genModel.setModelFileUri(null);
		genModel.load();
		
		EmfMetaModel ecoreModel = new EmfMetaModel();
		ecoreModel.setMetamodelUri("http://www.eclipse.org/enf/2002/Ecore");
		
		module.getContext().getModelRepository().addModel(genModel);
		module.getContext().getModelRepository().addModel(ecoreModel);
		patternMatchModel = (PatternMatchModel) module.execute();
		System.out.println(patternMatchModel);
		
	}

}
