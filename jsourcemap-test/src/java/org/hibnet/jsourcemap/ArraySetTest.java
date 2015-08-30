/*
 *  Copyright 2015 JSourceMap contributors
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.hibnet.jsourcemap;

import static org.hibnet.jsourcemap.TestUtil.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

public class ArraySetTest {

    ArraySet<String> makeTestSet() {
        ArraySet<String> set = new ArraySet<>();
        for (int i = 0; i < 100; i++) {
            set.add(String.valueOf(i));
        }
        return set;
    }

    @Test
    public void testHas() throws Exception {
        ArraySet<String> set = makeTestSet();
        for (int i = 0; i < 100; i++) {
            assertTrue(set.has(String.valueOf(i)));
        }
    }

    @Test
    public void testIndexOf() throws Exception {
        ArraySet<String> set = makeTestSet();
        for (int i = 0; i < 100; i++) {
            assertEquals(set.indexOf(String.valueOf(i)).intValue(), i);
        }
    }

    @Test
    public void testAt() throws Exception {
        ArraySet<String> set = makeTestSet();
        for (int i = 0; i < 100; i++) {
            assertEquals(set.at(i), String.valueOf(i));
        }
    }

    @Test
    public void testFromArray() throws Exception {
        ArraySet<String> set = ArraySet.fromArray(Arrays.asList("foo", "bar", "baz", "quux", "hasOwnProperty"), null);

        assertTrue(set.has("foo"));
        assertTrue(set.has("bar"));
        assertTrue(set.has("baz"));
        assertTrue(set.has("quux"));
        assertTrue(set.has("hasOwnProperty"));

        assertEquals(set.indexOf("foo").intValue(), 0);
        assertEquals(set.indexOf("bar").intValue(), 1);
        assertEquals(set.indexOf("baz").intValue(), 2);
        assertEquals(set.indexOf("quux").intValue(), 3);

        assertEquals(set.at(0), "foo");
        assertEquals(set.at(1), "bar");
        assertEquals(set.at(2), "baz");
        assertEquals(set.at(3), "quux");
    }

    @Test
    public void testFromArrayWithDuplicates() throws Exception {
        ArraySet<String> set = ArraySet.fromArray(Arrays.asList("foo", "foo"), null);
        assertTrue(set.has("foo"));
        assertEquals(set.at(0), "foo");
        assertEquals(set.indexOf("foo").intValue(), 0);
        assertEquals(set.toArray().size(), 1);

        set = ArraySet.fromArray(Arrays.asList("foo", "foo"), true);
        assertTrue(set.has("foo"));
        assertEquals(set.at(0), "foo");
        assertEquals(set.at(1), "foo");
        assertEquals(set.indexOf("foo").intValue(), 0);
        assertEquals(set.toArray().size(), 2);
    }

    @Test
    public void testAddWithDuplicates() throws Exception {
        ArraySet<String> set = new ArraySet<>();
        set.add("foo");

        set.add("foo");
        assertTrue(set.has("foo"));
        assertEquals(set.at(0), "foo");
        assertEquals(set.indexOf("foo").intValue(), 0);
        assertEquals(set.toArray().size(), 1);

        set.add("foo", true);
        assertTrue(set.has("foo"));
        assertEquals(set.at(0), "foo");
        assertEquals(set.at(1), "foo");
        assertEquals(set.indexOf("foo").intValue(), 0);
        assertEquals(set.toArray().size(), 2);
    }

    @Test
    public void testSize() throws Exception {
        ArraySet<String> set = new ArraySet<>();
        set.add("foo");
        set.add("bar");
        set.add("baz");
        assertEquals(set.size(), 3);
    }

    @Test
    public void testDisallowDuplicates() throws Exception {
        ArraySet<String> set = new ArraySet<>();

        set.add("foo");
        set.add("foo");

        set.add("bar");
        set.add("bar");

        set.add("baz");
        set.add("baz");

        assertEquals(set.size(), 3);
    }

    @Test
    public void testWithAllowedDuplicates() throws Exception {
        ArraySet<String> set = new ArraySet<>();

        set.add("foo");
        set.add("foo", true);

        set.add("bar");
        set.add("bar", true);

        set.add("baz");
        set.add("baz", true);

        assertEquals(set.size(), 3);
    }
}
