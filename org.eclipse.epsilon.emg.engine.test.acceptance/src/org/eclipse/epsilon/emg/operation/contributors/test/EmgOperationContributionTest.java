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
package org.eclipse.epsilon.emg.operation.contributors.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.epsilon.emg.EmgModule;
import org.eclipse.epsilon.emg.operations.contributors.EmgOperationContributor;
import org.eclipse.epsilon.emg.random.test.EmgRandomGeneratorTest;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.execute.context.IEolContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

@RunWith(JUnitParamsRunner.class)
public class EmgOperationContributionTest extends EmgRandomGeneratorTest {
	
	private class Villian {
		private final String name;
		private final String series;
		public Villian(String name, String series) {
			super();
			this.name = name;
			this.series = series;
		}
	}
	
	private static final String[] VILLIAN_SERIES = {"Fantastic Four","X-men", "Spider-man", "Captain America", "Thor", "Fantastic Four", "Avengers", "Avengers"};
	
	private static final String[] VILLIAN_NAMES = {"Doctor Doom","Magneto", "Norman Osborn", "Red Skull", "Loki", "Galactus", "Ultron", "Thanos"};
	
	private ArrayList<Object> villians;

	@Override
	public IEolContext createGenerator(int seed) {
		EmgModule module = new EmgModule();
		gen = new EmgOperationContributor(module);
		gen.setSeed(seed);
		// Add the mock @list
		String[] names = VILLIAN_NAMES;
		String[] series = VILLIAN_SERIES;
		villians = new ArrayList<Object>();
		for (int i = 0; i < names.length; i++) {
			villians.add(new Villian(names[i], series[i]));
		}
		module.getNamedCreatedObjects().put("villians", villians);
		return module.getContext();
	}
	
	@Test
	public void nextFromListAsSampleRefill() throws Exception {
		gen.setFromListAsSampleRefill(true);
		Object oldPick = "";
		Object pick;
		for (int i = 0; i < villians.size(); i++) {
			pick = gen.nextFromListAsSample("villians");
			assertThat(villians, hasItem(pick));
			assertThat(pick, is(not(oldPick)));
			oldPick = pick;
		}
		pick = gen.nextFromListAsSample("villians");
		assertThat(villians, hasItem(pick));
	}
	
	/**
	 * Test that we can access elements from an @list annotation
	 * @throws Exception
	 */
	@Test
	public void nextFromListCreate() throws Exception {
		Object pick = gen.nextFromList("villians");
		assertThat(pick, instanceOf(Villian.class));
		assertThat(villians, hasItem(pick));
		Villian v = (Villian) pick;
		assertThat(VILLIAN_NAMES, hasItemInArray(v.name));
		assertThat(VILLIAN_SERIES, hasItemInArray(v.series));
	}
	
	@Test
	public void nextFromListCreateAsSampleNoRefill() throws Exception {
		Object oldPick = "";
		try {
			for (int i = 0; i < villians.size(); i++) {
				Object pick = gen.nextFromListAsSample("villians");
				assertThat(villians, hasItem(pick));
				assertThat(pick, is(not(oldPick)));
				oldPick = pick;
			}
			gen.nextFromListAsSample("villians");
			fail("Sample should have been exhausted.");
		}
		catch (EolRuntimeException ex) {
			assertThat(ex.getMessage(), is("No more elements to sample from the list, villians"));
		}
	}
	
	@Test
	@Parameters
	public void nextSampleFromListCreate(int sampleSize) throws Exception {
		List<Object> sample = gen.nextSample("villians", sampleSize);
		assertThat(sample, everyItem(isIn(villians)));
		assertThat(sample, hasSize(sampleSize));
	}
	@SuppressWarnings("unused")
	private List<Object> parametersForNextSampleFromListCreate() {
        return Arrays.asList(2,3,4);
    }
	
	/**
	 * Sets the up.
	 *
	 * @throws Exception the exception
	 */
	@Before
	public void setUp() throws Exception {
		super.setUp();
	}
	

	
	
	
}
