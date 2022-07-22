/**
 *
 */
package org.opensrp.web.utils;

import org.junit.Test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Samuel Githengi created on 10/09/19
 */
public class LocationUtilsTest {

    @Test
    public void testgetRootParentWithParentWithEmptyLocations() {
        Map<String, String> locations = new HashMap<>();
        Set<String> parents = LocationUtils.getRootLocation(locations);
        assertTrue(parents.isEmpty());
    }

    @Test
    public void testgetRootParentWithParentWithChildAndParentLocation() {
        Map<String, String> locations = new HashMap<>();
        locations.put("Cho_1", "Choma");
        locations.put("Cho_2", "Choma");
        locations.put("Choma", "Zone1");
        Set<String> parents = LocationUtils.getRootLocation(locations);
        assertTrue(!parents.isEmpty());
        assertEquals(1, parents.size());
        assertEquals("Zone1", parents.iterator().next());
    }

    @Test
    public void testgetRootParentWithParentWithoutParentLocations() {
        Map<String, String> locations = new HashMap<>();
        locations.put("Cho_1", "Choma");
        locations.put("Cho_2", "Choma");
        locations.put("Cho_3", "Choma");
        locations.put("Cho_4", "Choma");
        Set<String> parents = LocationUtils.getRootLocation(locations);
        assertTrue(!parents.isEmpty());
        assertEquals(1, parents.size());
        assertEquals("Choma", parents.iterator().next());
    }

    @Test
    public void testgetRootParentWithNullParents() {
        Map<String, String> locations = new HashMap<>();
        locations.put("Cho_1", "Choma");
        locations.put("Cho_2", "Choma");
        locations.put("Cho_3", "Choma");
        locations.put("Cho_4", null);
        Set<String> parents = LocationUtils.getRootLocation(locations);
        assertTrue(!parents.isEmpty());
        assertEquals(2, parents.size());
        Iterator<String> iterator = parents.iterator();
        assertEquals("Cho_4", iterator.next());
        assertEquals("Choma", iterator.next());
    }

}
