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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.hibnet.jsourcemap.SourceMapConsumer.Order;
import org.junit.Test;

public class SourceMapConsumerTest {

    @Test
    public void testThatTheSourcesFieldHasTheOriginalSources() throws Exception {
        SourceMapConsumer map;
        List<String> sources;

        map = SourceMapConsumer.create(TestUtil.testMap);
        sources = map.sources();
        assertEquals(sources.get(0), "/the/root/one.js");
        assertEquals(sources.get(1), "/the/root/two.js");
        assertEquals(sources.size(), 2);

        map = SourceMapConsumer.create(TestUtil.indexedTestMap);
        sources = map.sources();
        assertEquals(sources.get(0), "/the/root/one.js");
        assertEquals(sources.get(1), "/the/root/two.js");
        assertEquals(sources.size(), 2);

        map = SourceMapConsumer.create(TestUtil.indexedTestMapDifferentSourceRoots);
        sources = map.sources();
        assertEquals(sources.get(0), "/the/root/one.js");
        assertEquals(sources.get(1), "/different/root/two.js");
        assertEquals(sources.size(), 2);

        map = SourceMapConsumer.create(TestUtil.testMapNoSourceRoot);
        sources = map.sources();
        assertEquals(sources.get(0), "one.js");
        assertEquals(sources.get(1), "two.js");
        assertEquals(sources.size(), 2);

        map = SourceMapConsumer.create(TestUtil.testMapEmptySourceRoot);
        sources = map.sources();
        assertEquals(sources.get(0), "one.js");
        assertEquals(sources.get(1), "two.js");
        assertEquals(sources.size(), 2);
    }

    @Test
    public void testThatTheSourceRootIsReflectedInAMappingsSourceField() throws Exception {
        SourceMapConsumer map;
        OriginalPosition mapping;

        map = SourceMapConsumer.create(TestUtil.testMap);

        mapping = map.originalPositionFor(2, 1, null);
        assertEquals(mapping.source, "/the/root/two.js");

        mapping = map.originalPositionFor(1, 1, null);
        assertEquals(mapping.source, "/the/root/one.js");

        map = SourceMapConsumer.create(TestUtil.testMapNoSourceRoot);

        mapping = map.originalPositionFor(2, 1, null);
        assertEquals(mapping.source, "two.js");

        mapping = map.originalPositionFor(1, 1, null);
        assertEquals(mapping.source, "one.js");

        map = SourceMapConsumer.create(TestUtil.testMapEmptySourceRoot);

        mapping = map.originalPositionFor(2, 1, null);
        assertEquals(mapping.source, "two.js");

        mapping = map.originalPositionFor(1, 1, null);
        assertEquals(mapping.source, "one.js");
    }

    @Test
    public void testMappingTokensBackExactly() throws Exception {
        SourceMapConsumer map = SourceMapConsumer.create(TestUtil.testMap);

        TestUtil.assertMapping(1, 1, "/the/root/one.js", 1, 1, null, null, map);
        TestUtil.assertMapping(1, 5, "/the/root/one.js", 1, 5, null, null, map);
        TestUtil.assertMapping(1, 9, "/the/root/one.js", 1, 11, null, null, map);
        TestUtil.assertMapping(1, 18, "/the/root/one.js", 1, 21, "bar", null, map);
        TestUtil.assertMapping(1, 21, "/the/root/one.js", 2, 3, null, null, map);
        TestUtil.assertMapping(1, 28, "/the/root/one.js", 2, 10, "baz", null, map);
        TestUtil.assertMapping(1, 32, "/the/root/one.js", 2, 14, "bar", null, map);

        TestUtil.assertMapping(2, 1, "/the/root/two.js", 1, 1, null, null, map);
        TestUtil.assertMapping(2, 5, "/the/root/two.js", 1, 5, null, null, map);
        TestUtil.assertMapping(2, 9, "/the/root/two.js", 1, 11, null, null, map);
        TestUtil.assertMapping(2, 18, "/the/root/two.js", 1, 21, "n", null, map);
        TestUtil.assertMapping(2, 21, "/the/root/two.js", 2, 3, null, null, map);
        TestUtil.assertMapping(2, 28, "/the/root/two.js", 2, 10, "n", null, map);
    };

    @Test
    public void testMappingTokensBackExactlyInIndexedSourceMap() throws Exception {
        SourceMapConsumer map = SourceMapConsumer.create(TestUtil.indexedTestMap);

        TestUtil.assertMapping(1, 1, "/the/root/one.js", 1, 1, null, null, map);
        TestUtil.assertMapping(1, 5, "/the/root/one.js", 1, 5, null, null, map);
        TestUtil.assertMapping(1, 9, "/the/root/one.js", 1, 11, null, null, map);
        TestUtil.assertMapping(1, 18, "/the/root/one.js", 1, 21, "bar", null, map);
        TestUtil.assertMapping(1, 21, "/the/root/one.js", 2, 3, null, null, map);
        TestUtil.assertMapping(1, 28, "/the/root/one.js", 2, 10, "baz", null, map);
        TestUtil.assertMapping(1, 32, "/the/root/one.js", 2, 14, "bar", null, map);

        TestUtil.assertMapping(2, 1, "/the/root/two.js", 1, 1, null, null, map);
        TestUtil.assertMapping(2, 5, "/the/root/two.js", 1, 5, null, null, map);
        TestUtil.assertMapping(2, 9, "/the/root/two.js", 1, 11, null, null, map);
        TestUtil.assertMapping(2, 18, "/the/root/two.js", 1, 21, "n", null, map);
        TestUtil.assertMapping(2, 21, "/the/root/two.js", 2, 3, null, null, map);
        TestUtil.assertMapping(2, 28, "/the/root/two.js", 2, 10, "n", null, map);
    };

