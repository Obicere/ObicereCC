package org.obicere.cc.methods.protocol;

import java.lang.reflect.Array;
import java.util.InputMismatchException;
import java.util.Objects;

/**
 * Dear Future Self,
 * <p>
 * You probably don't like me right now, but I had to do this. Chances are you are reading this
 * because the protocol finally broke. Well, it ain't getting fixed. Better start rewriting it now,
 * because there is nothing here salvageable.
 * <p>
 * Sincerely, Past Self.
 * <p>
 * P.S: While I have you, I'm also sorry I probably made you fat. Go hit the gym you loser.
 *
 * @author Obicere
 */
public class ByteConsumer {

    /**
     * The basic identifier for a <tt>boolean</tt>,
     *
     * @see ByteConsumer#identifierFor(Class)
     */
    private static final int IDENTIFIER_BOOLEAN = 0x01;

    /**
     * The basic identifier for a signed <tt>byte</tt>. Unless otherwise specified, the unsigned
     * type would be <tt>IDENTIFIER_BYTE + 1</tt>.
     *
     * @see ByteConsumer#identifierFor(Class)
     */
    private static final int IDENTIFIER_BYTE = 0x02;

    /**
     * The basic identifier for a signed <tt>short</tt>. Unless otherwise specified, the unsigned
     * type would be <tt>IDENTIFIER_SHORT + 1</tt>.
     *
     * @see ByteConsumer#identifierFor(Class)
     */
    private static final int IDENTIFIER_SHORT = 0x04;

    /**
     * The basic identifier for a UTF-16 <tt>char</tt>. All characters added to this protocol should
     * be in a UTF-16 format.
     *
     * @see ByteConsumer#identifierFor(Class)
     */
    private static final int IDENTIFIER_CHAR = 0x06;

    /**
     * The basic identifier for a signed <tt>int</tt>. Unless otherwise specified, the unsigned type
     * would be <tt>IDENTIFIER_INT + 1</tt>.
     *
     * @see ByteConsumer#identifierFor(Class)
     */
    private static final int IDENTIFIER_INT = 0x07;

    /**
     * The basic identifier for a signed <tt>long</tt>. Unless otherwise specified, the unsigned
     * type would be <tt>IDENTIFIER_LONG + 1</tt>.
     *
     * @see ByteConsumer#identifierFor(Class)
     */
    private static final int IDENTIFIER_LONG = 0x09;

    /**
     * The basic identifier for a signed <tt>float</tt>. Unless otherwise specified, the unsigned
     * type would be <tt>IDENTIFIER_FLOAT + 1</tt>.
     *
     * @see ByteConsumer#identifierFor(Class)
     */
    private static final int IDENTIFIER_FLOAT = 0x0B;

    /**
     * The basic identifier for a signed <tt>double</tt>. Unless otherwise specified, the unsigned
     * type would be <tt>IDENTIFIER_DOUBLE + 1</tt>.
     *
     * @see ByteConsumer#identifierFor(Class)
     */
    private static final int IDENTIFIER_DOUBLE = 0x0D;

    /**
     * The basic identifier for a UTF-16 <tt>String</tt>. All characters added to this protocol
     * should be in a UTF-16 format.
     *
     * @see ByteConsumer#identifierFor(Class)
     */
    private static final int IDENTIFIER_STRING = 0x0F;

    /**
     * The basic identifier for an array of a designed type. Revision 1.0 of the protocol dictates
     * that a generic array is impossible and that a one-dimensional array must have a defined
     * component type specified by an identifier.
     *
     * @see ByteConsumer#IDENTIFIER_BOOLEAN
     * @see ByteConsumer#IDENTIFIER_BYTE
     * @see ByteConsumer#IDENTIFIER_SHORT
     * @see ByteConsumer#IDENTIFIER_CHAR
     * @see ByteConsumer#IDENTIFIER_INT
     * @see ByteConsumer#IDENTIFIER_LONG
     * @see ByteConsumer#IDENTIFIER_FLOAT
     * @see ByteConsumer#IDENTIFIER_DOUBLE
     * @see ByteConsumer#identifierFor(Class)
     */
    private static final int IDENTIFIER_ARRAY = 0x10;

