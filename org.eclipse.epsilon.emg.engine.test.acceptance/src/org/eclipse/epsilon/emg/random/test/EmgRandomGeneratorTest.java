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
package org.eclipse.epsilon.emg.random.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.validator.routines.UrlValidator;
import org.eclipse.epsilon.emg.random.EmgRandomGenerator;
import org.eclipse.epsilon.emg.random.IEmgRandomGenerator.EmgCharacterSet;
import org.eclipse.epsilon.emg.random.IEmgRandomGenerator;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.execute.context.EolContext;
import org.eclipse.epsilon.eol.execute.context.IEolContext;
import org.eclipse.epsilon.eol.execute.context.Variable;
import org.eclipse.epsilon.eol.types.EolPrimitiveType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

/**
 * The Class EmgRandomGeneratorTest. This tests are not intended to test the 
 * randomness of the results, just to verify that exceptions are thrown as
 * expected, the correct character sets used, etc., i.e. functionality.
 */
@RunWith(JUnitParamsRunner.class)
public class EmgRandomGeneratorTest {
	
	private static final List<Object> PIXAR_LIST = 
			Arrays.asList("Bomb Voyage", "Dory", "Ramone", "Sherri Squibbles", "Anton Ego");
	private static final int seed = 91591;
	protected IEmgRandomGenerator gen;
	private List<Object> pokenamesList;

	/**
	 * Sets the up.
	 *
	 * @throws Exception the exception
	 */
	@Before
	public void setUp() throws Exception {
		
		Variable pixar = new Variable("pixar", "Bomb Voyage,Dory,Ramone,Sherri Squibbles", EolPrimitiveType.String);
		IEolContext context = createGenerator(seed);
		context .getFrameStack().put(pixar);
		URL pokenamesResource = getClass().getResource("/lists/PokemonNames.txt");
		Variable pokenames = new Variable("pokenames", pokenamesResource.getPath(), EolPrimitiveType.String);
		context.getFrameStack().put(pokenames);
		Scanner s = new Scanner(pokenamesResource.openStream());
		pokenamesList = new ArrayList<Object>();
		while (s.hasNextLine()){
			pokenamesList.add(s.nextLine());
		}
		s.close();
	}
	
	public IEolContext createGenerator(int seed) {
		EolContext context = new EolContext();
		gen = new EmgRandomGenerator(context);
		gen.setSeed(seed);
		return context;
	}