    @Test
    public void testMappingTokensFuzzy() throws Exception {
        SourceMapConsumer map = SourceMapConsumer.create(TestUtil.testMap);

        // Finding original positions with default (glb) bias.
        TestUtil.assertMapping(1, 20, "/the/root/one.js", 1, 21, "bar", null, map, true);
        TestUtil.assertMapping(1, 30, "/the/root/one.js", 2, 10, "baz", null, map, true);
        TestUtil.assertMapping(2, 12, "/the/root/two.js", 1, 11, null, null, map, true);

        // Finding original positions with lub bias.
        TestUtil.assertMapping(1, 16, "/the/root/one.js", 1, 21, "bar", BinarySearch.Bias.LEAST_UPPER_BOUND, map, true);
        TestUtil.assertMapping(1, 26, "/the/root/one.js", 2, 10, "baz", BinarySearch.Bias.LEAST_UPPER_BOUND, map, true);
        TestUtil.assertMapping(2, 6, "/the/root/two.js", 1, 11, null, BinarySearch.Bias.LEAST_UPPER_BOUND, map, true);

        // Finding generated positions with default (glb) bias.
        TestUtil.assertMapping(1, 18, "/the/root/one.js", 1, 22, "bar", null, map, null, true);
        TestUtil.assertMapping(1, 28, "/the/root/one.js", 2, 13, "baz", null, map, null, true);
        TestUtil.assertMapping(2, 9, "/the/root/two.js", 1, 16, null, null, map, null, true);

        // Finding generated positions with lub bias.
        TestUtil.assertMapping(1, 18, "/the/root/one.js", 1, 20, "bar", BinarySearch.Bias.LEAST_UPPER_BOUND, map, null, true);
        TestUtil.assertMapping(1, 28, "/the/root/one.js", 2, 7, "baz", BinarySearch.Bias.LEAST_UPPER_BOUND, map, null, true);
        TestUtil.assertMapping(2, 9, "/the/root/two.js", 1, 6, null, BinarySearch.Bias.LEAST_UPPER_BOUND, map, null, true);
    };

    @Test
    public void testMappingTokensFuzzyInIndexedSourceMap() throws Exception {
        SourceMapConsumer map = SourceMapConsumer.create(TestUtil.indexedTestMap);

        // Finding original positions with default (glb) bias.
        TestUtil.assertMapping(1, 20, "/the/root/one.js", 1, 21, "bar", null, map, true);
        TestUtil.assertMapping(1, 30, "/the/root/one.js", 2, 10, "baz", null, map, true);
        TestUtil.assertMapping(2, 12, "/the/root/two.js", 1, 11, null, null, map, true);

        // Finding original positions with lub bias.
        TestUtil.assertMapping(1, 16, "/the/root/one.js", 1, 21, "bar", BinarySearch.Bias.LEAST_UPPER_BOUND, map, true);
        TestUtil.assertMapping(1, 26, "/the/root/one.js", 2, 10, "baz", BinarySearch.Bias.LEAST_UPPER_BOUND, map, true);
        TestUtil.assertMapping(2, 6, "/the/root/two.js", 1, 11, null, BinarySearch.Bias.LEAST_UPPER_BOUND, map, true);

        // Finding generated positions with default (glb) bias.
        TestUtil.assertMapping(1, 18, "/the/root/one.js", 1, 22, "bar", null, map, null, true);
        TestUtil.assertMapping(1, 28, "/the/root/one.js", 2, 13, "baz", null, map, null, true);
        TestUtil.assertMapping(2, 9, "/the/root/two.js", 1, 16, null, null, map, null, true);

        // Finding generated positions with lub bias.
        TestUtil.assertMapping(1, 18, "/the/root/one.js", 1, 20, "bar", BinarySearch.Bias.LEAST_UPPER_BOUND, map, null, true);
        TestUtil.assertMapping(1, 28, "/the/root/one.js", 2, 7, "baz", BinarySearch.Bias.LEAST_UPPER_BOUND, map, null, true);
        TestUtil.assertMapping(2, 9, "/the/root/two.js", 1, 6, null, BinarySearch.Bias.LEAST_UPPER_BOUND, map, null, true);
    };