    /**
     * Used to define the buffer size of an identifier flag. Revision 1.0 of the protocol dictates
     * that the default identifier flag is an 8-bit integer, occupying 1 <tt>byte</tt>.
     */

    private static final int IDENTIFIER_SIZE = 1;

    /**
     * The default buffer size required to store an 8-bit value, or a <tt>boolean</tt>, in the
     * protocol. This size includes the identifier size.
     */
    private static final int BUFFER_SIZE_8_BIT = (1 << 0) + IDENTIFIER_SIZE;

    /**
     * The default buffer size required to store a 16-bit value, or a <tt>character</tt>, in the
     * protocol. This size includes the identifier size.
     */
    private static final int BUFFER_SIZE_16_BIT = (1 << 1) + IDENTIFIER_SIZE;

    /**
     * The default buffer size required to store a 32-bit value, length of an array, or a length
     * of a <tt>String</tt>, in the protocol. This size includes the identifier size.
     */
    private static final int BUFFER_SIZE_32_BIT = (1 << 2) + IDENTIFIER_SIZE;

    /**
     * The default buffer size required to store a 64-bit value in the protocol. This size includes
     * the identifier size.
     */
    private static final int BUFFER_SIZE_64_BIT = (1 << 3) + IDENTIFIER_SIZE;

    /**
     * The maximum size for the buffer. This values corresponds to the largest positive power of two
     * available in indexing.
     * <p>
     * The maximum size corresponds to: <tt>134,217,728 bytes (128MB)</tt>.
     */
    private static final int MAXIMUM_BUFFER_SIZE = 1 << 30;

    private static final float DEFAULT_GROWTH = 2.0f;

    private static final int DEFAULT_SIZE = 32;

    private int bufferSize;

    private int lastWriteIndex = 0;

    private int lastReadIndex = 0;


    private final float growth;

    private byte[] buffer;

    protected ByteConsumer() {
        this.growth = DEFAULT_GROWTH;
        this.buffer = new byte[DEFAULT_SIZE];
        this.bufferSize = DEFAULT_SIZE;
    }

    protected ByteConsumer(final int initialBuffer) {
        if (initialBuffer <= 0) {
            throw new IllegalArgumentException("Initial buffer size must be > 0; given size: " + initialBuffer);
        }
        this.growth = DEFAULT_GROWTH;
        this.buffer = new byte[initialBuffer];
        this.bufferSize = initialBuffer;
    }

    protected ByteConsumer(final int initialBuffer, final float growth) {
        if (initialBuffer <= 0) {
            throw new IllegalArgumentException("Initial buffer size must be > 0; given size: " + initialBuffer);
        }
        if (growth <= 1.0) {
            throw new IllegalArgumentException("Growth ratio must be greater than 1; given growth: " + growth);
        }
        this.growth = growth;
        this.buffer = new byte[initialBuffer];
        this.bufferSize = initialBuffer;
    }

    protected void expand() {
        int newLength = (int) (growth * bufferSize);
        if (newLength > MAXIMUM_BUFFER_SIZE) {
            newLength = MAXIMUM_BUFFER_SIZE;
        }
        final byte[] newBuffer = new byte[newLength];
        System.arraycopy(buffer, 0, newBuffer, 0, lastWriteIndex);
        this.buffer = newBuffer;
        this.bufferSize = newLength;
    }

    public synchronized boolean shouldClear() {
        return buffer.length == MAXIMUM_BUFFER_SIZE;
    }

    public synchronized void clearReadBuffer() {
        final int lastRead = lastReadIndex;
        final int lastWrite = lastWriteIndex;
        if (lastRead == 0) {
            // No content read yet.
            return;
        }
        final int newContent = lastWrite - lastRead;
        final byte[] newBuffer = new byte[newContent * 2]; // create a new buffer to avoid continuous clear
        System.arraycopy(buffer, lastRead, newBuffer, 0, newContent);
        this.lastReadIndex = 0; // Reset last read
        this.lastWriteIndex = newContent;
        this.buffer = newBuffer;
    }

