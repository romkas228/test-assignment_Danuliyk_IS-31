/*
 * Copyright (c) 2014, NTUU KPI, Computer systems department and/or its affiliates. All rights reserved.
 * NTUU KPI PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 */

package ua.kpi.comsys.test2.implementation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import ua.kpi.comsys.test2.NumberList;

/**
 * Custom implementation of INumberList interface.
 * Represents a number in ternary (base-3) system using a linear doubly-linked list.
 * 
 * List type: Linear doubly-linked (C3 = 0)
 * Number system: Ternary/Base-3 (C5 = 1)
 * Additional system: Octal/Base-8 ((C5+1) mod 5 = 2)
 * Additional operation: OR (C7 = 6)
 *
 * @author Данулюк, ІС-31, залікова книжка № 4311
 */
public class NumberListImpl implements NumberList {

    /** Student's record book number: 4311 (satisfies C3=0, C5=1, C7=6) */
    private static final int RECORD_BOOK_NUMBER = 4311;
    
    /** Primary number system base (ternary) */
    private static final int PRIMARY_BASE = 3;
    
    /** Secondary number system base (octal) for changeScale() */
    private static final int SECONDARY_BASE = 8;

    /** Head of the doubly-linked list */
    private Node head;
    
    /** Tail of the doubly-linked list */
    private Node tail;
    
    /** Current size of the list */
    private int size;
    
    /** The base (radix) this list uses to represent the number */
    private int base;

    /**
     * Node class for doubly-linked list
     */
    private static class Node {
        Byte data;
        Node prev;
        Node next;
        
        Node(Byte data) {
            this.data = data;
        }
    }

    /**
     * Default constructor. Returns empty <tt>NumberListImpl</tt>
     */
    public NumberListImpl() {
        head = null;
        tail = null;
        size = 0;
        base = PRIMARY_BASE;
    }


