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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Test;

public class SourceNodeTest {

    @Test
    public void testAdd() throws Exception {
        SourceNode node = new SourceNode();

        // Adding a string works.
        node.add("function noop() {}");

        // Adding another source node works.
        node.add(new SourceNode());

        // Adding an array works.
        node.add("function foo() {", new SourceNode(null, null, null, "return 10;"), "}");
    }

    @Test
    public void testPrepend() throws Exception {
        SourceNode node = new SourceNode();

        // Prepending a string works.
        node.prepend("function noop() {}");
        assertEquals(node.children.get(0).toString(), "function noop() {}");
        assertEquals(node.children.size(), 1);

        // Prepending another source node works.
        node.prepend(new SourceNode());
        assertEquals(node.children.get(0).toString(), "");
        assertEquals(node.children.get(1).toString(), "function noop() {}");
        assertEquals(node.children.size(), 2);

        // Prepending an array works.
        node.prepend("function foo() {", new SourceNode(null, null, null, "return 10;"), "}");
        assertEquals(node.children.get(0).toString(), "function foo() {");
        assertEquals(node.children.get(1).toString(), "return 10;");
        assertEquals(node.children.get(2).toString(), "}");
        assertEquals(node.children.get(3).toString(), "");
        assertEquals(node.children.get(4).toString(), "function noop() {}");
        assertEquals(node.children.size(), 5);

    }

    @Test
    public void testToString() throws Exception {
        assertEquals(
                new SourceNode(null, null, null, Arrays.asList("function foo() {", new SourceNode(null, null, null, "return 10;"), "}")).toString(),
                "function foo() {return 10;}");
    }

    @Test
    public void testJoin() throws Exception {
        assertEquals(new SourceNode(null, null, null, Util.join(Arrays.asList("a", "b", "c", "d"), ", ")).toString(), "a, b, c, d");
    }

    private static final class Expected {
        String str;
        String source;
        Integer line;
        Integer column;

        public Expected(String str, String source, Integer line, Integer column) {
            this.str = str;
            this.source = source;
            this.line = line;
            this.column = column;
        }

    }

    @Test
    public void testWalk() throws Exception {
        SourceNode node = new SourceNode(null, null, null,
                Arrays.asList("(function () {\n", "  ", new SourceNode(1, 0, "a.js", Arrays.asList("someCall()")), ";\n", "  ",
                        new SourceNode(2, 0, "b.js", Arrays.asList("if (foo) bar()")), ";\n", "}());"));
        List<Expected> expected = Arrays.asList(new Expected("(function () {\n", null, null, null), new Expected("  ", null, null, null),
                new Expected("someCall()", "a.js", 1, 0), new Expected(";\n", null, null, null), new Expected("  ", null, null, null),
                new Expected("if (foo) bar()", "b.js", 2, 0), new Expected(";\n", null, null, null), new Expected("}());", null, null, null));
        int[] i = { 0 };
        node.walk((chunk, loc) -> {
            assertEquals(expected.get(i[0]).str, chunk);
            assertEquals(expected.get(i[0]).source, loc.source);
            assertEquals(expected.get(i[0]).line, loc.line);
            assertEquals(expected.get(i[0]).column, loc.column);
            i[0]++;
        });
    }

    @Test
    public void testReplaceRight() throws Exception {
        // Not nested
        SourceNode node = new SourceNode(null, null, null, "hello world");
        node.replaceRight(Pattern.compile("world"), "universe");
        assertEquals(node.toString(), "hello universe");

        // Nested
        node = new SourceNode(null, null, null,
                Arrays.asList(new SourceNode(null, null, null, "hey sexy mama, "), new SourceNode(null, null, null, "want to kill all humans?")));
        node.replaceRight(Pattern.compile("kill all humans"), "watch Futurama");
        assertEquals(node.toString(), "hey sexy mama, want to watch Futurama?");
    }

    @Test
    public void testToStringWithSourceMap() throws Exception {
        testToStringWithSourceMap("\n");
        testToStringWithSourceMap("\r\n");
    }

