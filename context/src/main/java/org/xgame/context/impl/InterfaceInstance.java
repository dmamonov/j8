package org.xgame.context.impl;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class InterfaceInstance {
    private final Class<?> base;
    private final Loader loader = new Loader();
    private static final AtomicLong uniq = new AtomicLong();

    public InterfaceInstance(final Class<?> base) throws IllegalArgumentException {
        for (final Method m : base.getMethods()) {
            if (!Modifier.isStatic(m.getModifiers()) && !m.isDefault()) {
                throw new IllegalArgumentException("Interface "+base.getName()+"\n contains non-static method " + m + "\n without default");
            }
        }
        this.base = base;
    }

    private class Loader extends ClassLoader {

        public Class findClass(final String name) {
            final byte[] definition = loadClassData(name);
            return defineClass(name, definition, 0, definition.length);
        }

        private byte[] loadClassData(final String name) {
            final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try {
                final ClassWriter writer = new ClassWriter(new ClassInfo(
                        name,
                        "java.lang.Object",
                        new String[] { InterfaceInstance.this.base.getName() }
                ));
                writer.writeTo(bytes);
                final byte[] definition = bytes.toByteArray();

                // DEBUG
                // java.nio.file.Files.write(java.nio.file.Paths.get(name + ".class"), definition);

                return definition;
            } catch (final Exception e) {
                throw new RuntimeException("Cannot load " + name, e);
            }
        }
    }

    public Class<?> defineClass() throws ClassNotFoundException {
        return loader.loadClass(base.getName() + "$Proxy$" + uniq.getAndIncrement());
    }
}

class ClassInfo {
    public String name;
    public String parent;
    public String[] interfaces;

    public ClassInfo(final String name, final String parent, final String[] interfaces) {
        this.name = name;
        this.parent = parent;
        this.interfaces = interfaces;
    }
}
class ClassWriter {
    ClassInfo clazz;

    private static final int CLASSFILE_MAJOR_VERSION = 49;
    private static final int CLASSFILE_MINOR_VERSION = 0;

    private static class ConstantPool {
        private static final int CONSTANT_UTF8              = 1;
        private static final int CONSTANT_UNICODE           = 2;
        private static final int CONSTANT_INTEGER           = 3;
        private static final int CONSTANT_FLOAT             = 4;
        private static final int CONSTANT_LONG              = 5;
        private static final int CONSTANT_DOUBLE            = 6;
        private static final int CONSTANT_CLASS             = 7;
        private static final int CONSTANT_STRING            = 8;
        private static final int CONSTANT_FIELD             = 9;
        private static final int CONSTANT_METHOD            = 10;
        private static final int CONSTANT_INTERFACE_METHOD = 11;
        private static final int CONSTANT_NAME_AND_TYPE = 12;

        private final List<Entry> pool = new ArrayList<>(32);
        private final Map<Object, Short> map = new HashMap<>(16);

        private static abstract class Entry {
            public abstract void writeTo(DataOutputStream out) throws IOException;
        }

        private static class ValueEntry extends Entry {
            private final Object value;

            public ValueEntry(final Object value) {
                this.value = value;
            }

            public void writeTo(final DataOutputStream out) throws IOException {
                if (value instanceof String) {
                    out.writeByte(CONSTANT_UTF8);
                    out.writeUTF((String) value);
                } else if (value instanceof Integer) {
                    out.writeByte(CONSTANT_INTEGER);
                    out.writeInt(((Integer) value).intValue());
                } else if (value instanceof Float) {
                    out.writeByte(CONSTANT_FLOAT);
                    out.writeFloat(((Float) value).floatValue());
                } else if (value instanceof Long) {
                    out.writeByte(CONSTANT_LONG);
                    out.writeLong(((Long) value).longValue());
                } else if (value instanceof Double) {
                    out.writeDouble(CONSTANT_DOUBLE);
                    out.writeDouble(((Double) value).doubleValue());
                } else {
                    throw new InternalError("bogus value entry: " + value);
                }
            }
        }

        private static class IndirectEntry extends Entry {
            private final int tag;
            private final short index0;
            private final short index1;

            public IndirectEntry(final int tag, final short index) {
                this.tag = tag;
                this.index0 = index;
                this.index1 = 0;
            }

            public IndirectEntry(final int tag, final short index0, final short index1) {
                this.tag = tag;
                this.index0 = index0;
                this.index1 = index1;
            }