    @Test
    public void testMappingsAndEndOfLines() throws Exception {
        SourceMapGenerator smg = new SourceMapGenerator("foo.js", null);
        smg.addMapping(new Mapping(new Position(1, 1), new Position(1, 1), "bar.js"));
        smg.addMapping(new Mapping(new Position(2, 2), new Position(2, 2), "bar.js"));
        smg.addMapping(new Mapping(new Position(1, 1), new Position(1, 1), "baz.js"));

        SourceMapConsumer map = SourceMapConsumer.fromSourceMap(smg);

        // When finding original positions, mappings end at the end of the line.
        TestUtil.assertMapping(2, 1, null, null, null, null, null, map, true);

        // When finding generated positions, mappings do not end at the end of the line.
        TestUtil.assertMapping(1, 1, "bar.js", 2, 1, null, null, map, null, true);

        // When finding generated positions with, mappings end at the end of the source.
        TestUtil.assertMapping(null, null, "bar.js", 3, 1, null, BinarySearch.Bias.LEAST_UPPER_BOUND, map, null, true);
    }

    @Test
    public void testEachMapping() throws Exception {
        SourceMapConsumer map;

        map = SourceMapConsumer.create(TestUtil.testMap);
        int[] previousLine = new int[1];
        previousLine[0] = Integer.MIN_VALUE;
        int[] previousColumn = new int[1];
        previousColumn[0] = Integer.MIN_VALUE;
        map.eachMapping().forEach(mapping -> {
            assertTrue(mapping.generated.line >= previousLine[0]);

            assertTrue(mapping.source.equals("/the/root/one.js") || mapping.source.equals("/the/root/two.js"));

            if (mapping.generated.line == previousLine[0]) {
                assertTrue(mapping.generated.column >= previousColumn[0]);
                previousColumn[0] = mapping.generated.column;
            } else {
                previousLine[0] = mapping.generated.line;
                previousColumn[0] = Integer.MIN_VALUE;
            }
        });

        map = SourceMapConsumer.create(TestUtil.testMapNoSourceRoot);
        map.eachMapping().forEach(mapping -> {
            assertTrue(mapping.source.equals("one.js") || mapping.source.equals("two.js"));
        });

        map = SourceMapConsumer.create(TestUtil.testMapEmptySourceRoot);
        map.eachMapping().forEach(mapping -> {
            assertTrue(mapping.source.equals("one.js") || mapping.source.equals("two.js"));
        });
    }

    @Test
    public void testEachMappingForIndexedSourceMaps() throws Exception {
        SourceMapConsumer map = SourceMapConsumer.create(TestUtil.indexedTestMap);
        int[] previousLine = new int[1];
        previousLine[0] = Integer.MIN_VALUE;
        int[] previousColumn = new int[1];
        previousColumn[0] = Integer.MIN_VALUE;
        map.eachMapping().forEach(mapping -> {
            assertTrue(mapping.generated.line >= previousLine[0]);

            if (mapping.source != null) {
                assertEquals(mapping.source.indexOf(TestUtil.testMap.sourceRoot), 0);
            }

            if (mapping.generated.line == previousLine[0]) {
                assertTrue(mapping.generated.column >= previousColumn[0]);
                previousColumn[0] = mapping.generated.column;
            } else {
                previousLine[0] = mapping.generated.line;
                previousColumn[0] = Integer.MIN_VALUE;
            }
        });
    }

    @Test
    public void testIteratingOverMappingsInADifferentOrder() throws Exception {
        SourceMapConsumer map = SourceMapConsumer.create(TestUtil.testMap);
        int[] previousLine = new int[1];
        previousLine[0] = Integer.MIN_VALUE;
        int[] previousColumn = new int[1];
        previousColumn[0] = Integer.MIN_VALUE;
        String[] previousSource = new String[1];
        previousSource[0] = "";
        map.eachMapping(Order.original).forEach(mapping -> {
            assertTrue(mapping.source.compareTo(previousSource[0]) >= 0);

            if (mapping.source == previousSource[0]) {
                assertTrue(mapping.original.line >= previousLine[0]);

                if (mapping.original.line == previousLine[0]) {
                    assertTrue(mapping.original.column >= previousColumn[0]);
                    previousColumn[0] = mapping.original.column;
                } else {
                    previousLine[0] = mapping.original.line;
                    previousColumn[0] = Integer.MIN_VALUE;
                }
            } else {
                previousSource[0] = mapping.source;
                previousLine[0] = Integer.MIN_VALUE;
                previousColumn[0] = Integer.MIN_VALUE;
            }
        });
    }

    @Test
    public void testIteratingOverMappingsInADifferentOrderInIndexedSourceMaps() throws Exception {
        SourceMapConsumer map = SourceMapConsumer.create(TestUtil.indexedTestMap);
        int[] previousLine = new int[1];
        previousLine[0] = Integer.MIN_VALUE;
        int[] previousColumn = new int[1];
        previousColumn[0] = Integer.MIN_VALUE;
        String[] previousSource = new String[1];
        previousSource[0] = "";
        map.eachMapping(Order.original).forEach(mapping -> {
            assertTrue(mapping.source.compareTo(previousSource[0]) >= 0);

            if (mapping.source == previousSource[0]) {
                assertTrue(mapping.original.line >= previousLine[0]);

                if (mapping.original.line == previousLine[0]) {
                    assertTrue(mapping.original.column >= previousColumn[0]);
                    previousColumn[0] = mapping.original.column;
                } else {
                    previousLine[0] = mapping.original.line;
                    previousColumn[0] = Integer.MIN_VALUE;
                }
            } else {
                previousSource[0] = mapping.source;
                previousLine[0] = Integer.MIN_VALUE;
                previousColumn[0] = Integer.MIN_VALUE;
            }
        });
    }

