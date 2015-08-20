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
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

public class SourceMapGeneratorTest {

    @Test
    public void testSomeSimpleStuff() throws Exception {
        SourceMap map = new SourceMapGenerator("foo.js", ".").toJSON();
        assertNotNull(map.file);
        assertNotNull(map.sourceRoot);

        map = new SourceMapGenerator(null, null).toJSON();
        assertNull(map.file);
        assertNull(map.sourceRoot);
    }

    @Test
    public void testThatTheCorrectMappingsAreBeingGenerated() throws Exception {
        SourceMapGenerator map = new SourceMapGenerator("min.js", "/the/root");

        map.addMapping(new Mapping(new Position(1, 1), new Position(1, 1), "one.js"));
        map.addMapping(new Mapping(new Position(1, 5), new Position(1, 5), "one.js"));
        map.addMapping(new Mapping(new Position(1, 9), new Position(1, 11), "one.js"));
        map.addMapping(new Mapping(new Position(1, 18), new Position(1, 21), "one.js", "bar"));
        map.addMapping(new Mapping(new Position(1, 21), new Position(2, 3), "one.js"));
        map.addMapping(new Mapping(new Position(1, 28), new Position(2, 10), "one.js", "baz"));
        map.addMapping(new Mapping(new Position(1, 32), new Position(2, 14), "one.js", "bar"));
        map.addMapping(new Mapping(new Position(2, 1), new Position(1, 1), "two.js"));
        map.addMapping(new Mapping(new Position(2, 5), new Position(1, 5), "two.js"));
        map.addMapping(new Mapping(new Position(2, 9), new Position(1, 11), "two.js"));
        map.addMapping(new Mapping(new Position(2, 18), new Position(1, 21), "two.js", "n"));
        map.addMapping(new Mapping(new Position(2, 21), new Position(2, 3), "two.js"));
        map.addMapping(new Mapping(new Position(2, 28), new Position(2, 10), "two.js", "n"));

        TestUtil.assertEqualMaps(map.toJSON(), TestUtil.testMap);
    }

    @Test
    public void testThatAddingAMappingWithAnEmptyStringNameDoesNotBreakGeneration() throws Exception {
        SourceMapGenerator map = new SourceMapGenerator("generated-foo.js", ".");
        map.addMapping(new Mapping(new Position(1, 1), new Position(1, 1), "bar.js", ""));
        map.toJSON();
    }

    @Test
    public void testThatSourceContentCanBeSet() throws Exception {
        SourceMapGenerator map = new SourceMapGenerator("min.js", "/the/root");
        map.addMapping(new Mapping(new Position(1, 1), new Position(1, 1), "one.js"));
        map.addMapping(new Mapping(new Position(2, 1), new Position(1, 1), "two.js"));
        map.setSourceContent("one.js", "one file content");

        SourceMap map2 = map.toJSON();
        assertEquals(map2.sources.get(0), "one.js");
        assertEquals(map2.sources.get(1), "two.js");
        assertEquals(map2.sourcesContent.get(0), "one file content");
        assertEquals(map2.sourcesContent.get(1), null);
    }

    @Test
    public void testFromSourceMap() throws Exception {
        SourceMapGenerator map = SourceMapGenerator.fromSourceMap(SourceMapConsumer.create(TestUtil.testMap));
        TestUtil.assertEqualMaps(map.toJSON(), TestUtil.testMap);
    }

    @Test
    public void testFromSourceMapWithSourcesContent() throws Exception {
        SourceMapGenerator map = SourceMapGenerator.fromSourceMap(SourceMapConsumer.create(TestUtil.testMapWithSourcesContent));
        TestUtil.assertEqualMaps(map.toJSON(), TestUtil.testMapWithSourcesContent);
    }

