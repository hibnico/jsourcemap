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

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

    private static final Pattern urlRegexp = Pattern.compile("^(?:([\\w+\\-.]+):)?//(?:(\\w+:\\w+)@)?([\\w.]*)(?::(\\d+))?(\\S*)$");

    private static final Pattern dataUrlRegexp = Pattern.compile("^data:.+\\,.+$");

    static final class ParsedUrl {
        String scheme;
        String auth;
        String host;
        String port;
        String path;
    }

    static final ParsedUrl urlParse(String aUrl) {
        Matcher match = urlRegexp.matcher(aUrl);
        if (!match.matches()) {
            return null;
        }
        ParsedUrl parsed = new ParsedUrl();
        parsed.scheme = match.group(1);
        parsed.auth = match.group(2);
        parsed.host = match.group(3);
        parsed.port = match.group(4);
        parsed.path = match.group(5);
        if (parsed.path.length() == 0) {
            parsed.path = null;
        }
        return parsed;
    }

    static final String urlGenerate(ParsedUrl aParsedUrl) {
        StringBuilder url = new StringBuilder();
        if (aParsedUrl.scheme != null) {
            url.append(aParsedUrl.scheme);
            url.append(':');
        }
        url.append("//");
        if (aParsedUrl.auth != null) {
            url.append(aParsedUrl.auth);
            url.append('@');
        }
        if (aParsedUrl.host != null) {
            url.append(aParsedUrl.host);
        }
        if (aParsedUrl.port != null) {
            url.append(":");
            url.append(aParsedUrl.port);
        }
        if (aParsedUrl.path != null) {
            url.append(aParsedUrl.path);
        }
        return url.toString();
    }

    static final String normalize(String aPath) {
        String path = aPath;
        ParsedUrl url = urlParse(aPath);
        if (url != null) {
            if (url.path == null) {
                return aPath;
            }
            path = url.path;
        }
        boolean isAbsolute = isAbsolute(path);

        String[] parts = path.split("/+");

        // because String.split in JS is returning an empty string and not the Java one
        if (path.endsWith("/")) {
            String[] p = new String[parts.length + 1];
            System.arraycopy(parts, 0, p, 0, parts.length);
            p[parts.length] = "";
            parts = p;
        }

        int up = 0;
        for (int i = parts.length - 1; i >= 0; i--) {
            String part = parts[i];
            if (part.equals(".")) {
                parts = splice(parts, i, 1);
            } else if (part.equals("..")) {
                up++;
            } else if (up > 0) {
                if (part.length() == 0) {
                    // The first part is blank if the path is absolute. Trying to go
                    // above the root is a no-op. Therefore we can remove all '..' parts
                    // directly after the root.
                    parts = splice(parts, i + 1, up);
                    up = 0;
                } else {
                    parts = splice(parts, i, 2);
                    up--;
                }
            }
        }
        path = join(Arrays.asList(parts), "/");

        if (path.length() == 0) {
            path = isAbsolute ? "/" : ".";
        }

        if (url != null) {
            url.path = path;
            return urlGenerate(url);
        }
        return path;
    }

    static final String join(String aRoot, String aPath) {
        if (aRoot.length() == 0) {
            aRoot = ".";
        }
        if (aPath.length() == 0) {
            aPath = ".";
        }
        ParsedUrl aPathUrl = urlParse(aPath);
        ParsedUrl aRootUrl = urlParse(aRoot);
        if (aRootUrl != null) {
            aRoot = (aRootUrl.path == null || aRootUrl.path.isEmpty()) ? "/" : aRootUrl.path;
        }

        // `join(foo, '//www.example.org')`
        if (aPathUrl != null && aPathUrl.scheme == null) {
            if (aRootUrl != null) {
                aPathUrl.scheme = aRootUrl.scheme;
            }
            return urlGenerate(aPathUrl);
        }

        if (aPathUrl != null || dataUrlRegexp.matcher(aPath).matches()) {
            return aPath;
        }

        // `join('http://', 'www.example.com')`
        if (aRootUrl != null && (aRootUrl.host == null || aRootUrl.host.isEmpty()) && (aRootUrl.path == null || aRootUrl.path.isEmpty())) {
            aRootUrl.host = aPath;
            return urlGenerate(aRootUrl);
        }

        String joined = aPath.charAt(0) == '/' ? aPath : normalize(aRoot.replaceAll("/+$", "") + '/' + aPath);

        if (aRootUrl != null) {
            aRootUrl.path = joined;
            return urlGenerate(aRootUrl);
        }
        return joined;
    }

    static final boolean isAbsolute(String aPath) {
        return aPath.length() > 0 && aPath.charAt(0) == '/' || urlRegexp.matcher(aPath).matches();
    }

    static final String relative(String aRoot, String aPath) {
        if (aRoot.length() == 0) {
            aRoot = ".";
        }

        aRoot = aRoot.replaceAll("/$", "");

        // It is possible for the path to be above the root. In this case, simply
        // checking whether the root is a prefix of the path won't work. Instead, we
        // need to remove components from the root one by one, until either we find
        // a prefix that fits, or we run out of components to remove.
        int level = 0;
        while (aPath.indexOf(aRoot + '/') != 0) {
            int index = aRoot.lastIndexOf("/");
            if (index < 0) {
                return aPath;
            }

            // If the only part of the root that is left is the scheme (i.e. http://,
            // file:///, etc.), one or more slashes (/), or simply nothing at all, we
            // have exhausted all components, so the path is not relative to the root.
            aRoot = aRoot.substring(0, index);
            if (aRoot.matches("^([^/]+:/)?/*$")) {
                return aPath;
            }

            ++level;
        }

        // Make sure we add a "../" for each component we removed from the root.
        return join(level + 1, "../") + aPath.substring(aRoot.length() + 1);
    }

    static String toSetString(String aStr) {
        return '$' + aStr;
    }

    static String fromSetString(String aStr) {
        return aStr.substring(1);
    }

    // mimic the behaviour of i1 - i2 in JS
    private static final int intcmp(Integer i1, Integer i2) {
        if (i1 == null && i2 == null) {
            return 0;
        }
        if (i1 == null) {
            return -i2;
        }
        if (i2 == null) {
            return i1;
        }
        return i1 - i2;
    }

    static int compareByOriginalPositions(ConsumerMapping mappingA, ConsumerMapping mappingB) {
        return compareByOriginalPositions(mappingA, mappingB, null);
    }

    static int compareByOriginalPositions(ConsumerMapping mappingA, ConsumerMapping mappingB, Boolean onlyCompareOriginal) {
        int cmp = intcmp(mappingA.source, mappingB.source);
        if (cmp != 0) {
            return cmp;
        }

        cmp = intcmp(mappingA.originalLine, mappingB.originalLine);
        if (cmp != 0) {
            return cmp;
        }

        cmp = intcmp(mappingA.originalColumn, mappingB.originalColumn);
        if (cmp != 0 || (onlyCompareOriginal != null && onlyCompareOriginal)) {
            return cmp;
        }

        cmp = intcmp(mappingA.generatedColumn, mappingB.generatedColumn);
        if (cmp != 0) {
            return cmp;
        }

        cmp = intcmp(mappingA.generatedLine, mappingB.generatedLine);
        if (cmp != 0) {
            return cmp;
        }

        return intcmp(mappingA.name, mappingB.name);
    }

    static int compareByGeneratedPositionsDeflated(ConsumerMapping mappingA, ConsumerMapping mappingB) {
        return compareByGeneratedPositionsDeflated(mappingA, mappingB, null);
    }

    static int compareByGeneratedPositionsDeflated(ConsumerMapping mappingA, ConsumerMapping mappingB, Boolean onlyCompareGenerated) {
        int cmp = intcmp(mappingA.generatedLine, mappingB.generatedLine);
        if (cmp != 0) {
            return cmp;
        }

        cmp = intcmp(mappingA.generatedColumn, mappingB.generatedColumn);
        if (cmp != 0 || (onlyCompareGenerated != null && onlyCompareGenerated)) {
            return cmp;
        }

        cmp = intcmp(mappingA.source, mappingB.source);
        if (cmp != 0) {
            return cmp;
        }

        cmp = intcmp(mappingA.originalLine, mappingB.originalLine);
        if (cmp != 0) {
            return cmp;
        }

        cmp = intcmp(mappingA.originalColumn, mappingB.originalColumn);
        if (cmp != 0) {
            return cmp;
        }

        return intcmp(mappingA.name, mappingB.name);
    }

    private static final int strcmp(String aStr1, String aStr2) {
        if (aStr1 == aStr2 || (aStr1 != null && aStr1.equals(aStr2))) {
            return 0;
        }
        if (aStr1 != null && aStr2 != null && aStr1.compareTo(aStr2) > 0) {
            return 1;
        }
        return -1;
    }

    static final int compareByGeneratedPositionsInflated(Mapping mappingA, Mapping mappingB) {
        int cmp = intcmp(mappingA.generatedLine, mappingB.generatedLine);
        if (cmp != 0) {
            return cmp;
        }

        cmp = intcmp(mappingA.generatedColumn, mappingB.generatedColumn);
        if (cmp != 0) {
            return cmp;
        }

        cmp = strcmp(mappingA.source, mappingB.source);
        if (cmp != 0) {
            return cmp;
        }

        cmp = intcmp(mappingA.originalLine, mappingB.originalLine);
        if (cmp != 0) {
            return cmp;
        }

        cmp = intcmp(mappingA.originalColumn, mappingB.originalColumn);
        if (cmp != 0) {
            return cmp;
        }

        return strcmp(mappingA.name, mappingB.name);
    }

    static final String join(Collection<String> list, String join) {
        StringBuilder buffer = new StringBuilder();
        boolean first = true;
        for (String item : list) {
            if (!first) {
                buffer.append(join);
            }
            first = false;
            buffer.append(item);
        }
        return buffer.toString();
    }

    private static final String join(int n, String join) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 1; i < n; i++) {
            buffer.append(join);
        }
        return buffer.toString();
    }

    private static final String[] splice(String[] array, int start, int nb) {
        String[] newArray = new String[array.length - nb];
        System.arraycopy(array, 0, newArray, 0, start);
        System.arraycopy(array, start + nb, newArray, start, array.length - (start + nb));
        return newArray;
    }
}
