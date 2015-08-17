package org.hibnet.jsourcemap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.hibnet.jsourcemap.BinarySearch.Bias;
import org.hibnet.jsourcemap.Section.Offset;

/**
 * An IndexedSourceMapConsumer instance represents a parsed source map which we can query for information. It differs
 * from BasicSourceMapConsumer in that it takes "indexed" source maps (i.e. ones with a "sections" field) as input.
 * <p>
 * The only parameter is a raw source map (either as a JSON string, or already parsed to an object). According to the
 * spec for indexed source maps, they have the following attributes:
 * <ul>
 * <li>version: Which version of the source map spec this map is following.</li>
 * <li>file: Optional. The generated file this source map is associated with.</li>
 * <li>sections: A list of section definitions.</li>
 * </ul>
 * Each value under the "sections" field has two fields:
 * <ul>
 * <li>offset: The offset into the original specified at which this section begins to apply, defined as an object with a
 * "line" and "column" field.</li>
 * <li>map: A source map definition. This source map could also be indexed, but doesn't have to be.</li>
 * </ul>
 * Instead of the "map" field, it's also possible to have a "url" field specifying a URL to retrieve a source map from,
 * but that's currently unsupported.
 * <p>
 * Here's an example source map, taken from the source map spec[0], but modified to omit a section which uses the "url"
 * field.
 * 
 * <pre>
 *  {
 *    version : 3,
 *    file: "app.js",
 *    sections: [{
 *      offset: {line:100, column:10},
 *      map: {
 *        version : 3,
 *        file: "section.js",
 *        sources: ["foo.js", "bar.js"],
 *        names: ["src", "maps", "are", "fun"],
 *        mappings: "AAAA,E;;ABCDE;"
 *      }
 *    }],
 *  }
 * </pre>
 * 
 * [0]: https://docs.google.com/document/d/1U1RGAehQwRypUTovF1KRlpiOFze0b-_2gc6fAH0KY0k/edit#heading=h.535es3xeprgt
 */
public class IndexedSourceMapConsumer extends SourceMapConsumer {

    public static class ParsedSection {
        Offset generatedOffset;
        SourceMapConsumer consumer;

        public ParsedSection(Offset generatedOffset, SourceMapConsumer consumer) {
            this.generatedOffset = generatedOffset;
            this.consumer = consumer;
        }
    }

    private List<ParsedSection> _sections;

    public IndexedSourceMapConsumer(Object aSourceMap) {
        SourceMap sourceMap;
        if (aSourceMap instanceof String) {
            sourceMap = new SourceMap(((String) aSourceMap).replace("^\\)\\]\\}'", ""));
        } else if (aSourceMap instanceof SourceMap) {
            sourceMap = (SourceMap) aSourceMap;
        } else {
            throw new IllegalArgumentException();
        }
        int version = sourceMap.version;
        List<Section> sections = sourceMap.sections;

        if (version != this._version) {
            throw new RuntimeException("Unsupported version: " + version);
        }

        this._sources = new ArraySet<>();
        this._names = new ArraySet<>();

        final Offset[] lastOffset = new Offset[1];
        lastOffset[0] = new Offset(-1, 0);
        this._sections = sections.stream().map(s -> {
            if (s.url != null) {
                // The url field will require support for asynchronicity.
                // See https://github.com/mozilla/source-map/issues/16
                throw new RuntimeException("Support for url field in sections not implemented.");
            }
            Offset offset = s.offset;
            int offsetLine = offset.line;
            int offsetColumn = offset.column;

            if (offsetLine < lastOffset[0].line
                    || (offsetLine == lastOffset[0].line && offsetColumn < lastOffset[0].column)) {
                throw new RuntimeException("Section offsets must be ordered and non-overlapping.");
            }
            lastOffset[0] = offset;

            return new ParsedSection(new Offset(offsetLine + 1, offsetColumn + 1), SourceMapConsumer.create(s.map));
        }).collect(Collectors.toList());
    }

    /**
     * The version of the source mapping spec that we are consuming.
     */
    int _version = 3;

    /**
     * The list of original sources.
     */
    @Override
    protected List<String> sources() {
        List<String> sources = new ArrayList<>();
        for (int i = 0; i < this._sections.size(); i++) {
            for (int j = 0; j < this._sections.get(i).consumer.sources().size(); j++) {
                sources.add(this._sections.get(i).consumer.sources().get(j));
            }
        }
        return sources;
    }