    private void testToStringWithSourceMap(String nl) throws Exception {
        SourceNode node = new SourceNode(null, null, null,
                Arrays.asList("(function () {" + nl, "  ", new SourceNode(1, 0, "a.js", "someCall", "originalCall"),
                        new SourceNode(1, 8, "a.js", "()"), ";" + nl, "  ", new SourceNode(2, 0, "b.js", Arrays.asList("if (foo) bar()")), ";" + nl,
                        "}());"));
        Code result = node.toStringWithSourceMap("foo.js", null);

        assertEquals(result.getCode(), Util.join(Arrays.asList("(function () {", "  someCall();", "  if (foo) bar();", "}());"), nl));

        SourceMapGenerator map = result.getMap();
        SourceMapGenerator mapWithoutOptions = node.toStringWithSourceMap(null, null).getMap();

        assertTrue(mapWithoutOptions._file == null);
        mapWithoutOptions._file = "foo.js";
        TestUtil.assertEqualMaps(map.toJSON(), mapWithoutOptions.toJSON());

        SourceMapConsumer map2 = SourceMapConsumer.create(map.toJSON());

        OriginalPosition actual;

        actual = map2.originalPositionFor(1, 4, null);
        assertEquals(actual.source, null);
        assertEquals(actual.line, null);
        assertEquals(actual.column, null);

        actual = map2.originalPositionFor(2, 2, null);
        assertEquals(actual.source, "a.js");
        assertEquals(actual.line.intValue(), 1);
        assertEquals(actual.column.intValue(), 0);
        assertEquals(actual.name, "originalCall");

        actual = map2.originalPositionFor(3, 2, null);
        assertEquals(actual.source, "b.js");
        assertEquals(actual.line.intValue(), 2);
        assertEquals(actual.column.intValue(), 0);

        actual = map2.originalPositionFor(3, 16, null);
        assertEquals(actual.source, null);
        assertEquals(actual.line, null);
        assertEquals(actual.column, null);

        actual = map2.originalPositionFor(4, 2, null);
        assertEquals(actual.source, null);
        assertEquals(actual.line, null);
        assertEquals(actual.column, null);
    }

    @Test
    public void testFromStringWithSourceMap() throws Exception {
        testFromStringWithSourceMap("\n");
        testFromStringWithSourceMap("\r\n");
    }

    public void testFromStringWithSourceMap(String nl) throws Exception {
        String testCode = TestUtil.testGeneratedCode.replaceAll("\n", nl);
        SourceNode node = SourceNode.fromStringWithSourceMap(testCode, SourceMapConsumer.create(TestUtil.testMap), null);

        Code result = node.toStringWithSourceMap("min.js", null);
        SourceMapGenerator map = result.getMap();
        String code = result.getCode();

        assertEquals(code, testCode);
        SourceMap map2 = map.toJSON();
        assertEquals(map2.version, TestUtil.testMap.version);
        assertEquals(map2.file, TestUtil.testMap.file);
        assertEquals(map2.mappings, TestUtil.testMap.mappings);
    }

    @Test
    public void testFromStringWithSourceMapEmptyMap() throws Exception {
        testFromStringWithSourceMapEmptyMap("\n");
        testFromStringWithSourceMapEmptyMap("\r\n");
    }

    private void testFromStringWithSourceMapEmptyMap(String nl) throws Exception {
        SourceNode node = SourceNode.fromStringWithSourceMap(TestUtil.testGeneratedCode.replaceAll("\n", nl),
                SourceMapConsumer.create(TestUtil.emptyMap), null);
        Code result = node.toStringWithSourceMap("min.js", null);
        SourceMapGenerator map = result.getMap();
        String code = result.getCode();

        assertEquals(code, TestUtil.testGeneratedCode.replaceAll("\n", nl));
        SourceMap map2 = map.toJSON();
        assertEquals(map2.version, TestUtil.emptyMap.version);
        assertEquals(map2.file, TestUtil.emptyMap.file);
        assertEquals(map2.mappings.length(), TestUtil.emptyMap.mappings.length());
        assertEquals(map2.mappings, TestUtil.emptyMap.mappings);
    }

    @Test
    public void testFromStringWithSourceMapComplexVersion() throws Exception {
        testFromStringWithSourceMapComplexVersion("\n");
        testFromStringWithSourceMapComplexVersion("\r\n");
    }

