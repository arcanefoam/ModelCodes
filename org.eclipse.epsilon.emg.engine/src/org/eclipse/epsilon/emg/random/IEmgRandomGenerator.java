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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;

/**
 * The Random Attribute Generator interface defines the different available methods
 * to generate random data. This can be boolean values, numbers and strings, or
 * more specialised data as names, cities, email addresses, phone numbers, etc.
 * 
 * @author Hoacio Hoyos
 *
 */
public interface IEmgRandomGenerator {
	
	/**
	 * The available distributions form the Apache Commons Random library,
	 * @author Horacio Hoyos
	 */
    public enum Distribution {
        Beta,
        Binomial,
        Cauchy,
        ChiSquare,
        Exponential,
        F,
        Gamma,
        Gaussian,
        HyperGeometric,
        Uniform,
        Pascal,
        Poisson,
        T,
        Weibull,
        Zipf
    }
    
    /**
	 * A CharacterSet that provides a set of commonly used character sets.
	 * @author Horacio Hoyos
	 */
	public enum EmgCharacterSet {
	    ID("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"),
	    NUMERIC("1234567890"),
	    LETTER("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"),
	    LETTER_UPPER("ABCDEFGHIJKLMNOPQRSTUVWXYZ"),
	    LETTER_LOWER("abcdefghijklmnopqrstuvwxyz"),
	    UPPER_NUM("ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"),
	    LOWER_NUM("abcdefghijklmnopqrstuvwxyz1234567890"),
	    ID_SYMBOL("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890~!@#$%^&*()_+-=[]\\{}|;':\"<>?,./"),
	    HEX_LOWER("abcdef1234567890"),
	    HEX_UPPER("ABCDEF1234567890");
		
	    private char[] characters;
	    private int lenght;

	    private EmgCharacterSet(String characters) {
	        this.characters = characters.toCharArray();
	        Arrays.sort(this.characters);
	        this.lenght = characters.length();
	    }

	    public char[] getCharacters() {
	        return characters;
	    }

	    public boolean isAlpha() {
	        for (char c : characters) {
	            if(!Character.isLetter(c)) {
	                return false;
	            }
	        }
	        return true;
	    }
	    
	    public boolean isInSet(char c) {
	    	return Arrays.binarySearch(characters, c) >= 0;
	    }
	    
	    protected int getLenght() {
	        return lenght;
	    }
	    
	}

    /**
     * Return the configuration value of the behaviour of the
     * {@link #nextFromListAsSample(String)}.
     * 
     * @return
     */
    boolean isFromListAsSampleRefill();

    /**
     * Returns a list of <code>n</code> integers who's sum is equal
     * to <code>m</code>.
     * 
     * @param n the number of integers
     * @param m the total sum
     * @return a list of <code>n</code> integers who's sum is equal
     * to <code>m</code>.
     * @throws EolRuntimeException
     */
    List<Integer> nextAddTo(int n, int m) throws EolRuntimeException;

    /**
     * Returns the next pseudorandom, uniformly distributed
     * <code>boolean</code> value from this random number generator's
     * sequence.
     *
     * @return  the next pseudorandom, uniformly distributed
     * <code>boolean</code> value from this random number generator's
     * sequence
     */
    boolean nextBoolean();

    /**
     * Generates random bytes and places them into a user-supplied
     * byte array.  The number of random bytes produced is equal to
     * the length of the byte array.
     *
     * @param bytes the non-null byte array in which to put the
     * random bytes
     */
    void nextBytes(byte[] bytes);

    /**
     * Generates a string of the given <code>length</code>
     * using the specified <code>character set</code> formatted in
     * <a href="https://en.wikipedia.org/wiki/CamelCase"> CameCase</a>
     * format. The words of in the string will have a minimum length
     * of the provided value. Specific implementations must provide
     * additional information on how the characters are picked.
     * 
     * @param characterSet The name of the character set to use
     * @param length the length of the string
     * @param minWordLenght the minimum word length
     *
     * @return the string
     * @throws EolRuntimeException if minWordLength > length or if the characterSet is not Alpha
     */
    String nextCamelCaseWords(String characterSet, int length, int minWordLength) throws EolRuntimeException;

