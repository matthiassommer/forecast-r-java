package forecasting.combinationStrategies.xcsf;

import forecasting.combinationStrategies.xcsf.classifier.Classifier;
import forecasting.combinationStrategies.xcsf.classifier.ConditionHyperellipsoid;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

/**
 * Since the methods of <code>Population</code> and <code>MatchSet</code> are
 * most frequently used in xcsf, the implementation is an even more efficient
 * version of a ArrayList (much code is stolen from <code>ArrayList</code>,
 * indeed).
 *
 * @author Patrick O. Stalph, Martin V. Butz
 */
abstract class ClassifierSet implements Iterable<Classifier>, Serializable {
    private Classifier[] elements;
    private int size;

    /**
     * Default constructor for subclasses only.
     */
    ClassifierSet() {
        this.elements = new Classifier[XCSFConstants.maxPopSize];
        this.size = 0;
    }

    /**
     * Returns an iterator over the classifiers in this set.
     *
     * @return The <code>Iterator</code> over the <code>Classifier</code>
     * objects of this set..
     */
    public Iterator<Classifier> iterator() {
        return new Itr();
    }

    /**
     * Returns the number of elements in this list.
     *
     * @return the number of elements in this list
     */
    public int size() {
        return this.size;
    }

    /**
     * Searches this set for a <code>Classifier</code> with an identical
     * <code>ConditionHyperEllipsoid</code>, i.e. condition1.equals(condition2).
     * If no such classifier is found, the method returns null.
     *
     * @param other the condition to search for.
     * @return null, if no identical condition is found.
     */
    Classifier findIdenticalCondition(ConditionHyperellipsoid other) {
        for (Classifier cl : this) {
            if (cl.getCondition().equals(other)) {
                return cl;
            }
        }
        return null; // nothing found
    }

    /**
     * Sorts this array of <code>Classifier</code> objects according to the
     * order induced by the specified comparator.
     *
     * @param c the comparator to determine the order of the array. A
     *          <tt>null</tt> value indicates that the elements'
     *          {@linkplain Comparable natural ordering} should be used.
     * @see Arrays#sort(Object[], int, int, Comparator)
     */
    void sort(Comparator<Classifier> c) {
        Arrays.sort(this.elements, 0, this.size, c);
    }

    /**
     * Removes all of the elements from this list. Actually the elements are not
     * removed, but the insertion index and numerositySum are reset to zero.
     */
    void clear() {
        this.size = 0;
        // no null asignment for GC
        // this is called frequently for matchsets, which are filled again.
    }

    /**
     * Appends the specified <code>Classifier</code> to the end of this list and
     * updates the numerositySum.
     *
     * @param classifier classifier to be appended to this list
     */
    void add(Classifier classifier) {
        if (this.size == this.elements.length - 1)
            throw new IndexOutOfBoundsException("Succeded maximum capacity: " + this.elements.length);
        this.elements[size++] = classifier;
    }

    /**
     * Returns the <code>Classifier</code> at the specified index.
     *
     * @param index Index of the <code>Classifier</code> to return.
     * @return <code>Classifier</code> at the specified index.
     */
    Classifier get(int index) {
        rangeCheck(index);
        return this.elements[index];
    }

    /**
     * Removes the element at the specified position in this list. Shifts any
     * subsequent elements to the left (subtracts one from their indices).
     * <p>
     * Note: The numerositySum is not updated. E.g. subsumption methods add the
     * numerosity of the removed classifier to another one in the list.
     *
     * @param index the index of the element to be removed
     * @throws IndexOutOfBoundsException if the <code>index</code> is not valid.
     */
    void remove(int index) {
        rangeCheck(index);
        // shift subsequent elements
        shiftRemove(index);
    }

    /**
     * Removes the elements at the specified positions in this list. Shifts any
     * subsequent elements to the left (subtracts one from their indices).
     * <p>
     * Note: The numerositySum is not updated. E.g. subsumption methods add the
     * numerosity of the removed classifier to another one in the list.
     *
     * @param indices the indices of the elements to be removed
     * @throws IndexOutOfBoundsException if any of the indices is not valid.
     */
    void remove(int[] indices) {
        Arrays.sort(indices);
        rangeCheck(indices[indices.length - 1]); // check largest index
        int i, numMoved;
        for (i = 0; i < indices.length - 1; i++) {
            // shift elements between indices
            numMoved = indices[i + 1] - indices[i] - 1;
            // shift by 1 + i
            System.arraycopy(this.elements, indices[i] + 1, this.elements, indices[i] - i, numMoved);
        }
        // last call: upper border = this.size
        numMoved = this.size - indices[i] - 1;
        System.arraycopy(this.elements, indices[i] + 1, this.elements, indices[i] - i, numMoved);
        this.size -= indices.length;
    }

    /**
     * Returns a shallow copy (object references) of the elements contained in
     * this set.
     *
     * @return a shallow copy of the <code>Classifier</code> array.
     */
    Classifier[] shallowCopy() {
        Classifier[] copy = new Classifier[size];
        System.arraycopy(this.elements, 0, copy, 0, size);
        return copy;
    }

    /**
     * Private remove method that skips bounds checking.
     *
     * @param index the index of the element to be removed
     */
    private void shiftRemove(int index) {
        int numMoved = this.size - index - 1;
        if (numMoved > 0) {
            System.arraycopy(this.elements, index + 1, this.elements, index, numMoved);
        }
        this.size--;
        // no null assignment for GC
    }

    /**
     * Throws <code>IndexOutOfBoundsException</code> if the index exceeds the
     * size. Elements at this index may exists, but may not be accessed.
     *
     * @param index the index to check
     */
    private void rangeCheck(int index) {
        if (index >= this.size)
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
    }

    /**
     * Implementation of the <code>Iterator</code> interface without check for
     * concurrent modification.
     *
     * @author Patrick Stalph
     */
    private class Itr implements Iterator<Classifier> {
        // Index of element to be returned by subsequent call to next.
        private int cursor = 0;
        // flag to indicate removal and prevent remove call before next call
        private boolean preventRemoval = true;

        public boolean hasNext() {
            return this.cursor != size();
        }

        public Classifier next() {
            this.preventRemoval = false;
            return ClassifierSet.this.get(this.cursor++);
        }

        public void remove() {
            if (this.preventRemoval) {
                return;
            }
            this.cursor--;
            this.preventRemoval = true;
            ClassifierSet.this.remove(this.cursor);
        }
    }
}