    // @Test
    // public void testApplySourceMap() throws Exception {
    // var node = new SourceNode(null, null, null, [
    // new SourceNode(2, 0, "fileX", "lineX2\n"),
    // "genA1\n",
    // new SourceNode(2, 0, "fileY", "lineY2\n"),
    // "genA2\n",
    // new SourceNode(1, 0, "fileX", "lineX1\n"),
    // "genA3\n",
    // new SourceNode(1, 0, "fileY", "lineY1\n")
    // ]);
    // var mapStep1 = node.toStringWithSourceMap({
    // file: "fileA"
    // }).map;
    // mapStep1.setSourceContent("fileX", "lineX1\nlineX2\n");
    // mapStep1 = mapStep1.toJSON();
    //
    // node = new SourceNode(null, null, null, [
    // "gen1\n",
    // new SourceNode(1, 0, "fileA", "lineA1\n"),
    // new SourceNode(2, 0, "fileA", "lineA2\n"),
    // new SourceNode(3, 0, "fileA", "lineA3\n"),
    // new SourceNode(4, 0, "fileA", "lineA4\n"),
    // new SourceNode(1, 0, "fileB", "lineB1\n"),
    // new SourceNode(2, 0, "fileB", "lineB2\n"),
    // "gen2\n"
    // ]);
    // var mapStep2 = node.toStringWithSourceMap({
    // file: "fileGen"
    // }).map;
    // mapStep2.setSourceContent("fileB", "lineB1\nlineB2\n");
    // mapStep2 = mapStep2.toJSON();
    //
    // node = new SourceNode(null, null, null, [
    // "gen1\n",
    // new SourceNode(2, 0, "fileX", "lineA1\n"),
    // new SourceNode(2, 0, "fileA", "lineA2\n"),
    // new SourceNode(2, 0, "fileY", "lineA3\n"),
    // new SourceNode(4, 0, "fileA", "lineA4\n"),
    // new SourceNode(1, 0, "fileB", "lineB1\n"),
    // new SourceNode(2, 0, "fileB", "lineB2\n"),
    // "gen2\n"
    // ]);
    // var expectedMap = node.toStringWithSourceMap({
    // file: "fileGen"
    // }).map;
    // expectedMap.setSourceContent("fileX", "lineX1\nlineX2\n");
    // expectedMap.setSourceContent("fileB", "lineB1\nlineB2\n");
    // expectedMap = expectedMap.toJSON();
    //
    // // apply source map "mapStep1" to "mapStep2"
    // var generator = SourceMapGenerator.fromSourceMap(SourceMapConsumer.create(mapStep2));
    // generator.applySourceMap(SourceMapConsumer.create(mapStep1));
    // var actualMap = generator.toJSON();
    //
    // TestUtil.assertEqualMaps(actualMap, expectedMap);
    // }

    @Test
    public void testApplySourceMapThrowsWhenFileIsMissing() throws Exception {
        SourceMapGenerator map = new SourceMapGenerator("test.js", null);
        SourceMapGenerator map2 = new SourceMapGenerator(null, null);
        try {
            map.applySourceMap(SourceMapConsumer.create(map2.toJSON()), null, null);
            fail("exception expected");
        } catch (RuntimeException e) {
            // ok
        }
    }

    private SourceMap buildExpectedMap(String... sources) {
        SourceMapGenerator map = new SourceMapGenerator("bundle.min.js", "..");
        map.addMapping(new Mapping(new Position(1, 1), new Position(2, 2), sources[0]));
        map.setSourceContent(sources[0], "foo coffee");
        map.addMapping(new Mapping(new Position(11, 11), new Position(12, 12), sources[1]));
        map.setSourceContent(sources[1], "bar coffee");
        map.addMapping(new Mapping(new Position(21, 21), new Position(22, 22), sources[2]));
        map.setSourceContent(sources[2], "baz coffee");
        return map.toJSON();
    }

    private SourceMap buildActualMap(SourceMapConsumer bundleMap2, SourceMapConsumer minifiedMap2, String aSourceMapPath) {
        SourceMapGenerator map = SourceMapGenerator.fromSourceMap(minifiedMap2);
        // Note that relying on `bundleMap.file` (which is simply "bundle.js")
        // instead of supplying the second parameter wouldn"t work here.
        map.applySourceMap(bundleMap2, "../temp/bundle.js", aSourceMapPath);
        return map.toJSON();
    }

