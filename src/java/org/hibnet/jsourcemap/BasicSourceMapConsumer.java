package org.hibnet.jsourcemap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hibnet.jsourcemap.Base64VLQ.Base64VLQResult;
import org.hibnet.jsourcemap.BinarySearch.Bias;
import org.hibnet.jsourcemap.Util.ParsedUrl;

/**
 * A BasicSourceMapConsumer instance represents a parsed source map which we can query for information about the
 * original file positions by giving it a file position in the generated source.
 *
 * The only parameter is the raw source map (either as a JSON string, or already parsed to an object). According to the
 * spec, source maps have the following attributes:
 * <ul>
 * <li>version: Which version of the source map spec this map is following.</li>
 * <li>sources: An array of URLs to the original source files.</li>
 * <li>names: An array of identifiers which can be referrenced by individual mappings.</li>
 * <li>sourceRoot: Optional. The URL root from which all sources are relative.</li>
 * <li>sourcesContent: Optional. An array of contents of the original source files.</li>
 * <li>mappings: A string of base64 VLQs which contain the actual mappings.</li>
 * <li>file: Optional. The generated file this source map is associated with.</li>
 * </ul>
 * Here is an example source map, taken from the source map spec[0]:
 * 
 * <pre>
 *     {
 *       version : 3,
 *       file: "out.js",
 *       sourceRoot : "",
 *       sources: ["foo.js", "bar.js"],
 *       names: ["src", "maps", "are", "fun"],
 *       mappings: "AA,AB;;ABCDE;"
 *     }
 * </pre>
 * 
 * [0]: https://docs.google.com/document/d/1U1RGAehQwRypUTovF1KRlpiOFze0b-_2gc6fAH0KY0k/edit?pli=1#
 */
public class BasicSourceMapConsumer extends SourceMapConsumer {

    private List<String> sourcesContent;

    public BasicSourceMapConsumer(Object aSourceMap) {
        SourceMap sourceMap;
        if (aSourceMap instanceof String) {
            sourceMap = new SourceMap(((String) aSourceMap).replace("^\\)\\]\\}'", ""));
        } else if (aSourceMap instanceof SourceMap) {
            sourceMap = (SourceMap) aSourceMap;
        } else {
            throw new IllegalArgumentException();
        }

        int version = sourceMap.version;
        List<String> sources = sourceMap.sources;
        // Sass 3.3 leaves out the 'names' array, so we deviate from the spec (which
        // requires the array) to play nice here.
        List<String> names = sourceMap.names == null ? Collections.emptyList() : sourceMap.names;
        String sourceRoot = sourceMap.sourceRoot;
        List<String> sourcesContent = sourceMap.sourcesContent;
        String mappings = sourceMap.mappings;
        String file = sourceMap.file;

        // Once again, Sass deviates from the spec and supplies the version as a
        // string rather than a number, so we use loose equality checking here.
        if (version != this._version) {
            throw new RuntimeException("Unsupported version:" + version);
        }

        sources = sources.stream()
                // Some source maps produce relative source paths like "./foo.js" instead of
                // "foo.js". Normalize these first so that future comparisons will succeed.
                // See bugzil.la/1090768.
                .map(Util::normalize)
                // Always ensure that absolute sources are internally stored relative to
                // the source root, if the source root is absolute. Not doing this would
                // be particularly problematic when the source root is a prefix of the
                // source (valid, but why??). See github issue #199 and bugzil.la/1188982.
                .map(source -> sourceRoot != null && Util.isAbsolute(sourceRoot) && Util.isAbsolute(source)
                        ? Util.relative(sourceRoot, source) : source)
                .collect(Collectors.toList());

        // Pass `true` below to allow duplicate names and sources. While source maps
        // are intended to be compressed and deduplicated, the TypeScript compiler
        // sometimes generates source maps with duplicates in them. See Github issue
        // #72 and bugzil.la/889492.
        this._names = ArraySet.fromArray(names, true);
        this._sources = ArraySet.fromArray(sources, true);

        this.sourceRoot = sourceRoot;
        this.sourcesContent = sourcesContent;
        this._mappings = mappings;
        this.file = file;
    }

    private BasicSourceMapConsumer() {
        // TODO Auto-generated constructor stub
    }

