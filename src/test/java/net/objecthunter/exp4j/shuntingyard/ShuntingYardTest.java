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
package net.objecthunter.exp4j.shuntingyard;

import net.objecthunter.exp4j.operator.Operator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static net.objecthunter.exp4j.TestUtil.*;

class ShuntingYardTest {

    @Test
    void testShuntingYard1() {
        final var expression = "2+3";
        final var tokens = ShuntingYard.convertToRPN(expression, null, null, null, true);
        assertNumberToken(tokens[0], 2d);
        assertNumberToken(tokens[1], 3d);
        assertOperatorToken(tokens[2], "+", 2, Operator.PRECEDENCE_ADDITION);
    }

    @Test
    void testShuntingYard2() {
        final var expression = "3*x";
        final var tokens = ShuntingYard.convertToRPN(expression, null, null, new HashSet<>(Collections.singletonList("x")), true);
        assertNumberToken(tokens[0], 3d);
        assertVariableToken(tokens[1], "x");
        assertOperatorToken(tokens[2], "*", 2, Operator.PRECEDENCE_MULTIPLICATION);
    }
 
    @Test
    void testShuntingYard3() {
        final var expression = "-3";
        final var tokens = ShuntingYard.convertToRPN(expression, null, null, null, true);
        assertNumberToken(tokens[0], 3d);
        assertOperatorToken(tokens[1], "-", 1, Operator.PRECEDENCE_UNARY_MINUS);
    }

    @Test
    void testShuntingYard4() {
        final var expression = "-2^2";
        final var tokens = ShuntingYard.convertToRPN(expression, null, null, null, true);
        assertNumberToken(tokens[0], 2d);
        assertNumberToken(tokens[1], 2d);
        assertOperatorToken(tokens[2], "^", 2, Operator.PRECEDENCE_POWER);
        assertOperatorToken(tokens[3], "-", 1, Operator.PRECEDENCE_UNARY_MINUS);
    }

    @Test
    void testShuntingYard5() {
        final var expression = "2^-2";
        final var tokens = ShuntingYard.convertToRPN(expression, null, null, null, true);
        assertNumberToken(tokens[0], 2d);
        assertNumberToken(tokens[1], 2d);
        assertOperatorToken(tokens[2], "-", 1, Operator.PRECEDENCE_UNARY_MINUS);
        assertOperatorToken(tokens[3], "^", 2, Operator.PRECEDENCE_POWER);
    }

    @Test
    void testShuntingYard6() {
        final var expression = "2^---+2";
        final var tokens = ShuntingYard.convertToRPN(expression, null, null, null, true);
        assertNumberToken(tokens[0], 2d);
        assertNumberToken(tokens[1], 2d);
        assertOperatorToken(tokens[2], "+", 1, Operator.PRECEDENCE_UNARY_PLUS);
        assertOperatorToken(tokens[3], "-", 1, Operator.PRECEDENCE_UNARY_MINUS);
        assertOperatorToken(tokens[4], "-", 1, Operator.PRECEDENCE_UNARY_MINUS);
        assertOperatorToken(tokens[5], "-", 1, Operator.PRECEDENCE_UNARY_MINUS);
        assertOperatorToken(tokens[6], "^", 2, Operator.PRECEDENCE_POWER);
    }

    @Test
    void testShuntingYard7() {
        final var expression = "2^-2!";
        final var factorial = new Operator("!", 1, true, Operator.PRECEDENCE_POWER + 1) {

            @Override
            public BigDecimal apply(final BigDecimal... args) {
                final var arg = args[0].intValue();
/*
        if ((double) arg != args[0]) {
          throw new IllegalArgumentException("Operand for factorial has to be an integer");
        }
        if (arg < 0) {
          throw new IllegalArgumentException("The operand of the factorial can not be less than zero");
        }
 */
                var result = BigDecimal.ONE;
                for (var i = 1; i <= arg; i++) {
                    result = result.multiply(BigDecimal.valueOf(i));
                }
                return result;
            }
        };
        final Map<String, Operator> userOperators = new HashMap<>();
        userOperators.put("!", factorial);
        final var tokens = ShuntingYard.convertToRPN(expression, null, userOperators, null, true);
        assertNumberToken(tokens[0], 2d);
        assertNumberToken(tokens[1], 2d);
        assertOperatorToken(tokens[2], "!", 1, Operator.PRECEDENCE_POWER + 1);
        assertOperatorToken(tokens[3], "-", 1, Operator.PRECEDENCE_UNARY_MINUS);
        assertOperatorToken(tokens[4], "^", 2, Operator.PRECEDENCE_POWER);
    }

    @Test
    void testShuntingYard8() {
        final var expression = "-3^2";
        final var tokens = ShuntingYard.convertToRPN(expression, null, null, null, true);
        assertNumberToken(tokens[0], 3d);
        assertNumberToken(tokens[1], 2d);
        assertOperatorToken(tokens[2], "^", 2, Operator.PRECEDENCE_POWER);
        assertOperatorToken(tokens[3], "-", 1, Operator.PRECEDENCE_UNARY_MINUS);
    }

    @Test
    void testShuntingYard9() {
        final var reciprocal = new Operator("$", 1, true, Operator.PRECEDENCE_DIVISION) {
            @Override
            public BigDecimal apply(final BigDecimal... args) {
                if (args[0].signum() == 0) {
                    throw new ArithmeticException("Division by zero!");
                }
                return BigDecimal.valueOf(1d / args[0].doubleValue());
            }
        };
        final Map<String, Operator> userOperators = new HashMap<>();
        userOperators.put("$", reciprocal);
        final var tokens = ShuntingYard.convertToRPN("1$", null, userOperators, null, true);
        assertNumberToken(tokens[0], 1d);
        assertOperatorToken(tokens[1], "$", 1, Operator.PRECEDENCE_DIVISION);
    }

}