    /**
     * Generates a string of the given <code>length</code>
     * using the specified <code>character set</code> with
     * the first character in upper case.
     *
     * @param characterSet the character set
     * @param length the length of the string
     * @return the string
     * @throws EolRuntimeException if the characterSet is not Alpha
     */
    String nextCapitalisedWord(String characterSet, int length) throws EolRuntimeException;

    /**
     * Returns the next <code>object</code> from the collection, 
     * selected pseudoramdomly using the uniform distribution. If
     * the collection is empty, returns null.
     *
     * @param collection the collection
     * @return the next <code>object</code> from the collection, 
     * selected pseudoramdomly using the uniform distribution. If
     * the collection is empty, returns null.
     * @throws EolRuntimeException 
     */
    Object nextFromCollection(Collection<?> collection) throws EolRuntimeException;

    /**
     * Returns the next <code>object</code> from the list, 
     * selected pseudoramdomly using the uniform distribution. If
     * the collection is empty, returns null.
     * Particular implementations need to define what a valid
     * listID is and how the data associated with it will be
     * retrieved.
     *
     * @param listID the listID
     * @return the next code>object</code> from the list,
     * selected pseudoramdomly using the uniform distribution. If
     * the collection is empty, returns null.
     * 
     * @throws EolRuntimeException if the list is not found
     */
    Object nextFromList(String listID) throws EolRuntimeException;
    
    /**
     * Returns the next <code>object</code> from the list, 
     * selected pseudoramdomly using the uniform distribution.
     * Particular implementations need to define what a valid
     * listID is and how the data associated with it will be
     * retrieved.
     * <p>
     * The list is treated as a sample without replacement,
     * i.e. each call will return a different object of the list.
     * Throws an exception if the list is empty. By default 
     * an exception is thrown if the method is invoked more times
     * than the number of items in the list. This can be
     * configured via {@link #setFromListAsSampleRefill(boolean)}.
     *
     * @param listID the list ID
     * @return the next <code>object</code> from the list, 
     * selected pseudorandomly using the uniform distribution.
     * @throws EolRuntimeException if the list is empty, or if
     * the list is exhausted and {@link #setFromListAsSampleRefill(boolean)}
     * is set to false.
     */
    Object nextFromListAsSample(String listID) throws EolRuntimeException;
    
    /**
     * Returns the next pseudorandom, Gaussian ("normally") distributed
     * <code>float</code> value with mean <code>0.0</code> and standard
     * deviation <code>1.0</code> from this random number generator's sequence.
     *
     * @return  the next pseudorandom, Gaussian ("normally") distributed
     * <code>float</code> value with mean <code>0.0</code> and
     * standard deviation <code>1.0</code> from this random number
     *  generator's sequence
     */
    float nextGaussian();

    /**
     * Generates a random URI that complies to:
     * <code>http://host[:port][/path][?query][#fragment]</code>
     *
     * The host is generated from a random string and uses a top-level domain.
     * The optional parameters will add additional information to the URI.
     * The number of paths are random between 1 and 4.
     * 
     * @param addPort if <code>true</code> a port is added to the URI
     * @param addPath if <code>true</code> additional paths are added to the URI
     * @param addQuery	if <code>true</code> a query is added to the URI
     * @param addFragment if <code>true</code> a fragment is added to the URI
     * @return
     */
    String nextHttpURI(boolean addPort, boolean addPath, boolean addQuery, boolean addFragment) throws EolRuntimeException;

    /**
     * Returns the next pseudorandom, uniformly distributed <code>int</code>
     * value from this random number generator's sequence.
     * All 2<font size="-1"><sup>32</sup></font> possible <tt>int</tt> values
     * should be produced with  (approximately) equal probability.
     *
     * @return the next pseudorandom, uniformly distributed <code>int</code>
     *  value from this random number generator's sequence
     */
    int nextInt();