    public synchronized void streamInput(final byte[] input) {
        Objects.requireNonNull(input);
        final int length = input.length;
        checkWriteSize(length);
        System.arraycopy(input, 0, buffer, lastWriteIndex, length);
        lastWriteIndex += length; // We just wrote n bytes where n=length
    }

    private void checkWriteSize(final int newWrite) {
        while (lastWriteIndex + newWrite > bufferSize) {
            expand();
        }
    }

    public byte[] toOutputArray() {
        final byte[] output = new byte[lastWriteIndex];
        System.arraycopy(buffer, 0, output, 0, lastWriteIndex);
        return output;
    }

    private void writeIdentifier(final int identifier) {
        checkWriteSize(1);
        try {
            buffer[lastWriteIndex++] = (byte) identifier;
        } catch (final ArrayIndexOutOfBoundsException e) {
            throw new OutOfMemoryError("Buffer is full.");
        }
    }

    private void writeRawByteValue(final int b) {
        try {
            checkWriteSize(1);
            buffer[lastWriteIndex++] = (byte) (b & 0xFF);
        } catch (final ArrayIndexOutOfBoundsException e) {
            throw new OutOfMemoryError("Buffer is full.");
        }
    }

    private void writeRawShortValue(final int s) {
        writeRawByteValue(s >> 8);
        writeRawByteValue(s >> 0);
    }

    private void writeRawIntValue(final int i) {
        writeRawShortValue(i >> 16);
        writeRawShortValue(i >> 0);
    }

    private void writeRawLongValue(final long l) {
        writeRawIntValue((int) (l >> 32)); // Damn 32-bit ALUs
        writeRawIntValue((int) (l >> 0));
    }

    private void writeRawStringValue(final String str) {
        final char[] data = str.toCharArray();
        writeRawIntValue(data.length);
        for (final char c : data) {
            writeRawShortValue(c);
        }
    }

    public synchronized void write(final boolean value) {
        writeIdentifier(IDENTIFIER_BOOLEAN);
        writeRawByteValue(value ? 1 : 0);
    }

    public synchronized void write(final byte value) {
        writeIdentifier(IDENTIFIER_BYTE);
        writeRawByteValue(value);
    }

    public synchronized void write(final short value) {
        writeIdentifier(IDENTIFIER_SHORT);
        writeRawShortValue(value);
    }

    public synchronized void write(final char value) {
        writeIdentifier(IDENTIFIER_CHAR);
        writeRawShortValue(value);
    }

    public synchronized void write(final int value) {
        writeIdentifier(IDENTIFIER_INT);
        writeRawIntValue(value);
    }

    public synchronized void write(long value) {
        writeIdentifier(IDENTIFIER_LONG);
        writeRawLongValue(value);
    }

    public synchronized void write(final float value) {
        final int write = Float.floatToIntBits(value);
        writeIdentifier(IDENTIFIER_FLOAT);
        writeRawIntValue(write);
    }

    public synchronized void write(final double value) {
        final long write = Double.doubleToLongBits(value);
        writeIdentifier(IDENTIFIER_DOUBLE);
        writeRawLongValue(write);
    }

    public synchronized void write(final String value) {
        Objects.requireNonNull(value, "Cannot write null string to buffer.");
        final int length = value.length();
        writeIdentifier(IDENTIFIER_STRING);
        writeRawStringValue(value);
    }

    public synchronized <T> void write(final T[] value) {
        Objects.requireNonNull(value);
        final int length = value.length;
        final Class<?> cls = value.getClass().getComponentType();
        final int identifier = identifierFor(cls);
        final int size = sizeFor(cls);
        if (identifier == -1 || size == -1) {
            throw new IllegalArgumentException("Non-supported class: " + cls);
        }
        writeIdentifier(IDENTIFIER_ARRAY);
        writeIdentifier(identifier);
        writeRawIntValue(length);
        for (final Object t : value) {
            writeFor(cls, t);
        }
    }

    public synchronized void write(final boolean[] value) {
        Objects.requireNonNull(value);
        writeIdentifier(IDENTIFIER_ARRAY);
        writeIdentifier(IDENTIFIER_BOOLEAN);
        writeRawIntValue(value.length);
        for (final boolean aValue : value) {
            writeRawByteValue(aValue ? 1 : 0);
        }
    }

