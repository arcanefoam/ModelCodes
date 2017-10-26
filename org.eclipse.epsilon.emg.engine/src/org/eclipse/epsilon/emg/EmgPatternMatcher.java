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
package org.eclipse.epsilon.emg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.epsilon.emg.random.IEmgRandomGenerator;
import org.eclipse.epsilon.eol.dom.AnnotatableModuleElement;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.execute.context.FrameType;
import org.eclipse.epsilon.eol.execute.context.IEolContext;
import org.eclipse.epsilon.epl.combinations.CompositeCombinationGenerator;
import org.eclipse.epsilon.epl.dom.Pattern;
import org.eclipse.epsilon.epl.execute.PatternMatch;
import org.eclipse.epsilon.epl.execute.PatternMatcher;

/**
 * The Class EmgPatternMatcher extends the EPL pattern matcher to provide
 * EMG specific functionality.
 */
public class EmgPatternMatcher extends PatternMatcher {

    /**
     * How many matches should the pattern find
     */
    private static final String NUMBER_MATCHES_ANNOTATION = "matches";

    /**
     * What is the probability of executing the pattern
     */
    private static final String PROBABILITY_ANNOTATION = "probability";

    /**
     * Don't re-execute the pattern for the same set of input elements
     */
    private static final String NO_REPEAT_ANNOTATION = "noRepeat";

    /** The random generator. */
    IEmgRandomGenerator randomGenerator;

    /**
     * Instantiates a new EMG pattern matcher.
     *
     * @param rand the EmgRandomGenerator
     */
    public EmgPatternMatcher(IEmgRandomGenerator rand){
        randomGenerator=rand;
    }

    /* (non-Javadoc)
     * @see org.eclipse.epsilon.epl.execute.PatternMatcher#match(org.eclipse.epsilon.epl.dom.Pattern, org.eclipse.epsilon.eol.execute.context.IEolContext)
     */
    @Override
    public List<PatternMatch> match(final Pattern pattern, final IEolContext context) throws Exception {

    	List<PatternMatch> patternMatches = new ArrayList<PatternMatch>();
        
    	int matchCounter = 0;
    	
    	// Cache of matches, for no repeat
    	List<List<Object>> matchList = null;
        
    	boolean noRepeat= pattern.hasAnnotation(NO_REPEAT_ANNOTATION);
                
    	int maxMatches = getMaxMatches(pattern, context);
        
    	// The probability to enforce the pattern, 1 of not present
        double enforceProbability = getExecuteMatchProbability(pattern, context);
        
        if (noRepeat) {
        	matchList = new ArrayList<List<Object>>();
        }
        
        context.getFrameStack().enterLocal(FrameType.PROTECTED, pattern);
        
        CompositeCombinationGenerator<Object> generator = initGenerator(pattern, context);
        
        while (generator.hasMore() && (matchCounter < maxMatches)) {
        	
            List<List<Object>> candidate = generator.getNext();
            if (skipRepeated(noRepeat, candidate, matchList)) {
            	continue;
            }
            populateFrame(pattern, context, candidate);
            boolean matches = getMatchResult(pattern, context);
            if (matches) {
            	if (randomGenerator.nextValue() < enforceProbability) {
            		context.getExecutorFactory().execute(pattern.getOnMatch(), context);
                    patternMatches.add(createPatternMatch(pattern, candidate));
                    if (noRepeat) {
                        matchList.addAll(candidate);
                    }
                    matchCounter++;
                }
            }
            else {
            	context.getExecutorFactory().execute(pattern.getNoMatch(), context);
            }
            context.getFrameStack().leaveLocal(pattern);
        }
        context.getFrameStack().leaveLocal(pattern);
        return patternMatches;
    }
    
    

	/**
     * Get the $probability annotation value. If not present or no value is provided, returns 1
     * @param hasProbabilityAnnotation 
     * @param pattern 
     * @param context 
     * @return
     * @throws EolRuntimeException 
     */
    private double getExecuteMatchProbability(AnnotatableModuleElement pattern, IEolContext context) throws EolRuntimeException {
	    if (pattern.getAnnotationsValues(PROBABILITY_ANNOTATION, context).size() > 0) {
            Object val = pattern.getAnnotationsValues(PROBABILITY_ANNOTATION, context).get(0);
            if (!val.equals(null)) {
            	return getFloat(val);
            }
        } 
		return 1; //FIXME is it one for other distributions?
	}

	/**
     * If no repeat, returns true if the candidates have all ready been matched. 
     * @param noRepeat Flag that indicates if no repeat is selected
     * @param candidate	the candidate objects to match
     * @param matchList the chache of matches
     * @return
     */
    private boolean skipRepeated(boolean noRepeat, List<List<Object>> candidate, List<List<Object>> matchList) {
    	
        if (noRepeat){
            for (List<Object> temp : candidate) {
                if (matchList.contains(temp)) {
                   return true;
                }
            }
        }
        return false;
	}

	/**
     * If the Pattern is annotated with a "number" annotation returns the expression value, if not returns
     * 0 which will cause all matches to happen. If no value is given then 0 is assumed and no limit will
     * be enforced.
     * @param pattern 
     * @param context 
     * @return
     * @throws EolRuntimeException 
     */
    private int getMaxMatches(Pattern pattern, IEolContext context) throws EolRuntimeException {
    	
    	List<Object> annotationValues = pattern.getAnnotationsValues(NUMBER_MATCHES_ANNOTATION, context);
    	int value = 0;
		// Is it a sequence?
        if (annotationValues.size()>1) {
            Object lowerVal = annotationValues.get(0);
            Object upperVal = annotationValues.get(1);
            if(!(lowerVal.equals(null) || (upperVal.equals(null)))) {
                value = randomGenerator.nextInt(getInt(lowerVal), getInt(upperVal));
            }
        }
        else if (annotationValues.size() > 0) {
            Object limit = annotationValues.get(0);
            if (!(limit.equals(null))) {
                if (limit instanceof Collection) {
                    List<Object> collection = (List<Object>) limit;
                    if (collection.size() > 1) {
                        value = randomGenerator.nextInt(getInt(collection.get(0)), getInt(collection.get(1)));
                    }
                    else {
                        value = getInt(collection.get(0));
                    }
                }
                else {
                    value=getInt(limit);
                }
            }
        }
        else {
        	// More than MAX_VALUE total matches?
        	value = Integer.MAX_VALUE;
        }
		return value;
	}

	/**
     * Gets the int value of the Object returned by the EOL engine.
     *
     * @param object the object
     * @return the int
     */
    protected int getInt(Object object){
        if(object instanceof Integer)
            return (int)object;
        else
            return Integer.parseInt((String) object);
    }

    /**
     * Gets the float.
     *
     * @param object the object
     * @return the float
     */
    protected double getFloat(Object object){
        if(object instanceof Number) // || object instanceof Double)
            return ((Number)object).doubleValue();
        else
            return Float.parseFloat((String) object);
    }

    /**
     * Contain any.
     *
     * @param first the first
     * @param last the last
     * @return true, if successful
     */
    protected boolean containAny(Collection<Object> first, Collection<Object> last){
        for(Object o : first){
            if(last.contains(o))
                return true;
        }
        return false;
    }
}