    /**
     * Create a BasicSourceMapConsumer from a SourceMapGenerator.
     *
     * @param SourceMapGenerator
     *            aSourceMap The source map that will be consumed.
     * @returns BasicSourceMapConsumer
     */
    public static BasicSourceMapConsumer fromSourceMap(SourceMapGenerator aSourceMap) {
        BasicSourceMapConsumer smc = new BasicSourceMapConsumer();

        ArraySet<String> names = smc._names = ArraySet.fromArray(aSourceMap._names.toArray(), true);
        ArraySet<String> sources = smc._sources = ArraySet.fromArray(aSourceMap._sources.toArray(), true);
        smc.sourceRoot = aSourceMap._sourceRoot;
        smc.sourcesContent = aSourceMap._generateSourcesContent(smc._sources.toArray(), smc.sourceRoot);
        smc.file = aSourceMap._file;

        // Because we are modifying the entries (by converting string sources and
        // names to indices into the sources and names ArraySets), we have to make
        // a copy of the entry or else bad things happen. Shared mutable state
        // strikes again! See github issue #191.

        List<Mapping> generatedMappings = new ArrayList<>(aSourceMap._mappings.toArray());
        List<Mapping> destGeneratedMappings = smc.__generatedMappings = new ArrayList<>();
        List<Mapping> destOriginalMappings = smc.__originalMappings = new ArrayList<>();

        for (int i = 0, length = generatedMappings.size(); i < length; i++) {
            Mapping srcMapping = generatedMappings.get(i);
            Mapping destMapping = new Mapping();
            destMapping.generatedLine = srcMapping.generatedLine;
            destMapping.generatedColumn = srcMapping.generatedColumn;

            if (srcMapping.source != null) {
                destMapping.source = sources.indexOf(srcMapping.source);
                destMapping.originalLine = srcMapping.originalLine;
                destMapping.originalColumn = srcMapping.originalColumn;
                if (srcMapping.name != null) {
                    destMapping.name = names.indexOf(srcMapping.name);
                }

                destOriginalMappings.add(destMapping);
            }

            destGeneratedMappings.add(destMapping);
        }

        Collections.sort(smc.__originalMappings, new Comparator<Mapping>() {
            @Override
            public int compare(Mapping o1, Mapping o2) {
                return Util.compareByOriginalPositions(o1, o2, null);
            }
        });

        return smc;
    }

    /**
     * The version of the source mapping spec that we are consuming.
     */
    int _version = 3;

    @Override
    protected List<String> sources() {
        return this._sources.toArray().stream().map(s -> this.sourceRoot != null ? Util.join(this.sourceRoot, s) : s)
                .collect(Collectors.toList());
    }

    /**
     * Parse the mappings in a string in to a data structure which we can easily query (the ordered arrays in the
     * `this.__generatedMappings` and `this.__originalMappings` properties).
     */
    @Override
    protected void _parseMappings(String aStr, String aSourceRoot) {
        int generatedLine = 1;
        int previousGeneratedColumn = 0;
        int previousOriginalLine = 0;
        int previousOriginalColumn = 0;
        int previousSource = 0;
        int previousName = 0;
        int length = aStr.length();
        int index = 0;
        Map<String, List<Integer>> cachedSegments = new HashMap<>();
        Base64VLQResult temp;
        List<Mapping> originalMappings = new ArrayList<>();
        List<Mapping> generatedMappings = new ArrayList<>();
        Mapping mapping;
        String str;
        List<Integer> segment;
        int end;
        int value;

        while (index < length) {
            if (aStr.charAt(index) == ';') {
                generatedLine++;
                index++;
                previousGeneratedColumn = 0;
            } else if (aStr.charAt(index) == ',') {
                index++;
            } else {
                mapping = new Mapping();
                mapping.generatedLine = generatedLine;

                // Because each offset is encoded relative to the previous one,
                // many segments often have the same encoding. We can exploit this
                // fact by caching the parsed variable length fields of each segment,
                // allowing us to avoid a second parse if we encounter the same
                // segment again.
                for (end = index; end < length; end++) {
                    if (this._charIsMappingSeparator(aStr, end)) {
                        break;
                    }
                }
                str = aStr.substring(index, end);

                segment = cachedSegments.get(str);
                if (segment != null) {
                    index += str.length();
                } else {
                    segment = new ArrayList<>();
                    while (index < end) {
                        Base64VLQResult tmp = Base64VLQ.decode(aStr, index, temp);
                        value = temp.value;
                        index = temp.rest;
                        segment.add(value);
                    }

                    if (segment.size() == 2) {
                        throw new Error("Found a source, but no line and column");
                    }

                    if (segment.size() == 3) {
                        throw new Error("Found a source and line, but no column");
                    }

                    cachedSegments.put(str, segment);
                }

                // Generated column.
                mapping.generatedColumn = previousGeneratedColumn + segment.get(0);
                previousGeneratedColumn = mapping.generatedColumn;

                if (segment.size() > 1) {
                    // Original source.
                    mapping.source = previousSource + segment.get(1);
                    previousSource += segment.get(1);

                    // Original line.
                    mapping.originalLine = previousOriginalLine + segment.get(2);
                    previousOriginalLine = mapping.originalLine;
                    // Lines are stored 0-based
                    mapping.originalLine += 1;

                    // Original column.
                    mapping.originalColumn = previousOriginalColumn + segment.get(3);
                    previousOriginalColumn = mapping.originalColumn;

                    if (segment.size() > 4) {
                        // Original name.
                        mapping.name = previousName + segment.get(4);
                        previousName += segment.get(4);
                    }
                }

                generatedMappings.add(mapping);
                if (mapping.originalLine != null) {
                    originalMappings.add(mapping);
                }
            }
        }

        Collections.sort(generatedMappings, new Comparator<Mapping>() {
            @Override
            public int compare(Mapping o1, Mapping o2) {
                return Util.compareByGeneratedPositionsDeflated(o1, o2, null);
            }
        });
        this.__generatedMappings = generatedMappings;

        Collections.sort(originalMappings, new Comparator<Mapping>() {
            @Override
            public int compare(Mapping o1, Mapping o2) {
                return Util.compareByOriginalPositions(o1, o2, null);
            }
        });
        this.__originalMappings = originalMappings;
    }

