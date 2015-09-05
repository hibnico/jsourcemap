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
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.regex.Pattern;

import org.junit.Test;

public class UtilTest {

    private void assertUrl(String url) {
        assertEquals(url, Util.urlGenerate(Util.urlParse(url)));
    }

    @Test
    public void testUrls() throws Exception {
        assertUrl("http://");
        assertUrl("http://www.example.com");
        assertUrl("http://user:pass@www.example.com");
        assertUrl("http://www.example.com:80");
        assertUrl("http://www.example.com/");
        assertUrl("http://www.example.com/foo/bar");
        assertUrl("http://www.example.com/foo/bar/");
        assertUrl("http://user:pass@www.example.com:80/foo/bar/");

        assertUrl("//");
        assertUrl("//www.example.com");
        assertUrl("file:///www.example.com");

        assertNull(Util.urlParse(""));
        assertNull(Util.urlParse("."));
        assertNull(Util.urlParse(".."));
        assertNull(Util.urlParse("a"));
        assertNull(Util.urlParse("a/b"));
        assertNull(Util.urlParse("a//b"));
        assertNull(Util.urlParse("/a"));
        assertNull(Util.urlParse("data:foo,bar"));
    }

    @Test
    public void testNormalize() throws Exception {
        assertEquals(Util.normalize("/.."), "/");
        assertEquals(Util.normalize("/../"), "/");
        assertEquals(Util.normalize("/../../../.."), "/");
        assertEquals(Util.normalize("/../../../../a/b/c"), "/a/b/c");
        assertEquals(Util.normalize("/a/b/c/../../../d/../../e"), "/e");

        assertEquals(Util.normalize(".."), "..");
        assertEquals(Util.normalize("../"), "../");
        assertEquals(Util.normalize("../../a/"), "../../a/");
        assertEquals(Util.normalize("a/.."), ".");
        assertEquals(Util.normalize("a/../../.."), "../..");

        assertEquals(Util.normalize("/."), "/");
        assertEquals(Util.normalize("/./"), "/");
        assertEquals(Util.normalize("/./././."), "/");
        assertEquals(Util.normalize("/././././a/b/c"), "/a/b/c");
        assertEquals(Util.normalize("/a/b/c/./././d/././e"), "/a/b/c/d/e");

        assertEquals(Util.normalize(""), ".");
        assertEquals(Util.normalize("."), ".");
        assertEquals(Util.normalize("./"), ".");
        assertEquals(Util.normalize("././a"), "a");
        assertEquals(Util.normalize("a/./"), "a/");
        assertEquals(Util.normalize("a/././."), "a");

        assertEquals(Util.normalize("/a/b//c////d/////"), "/a/b/c/d/");
        assertEquals(Util.normalize("///a/b//c////d/////"), "///a/b/c/d/");
        assertEquals(Util.normalize("a/b//c////d"), "a/b/c/d");

        assertEquals(Util.normalize(".///.././../a/b//./.."), "../../a");

        assertEquals(Util.normalize("http://www.example.com"), "http://www.example.com");
        assertEquals(Util.normalize("http://www.example.com/"), "http://www.example.com/");
        assertEquals(Util.normalize("http://www.example.com/./..//a/b/c/.././d//"), "http://www.example.com/a/b/d/");
    }

