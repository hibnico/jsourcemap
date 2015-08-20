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
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.hibnet.jsourcemap.BinarySearch.Bias;

public abstract class SourceMapConsumer {

    ArraySet<String> _names;
    ArraySet<String> _sources;
    String sourceRoot;
    List<String> sourcesContent;
    String _mappings;
    String file;

    enum Order {
        generated, original
    }

    static class ParsedMapping {

        Integer generatedLine;

        Integer generatedColumn;

        Integer originalLine;

        Integer originalColumn;

        Integer source;

        Integer name;

        Integer lastGeneratedColumn;

        ParsedMapping() {
            // TODO Auto-generated constructor stub
        }

        ParsedMapping(Integer generatedLine, Integer generatedColumn) {
            this.generatedLine = generatedLine;
            this.generatedColumn = generatedColumn;
        }

        ParsedMapping(Integer generatedLine, Integer generatedColumn, Integer originalLine, Integer originalColumn, Integer source) {
            this.generatedLine = generatedLine;
            this.generatedColumn = generatedColumn;
            this.originalLine = originalLine;
            this.originalColumn = originalColumn;
            this.source = source;
        }

        ParsedMapping(Integer generatedLine, Integer generatedColumn, Integer originalLine, Integer originalColumn, Integer source, Integer name) {
            this.generatedLine = generatedLine;
            this.generatedColumn = generatedColumn;
            this.originalLine = originalLine;
            this.originalColumn = originalColumn;
            this.source = source;
            this.name = name;
        }

        ParsedMapping(Integer generatedLine, Integer generatedColumn, Integer lastGeneratedColumn) {
            this.generatedLine = generatedLine;
            this.generatedColumn = generatedColumn;
            this.lastGeneratedColumn = lastGeneratedColumn;
        }

    }

    public static SourceMapConsumer create(SourceMap sourceMap) {
        return sourceMap.sections != null ? new IndexedSourceMapConsumer(sourceMap) : new BasicSourceMapConsumer(sourceMap);
    }

    public static SourceMapConsumer fromSourceMap(SourceMapGenerator aSourceMap) {
        return BasicSourceMapConsumer.fromSourceMap(aSourceMap);
    }

    /**
     * The version of the source mapping spec that we are consuming.
     */
    int _version = 3;

    // `__generatedMappings` and `__originalMappings` are arrays that hold the
    // parsed mapping coordinates from the source map's "mappings" attribute. They
    // are lazily instantiated, accessed via the `_generatedMappings` and
    // `_originalMappings` getters respectively, and we only parse the mappings
    // and create these arrays once queried for a source location. We jump through
    // these hoops because there can be many thousands of mappings, and parsing
    // them is expensive, so we only want to do it if we must.
    //
    // Each object in the arrays is of the form:
    //
    // {
    // generatedLine: The line number in the generated code,
    // generatedColumn: The column number in the generated code,
    // source: The path to the original source file that generated this
    // chunk of code,
    // originalLine: The line number in the original source that
    // corresponds to this chunk of generated code,
    // originalColumn: The column number in the original source that
    // corresponds to this chunk of generated code,
    // name: The name of the original symbol which generated this chunk of
    // code.
    // }
    //
    // All properties except for `generatedLine` and `generatedColumn` can be
    // `null`.
    //
    // `_generatedMappings` is ordered by the generated positions.
    //
    // `_originalMappings` is ordered by the original positions.

    List<ParsedMapping> __generatedMappings = null;

    List<ParsedMapping> _generatedMappings() {
        if (this.__generatedMappings == null) {
            this._parseMappings(this._mappings, this.sourceRoot);
        }
        return this.__generatedMappings;
    }

    List<ParsedMapping> __originalMappings = null;

    List<ParsedMapping> _originalMappings() {
        if (this.__originalMappings == null) {
            this._parseMappings(this._mappings, this.sourceRoot);
        }
        return this.__originalMappings;
    }

    boolean _charIsMappingSeparator(String aStr, int index) {
        char c = aStr.charAt(index);
        return c == ';' || c == ',';
    }

    /**
     * Parse the mappings in a string in to a data structure which we can easily query (the ordered arrays in the `this.__generatedMappings` and
     * `this.__originalMappings` properties).
     */
    abstract void _parseMappings(String aStr, String aSourceRoot);

    public abstract List<String> sources();

    public String sourceContentFor(String aSource) {
        return sourceContentFor(aSource, null);
    }

    abstract String sourceContentFor(String aSource, Boolean nullOnMissing);

    public abstract OriginalPosition originalPositionFor(int line, int column, Bias bias);

    public abstract boolean hasContentsOfAllSources();

    public abstract GeneratedPosition generatedPositionFor(String source, int line, int column, Bias bias);

    /**
     * Iterate over each mapping between an original source/line/column and a generated line/column in this source map.
     *
     * @param Function
     *            aCallback The function that is called with each mapping.
     * @param Object
     *            aContext Optional. If specified, this object will be the value of `this` every time that `aCallback` is called.
     * @param aOrder
     *            Either `SourceMapConsumer.GENERATED_ORDER` or `SourceMapConsumer.ORIGINAL_ORDER`. Specifies whether you want to iterate over the
     *            mappings sorted by the generated file's line/column order or the original's source/line/column order, respectively. Defaults to
     *            `SourceMapConsumer.GENERATED_ORDER`.
     */
    public Stream<Mapping> eachMapping() {
        return eachMapping(null);
    }

