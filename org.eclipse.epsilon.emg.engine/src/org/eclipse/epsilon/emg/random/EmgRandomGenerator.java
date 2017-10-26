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
package org.eclipse.epsilon.emg.random;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.execute.context.IEolContext;

/**
 * The simplest implementation of the RandomAttributeGenerator interface.
 * All attributes are generated as strings. It is the responsibility of the
 * calling class/method to do the appropriate conversion. This classes uses
 * the Apache Commons Math RandomDataGenerator.
 *
 */
public class EmgRandomGenerator implements IEmgRandomGenerator {

    /** The uri scheme. */
    private final String[] URI_SCHEME = {"http", "ssh", "ftp"};

    /** The uri domain. */
    private final String[] URI_DOMAIN = {".com", ".org", ".net", ".int", ".edu", ".gov", ".mil"};

    /** The generator. */
    private final RandomDataGenerator generator = new RandomDataGenerator();

    /** The context. */
    private final IEolContext context;

    /** Be default we use a uniform distribution. */
    private Distribution globalDistribution = Distribution.Uniform;

    /** The lower bound of the distribution is 0. */
    private Number firstArg = 0;

    /** The upper bound of the distribution is 1 so probability tests will work */
    private Number secondArg = 1;

    /** The sample indices for a given list. */
    private Map<String, List<Integer>> listSamplesIndices;
    
    /** The list values for a given list. */
    private Map<String, List<Object>> listValues;
    
    boolean refillListSamples;
    

    /**
     * Instantiates a new emg random generator.
     *
     * @param context the context
     */
    public EmgRandomGenerator(IEolContext context) {
        super();
        this.context = context;
    }

    /**
     * Instantiates a new emg random generator.
     *
     * @param context the context
     * @param seed the seed
     */
    public EmgRandomGenerator(IEolContext context, long seed) {
        this(context);
        this.generator.reSeed(seed);
    }

    public List<Integer> createListSampleIndices(int size) throws EolRuntimeException {
		List<Integer> sampleIndices;
		sampleIndices = new ArrayList<Integer>(size);
		int[] indexArray = null;
		try {
		    indexArray = generator.nextPermutation(size, size);
		} catch (NotStrictlyPositiveException | NumberIsTooLargeException e) {
		    EolRuntimeException.propagate(e);
		}
		for (int i = 0; i < indexArray.length; i++) {
		    sampleIndices.add(indexArray[i]);
		}
		return sampleIndices;
	}

    /**
     * @return the generator
     */
    public RandomDataGenerator getGenerator() {
        return generator;
    }

    @Override
	public boolean isFromListAsSampleRefill() {

		return refillListSamples;
	}

    @Override
    public List<Integer> nextAddTo(int n, int m) throws EolRuntimeException {
        assert n > 1;
        int len = n>1 ? n-1: n;
        int[] index = null;
		try {
        	index = generator.nextPermutation(m, len);
        }
        catch (NumberIsTooLargeException | NotStrictlyPositiveException ex ){
        	EolRuntimeException.propagate(ex);
        }
        List<Integer> values = new ArrayList<>();
        for (int i = 0; i < len; i++) {
            values.add(index[i]);
        }
        values.add(0, 0);
        values.add(m);
        Collections.sort(values);
        List<Integer> result = new ArrayList<>();
        ListIterator<Integer> it = values.listIterator(1);
        while (it.hasNext()) {
            int low = it.previous();
            it.next();
            int high = it.next();
            result.add(high-low);
        }
        return result;
    }

    @Override
    public boolean nextBoolean() {
        return generator.getRandomGenerator().nextBoolean();
    }

    @Override
    public void nextBytes(byte[] bytes) {
         generator.getRandomGenerator().nextBytes(bytes);
    }