    @Test
    public void testThatTheSourcesContentFieldHasTheOriginalSources() throws Exception {

        SourceMapConsumer map = SourceMapConsumer.create(TestUtil.testMapWithSourcesContent);
        List<String> sourcesContent = map.sourcesContent;

        assertEquals(sourcesContent.get(0), " ONE.foo = function (bar) {\n   return baz(bar);\n };");
        assertEquals(sourcesContent.get(1), " TWO.inc = function (n) {\n   return n + 1;\n };");
        assertEquals(sourcesContent.size(), 2);
    };

    @Test
    public void testThatWeCanGetTheOriginalSourcesForTheSources() throws Exception {
        SourceMapConsumer map = SourceMapConsumer.create(TestUtil.testMapWithSourcesContent);
        List<String> sources = map.sources();

        assertEquals(map.sourceContentFor(sources.get(0)), " ONE.foo = function (bar) {\n   return baz(bar);\n };");
        assertEquals(map.sourceContentFor(sources.get(1)), " TWO.inc = function (n) {\n   return n + 1;\n };");
        assertEquals(map.sourceContentFor("one.js"), " ONE.foo = function (bar) {\n   return baz(bar);\n };");
        assertEquals(map.sourceContentFor("two.js"), " TWO.inc = function (n) {\n   return n + 1;\n };");
        try {
            map.sourceContentFor("");
            fail("Excepted error");
            ;
        } catch (RuntimeException e) {
            // ok
        }

        try {
            map.sourceContentFor("/the/root/three.js");
            fail("Excepted error");
        } catch (RuntimeException e) {
            // ok
        }

        try {
            map.sourceContentFor("three.js");
            fail("Excepted error");
        } catch (RuntimeException e) {
            // ok
        }
    }

    @Test
    public void testThatWeCanGetTheOriginalSourceContentWithRelativeSourcePaths() throws Exception {

        SourceMapConsumer map = SourceMapConsumer.create(TestUtil.testMapRelativeSources);
        List<String> sources = map.sources();

        assertEquals(map.sourceContentFor(sources.get(0)), " ONE.foo = function (bar) {\n   return baz(bar);\n };");
        assertEquals(map.sourceContentFor(sources.get(1)), " TWO.inc = function (n) {\n   return n + 1;\n };");
        assertEquals(map.sourceContentFor("one.js"), " ONE.foo = function (bar) {\n   return baz(bar);\n };");
        assertEquals(map.sourceContentFor("two.js"), " TWO.inc = function (n) {\n   return n + 1;\n };");

        try {
            map.sourceContentFor("");
            fail("Excepted error");
        } catch (RuntimeException e) {
            // ok
        }

        try {

            map.sourceContentFor("/the/root/three.js");
            fail("Excepted error");
        } catch (RuntimeException e) {
            // ok
        }

        try {

            map.sourceContentFor("three.js");
            fail("Excepted error");
        } catch (RuntimeException e) {
            // ok
        }
    }

    @Test
    public void testThatWeCanGetTheOriginalSourceContentForTheSourcesOnAnIndexedSourceMap() throws Exception {

        SourceMapConsumer map = SourceMapConsumer.create(TestUtil.indexedTestMap);
        List<String> sources = map.sources();

        assertEquals(map.sourceContentFor(sources.get(0)), " ONE.foo = function (bar) {\n   return baz(bar);\n };");
        assertEquals(map.sourceContentFor(sources.get(1)), " TWO.inc = function (n) {\n   return n + 1;\n };");
        assertEquals(map.sourceContentFor("one.js"), " ONE.foo = function (bar) {\n   return baz(bar);\n };");
        assertEquals(map.sourceContentFor("two.js"), " TWO.inc = function (n) {\n   return n + 1;\n };");
        try {
            map.sourceContentFor("");
            fail("Excepted error");
        } catch (RuntimeException e) {
            // ok
        }

        try {
            map.sourceContentFor("/the/root/three.js");
            fail("Excepted error");
        } catch (RuntimeException e) {
            // ok
        }

        try {
            map.sourceContentFor("three.js");
            fail("Excepted error");
        } catch (RuntimeException e) {
            // ok
        }

    }

    @Test
    public void testHasContentsOfAllSources_SingleSourceWithContents() throws Exception {
        // Has one source: foo.js (with contents).
        SourceMapGenerator mapWithContents = new SourceMapGenerator(null, null);
        mapWithContents.addMapping(new Mapping(new Position(1, 10), new Position(1, 10), "foo.js"));
        mapWithContents.setSourceContent("foo.js", "content of foo.js");
        SourceMapConsumer consumer = SourceMapConsumer.create(mapWithContents.toJSON());
        assertTrue(consumer.hasContentsOfAllSources());
    }

    @Test
    public void testHasContentsOfAllSources_SingleSourceWithoutContents() throws Exception {
        // Has one source: foo.js (without contents).
        SourceMapGenerator mapWithoutContents = new SourceMapGenerator(null, null);
        mapWithoutContents.addMapping(new Mapping(new Position(1, 1), new Position(1, 10), "foo.js"));
        SourceMapConsumer consumer = SourceMapConsumer.create(mapWithoutContents.toJSON());
        assertTrue(!consumer.hasContentsOfAllSources());
    }

