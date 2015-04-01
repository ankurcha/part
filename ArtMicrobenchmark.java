package com.ankurdave.part;

import java.util.Random;

public class ArtMicrobenchmark {

    private static final int m_0 = 10000000;
    private static final int T =   1000000;
    private static final int key_len = 4;
    private static final int value_len = 4;
    private static final long max_duration = (long)5 * 1000 * 1000 * 1000;
    private static Random r = new Random();

    private static byte[] gen_key() {
        byte[] str = new byte[key_len];
        for (int j = 0; j < key_len; j++) {
            str[j] = (byte)r.nextInt(256);
        }
        return str;
    }

    private static byte[] gen_value() {
        byte[] value = new byte[value_len];
        for (int j = 0; j < value_len; j++) {
            value[j] = (byte)r.nextInt(256);
        }
        return value;
    }

    private static void increment_value(byte[] value) {
        for (int j = value_len - 1; j >= 0; j--) {
            if (value[j] < 127) {
                value[j]++;
                break;
            }
        }
    }

    private static class Test implements IterCallback {
        private int n = 0;
        private long sum = 0;
        @Override public void apply(final byte[] key, Object value) {
            n++;
            sum += key[0] + ((byte[])value)[0];
        }
    }

    public static void main(String[] args) {
        long sum = 0;

        ArtTree art = new ArtTree();
        String label = "jart";
        String label_clone = "jpart";

        for (int i = 0; i < m_0; i++) {
            art.insert(gen_key(), gen_value());
        }

        {
            long begin = System.nanoTime();
            for (int iter = 0; iter < T; iter++) {
                byte[] str = gen_key();
                byte[] result = (byte[])art.search(str);
                if (result != null) {
                    sum += result[0];
                }
            }
            long end = System.nanoTime();
            long ns = end - begin;
            double rate = T / ((double)ns / (1000 * 1000 * 1000));
            System.out.println("{'measurement': 'read', 'datastructure': '" + label
                               + "', 'y': " + rate + ", 'valsize': "
                               + value_len + "},");
            System.out.println(sum);
        }

        {
            int n = 0;
            Test t = new Test();
            long begin = System.nanoTime();
            art.iter(t);
            n = t.n;
            sum += t.sum;
            long end = System.nanoTime();
            long ns = end - begin;
            double rate = n / ((double)ns / (1000 * 1000 * 1000));
            System.out.println("{'measurement': 'scan', 'datastructure': '" + label
                               + "', 'y': " + rate + ", 'valsize': "
                               + value_len + "},");
        }

        {
            for (int m = 1; m <= m_0; m *= 10) {
                int insertions = 0, updates = 0;
                long ns = 0;
                long allocd = 0;
                int num_trials = 0;
                while (ns < max_duration) {
                    long begin = System.nanoTime();
                    ArtTree art2 = art.snapshot();
                    for (int i = 0; i < m; i++) {
                        byte[] str = gen_key();
                        byte[] result = (byte[])art2.search(str);
                        if (result == null) {
                            art2.insert(str, gen_value());
                            insertions++;
                        } else {
                            increment_value(result);
                            updates++;
                        }
                    }
                    ns += System.nanoTime() - begin;
                    allocd += art2.destroy();
                    num_trials++;
                }
                double rate = num_trials * m / ((double)ns / (1000 * 1000 * 1000));
                System.out.println("{'measurement': 'insert', 'datastructure': '" + label_clone
                                   + "', 'x': " + m + ", 'y': " + rate + ", 'valsize': "
                                   + value_len + ", 'inplace': False},");
                // << " (insert: " << insertions << ", update: " << updates
                // << ", alloc'd bytes per elem: " << (double)allocd / (num_trials * m)
                // << ")"
            }
        }
        {
            for (int m = 1; m <= m_0; m *= 10) {
                int insertions = 0, updates = 0;
                long ns = 0;
                int num_trials = 0;
                while (ns < max_duration) {
                    long begin = System.nanoTime();
                    for (int i = 0; i < m; i++) {
                        byte[] str = gen_key();
                        byte[] result = (byte[])art.search(str);
                        if (result == null) {
                            art.insert(str, gen_value());
                            insertions++;
                        } else {
                            increment_value(result);
                            updates++;
                        }
                    }
                    ns += System.nanoTime() - begin;
                    num_trials++;
                }
                double rate = num_trials * m / ((double)ns / (1000 * 1000 * 1000));
                System.out.println("{'measurement': 'insert', 'datastructure': '" + label
                                   + "', 'x': " + m + ", 'y': " + rate + ", 'valsize': "
                                   + value_len + ", 'inplace': True},");
            }
        }

//     {
//         int art_size = art.destroy();
//         std::cout << "art size " << art_size << std::endl;
//     }

        System.out.println(sum);
    }
}