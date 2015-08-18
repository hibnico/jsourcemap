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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

public class Base64Test {

    @Test
    public void testOutOfRangeEncoding() throws Exception {
        try {
            Base64.encode(-1);
            fail("expecting an error");
        } catch (RuntimeException r) {
            // OK
        }
        try {
            Base64.encode(64);
            fail("expecting an error");
        } catch (RuntimeException r) {
            // OK
        }
    }

    @Test
    public void testOutOfRangeDecoding() throws Exception {
        assertEquals(Base64.decode('='), -1);
    }

    @Test
    public void testNormalEncodingAndDecoding() throws Exception {
        for (int i = 0; i < 64; i++) {
            assertEquals(Base64.decode(Base64.encode(i)), i);
        }
    }
}
