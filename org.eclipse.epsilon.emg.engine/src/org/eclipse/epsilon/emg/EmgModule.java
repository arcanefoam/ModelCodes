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
package org.eclipse.epsilon.emg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.epsilon.emg.operations.contributors.EmgOperationContributor;
import org.eclipse.epsilon.eol.dom.Annotation;
import org.eclipse.epsilon.eol.dom.AnnotationBlock;
import org.eclipse.epsilon.eol.dom.Operation;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.exceptions.models.EolModelElementTypeNotFoundException;
import org.eclipse.epsilon.eol.types.EolModelElementType;
import org.eclipse.epsilon.epl.EplModule;
import org.eclipse.epsilon.epl.execute.PatternMatchModel;

/**
 * The Emg Module is responsible for execution emg scripts. Emg scripts are used to generate models.
 */
public class EmgModule extends EplModule {

    /**
     * Assign the created elements to a list
     */
    private static final String LIST_ID_ANNOTATION = "list";

    /**
     * How many instances must be created
     */
    private static final String NUMBER_OF_INSTANCES_ANNOTATION = "instances";

    /**
     * Parameters to pass for instance craetion
     */
    private static final String PARAMETERS_ANNOTATION = "parameters";

    /**
     * The name of the create operation
     */
    private static final String CREATE_OPERATION = "create";

    /** The random generator */
    private EmgOperationContributor randomGenerator;

    /** The seed used for random generation. */
    private long seed;

    private boolean useSeed;


    /** A maps to keep track of objects created by create operations that
     * us the @name annotation. The key of the map is the value of the
     * annotation.
     */
    private Map<String, List<Object>> namedCreatedObjects= new HashMap<String, List<Object>>();


    /**
     * @param seed the seed to set
     */
    public void setSeed(long seed) {
        this.seed = seed;
    }


    /**
     * @param useSeed the useSeed to set
     */
    public void setUseSeed(boolean useSeed) {
        this.useSeed = useSeed;
    }

    /**
     * @return the namedCreatedObjects
     */
    public Map<String, List<Object>> getNamedCreatedObjects() {
        return namedCreatedObjects;
    }


    /**
     * Initialise the contributors
     */
    private void preload() {
        context.setModule(this);
        if (useSeed) {
            randomGenerator = new EmgOperationContributor(this, seed);
        }
        else {
            randomGenerator = new EmgOperationContributor(this);
        }
        context.getOperationContributorRegistry().add(randomGenerator);
    }

    /* (non-Javadoc)
     * @see org.eclipse.epsilon.epl.EplModule#getMainRule()
     */
    @Override
    public String getMainRule() {
        return "eplModule";

    }

    /* (non-Javadoc)
     * @see org.eclipse.epsilon.epl.EplModule#getImportConfiguration()
     */
    @Override
    public HashMap<String, Class<?>> getImportConfiguration() {
        HashMap<String, Class<?>> importConfiguration = super.getImportConfiguration();
        importConfiguration.put("emg", EmgModule.class);
        return importConfiguration;
    }

    /* (non-Javadoc)
     * @see org.eclipse.epsilon.epl.EplModule#execute()
     */
    @Override
    public Object execute() throws EolRuntimeException {
        preload();
        execute(getPre(), context);
        executeCreateOperations();
        prepareContext(context);
        EmgPatternMatcher patternMatcher = new EmgPatternMatcher(randomGenerator);
        PatternMatchModel matchModel = null;
        try {
            int loops = 1;
            matchModel = patternMatcher.match(this);
            if (repeatWhileMatchesFound) {

                while (!matchModel.allContents().isEmpty()) {
                    if (maxLoops != INFINITE) {
                        if (loops == maxLoops) break;
                    }
                    matchModel = patternMatcher.match(this);
                    loops++;
                }
            }
        }
        catch (Exception ex) {
            EolRuntimeException.propagate(ex);
        }
        execute(getPost(), context);
        context.getModelRepository().getModels().get(0).store();
//        return matchModel;
        // Is the total size more important than the matches?
        return context.getModelRepository().getModels().get(0).allContents().size();
    }