    public void testFromStringWithSourceMapComplexVersion(String nl) throws Exception {
        SourceNode input = new SourceNode(null, null, null,
                Arrays.asList(
                        // @formatter:off
                        "(function() {" + nl, "  var Test = {};" + nl,
                        "  ",
                        new SourceNode(1, 0, "a.js", "Test.A = { value: 1234 };" + nl),
                        "  ",
                        new SourceNode(2, 0, "a.js", "Test.A.x = \"xyz\";"),
                        nl,
                        "}());" + nl,
                        "/* Generated Source */"
                        // @formatter:on
        ));
        Code input2 = input.toStringWithSourceMap("foo.js", null);

        SourceNode node = SourceNode.fromStringWithSourceMap(input2.getCode(), SourceMapConsumer.create(input2.getMap().toJSON()), null);

        Code result = node.toStringWithSourceMap("foo.js", null);
        SourceMapGenerator map = result.getMap();
        String code = result.getCode();

        assertEquals(code, input2.getCode());
        SourceMap map2 = map.toJSON();
        SourceMap inputMap = input2.getMap().toJSON();
        TestUtil.assertEqualMaps(map2, inputMap);
    }

    private void test(Code coffeeBundle2, SourceNode foo, String relativePath, List<String> expectedSources) {
        SourceNode app = new SourceNode();
        app.add(SourceNode.fromStringWithSourceMap(coffeeBundle2.getCode(), SourceMapConsumer.create(coffeeBundle2.getMap().toJSON()), relativePath));
        app.add(foo);
        int[] i = { 0 };
        app.walk((chunk, loc) -> {
            assertEquals(loc.source, expectedSources.get(i[0]));
            i[0]++;
        });
        app.walkSourceContents((sourceFile, sourceContent) -> {
            assertEquals(sourceFile, expectedSources.get(0));
            assertEquals(sourceContent, "foo coffee");
        });
    }

    @Test
    public void testFromStringWithSourceMapThirdArgument() throws Exception {
        // Assume the following directory structure:
        //
        // http://foo.org/
        // bar.coffee
        // app/
        // coffee/
        // foo.coffee
        // coffeeBundle.js # Made from {foo,bar,baz}.coffee
        // maps/
        // coffeeBundle.js.map
        // js/
        // foo.js
        // public/
        // app.js # Made from {foo,coffeeBundle}.js
        // app.js.map
        //
        // http://www.example.com/
        // baz.coffee

        SourceNode coffeeBundle = new SourceNode(1, 0, "foo.coffee", "foo(coffee);\n");
        coffeeBundle.setSourceContent("foo.coffee", "foo coffee");
        coffeeBundle.add(new SourceNode(2, 0, "/bar.coffee", "bar(coffee);\n"));
        coffeeBundle.add(new SourceNode(3, 0, "http://www.example.com/baz.coffee", "baz(coffee);"));
        Code coffeeBundle2 = coffeeBundle.toStringWithSourceMap("foo.js", "..");

        SourceNode foo = new SourceNode(1, 0, "foo.js", "foo(js);");

        test(coffeeBundle2, foo, "../coffee/maps",
                Arrays.asList("../coffee/foo.coffee", "/bar.coffee", "http://www.example.com/baz.coffee", "foo.js"));

        // If the third parameter is omitted or set to the current working
        // directory we get incorrect source paths:

        test(coffeeBundle2, foo, null, Arrays.asList("../foo.coffee", "/bar.coffee", "http://www.example.com/baz.coffee", "foo.js"));

        test(coffeeBundle2, foo, "", Arrays.asList("../foo.coffee", "/bar.coffee", "http://www.example.com/baz.coffee", "foo.js"));

        test(coffeeBundle2, foo, ".", Arrays.asList("../foo.coffee", "/bar.coffee", "http://www.example.com/baz.coffee", "foo.js"));

        test(coffeeBundle2, foo, "./", Arrays.asList("../foo.coffee", "/bar.coffee", "http://www.example.com/baz.coffee", "foo.js"));
    }

    @Test
    public void testToStringWithSourceMapMergingDuplicateMappings() throws Exception {
        testToStringWithSourceMapMergingDuplicateMappings("\n");
        testToStringWithSourceMapMergingDuplicateMappings("\r\n");
    }

