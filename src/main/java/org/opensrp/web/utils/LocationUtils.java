/**
 *
 */
package org.opensrp.web.utils;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author Samuel Githengi created on 10/09/19
 */
public class LocationUtils {


    public static Set<String> getRootLocation(Map<String, String> locations) {
        // get all parents
        Set<String> parents = new HashSet<>(locations.values());
        Set<String> ids = locations.keySet();
        // remove parents that are also children
        parents.removeAll(ids);
        parents.remove(null);

        // add ids that dot have parents
        for (Entry<String, String> entry : locations.entrySet()) {
            if (entry.getValue() == null)
                parents.add(entry.getKey());
        }

        //add ids if parent not added


        return parents;
    }
}