            public void writeTo(final DataOutputStream out) throws IOException {
                out.writeByte(tag);
                out.writeShort(index0);

                if (
                        tag == CONSTANT_FIELD ||
                                tag == CONSTANT_METHOD ||
                                tag == CONSTANT_INTERFACE_METHOD ||
                                tag == CONSTANT_NAME_AND_TYPE
                        ) {
                    out.writeShort(index1);
                }
            }

            public int hashCode() {
                return tag + index0 + index1;
            }

            public boolean equals(final Object obj) {
                if (obj instanceof IndirectEntry) {
                    final IndirectEntry other = (IndirectEntry) obj;
                    if (tag == other.tag && index0 == other.index0 && index1 == other.index1) {
                        return true;
                    }
                }
                return false;
            }
        }

        private short addEntry(final Entry entry) {
            pool.add(entry);
            if (pool.size() >= 0xFFFF) {
                throw new IllegalArgumentException("Constant pool size limit exceeded");
            }
            return (short)pool.size();
        }

        private short get(final Object key, final java.util.function.Supplier<Entry> entry) {
            final Short index = map.get(key);
            if (index != null) {
                return index;
            } else {
                final short i = addEntry(entry.get());
                map.put(key, i);
                return i;
            }
        }

        private short getValue(final Object key) {
            return get(key, () -> new ValueEntry(key));
        }

        private short getIndirect(final IndirectEntry e) {
            return get(e, () -> e);
        }

        public short getUtf8(final String s) {
            if (s == null) {
                throw new NullPointerException();
            }
            return getValue(s);
        }

        public short getNameAndType(final String name, final String descriptor) {
            return getIndirect(new IndirectEntry(
                    CONSTANT_NAME_AND_TYPE,
                    getUtf8(name),
                    getUtf8(descriptor)
            ));
        }

        public short getClass(final String name) {
            return getIndirect(new IndirectEntry(
                    CONSTANT_CLASS,
                    getUtf8(name.replace('.', '/'))
            ));
        }

        public short getMethodRef(final String className, final String name, final String descriptor) {
            return getIndirect(new IndirectEntry(CONSTANT_METHOD,
                    getClass(className),
                    getNameAndType(name, descriptor)
            ));
        }

        public void writeTo(final DataOutputStream out) throws IOException {
            out.writeShort(pool.size() + 1);
            for (final Entry e : pool) {
                e.writeTo(out);
            }
        }
    }

    public ClassWriter(final ClassInfo clazz) {
        this.clazz = clazz;
    }

    public void writeTo(final OutputStream target) throws IOException {
        final ConstantPool cp = new ConstantPool();
        final DataOutputStream out = new DataOutputStream(target);

        // Reserve indexes in constant pool
        final short thisClass = cp.getClass(clazz.name);
        final short superClass = cp.getClass(clazz.parent);
        final short[] interfaces = new short[clazz.interfaces.length];
        for (final String intf : clazz.interfaces) {
            interfaces[interfaces.length - 1] = cp.getClass(intf);
        }

        // Default constructor
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        final DataOutputStream code = new DataOutputStream(bytes);
        cp.getUtf8("<init>");
        cp.getUtf8("()V");
        cp.getUtf8("Code");
        code.writeByte(42);                    // ALOAD_0
        code.writeByte(183);                   // INVOKESPECIAL
        code.writeShort(cp.getMethodRef(clazz.parent, "<init>", "()V"));
        code.writeByte(177);                   // RETURN

        // Header and constant pool
        out.writeInt(0xCAFEBABE);
        out.writeShort(CLASSFILE_MINOR_VERSION);
        out.writeShort(CLASSFILE_MAJOR_VERSION);
        cp.writeTo(out);

        // Declaration
        out.writeShort(0x00000001);
        out.writeShort(thisClass);
        out.writeShort(superClass);
        out.writeShort((short)interfaces.length);
        for (final short base : interfaces) {
            out.writeShort(base);
        }

        // Member counts: Fields, methods
        out.writeShort(0);
        out.writeShort(1);

        // Default constructor
        out.writeShort(0x00000001);
        out.writeShort(cp.getUtf8("<init>"));
        out.writeShort(cp.getUtf8("()V"));
        out.writeShort(1);                        // One attribute: "Code"
        out.writeShort(cp.getUtf8("Code"));
        out.writeInt(12 + bytes.size() + 8 * 0);  // Exception table length: 0
        out.writeShort(1);                        // Stack
        out.writeShort(1);                        // Locals
        out.writeInt(bytes.size());
        bytes.writeTo(out);
        out.writeShort(0);                        // Exception table
        out.writeShort(0);

        // ClassFile attributes
        out.writeShort(0);
    }
}