package org.hibnet.jsourcemap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class MappingList {

    private List<Mapping> _array;
    private boolean _sorted;
    private Mapping _last;

    /**
     * Determine whether mappingB is after mappingA with respect to generated position.
     */
    private boolean generatedPositionAfter(Mapping mappingA, Mapping mappingB) {
        // Optimized for most common case
        int lineA = mappingA.generatedLine;
        int lineB = mappingB.generatedLine;
        int columnA = mappingA.generatedColumn;
        int columnB = mappingB.generatedColumn;
        return lineB > lineA || lineB == lineA && columnB >= columnA
                || Util.compareByGeneratedPositionsInflated(mappingA, mappingB) <= 0;
    }

    /**
     * A data structure to provide a sorted view of accumulated mappings in a performance conscious manner. It trades a
     * neglibable overhead in general case for a large speedup in case of mappings being added in order.
     */
    public MappingList() {
        this._array = new ArrayList<>();
        this._sorted = true;
        // Serves as infimum
        this._last = new Mapping(-1, 0);
    }

    /**
     * Iterate through internal items. This method takes the same arguments that `Array.prototype.forEach` takes.
     *
     * NOTE: The order of the mappings is NOT guaranteed.
     * 
     * @return
     */
    Stream<Mapping> unsortedForEach() {
        return this._array.stream();
    }

    /**
     * Add the given source mapping.
     *
     * @param Object
     *            aMapping
     */
    void add(Mapping aMapping) {
        if (generatedPositionAfter(this._last, aMapping)) {
            this._last = aMapping;
            this._array.add(aMapping);
        } else {
            this._sorted = false;
            this._array.add(aMapping);
        }
    };

    /**
     * Returns the flat, sorted array of mappings. The mappings are sorted by generated position.
     *
     * WARNING: This method returns internal data without copying, for performance. The return value must NOT be
     * mutated, and should be treated as an immutable borrow. If you want to take ownership, you must make your own
     * copy.
     * 
     * @return
     */
    List<Mapping> toArray() {
        if (!this._sorted) {
            Collections.sort(this._array, new Comparator<Mapping>() {
                @Override
                public int compare(Mapping o1, Mapping o2) {
                    return Util.compareByGeneratedPositionsInflated(o1, o2);
                }
            });
            this._sorted = true;
        }
        return this._array;
    };

}