    /**
     * Constructs new <tt>NumberListImpl</tt> by <b>decimal</b> number
     * from file, defined in string format.
     *
     * @param file - file where number is stored.
     */
    public NumberListImpl(File file) {
        this();
        if (file == null || !file.exists()) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            if (line != null && !line.isEmpty()) {
                initFromDecimalString(line);
            }
        } catch (IOException e) {
            // Leave list empty on error
        }
    }


    /**
     * Constructs new <tt>NumberListImpl</tt> by <b>decimal</b> number
     * in string notation.
     *
     * @param value - number in string notation.
     */
    public NumberListImpl(String value) {
        this();
        if (value != null && !value.isEmpty()) {
            initFromDecimalString(value);
        }
    }

    /**
     * Private constructor to create list with specific base
     */
    private NumberListImpl(BigInteger decimalValue, int targetBase) {
        this();
        this.base = targetBase;
        
        if (decimalValue.compareTo(BigInteger.ZERO) < 0) {
            return;
        }
        if (decimalValue.equals(BigInteger.ZERO)) {
            add((byte) 0);
            return;
        }
        
        BigInteger baseBI = BigInteger.valueOf(targetBase);
        StringBuilder digits = new StringBuilder();
        while (decimalValue.compareTo(BigInteger.ZERO) > 0) {
            BigInteger[] divRem = decimalValue.divideAndRemainder(baseBI);
            digits.append((char)('0' + divRem[1].intValue()));
            decimalValue = divRem[0];
        }
        
        // Add digits in reverse order (most significant first)
        for (int i = digits.length() - 1; i >= 0; i--) {
            int digit = digits.charAt(i) - '0';
            add((byte) digit);
        }
    }

    /**
     * Initializes list from a decimal string
     */
    private void initFromDecimalString(String value) {
        // Validate input - must be non-negative decimal number
        if (!value.matches("\\d+")) {
            return;
        }
        
        BigInteger decimal;
        try {
            decimal = new BigInteger(value);
        } catch (NumberFormatException e) {
            return;
        }
        
        if (decimal.compareTo(BigInteger.ZERO) < 0) {
            return;
        }
        
        if (decimal.equals(BigInteger.ZERO)) {
            add((byte) 0);
            return;
        }
        
        // Convert decimal to primary base (ternary)
        BigInteger baseBI = BigInteger.valueOf(PRIMARY_BASE);
        StringBuilder digits = new StringBuilder();
        while (decimal.compareTo(BigInteger.ZERO) > 0) {
            BigInteger[] divRem = decimal.divideAndRemainder(baseBI);
            digits.append((char)('0' + divRem[1].intValue()));
            decimal = divRem[0];
        }
        
        // Add digits in reverse order (most significant first)
        for (int i = digits.length() - 1; i >= 0; i--) {
            int digit = digits.charAt(i) - '0';
            add((byte) digit);
        }
    }


    /**
     * Saves the number, stored in the list, into specified file
     * in <b>decimal</b> scale of notation.
     *
     * @param file - file where number has to be stored.
     */
    public void saveList(File file) {
        if (file == null) {
            return;
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(toDecimalString());
        } catch (IOException e) {
            // Ignore errors
        }
    }


    /**
     * Returns student's record book number, which has 4 decimal digits.
     *
     * @return student's record book number.
     */
    public static int getRecordBookNumber() {
        return RECORD_BOOK_NUMBER;
    }


    /**
     * Returns new <tt>NumberListImpl</tt> which represents the same number
     * in octal (base-8) scale of notation.<p>
     *
     * Does not impact the original list.
     *
     * @return <tt>NumberListImpl</tt> in octal scale of notation.
     */
    public NumberListImpl changeScale() {
        if (isEmpty()) {
            NumberListImpl result = new NumberListImpl();
            result.base = SECONDARY_BASE;
            return result;
        }
        
        // Get decimal value first
        BigInteger decimal = getDecimalValue();
        
        // Create new list with secondary base (octal)
        return new NumberListImpl(decimal, SECONDARY_BASE);
    }

    /**
     * Helper method to get the decimal value of this list
     */
    private BigInteger getDecimalValue() {
        if (isEmpty()) {
            return BigInteger.ZERO;
        }
        
        BigInteger result = BigInteger.ZERO;
        BigInteger baseBI = BigInteger.valueOf(this.base);
        
        Node current = head;
        while (current != null) {
            result = result.multiply(baseBI).add(BigInteger.valueOf(current.data));
            current = current.next;
        }
        
        return result;
    }


    /**
     * Returns new <tt>NumberListImpl</tt> which represents the result of
     * bitwise OR operation on two numbers.<p>
     *
     * Does not impact the original list.
     *
     * @param arg - second argument of OR operation
     *
     * @return result of OR operation.
     */
    public NumberListImpl additionalOperation(NumberList arg) {
        if (arg == null) {
            return new NumberListImpl();
        }
        
        BigInteger val1 = this.getDecimalValue();
        BigInteger val2;
        
        if (arg instanceof NumberListImpl) {
            val2 = ((NumberListImpl) arg).getDecimalValue();
        } else {
            // Convert from List<Byte> to decimal using PRIMARY_BASE
            val2 = BigInteger.ZERO;
            BigInteger baseBI = BigInteger.valueOf(PRIMARY_BASE);
            for (Byte b : arg) {
                val2 = val2.multiply(baseBI).add(BigInteger.valueOf(b));
            }
        }
        
        // Perform OR operation
        BigInteger result = val1.or(val2);
        
        return new NumberListImpl(result, PRIMARY_BASE);
    }


    /**
     * Returns string representation of number, stored in the list
     * in <b>decimal</b> scale of notation.
     *
     * @return string representation in <b>decimal</b> scale.
     */
    public String toDecimalString() {
        if (isEmpty()) {
            return "0";
        }
        return getDecimalValue().toString();
    }


    @Override
    public String toString() {
        if (isEmpty()) {
            return "0";
        }
        
        StringBuilder sb = new StringBuilder();
        Node current = head;
        while (current != null) {
            int digit = current.data;
            if (digit < 10) {
                sb.append(digit);
            } else {
                // For hex digits (A-F)
                sb.append((char) ('A' + digit - 10));
            }
            current = current.next;
        }
        return sb.toString();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NumberListImpl)) return false;
        
        NumberListImpl other = (NumberListImpl) o;
        
        // Compare by decimal value, not by internal representation
        return this.getDecimalValue().equals(other.getDecimalValue());
    }

    @Override
    public int hashCode() {
        return getDecimalValue().hashCode();
    }


    @Override
    public int size() {
        return size;
    }


    @Override
    public boolean isEmpty() {
        return size == 0;
    }


    @Override
    public boolean contains(Object o) {
        if (!(o instanceof Byte)) return false;
        Node current = head;
        while (current != null) {
            if (current.data.equals(o)) {
                return true;
            }
            current = current.next;
        }
        return false;
    }


    @Override
    public Iterator<Byte> iterator() {
        return new Iterator<Byte>() {
            private Node current = head;
            
            @Override
            public boolean hasNext() {
                return current != null;
            }
            
            @Override
            public Byte next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                Byte data = current.data;
                current = current.next;
                return data;
            }
        };
    }


    @Override
    public Object[] toArray() {
        Object[] result = new Object[size];
        int i = 0;
        Node current = head;
        while (current != null) {
            result[i++] = current.data;
            current = current.next;
        }
        return result;
    }


    @Override
    public <T> T[] toArray(T[] a) {
        // Not implemented as per requirements
        return null;
    }


    @Override
    public boolean add(Byte e) {
        if (e == null) return false;
        
        Node newNode = new Node(e);
        if (tail == null) {
            head = tail = newNode;
        } else {
            newNode.prev = tail;
            tail.next = newNode;
            tail = newNode;
        }
        size++;
        return true;
    }


    @Override
    public boolean remove(Object o) {
        if (!(o instanceof Byte)) return false;
        
        Node current = head;
        while (current != null) {
            if (current.data.equals(o)) {
                removeNode(current);
                return true;
            }
            current = current.next;
        }
        return false;
    }

    /**
     * Helper method to remove a node
     */
    private void removeNode(Node node) {
        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            head = node.next;
        }
        
        if (node.next != null) {
            node.next.prev = node.prev;
        } else {
            tail = node.prev;
        }
        
        size--;
    }


    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object e : c) {
            if (!contains(e)) {
                return false;
            }
        }
        return true;
    }


    @Override
    public boolean addAll(Collection<? extends Byte> c) {
        boolean modified = false;
        for (Byte e : c) {
            if (add(e)) {
                modified = true;
            }
        }
        return modified;
    }


    @Override
    public boolean addAll(int index, Collection<? extends Byte> c) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        
        boolean modified = false;
        int i = index;
        for (Byte e : c) {
            add(i++, e);
            modified = true;
        }
        return modified;
    }


    @Override
    public boolean removeAll(Collection<?> c) {
        boolean modified = false;
        for (Object e : c) {
            while (remove(e)) {
                modified = true;
            }
        }
        return modified;
    }


    @Override
    public boolean retainAll(Collection<?> c) {
        boolean modified = false;
        Node current = head;
        while (current != null) {
            Node next = current.next;
            if (!c.contains(current.data)) {
                removeNode(current);
                modified = true;
            }
            current = next;
        }
        return modified;
    }


    @Override
    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }


    @Override
    public Byte get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        return getNode(index).data;
    }

    /**
     * Helper method to get node at index
     */
    private Node getNode(int index) {
        Node current;
        if (index < size / 2) {
            current = head;
            for (int i = 0; i < index; i++) {
                current = current.next;
            }
        } else {
            current = tail;
            for (int i = size - 1; i > index; i--) {
                current = current.prev;
            }
        }
        return current;
    }


    @Override
    public Byte set(int index, Byte element) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        if (element == null) {
            throw new NullPointerException("Null elements not permitted");
        }
        
        Node node = getNode(index);
        Byte oldValue = node.data;
        node.data = element;
        return oldValue;
    }


    @Override
    public void add(int index, Byte element) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        if (element == null) {
            throw new NullPointerException("Null elements not permitted");
        }
        
        if (index == size) {
            add(element);
            return;
        }
        
        Node newNode = new Node(element);
        if (index == 0) {
            newNode.next = head;
            if (head != null) {
                head.prev = newNode;
            }
            head = newNode;
            if (tail == null) {
                tail = newNode;
            }
        } else {
            Node current = getNode(index);
            newNode.prev = current.prev;
            newNode.next = current;
            current.prev.next = newNode;
            current.prev = newNode;
        }
        size++;
    }


    @Override
    public Byte remove(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        
        Node node = getNode(index);
        Byte data = node.data;
        removeNode(node);
        return data;
    }


    @Override
    public int indexOf(Object o) {
        if (!(o instanceof Byte)) return -1;
        
        int index = 0;
        Node current = head;
        while (current != null) {
            if (current.data.equals(o)) {
                return index;
            }
            current = current.next;
            index++;
        }
        return -1;
    }


    @Override
    public int lastIndexOf(Object o) {
        if (!(o instanceof Byte)) return -1;
        
        int index = size - 1;
        Node current = tail;
        while (current != null) {
            if (current.data.equals(o)) {
                return index;
            }
            current = current.prev;
            index--;
        }
        return -1;
    }


    @Override
    public ListIterator<Byte> listIterator() {
        return listIterator(0);
    }


    @Override
    public ListIterator<Byte> listIterator(int index) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        
        return new ListIterator<Byte>() {
            private Node current = index == size ? null : getNode(index);
            private Node lastReturned = null;
            private int cursor = index;
            
            @Override
            public boolean hasNext() {
                return cursor < size;
            }
            
            @Override
            public Byte next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                if (current == null) {
                    current = head;
                }
                lastReturned = current;
                Byte data = current.data;
                current = current.next;
                cursor++;
                return data;
            }
            
            @Override
            public boolean hasPrevious() {
                return cursor > 0;
            }
            
            @Override
            public Byte previous() {
                if (!hasPrevious()) {
                    throw new NoSuchElementException();
                }
                if (current == null) {
                    current = tail;
                } else {
                    current = current.prev;
                }
                lastReturned = current;
                cursor--;
                return current.data;
            }
            
            @Override
            public int nextIndex() {
                return cursor;
            }
            
            @Override
            public int previousIndex() {
                return cursor - 1;
            }
            
            @Override
            public void remove() {
                if (lastReturned == null) {
                    throw new IllegalStateException();
                }
                removeNode(lastReturned);
                if (lastReturned == current) {
                    current = current.next;
                } else {
                    cursor--;
                }
                lastReturned = null;
            }
            
            @Override
            public void set(Byte e) {
                if (lastReturned == null) {
                    throw new IllegalStateException();
                }
                lastReturned.data = e;
            }
            
            @Override
            public void add(Byte e) {
                if (current == null) {
                    NumberListImpl.this.add(e);
                } else {
                    NumberListImpl.this.add(cursor, e);
                }
                cursor++;
                lastReturned = null;
            }
        };
    }


    @Override
    public List<Byte> subList(int fromIndex, int toIndex) {
        if (fromIndex < 0 || toIndex > size || fromIndex > toIndex) {
            throw new IndexOutOfBoundsException();
        }
        
        NumberListImpl subList = new NumberListImpl();
        subList.base = this.base;
        Node current = getNode(fromIndex);
        for (int i = fromIndex; i < toIndex; i++) {
            subList.add(current.data);
            current = current.next;
        }
        return subList;
    }


    @Override
    public boolean swap(int index1, int index2) {
        if (index1 < 0 || index1 >= size || index2 < 0 || index2 >= size) {
            return false;
        }
        if (index1 == index2) {
            return true;
        }
        
        Node node1 = getNode(index1);
        Node node2 = getNode(index2);
        
        Byte temp = node1.data;
        node1.data = node2.data;
        node2.data = temp;
        
        return true;
    }


    @Override
    public void sortAscending() {
        if (size <= 1) return;
        
        // Simple bubble sort for linked list
        boolean swapped;
        do {
            swapped = false;
            Node current = head;
            while (current != null && current.next != null) {
                if (current.data > current.next.data) {
                    Byte temp = current.data;
                    current.data = current.next.data;
                    current.next.data = temp;
                    swapped = true;
                }
                current = current.next;
            }
        } while (swapped);
    }


    @Override
    public void sortDescending() {
        if (size <= 1) return;
        
        // Simple bubble sort for linked list
        boolean swapped;
        do {
            swapped = false;
            Node current = head;
            while (current != null && current.next != null) {
                if (current.data < current.next.data) {
                    Byte temp = current.data;
                    current.data = current.next.data;
                    current.next.data = temp;
                    swapped = true;
                }
                current = current.next;
            }
        } while (swapped);
    }


    @Override
    public void shiftLeft() {
        if (size <= 1) return;
        
        // Move first element to end
        Node first = head;
        head = head.next;
        head.prev = null;
        
        first.prev = tail;
        first.next = null;
        tail.next = first;
        tail = first;
    }


    @Override
    public void shiftRight() {
        if (size <= 1) return;
        
        // Move last element to beginning
        Node last = tail;
        tail = tail.prev;
        tail.next = null;
        
        last.next = head;
        last.prev = null;
        head.prev = last;
        head = last;
    }
}
