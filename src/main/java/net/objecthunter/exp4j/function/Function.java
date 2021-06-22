/*
 * Copyright 2014 Frank Asseg
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

package net.objecthunter.exp4j.function;

import java.math.BigDecimal;

/**
 * A class representing a Function which can be used in an expression
 */
public abstract class Function {

    private final String name;

    protected final int numArguments;

    /**
     * Create a new Function with a given name and number of arguments
     *
     * @param name         the name of the Function
     * @param numArguments the number of arguments the function takes
     */
    public Function(final String name, final int numArguments) {
        if (0 > numArguments) {
            throw new IllegalArgumentException("The number of function arguments can not be less than 0 for '" +
                    name + "'");
        }
        if (!isValidFunctionName(name)) {
            throw new IllegalArgumentException("The function name '" + name + "' is invalid");
        }
        this.name = name;
        this.numArguments = numArguments;

    }

    /**
     * Create a new Function with a given name that takes a single argument
     *
     * @param name the name of the Function
     */
    public Function(final String name) {
        this(name, 1);
    }

    /**
     * Get the name of the Function
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the number of arguments for this function
     *
     * @return the number of arguments
     */
    public int getNumArguments() {
        return numArguments;
    }

    /**
     * Method that does the actual calculation of the function value given the arguments
     *
     * @param args the set of arguments used for calculating the function
     * @return the result of the function evaluation
     */
    public abstract BigDecimal apply(BigDecimal... args);

    /**
     * Get the set of characters which are allowed for use in Function names.
     *
     * @return the set of characters allowed
     * @deprecated since 0.4.5 All unicode letters are allowed to be used in function names since 0.4.3. This API Function can be safely
     * ignored. Checks for function name validity can be done using Character.isLetter() et al.
     */
    public static char[] getAllowedFunctionCharacters() {
        final var chars = new char[53];
        var count = 0;
        for (var i = 65; 91 > i; i++) {
            chars[count++] = (char) i;
        }
        for (var i = 97; 123 > i; i++) {
            chars[count++] = (char) i;
        }
        chars[count] = '_';
        return chars;
    }

    public static boolean isValidFunctionName(final String name) {
        if (null == name) {
            return false;
        }

        final var size = name.length();

        if (0 == size) {
            return false;
        }

        for (var i = 0; i < size; i++) {
            final var c = name.charAt(i);
            if (Character.isLetter(c) || '_' == c) {
                continue;
            } else if (Character.isDigit(c) && 0 < i) {
                continue;
            }
            return false;
        }
        return true;
    }
}
