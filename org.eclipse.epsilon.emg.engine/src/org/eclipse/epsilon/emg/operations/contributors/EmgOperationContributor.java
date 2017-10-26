/*******************************************************************************
 * Copyright (c) 2012 The University of York.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dimitrios Kolovos - initial API and implementation
 *     Saheed Popoola - additional functionality
 *     Horacio Hoyos - additional functionality
 ******************************************************************************/
package org.eclipse.epsilon.emg.operations.contributors;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.epsilon.emg.EmgModule;
import org.eclipse.epsilon.emg.random.EmgRandomGenerator;
import org.eclipse.epsilon.emg.random.IEmgRandomGenerator;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.execute.operations.contributors.OperationContributor;


/**
 * The Class ObjectOperationContributor delegates all the random generating
 * functions to a EmgRandomGenerator but overloads the nextXXXList operations
 * to allow the user to specify @list annotations values as listIDs.
 */
public class EmgOperationContributor extends OperationContributor implements IEmgRandomGenerator {

    /** The delegate. */
    private final EmgRandomGenerator delegate;

    /** The module. */
    private final EmgModule module;
    
    /** The list samples. */
    private Map<String, List<Integer>> createListSampleIndices;

    /**
     * Instantiates a new emg operation contributor.
     *
     * @param module the module
     */
    public EmgOperationContributor(EmgModule module) {
        delegate = new EmgRandomGenerator(module.getContext());
        this.module = module;
    }

    /**
     * Instantiates a new emg operation contributor.
     *
     * @param module the module
     * @param seed the seed
     */
    public EmgOperationContributor(EmgModule module, long seed) {
        delegate = new EmgRandomGenerator(module.getContext(), seed);
        this.module = module;
    }

    @Override
    public boolean contributesTo(Object target) {
        return target instanceof Object;
    }

    /**
     * Gets the elements from create rules with the @list annotation with the
     * given name parameter.
     *
     * @param name the name
     * @return the named list
     */
    public Collection<Object> getNamedListValues(String name) {
        return module.getNamedCreatedObjects().get(name);
    }
    
    @Override
    public boolean isFromListAsSampleRefill() {
		return delegate.isFromListAsSampleRefill();
	}

    @Override
    public List<Integer> nextAddTo(int n, int m) throws EolRuntimeException {
        return delegate.nextAddTo(n, m);
    }

    @Override
    public boolean nextBoolean() {
        return delegate.nextBoolean();
    }

    @Override
    public void nextBytes(byte[] bytes) {
        delegate.nextBytes(bytes);
    }
    
    @Override
    public String nextCamelCaseWords(String characterSet, int length, int minWordLength) throws EolRuntimeException {
        return delegate.nextCamelCaseWords(characterSet, length, minWordLength);
    }
    
    @Override
    public String nextCapitalisedWord(String charSet, int length) throws EolRuntimeException {
        return delegate.nextCapitalisedWord(charSet, length);
    }
    
    @Override
    public Object nextFromCollection(Collection<?> c) throws EolRuntimeException {
        return delegate.nextFromCollection(c);
    }
    
    @Override
    public Object nextFromList(String listID) throws EolRuntimeException {
        Collection<Object> existing = getNamedListValues(listID);
        if (existing == null) {
            return delegate.nextFromList(listID);
        }
        else {
            return delegate.nextFromCollection(existing);
        }
    }
    
    @Override
    public Object nextFromListAsSample(String listID) throws EolRuntimeException {
        List<Object> existing = (List<Object>) getNamedListValues(listID);
        if (existing == null) {
            return delegate.nextFromListAsSample(listID);
        }
        else {
        	List<Integer> sampleIndices = getCreateListSampleIndices().get(listID);
        	if (sampleIndices == null) {
        		sampleIndices = delegate.createListSampleIndices(existing.size());
        		getCreateListSampleIndices().put(listID, sampleIndices);
        	}	
            Object result = null;
            int index = 0;
    		try {
                index = sampleIndices.remove(0);
            } catch (IndexOutOfBoundsException e) {
            	if (isFromListAsSampleRefill()) {
            		sampleIndices = delegate.createListSampleIndices(existing.size());
            		getCreateListSampleIndices().put(listID, sampleIndices);
            		index = sampleIndices.remove(0);
            	}
            	else {
            		throw new EolRuntimeException("No more elements to sample from the list, " + listID);
            	}
            }
            result = existing.get(index);
            return result;
        }
    }

	@Override
    public float nextGaussian() {
		return delegate.nextGaussian();
	}
	
    @Override
    public String nextHttpURI(boolean addPort, boolean addPath, boolean addQuery, boolean addFragment)
            throws EolRuntimeException {
        return delegate.nextHttpURI(addPort, addPath, addQuery, addFragment);
    }
    
    @Override
    public int nextInt() {
        return delegate.nextInt();
    }
    
    @Override
    public int nextInt(int n) {
        return delegate.nextInt(n);
    }
    
    @Override
    public int nextInt(int lower, int upper) throws EolRuntimeException {
        return delegate.nextInt(lower, upper);
    }
    
    @Override
    public float nextReal() {
		return delegate.nextReal();
	}
    @Override
    public float nextReal(float n) throws EolRuntimeException {
		return delegate.nextReal(n);
	}
    
    @Override
    public float nextReal(float lower, float upper) throws EolRuntimeException {
		return delegate.nextReal(lower, upper);
	}
    
    @Override
    public List<Object> nextSample(Collection<?> c, int k) throws EolRuntimeException {
        return delegate.nextSample(c, k);
    }
    
    @Override
    public List<Object> nextSample(String listID, int k) throws EolRuntimeException {
        List<Object> existing = (List<Object>) getNamedListValues(listID);
        if (existing == null) {
            return delegate.nextSample(listID, k);
        }
        else {
            return nextSample(existing, k);
        }
    }

    @Override
    public String nextString() {
		return delegate.nextString();
	}

	@Override
	public String nextString(int length) {
		return delegate.nextString(length);
	}
    
    @Override
    public String nextString(String charSet, int length) {
        return delegate.nextString(charSet, length);
    }
    
    @Override
    public String nextURI() throws EolRuntimeException {
        return delegate.nextURI();
    }
    
    @Override
	public String nextURI(boolean addPort, boolean addPath, boolean addQuery, boolean addFragment)
            throws EolRuntimeException {
        return delegate.nextURI(addPort, addPath, addQuery, addFragment);
    }
    
	@Override
	public String nextUUID() {
		return delegate.nextUUID();
	}
    
    
    @Override
	public float nextValue() throws EolRuntimeException {
        return delegate.nextValue();
    }
    
    @Override
	public float nextValue(Distribution distribution, Number[] args)
			throws EolRuntimeException {
		return delegate.nextValue(distribution, args);
	}
    
    @Override
	public void setFromListAsSampleRefill(boolean refill) {
		delegate.setFromListAsSampleRefill(refill);
	}
    
    @Override
	public void setNextValueDistribution(Distribution distribution,
			Number[] args) throws EolRuntimeException {
		delegate.setNextValueDistribution(distribution, args);
	}
    
    @Override
    public void setSeed(int seed) {
        delegate.setSeed(seed);
    }

	@Override
	public void setSeed(int[] seed) {
        delegate.setSeed(seed);
    }

	@Override
    public void setSeed(long seed) {
        delegate.setSeed(seed);
    }

	private Map<String, List<Integer>> getCreateListSampleIndices() {
		if (createListSampleIndices == null) {
			createListSampleIndices = new HashMap<>();
		}
		return createListSampleIndices;
	}

}


