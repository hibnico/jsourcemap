package org.hibnet.jsourcemap;

public class Mapping {

    public Integer generatedLine;

    public Integer generatedColumn;

    public Integer originalLine;

    public Integer originalColumn;

    public String source;

    public String name;

    public Integer lastGeneratedColumn;

    public Mapping() {
        // TODO Auto-generated constructor stub
    }
    public Mapping(int generatedLine, int generatedColumn) {
        this.generatedLine = generatedLine;
        this.generatedColumn = generatedColumn;
    }

    public Mapping(int generatedLine, int generatedColumn, int originalLine, int originalColumn, String source) {
        this.generatedLine = generatedLine;
        this.generatedColumn = generatedColumn;
        this.originalLine = originalLine;
        this.originalColumn = originalColumn;
        this.source = source;
    }

    public Mapping(int generatedLine, int generatedColumn, int originalLine, int originalColumn, String source,
            String name) {
        this.generatedLine = generatedLine;
        this.generatedColumn = generatedColumn;
        this.originalLine = originalLine;
        this.originalColumn = originalColumn;
        this.source = source;
        this.name = name;
    }

    public Mapping(int generatedLine, int generatedColumn, int lastGeneratedColumn) {
        this.generatedLine = generatedLine;
        this.generatedColumn = generatedColumn;
        this.lastGeneratedColumn = lastGeneratedColumn;
    }

}