    public synchronized void write(final byte[] value) {
        Objects.requireNonNull(value);
        writeIdentifier(IDENTIFIER_ARRAY);
        writeIdentifier(IDENTIFIER_BYTE);
        writeRawIntValue(value.length);
        for (final byte aValue : value) {
            writeRawByteValue(aValue);
        }
    }

    public synchronized void write(final char[] value) {
        Objects.requireNonNull(value);
        writeIdentifier(IDENTIFIER_ARRAY);
        writeIdentifier(IDENTIFIER_CHAR);
        writeRawIntValue(value.length);
        for (final char aValue : value) {
            writeRawShortValue(aValue);
        }
    }

    public synchronized void write(final short[] value) {
        Objects.requireNonNull(value);
        writeIdentifier(IDENTIFIER_ARRAY);
        writeIdentifier(IDENTIFIER_SHORT);
        writeRawIntValue(value.length);
        for (final short aValue : value) {
            writeRawShortValue(aValue);
        }
    }

    public synchronized void write(final int[] value) {
        Objects.requireNonNull(value);
        writeIdentifier(IDENTIFIER_ARRAY);
        writeIdentifier(IDENTIFIER_INT);
        writeRawIntValue(value.length);
        for (final int aValue : value) {
            writeRawIntValue(aValue);
        }
    }

    public synchronized void write(final long[] value) {
        Objects.requireNonNull(value);
        writeIdentifier(IDENTIFIER_ARRAY);
        writeIdentifier(IDENTIFIER_LONG);
        writeRawIntValue(value.length);
        for (final long aValue : value) {
            writeRawLongValue(aValue);
        }
    }

    public synchronized void write(final float[] value) {
        Objects.requireNonNull(value);
        writeIdentifier(IDENTIFIER_ARRAY);
        writeIdentifier(IDENTIFIER_FLOAT);
        writeRawIntValue(value.length);
        for (final float aValue : value) {
            writeRawIntValue(Float.floatToIntBits(aValue));
        }
    }

    public synchronized void write(final double[] value) {
        Objects.requireNonNull(value);
        writeIdentifier(IDENTIFIER_ARRAY);
        writeIdentifier(IDENTIFIER_DOUBLE);
        writeRawIntValue(value.length);
        for (final double aValue : value) {
            writeRawLongValue(Double.doubleToLongBits(aValue));
        }
    }

    private synchronized byte peekNext() {
        if (buffer.length <= lastReadIndex) {
            return -1; // Waiting on more input
        }
        return buffer[lastReadIndex];
    }

    private synchronized byte next() {
        try {
            return buffer[lastReadIndex++];
        } catch (final ArrayIndexOutOfBoundsException e) {
            throw new InvalidProtocolException("Next byte is missing. Index: " + lastReadIndex);
        }
    }

    private synchronized int nextIdentifier() {
        try {
            return buffer[lastReadIndex++];
        } catch (final ArrayIndexOutOfBoundsException e) {
            throw new InvalidProtocolException("Next identifier is missing. Index: " + lastReadIndex);
        }
    }

    public boolean hasNext() {
        return peekNext() != 0;
    }

    public boolean hasBoolean() {
        return peekNext() == IDENTIFIER_BOOLEAN;
    }

    public boolean hasByte() {
        return peekNext() == IDENTIFIER_BYTE;
    }

    public boolean hasShort() {
        return peekNext() == IDENTIFIER_SHORT;
    }

    public boolean hasChar() {
        return peekNext() == IDENTIFIER_CHAR;
    }

    public boolean hasInt() {
        return peekNext() == IDENTIFIER_INT;
    }

    public boolean hasFloat() {
        return peekNext() == IDENTIFIER_FLOAT;
    }

    public boolean hasLong() {
        return peekNext() == IDENTIFIER_LONG;
    }

    public boolean hasDouble() {
        return peekNext() == IDENTIFIER_DOUBLE;
    }

    public boolean hasString() {
        return peekNext() == IDENTIFIER_STRING;
    }

    public boolean hasArray() {
        return peekNext() == IDENTIFIER_ARRAY;
    }