    @Override
    public String nextCamelCaseWords(String characterSet, int length, int minWordLength) throws EolRuntimeException {
    	
    	if (minWordLength > length) {
    		throw new EolRuntimeException("Minimum word length can not be greater than length.");
    	}
    	if (!EmgCharacterSet.valueOf(characterSet).isAlpha()) {
    		throw new EolRuntimeException("Character set is non-Alpha.");
    	}
    	double maxNumWords = Math.floor(length/minWordLength);
    	int numWords = nextInt(1, (int) maxNumWords);
    	int free = length - (numWords*minWordLength);
    	List<Integer> extra;
		try {
    		extra = nextAddTo(numWords, free);
    	}
    	catch (EolRuntimeException ex) {
    		Integer[] vals = new Integer[numWords]; 
    		Arrays.fill(vals, 0);
    		vals[0] = free;
    		extra = Arrays.asList(vals);
    	}
    	for (int i = 0; i < extra.size(); i++) {
    		extra.set(i, extra.get(i)+minWordLength);
    	}
		
        StringBuilder sb = new StringBuilder();
		for (Integer size : extra) {
            String word = nextString(characterSet, size);
        	char capital = word.charAt(0);
            if (!Character.isUpperCase(capital)) {
            	if (Character.isLetter(capital)) {
            		capital = Character.toUpperCase(capital);
            	}
            	else {
            		// Making it work for unknown charsets might be impossible...
            		capital = 'A';
            	}
            }
            word = capital + word.substring(1).toLowerCase();
            sb.append(word);
        }
        return sb.toString();
    }

    @Override
    public String nextCapitalisedWord(String charSet, int length) throws EolRuntimeException {
    	if (!EmgCharacterSet.valueOf(charSet).isAlpha()) {
    		throw new EolRuntimeException("Character set is not Alpha (only letters).");
    	}
        String lower = nextString(charSet, length);
        if (length > 1) {
            return Character.toUpperCase(lower.charAt(0)) + lower.substring(1).toLowerCase();
        }
        else {
            return Character.toString(lower.charAt(0)).toUpperCase();
        }
    }

    @Override
    public Object nextFromCollection(Collection<?> c) throws EolRuntimeException {
    	
    	if (c.isEmpty()) {
    		return null;
    	}
        int upper = c.size()-1;
        int index = 0;
        index = nextInt(0, upper);
        Object[] objects = c.toArray();
        return objects[index];
    }

	/**
     * The listID must be the name of a parameter in the launch configuration.
     * The value of the parameter can be either a CSV list of strings or the
     * name of a file. The name of the file should be full path and each line in
     * the file is considered a separate item.
     *
     * @see org.eclipse.epsilon.emg.random.IEmgRandomGenerator#nextFromList(java.lang.String)
     */
    @Override
    public Object nextFromList(String listID) throws EolRuntimeException {
        List<Object> values = getValuesByListId(listID);
    	return (String) nextFromCollection(values);
    }

	/**
     * The listID must be the name of a parameter in the launch configuration.
     * The value of the parameter can be either a CSV list of strings or the
     * name of a file. The name of the file should be full path and each line in
     * the file is considered a separate item.
     *
     * @see org.eclipse.epsilon.emg.random.IEmgRandomGenerator#nextSampleFromList(java.lang.String)
     */
    @Override
    public Object nextFromListAsSample(String listID) throws EolRuntimeException {
    	
    	List<Object> values = getValuesByListId(listID);
    	List<Integer> sampleIndices = getListSampleIndices().get(listID);
    	if (sampleIndices == null) {
    		sampleIndices = createListSampleIndices(values.size());
    		getListSampleIndices().put(listID, sampleIndices);
    	}	
        Object result = null;
        int index = 0;
		try {
            index = sampleIndices.remove(0);
        } catch (IndexOutOfBoundsException e) {
        	if (refillListSamples) {
        		sampleIndices = createListSampleIndices(values.size());
        		getListSampleIndices().put(listID, sampleIndices);
        		index = sampleIndices.remove(0);
        	}
        	else {
        		throw new EolRuntimeException("No more elements to sample from the list, " + listID);
        	}
        }
        result = values.get(index);
        return result;
    }