    /**
     * Find the mapping that best matches the hypothetical "needle" mapping that we are searching for in the given
     * "haystack" of mappings.
     */
    private int findMapping(Needle aNeedle, List aMappings, Object aLineName, Object aColumnName,
            Comparator aComparator, BinarySearch.Bias aBias) {
        // To return the position we are searching for, we must first find the
        // mapping for the given position and then return the opposite position it
        // points to. Because the mappings are sorted, we can use binary search to
        // find the best mapping.

        if (aNeedle[aLineName] <= 0) {
            throw new TypeError("Line must be greater than or equal to 1, got " + aNeedle[aLineName]);
        }
        if (aNeedle[aColumnName] < 0) {
            throw new TypeError("Column must be greater than or equal to 0, got " + aNeedle[aColumnName]);
        }

        return BinarySearch.search(aNeedle, aMappings, aComparator, aBias);
    }

    /**
     * Compute the last column for each generated mapping. The last column is inclusive.
     */
    private void computeColumnSpans() {
        for (int index = 0; index < _generatedMappings().size(); ++index) {
            Mapping mapping = _generatedMappings().get(index);

            // Mappings do not contain a field for the last generated columnt. We
            // can come up with an optimistic estimate, however, by assuming that
            // mappings are contiguous (i.e. given two consecutive mappings, the
            // first mapping ends where the second one starts).
            if (index + 1 < _generatedMappings().size()) {
                Mapping nextMapping = _generatedMappings().get(index + 1);

                if (mapping.generatedLine == nextMapping.generatedLine) {
                    mapping.lastGeneratedColumn = nextMapping.generatedColumn - 1;
                    continue;
                }
            }

            // The last mapping for each line spans the entire line.
            mapping.lastGeneratedColumn = Integer.MAX_VALUE;
        }
    }

    /**
     * Returns the original source, line, and column information for the generated source's line and column positions
     * provided. The only argument is an object with the following properties:
     * <ul>
     * <li>line: The line number in the generated source.</li>
     * <li>column: The column number in the generated source.</li>
     * <li>bias: Either 'SourceMapConsumer.GREATEST_LOWER_BOUND' or 'SourceMapConsumer.LEAST_UPPER_BOUND'. Specifies
     * whether to return the closest element that is smaller than or greater than the one we are searching for,
     * respectively, if the exact element cannot be found. Defaults to 'SourceMapConsumer.GREATEST_LOWER_BOUND'.</li>
     * </ul>
     * and an object is returned with the following properties:
     * <ul>
     * <li>source: The original source file, or null.</li>
     * <li>line: The line number in the original source, or null.</li>
     * <li>column: The column number in the original source, or null.</li>
     * <li>name: The original identifier, or null.</li>
     * </ul>
     */
    @Override
    OriginalMapping originalPositionFor(int line, int column, Bias bias) {
        Needle needle = new Needle(line, column);
        if (bias == null) {
            bias = Bias.GREATEST_LOWER_BOUND;
        }
        int index = this._findMapping(needle, this._generatedMappings(), "generatedLine", "generatedColumn",
                Util.compareByGeneratedPositionsDeflated, bias);

        if (index >= 0) {
            Mapping mapping = this._generatedMappings().get(index);

            if (mapping.generatedLine == needle.generatedLine)

            {
                String source = mapping.source;
                if (source != null) {
                    source = this._sources.at(source);
                    if (this.sourceRoot != null) {
                        source = Util.join(this.sourceRoot, source);
                    }
                }
                String name = mapping.name;
                if (name != null) {
                    name = this._names.at(name);
                }
                return new OriginalMapping(mapping.originalLine, mapping.originalColumn, source, name);
            }

        }
        return new OriginalMapping();
    }

