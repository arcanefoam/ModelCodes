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
package org.eclipse.epsilon.emg.dt.editor;

import org.eclipse.epsilon.common.module.IModule;
import org.eclipse.epsilon.emg.EmgModule;
import org.eclipse.epsilon.epl.dt.editor.EplEditor;

public class EmgEditor extends EplEditor {
	@Override
	public IModule createModule(){
		return new EmgModule();
	}
}
