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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import junit.framework.Assert;

public class TestUtil {

    // This is a test mapping which maps functions from two different files
    // (one.js and two.js) to a minified generated source.
    //
    // Here is one.js:
    //
    // ONE.foo = function (bar) {
    // return baz(bar);
    // };
    //
    // Here is two.js:
    //
    // TWO.inc = function (n) {
    // return n + 1;
    // };
    //
    // And here is the generated code (min.js):
    //
    // ONE.foo=function(a){return baz(a);};
    // TWO.inc=function(a){return a+1;};
    static String testGeneratedCode;
    static SourceMap testMap;
    static SourceMap testMapNoSourceRoot;
    static SourceMap testMapEmptySourceRoot;
    static SourceMap indexedTestMap;
    static SourceMap indexedTestMapDifferentSourceRoots;
    static SourceMap testMapWithSourcesContent;
    static SourceMap testMapRelativeSources;
    static SourceMap emptyMap;

    static {
        testGeneratedCode = " ONE.foo=function(a){return baz(a);};\n" + " TWO.inc=function(a){return a+1;};";
        testMap = new SourceMap();
        testMap.version = 3;
        testMap.file = "min.js";
        testMap.names = Arrays.asList("bar", "baz", "n");
        testMap.sources = Arrays.asList("one.js", "two.js");
        testMap.sourceRoot = "/the/root";
        testMap.mappings = "CAAC,IAAI,IAAM,SAAUA,GAClB,OAAOC,IAAID;CCDb,IAAI,IAAM,SAAUE,GAClB,OAAOA";

        testMapNoSourceRoot = new SourceMap();
        testMapNoSourceRoot.version = 3;
        testMapNoSourceRoot.file = "min.js";
        testMapNoSourceRoot.names = Arrays.asList("bar", "baz", "n");
        testMapNoSourceRoot.sources = Arrays.asList("one.js", "two.js");
        testMapNoSourceRoot.mappings = "CAAC,IAAI,IAAM,SAAUA,GAClB,OAAOC,IAAID;CCDb,IAAI,IAAM,SAAUE,GAClB,OAAOA";

        testMapEmptySourceRoot = new SourceMap();
        testMapEmptySourceRoot.version = 3;
        testMapEmptySourceRoot.file = "min.js";
        testMapEmptySourceRoot.names = Arrays.asList("bar", "baz", "n");
        testMapEmptySourceRoot.sources = Arrays.asList("one.js", "two.js");
        testMapEmptySourceRoot.sourceRoot = "";
        testMapEmptySourceRoot.mappings = "CAAC,IAAI,IAAM,SAAUA,GAClB,OAAOC,IAAID;CCDb,IAAI,IAAM,SAAUE,GAClB,OAAOA";

        // This mapping is identical to above, but uses the indexed format instead.
        indexedTestMap = new SourceMap();
        indexedTestMap.version = 3;
        indexedTestMap.file = "min.js";
        indexedTestMap.sections = new ArrayList<>();
        indexedTestMap.sections.add(new Section());
        indexedTestMap.sections.get(0).offset = new Position(0, 0);
        indexedTestMap.sections.get(0).map = new SourceMap();
        indexedTestMap.sections.get(0).map.version = 3;
        indexedTestMap.sections.get(0).map.sources = Arrays.asList("one.js");
        indexedTestMap.sections.get(0).map.sourcesContent = Arrays.asList(" ONE.foo = function (bar) {\n" + "   return baz(bar);\n" + " };");
        indexedTestMap.sections.get(0).map.names = Arrays.asList("bar", "baz");
        indexedTestMap.sections.get(0).map.mappings = "CAAC,IAAI,IAAM,SAAUA,GAClB,OAAOC,IAAID";
        indexedTestMap.sections.get(0).map.file = "min.js";
        indexedTestMap.sections.get(0).map.sourceRoot = "/the/root";
        indexedTestMap.sections.add(new Section());
        indexedTestMap.sections.get(1).offset = new Position(1, 0);
        indexedTestMap.sections.get(1).map = new SourceMap();
        indexedTestMap.sections.get(1).map.version = 3;
        indexedTestMap.sections.get(1).map.sources = Arrays.asList("two.js");
        indexedTestMap.sections.get(1).map.sourcesContent = Arrays.asList(" TWO.inc = function (n) {\n" + "   return n + 1;\n" + " };");
        indexedTestMap.sections.get(1).map.names = Arrays.asList("n");
        indexedTestMap.sections.get(1).map.mappings = "CAAC,IAAI,IAAM,SAAUA,GAClB,OAAOA";
        indexedTestMap.sections.get(1).map.file = "min.js";
        indexedTestMap.sections.get(1).map.sourceRoot = "/the/root";

        indexedTestMapDifferentSourceRoots = new SourceMap();

        indexedTestMapDifferentSourceRoots.version = 3;
        indexedTestMapDifferentSourceRoots.file = "min.js";
        indexedTestMapDifferentSourceRoots.sections = new ArrayList<>();
        indexedTestMapDifferentSourceRoots.sections.add(new Section());
        indexedTestMapDifferentSourceRoots.sections.get(0).offset = new Position(0, 0);
        indexedTestMapDifferentSourceRoots.sections.get(0).map = new SourceMap();
        indexedTestMapDifferentSourceRoots.sections.get(0).map.version = 3;
        indexedTestMapDifferentSourceRoots.sections.get(0).map.sources = Arrays.asList("one.js");
        indexedTestMapDifferentSourceRoots.sections.get(0).map.sourcesContent = Arrays
                .asList(" ONE.foo = function (bar) {\n" + "   return baz(bar);\n" + " };");
        indexedTestMapDifferentSourceRoots.sections.get(0).map.names = Arrays.asList("bar", "baz");
        indexedTestMapDifferentSourceRoots.sections.get(0).map.mappings = "CAAC,IAAI,IAAM,SAAUA,GAClB,OAAOC,IAAID";
        indexedTestMapDifferentSourceRoots.sections.get(0).map.file = "min.js";
        indexedTestMapDifferentSourceRoots.sections.get(0).map.sourceRoot = "/the/root";
        indexedTestMapDifferentSourceRoots.sections.add(new Section());
        indexedTestMapDifferentSourceRoots.sections.get(1).offset = new Position(1, 0);
        indexedTestMapDifferentSourceRoots.sections.get(1).map = new SourceMap();
        indexedTestMapDifferentSourceRoots.sections.get(1).map.version = 3;
        indexedTestMapDifferentSourceRoots.sections.get(1).map.sources = Arrays.asList("two.js");
        indexedTestMapDifferentSourceRoots.sections.get(1).map.sourcesContent = Arrays
                .asList(" TWO.inc = function (n) {\n" + "   return n + 1;\n" + " };");
        indexedTestMapDifferentSourceRoots.sections.get(1).map.names = Arrays.asList("n");
        indexedTestMapDifferentSourceRoots.sections.get(1).map.mappings = "CAAC,IAAI,IAAM,SAAUA,GAClB,OAAOA";
        indexedTestMapDifferentSourceRoots.sections.get(1).map.file = "min.js";
        indexedTestMapDifferentSourceRoots.sections.get(1).map.sourceRoot = "/different/root";

        testMapWithSourcesContent = new SourceMap();
        testMapWithSourcesContent.version = 3;
        testMapWithSourcesContent.file = "min.js";
        testMapWithSourcesContent.names = Arrays.asList("bar", "baz", "n");
        testMapWithSourcesContent.sources = Arrays.asList("one.js", "two.js");
        testMapWithSourcesContent.sourcesContent = Arrays.asList(" ONE.foo = function (bar) {\n" + "   return baz(bar);\n" + " };",
                " TWO.inc = function (n) {\n" + "   return n + 1;\n" + " };");
        testMapWithSourcesContent.sourceRoot = "/the/root";
        testMapWithSourcesContent.mappings = "CAAC,IAAI,IAAM,SAAUA,GAClB,OAAOC,IAAID;CCDb,IAAI,IAAM,SAAUE,GAClB,OAAOA";

        testMapRelativeSources = new SourceMap();
        testMapRelativeSources.version = 3;
        testMapRelativeSources.file = "min.js";
        testMapRelativeSources.names = Arrays.asList("bar", "baz", "n");
        testMapRelativeSources.sources = Arrays.asList("./one.js", "./two.js");
        testMapRelativeSources.sourcesContent = Arrays.asList(" ONE.foo = function (bar) {\n" + "   return baz(bar);\n" + " };",
                " TWO.inc = function (n) {\n" + "   return n + 1;\n" + " };");
        testMapRelativeSources.sourceRoot = "/the/root";
        testMapRelativeSources.mappings = "CAAC,IAAI,IAAM,SAAUA,GAClB,OAAOC,IAAID;CCDb,IAAI,IAAM,SAAUE,GAClB,OAAOA";

        emptyMap = new SourceMap();
        emptyMap.version = 3;
        emptyMap.file = "min.js";
        emptyMap.names = Collections.emptyList();
        emptyMap.sources = Collections.emptyList();
        emptyMap.mappings = "";

    }