    @Test
    public void testHasContentsOfAllSources_TwoSourcesWithContents() throws Exception {
        // Has two sources: foo.js (with contents) and bar.js (with contents).
        SourceMapGenerator mapWithBothContents = new SourceMapGenerator(null, null);
        mapWithBothContents.addMapping(new Mapping(new Position(1, 10), new Position(1, 10), "foo.js"));
        mapWithBothContents.addMapping(new Mapping(new Position(1, 10), new Position(1, 10), "bar.js"));
        mapWithBothContents.setSourceContent("foo.js", "content of foo.js");
        mapWithBothContents.setSourceContent("bar.js", "content of bar.js");
        SourceMapConsumer consumer = SourceMapConsumer.create(mapWithBothContents.toJSON());
        assertTrue(consumer.hasContentsOfAllSources());
    };

    @Test
    public void testHasContentsOfAllSources_TwoSourcesOneWithAndOneWithoutContents() throws Exception {
        // Has two sources: foo.js (with contents) and bar.js (without contents).
        SourceMapGenerator mapWithoutSomeContents = new SourceMapGenerator(null, null);
        mapWithoutSomeContents.addMapping(new Mapping(new Position(1, 10), new Position(1, 10), "foo.js"));
        mapWithoutSomeContents.addMapping(new Mapping(new Position(1, 10), new Position(1, 10), "bar.js"));
        mapWithoutSomeContents.setSourceContent("foo.js", "content of foo.js");
        SourceMapConsumer consumer = SourceMapConsumer.create(mapWithoutSomeContents.toJSON());
        assertTrue(!consumer.hasContentsOfAllSources());
    };

    @Test
    public void testSourceRoot_generatedPositionFor() throws Exception {
        SourceMapGenerator map = new SourceMapGenerator("baz.js", "foo/bar");
        map.addMapping(new Mapping(new Position(2, 2), new Position(1, 1), "bang.coffee"));
        map.addMapping(new Mapping(new Position(6, 6), new Position(5, 5), "bang.coffee"));
        SourceMapConsumer map2 = SourceMapConsumer.create(map.toJSON());

        // Should handle without sourceRoot.
        GeneratedPosition pos = map2.generatedPositionFor("bang.coffee", 1, 1, null);

        assertEquals(pos.line.intValue(), 2);
        assertEquals(pos.column.intValue(), 2);

        // Should handle with sourceRoot.
        pos = map2.generatedPositionFor("foo/bar/bang.coffee", 1, 1, null);

        assertEquals(pos.line.intValue(), 2);
        assertEquals(pos.column.intValue(), 2);
    }

    @Test
    public void testSourceRoot_generatedPositionFor_ForPathAboveTheRoot() throws Exception {
        SourceMapGenerator map = new SourceMapGenerator("baz.js", "foo/bar");
        map.addMapping(new Mapping(new Position(2, 2), new Position(1, 1), "../bang.coffee"));
        SourceMapConsumer map2 = SourceMapConsumer.create(map.toJSON());

        // Should handle with sourceRoot.
        GeneratedPosition pos = map2.generatedPositionFor("foo/bang.coffee", 1, 1, null);

        assertEquals(pos.line.intValue(), 2);
        assertEquals(pos.column.intValue(), 2);
    }

    @Test
    public void testAllGeneratedPositionsFor_ForLine() throws Exception {
        SourceMapGenerator map = new SourceMapGenerator("generated.js", null);
        map.addMapping(new Mapping(new Position(2, 2), new Position(1, 1), "foo.coffee"));
        map.addMapping(new Mapping(new Position(2, 2), new Position(1, 1), "bar.coffee"));
        map.addMapping(new Mapping(new Position(3, 2), new Position(2, 1), "bar.coffee"));
        map.addMapping(new Mapping(new Position(3, 3), new Position(2, 2), "bar.coffee"));
        map.addMapping(new Mapping(new Position(4, 2), new Position(3, 1), "bar.coffee"));
        SourceMapConsumer map2 = SourceMapConsumer.create(map.toJSON());

        List<GeneratedPosition> mappings = map2.allGeneratedPositionsFor(2, null, "bar.coffee");

        assertEquals(mappings.size(), 2);
        assertEquals(mappings.get(0).line.intValue(), 3);
        assertEquals(mappings.get(0).column.intValue(), 2);
        assertEquals(mappings.get(1).line.intValue(), 3);
        assertEquals(mappings.get(1).column.intValue(), 3);
    }

    @Test
    public void testAllGeneratedPositionsFor_ForLineFuzzy() throws Exception {
        SourceMapGenerator map = new SourceMapGenerator("generated.js", null);
        map.addMapping(new Mapping(new Position(2, 2), new Position(1, 1), "foo.coffee"));
        map.addMapping(new Mapping(new Position(2, 2), new Position(1, 1), "bar.coffee"));
        map.addMapping(new Mapping(new Position(4, 2), new Position(3, 1), "bar.coffee"));
        SourceMapConsumer map2 = SourceMapConsumer.create(map.toJSON());

        List<GeneratedPosition> mappings = map2.allGeneratedPositionsFor(2, null, "bar.coffee");
        assertEquals(mappings.size(), 1);
        assertEquals(mappings.get(0).line.intValue(), 4);
        assertEquals(mappings.get(0).column.intValue(), 2);
    }