    @Test
    public void testTheTwoAdditionalParametersOfApplySourceMap() throws Exception {
        // Assume the following directory structure:
        //
        // http://foo.org/
        // bar.coffee
        // app/
        // coffee/
        // foo.coffee
        // temp/
        // bundle.js
        // temp_maps/
        // bundle.js.map
        // public/
        // bundle.min.js
        // bundle.min.js.map
        //
        // http://www.example.com/
        // baz.coffee

        SourceMapGenerator bundleMap = new SourceMapGenerator("bundle.js", null);
        bundleMap.addMapping(new Mapping(new Position(3, 3), new Position(2, 2), "../../coffee/foo.coffee"));
        bundleMap.setSourceContent("../../coffee/foo.coffee", "foo coffee");
        bundleMap.addMapping(new Mapping(new Position(13, 13), new Position(12, 12), "/bar.coffee"));
        bundleMap.setSourceContent("/bar.coffee", "bar coffee");
        bundleMap.addMapping(new Mapping(new Position(23, 23), new Position(22, 22), "http://www.example.com/baz.coffee"));
        bundleMap.setSourceContent("http://www.example.com/baz.coffee", "baz coffee");
        SourceMapConsumer bundleMap2 = SourceMapConsumer.create(bundleMap.toJSON());

        SourceMapGenerator minifiedMap = new SourceMapGenerator("bundle.min.js", "..");
        minifiedMap.addMapping(new Mapping(new Position(1, 1), new Position(3, 3), "temp/bundle.js"));
        minifiedMap.addMapping(new Mapping(new Position(11, 11), new Position(13, 13), "temp/bundle.js"));
        minifiedMap.addMapping(new Mapping(new Position(21, 21), new Position(23, 23), "temp/bundle.js"));
        SourceMapConsumer minifiedMap2 = SourceMapConsumer.create(minifiedMap.toJSON());

        TestUtil.assertEqualMaps(buildActualMap(bundleMap2, minifiedMap2, "../temp/temp_maps"),
                buildExpectedMap("coffee/foo.coffee", "/bar.coffee", "http://www.example.com/baz.coffee"));

        TestUtil.assertEqualMaps(buildActualMap(bundleMap2, minifiedMap2, "/app/temp/temp_maps"),
                buildExpectedMap("/app/coffee/foo.coffee", "/bar.coffee", "http://www.example.com/baz.coffee"));

        TestUtil.assertEqualMaps(buildActualMap(bundleMap2, minifiedMap2, "http://foo.org/app/temp/temp_maps"),
                buildExpectedMap("http://foo.org/app/coffee/foo.coffee", "http://foo.org/bar.coffee", "http://www.example.com/baz.coffee"));

        // If the third parameter is omitted or set to the current working
        // directory we get incorrect source paths:

        TestUtil.assertEqualMaps(buildActualMap(bundleMap2, minifiedMap2, null),
                buildExpectedMap("../coffee/foo.coffee", "/bar.coffee", "http://www.example.com/baz.coffee"));

        TestUtil.assertEqualMaps(buildActualMap(bundleMap2, minifiedMap2, ""),
                buildExpectedMap("../coffee/foo.coffee", "/bar.coffee", "http://www.example.com/baz.coffee"));

        TestUtil.assertEqualMaps(buildActualMap(bundleMap2, minifiedMap2, "."),
                buildExpectedMap("../coffee/foo.coffee", "/bar.coffee", "http://www.example.com/baz.coffee"));

        TestUtil.assertEqualMaps(buildActualMap(bundleMap2, minifiedMap2, "./"),
                buildExpectedMap("../coffee/foo.coffee", "/bar.coffee", "http://www.example.com/baz.coffee"));
    }

