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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ArraySet<T> {

    private Map<T, Integer> _set;
    private List<T> _array;

    /**
     * A data structure which is a combination of an array and a set. Adding a new member is O(1), testing for membership is O(1), and finding the
     * index of an element is O(1). Removing elements from the set is not supported. Only strings are supported for membership.
     */
    ArraySet() {
        _set = new HashMap<>();
        _array = new ArrayList<>();
    }

    /**
     * Static method for creating ArraySet instances from an existing array.
     */
    static <T> ArraySet<T> fromArray(List<T> aArray, Boolean aAllowDuplicates) {
        ArraySet<T> set = new ArraySet<>();
        for (int i = 0, len = aArray.size(); i < len; i++) {
            set.add(aArray.get(i), aAllowDuplicates);
        }
        return set;
    }

    /**
     * Return how many unique items are in this ArraySet. If duplicates have been added, than those do not count towards the size.
     *
     * @returns Number
     */
    int size() {
        return _array.size();
    }

    /**
     * Add the given string to this set.
     *
     * @param String
     *            aStr
     */
    void add(T t) {
        add(t, null);
    }

    void add(T t, Boolean aAllowDuplicates) {
        if (t == null) {
            return;
        }
        boolean isDuplicate = this.has(t);
        int idx = _array.size();
        if (!isDuplicate || (aAllowDuplicates != null && aAllowDuplicates)) {
            _array.add(t);
        }
        if (!isDuplicate) {
            _set.put(t, idx);
        }
    }

    /**
     * Is the given string a member of this set?
     *
     * @param String
     *            aStr
     */
    boolean has(T t) {
        if (t == null) {
            return false;
        }
        return _set.containsKey(t);
    }

    /**
     * What is the index of the given string in the array?
     *
     * @param String
     *            aStr
     */
    Integer indexOf(T t) {
        if (t == null) {
            return null;
        }
        Integer i = _set.get(t);
        if (i == null) {
            return -1;
        }
        return i;
    }

    /**
     * What is the element at the given index?
     *
     * @param Number
     *            aIdx
     */
    T at(Integer i) {
        if (i == null) {
            return null;
        }
        return _array.get(i);
    }

    /**
     * Returns the array representation of this set (which has the proper indices indicated by indexOf). Note that this is a copy of the internal
     * array used for storing the members so that no one can mess with internal state.
     */
    List<T> toArray() {
        return new ArrayList<>(_array);
    }
}
