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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.hibnet.jsourcemap.SourceMapConsumer.Position;

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
        indexedTestMap.sections.get(0).offset = new Section.Offset(0, 0);
        indexedTestMap.sections.get(0).map = new SourceMap();
        indexedTestMap.sections.get(0).map.version = 3;
        indexedTestMap.sections.get(0).map.sources = Arrays.asList("one.js");
        indexedTestMap.sections.get(0).map.sourcesContent = Arrays.asList(" ONE.foo = function (bar) {\n" + "   return baz(bar);\n" + " };");
        indexedTestMap.sections.get(0).map.names = Arrays.asList("bar", "baz");
        indexedTestMap.sections.get(0).map.mappings = "CAAC,IAAI,IAAM,SAAUA,GAClB,OAAOC,IAAID";
        indexedTestMap.sections.get(0).map.file = "min.js";
        indexedTestMap.sections.get(0).map.sourceRoot = "/the/root";
        indexedTestMap.sections.add(new Section());
        indexedTestMap.sections.get(1).offset = new Section.Offset(1, 0);
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
        indexedTestMapDifferentSourceRoots.sections.get(0).offset = new Section.Offset(0, 0);
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
        indexedTestMapDifferentSourceRoots.sections.get(1).offset = new Section.Offset(1, 0);
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
            OriginalMapping origMapping = map.originalPositionFor(generatedLine, generatedColumn, bias);
            assertEquals("Incorrect name, expected " + name + ", got " + origMapping.name, origMapping.name, name);
            assertEquals("Incorrect line, expected " + originalLine + ", got " + origMapping.line, origMapping.line, originalLine);
            assertEquals("Incorrect column, expected " + originalColumn + ", got " + origMapping.column, origMapping.column, originalColumn);

            Object expectedSource;

            if (originalSource != null && map.sourceRoot != null && originalSource.indexOf(map.sourceRoot) == 0) {
                expectedSource = originalSource;
            } else if (originalSource != null) {
                expectedSource = map.sourceRoot != null ? Util.join(map.sourceRoot, originalSource) : originalSource;
            } else {
                expectedSource = null;
            }

            assertEquals("Incorrect source, expected " + expectedSource + ", got " + origMapping.source, origMapping.source, expectedSource);
        }

        if (dontTestGenerated == null || !dontTestGenerated) {
            Position genMapping = map.generatedPositionFor(originalSource, originalLine, originalColumn, bias);
            assertEquals("Incorrect line, expected " + generatedLine + ", got " + genMapping.line, genMapping.line, generatedLine);
            assertEquals("Incorrect column, expected " + generatedColumn + ", got " + genMapping.column, genMapping.column, generatedColumn);
        }
    }

    static void assertEqualMaps(SourceMap actualMap, SourceMap expectedMap) {
        assertEquals("version mismatch", actualMap.version, expectedMap.version);
        assertEquals("file mismatch", actualMap.file, expectedMap.file);
        assertEquals("names length mismatch: " + Util.join(actualMap.names, ", ") + " != " + Util.join(expectedMap.names, ", "),
                actualMap.names.size(), expectedMap.names.size());
        for (int i = 0; i < actualMap.names.size(); i++) {
            assertEquals("names[" + i + "] mismatch: " + Util.join(actualMap.names, ", ") + " != " + Util.join(expectedMap.names, ", "),
                    actualMap.names.get(i), expectedMap.names.get(i));
        }
        assertEquals("sources length mismatch: " + Util.join(actualMap.sources, ", ") + " != " + Util.join(expectedMap.sources, ", "),
                actualMap.sources.size(), expectedMap.sources.size());
        for (int i = 0; i < actualMap.sources.size(); i++) {
            assertEquals("sources[" + i + "] length mismatch: " + Util.join(actualMap.sources, ", ") + " != " + Util.join(expectedMap.sources, ", "),
                    actualMap.sources.get(i), expectedMap.sources.get(i));
        }
        assertEquals(actualMap.sourceRoot, expectedMap.sourceRoot, "sourceRoot mismatch: " + actualMap.sourceRoot + " != " + expectedMap.sourceRoot);
        assertEquals(actualMap.mappings, expectedMap.mappings,
                "mappings mismatch:\nActual:   " + actualMap.mappings + "\nExpected: " + expectedMap.mappings);
        if (actualMap.sourcesContent != null) {
            assertEquals("sourcesContent length mismatch", actualMap.sourcesContent.size(), expectedMap.sourcesContent.size());
            for (int i = 0; i < actualMap.sourcesContent.size(); i++) {
                assertEquals(actualMap.sourcesContent.get(i), expectedMap.sourcesContent.get(i), "sourcesContent[" + i + "] mismatch");
            }
        }
    }

}