    @Test
    public void testAllGeneratedPositionsFor_ForEmptySourceMap() throws Exception {
        SourceMapGenerator map = new SourceMapGenerator("generated.js", null);
        SourceMapConsumer map2 = SourceMapConsumer.create(map.toJSON());

        List<GeneratedPosition> mappings = map2.allGeneratedPositionsFor(2, null, "bar.coffee");

        assertEquals(mappings.size(), 0);
    }

    @Test
    public void testAllGeneratedPositionsForForColumn() throws Exception {
        SourceMapGenerator map = new SourceMapGenerator("generated.js", null);
        map.addMapping(new Mapping(new Position(1, 2), new Position(1, 1), "foo.coffee"));
        map.addMapping(new Mapping(new Position(1, 3), new Position(1, 1), "foo.coffee"));
        SourceMapConsumer map2 = SourceMapConsumer.create(map.toJSON());

        List<GeneratedPosition> mappings = map2.allGeneratedPositionsFor(1, 1, "foo.coffee");

        assertEquals(mappings.size(), 2);
        assertEquals(mappings.get(0).line.intValue(), 1);
        assertEquals(mappings.get(0).column.intValue(), 2);
        assertEquals(mappings.get(1).line.intValue(), 1);
        assertEquals(mappings.get(1).column.intValue(), 3);
    }

    @Test
    public void testAllGeneratedPositionsFor_ForColumnFuzzy() throws Exception {
        SourceMapGenerator map = new SourceMapGenerator("generated.js", null);
        map.addMapping(new Mapping(new Position(1, 2), new Position(1, 1), "foo.coffee"));
        map.addMapping(new Mapping(new Position(1, 3), new Position(1, 1), "foo.coffee"));
        SourceMapConsumer map2 = SourceMapConsumer.create(map.toJSON());

        List<GeneratedPosition> mappings = map2.allGeneratedPositionsFor(1, 0, "foo.coffee");

        assertEquals(mappings.size(), 2);
        assertEquals(mappings.get(0).line.intValue(), 1);
        assertEquals(mappings.get(0).column.intValue(), 2);
        assertEquals(mappings.get(1).line.intValue(), 1);
        assertEquals(mappings.get(1).column.intValue(), 3);
    }

    @Test
    public void testAllGeneratedPositionsFor_ForColumnOnDifferentLineFuzzy() throws Exception {
        SourceMapGenerator map = new SourceMapGenerator("generated.js", null);
        map.addMapping(new Mapping(new Position(2, 2), new Position(2, 1), "foo.coffee"));
        map.addMapping(new Mapping(new Position(2, 3), new Position(2, 1), "foo.coffee"));
        SourceMapConsumer map2 = SourceMapConsumer.create(map.toJSON());

        List<GeneratedPosition> mappings = map2.allGeneratedPositionsFor(1, 0, "foo.coffee");

        assertEquals(mappings.size(), 0);
    }

    @Test
    public void testComputeColumnSpans() throws Exception {
        SourceMapGenerator map = new SourceMapGenerator("generated.js", null);
        map.addMapping(new Mapping(new Position(1, 1), new Position(1, 1), "foo.coffee"));
        map.addMapping(new Mapping(new Position(2, 1), new Position(2, 1), "foo.coffee"));
        map.addMapping(new Mapping(new Position(2, 10), new Position(2, 2), "foo.coffee"));
        map.addMapping(new Mapping(new Position(2, 20), new Position(2, 3), "foo.coffee"));
        map.addMapping(new Mapping(new Position(3, 1), new Position(3, 1), "foo.coffee"));
        map.addMapping(new Mapping(new Position(3, 2), new Position(3, 2), "foo.coffee"));
        SourceMapConsumer map2 = SourceMapConsumer.create(map.toJSON());

        ((BasicSourceMapConsumer) map2).computeColumnSpans();

        List<GeneratedPosition> mappings = map2.allGeneratedPositionsFor(1, null, "foo.coffee");

        assertEquals(mappings.size(), 1);
        assertEquals(mappings.get(0).lastColumn.intValue(), Integer.MAX_VALUE);

        mappings = map2.allGeneratedPositionsFor(2, null, "foo.coffee");

        assertEquals(mappings.size(), 3);
        assertEquals(mappings.get(0).lastColumn.intValue(), 9);
        assertEquals(mappings.get(1).lastColumn.intValue(), 19);
        assertEquals(mappings.get(2).lastColumn.intValue(), Integer.MAX_VALUE);

        mappings = map2.allGeneratedPositionsFor(3, null, "foo.coffee");

        assertEquals(mappings.size(), 2);
        assertEquals(mappings.get(0).lastColumn.intValue(), 1);
        assertEquals(mappings.get(1).lastColumn.intValue(), Integer.MAX_VALUE);
    }

    @Test
    public void testSourceRoot_originalPositionFor() throws Exception {
        SourceMapGenerator map = new SourceMapGenerator("baz.js", "foo/bar");
        map.addMapping(new Mapping(new Position(2, 2), new Position(1, 1), "bang.coffee"));
        SourceMapConsumer map2 = SourceMapConsumer.create(map.toJSON());

        OriginalPosition pos = map2.originalPositionFor(2, 2, null);

        // Should always have the prepended source root
        assertEquals(pos.source, "foo/bar/bang.coffee");
        assertEquals(pos.line.intValue(), 1);
        assertEquals(pos.column.intValue(), 1);
    }

