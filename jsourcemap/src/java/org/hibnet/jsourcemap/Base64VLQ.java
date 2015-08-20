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

class Base64VLQ {

    // A single base 64 digit can contain 6 bits of data. For the base 64 variable
    // length quantities we use in the source map spec, the first bit is the sign,
    // the next four bits are the actual value, and the 6th bit is the
    // continuation bit. The continuation bit tells us whether there are more
    // digits in this value following this digit.
    //
    // Continuation
    // | Sign
    // | |
    // V V
    // 101011

    private static final int VLQ_BASE_SHIFT = 5;

    // binary: 100000
    private static final int VLQ_BASE = 1 << VLQ_BASE_SHIFT;

    // binary: 011111
    private static final int VLQ_BASE_MASK = VLQ_BASE - 1;

    // binary: 100000
    private static final int VLQ_CONTINUATION_BIT = VLQ_BASE;

    /**
     * Converts from a two-complement value to a value where the sign bit is placed in the least significant bit. For example, as decimals: 1 becomes
     * 2 (10 binary), -1 becomes 3 (11 binary) 2 becomes 4 (100 binary), -2 becomes 5 (101 binary)
     */
    private static int toVLQSigned(int aValue) {
        return aValue < 0 ? ((-aValue) << 1) + 1 : (aValue << 1) + 0;
    }

    /**
     * Converts to a two-complement value from a value where the sign bit is placed in the least significant bit. For example, as decimals: 2 (10
     * binary) becomes 1, 3 (11 binary) becomes -1 4 (100 binary) becomes 2, 5 (101 binary) becomes -2
     */
    private static int fromVLQSigned(int aValue) {
        boolean isNegative = (aValue & 1) == 1;
        int shifted = aValue >> 1;
        return isNegative ? -shifted : shifted;
    }

    /**
     * Returns the base 64 VLQ encoded value.
     */
    static final String encode(int aValue) {
        String encoded = "";
        int digit;

        int vlq = toVLQSigned(aValue);

        do {
            digit = vlq & VLQ_BASE_MASK;
            vlq >>>= VLQ_BASE_SHIFT;
            if (vlq > 0) {
                // There are still more digits in this value, so we must make sure the
                // continuation bit is marked.
                digit |= VLQ_CONTINUATION_BIT;
            }
            encoded += Base64.encode(digit);
        } while (vlq > 0);

        return encoded;
    }

    /**
     * Decodes the next base 64 VLQ value from the given string and returns the value and the rest of the string via the out parameter.
     * 
     * @return
     */
    static Base64VLQResult decode(String aStr, int aIndex) {
        int strLen = aStr.length();
        int result = 0;
        int shift = 0;
        boolean continuation;
        int digit;

        do {
            if (aIndex >= strLen) {
                throw new Error("Expected more digits in base 64 VLQ value.");
            }

            digit = Base64.decode(aStr.charAt(aIndex++));
            if (digit == -1) {
                throw new Error("Invalid base64 digit: " + aStr.charAt(aIndex - 1));
            }

            continuation = (digit & VLQ_CONTINUATION_BIT) != 0;
            digit &= VLQ_BASE_MASK;
            result = result + (digit << shift);
            shift += VLQ_BASE_SHIFT;
        } while (continuation);

        return new Base64VLQResult(fromVLQSigned(result), aIndex);
    }

    static final class Base64VLQResult {
        int value;
        int rest;

        Base64VLQResult(int value, int rest) {
            this.value = value;
            this.rest = rest;
        }
    }

}
