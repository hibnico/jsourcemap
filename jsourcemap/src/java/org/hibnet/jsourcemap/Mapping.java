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

    Position generated;
    Position original;
    String source;
    String name;

    public Mapping(Position generated) {
        this.generated = generated;
    }

    public Mapping(Position generated, Position original, String source) {
        this.generated = generated;
        this.original = original;
        this.source = source;
    }

    public Mapping(Position generated, Position original, String source, String name) {
        this.generated = generated;
        this.original = original;
        this.source = source;
        this.name = name;
    }

    public Position getGenerated() {
        return generated;
    }

    public Position getOriginal() {
        return original;
    }

    public String getSource() {
        return source;
    }

    public String getName() {
        return name;
    }
}