    private byte readRawByte() {
        return next();
    }

    private short readRawShort() {
        return (short) (
                (0xFF & readRawByte()) << 8 |
                (0xFF & readRawByte()) << 0);
    }

    private int readRawInt() {
        return ((0xFFFF & readRawShort()) << 16 |
                (0xFFFF & readRawShort()) << 0);
    }

    private long readRawLong() {
        return ((0xFFFFFFFFL & readRawInt()) << 32 |
                (0xFFFFFFFFL & readRawInt()) << 0);
    }

    private String readRawString() {
        final int length = readRawInt();
        final char[] chars = new char[length];
        for (int i = 0; i < length; i++) {
            chars[i] = (char) readRawShort();
        }
        return new String(chars);
    }

    public synchronized boolean readBoolean() {
        if (nextIdentifier() != IDENTIFIER_BOOLEAN) {
            throw new InputMismatchException();
        }
        return readRawByte() != 0;
    }

    public synchronized byte readByte() {
        if (nextIdentifier() != IDENTIFIER_BYTE) {
            throw new InputMismatchException();
        }
        return readRawByte();
    }

    public synchronized short readShort() {
        if (nextIdentifier() != IDENTIFIER_SHORT) {
            throw new InputMismatchException();
        }
        return readRawShort();
    }

    public synchronized char readChar() {
        if (nextIdentifier() != IDENTIFIER_CHAR) {
            throw new InputMismatchException();
        }
        return (char) readRawShort();
    }

    public synchronized int readInt() {
        if (nextIdentifier() != IDENTIFIER_INT) {
            throw new InputMismatchException();
        }
        return readRawInt();
    }

    public synchronized long readLong() {
        if (nextIdentifier() != IDENTIFIER_LONG) {
            throw new InputMismatchException();
        }
        return readRawLong();
    }

    public synchronized float readFloat() {
        if (nextIdentifier() != IDENTIFIER_FLOAT) {
            throw new InputMismatchException();
        }
        return Float.intBitsToFloat(readRawInt());
    }

    public synchronized double readDouble() {
        if (nextIdentifier() != IDENTIFIER_DOUBLE) {
            throw new InputMismatchException();
        }
        return Double.longBitsToDouble(readRawLong());
    }

    public synchronized String readString() {
        if (nextIdentifier() != IDENTIFIER_STRING) {
            throw new InputMismatchException();
        }
        return readRawString();
    }

    @SuppressWarnings("unchecked")
    // This is all checked - not really though
    public synchronized <T, S> T readArray(final Class<T> cls) {
        if (nextIdentifier() != IDENTIFIER_ARRAY) {
            throw new InputMismatchException();
        }
        final int nextID = nextIdentifier();
        final int length = readRawInt();
        final Class<S> component = (Class<S>) cls.getComponentType();
        if (component.isPrimitive()) {
            switch (component.getCanonicalName()) {
                case "boolean":
                    return (T) readRawBooleanArray(length);
                case "byte":
                    return (T) readRawByteArray(length);
                case "short":
                    return (T) readRawShortArray(length);
                case "char":
                    return (T) readRawCharArray(length);
                case "int":
                    return (T) readRawIntArray(length);
                case "float":
                    return (T) readRawFloatArray(length);
                case "long":
                    return (T) readRawLongArray(length);
                case "double":
                    return (T) readRawDoubleArray(length);
            }
        }
        final S[] array = (S[]) Array.newInstance(component, length);
        for (int i = 0; i < length; i++) {
            array[i] = (S) readMethodFor(component, nextID);
        }
        return (T) array;
    }

    private boolean[] readRawBooleanArray(final int length) {
        final boolean[] array = new boolean[length];
        for (int i = 0; i < length; i++) {
            array[i] = readRawByte() != 0;
        }
        return array;
    }

    private byte[] readRawByteArray(final int length) {
        final byte[] array = new byte[length];
        for (int i = 0; i < length; i++) {
            array[i] = readRawByte();
        }
        return array;
    }

    private short[] readRawShortArray(final int length) {
        final short[] array = new short[length];
        for (int i = 0; i < length; i++) {
            array[i] = readRawShort();
        }
        return array;
    }

