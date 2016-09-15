package no.npolar.common.forms.view;

/*import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Iterator;*/

/**
 * Convenience class to simplify working with "filter links", that is; regular 
 * links used to narrow down a result list when viewing form (database) entries.
 * <p>
 * The links will typically all point to the same result page, only the 
 * parameters will vary.
 * <p>
 * An example "filter link": 
 * &lt;a href="#country=no"&gt;Norway&lt;/a&gt;
 * <p>
 * When multiple filters are active, it may look like this:
 * &lt;a href="#country=no&amp;gender=male"&gt;Norway&lt;/a&gt;
 * <p>
 * If the current "Country = Norway" filter is active, it should be a "remove" 
 * link, like this:
 * &lt;a href="#gender=male"&gt;Norway&lt;/a&gt;. (Note the missing country 
 * parameter.)
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class FilterLink {
    
    /** Internal flag indicating if this filter link is active or not. */
    private boolean active = false;
    /** The parameter(s) associated with this filter link. */
    private String param = null;

    /**
     * Creates a new filter link with the given active state and parameters.
     * 
     * @param active If <code>true</code>, the filter link is instantiated as active, otherwise it will be inactive.
     * @param parameters The string defining all parameters associated with this filter link (e.g. "country=no&amp;country=en&amp;country=se" or "gender=male")
     */
    public FilterLink(boolean active, String parameters) {
        this.active = active;
        this.param = parameters;
    }

    /**
     * Gets the parameters associated with this filter link.
     * 
     * @return the parameters associated with this filter link.
     */
    public String getParam() { return this.param; }
    
    /**
     * Used to determine if this filter link is active or not.
     * 
     * @return <code>true</code> if this filter link is active, <code>false</code> if not.
     */
    public boolean isActive() { return this.active; }
    
    
}