    private void assertName(String coffeeName, String jsName, String expectedName) {
        SourceMapGenerator minifiedMap = new SourceMapGenerator("test.js.min", null);
        minifiedMap.addMapping(new Mapping(new Position(1, 4), new Position(1, 4), "test.js", jsName));

        SourceMapGenerator coffeeMap = new SourceMapGenerator("test.js", null);
        coffeeMap.addMapping(new Mapping(new Position(1, 4), new Position(1, 0), "test.coffee", coffeeName));

        minifiedMap.applySourceMap(SourceMapConsumer.create(coffeeMap.toJSON()), null, null);

        SourceMapConsumer.create(minifiedMap.toJSON()).eachMapping().forEach(mapping -> {
            assertEquals(mapping.name, expectedName);
        });
    }

    @Test
    public void testApplySourceMapNameHandling() throws Exception {
        // Imagine some CoffeeScript code being compiled into JavaScript and then
        // minified.

        // `foo = 1` -> `var foo = 1;` -> `var a=1`
        // CoffeeScript doesn’t rename variables, so there’s no need for it to
        // provide names in its source maps. Minifiers do rename variables and
        // therefore do provide names in their source maps. So that name should be
        // retained if the original map lacks names.
        assertName(null, "foo", "foo");

        // `foo = 1` -> `var coffee$foo = 1;` -> `var a=1`
        // Imagine that CoffeeScript prefixed all variables with `coffee$`. Even
        // though the minifier then also provides a name, the original name is
        // what corresponds to the source.
        assertName("foo", "coffee$foo", "foo");

        // `foo = 1` -> `var coffee$foo = 1;` -> `var coffee$foo=1`
        // Minifiers can turn off variable mangling. Then there’s no need to
        // provide names in the source map, but the names from the original map are
        // still needed.
        assertName("foo", null, "foo");

        // `foo = 1` -> `var foo = 1;` -> `var foo=1`
        // No renaming at all.
        assertName(null, null, null);
    }

    @Test
    public void testSortingWithDuplicateGeneratedMappings() throws Exception {
        SourceMapGenerator map = new SourceMapGenerator("test.js", null);
        map.addMapping(new Mapping(new Position(3, 0), new Position(2, 0), "a.js"));
        map.addMapping(new Mapping(new Position(2, 0)));
        map.addMapping(new Mapping(new Position(2, 0)));
        map.addMapping(new Mapping(new Position(1, 0), new Position(1, 0), "a.js"));

        SourceMap sourceMap = new SourceMap();
        sourceMap.version = 3;
        sourceMap.file = "test.js";
        sourceMap.sources = Arrays.asList("a.js");
        sourceMap.names = Collections.emptyList();
        sourceMap.mappings = "AAAA;A;AACA";

        TestUtil.assertEqualMaps(map.toJSON(), sourceMap);
    }

