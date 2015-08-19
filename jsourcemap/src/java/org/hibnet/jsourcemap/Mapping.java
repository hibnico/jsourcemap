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

    public Mapping(int generatedLine, int generatedColumn, Integer originalLine, Integer originalColumn, String source, String name) {
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