    static void assertMapping(Integer generatedLine, Integer generatedColumn, String originalSource, Integer originalLine, Integer originalColumn,
            String name, BinarySearch.Bias bias, SourceMapConsumer map) {
        assertMapping(generatedLine, generatedColumn, originalSource, originalLine, originalColumn, name, bias, map, null, null);
    }

    static void assertMapping(Integer generatedLine, Integer generatedColumn, String originalSource, Integer originalLine, Integer originalColumn,
            String name, BinarySearch.Bias bias, SourceMapConsumer map, boolean dontTestGenerated) {
        assertMapping(generatedLine, generatedColumn, originalSource, originalLine, originalColumn, name, bias, map, dontTestGenerated, null);
    }

    static void assertMapping(Integer generatedLine, Integer generatedColumn, String originalSource, Integer originalLine, Integer originalColumn,
            String name, BinarySearch.Bias bias, SourceMapConsumer map, Boolean dontTestGenerated, Boolean dontTestOriginal) {
        if (dontTestOriginal == null || !dontTestOriginal) {
            OriginalPosition origMapping = map.originalPositionFor(generatedLine, generatedColumn, bias);
            assertEquals(origMapping.name, name, "Incorrect name, expected " + name + ", got " + origMapping.name);
            assertEquals(origMapping.line, originalLine, "Incorrect line, expected " + originalLine + ", got " + origMapping.line);
            assertEquals(origMapping.column, originalColumn, "Incorrect column, expected " + originalColumn + ", got " + origMapping.column);

            String expectedSource;

            if (originalSource != null && map.sourceRoot != null && originalSource.indexOf(map.sourceRoot) == 0) {
                expectedSource = originalSource;
            } else if (originalSource != null) {
                expectedSource = map.sourceRoot != null ? Util.join(map.sourceRoot, originalSource) : originalSource;
            } else {
                expectedSource = null;
            }

            assertEquals(origMapping.source, expectedSource, "Incorrect source, expected " + expectedSource + ", got " + origMapping.source);
        }

        if (dontTestGenerated == null || !dontTestGenerated) {
            GeneratedPosition genMapping = map.generatedPositionFor(originalSource, originalLine, originalColumn, bias);
            assertEquals(genMapping.line, generatedLine, "Incorrect line, expected " + generatedLine + ", got " + genMapping.line);
            assertEquals(genMapping.column, generatedColumn, "Incorrect column, expected " + generatedColumn + ", got " + genMapping.column);
        }
    }