    @Override
	public float nextGaussian() {

		return (float) generator.nextGaussian(0.0, 1.0);
	}

	@Override
    public String nextHttpURI(boolean addPort, boolean addPath,
            boolean addQuery, boolean addFragment) throws EolRuntimeException {

        StringBuilder sb = new StringBuilder();
        // scheme
        sb.append("http");
        sb.append("://");
        // Host
        sb.append("www.");
        sb.append(nextString("LETTER", nextInt(6, 10)));
        sb.append(getRandomUriDomain());
        if (addPort) {
            sb.append(":");
            sb.append(nextInt(9999));
        }
        sb.append("/");
        if (addPath) {
            for (int i = 0; i < nextInt(1, 4); i++) {
                sb.append(nextString("LETTER_LOWER", nextInt(3, 6)));
                sb.append("/");
            }
        }
        if (addQuery) {
            String separator = "?";
            for (int i = 0; i < nextInt(1, 4); i++) {
                sb.append(separator);
                sb.append(nextString("LETTER_LOWER", nextInt(3, 5)));
                sb.append("=");
                sb.append(nextString("NUMERIC", nextInt(5, 8)));
                separator = "&";
            }
        }
        if (addFragment) {
            sb.append("#");
            sb.append(nextString("ID_SYMBOL", nextInt(1, 15)));
        }
        return sb.toString();
    }

	@Override
    public int nextInt() {
        return generator.getRandomGenerator().nextInt();
    }
    	
    @Override
    public int nextInt(int n) {
        return generator.getRandomGenerator().nextInt(n);
    }


    @Override
	public int nextInt(int lower, int upper) throws EolRuntimeException {
    	
    	try {
            return generator.nextInt(lower, upper);
        } catch (NumberIsTooLargeException e) {
            EolRuntimeException.propagate(e);
        }
		return 0;
	}

    @Override
	public float nextReal() {
    	return generator.getRandomGenerator().nextFloat();
	}

    @Override
	public float nextReal(float n) throws EolRuntimeException {
    	return nextReal(0, n);
	}

    @Override
	public float nextReal(float lower, float upper) throws EolRuntimeException {
    	if(upper < lower) {
            //return generator.getRandomGenerator().nextFloat()*diff + upper;
    		throw new EolRuntimeException("Upper limit must be greater than lower limit.");
        }
    	float diff = upper-lower;
        if (diff == 0) {
            return lower;
        }
        return generator.getRandomGenerator().nextFloat()*diff + lower;
	}

    @Override
    public List<Object> nextSample(Collection<?> c, int k) throws EolRuntimeException {

        List<Object> sample = null;
        try {
            Object[] result = generator.nextSample(c, k);
            sample = new ArrayList<Object>(Arrays.asList(result));
        } catch (NotStrictlyPositiveException | NumberIsTooLargeException e) {
            EolRuntimeException.propagate(e);
        }
        return sample;
    }

    /**
     * The listID must be the name of a parameter in the launch configuration.
     * The value of the parameter can be either a CSV list of strings or the
     * name of a file. The name of the file should be full path and each line in
     * the file is considered a separate item.
     *
     * @see org.eclipse.epsilon.emg.random.IEmgRandomGenerator#nextSample(java.lang.String, int)
     */
    @Override
    public List<Object> nextSample(String listID, int k) throws EolRuntimeException {
        List<Object> valuesList = getValuesByListId(listID);
        List<Object> sample = null;
        try {
            Object[] result = generator.nextSample(valuesList, k);
            sample = new ArrayList<Object>(result.length);
            for (int i=0; i < result.length; i++) {
                sample.add((String) result[i]);
            }
        } catch (NotStrictlyPositiveException | NumberIsTooLargeException e) {
            EolRuntimeException.propagate(e);
        }
        return sample;
    }
   