    private char[] readRawCharArray(final int length) {
        final char[] array = new char[length];
        for (int i = 0; i < length; i++) {
            array[i] = (char) readRawShort();
        }
        return array;
    }

    private int[] readRawIntArray(final int length) {
        final int[] array = new int[length];
        for (int i = 0; i < length; i++) {
            array[i] = readRawInt();
        }
        return array;
    }

    private float[] readRawFloatArray(final int length) {
        final float[] array = new float[length];
        for (int i = 0; i < length; i++) {
            array[i] = Float.intBitsToFloat(readRawInt());
        }
        return array;
    }

    private long[] readRawLongArray(final int length) {
        final long[] array = new long[length];
        for (int i = 0; i < length; i++) {
            array[i] = readRawLong();
        }
        return array;
    }

    private double[] readRawDoubleArray(final int length) {
        final double[] array = new double[length];
        for (int i = 0; i < length; i++) {
            array[i] = Double.longBitsToDouble(readRawLong());
        }
        return array;
    }

    public synchronized boolean[] readBooleanArray() {
        if (nextIdentifier() != IDENTIFIER_ARRAY || nextIdentifier() != IDENTIFIER_BOOLEAN) {
            throw new InputMismatchException();
        }
        final int length = readRawInt();
        return readRawBooleanArray(length);
    }

    public synchronized byte[] readByteArray() {
        if (nextIdentifier() != IDENTIFIER_ARRAY || nextIdentifier() != IDENTIFIER_BYTE) {
            throw new InputMismatchException();
        }
        final int length = readRawInt();
        return readRawByteArray(length);
    }

    public synchronized short[] readShortArray() {
        if (nextIdentifier() != IDENTIFIER_ARRAY || nextIdentifier() != IDENTIFIER_SHORT) {
            throw new InputMismatchException();
        }
        final int length = readRawInt();
        return readRawShortArray(length);
    }

    public synchronized char[] readCharArray() {
        if (nextIdentifier() != IDENTIFIER_ARRAY || nextIdentifier() != IDENTIFIER_CHAR) {
            throw new InputMismatchException();
        }
        final int length = readRawInt();
        return readRawCharArray(length);
    }

    public synchronized int[] readIntArray() {
        if (nextIdentifier() != IDENTIFIER_ARRAY || nextIdentifier() != IDENTIFIER_INT) {
            throw new InputMismatchException();
        }
        final int length = readRawInt();
        return readRawIntArray(length);
    }

    public synchronized float[] readFloatArray() {
        if (nextIdentifier() != IDENTIFIER_ARRAY || nextIdentifier() != IDENTIFIER_FLOAT) {
            throw new InputMismatchException();
        }
        final int length = readRawInt();
        return readRawFloatArray(length);
    }

    public synchronized long[] readLongArray() {
        if (nextIdentifier() != IDENTIFIER_ARRAY || nextIdentifier() != IDENTIFIER_LONG) {
            throw new InputMismatchException();
        }
        final int length = readRawInt();
        return readRawLongArray(length);
    }

    public synchronized double[] readDoubleArray() {
        if (nextIdentifier() != IDENTIFIER_ARRAY || nextIdentifier() != IDENTIFIER_DOUBLE) {
            throw new InputMismatchException();
        }
        final int length = readRawInt();
        return readRawDoubleArray(length);
    }

    private <T> Object readMethodFor(final Class<T> component, final int id) {
        switch (id) {
            case IDENTIFIER_BOOLEAN:
                return readRawByte() != 0;
            case IDENTIFIER_BYTE:
                return readRawByte();
            case IDENTIFIER_SHORT:
                return readRawShort();
            case IDENTIFIER_CHAR:
                return readRawShort();
            case IDENTIFIER_INT:
                return readRawInt();
            case IDENTIFIER_FLOAT:
                return Float.intBitsToFloat(readRawInt());
            case IDENTIFIER_LONG:
                return readRawLong();
            case IDENTIFIER_DOUBLE:
                return Double.longBitsToDouble(readRawLong());
            case IDENTIFIER_STRING:
                return readRawString();
            case IDENTIFIER_ARRAY:
                return readArray(component);
        }
        return null;
    }