    /**
     * Return true if we have the source content for every source in the source map, false otherwise.
     */
    @Override
    boolean hasContentsOfAllSources() {
        if (sourcesContent == null) {
            return false;
        }
        return this.sourcesContent.size() >= this._sources.size()
                && !this.sourcesContent.stream().anyMatch(sc -> sc == null);
    }

    /**
     * Returns the original source content. The only argument is the url of the original source file. Returns null if no
     * original source content is available.
     */
    @Override
    String sourceContentFor(String aSource, Boolean nullOnMissing) {
        if (this.sourcesContent == null) {
            return null;
        }

        if (this.sourceRoot != null) {
            aSource = Util.relative(this.sourceRoot, aSource);
        }

        if (this._sources.has(aSource)) {
            return this.sourcesContent.get(this._sources.indexOf(aSource));
        }

        ParsedUrl url;
        if (this.sourceRoot != null && ((url = Util.urlParse(this.sourceRoot)) != null)) {
            // XXX: file:// URIs and absolute paths lead to unexpected behavior for
            // many users. We can help them out when they expect file:// URIs to
            // behave like it would if they were running a local HTTP server. See
            // https://bugzilla.mozilla.org/show_bug.cgi?id=885597.
            String fileUriAbsPath = aSource.replace("^file://", "");
            if (url.scheme.equals("file") && this._sources.has(fileUriAbsPath)) {
                return this.sourcesContent.get(this._sources.indexOf(fileUriAbsPath));
            }

            if ((url.path == null || url.path.equals("/")) && this._sources.has("/" + aSource)) {
                return this.sourcesContent.get(this._sources.indexOf("/" + aSource));
            }
        }

        // This function is used recursively from
        // IndexedSourceMapConsumer.prototype.sourceContentFor. In that case, we
        // don't want to throw if we can't find the source - we just want to
        // return null, so we provide a flag to exit gracefully.
        if (nullOnMissing != null && nullOnMissing) {
            return null;
        } else {
            throw new RuntimeException("\"" + aSource + "\" is not in the SourceMap.");
        }
    }

    /**
     * Returns the generated line and column information for the original source, line, and column positions provided.
     * The only argument is an object with the following properties:
     *
     * - source: The filename of the original source. - line: The line number in the original source. - column: The
     * column number in the original source. - bias: Either 'SourceMapConsumer.GREATEST_LOWER_BOUND' or
     * 'SourceMapConsumer.LEAST_UPPER_BOUND'. Specifies whether to return the closest element that is smaller than or
     * greater than the one we are searching for, respectively, if the exact element cannot be found. Defaults to
     * 'SourceMapConsumer.GREATEST_LOWER_BOUND'.
     *
     * and an object is returned with the following properties:
     *
     * - line: The line number in the generated source, or null. - column: The column number in the generated source, or
     * null.
     */
    @Override
    Position generatedPositionFor(String source, int line, int column, Bias bias) {
        if (this.sourceRoot != null) {
            source = Util.relative(this.sourceRoot, source);
        }
        if (!this._sources.has(source)) {
            return new Position();
        }
        int source_ = this._sources.indexOf(source);

        Needle needle = new Needle(source_, line, column);

        if (bias == null) {
            bias = Bias.GREATEST_LOWER_BOUND;
        }

        int index = findMapping(needle, this._originalMappings(), "originalLine", "originalColumn",
                Util::compareByOriginalPositions, bias);

        if (index >= 0) {
            Mapping mapping = this._originalMappings().get(index);

            if (mapping.source == needle.source) {
                return new Position(mapping.generatedLine != null ? mapping.generatedLine : null,
                        mapping.generatedColumn != null ? mapping.generatedColumn : null,
                        mapping.lastGeneratedColumn != null ? mapping.lastGeneratedColumn : null);
            }
        }

        return new Position();
    }
}