    @Test
    public void testJoin() throws Exception {
        assertEquals(Util.join("a", "b"), "a/b");
        assertEquals(Util.join("a/", "b"), "a/b");
        assertEquals(Util.join("a//", "b"), "a/b");
        assertEquals(Util.join("a", "b/"), "a/b/");
        assertEquals(Util.join("a", "b//"), "a/b/");
        assertEquals(Util.join("a/", "/b"), "/b");
        assertEquals(Util.join("a//", "//b"), "//b");

        assertEquals(Util.join("a", ".."), ".");
        assertEquals(Util.join("a", "../b"), "b");
        assertEquals(Util.join("a/b", "../c"), "a/c");

        assertEquals(Util.join("a", "."), "a");
        assertEquals(Util.join("a", "./b"), "a/b");
        assertEquals(Util.join("a/b", "./c"), "a/b/c");

        assertEquals(Util.join("a", "http://www.example.com"), "http://www.example.com");
        assertEquals(Util.join("a", "data:foo,bar"), "data:foo,bar");

        assertEquals(Util.join("", "b"), "b");
        assertEquals(Util.join(".", "b"), "b");
        assertEquals(Util.join("", "b/"), "b/");
        assertEquals(Util.join(".", "b/"), "b/");
        assertEquals(Util.join("", "b//"), "b/");
        assertEquals(Util.join(".", "b//"), "b/");

        assertEquals(Util.join("", ".."), "..");
        assertEquals(Util.join(".", ".."), "..");
        assertEquals(Util.join("", "../b"), "../b");
        assertEquals(Util.join(".", "../b"), "../b");

        assertEquals(Util.join("", "."), ".");
        assertEquals(Util.join(".", "."), ".");
        assertEquals(Util.join("", "./b"), "b");
        assertEquals(Util.join(".", "./b"), "b");

        assertEquals(Util.join("", "http://www.example.com"), "http://www.example.com");
        assertEquals(Util.join(".", "http://www.example.com"), "http://www.example.com");
        assertEquals(Util.join("", "data:foo,bar"), "data:foo,bar");
        assertEquals(Util.join(".", "data:foo,bar"), "data:foo,bar");

        assertEquals(Util.join("..", "b"), "../b");
        assertEquals(Util.join("..", "b/"), "../b/");
        assertEquals(Util.join("..", "b//"), "../b/");

        assertEquals(Util.join("..", ".."), "../..");
        assertEquals(Util.join("..", "../b"), "../../b");

        assertEquals(Util.join("..", "."), "..");
        assertEquals(Util.join("..", "./b"), "../b");

        assertEquals(Util.join("..", "http://www.example.com"), "http://www.example.com");
        assertEquals(Util.join("..", "data:foo,bar"), "data:foo,bar");

        assertEquals(Util.join("a", ""), "a");
        assertEquals(Util.join("a", "."), "a");
        assertEquals(Util.join("a/", ""), "a");
        assertEquals(Util.join("a/", "."), "a");
        assertEquals(Util.join("a//", ""), "a");
        assertEquals(Util.join("a//", "."), "a");
        assertEquals(Util.join("/a", ""), "/a");
        assertEquals(Util.join("/a", "."), "/a");
        assertEquals(Util.join("", ""), ".");
        assertEquals(Util.join(".", ""), ".");
        assertEquals(Util.join(".", ""), ".");
        assertEquals(Util.join(".", "."), ".");
        assertEquals(Util.join("..", ""), "..");
        assertEquals(Util.join("..", "."), "..");
        assertEquals(Util.join("http://foo.org/a", ""), "http://foo.org/a");
        assertEquals(Util.join("http://foo.org/a", "."), "http://foo.org/a");
        assertEquals(Util.join("http://foo.org/a/", ""), "http://foo.org/a");
        assertEquals(Util.join("http://foo.org/a/", "."), "http://foo.org/a");
        assertEquals(Util.join("http://foo.org/a//", ""), "http://foo.org/a");
        assertEquals(Util.join("http://foo.org/a//", "."), "http://foo.org/a");
        assertEquals(Util.join("http://foo.org", ""), "http://foo.org/");
        assertEquals(Util.join("http://foo.org", "."), "http://foo.org/");
        assertEquals(Util.join("http://foo.org/", ""), "http://foo.org/");
        assertEquals(Util.join("http://foo.org/", "."), "http://foo.org/");
        assertEquals(Util.join("http://foo.org//", ""), "http://foo.org/");
        assertEquals(Util.join("http://foo.org//", "."), "http://foo.org/");
        assertEquals(Util.join("//www.example.com", ""), "//www.example.com/");
        assertEquals(Util.join("//www.example.com", "."), "//www.example.com/");

        assertEquals(Util.join("http://foo.org/a", "b"), "http://foo.org/a/b");
        assertEquals(Util.join("http://foo.org/a/", "b"), "http://foo.org/a/b");
        assertEquals(Util.join("http://foo.org/a//", "b"), "http://foo.org/a/b");
        assertEquals(Util.join("http://foo.org/a", "b/"), "http://foo.org/a/b/");
        assertEquals(Util.join("http://foo.org/a", "b//"), "http://foo.org/a/b/");
        assertEquals(Util.join("http://foo.org/a/", "/b"), "http://foo.org/b");
        assertEquals(Util.join("http://foo.org/a//", "//b"), "http://b");

        assertEquals(Util.join("http://foo.org/a", ".."), "http://foo.org/");
        assertEquals(Util.join("http://foo.org/a", "../b"), "http://foo.org/b");
        assertEquals(Util.join("http://foo.org/a/b", "../c"), "http://foo.org/a/c");

        assertEquals(Util.join("http://foo.org/a", "."), "http://foo.org/a");
        assertEquals(Util.join("http://foo.org/a", "./b"), "http://foo.org/a/b");
        assertEquals(Util.join("http://foo.org/a/b", "./c"), "http://foo.org/a/b/c");

        assertEquals(Util.join("http://foo.org/a", "http://www.example.com"), "http://www.example.com");
        assertEquals(Util.join("http://foo.org/a", "data:foo,bar"), "data:foo,bar");

        assertEquals(Util.join("http://foo.org", "a"), "http://foo.org/a");
        assertEquals(Util.join("http://foo.org/", "a"), "http://foo.org/a");
        assertEquals(Util.join("http://foo.org//", "a"), "http://foo.org/a");
        assertEquals(Util.join("http://foo.org", "/a"), "http://foo.org/a");
        assertEquals(Util.join("http://foo.org/", "/a"), "http://foo.org/a");
        assertEquals(Util.join("http://foo.org//", "/a"), "http://foo.org/a");

        assertEquals(Util.join("http://", "www.example.com"), "http://www.example.com");
        assertEquals(Util.join("file:///", "www.example.com"), "file:///www.example.com");
        assertEquals(Util.join("http://", "ftp://example.com"), "ftp://example.com");

        assertEquals(Util.join("http://www.example.com", "//foo.org/bar"), "http://foo.org/bar");
        assertEquals(Util.join("//www.example.com", "//foo.org/bar"), "//foo.org/bar");
    }