    static void assertEqualMaps(SourceMap actualMap, SourceMap expectedMap) {
        assertEquals(actualMap.version, expectedMap.version, "version mismatch");
        assertEquals(actualMap.file, expectedMap.file, "file mismatch");
        assertEquals(actualMap.names.size(), expectedMap.names.size(),
                "names length mismatch: " + Util.join(actualMap.names, ", ") + " != " + Util.join(expectedMap.names, ", "));
        for (int i = 0; i < actualMap.names.size(); i++) {
            assertEquals(actualMap.names.get(i), expectedMap.names.get(i),
                    "names[" + i + "] mismatch: " + Util.join(actualMap.names, ", ") + " != " + Util.join(expectedMap.names, ", "));
        }
        assertEquals(actualMap.sources.size(), expectedMap.sources.size(),
                "sources length mismatch: " + Util.join(actualMap.sources, ", ") + " != " + Util.join(expectedMap.sources, ", "));
        for (int i = 0; i < actualMap.sources.size(); i++) {
            assertEquals(actualMap.sources.get(i), expectedMap.sources.get(i),
                    "sources[" + i + "] length mismatch: " + Util.join(actualMap.sources, ", ") + " != " + Util.join(expectedMap.sources, ", "));
        }
        assertEquals(actualMap.sourceRoot, expectedMap.sourceRoot, "sourceRoot mismatch: " + actualMap.sourceRoot + " != " + expectedMap.sourceRoot);
        assertEquals(actualMap.mappings, expectedMap.mappings,
                "mappings mismatch:\nActual:   " + actualMap.mappings + "\nExpected: " + expectedMap.mappings);
        if (actualMap.sourcesContent != null) {
            assertEquals(actualMap.sourcesContent.size(), expectedMap.sourcesContent.size(), "sourcesContent length mismatch");
            for (int i = 0; i < actualMap.sourcesContent.size(); i++) {
                assertEquals(actualMap.sourcesContent.get(i), expectedMap.sourcesContent.get(i), "sourcesContent[" + i + "] mismatch");
            }
        }
    }

    static void assertEquals(String actual, String expected, String message) {
        Assert.assertEquals(message, expected, actual);
    }

    static void assertEquals(String actual, String expected) {
        Assert.assertEquals(expected, actual);
    }

    static void assertEquals(int actual, int expected) {
        Assert.assertEquals(expected, actual);
    }

    static void assertEquals(int actual, int expected, String message) {
        Assert.assertEquals(expected, actual);
    }

    static void assertEquals(Integer actual, Integer expected) {
        Assert.assertEquals(expected, actual);
    }

    static void assertEquals(Integer actual, Integer expected, String message) {
        Assert.assertEquals(expected, actual);
    }

    static void assertEquals(long actual, long expected) {
        Assert.assertEquals(expected, actual);
    }

    static void assertEquals(long actual, long expected, String message) {
        Assert.assertEquals(expected, actual);
    }
}