    /**
     * Execute the create operations in the EMG script.
     *
     * @throws EolModelElementTypeNotFoundException the eol model element type not found exception
     * @throws EolRuntimeException If the type to be instantiated can't be found or any of the random functions fails.
     */
    @SuppressWarnings("unchecked")
	protected void executeCreateOperations() throws EolRuntimeException  {

        AnnotationBlock annotationBlock;
        String annotationName;
        String instancesListName;
        List<Object> arguments;
        int numInstances;
        
        for (Operation operation: getOperations()) {
            if (operation.getName().equals(CREATE_OPERATION)) {
                //Get the class that has to be instantiated
                EolModelElementType instancesType = (EolModelElementType) operation.getContextType(context);
                if (!instancesType.isInstantiable()) {
                    continue;
                }
                // Default values
                numInstances = 1;
                instancesListName = "";             
                arguments = Collections.emptyList();
                
                annotationBlock = operation.getAnnotationBlock();
                if (!(annotationBlock==null)){
                    List<Object> annotationValues;
                    for (Annotation annotation:annotationBlock.getAnnotations()){
                        if (!(annotation.hasValue())) {
                            continue;
                        }
                        annotationName = annotation.getName();
                        annotationValues = operation.getAnnotationsValues(annotationName, context);
                        
                        switch (annotationName) {
                        case NUMBER_OF_INSTANCES_ANNOTATION:
                        	if (!annotationValues.isEmpty()) {
                                Object val=annotationValues.get(0);
                                if (val instanceof List) {
                                    List<?> valC = (List<?>) val;
                                    if(valC.size()>1)
                                        numInstances = randomGenerator.nextInt(getInt(valC.get(0)), getInt(valC.get(1)));
                                    else
                                        numInstances= getInt(valC.get(0));
                                }
                                else {
                                    numInstances = getInt( annotationValues.get(0));
                                }
                            }
                            if (numInstances < 0) {
                                numInstances = 0;
                            }
                            break;
                        case LIST_ID_ANNOTATION:
                        	if (!annotationValues.isEmpty()) {
                                instancesListName = (String) annotationValues.get(0);
                            }
                        	break;
                        case PARAMETERS_ANNOTATION:
                        	if (!annotationValues.isEmpty()) {
                        		Object annotationParameters = annotationValues.get(0);
                        		assert annotationParameters instanceof List;
                        		arguments = (List<Object>) annotationParameters;
                            }
                        	break;
                        }                    
                    }//end for loop annotations
                }
                // Create the instances
                createInstances(operation, instancesType, numInstances, instancesListName, arguments);
                
            }

        }//end for loop (operations)
    }
    

    /**
     * @param operation				The "create" operation, will be invoked with the new instance as argument
     * @param instancesType			The type of the new instance
     * @param numInstances			Number of instances to create
     * @param instancesListName		Name of the list where instances are collected, if any
     * @param arguments			The list of arguments used to instantiate the object
     * @return
     * @throws EolRuntimeException
     */
    private void createInstances(Operation operation, EolModelElementType instancesType,
            int numInstances, String instancesListName, List<Object> arguments) throws EolRuntimeException {
    	
    	List<Object> instances = null;
    	if (!instancesListName.isEmpty()) {
            instances = namedCreatedObjects.get(instancesListName);
            if (instances == null) {
                instances = new ArrayList<Object>(numInstances);
            	namedCreatedObjects.put(instancesListName, instances);
            }
        }
        for (int i=0; i<numInstances; i++) {  
            Object modelObject = instancesType.createInstance(arguments);
            operation.execute(modelObject, null, context);
            if (!instancesListName.isEmpty()) {
            	instances.add(modelObject);
            }
        }
    }

    /**
     * Gets the integer representation of the object, either by casting or by parsing it as a String.
     *
     * @param object the object
     * @return the int
     */
    protected int getInt(Object object){
        if(object instanceof Integer)
            return (Integer)object;
        else
            return Integer.parseInt((String) object);
    }

}
