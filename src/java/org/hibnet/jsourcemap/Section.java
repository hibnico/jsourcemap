package org.hibnet.jsourcemap;

public class Section {

    public static class Offset {
        int line;
        int column;

        public Offset(int line, int column) {
            this.line = line;
            this.column = column;
        }
    }

    String url;

    Offset offset;

    SourceMap map;
}