    private void testToStringWithSourceMapMergingDuplicateMappings(String nl) throws Exception {
        SourceNode input = new SourceNode(null, null, null,
                Arrays.asList(
                // @formatter:off
                        new SourceNode(1, 0, "a.js", "(function"),
                        new SourceNode(1, 0, "a.js", "() {" + nl),
                        "  ",
                        new SourceNode(1, 0, "a.js", "var Test = "),
                        new SourceNode(1, 0, "b.js", "{};" + nl),
                        new SourceNode(2, 0, "b.js", "Test"),
                        new SourceNode(2, 0, "b.js", ".A", "A"),
                        new SourceNode(2, 20, "b.js", " = { value: ", "A"),
                        "1234",
                        new SourceNode(2, 40, "b.js", " };" + nl, "A"),
                        "}());" + nl,
                        "/* Generated Source */"
                 // @formatter:on
        ));
        Code input2 = input.toStringWithSourceMap("foo.js", null);

        assertEquals(input2.getCode(),
                Util.join(Arrays.asList("(function() {", "  var Test = {};", "Test.A = { value: 1234 };", "}());", "/* Generated Source */"), nl));

        SourceMapGenerator correctMap = new SourceMapGenerator("foo.js", null);
        correctMap.addMapping(new Mapping(new Position(1, 0), new Position(1, 0), "a.js"));
        // Here is no need for a empty mapping,
        // because mappings ends at eol
        correctMap.addMapping(new Mapping(new Position(2, 2), new Position(1, 0), "a.js"));
        correctMap.addMapping(new Mapping(new Position(2, 13), new Position(1, 0), "b.js"));
        correctMap.addMapping(new Mapping(new Position(3, 0), new Position(2, 0), "b.js"));
        correctMap.addMapping(new Mapping(new Position(3, 4), new Position(2, 0), "b.js", "A"));
        correctMap.addMapping(new Mapping(new Position(3, 6), new Position(2, 20), "b.js", "A"));
        // This empty mapping is required,
        // because there is a hole in the middle of the line
        correctMap.addMapping(new Mapping(new Position(3, 18)));
        correctMap.addMapping(new Mapping(new Position(3, 22), new Position(2, 40), "b.js", "A"));
        // Here is no need for a empty mapping,
        // because mappings ends at eol

        SourceMap inputMap = input2.getMap().toJSON();
        SourceMap correctMap2 = correctMap.toJSON();
        TestUtil.assertEqualMaps(inputMap, correctMap2);
    }

    @Test
    public void testToStringWithSourceMapMultiLineSourceNodes() throws Exception {
        testToStringWithSourceMapMultiLineSourceNodes("\n");
        testToStringWithSourceMapMultiLineSourceNodes("\r\n");
    }

    private void testToStringWithSourceMapMultiLineSourceNodes(String nl) throws Exception {
        SourceNode input = new SourceNode(null, null, null,
                Arrays.asList(
                        // @formatter:off
                        new SourceNode(1, 0, "a.js", "(function() {" + nl + "var nextLine = 1;" + nl + "anotherLine();" + nl),
                        new SourceNode(2, 2, "b.js", "Test.call(this, 123);" + nl),
                        new SourceNode(2, 2, "b.js", "this[\"stuff\"] = \"v\";" + nl),
                        new SourceNode(2, 2, "b.js", "anotherLine();" + nl),
                        "/*" + nl + "Generated" + nl + "Source" + nl + "*/" + nl,
                        new SourceNode(3, 4, "c.js", "anotherLine();" + nl),
                        "/*" + nl + "Generated" + nl + "Source" + nl + "*/"
                        // @formatter:on
        ));
        Code input2 = input.toStringWithSourceMap("foo.js", null);

        assertEquals(input2.getCode(),
                Util.join(Arrays.asList("(function() {", "var nextLine = 1;", "anotherLine();", "Test.call(this, 123);", "this[\"stuff\"] = \"v\";",
                        "anotherLine();", "/*", "Generated", "Source", "*/", "anotherLine();", "/*", "Generated", "Source", "*/"), nl));

        SourceMapGenerator correctMap = new SourceMapGenerator("foo.js", null);
        correctMap.addMapping(new Mapping(new Position(1, 0), new Position(1, 0), "a.js"));
        correctMap.addMapping(new Mapping(new Position(2, 0), new Position(1, 0), "a.js"));
        correctMap.addMapping(new Mapping(new Position(3, 0), new Position(1, 0), "a.js"));
        correctMap.addMapping(new Mapping(new Position(4, 0), new Position(2, 2), "b.js"));
        correctMap.addMapping(new Mapping(new Position(5, 0), new Position(2, 2), "b.js"));
        correctMap.addMapping(new Mapping(new Position(6, 0), new Position(2, 2), "b.js"));
        correctMap.addMapping(new Mapping(new Position(11, 0), new Position(3, 4), "c.js"));
        SourceMap inputMap = input2.getMap().toJSON();
        SourceMap correctMap2 = correctMap.toJSON();
        TestUtil.assertEqualMaps(inputMap, correctMap2);
    }