    Stream<Mapping> eachMapping(Order aOrder) {
        if (aOrder == null) {
            aOrder = Order.generated;
        }

        List<ParsedMapping> mappings;
        switch (aOrder) {
        case generated:
            mappings = _generatedMappings();
            break;
        case original:
            mappings = _originalMappings();
            break;
        default:
            throw new RuntimeException("Unknown order of iteration.");
        }

        String sourceRoot = this.sourceRoot;
        return mappings.stream().map(mapping -> {
            String source = mapping.source == null ? null : this._sources.at(mapping.source);
            if (source != null && sourceRoot != null) {
                source = Util.join(sourceRoot, source);
            }
            return new Mapping(new Position(mapping.generatedLine, mapping.generatedColumn),
                    new Position(mapping.originalLine, mapping.originalColumn), source,
                    mapping.name == null ? null : this._names.at(mapping.name));
        });
    }

    /**
     * Returns all generated line and column information for the original source, line, and column provided. If no column is provided, returns all
     * mappings corresponding to a either the line we are searching for or the next closest line that has any mappings. Otherwise, returns all
     * mappings corresponding to the given line and either the column we are searching for or the next closest column that has any offsets.
     *
     * The only argument is an object with the following properties:
     * <ul>
     * <li>source: The filename of the original source.</li>
     * <li>line: The line number in the original source.</li>
     * <li>column: Optional. the column number in the original source.</li>
     * </ul>
     * and an array of objects is returned, each with the following properties:
     * <ul>
     * <li>line: The line number in the generated source, or null.</li>
     * <li>column: The column number in the generated source, or null.</li>
     * </ul>
     */
    public List<GeneratedPosition> allGeneratedPositionsFor(int line, Integer column, String source) {
        // When there is no exact match, BasicSourceMapConsumer.prototype._findMapping
        // returns the index of the closest mapping less than the needle. By
        // setting needle.originalColumn to 0, we thus find the last mapping for
        // the given line, provided such a mapping exists.
        ParsedMapping needle = new ParsedMapping(null, null, line, column == null ? 0 : column, null, null);

        if (this.sourceRoot != null) {
            source = Util.relative(this.sourceRoot, source);
        }
        if (!this._sources.has(source)) {
            return Collections.emptyList();
        }
        needle.source = this._sources.indexOf(source);

        List<GeneratedPosition> mappings = new ArrayList<>();

        int index = _findMapping(needle, this._originalMappings(), "originalLine", "originalColumn",
                (mapping1, mapping2) -> Util.compareByOriginalPositions(mapping1, mapping2, true), BinarySearch.Bias.LEAST_UPPER_BOUND);
        if (index >= 0) {
            ParsedMapping mapping = this._originalMappings().get(index);

            if (column == null) {
                int originalLine = mapping.originalLine;

                // Iterate until either we run out of mappings, or we run into
                // a mapping for a different line than the one we found. Since
                // mappings are sorted, this is guaranteed to find all mappings for
                // the line we found.
                while (mapping != null && mapping.originalLine == originalLine) {
                    mappings.add(new GeneratedPosition(mapping.generatedLine, mapping.generatedColumn, mapping.lastGeneratedColumn));
                    index++;
                    if (index >= this._originalMappings().size()) {
                        mapping = null;
                    } else {
                        mapping = this._originalMappings().get(index);
                    }
                }
            } else {
                int originalColumn = mapping.originalColumn;

                // Iterate until either we run out of mappings, or we run into
                // a mapping for a different line than the one we were searching for.
                // Since mappings are sorted, this is guaranteed to find all mappings for
                // the line we are searching for.
                while (mapping != null && mapping.originalLine == line && mapping.originalColumn == originalColumn) {
                    mappings.add(new GeneratedPosition(mapping.generatedLine, mapping.generatedColumn, mapping.lastGeneratedColumn));
                    index++;
                    if (index >= this._originalMappings().size()) {
                        mapping = null;
                    } else {
                        mapping = this._originalMappings().get(index);
                    }
                }
            }
        }

        return mappings;
    }

    /**
     * Find the mapping that best matches the hypothetical "needle" mapping that we are searching for in the given "haystack" of mappings.
     */
    <T> int _findMapping(T aNeedle, List<T> aMappings, Object aLineName, Object aColumnName, BinarySearch.Comparator<T> aComparator,
            BinarySearch.Bias aBias) {
        // To return the position we are searching for, we must first find the
        // mapping for the given position and then return the opposite position it
        // points to. Because the mappings are sorted, we can use binary search to
        // find the best mapping.

        // if (aNeedle[aLineName] <= 0) {
        // throw new TypeError("Line must be greater than or equal to 1, got " + aNeedle[aLineName]);
        // }
        // if (aNeedle[aColumnName] < 0) {
        // throw new TypeError("Column must be greater than or equal to 0, got " + aNeedle[aColumnName]);
        // }

        return BinarySearch.search(aNeedle, aMappings, aComparator, aBias);
    }

}