    /**
     * Returns the original source, line, and column information for the generated source's line and column positions
     * provided. The only argument is an object with the following properties:
     * <ul>
     * <li>line: The line number in the generated source.</li>
     * <li>column: The column number in the generated source.</li>
     * </ul>
     * and an object is returned with the following properties:
     * <ul>
     * <li>source: The original source file, or null.</li>
     * <li>line: The line number in the original source, or null.</li>
     * <li>column: The column number in the original source, or null.</li>
     * <li>name: The original identifier, or null.</li>
     * </ul>
     */
    public OriginalMapping originalPositionFor(int line, int column, Bias bias) {
        Needle needle = new Needle(line, column);

        // Find the section containing the generated position we're trying to map
        // to an original position.
        int sectionIndex = BinarySearch.search(needle, this._sections,
          needle, section -> {
            int cmp = needle.generatedLine - section.generatedOffset.generatedLine;
            if (cmp) {
              return cmp;
            }

            return (needle.generatedColumn - section.generatedOffset.generatedColumn);
          }, null);
        ParsedSection section = this._sections.get(sectionIndex);

        if (section == null) {
          return new OriginalMapping();
        }

        return section.consumer.originalPositionFor(
           needle.generatedLine -
            (section.generatedOffset.generatedLine - 1), needle.generatedColumn -
            (section.generatedOffset.generatedLine == needle.generatedLine
             ? section.generatedOffset.generatedColumn - 1
             : 0), bias);
        );
}

    /**
     * Return true if we have the source content for every source in the source map, false otherwise.
     */
    @Override
    boolean hasContentsOfAllSources() {
        return this._sections.stream().allMatch(s -> s.consumer.hasContentsOfAllSources());
    }

    /**
     * Returns the original source content. The only argument is the url of the original source file. Returns null if no
     * original source content is available.
     */
    @Override
    String sourceContentFor(String aSource, Boolean nullOnMissing) {
        for (int i = 0; i < this._sections.size(); i++) {
            ParsedSection section = this._sections.get(i);
            String content = section.consumer.sourceContentFor(aSource, true);
            if (content != null) {
                return content;
            }
        }
        if (nullOnMissing != null && nullOnMissing) {
            return null;
        } else {
            throw new RuntimeException("\"" + aSource + "\" is not in the SourceMap.");
        }
    }

    /**
     * Returns the generated line and column information for the original source, line, and column positions provided.
     * The only argument is an object with the following properties:
     * <ul>
     * <li>source: The filename of the original source.</li>
     * <li>line: The line number in the original source.</li>
     * <li>column: The column number in the original source.</li>
     * </ul>
     * and an object is returned with the following properties:
     * <ul>
     * <li>line: The line number in the generated source, or null.</li>
     * <li>column: The column number in the generated source, or null.</li>
     * </ul>
     */
    @Override
    Position generatedPositionFor(String source, int line, int column, Bias bias) {
        for (int i = 0; i < this._sections.size(); i++) {
            ParsedSection section = this._sections.get(i);

            // Only consider this section if the requested source is in the list of
            // sources of the consumer.
            if (section.consumer.sources().indexOf(source) == -1) {
                continue;
            }
            Position generatedPosition = section.consumer.generatedPositionFor(source, line, column, bias);
            if (generatedPosition != null) {
                Position ret = new Position(generatedPosition.line + (section.generatedOffset.generatedLine - 1),
                        generatedPosition.column + (section.generatedOffset.generatedLine == generatedPosition.line
                                ? section.generatedOffset.generatedColumn - 1 : 0));
                return ret;
            }
        }

        return new Position();
    }

    /**
     * Parse the mappings in a string in to a data structure which we can easily query (the ordered arrays in the
     * `this.__generatedMappings` and `this.__originalMappings` properties).
     */
    @Override
    void _parseMappings(String aStr, String aSourceRoot) {
        this.__generatedMappings = new ArrayList<>();
        this.__originalMappings = new ArrayList<>();
        for (int i = 0; i < this._sections.size(); i++) {
            ParsedSection section = this._sections.get(i);
            List<Mapping> sectionMappings = section.consumer._generatedMappings();
            for (int j = 0; j < sectionMappings.size(); j++) {
                Mapping mapping = sectionMappings.get(i);

                String source = section.consumer._sources.at(mapping.source);
                if (section.consumer.sourceRoot != null) {
                    source = Util.join(section.consumer.sourceRoot, source);
                }
                this._sources.add(source, null);
                source = this._sources.indexOf(source);

                String name = section.consumer._names.at(mapping.name);
                this._names.add(name, null);
                name = this._names.indexOf(name);

                // The mappings coming from the consumer for the section have
                // generated positions relative to the start of the section, so we
                // need to offset them to be relative to the start of the concatenated
                // generated file.
                Mapping adjustedMapping = new Mapping(
                        mapping.generatedLine + (section.generatedOffset.generatedLine - 1),
                        mapping.column + (section.generatedOffset.generatedLine == mapping.generatedLine)
                                ? section.generatedOffset.generatedColumn - 1 : 0,
                        mapping.originalLine, mapping.originalColumn, source, name);

                this.__generatedMappings.add(adjustedMapping);
                if (adjustedMapping.originalLine != null) {
                    this.__originalMappings.add(adjustedMapping);
                }
            }
        }

        Collections.sort(this.__generatedMappings, new Comparator<Mapping>() {
            @Override
            public int compare(Mapping o1, Mapping o2) {
                return Util.compareByGeneratedPositionsDeflated(o1, o2, null);
            }
        });
        Collections.sort(this.__originalMappings, new Comparator<Mapping>() {
            @Override
            public int compare(Mapping o1, Mapping o2) {
                return Util.compareByOriginalPositions(o1, o2, null);
            }
        });
    }
}