    /**
     * Returns a pseudorandom, uniformly distributed <tt>int</tt> value
     * between 0 (inclusive) and the specified value (exclusive), drawn from
     * this random number generator's sequence.
     *
     * @param n the bound on the random number to be returned.  Must be
     * positive.
     * @return  a pseudorandom, uniformly distributed <tt>int</tt>
     * value between 0 (inclusive) and n (exclusive).
     * @throws EolRuntimeException  if n is not positive.
     */
    int nextInt(int n) throws EolRuntimeException;

    /**
     * Returns a pseudorandom, uniformly distributed <tt>int</tt> value
     * between lower and upper (endpoints included), drawn from
     * this random generator's sequence.
     * @param lower the lower the bound on the random number to be
     * returned.  Must be positive.
     * @param upper the upper the bound on the random number to be
     * returned.  Must be positive.
     * @return a pseudorandom, uniformly distributed <tt>int</tt> value
     * between lower and upper (endpoints included).
     * @throws EolRuntimeException if lower and upper are not positive,
     * and if if upper < lower.
     */
    int nextInt(int lower, int upper) throws EolRuntimeException;

    /**
     * Returns the next pseudorandom, uniformly distributed <code>float</code>
     * value between <code>0.0</code> and <code>1.0</code> from this random
     * number generator's sequence.
     *
     * @return  the next pseudorandom, uniformly distributed <code>float</code>
     * value between <code>0.0</code> and <code>1.0</code>.
     */
    float nextReal();

    /**
     * Returns the next pseudorandom, uniformly distributed <code>float</code>
     * value between <code>0.0</code> and the specified value (inclusive)
     * from this random number generator's sequence.
     * 
     * @param n the bound on the random number to be returned.  Must be
     * positive.
     * @return
     * @throws EolRuntimeException if n is not positive.
     */
    float nextReal(float n) throws EolRuntimeException;
    
    /**
     * Returns a pseudorandom, uniformly distributed <code>float</code>
     * value between lower and upper (endpoints included),
     * drawn from this random attribute generator's sequence.
     * 
     * @param lower the lower the bound on the random number to be
     * returned.  Must be positive.
     * @param upper the upper the bound on the random number to be
     * returned.  Must be positive.
     * @return a pseudorandom, uniformly distributed <code>float</code>
     * value between lower and upper (endpoints included)
     * @throws EolRuntimeException if lower and upper are not positive,
     * and if if upper < lower.
     */
    float nextReal(float lower, float upper) throws EolRuntimeException;

    /**
     * Returns an array of k objects selected randomly from the Collection c
     * using a uniform distribution.
     * Sampling from c is without replacement; but if c contains identical
     * objects, the sample may include repeats. If all elements of c are
     * distinct, the resulting object collection represents a Simple Random
     * Sample of size k from the elements of c.
     */
    List<Object> nextSample(Collection<?> c, int k) throws EolRuntimeException;

    /**
     * Returns an array of k objects selected randomly from the list using a
     * uniform distribution. Particular implementations need to define
     * what a valid listID is and how the data associated with it will be
     * retrieved.
     *
     * Sampling from the list is without replacement; but if the list contains
     * identical objects, the sample may include repeats. If all elements of the
     * list are distinct, the resulting object collection represents a Simple
     * Random Sample of size k from the elements of c.
     */
    List<Object> nextSample(String listID, int k) throws EolRuntimeException;
    
    /**
     * Returns the next String made up from characters of the
     * {@link EmgCharacterSet#LETTER} character set, pseudorandomly selected with
     * an uniform distribution. The length of the String is between 4 and 10
     * characters.
     *
     * @return  the next String made up from characters of the
     * {@link EmgCharacterSet#LETTER} character set, pseudorandomly selected with
     * an uniform distribution
     */
    String nextString();
    