    @Override
	public String nextString() {
		try {
			return nextString(nextInt(4, 10));
		} catch (EolRuntimeException e) {
			// should not get here
		}
		return "DefaultString";
	}

    @Override
	public String nextString(int length) {
		return nextString("LETTER", length);
	}

    @Override
    public String nextString(String charSet, int length) {

		EmgCharacterSet cSet;
		try {
        	cSet = EmgCharacterSet.valueOf(charSet);
    	}
    	catch (IllegalArgumentException ex) {
    		cSet = EmgCharacterSet.LETTER;
        }
        StringBuilder sb = new StringBuilder();
        char[] chars = cSet.getCharacters();
        for (int i = 0; i < length; i++) {
            sb.append(chars[generator.nextInt(0, chars.length-1)]);
        }
        return sb.toString();
    }

    @Override
    public String nextURI() throws EolRuntimeException {

        return nextURI(nextBoolean(), nextBoolean(), nextBoolean(), nextBoolean());
    }

    @Override
    public String nextURI(boolean addPort, boolean addPath, boolean addQuery, boolean addFragment) throws EolRuntimeException {

        StringBuilder sb = new StringBuilder();
        // scheme
        String uriScheme = getRandomUriScheme();
        sb.append(uriScheme);
        sb.append("://");
        // user:password
        if (!uriScheme.equals("http")) {
            if (nextBoolean()) {
                sb.append(nextString("LETTER_LOWER", nextInt(6, 10)));
                if (nextBoolean()) {
                    sb.append(":");
                    sb.append(generator.nextSecureHexString(nextInt(6, 10)));
                }
                sb.append("@");
            }
        }
        // Host
        sb.append("www.");
        sb.append(nextString("LETTER", nextInt(6, 10)));
        sb.append(getRandomUriDomain());
        if (addPort) {
            sb.append(":");
            sb.append(nextInt(9999));
        }
        sb.append("/");
        if (addPath) {
            for (int i = 0; i < nextInt(1, 4); i++) {
                sb.append(nextString("LETTER_LOWER", nextInt(3, 6)));
                sb.append("/");
            }
        }
        if (addQuery) {
            String separator = "?";
            for (int i = 0; i < nextInt(1, 4); i++) {
                sb.append(separator);
                sb.append(nextString("LETTER_LOWER", nextInt(3, 5)));
                sb.append("=");
                sb.append(nextString("NUMERIC", nextInt(5, 8)));
                separator = "&";
            }
        }
        if (addFragment) {
            sb.append("#");
            sb.append(nextString("ID_SYMBOL", nextInt(1, 15)));
        }
        return sb.toString();
    }
    
    @Override
    public String nextUUID() {
    	return UUID.randomUUID().toString();
    }
    

    @Override
    public float nextValue() throws EolRuntimeException {
    	double result = 0.0;
    	try {
        switch(globalDistribution) {
        case Binomial:
        	result =  generator.nextBinomial(firstArg.intValue(), secondArg.doubleValue());
        	break;
        case Exponential:
        	result =  generator.nextExponential(firstArg.doubleValue());
        	break;
        case Uniform:
        	result = generator.nextUniform(firstArg.doubleValue(), secondArg.doubleValue(), true);
        	break;
		default:
			break;
        }
    	}
    	catch (Exception ex) {
    		EolRuntimeException.propagate(ex);
    		
    	}
        return (float) result;
    }

    @Override
	public float nextValue(Distribution distribution, Number[] args)
			throws EolRuntimeException {
    	
    	double result = 0.0;
    	try {
    	switch(globalDistribution) {
        case Binomial:
        	result =  generator.nextBinomial(args[0].intValue(), args[1].doubleValue());
        	break;
        case Exponential:
        	result =  generator.nextExponential(args[0].doubleValue());
        	break;
        case Uniform:
        	result = generator.nextUniform(args[0].doubleValue(), args[1].doubleValue(), true);
        	break;
		default:
			break;
        }
    	}
    	catch (IndexOutOfBoundsException ex) {
    		EolRuntimeException.propagate(ex);
    	}
    	return (float) result;
	}

