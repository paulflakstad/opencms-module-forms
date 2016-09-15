package no.npolar.common.forms.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Helper class with static methods for processing filter information.
 * 
 * @author Paul-Inge Flakstad
 */
public class FilterUtils {
    
    /**
     * Default constructor.
     */
    public FilterUtils() {
        // 
    }
    
    /**
     * Returns a filter, with the given value added (if it isn't currently 
     * active) or removed (if it is currently active).
     * <p>
     * The given Map (of parameters) is used for comparison to evaluate if a 
     * filter is currently active or not. (if name=value is present in the 
     * parameterMap, the filter is evaluated as currently active.)
     * 
     * @param name the filter name (typically the parameter name)
     * @param value the filter value (typically a value for the parameter)
     * @param parameterMap the parameters to use when evaluating a filter's active state (typically the current request.getParameterMap(), with non-filter parameters removed)
     * @return a FilterLink containing all the necessary parameters, and with the given filter value added or removed
     */
    public static FilterLink addOrRemoveFilter(String name, String value, Map parameterMap) {
        boolean active = false;
        String s = "";
        if (name == null || value == null) {
            throw new NullPointerException("Unable to create filter link using [name:" + name + "]=[value:" + value + "]. (Name or value cannot be null.)");
        }
        Map<String, String[]> m = new HashMap<String, String[]>(parameterMap);
        if (m.containsKey(name)) { // A filter with this name already exists
            
            List<String> existingFilterValues = new ArrayList<String>(Arrays.asList(m.get(name)));
            m.remove(name);
            if (existingFilterValues.contains(value)) {
                existingFilterValues.remove(value);
                active = true;
            } else {
                try {
                    existingFilterValues.add(value);
                } catch (Exception e) {
                    throw new NullPointerException("Unable to add filter for '" + value + "'.");
                }
            }
            m.put(name, existingFilterValues.toArray(new String[0]));
        }
        else {
            m.put(name, new String[] {value});
        }
        
        Iterator<String> iKeys = m.keySet().iterator();
        while (iKeys.hasNext()) {
            String key = iKeys.next();
            String[] val = m.get(key);
            for (int i = 0; i < val.length; i++) {
                s += (!s.isEmpty() ? "&amp;" : "") + key + "=" + val[i];
            }
        }
        
        return new FilterLink(active, s);
    }
    
    /**
     * Converts a given map, retaining only entries specified by the given set 
     * of keys.
     * 
     * @param parameterMap The map to remove unwanted entries from.
     * @param filterKeys The keys to keep in the map (every map entry with a key existing here will be left untouched).
     * @return A map containing only the keys specified in the given list of keys
     */
    public static Map<String, String[]> retainOnlyFilterKeys(Map<String, String[]> parameterMap, List<String> filterKeys) {
        Map<String, String[]> filterMap = new HashMap(parameterMap);
        List<String> removeKeys = new ArrayList<String>();
        Iterator<String> iFilterMapKeys = filterMap.keySet().iterator();
        while (iFilterMapKeys.hasNext()) {
            String filterMapKey = iFilterMapKeys.next();
            if (!filterKeys.contains(filterMapKey)) {
                removeKeys.add(filterMapKey);
            }
        }
        Iterator<String> iRemoveKeys = removeKeys.iterator();
        while (iRemoveKeys.hasNext()) {
            filterMap.remove(iRemoveKeys.next());
        }
        return filterMap;
    }
    
    /**
     * Converts an array to a String.
     * <p>
     * No delimiter (separator) character is appended.
     * 
     * @param stringArray The array to convert to a String.
     * @return The array converted to a String.
     */
    public static String arrayAsString(String[] stringArray) {
        String s = "";
        for (int i = 0; i < stringArray.length; i++) {
            s += stringArray[i];
        }
        return s;
    }
    
    /**
     * Converts a given map of parameters to a parameter string.
     * 
     * @param parameters The parameter map to convert to a String.
     * @return The parameter map converted to a String.
     */
    public static String createParameterString(Map<String, String[]> parameters) {
        String s = "";
        Iterator<String> iKeys = parameters.keySet().iterator();
        while (iKeys.hasNext()) {
            String key = iKeys.next();
            String [] values = parameters.get(key);
            for (int i = 0; i < values.length; i++) {
                s += (s.isEmpty() ? "" : "&amp;") + key + "=" + values[i];
            }
        }
        return s;
    }
}
