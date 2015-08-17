package org.hibnet.jsourcemap;

import java.util.List;

public class SourceMap {
    public SourceMap() {
        // TODO Auto-generated constructor stub
    }
    public SourceMap(String json) {
        // TODO Auto-generated constructor stub
    }
    int version;
    List<String> sources;
    List<String> names;
    String mappings;
    String file;
    String sourceRoot;
    List<String> sourcesContent;
    List<Section> sections;
}