    @Override
	public void setFromListAsSampleRefill(boolean refill) {
    	
    	refillListSamples = refill;
	}

	@Override
	public void setNextValueDistribution(Distribution distribution,
			Number[] args) throws EolRuntimeException {
		
		this.globalDistribution = distribution;
		try {
			this.firstArg = args[0];
			this.secondArg = args[1];
		}
		catch (IndexOutOfBoundsException ex) {
			// Pass, the exception will be thrown when creating the value
		}
	}

	@Override
    public void setSeed(int seed) {
        generator.reSeed(seed);
    }

	@Override
    public void setSeed(int[] seed) {
        // the following number is the largest prime that fits in 32 bits (it is 2^32 - 5)
        final long prime = 4294967291l;
        long combined = 0l;
        for (int s : seed) {
            combined = combined * prime + s;
        }
        setSeed(combined);
    }

	@Override
    public void setSeed(long seed) {
        generator.reSeed(seed);
    }

	/**
     * Gets the values from list. If the list has commas it is treated as a CSV
     * and the result is crated by splitting the list. If not, the list
     * is considered a path and each line of the target file is used as an
     * element of the result.
     *
     * @param list the list
     * @return the values from list
     * @throws EolRuntimeException the eol runtime exception
     */
    private List<Object> getListValues(String list) throws EolRuntimeException {
        // TODO We assume URI/paths don't have commas
        String[] values = list.split(",");
        List<Object> valuesList = null;
        if (values.length == 1) {
            // It should be a path
            File file = new File(values[0]);
            if (file.isDirectory()) {
               throw new EolRuntimeException("Path is not a file: " + values[0]);
            }
            if (file.exists()) {
                Scanner s = null;
                try {
                    s = new Scanner(file);
                } catch (FileNotFoundException e) {
                    EolRuntimeException.propagate(e);
                }
                if (s != null) {
                    valuesList = new ArrayList<Object>();
                    while (s.hasNextLine()){
                        valuesList.add(s.nextLine());
                    }
                    s.close();
                }
            }
            else {
            	throw new EolRuntimeException("Path does not exist: " + values[0]);
            }
        }
        else {
            valuesList = Arrays.stream(values).map(Object.class::cast).collect(Collectors.toList());

        }
        return valuesList;
    }

	/**
     * Gets the random uri domain.
     *
     * @return the random uri domain
	 * @throws EolRuntimeException 
     */
    private String getRandomUriDomain() throws EolRuntimeException {
        return (String) nextFromCollection(Arrays.asList(URI_DOMAIN));
    }

	/**
     * Gets the random uri scheme.
     *
     * @return the random uri scheme
	 * @throws EolRuntimeException 
     */
    private String getRandomUriScheme() throws EolRuntimeException {
        return (String) nextFromCollection(Arrays.asList(URI_SCHEME));
    }

	private List<Object> getValuesByListId(String listID) throws EolRuntimeException {
		List<Object> values = getListValues().get(listID);
    	if (values == null) {
    		String list;
			try {
    			list = (String) context.getFrameStack().get(listID).getValue();
    		}
    		catch (NullPointerException ex) {
    			throw new EolRuntimeException(String.format("List $s not found", listID));
    		}
    		values = getListValues(list);
    		getListValues().put(listID, values);
    	}
		return values;
	}

	protected Map<String, List<Integer>> getListSampleIndices() {
		if (listSamplesIndices == null) {
			listSamplesIndices = new HashMap<>();
		}
		return listSamplesIndices;
	}

	protected Map<String, List<Object>> getListValues() {
		if (listValues == null) {
			listValues = new HashMap<>();
		}
		return listValues;
	}
}