    private void writeFor(final Class<?> cls, final Object obj) {
        Objects.requireNonNull(cls);
        Objects.requireNonNull(obj);
        if (cls.isArray()) {
            final Class<?> component = cls.getComponentType();
            if (component.isPrimitive()) {
                switch (component.getCanonicalName()) {
                    case "boolean":
                        write((boolean[]) obj);
                        return;
                    case "byte":
                        write((byte[]) obj);
                        return;
                    case "short":
                        write((short[]) obj);
                        return;
                    case "char":
                        write((char[]) obj);
                        return;
                    case "int":
                        write((int[]) obj);
                        return;
                    case "float":
                        write((float[]) obj);
                        return;
                    case "long":
                        write((long[]) obj);
                        return;
                    case "double":
                        write((double[]) obj);
                        return;
                }
            } else {
                write((Object[]) obj);
            }
            return;
        }
        switch (cls.getCanonicalName()) {
            case "boolean":
            case "java.lang.Boolean":
                final int value = ((boolean) obj) ? 1 : 0;
                writeRawByteValue(value);
                return;

            case "byte":
            case "java.lang.Byte":
                writeRawByteValue((byte) obj);
                return;

            case "short":
            case "char":
            case "java.lang.Short":
            case "java.lang.Character":
                writeRawShortValue((byte) obj);
                return;

            case "int":
            case "java.lang.Integer":
                writeRawIntValue((int) obj);
                return;

            case "long":
            case "java.lang.Long":
                writeRawLongValue((long) obj);
                return;

            case "float":
            case "java.lang.Float":
                final int intValue = Float.floatToIntBits((float) obj);
                writeRawIntValue(intValue);
                return;

            case "double":
            case "java.lang.Double":
                final long longValue = Double.doubleToLongBits((double) obj);
                writeRawLongValue(longValue);
                return;

            case "java.lang.String":
                writeRawStringValue((String) obj);
                return;
            default:
                throw new AssertionError("Could not write object for class: " + cls);
        }
    }

    private int identifierFor(final Class<?> cls) {
        Objects.requireNonNull(cls);
        if (cls.isArray()) {
            return IDENTIFIER_ARRAY;
        }
        switch (cls.getCanonicalName()) {
            case "boolean":
            case "java.lang.Boolean":
                return IDENTIFIER_BOOLEAN;

            case "byte":
            case "java.lang.Byte":
                return IDENTIFIER_BYTE;

            case "short":
            case "java.lang.Short":
                return IDENTIFIER_SHORT;

            case "char":
            case "java.lang.Character":
                return IDENTIFIER_CHAR;

            case "int":
            case "java.lang.Integer":
                return IDENTIFIER_INT;

            case "long":
            case "java.lang.Long":
                return IDENTIFIER_LONG;

            case "float":
            case "java.lang.Float":
                return IDENTIFIER_FLOAT;

            case "double":
            case "java.lang.Double":
                return IDENTIFIER_DOUBLE;

            case "java.lang.String":
                return IDENTIFIER_STRING;
            default:
                return -1; // Unsupported type
        }
    }

    // This will only return header size. Additional memory may need to be allocated
    private int sizeFor(final Class<?> cls) {
        Objects.requireNonNull(cls);
        if (cls.isArray()) {
            return BUFFER_SIZE_32_BIT + 1; // Allocate length (32-bit int) and array type (8-bit int)
        }
        switch (cls.getCanonicalName()) {
            case "boolean":
            case "byte":
            case "java.lang.Boolean": // Sorry, no optimizations yet
            case "java.lang.Byte":
                return BUFFER_SIZE_8_BIT;

            case "short":
            case "char":
            case "java.lang.Short":
            case "java.lang.Character":
                return BUFFER_SIZE_16_BIT;

            case "int":
            case "float":
            case "java.lang.String": // Technically an array - record the length
            case "java.lang.Integer":
            case "java.lang.Float":
                return BUFFER_SIZE_32_BIT;

            case "long":
            case "double":
            case "java.lang.Long":
            case "java.lang.Double":
                return BUFFER_SIZE_64_BIT;
            default:
                return -1; // Unsupported type
        }
    }

}