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
package net.objecthunter.exp4j;

import net.objecthunter.exp4j.function.Functions;
import net.objecthunter.exp4j.operator.Operator;
import net.objecthunter.exp4j.operator.Operators;
import net.objecthunter.exp4j.tokenizer.*;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;


public class ExpressionTest {

    @Test
    public void testExpression1() {
        final var tokens = new Token[]{
                new NumberToken(3d),
                new NumberToken(2d),
                new OperatorToken(Operators.getBuiltinOperator('+', 2))
        };
        final var exp = new Expression(tokens);
        assertEquals(5d, exp.evaluate().doubleValue(), 0d);
    }

    @Test
    public void testExpression2() {
        final var tokens = new Token[]{
                new NumberToken(1d),
                new FunctionToken(Functions.getBuiltinFunction("log")),
        };
        final var exp = new Expression(tokens);
        assertEquals(0d, exp.evaluate().doubleValue(), 0d);
    }

    @Test
    public void testGetVariableNames1() {
        final var tokens = new Token[]{
                new VariableToken("a"),
                new VariableToken("b"),
                new OperatorToken(Operators.getBuiltinOperator('+', 2))
        };
        final var exp = new Expression(tokens);

        assertEquals(2, exp.getVariableNames().size());
    }

    @Test
    public void testFactorial() {
        final var factorial = new Operator("!", 1, true, Operator.PRECEDENCE_POWER + 1) {

            @Override
            public BigDecimal apply(final BigDecimal... args) {
                final var arg = args[0].intValue();
/*        if ((double) arg != args[0]) {
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

        var e = new ExpressionBuilder("2!+3!")
                .operator(factorial)
                .build();
        assertEquals(8d, e.evaluate().doubleValue(), 0d);

        e = new ExpressionBuilder("3!-2!")
                .operator(factorial)
                .build();
        assertEquals(4d, e.evaluate().doubleValue(), 0d);

        e = new ExpressionBuilder("3!")
                .operator(factorial)
                .build();
        assertEquals(6, e.evaluate().doubleValue(), 0);

        e = new ExpressionBuilder("3!!")
                .operator(factorial)
                .build();
        assertEquals(720, e.evaluate().doubleValue(), 0);

        e = new ExpressionBuilder("4 + 3!")
                .operator(factorial)
                .build();
        assertEquals(10, e.evaluate().doubleValue(), 0);

        e = new ExpressionBuilder("3! * 2")
                .operator(factorial)
                .build();
        assertEquals(12, e.evaluate().doubleValue(), 0);

        e = new ExpressionBuilder("3!")
                .operator(factorial)
                .build();
        assertTrue(e.validate().isValid());
        assertEquals(6, e.evaluate().doubleValue(), 0);

        e = new ExpressionBuilder("3!!")
                .operator(factorial)
                .build();
        assertTrue(e.validate().isValid());
        assertEquals(720, e.evaluate().doubleValue(), 0);

        e = new ExpressionBuilder("4 + 3!")
                .operator(factorial)
                .build();
        assertTrue(e.validate().isValid());
        assertEquals(10, e.evaluate().doubleValue(), 0);

        e = new ExpressionBuilder("3! * 2")
                .operator(factorial)
                .build();
        assertTrue(e.validate().isValid());
        assertEquals(12, e.evaluate().doubleValue(), 0);

        e = new ExpressionBuilder("2 * 3!")
                .operator(factorial)
                .build();
        assertTrue(e.validate().isValid());
        assertEquals(12, e.evaluate().doubleValue(), 0);

        e = new ExpressionBuilder("4 + (3!)")
                .operator(factorial)
                .build();
        assertTrue(e.validate().isValid());
        assertEquals(10, e.evaluate().doubleValue(), 0);

        e = new ExpressionBuilder("4 + 3! + 2 * 6")
                .operator(factorial)
                .build();
        assertTrue(e.validate().isValid());
        assertEquals(22, e.evaluate().doubleValue(), 0);
    }

    @Test
    public void testCotangent1() {
        final var e = new ExpressionBuilder("cot(1)")
                .build();
        assertEquals(1 / Math.tan(1), e.evaluate().doubleValue(), 0d);

    }

    @Test(expected = ArithmeticException.class)
    public void testInvalidCotangent1() {
        final var e = new ExpressionBuilder("cot(0)")
                .build();
        e.evaluate();

    }

    @Test(expected = IllegalArgumentException.class)
    public void testOperatorFactorial2() {
        final var factorial = new Operator("!", 1, true, Operator.PRECEDENCE_POWER + 1) {

            @Override
            public BigDecimal apply(final BigDecimal... args) {
                final var arg = args[0].intValue();
/*        if ((double) arg != args[0]) {
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

        final var e = new ExpressionBuilder("!3").build();
        assertFalse(e.validate().isValid());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidFactorial2() {
        final var factorial = new Operator("!", 1, true, Operator.PRECEDENCE_POWER + 1) {

            @Override
            public BigDecimal apply(final BigDecimal... args) {
                final var arg = args[0].longValue();
/*        if ((double) arg != args[0]) {
          throw new IllegalArgumentException("Operand for factorial has to be an integer");
        }
        if (arg < 0) {
          throw new IllegalArgumentException("The operand of the factorial can not be less than zero");
        }
 */
                BigDecimal result = BigDecimal.ONE;
                for (var i = 1; i <= arg; i++) {
                    result = result.multiply(BigDecimal.valueOf(i));
                }
                return result;
            }
        };

        final var e = new ExpressionBuilder("!!3").build();
        assertFalse(e.validate().isValid());
    }

    @Test
    public void testClearVariables() {
        final var builder = new ExpressionBuilder("x + y");
        builder.variable("x");
        builder.variable("y");

        final var expression = builder.build();
        final var values = new HashMap<String, BigDecimal>();
        values.put("x", BigDecimal.valueOf(1.0));
        values.put("y", BigDecimal.valueOf(2.0));
        expression.setVariables(values);

        double result = expression.evaluate().doubleValue();
        assertEquals(3.0, result, 3.0 - result);

        expression.clearVariables();

        try {
            result = expression.evaluate().doubleValue();
            fail("Should fail as there aren't values in the expression.");
        } catch (final Exception ignored) {

        }

        final var emptyMap = new HashMap<String, BigDecimal>();
        expression.setVariables(emptyMap);

        try {
            result = expression.evaluate().doubleValue();
            fail("Should fail as there aren't values in the expression.");
        } catch (final Exception ignored) {

        }

    }


    @Test
    @Ignore
    // If Expression should be threads safe this test must pass
    public void evaluateFamily() {
        final var e = new ExpressionBuilder("sin(x)")
                .variable("x")
                .build();
        final Executor executor = Executors.newFixedThreadPool(100);
        for (var i = 0; i < 100000; i++) {
            executor.execute(() -> {
                final var x = Math.random();
                e.setVariable("x", x);
                try {
                    Thread.sleep(100);
                } catch (final InterruptedException e1) {
                    e1.printStackTrace();
                }
                assertEquals(Math.sin(x), e.evaluate().doubleValue(), 0f);
            });
        }
    }
}