	/**
	 * Tear down.
	 *
	 * @throws Exception the exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	@Parameters
	public void nextAddTo(int n, int m) throws Exception {
		List<Integer> values = gen.nextAddTo(n, m);
		int sum = values.stream().mapToInt(Integer::intValue).sum();
		assertEquals(m, sum);
	}
	@SuppressWarnings("unused")
	private List<List<Integer>> parametersForNextAddTo() {
        return Arrays.asList(
        			Arrays.asList(2, 10),
        			Arrays.asList(3, 10),
        			Arrays.asList(5, 100),
        			Arrays.asList(24, 1000));
    }
	
	
	@Test
	@Parameters
	public void nextCamelCaseString(String characterSet, int length, int minWordLength) throws Exception {
		String value = gen.nextCamelCaseWords(characterSet, length, minWordLength);
		assertEquals(length, value.length());
		// Split in words
		String[] words = value.split("(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])");
		try {
			EmgCharacterSet.valueOf(characterSet);
		}
		catch (IllegalArgumentException ex) {
			characterSet = "ID";
		}
		for (String w : words) {
			// Each word must start with an UpperCase letter
			char capital = w.charAt(0);
			assertTrue(Character.isUpperCase(capital));
			assertThat("Words have min length", w.length(), greaterThanOrEqualTo(minWordLength));
		}
	}
	@SuppressWarnings("unused")
	private List<List<Object>> parametersForNextCamelCaseString() {
        return Arrays.asList(
        			Arrays.asList("LETTER_LOWER", 10, 5),
        			Arrays.asList("LETTER_UPPER", 20, 7),
        			Arrays.asList("LETTER", 20, 7));
    }
	
	@Test
	@Parameters
	public void nextCamelCaseStringInvalidCharSet(String characterSet, int length, int minWordLength) throws Exception {
		try { 
			gen.nextCamelCaseWords(characterSet, length, minWordLength);
			fail("No exception for non alpha character set ");
		} 
		catch (EolRuntimeException ex) {
			assertThat(ex.getMessage(), is("Character set is non-Alpha."));
		}
	}
	@SuppressWarnings("unused")
	private List<List<Object>> parametersForNextCamelCaseStringInvalidCharSet() {
        return Arrays.asList(
        			Arrays.asList("ID", 10, 5),
        			Arrays.asList("NUMERIC", 10, 5),
        			Arrays.asList("UPPER_NUM", 10, 5),
        			Arrays.asList("LOWER_NUM", 10, 5),
        			Arrays.asList("ID_SYMBOL", 10, 5),
        			Arrays.asList("HEX_LOWER", 10, 5),
        			Arrays.asList("HEX_UPPER", 10, 5));
    }
	
	@Test
	public void nextFromCollection() throws Exception {
		String pick = (String) gen.nextFromCollection(PIXAR_LIST);
		assertThat(PIXAR_LIST, hasItem(pick));		
	}
	
	@Test
	public void nextFromCollectionEmpty() throws Exception {
		List<String> names = Collections.emptyList();
		String pick = (String) gen.nextFromCollection(names);
		assertThat(pick, is(nullValue()));		
	}
	
	@Test
	public void nextFromListCSV() throws Exception {
		String pick = (String) gen.nextFromList("pixar");
		assertThat(PIXAR_LIST, hasItem(pick));
	}
	
	@Test
	public void nextFromListFile() throws Exception {
		String pick = (String) gen.nextFromList("pokenames");
		assertThat(pokenamesList, hasItem(pick));
	}
	
	@Test
	public void nextFromListAsSampleNoRefill() throws Exception {
		String oldPick = "";
		try {
			for (int i = 0; i < PIXAR_LIST.size(); i++) {
				String pick = (String) gen.nextFromListAsSample("pixar");
				assertThat(PIXAR_LIST, hasItem(pick));
				assertThat(pick, is(not(oldPick)));
				oldPick = pick;
			}
			gen.nextFromListAsSample("pixar");
			fail("Sample should have been exhausted.");
		}
		catch (EolRuntimeException ex) {
			assertThat(ex.getMessage(), is("No more elements to sample from the list, pixar"));
		}
	}
	
	@Test
	public void nextFromListAsSampleRefill() throws Exception {
		gen.setFromListAsSampleRefill(true);
		String oldPick = "";
		String pick;
		for (int i = 0; i < PIXAR_LIST.size(); i++) {
			pick = (String) gen.nextFromListAsSample("pixar");
			assertThat(PIXAR_LIST, hasItem(pick));
			assertThat(pick, is(not(oldPick)));
			oldPick = pick;
		}
		pick = (String) gen.nextFromListAsSample("pixar");
		assertThat(PIXAR_LIST, hasItem(pick));
	}
	
	@Test
	public void nextHttpURI() throws Exception {
		String uri = gen.nextHttpURI(true, true, true, true);
		UrlValidator urlValidator = new UrlValidator();
		assertTrue(urlValidator.isValid(uri));
	}
	
	@Test
	public void nextIntUpperSmaller() throws Exception {
		try {
			gen.nextInt(5, 2);
			fail("If upper < smaller there should be an exception.");
		} catch (EolRuntimeException e) {
			assertThat(e.getCause(), instanceOf(NumberIsTooLargeException.class));
		}
	}
	
	@Test
	public void nextRealUpperSmaller() throws Exception {
		try {
			gen.nextReal(5.0f, 2.0f);
			fail("If upper < smaller there should be an exception.");
		} catch (EolRuntimeException e) {
			assertThat(e.getMessage(), is("Upper limit must be greater than lower limit."));
		}
	}
	
	@Test
	@Parameters
	public void nextSample(int sampleSize) throws Exception {
		List<Object> sample = gen.nextSample(PIXAR_LIST, sampleSize);
		assertThat(sample, everyItem(isIn(PIXAR_LIST)));
		assertThat(sample, hasSize(sampleSize));
	}
	@SuppressWarnings("unused")
	private List<Object> parametersForNextSample() {
        return Arrays.asList(2,3,4);
    }
	
	
	
	@Test
	public void nextSampleInvalidSize() throws Exception {
		try {
			gen.nextSample(PIXAR_LIST, 6);
		}
		catch (EolRuntimeException e) {
			assertThat(e.getCause(), instanceOf(NumberIsTooLargeException.class));
		}
		try {
			gen.nextSample(PIXAR_LIST, -6);
		}
		catch (EolRuntimeException e) {
			assertThat(e.getCause(), instanceOf(NotStrictlyPositiveException.class));
		}
		
	}
	
	@Test
	@Parameters
	public void nextSampleFromListCSV(int sampleSize) throws Exception {
		List<Object> sample = gen.nextSample("pixar", sampleSize);
		assertThat(sample, everyItem(isIn(PIXAR_LIST)));
		assertThat(sample, hasSize(sampleSize));
	}
	@SuppressWarnings("unused")
	private List<Object> parametersForNextSampleFromListCSV() {
        return Arrays.asList(2,3,4);
    }
	
	@Test
	@Parameters
	public void nextSampleFromListFile(int sampleSize) throws Exception {
		List<Object> sample = gen.nextSample("pokenames", sampleSize);
		assertThat(sample, everyItem(isIn(pokenamesList)));
		assertThat(sample, hasSize(sampleSize));
	}
	@SuppressWarnings("unused")
	private List<Object> parametersForNextSampleFromListFile() {
        return Arrays.asList(2,20,50);
    }
	
	@Test
	@Parameters
	public void nextURI(boolean addPort, boolean addPath, boolean addQuery, boolean addFragment) throws Exception {
		if (addPort)
			gen.setSeed(gen.nextInt());
		if (addPath)
			gen.setSeed(gen.nextInt());
		if (addQuery)
			gen.setSeed(gen.nextInt());
		if (addFragment)
			gen.setSeed(gen.nextInt());
		String uri = gen.nextURI(addPort, addPath, addQuery, addFragment);
		UrlValidator urlValidator = new UrlValidator(UrlValidator.ALLOW_ALL_SCHEMES);
		assertTrue(urlValidator.isValid(uri));
	}
	@SuppressWarnings("unused")
	private List<List<Object>>parametersForNextURI() {
        return Arrays.asList(
        		Arrays.asList(false, false, false, false),
        		Arrays.asList(false, false, false, true),
        		Arrays.asList(false, false, true,  false),
        		Arrays.asList(false, false, true,  true),
        		Arrays.asList(false, true,  false, false),
        		Arrays.asList(false, true,  false, true),
        		Arrays.asList(false, true,  true,  false),
        		Arrays.asList(false, true,  true,  true),
        		Arrays.asList(true,  false, false, false),
        		Arrays.asList(true,  false, false, true),
        		Arrays.asList(true,  false, true,  false),
        		Arrays.asList(true,  false, true,  true),
        		Arrays.asList(true,  true,  false, false),
        		Arrays.asList(true,  true,  false, true),
        		Arrays.asList(true,  true,  true,  false),
        		Arrays.asList(true,  true,  true,  true)
        		);
    }
	
	
	

}

