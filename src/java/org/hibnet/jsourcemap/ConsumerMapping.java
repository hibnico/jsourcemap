package org.hibnet.jsourcemap;

public class ConsumerMapping {

    public Integer generatedLine;

    public Integer generatedColumn;

    public Integer originalLine;

    public Integer originalColumn;

    public Integer source;

    public Integer name;

    public Integer lastGeneratedColumn;

    public ConsumerMapping() {
        // TODO Auto-generated constructor stub
    }

    public ConsumerMapping(int generatedLine, int generatedColumn) {
        this.generatedLine = generatedLine;
        this.generatedColumn = generatedColumn;
    }

    public ConsumerMapping(int generatedLine, int generatedColumn, int originalLine, int originalColumn,
            Integer source) {
        this.generatedLine = generatedLine;
        this.generatedColumn = generatedColumn;
        this.originalLine = originalLine;
        this.originalColumn = originalColumn;
        this.source = source;
    }

    public ConsumerMapping(int generatedLine, int generatedColumn, Integer originalLine, Integer originalColumn,
            Integer source,
            Integer name) {
        this.generatedLine = generatedLine;
        this.generatedColumn = generatedColumn;
        this.originalLine = originalLine;
        this.originalColumn = originalColumn;
        this.source = source;
        this.name = name;
    }

    public ConsumerMapping(int generatedLine, int generatedColumn, int lastGeneratedColumn) {
        this.generatedLine = generatedLine;
        this.generatedColumn = generatedColumn;
        this.lastGeneratedColumn = lastGeneratedColumn;
    }

}