    /**
     * Returns the next String made up from characters of the
     * {@link EmgCharacterSet#LETTER} character set, pseudorandomly selected with
     * an uniform distribution. The length of the String is equal to the provided
     * length.
     *
     * @param length the length of the string.
     * @return  the next String made up from characters of the
     * {@link EmgCharacterSet#LETTER} character set, pseudorandomly selected with
     * an uniform distribution
     */
    String nextString(int length);
    
    /**
     * Returns the next String made up from characters of the provided character
     * set, pseudorandomly selected with an uniform distribution. The length of
     * the String is equal to the provided length.
     *
     * @param charSet the char set to use
     * @param length the length of the String
     * @return  the next String made up from characters of the provided character
     * set, pseudorandomly selected with an uniform distribution
     */
    String nextString(String charSet, int length);

    /**
     * Generates a random URI. The port, path, query and fragment are added
     * randomly.
     * @return
     */
    String nextURI() throws EolRuntimeException;

    /**
     * Generates a random URI that complies to:
     * scheme:[//[user:password@]host[:port]][/]path[?query][#fragment]
     *
     * The scheme is randomly selected from:  http, ssh and ftp.
     * For ssh and ftp, a user and pasword are randomly generated.
     * The host is generated from a random string and uses a top-level domain.
     * The optional parameters will add additional information to the URI.
     * The number of paths and queries are random between 1 and 4.
     * @param addPort
     * @param addPath
     * @param addQuery
     * @param addFragment
     * @return
     */
    String nextURI(boolean addPort, boolean addPath, boolean addQuery,
            boolean addFragment) throws EolRuntimeException;
    
    /**
     * Returns a type 4 (pseudo randomly generated) UUID. The UUID is generated using
     * a cryptographically strong pseudo random number generator.
     * 
     * @return The string representation of the generated UUID.
     */
	String nextUUID();

    /**
     * Returns the next pseudorandom, value from this random attribute
     * generator's sequence. The value is picked from the configured
     * distribution. The distribution is configured using {@link #setNextValueDistribution(Distribution, Object[])}.
     * 
     * @see #setNextValueDistribution(Distribution, Object[])
     * @return the next pseudorandom, distributed according to the
     * configured distribution.
     */
    float nextValue() throws EolRuntimeException;

    /**
     * Returns the next pseudorandom, value from this random attribute
     * generator's sequence. The value is picked from the provided
     * distribution, which uses the specified arguments for
     * configuration
     * 
     * @param distribution
     * @param args
     * @return
     * @throws EolRuntimeException
     */
    float nextValue(Distribution distribution, Number[] args) throws EolRuntimeException;
    
    /**
     * Configure the behaviour of {@link #nextFromListAsSample(String)}.
     * If refill is <code>false</code> (the default) an exception
     * will be thrown after the list is exhausted. If set to true,
     * a new sampling order will be created when the list is exhausted. 
     * @param refill if <code>false</code> an exception will be
     * thrown after the list is exhausted. If <code>true</code>
     * a new sample order will be generated after the list is
     * exhausted.
     */
    void setFromListAsSampleRefill(boolean refill);
    
    /**
     * Define the distribution to use for calls to {@link #nextValue()}.
     * 
     * @param distribution the distribution
     * @param args any arguments needed to configure the distribution.
     * @throws EolRuntimeException
     */
    void setNextValueDistribution(Distribution distribution, Number[] args) throws EolRuntimeException;
    
    /**
     * Sets the seed of the underlying random number generator using an
     * <code>int</code> seed.
     * <p>Sequences of values generated starting with the same seeds
     * should be identical.
     * </p>
     * @param seed the seed value
     */
    void setSeed(int seed);

    /**
     * Sets the seed of the underlying random number generator using an
     * <code>int</code> array seed.
     * <p>Sequences of values generated starting with the same seeds
     * should be identical.
     * </p>
     * @param seed the seed value
     */
    void setSeed(int[] seed);

    /**
     * Sets the seed of the underlying random number generator using a
     * <code>long</code> seed.
     * <p>Sequences of values generated starting with the same seeds
     * should be identical.
     * </p>
     * @param seed the seed value
     */
    void setSeed(long seed);



}