    // TODO Issue #128: Define and test this function properly.
    @Test
    public void testRelative() throws Exception {
        assertEquals(Util.relative("/the/root", "/the/root/one.js"), "one.js");
        assertEquals(Util.relative("http://the/root", "http://the/root/one.js"), "one.js");
        assertEquals(Util.relative("/the/root", "/the/rootone.js"), "../rootone.js");
        assertEquals(Util.relative("http://the/root", "http://the/rootone.js"), "../rootone.js");
        assertEquals(Util.relative("/the/root", "/therootone.js"), "/therootone.js");
        assertEquals(Util.relative("http://the/root", "/therootone.js"), "/therootone.js");

        assertEquals(Util.relative("", "/the/root/one.js"), "/the/root/one.js");
        assertEquals(Util.relative(".", "/the/root/one.js"), "/the/root/one.js");
        assertEquals(Util.relative("", "the/root/one.js"), "the/root/one.js");
        assertEquals(Util.relative(".", "the/root/one.js"), "the/root/one.js");

        assertEquals(Util.relative("/", "/the/root/one.js"), "the/root/one.js");
        assertEquals(Util.relative("/", "the/root/one.js"), "the/root/one.js");
    }

    @Test
    public void testSubstr() throws Exception {
        assertEquals("", Util.substr("", 0, 0));
        assertEquals("", Util.substr("123456789", 3, 0));
        assertEquals("4", Util.substr("123456789", 3, 1));
        assertEquals("4567", Util.substr("123456789", 3, 4));
        assertEquals("2", Util.substr("12", 1, 4));
        assertEquals("", Util.substr("12", 1, -4));

        assertEquals("", Util.substr("", 0));
        assertEquals("123456789", Util.substr("123456789", 0));
        assertEquals("456789", Util.substr("123456789", 3));
        assertEquals("", Util.substr("123456789", 30));
        assertEquals("123456789", Util.substr("123456789", -30));
    }

    @Test
    public void testSplit() throws Exception {
        assertEquals(Arrays.asList(""), Util.split("", Pattern.compile("")));
        assertEquals(Arrays.asList("a", "cde"), Util.split("abcde", Pattern.compile("b")));
        assertEquals(Arrays.asList("a", "b", "cde"), Util.split("abcde", Pattern.compile("(b)")));
        assertEquals(Arrays.asList("", "bcde"), Util.split("abcde", Pattern.compile("a")));
        assertEquals(Arrays.asList("", "a", "bcde"), Util.split("abcde", Pattern.compile("(a)")));
        assertEquals(Arrays.asList("ab", "\n", "cde"), Util.split("ab\ncde", Pattern.compile("(\r?\n)")));
        assertEquals(Arrays.asList("ab", "\n", "cde", "\r\n", ""), Util.split("ab\ncde\r\n", Pattern.compile("(\r?\n)")));
    }
}