    @Test
    public void testGithubIssue_56() throws Exception {
        SourceMapGenerator map = new SourceMapGenerator("www.example.com/foo.js", "http://");
        map.addMapping(new Mapping(new Position(2, 2), new Position(1, 1), "www.example.com/original.js"));
        SourceMapConsumer map2 = SourceMapConsumer.create(map.toJSON());

        List<String> sources = map2.sources();

        assertEquals(sources.size(), 1);
        assertEquals(sources.get(0), "http://www.example.com/original.js");
    }

    @Test
    public void testGithubIssue_43() throws Exception {
        SourceMapGenerator map = new SourceMapGenerator("foo.js", "http://example.com");
        map.addMapping(new Mapping(new Position(2, 2), new Position(1, 1), "http://cdn.example.com/original.js"));
        SourceMapConsumer map2 = SourceMapConsumer.create(map.toJSON());

        List<String> sources = map2.sources();
        assertEquals(sources.size(), 1, "Should only be one source.");
        assertEquals(sources.get(0), "http://cdn.example.com/original.js", "Should not be joined with the sourceRoot.");
    }

    @Test
    public void testAbsolutePathButSameHostSources() throws Exception {
        SourceMapGenerator map = new SourceMapGenerator("foo.js", "http://example.com/foo/bar");
        map.addMapping(new Mapping(new Position(2, 2), new Position(1, 1), "/original.js"));
        SourceMapConsumer map2 = SourceMapConsumer.create(map.toJSON());

        List<String> sources = map2.sources();
        assertEquals(sources.size(), 1, "Should only be one source.");
        assertEquals(sources.get(0), "http://example.com/original.js", "Source should be relative the host of the source root.");
    }

    @Test
    public void testIndexedSourceMapErrorsWhenSectionsAreOutOfOrderByLine() throws Exception {
        // Make a deep copy of the indexedTestMap
        SourceMap misorderedIndexedTestMap = TestUtil.indexedTestMap;

        misorderedIndexedTestMap.sections.get(0).offset = new Position(2, 0);

        try {
            SourceMapConsumer.create(misorderedIndexedTestMap);
            fail("expecting an exception");
        } catch (RuntimeException e) {
            // ok
        }
    }

    @Test
    public void testGithubIssue_64() throws Exception {
        SourceMap sourceMap = new SourceMap();
        sourceMap.version = 3;
        sourceMap.file = "foo.js";
        sourceMap.sourceRoot = "http://example.com/";
        sourceMap.sources = Arrays.asList("/a");
        sourceMap.names = Collections.emptyList();
        sourceMap.mappings = "AACA";
        sourceMap.sourcesContent = Arrays.asList("foo");
        SourceMapConsumer map = SourceMapConsumer.create(sourceMap);
        assertEquals(map.sourceContentFor("a"), "foo");
        assertEquals(map.sourceContentFor("/a"), "foo");
    }

    @Test
    public void testBug_885597() throws Exception {
        SourceMap sourceMap = new SourceMap();
        sourceMap.version = 3;
        sourceMap.file = "foo.js";
        sourceMap.sourceRoot = "file:///Users/AlGore/Invented/The/Internet/";
        sourceMap.sources = Arrays.asList("/a");
        sourceMap.names = Collections.emptyList();
        sourceMap.mappings = "AACA";
        sourceMap.sourcesContent = Arrays.asList("foo");
        SourceMapConsumer map = SourceMapConsumer.create(sourceMap);
        String s = map.sources().get(0);
        assertEquals(map.sourceContentFor(s), "foo");
    }

    @Test
    public void testGithubIssue_72_DuplicateSources() throws Exception {
        SourceMap sourceMap = new SourceMap();
        sourceMap.version = 3;
        sourceMap.file = "foo.js";
        sourceMap.sources = Arrays.asList("source1.js", "source1.js", "source3.js");
        sourceMap.names = Collections.emptyList();
        sourceMap.mappings = ";EAAC;;IAEE;;MEEE";
        sourceMap.sourceRoot = "http://example.com";
        SourceMapConsumer map = SourceMapConsumer.create(sourceMap);

        OriginalPosition pos = map.originalPositionFor(2, 2, null);
        assertEquals(pos.source, "http://example.com/source1.js");
        assertEquals(pos.line.intValue(), 1);
        assertEquals(pos.column.intValue(), 1);

        pos = map.originalPositionFor(4, 4, null);
        assertEquals(pos.source, "http://example.com/source1.js");
        assertEquals(pos.line.intValue(), 3);
        assertEquals(pos.column.intValue(), 3);

        pos = map.originalPositionFor(6, 6, null);
        assertEquals(pos.source, "http://example.com/source3.js");
        assertEquals(pos.line.intValue(), 5);
        assertEquals(pos.column.intValue(), 5);
    }

