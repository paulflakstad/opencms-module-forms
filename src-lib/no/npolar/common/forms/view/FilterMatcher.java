package no.npolar.common.forms.view;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Paul-Inge Flakstad
 */
public class FilterMatcher {
    /** Maps each filter name (key) and whether or not that filter is currently a match (value). */
    private Map<String, Boolean> filterMatches;
    /** Maps all filtering info: Each filter's name (key) and the filter's value(s) (value) required for a match. */
    private Map<String, String[]> matcherMap;

    /**
     * Creates a new filter matcher.
     * <p>
     * This FilterMatcher object can be used to check form submissions against a 
     * set of filters.
     * <p>
     * The intended usage is that filters are provided as parameters, using a 
     * filter form or filter links. In this case, the parameter map can be 
     * provided as the argument - provided all other parameters are removed.
     * 
     * @param filterNamesAndRequiredValues A map containing each filter's name (the map keys) and the each filter's value(s) (the map values) required for a match.
     */
    public FilterMatcher(Map<String, String[]> filterNamesAndRequiredValues) {
        filterMatches = new HashMap<String, Boolean>(); // Will hold the match for each given filter (HashMap<[filter name], [match|not match]>)
        matcherMap = new HashMap<String, String[]>(filterNamesAndRequiredValues); // All filtering info is here, so store it as a class member

        // Initialize each individual match as false
        Iterator<String> i = matcherMap.keySet().iterator();
        while (i.hasNext()) {
            String filterName = i.next();
            filterMatches.put(filterName, false);
        }
    }

    /**
     * Adds a match on a filter identified by the given name, that is; sets 
     * <code>match=true</code> on the given filter.
     * 
     * @param filterName The name of the filter to set as a match.
     */
    private void addFilterMatch(String filterName) {
        filterMatches.put(filterName, true);
    }

    /**
     * Removes empty filter values (for example, produced by a drop-down in a 
     * form) from a string array, and transforms the resulting array to a list.
     * 
     * @param filterValues The array containing all (and possibly one or more empty) filter values.
     * @return Only the non-empty filter values.
     */
    private List removeEmptyValues(String[] filterValues) {
        List<String> onlyNonEmpty = new ArrayList<String>();
        for (int i = 0; i < filterValues.length; i++) {
            if (filterValues[i] != null && !filterValues[i].trim().isEmpty())
                onlyNonEmpty.add(filterValues[i]);
        }
        //return onlyNonEmpty.toArray(new String[onlyNonEmpty.size()]);            
        return onlyNonEmpty;
    }
    
    /**
     * Checks if a given filter value is a match for a filter identified by its 
     * filter name.
     * <p>
     * NOTE: An empty or <code>null</code> filter value is considered a match.
     * 
     * @param filterName The name of the filter to check.
     * @param filterValue The filter value to check for a match.
     * @return <code>true</code> if the given filter value is a match for the filter identified by the given name, <code>false</code> if not.
     */
    public boolean match(String filterName, String filterValue) {
        if (filterValue == null || filterValue.isEmpty()) {
            addFilterMatch(filterName);
            return true;
        }
        List matching = this.getMatches(filterName);
        if (matching == null || matching.isEmpty() || matching.contains(filterValue)) {
            addFilterMatch(filterName);
            return true;
        }
        return false;

    }
    
    /**
     * Determines if this FilterMatcher is currently a full match (matching on 
     * all filters).
     * 
     * @return <code>true</code> is this FilterMatcher is currently a full match (matching on all filters), <code>false</code> if not.
     */
    public boolean isMatch() {
        // No filters
        if (filterMatches.isEmpty())
            return true;
        // Filters exist, loop through all: Require ALL to be "true"
        Iterator<String> iKeys = filterMatches.keySet().iterator();
        while (iKeys.hasNext()) {
            if (!filterMatches.get(iKeys.next())) // If the current filter has not been matched ..
                return false; // ... return false (ALL must match)
        }
        return true; // Haven't returned false yet, so we've got a match
    }

    /**
     * Used to check if this FilterMatcher is a matcher for a particular filter, 
     * identified by the given filter name.
     * 
     * @param filterName The name of the filter.
     * @return <code>true</code> if this FilterMatcher is a matcher for a particular filter, identified by the given filter name, <code>false</code> if not.
     */
    public boolean isMatcherFor(String filterName) {
        return filterMatches.containsKey(filterName);
    }

    /**
     * Gets the values required for a match on the filter identified by the 
     * given filter name.
     * 
     * @param filterName The name of the filter.
     * @return The values required for a match on the filter identified by the given filter name.
     */
    public List getMatches(String filterName) {
        //return Arrays.asList(removeEmptyValues((String[])matcherMap.get(filterName)));
        return removeEmptyValues((String[])matcherMap.get(filterName));
    }
}
