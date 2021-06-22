/*
 * Copyright 2015 Federico Vera
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.objecthunter.exp4j;

import java.math.BigDecimal;
import java.util.EmptyStackException;

/**
 * Simple BigDecimal stack using a BigDecimal array as data storage
 *
 * @author Federico Vera (dktcoding [at] gmail)
 */
class ArrayStack {

    private BigDecimal[] data;

    private int idx;

    ArrayStack() {
        this(5);
    }

    ArrayStack(final int initialCapacity) {
        if (0 >= initialCapacity) {
            throw new IllegalArgumentException(
                    "Stack's capacity must be positive");
        }

        data = new BigDecimal[initialCapacity];
        idx = -1;
    }

    void push(final BigDecimal value) {
        if (idx + 1 == data.length) {
            final var temp = new BigDecimal[(int) (data.length * 1.2) + 1];
            System.arraycopy(data, 0, temp, 0, data.length);
            data = temp;
        }

        data[++idx] = value;
    }

    void push(final double value) {
        push(BigDecimal.valueOf(value));
    }

    void push(final int value) {
        push(BigDecimal.valueOf(value));
    }

    void push(final long value) {
        push(BigDecimal.valueOf(value));
    }

    BigDecimal peek() {
        if (-1 == idx) {
            throw new EmptyStackException();
        }
        return data[idx];
    }

    BigDecimal pop() {
        if (-1 == idx) {
            throw new EmptyStackException();
        }
        return data[idx--];
    }

    boolean isEmpty() {
        return -1 == idx;
    }

    int size() {
        return idx + 1;
    }
}