    @Test
    public void testIgnoreDuplicateMappings() throws Exception {
        // null original source location
        Mapping nullMapping1 = new Mapping(new Position(1, 0));
        Mapping nullMapping2 = new Mapping(new Position(2, 2));

        SourceMapGenerator map1 = new SourceMapGenerator("min.js", "/the/root");
        SourceMapGenerator map2 = new SourceMapGenerator("min.js", "/the/root");

        map1.addMapping(nullMapping1);
        map1.addMapping(nullMapping1);

        map2.addMapping(nullMapping1);

        TestUtil.assertEqualMaps(map1.toJSON(), map2.toJSON());

        map1.addMapping(nullMapping2);
        map1.addMapping(nullMapping1);

        map2.addMapping(nullMapping2);

        TestUtil.assertEqualMaps(map1.toJSON(), map2.toJSON());

        // original source location
        Mapping srcMapping1 = new Mapping(new Position(1, 0), new Position(11, 0), "srcMapping1.js");
        Mapping srcMapping2 = new Mapping(new Position(2, 2), new Position(11, 0), "srcMapping2.js");

        map1 = new SourceMapGenerator("min.js", "/the/root");
        map2 = new SourceMapGenerator("min.js", "/the/root");

        map1.addMapping(srcMapping1);
        map1.addMapping(srcMapping1);

        map2.addMapping(srcMapping1);

        TestUtil.assertEqualMaps(map1.toJSON(), map2.toJSON());

        map1.addMapping(srcMapping2);
        map1.addMapping(srcMapping1);

        map2.addMapping(srcMapping2);

        TestUtil.assertEqualMaps(map1.toJSON(), map2.toJSON());

        // full original source and name information
        Mapping fullMapping1 = new Mapping(new Position(1, 0), new Position(11, 0), "fullMapping1.js",
                "fullMapping1");
        Mapping fullMapping2 = new Mapping(new Position(2, 2), new Position(11, 0), "fullMapping2.js",
                "fullMapping2");

        map1 = new SourceMapGenerator("min.js", "/the/root");
        map2 = new SourceMapGenerator("min.js", "/the/root");

        map1.addMapping(fullMapping1);
        map1.addMapping(fullMapping1);

        map2.addMapping(fullMapping1);

        TestUtil.assertEqualMaps(map1.toJSON(), map2.toJSON());

        map1.addMapping(fullMapping2);
        map1.addMapping(fullMapping1);

        map2.addMapping(fullMapping2);

        TestUtil.assertEqualMaps(map1.toJSON(), map2.toJSON());
    }

    @Test
    public void testGithubIssue_72_CheckForDuplicateNamesOrSources() throws Exception {
        SourceMapGenerator map = new SourceMapGenerator("test.js", null);
        map.addMapping(new Mapping(new Position(1, 1), new Position(2, 2), "a.js", "foo"));
        map.addMapping(new Mapping(new Position(3, 3), new Position(4, 4), "a.js", "foo"));

        SourceMap sourceMap = new SourceMap();
        sourceMap.version = 3;
        sourceMap.file = "test.js";
        sourceMap.sources = Arrays.asList("a.js");
        sourceMap.names = Arrays.asList("foo");
        sourceMap.mappings = "CACEA;;GAEEA";

        TestUtil.assertEqualMaps(map.toJSON(), sourceMap);
    }

    @Test
    public void testSettingSourcesContentToNullWhenAlreadyNull() throws Exception {
        SourceMapGenerator smg = new SourceMapGenerator("foo.js", null);
        smg.setSourceContent("bar.js", null);
    }

    @Test
    public void testApplySourceMapWithUnexactMatch() throws Exception {
        SourceMapGenerator map1 = new SourceMapGenerator("bundled-source", null);
        map1.addMapping(new Mapping(new Position(1, 4), new Position(1, 4), "transformed-source"));
        map1.addMapping(new Mapping(new Position(2, 4), new Position(2, 4), "transformed-source"));

        SourceMapGenerator map2 = new SourceMapGenerator("transformed-source", null);
        map2.addMapping(new Mapping(new Position(2, 0), new Position(1, 0), "original-source"));

        SourceMapGenerator expectedMap = new SourceMapGenerator("bundled-source", null);
        expectedMap.addMapping(new Mapping(new Position(1, 4), new Position(1, 4), "transformed-source"));
        expectedMap.addMapping(new Mapping(new Position(2, 4), new Position(1, 0), "original-source"));

        map1.applySourceMap(SourceMapConsumer.create(map2.toJSON()), null, null);

        TestUtil.assertEqualMaps(map1.toJSON(), expectedMap.toJSON());
    }

    @Test
    public void testIssue_192() throws Exception {
        SourceMapGenerator generator = new SourceMapGenerator(null, null);
        generator.addMapping(new Mapping(new Position(1, 10), new Position(1, 10), "a.js"));
        generator.addMapping(new Mapping(new Position(1, 10), new Position(2, 20), "b.js"));

        SourceMapConsumer consumer = SourceMapConsumer.create(generator.toJSON());

        long n = consumer.eachMapping().count();

        assertEquals(n, 2, "Should not de-duplicate mappings that have the same generated positions, but different original positions.");
    }

}