    @Test
    public void testToStringWithSourceMapWithEmptyString() throws Exception {
        SourceNode node = new SourceNode(1, 0, "empty.js", "", null);
        Code result = node.toStringWithSourceMap(null, null);
        assertEquals(result.getCode(), "");
    }

    @Test
    public void testToStringWithSourceMapWithConsecutiveNewlines() throws Exception {
        testToStringWithSourceMapWithConsecutiveNewlines("\n");
        testToStringWithSourceMapWithConsecutiveNewlines("\r\n");
    }

    private void testToStringWithSourceMapWithConsecutiveNewlines(String nl) throws Exception {
        SourceNode input = new SourceNode(null, null, null,
                Arrays.asList("/***/" + nl + nl, new SourceNode(1, 0, "a.js", "\"use strict\";" + nl), new SourceNode(2, 0, "a.js", "a();")));
        Code input2 = input.toStringWithSourceMap("foo.js", null);

        assertEquals(input2.getCode(), Util.join(Arrays.asList("/***/", "", "\"use strict\";", "a();"), nl));

        SourceMapGenerator correctMap = new SourceMapGenerator("foo.js", null);
        correctMap.addMapping(new Mapping(new Position(3, 0), new Position(1, 0), "a.js"));
        correctMap.addMapping(new Mapping(new Position(4, 0), new Position(2, 0), "a.js"));
        SourceMap inputMap = input2.getMap().toJSON();
        SourceMap correctMap2 = correctMap.toJSON();
        TestUtil.assertEqualMaps(inputMap, correctMap2);
    };

    @Test
    public void testSetSourceContentWithToStringWithSourceMap() throws Exception {
        SourceNode aNode = new SourceNode(1, 1, "a.js", "a");
        aNode.setSourceContent("a.js", "someContent");
        SourceNode node = new SourceNode(null, null, null,
                Arrays.asList(
                        // @formatter:off
                        "(function () {\n", "  ",
                        aNode,
                        "  ",
                        new SourceNode(1, 1, "b.js", "b"),
                        "}());"
                        // @formatter:on
        ));
        node.setSourceContent("b.js", "otherContent");
        SourceMapGenerator map = node.toStringWithSourceMap("foo.js", null).getMap();

        SourceMapConsumer map2 = SourceMapConsumer.create(map.toJSON());

        assertEquals(map2.sources().size(), 2);
        assertEquals(map2.sources().get(0), "a.js");
        assertEquals(map2.sources().get(1), "b.js");
        assertEquals(map2.sourcesContent.size(), 2);
        assertEquals(map2.sourcesContent.get(0), "someContent");
        assertEquals(map2.sourcesContent.get(1), "otherContent");
    }

    @Test
    public void testWalkSourceContents() throws Exception {
        SourceNode aNode = new SourceNode(1, 1, "a.js", "a");
        aNode.setSourceContent("a.js", "someContent");
        SourceNode node = new SourceNode(null, null, null,
                Arrays.asList("(function () {\n", "  ", aNode, "  ", new SourceNode(1, 1, "b.js", "b"), "}());"));
        node.setSourceContent("b.js", "otherContent");
        List<List<String>> results = new ArrayList<>();
        node.walkSourceContents((sourceFile, sourceContent) -> results.add(Arrays.asList(sourceFile, sourceContent)));
        assertEquals(results.size(), 2);
        assertEquals(results.get(0).get(0), "a.js");
        assertEquals(results.get(0).get(1), "someContent");
        assertEquals(results.get(1).get(0), "b.js");
        assertEquals(results.get(1).get(1), "otherContent");
    };
}
