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

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class BinarySearchTest {

    static int numberCompare(Integer a, Integer b) {
        return a - b;
    }

    @Test
    public void testTooHighWithDefaultBias() throws Exception {
        Integer needle = 30;
        List<Integer> haystack = Arrays.asList(2, 4, 6, 8, 10, 12, 14, 16, 18, 20);
        BinarySearch.search(needle, haystack, BinarySearchTest::numberCompare, null);
        assertEquals(haystack.get(BinarySearch.search(needle, haystack, BinarySearchTest::numberCompare, null)).intValue(), 20);
    }

    @Test
    public void testTooLowWithDefaultBias() throws Exception {
        Integer needle = 1;
        List<Integer> haystack = Arrays.asList(2, 4, 6, 8, 10, 12, 14, 16, 18, 20);
        BinarySearch.search(needle, haystack, BinarySearchTest::numberCompare, null);
        assertEquals(BinarySearch.search(needle, haystack, BinarySearchTest::numberCompare, null), -1);
    }

    @Test
    public void testTooHighWithLubBias() throws Exception {
        Integer needle = 30;
        List<Integer> haystack = Arrays.asList(2, 4, 6, 8, 10, 12, 14, 16, 18, 20);
        BinarySearch.search(needle, haystack, BinarySearchTest::numberCompare, null);
        assertEquals(BinarySearch.search(needle, haystack, BinarySearchTest::numberCompare, BinarySearch.Bias.LEAST_UPPER_BOUND), -1);
    }

    @Test
    public void testTooLowWithLubBias() throws Exception {
        Integer needle = 1;
        List<Integer> haystack = Arrays.asList(2, 4, 6, 8, 10, 12, 14, 16, 18, 20);
        BinarySearch.search(needle, haystack, BinarySearchTest::numberCompare, null);
        assertEquals(
                haystack.get(BinarySearch.search(needle, haystack, BinarySearchTest::numberCompare, BinarySearch.Bias.LEAST_UPPER_BOUND)).intValue(),
                2);
    };

    @Test
    public void testExactSearch() throws Exception {
        Integer needle = 4;
        List<Integer> haystack = Arrays.asList(2, 4, 6, 8, 10, 12, 14, 16, 18, 20);

        assertEquals(haystack.get(BinarySearch.search(needle, haystack, BinarySearchTest::numberCompare, null)).intValue(), 4);
    };

    @Test
    public void testFuzzySearchWithDefaultBias() throws Exception {
        Integer needle = 19;
        List<Integer> haystack = Arrays.asList(2, 4, 6, 8, 10, 12, 14, 16, 18, 20);

        assertEquals(haystack.get(BinarySearch.search(needle, haystack, BinarySearchTest::numberCompare, null)).intValue(), 18);
    };

    @Test
    public void testFuzzySearchWithLubBias() throws Exception {
        Integer needle = 19;
        List<Integer> haystack = Arrays.asList(2, 4, 6, 8, 10, 12, 14, 16, 18, 20);

        assertEquals(
                haystack.get(BinarySearch.search(needle, haystack, BinarySearchTest::numberCompare, BinarySearch.Bias.LEAST_UPPER_BOUND)).intValue(),
                20);
    };

    @Test
    public void testMultipleMatches() throws Exception {
        Integer needle = 5;
        List<Integer> haystack = Arrays.asList(1, 1, 2, 5, 5, 5, 13, 21);

        assertEquals(BinarySearch.search(needle, haystack, BinarySearchTest::numberCompare, BinarySearch.Bias.LEAST_UPPER_BOUND), 3);
    };

    @Test
    public void testMultipleMatchesAtTheBeginning() throws Exception {
        Integer needle = 1;
        List<Integer> haystack = Arrays.asList(1, 1, 2, 5, 5, 5, 13, 21);

        assertEquals(BinarySearch.search(needle, haystack, BinarySearchTest::numberCompare, BinarySearch.Bias.LEAST_UPPER_BOUND), 0);
    };

}
