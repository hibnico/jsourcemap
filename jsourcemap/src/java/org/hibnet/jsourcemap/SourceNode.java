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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class SourceNode {

    // Matches a Windows-style `\r\n` newline or a `\n` newline used by all other
    // operating systems these days (capturing the result).
    private static final Pattern REGEX_NEWLINE = Pattern.compile("(\r?\n)");

    // Newline character code for charCodeAt() comparisons
    private static final int NEWLINE_CODE = 10;

    private List<Object> children = new ArrayList<>();

    private Map<String, String> sourceContents = new HashMap<>();

    private Integer line;

    private Integer column;

    private String source;

    private String name;

    /**
     * SourceNodes provide a way to abstract over interpolating/concatenating snippets of generated JavaScript source code while maintaining the line
     * and column information associated with the original source code.
     *
     * @param aLine
     *            The original line number.
     * @param aColumn
     *            The original column number.
     * @param aSource
     *            The original source's filename.
     * @param aChunks
     *            Optional. An array of strings which are snippets of generated JS, or other SourceNodes.
     * @param aName
     *            The original identifier.
     */
    public SourceNode() {
    }

    public SourceNode(Integer aLine, Integer aColumn, String aSource, String aChunks, String aName) {
        this.line = aLine;
        this.column = aColumn;
        this.source = aSource;
        this.name = aName;
        if (aChunks != null) {
            add(aChunks);
        }
    }

    public SourceNode(Integer aLine, Integer aColumn, String aSource, SourceNode aChunks, String aName) {
        this.line = aLine;
        this.column = aColumn;
        this.source = aSource;
        this.name = aName;
        if (aChunks != null) {
            add(aChunks);
        }
    }

    private static String shiftNextLine(List<String> remainingLines) {
        if (remainingLines.isEmpty()) {
            return null;
        }
        return remainingLines.remove(0) + "\n";
    }

    private static void addMappingWithCode(SourceNode node, String aRelativePath, Mapping mapping, String code) {
        if (mapping == null || mapping.source == null) {
            node.add(code);
        } else {
            String source = aRelativePath != null ? Util.join(aRelativePath, mapping.source) : mapping.source;
            node.add(new SourceNode(mapping.original.line, mapping.original.column, source, code, mapping.name));
        }
    }

    /**
     * Creates a SourceNode from generated code and a SourceMapConsumer.
     *
     * @param aGeneratedCode
     *            The generated code
     * @param aSourceMapConsumer
     *            The SourceMap for the generated code
     * @param aRelativePath
     *            Optional. The path that relative sources in the SourceMapConsumer should be relative to.
     */
    public static SourceNode fromStringWithSourceMap(String aGeneratedCode, SourceMapConsumer aSourceMapConsumer, final String aRelativePath) {
        // The SourceNode we want to fill with the generated code
        // and the SourceMap
        final SourceNode node = new SourceNode();

        // All even indices of this array are one line of the generated code,
        // while all odd indices are the newlines between two adjacent lines
        // (since `REGEX_NEWLINE` captures its match).
        // Processed fragments are removed from this array, by calling `shiftNextLine`.
        List<String> remainingLines = new ArrayList<>(Arrays.asList(REGEX_NEWLINE.split(aGeneratedCode)));

        // We need to remember the position of "remainingLines"
        int[] lastGeneratedLine = new int[] { 1 };
        int[] lastGeneratedColumn = new int[] { 0 };

        // The generate SourceNodes we need a code range.
        // To extract it current and last mapping is used.
        // Here we store the last mapping.
        Mapping[] lastMapping = new Mapping[1];

        aSourceMapConsumer.eachMapping().forEach(mapping -> {
            if (lastMapping[0] != null) {
                // We add the code from "lastMapping" to "mapping":
                // First check if there is a new line in between.
                if (lastGeneratedLine[0] < mapping.generated.line) {
                    // Associate first line with "lastMapping"
                    addMappingWithCode(node, aRelativePath, lastMapping[0], shiftNextLine(remainingLines));
                    lastGeneratedLine[0]++;
                    lastGeneratedColumn[0] = 0;
                    // The remaining code is added without mapping
                } else {
                    // There is no new line in between.
                    // Associate the code between "lastGeneratedColumn" and
                    // "mapping.generatedColumn" with "lastMapping"
                    String nextLine = remainingLines.get(0);
                    String code = Util.substr(nextLine, 0, mapping.generated.column - lastGeneratedColumn[0]);
                    remainingLines.set(0, Util.substr(nextLine, mapping.generated.column - lastGeneratedColumn[0]));
                    lastGeneratedColumn[0] = mapping.generated.column;
                    addMappingWithCode(node, aRelativePath, lastMapping[0], code);
                    // No more remaining code, continue
                    lastMapping[0] = mapping;
                    return;
                }
            }
            // We add the generated code until the first mapping
            // to the SourceNode without any mapping.
            // Each line is added as separate string.
            while (lastGeneratedLine[0] < mapping.generated.line) {
                node.add(shiftNextLine(remainingLines));
                lastGeneratedLine[0]++;
            }
            if (lastGeneratedColumn[0] < mapping.generated.column) {
                String nextLine = remainingLines.get(0);
                node.add(Util.substr(nextLine, 0, mapping.generated.column));
                remainingLines.set(0, Util.substr(nextLine, mapping.generated.column));
                lastGeneratedColumn[0] = mapping.generated.column;
            }
            lastMapping[0] = mapping;
        });
        // We have processed all mappings.
        if (remainingLines.size() > 0) {
            if (lastMapping[0] != null) {
                // Associate the remaining code in the current line with "lastMapping"
                addMappingWithCode(node, aRelativePath, lastMapping[0], shiftNextLine(remainingLines));
            }
            // and add the remaining lines without any mapping
            node.add(Util.join(remainingLines, "\n"));
        }

        // Copy sourcesContent into SourceNode
        aSourceMapConsumer.sources().forEach(sourceFile -> {
            String content = aSourceMapConsumer.sourceContentFor(sourceFile);
            if (content != null) {
                if (aRelativePath != null) {
                    sourceFile = Util.join(aRelativePath, sourceFile);
                }
                node.setSourceContent(sourceFile, content);
            }
        });

        return node;
    }

    /**
     * Add a chunk of generated JS to this source node.
     *
     * @param aChunk
     *            A string snippet of generated JS code, another instance of SourceNode, or an array where each member is one of those things.
     */
    public void add(List<String> aChunk) {
        aChunk.forEach(chunk -> {
            this.add(chunk);
        });
    }

    public void add(String aChunk) {
        this.children.add(aChunk);
    }

    public void add(SourceNode aChunk) {
        this.children.add(aChunk);
    }

    /**
     * Add a chunk of generated JS to the beginning of this source node.
     *
     * @param aChunk
     *            A string snippet of generated JS code, another instance of SourceNode, or an array where each member is one of those things.
     */
    public void prepend(List<String> aChunk) {
        for (int i = aChunk.size() - 1; i >= 0; i--) {
            this.prepend(aChunk.get(i));
        }
    }

    public void prepend(SourceNode aChunk) {
        this.children.add(0, aChunk);
    }

    public void prepend(String aChunk) {
        this.children.add(0, aChunk);
    }

    public static interface ChuckWalker {
        public void walk(String chunk, OriginalPosition pos);
    }

    /**
     * Walk over the tree of JS snippets in this node and its children. The walking function is called once for each snippet of JS and is passed that
     * snippet and the its original associated source's line/column location.
     *
     * @param aFn
     *            The traversal function.
     */
    public void walk(ChuckWalker walker) {
        Object chunk;
        for (int i = 0, len = this.children.size(); i < len; i++) {
            chunk = this.children.get(i);
            if (chunk instanceof SourceNode) {
                ((SourceNode) chunk).walk(walker);
            } else {
                if (((String) chunk).length() != 0) {
                    walker.walk((String) chunk, new OriginalPosition(this.line, this.column, this.source, this.name));
                }
            }
        }
    }

    /**
     * Like `String.prototype.join` except for SourceNodes. Inserts `aStr` between each of `this.children`.
     *
     * @param aSep
     *            The separator.
     */
    public void join(String aSep) {
        List<Object> newChildren;
        int i;
        int len = this.children.size();
        if (len > 0) {
            newChildren = new ArrayList<>();
            for (i = 0; i < len - 1; i++) {
                newChildren.add(this.children.get(i));
                newChildren.add(aSep);
            }
            newChildren.add(this.children.get(i));
            this.children = newChildren;
        }
    }

    /**
     * Call String.prototype.replace on the very right-most source snippet. Useful for trimming whitespace from the end of a source node, etc.
     *
     * @param aPattern
     *            The pattern to replace.
     * @param aReplacement
     *            The thing to replace the pattern with.
     */
    public void replaceRight(String aPattern, String aReplacement) {
        Object lastChild = this.children.get(this.children.size() - 1);
        if (lastChild instanceof SourceNode) {
            ((SourceNode) lastChild).replaceRight(aPattern, aReplacement);
        } else if (lastChild instanceof String) {
            this.children.set(this.children.size() - 1, ((String) lastChild).replaceFirst(aPattern, aReplacement));
        } else {
            this.children.add("".replaceFirst(aPattern, aReplacement));
        }
    }

    /**
     * Set the source content for a source file. This will be added to the SourceMapGenerator in the sourcesContent field.
     *
     * @param aSourceFile
     *            The filename of the source file
     * @param aSourceContent
     *            The content of the source file
     */
    public void setSourceContent(String aSourceFile, String aSourceContent) {
        this.sourceContents.put(aSourceFile, aSourceContent);
    }

    public static interface SourceWalker {
        public void walk(String chunk, String content);
    }

    /**
     * Walk over the tree of SourceNodes. The walking function is called for each source file content and is passed the filename and source content.
     *
     * @param aFn
     *            The traversal function.
     */
    public void walkSourceContents(SourceWalker walker) {
        for (int i = 0, len = this.children.size(); i < len; i++) {
            if (this.children.get(i) instanceof SourceNode) {
                ((SourceNode) this.children.get(i)).walkSourceContents(walker);
            }
        }

        List<String> sources = new ArrayList<>(this.sourceContents.keySet());
        for (int i = 0, len = sources.size(); i < len; i++) {
            walker.walk(sources.get(i), this.sourceContents.get(sources.get(i)));
        }
    }

    /**
     * Return the string representation of this source node. Walks over the tree and concatenates all the various snippets together to one string.
     */
    public String toString() {
        StringBuilder str = new StringBuilder();
        this.walk((chunk, pos) -> str.append(chunk));
        return str.toString();
    };

    /**
     * Returns the string representation of this source node along with a source map.
     */
    public Code toStringWithSourceMap(String file, String sourceRoot) {
        StringBuilder generatedCode = new StringBuilder();
        int[] generatedLine = new int[] { 1 };
        int[] generatedColumn = new int[] { 0 };
        final SourceMapGenerator map = new SourceMapGenerator(file, sourceRoot);
        boolean[] sourceMappingActive = new boolean[] { false };
        String[] lastOriginalSource = new String[1];
        Integer[] lastOriginalLine = new Integer[1];
        Integer[] lastOriginalColumn = new Integer[1];
        String[] lastOriginalName = new String[1];
        this.walk((chunk, original) -> {
            generatedCode.append(chunk);
            if (original.source != null && original.line != null && original.column != null) {
                if (original.source.equals(lastOriginalSource[0]) || lastOriginalLine[0] != original.line || lastOriginalColumn[0] != original.column
                        || (original.name != null && original.name.equals(lastOriginalName[0]))) {
                    map.addMapping(new Mapping(new Position(generatedLine[0], generatedColumn[0]), new Position(original.line, original.column),
                            original.source, original.name));
                }
                lastOriginalSource[0] = original.source;
                lastOriginalLine[0] = original.line;
                lastOriginalColumn[0] = original.column;
                lastOriginalName[0] = original.name;
                sourceMappingActive[0] = true;
            } else if (sourceMappingActive[0]) {
                map.addMapping(new Mapping(new Position(generatedLine[0], generatedColumn[0])));
                lastOriginalSource[0] = null;
                sourceMappingActive[0] = false;
            }
            for (int idx = 0, length = chunk.length(); idx < length; idx++) {
                if (chunk.charAt(idx) == NEWLINE_CODE) {
                    generatedLine[0]++;
                    generatedColumn[0] = 0;
                    // Mappings end at eol
                    if (idx + 1 == length) {
                        lastOriginalSource[0] = null;
                        sourceMappingActive[0] = false;
                    } else if (sourceMappingActive[0]) {
                        map.addMapping(new Mapping(new Position(generatedLine[0], generatedColumn[0]), new Position(original.line, original.column),
                                original.source, original.name));
                    }
                } else {
                    generatedColumn[0]++;
                }
            }
        });
        this.walkSourceContents((sourceFile, sourceContent) -> {
            map.setSourceContent(sourceFile, sourceContent);
        });

        return new Code(generatedCode.toString(), map);
    }

}
