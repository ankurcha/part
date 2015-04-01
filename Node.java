package com.ankurdave.part;

abstract class Node {
    static final int MAX_PREFIX_LEN = 8;

    public Node() {
        refcount = 0;
    }

    public Node(final Node other) {
        refcount = 0;
    }

    public abstract  Node n_clone();
    public static  Node n_clone(Node n) {
        if (n == null) {
            return null;
        } else {
            return n.n_clone();
        }
    }

    public abstract Leaf minimum();
    public static  Leaf minimum(Node n) {
        if (n == null) {
            return null;
        } else {
            return n.minimum();
        }
    }

    public abstract void insert(ChildPtr ref, final byte[] key, Object value, int depth,
                       boolean force_clone);
    public static  void insert(Node n, ChildPtr ref, final byte[] key, Object value, int depth,
                              boolean force_clone) {
        // If we are at a NULL node, inject a leaf
        if (n == null) {
            ref.change(new Leaf(key, value));
        } else {
            n.insert(ref, key, value, depth, force_clone);
        }
    }

    public abstract void iter(IterCallback cb);
    public static  void iter(Node n, IterCallback cb) {
        if (n == null) {
            return;
        } else {
            n.iter(cb);
        }
    }

    public abstract int decrement_refcount();
    public static  int decrement_refcount(Node n) {
        if (n == null) {
            return 0;
        } else {
            return n.decrement_refcount();
        }
    }

    protected static int to_uint(byte b) {
        return ((int)b) & 0xFF;
    }

    int refcount;
}