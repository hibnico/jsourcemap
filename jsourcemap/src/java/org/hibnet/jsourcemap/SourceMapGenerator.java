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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SourceMapGenerator {

    String _file;
    String _sourceRoot;
    ArraySet<String> _sources;
    ArraySet<String> _names;
    MappingList _mappings;
    Map<String, String> _sourcesContents;

    /**
     * An instance of the SourceMapGenerator represents a source map which is being built incrementally. You may pass an object with the following
     * properties:
     * <ul>
     * <li>file: The filename of the generated source.</li>
     * <li>sourceRoot: A root for all relative URLs in this source map.</li>
     * </ul>
     */
    public SourceMapGenerator(String file, String sourceRoot) {
        this._file = file;
        this._sourceRoot = sourceRoot;
        this._sources = new ArraySet<>();
        this._names = new ArraySet<>();
        this._mappings = new MappingList();
        this._sourcesContents = null;
    }

    private int _version = 3;

    /**
     * Creates a new SourceMapGenerator based on a SourceMapConsumer
     *
     * @param aSourceMapConsumer
     *            The SourceMap.
     */
    public static SourceMapGenerator fromSourceMap(SourceMapConsumer aSourceMapConsumer) {
        String sourceRoot = aSourceMapConsumer.sourceRoot;
        SourceMapGenerator generator = new SourceMapGenerator(aSourceMapConsumer.file, sourceRoot);
        aSourceMapConsumer.eachMapping().forEach(mapping -> {
            Mapping newMapping = new Mapping(mapping.generated);
            if (mapping.source != null) {
                newMapping.source = mapping.source;
                if (sourceRoot != null) {
                    newMapping.source = Util.relative(sourceRoot, newMapping.source);
                }
                newMapping.original = mapping.original;
                if (mapping.name != null) {
                    newMapping.name = mapping.name;
                }
            }
            generator.addMapping(newMapping);
        });
        aSourceMapConsumer.sources().stream().forEach(sourceFile -> {
            String content = aSourceMapConsumer.sourceContentFor(sourceFile, null);
            if (content != null) {
                generator.setSourceContent(sourceFile, content);
            }
        });
        return generator;
    }

    /**
     * Add a single mapping from original source line and column to the generated source's line and column for this source map being created. The
     * mapping object should have the following properties:
     * <ul>
     * <li>generated: An object with the generated line and column positions.</li>
     * <li>original: An object with the original line and column positions.</li>
     * <li>source: The original source file (relative to the sourceRoot).</li>
     * <li>name: An optional original token name for this mapping.</li>
     * </ul>
     */
    public void addMapping(Mapping aArgs) {
        Position generated = aArgs.generated;
        Position original = aArgs.original;
        String source = aArgs.source;
        String name = aArgs.name;

        if (source != null && !this._sources.has(source)) {
            _sources.add(source, false);
        }
        if (name != null && !this._names.has(name)) {
            _names.add(name, false);
        }
        _mappings.add(new Mapping(generated, original, source, name));
    }

    /**
     * Set the source content for a source file.
     */
    public void setSourceContent(String aSourceFile, String aSourceContent) {
        String source = aSourceFile;
        if (this._sourceRoot != null) {
            source = Util.relative(this._sourceRoot, source);
        }

        if (aSourceContent != null) {
            // Add the source content to the _sourcesContents map.
            // Create a new _sourcesContents map if the property is null.
            if (this._sourcesContents == null) {
                this._sourcesContents = new HashMap<>();
            }
            this._sourcesContents.put(source, aSourceContent);
        } else if (this._sourcesContents != null) {
            // Remove the source file from the _sourcesContents map.
            // If the _sourcesContents map is empty, set the property to null.
            this._sourcesContents.remove(source);
            if (this._sourcesContents.isEmpty()) {
                this._sourcesContents = null;
            }
        }
    }

    /**
     * Applies the mappings of a sub-source-map for a specific source file to the source map being generated. Each mapping to the supplied source file
     * is rewritten using the supplied source map. Note: The resolution for the resulting mappings is the minimium of this map and the supplied map.
     *
     * @param aSourceMapConsumer
     *            The source map to be applied.
     * @param aSourceFile
     *            Optional. The filename of the source file. If omitted, SourceMapConsumer's file property will be used.
     * @param aSourceMapPath
     *            Optional. The dirname of the path to the source map to be applied. If relative, it is relative to the SourceMapConsumer. This
     *            parameter is needed when the two source maps aren't in the same directory, and the source map to be applied contains relative source
     *            paths. If so, those relative source paths need to be rewritten relative to the SourceMapGenerator.
     */
    void applySourceMap(SourceMapConsumer aSourceMapConsumer, String aSourceFile, String aSourceMapPath) {
        String sourceFile = aSourceFile;
        // If aSourceFile is omitted, we will use the file property of the SourceMap
        if (aSourceFile == null) {
            if (aSourceMapConsumer.file == null) {
                throw new RuntimeException("SourceMapGenerator.prototype.applySourceMap requires either an explicit source file, "
                        + "or the source map's \"file\" property. Both were omitted.");
            }
            sourceFile = aSourceMapConsumer.file;
        }
        String sourceRoot = this._sourceRoot;
        // Make "sourceFile" relative if an absolute Url is passed.
        if (sourceRoot != null) {
            sourceFile = Util.relative(sourceRoot, sourceFile);
        }
        // Applying the SourceMap can add and remove items from the sources and
        // the names array.
        ArraySet<String> newSources = new ArraySet<>();
        ArraySet<String> newNames = new ArraySet<>();

        final String f_sourceFile = sourceFile;
        // Find mappings for the "sourceFile"
        this._mappings.unsortedForEach().forEach(mapping -> {
            if (mapping.source.equals(f_sourceFile) && mapping.original != null) {
                // Check if it can be mapped by the source map, then update the mapping.
                OriginalPosition original = aSourceMapConsumer.originalPositionFor(mapping.original.line, mapping.original.column, null);
                if (original.source != null) {
                    // Copy mapping
                    mapping.source = original.source;
                    if (aSourceMapPath != null) {
                        mapping.source = Util.join(aSourceMapPath, mapping.source);
                    }
                    if (sourceRoot != null) {
                        mapping.source = Util.relative(sourceRoot, mapping.source);
                    }
                    mapping.original.line = original.line;
                    mapping.original.column = original.column;
                    if (original.name != null) {
                        mapping.name = original.name;
                    }
                }
            }

            String source = mapping.source;
            if (source != null && !newSources.has(source)) {
                newSources.add(source, false);
            }

            String name = mapping.name;
            if (name != null && !newNames.has(name)) {
                newNames.add(name, false);
            }
        });
        this._sources = newSources;
        this._names = newNames;

        // Copy sourcesContents of applied map.
        aSourceMapConsumer.sources().stream().forEach(source -> {
            String content = aSourceMapConsumer.sourceContentFor(source, null);
            if (content != null) {
                if (aSourceMapPath != null) {
                    source = Util.join(aSourceMapPath, source);
                }
                if (sourceRoot != null) {
                    source = Util.relative(sourceRoot, source);
                }
                this.setSourceContent(source, content);
            }
        });
    }

    /**
     * Serialize the accumulated mappings in to the stream of base 64 VLQs specified by the source map format.
     */
    private String serializeMappings() {
        int previousGeneratedColumn = 0;
        int previousGeneratedLine = 1;
        int previousOriginalColumn = 0;
        int previousOriginalLine = 0;
        int previousName = 0;
        int previousSource = 0;
        String result = "";
        Mapping mapping;
        int nameIdx;
        int sourceIdx;

        List<Mapping> mappings = this._mappings.toArray();
        for (int i = 0, len = mappings.size(); i < len; i++) {
            mapping = mappings.get(i);
            if (mapping.generated.line != previousGeneratedLine) {
                previousGeneratedColumn = 0;
                while (mapping.generated.line != previousGeneratedLine) {
                    result += ';';
                    previousGeneratedLine++;
                }
            } else {
                if (i > 0) {
                    if (Util.compareByGeneratedPositionsInflated(mapping, mappings.get(i - 1)) == 0) {
                        continue;
                    }
                    result += ',';
                }
            }

            result += Base64VLQ.encode(mapping.generated.column - previousGeneratedColumn);
            previousGeneratedColumn = mapping.generated.column;

            if (mapping.source != null) {
                sourceIdx = this._sources.indexOf(mapping.source);
                result += Base64VLQ.encode(sourceIdx - previousSource);
                previousSource = sourceIdx;

                // lines are stored 0-based in SourceMap spec version 3
                result += Base64VLQ.encode(mapping.original.line - 1 - previousOriginalLine);
                previousOriginalLine = mapping.original.line - 1;

                result += Base64VLQ.encode(mapping.original.column - previousOriginalColumn);
                previousOriginalColumn = mapping.original.column;

                if (mapping.name != null) {
                    nameIdx = this._names.indexOf(mapping.name);
                    result += Base64VLQ.encode(nameIdx - previousName);
                    previousName = nameIdx;
                }
            }
        }

        return result;
    }

    List<String> _generateSourcesContent(List<String> aSources, String aSourceRoot) {
        return aSources.stream().map(source -> {
            if (this._sourcesContents == null) {
                return null;
            }
            if (aSourceRoot != null) {
                source = Util.relative(aSourceRoot, source);
            }
            return this._sourcesContents.get(source);
        }).collect(Collectors.toList());
    }

    /**
     * Externalize the source map.
     */
    public SourceMap toJSON() {
        SourceMap map = new SourceMap();
        map.version = this._version;
        map.sources = this._sources.toArray();
        map.names = this._names.toArray();
        map.mappings = serializeMappings();
        map.file = this._file;
        map.sourceRoot = this._sourceRoot;
        if (this._sourcesContents != null) {
            map.sourcesContent = _generateSourcesContent(map.sources, this._sourceRoot);
        }
        return map;
    }

}