    @Test
    public void testGithubIssue_72_DuplicateNames() throws Exception {
        SourceMap sourceMap = new SourceMap();
        sourceMap.version = 3;
        sourceMap.file = "foo.js";
        sourceMap.sources = Arrays.asList("source.js");
        sourceMap.names = Arrays.asList("name1", "name1", "name3");
        sourceMap.mappings = ";EAACA;;IAEEA;;MAEEE";
        sourceMap.sourceRoot = "http://example.com";
        SourceMapConsumer map = SourceMapConsumer.create(sourceMap);

        OriginalPosition pos = map.originalPositionFor(2, 2, null);
        assertEquals(pos.name, "name1");
        assertEquals(pos.line.intValue(), 1);
        assertEquals(pos.column.intValue(), 1);

        pos = map.originalPositionFor(4, 4, null);
        assertEquals(pos.name, "name1");
        assertEquals(pos.line.intValue(), 3);
        assertEquals(pos.column.intValue(), 3);

        pos = map.originalPositionFor(6, 6, null);
        assertEquals(pos.name, "name3");
        assertEquals(pos.line.intValue(), 5);
        assertEquals(pos.column.intValue(), 5);
    }

    @Test
    public void testSourceMapConsumer_fromSourceMap() throws Exception {
        SourceMapGenerator smg = new SourceMapGenerator("foo.js", "http://example.com/");
        smg.addMapping(new Mapping(new Position(2, 2), new Position(1, 1), "bar.js"));
        smg.addMapping(new Mapping(new Position(4, 4), new Position(2, 2), "baz.js", "dirtMcGirt"));
        smg.setSourceContent("baz.js", "baz.js content");

        SourceMapConsumer smc = SourceMapConsumer.fromSourceMap(smg);
        assertEquals(smc.file, "foo.js");
        assertEquals(smc.sourceRoot, "http://example.com/");
        assertEquals(smc.sources().size(), 2);
        assertEquals(smc.sources().get(0), "http://example.com/bar.js");
        assertEquals(smc.sources().get(1), "http://example.com/baz.js");
        assertEquals(smc.sourceContentFor("baz.js"), "baz.js content");

        OriginalPosition pos = smc.originalPositionFor(2, 2, null);
        assertEquals(pos.line.intValue(), 1);
        assertEquals(pos.column.intValue(), 1);
        assertEquals(pos.source, "http://example.com/bar.js");
        assertNull(pos.name);

        GeneratedPosition pos2 = smc.generatedPositionFor("http://example.com/bar.js", 1, 1, null);
        assertEquals(pos2.line.intValue(), 2);
        assertEquals(pos2.column.intValue(), 2);

        pos = smc.originalPositionFor(4, 4, null);
        assertEquals(pos.line.intValue(), 2);
        assertEquals(pos.column.intValue(), 2);
        assertEquals(pos.source, "http://example.com/baz.js");
        assertEquals(pos.name, "dirtMcGirt");

        pos2 = smc.generatedPositionFor("http://example.com/baz.js", 2, 2, null);
        assertEquals(pos2.line.intValue(), 4);
        assertEquals(pos2.column.intValue(), 4);
    }

    @Test
    public void testIssue_191() throws Exception {
        SourceMapGenerator generator = new SourceMapGenerator("a.css", null);
        generator.addMapping(new Mapping(new Position(1, 0), new Position(1, 0), "b.css"));

        // Create a SourceMapConsumer from the SourceMapGenerator, ...
        SourceMapConsumer.fromSourceMap(generator);
        // ... and then try and use the SourceMapGenerator again. This should not
        // throw.
        generator.toJSON();
    }

    @Test
    public void testSourcesWhereTheirPrefixIsTheSourceRoot_Issue_199() throws Exception {
        SourceMap testSourceMap = new SourceMap();
        testSourceMap.version = 3;
        testSourceMap.sources = Arrays.asList("/source/app/app/app.js");
        testSourceMap.names = Arrays.asList("System");
        testSourceMap.mappings = "AAAAA";
        testSourceMap.file = "app/app.js";
        testSourceMap.sourcesContent = Arrays.asList("'use strict';");
        testSourceMap.sourceRoot = "/source/";

        SourceMapConsumer consumer = SourceMapConsumer.create(testSourceMap);

        consumer.sources().stream().forEach(s -> assertNotNull(consumer.sourceContentFor(s)));
        testSourceMap.sources.stream().forEach(s -> assertNotNull(consumer.sourceContentFor(s)));
    }

    @Test
    public void testSourcesWhereTheirPrefixIsTheSourceRootAnTheSourceRootIsAUrl_Issue_199() throws Exception {
        SourceMap testSourceMap = new SourceMap();
        testSourceMap.version = 3;
        testSourceMap.sources = Arrays.asList("http://example.com/source/app/app/app.js");
        testSourceMap.names = Arrays.asList("System");
        testSourceMap.mappings = "AAAAA";
        testSourceMap.sourcesContent = Arrays.asList("'use strict';");
        testSourceMap.sourceRoot = "http://example.com/source/";

        SourceMapConsumer consumer = SourceMapConsumer.create(testSourceMap);

        consumer.sources().stream().forEach(s -> assertNotNull(consumer.sourceContentFor(s)));
        testSourceMap.sources.stream().forEach(s -> assertNotNull(consumer.sourceContentFor(s)));
    };

}
