package org.hibnet.jsourcemap;

public class Needle {

    int source;
    int originalLine;
    int originalColumn;

    public Needle(int source, int originalLine, int originalColumn) {
        this.source = source;
        this.originalLine = originalLine;
        this.originalColumn = originalColumn;
    }

}
