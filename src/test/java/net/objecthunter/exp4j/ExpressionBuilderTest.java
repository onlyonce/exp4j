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

import net.objecthunter.exp4j.function.Function;
import net.objecthunter.exp4j.operator.Operator;
import net.objecthunter.exp4j.operator.Operators;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.concurrent.Executors;

import static java.lang.Math.*;
import static org.junit.Assert.*;

@SuppressWarnings("MagicNumber")
public class ExpressionBuilderTest {

    @Test
    public void testExpressionBuilder1() {
        final var result = new ExpressionBuilder("2+1")
                .build()
                .evaluate();
        assertEquals(3d, result.doubleValue(), 0d);
    }

    @Test
    public void testExpressionBuilder2() {
        final var result = new ExpressionBuilder("cos(x)")
                .variables("x")
                .build()
                .setVariable("x", Math.PI)
                .evaluate();
        final var expected = cos(Math.PI);
        assertEquals(expected, result.doubleValue(), 0d);
    }

    @Test
    public void testExpressionBuilder3() {
        final var x = Math.PI;
        final var result = new ExpressionBuilder("sin(x)-log(3*x/4)")
                .variables("x")
                .build()
                .setVariable("x", x)
                .evaluate();

        final var expected = sin(x) - log(3 * x / 4);
        assertEquals(expected, result.doubleValue(), 0d);
    }

    @Test
    public void testExpressionBuilder4() {
        final var log2 = new Function("log2", 1) {

            @Override
            public BigDecimal apply(final BigDecimal... args) {
                return BigDecimal.valueOf(Math.log(args[0].doubleValue()) / Math.log(2));
            }
        };
        final var result = new ExpressionBuilder("log2(4)")
                .function(log2)
                .build()
                .evaluate();

        final double expected = 2;
        assertEquals(expected, result.doubleValue(), 0d);
    }

    @Test
    public void testExpressionBuilder5() {
        final var avg = new Function("avg", 4) {

            @Override
            public BigDecimal apply(final BigDecimal... args) {
                var sum = BigDecimal.ZERO;
                for (final var arg : args) {
                    sum = sum.add(arg);
                }
                return sum.divide(BigDecimal.valueOf(args.length), Operators.MC);
            }
        };
        final var result = new ExpressionBuilder("avg(1,2,3,4)")
                .function(avg)
                .build()
                .evaluate();

        final var expected = 2.5d;
        assertEquals(expected, result.doubleValue(), 0d);
    }

    @Test
    public void testExpressionBuilder6() {
        final var factorial = new Operator("!", 1, true, Operator.PRECEDENCE_POWER + 1) {

            @Override
            public BigDecimal apply(final BigDecimal... args) {
                final var arg = args[0];
                if (0 != BigDecimal.valueOf(arg.longValue()).compareTo(args[0])) {
                    throw new IllegalArgumentException("Operand for factorial has to be an integer");
                }
                if (0 > arg.signum()) {
                    throw new IllegalArgumentException("The operand of the factorial can not be less than zero");
                }
                var result = BigDecimal.ONE;
                for (var i = 1; i <= arg.intValue(); i++) {
                    result = result.multiply(BigDecimal.valueOf(i));
                }
                return result;
            }
        };

        final var result = new ExpressionBuilder("3!")
                .operator(factorial)
                .build()
                .evaluate();

        final var expected = 6d;
        assertEquals(expected, result.doubleValue(), 0d);
    }

    @Test
    public void testExpressionBuilder7() {
        final var res = new ExpressionBuilder("x")
                .variables("x")
                .build()
                .validate();
        assertFalse(res.isValid());
        assertEquals(1, res.getErrors().size());
    }

    @Test
    public void testExpressionBuilder8() {
        final var res = new ExpressionBuilder("x*y*z")
                .variables("x", "y", "z")
                .build()
                .validate();
        assertFalse(res.isValid());
        assertEquals(3, res.getErrors().size());
    }

    @Test
    public void testExpressionBuilder9() {
        final var res = new ExpressionBuilder("x")
                .variables("x")
                .build()
                .setVariable("x", 1d)
                .validate();
        assertTrue(res.isValid());
    }

    @Test
    public void testValidationDocExample() {
        final var e = new ExpressionBuilder("x")
                .variables("x")
                .build();
        var res = e.validate();
        assertFalse(res.isValid());
        assertEquals(1, res.getErrors().size());

        e.setVariable("x", 1d);
        res = e.validate();
        assertTrue(res.isValid());
    }

    @Test
    public void testExpressionBuilder10() {
        final var result = new ExpressionBuilder("1e1")
                .build()
                .evaluate();
        assertEquals(10d, result.doubleValue(), 0d);
    }

    @Test
    public void testExpressionBuilder11() {
        final var result = new ExpressionBuilder("1.11e-1")
                .build()
                .evaluate();
        assertEquals(0.111d, result.doubleValue(), 0d);
    }

    @Test
    public void testExpressionBuilder12() {
        final var result = new ExpressionBuilder("1.11e+1")
                .build()
                .evaluate();
        assertEquals(11.1d, result.doubleValue(), 0d);
    }

    @Test
    public void testExpressionBuilder13() {
        final var result = new ExpressionBuilder("-3^2")
                .build()
                .evaluate();
        assertEquals(-9d, result.doubleValue(), 0d);
    }

    @Test
    public void testExpressionBuilder14() {
        final var result = new ExpressionBuilder("(-3)^2")
                .build()
                .evaluate();
        assertEquals(9d, result.doubleValue(), 0d);
    }

    @Test(expected = ArithmeticException.class)
    public void testExpressionBuilder15() {
        final var result = new ExpressionBuilder("-3/0")
                .build()
                .evaluate();
    }

    @Test
    public void testExpressionBuilder16() {
        final var result = new ExpressionBuilder("log(x) - y * (sqrt(x^cos(y)))")
                .variables("x", "y")
                .build()
                .setVariable("x", 1d)
                .setVariable("y", 2d)
                .evaluate();
    }

    @Test
    public void testExpressionBuilder17() {
        final var e = new ExpressionBuilder("x-y*")
                .variables("x", "y")
                .build();
        final var res = e.validate(false);
        assertFalse(res.isValid());
        assertEquals(1, res.getErrors().size());
        assertEquals("Too many operators", res.getErrors().get(0));
    }

    @Test
    public void testExpressionBuilder18() {
        final var e = new ExpressionBuilder("log(x) - y *")
                .variables("x", "y")
                .build();
        final var res = e.validate(false);
        assertFalse(res.isValid());
        assertEquals(1, res.getErrors().size());
        assertEquals("Too many operators", res.getErrors().get(0));
    }

    @Test
    public void testExpressionBuilder19() {
        final var e = new ExpressionBuilder("x - y *")
                .variables("x", "y")
                .build();
        final var res = e.validate(false);
        assertFalse(res.isValid());
        assertEquals(1, res.getErrors().size());
        assertEquals("Too many operators", res.getErrors().get(0));
    }

    /* legacy tests from earlier exp4j versions */

    @Test
    public void testFunction1() {
        final var custom = new Function("timespi") {

            @Override
            public BigDecimal apply(final BigDecimal... values) {
                return values[0].multiply(BigDecimal.valueOf(Math.PI));
            }
        };
        final var e = new ExpressionBuilder("timespi(x)")
                .function(custom)
                .variables("x")
                .build()
                .setVariable("x", 1);
        final var result = e.evaluate().doubleValue();
        assertEquals(PI, result, 0.0);
    }

    //@Disabled("NaN")
    @Test
    public void testFunction2() {
        final var custom = new Function("loglog") {

            @Override
            public BigDecimal apply(final BigDecimal... values) {
                return BigDecimal.valueOf(Math.log(Math.log(values[0].doubleValue())));
            }
        };
        final var e = new ExpressionBuilder("loglog(x)")
                .variables("x")
                .function(custom)
                .build()
                .setVariable("x", 1);
        final var result = e.evaluate().doubleValue();
        assertEquals(log(log(1)), result, 0.0);
    }

    @Test
    public void testFunction3() {
        final var custom1 = new Function("foo") {

            @Override
            public BigDecimal apply(final BigDecimal... values) {
                return values[0].multiply(BigDecimal.valueOf(Math.E));
            }
        };
        final var custom2 = new Function("bar") {

            @Override
            public BigDecimal apply(final BigDecimal... values) {
                return values[0].multiply(BigDecimal.valueOf(Math.PI));
            }
        };
        final var e = new ExpressionBuilder("foo(bar(x))")
                .function(custom1)
                .function(custom2)
                .variables("x")
                .build()
                .setVariable("x", 1);
        final var result = e.evaluate().doubleValue();
        assertEquals(1 * E * PI, result, 0.0);
    }

    @Test
    public void testFunction4() {
        final var custom1 = new Function("foo") {

            @Override
            public BigDecimal apply(final BigDecimal... values) {
                return values[0].multiply(BigDecimal.valueOf(Math.E));
            }
        };
        final var varX = 32.24979131d;
        final var e = new ExpressionBuilder("foo(log(x))")
                .variables("x")
                .function(custom1)
                .build()
                .setVariable("x", varX);
        final var result = e.evaluate().doubleValue();
        assertEquals(log(varX) * E, result, 0.0);
    }

    @Test
    public void testFunction5() {
        final var custom1 = new Function("foo") {

            @Override
            public BigDecimal apply(final BigDecimal... values) {
                return values[0].multiply(BigDecimal.valueOf(Math.E));
            }
        };
        final var custom2 = new Function("bar") {

            @Override
            public BigDecimal apply(final BigDecimal... values) {
                return values[0].multiply(BigDecimal.valueOf(Math.PI));
            }
        };
        final var varX = 32.24979131d;
        final var e = new ExpressionBuilder("bar(foo(log(x)))")
                .variables("x")
                .function(custom1)
                .function(custom2)
                .build()
                .setVariable("x", varX);
        final var result = e.evaluate().doubleValue();
        assertEquals(log(varX) * E * PI, result, 0.0);
    }

    @Test
    public void testFunction6() {
        final var custom1 = new Function("foo") {

            @Override
            public BigDecimal apply(final BigDecimal... values) {
                return values[0].multiply(BigDecimal.valueOf(Math.E));
            }
        };
        final var custom2 = new Function("bar") {

            @Override
            public BigDecimal apply(final BigDecimal... values) {
                return values[0].multiply(BigDecimal.valueOf(Math.PI));
            }
        };
        final var varX = 32.24979131d;
        final var e = new ExpressionBuilder("bar(foo(log(x)))")
                .variables("x")
                .functions(custom1, custom2)
                .build()
                .setVariable("x", varX);
        final var result = e.evaluate().doubleValue();
        assertEquals(log(varX) * E * PI, result, 0.0);
    }

    @Test
    public void testFunction7() {
        final var custom1 = new Function("half") {

            @Override
            public BigDecimal apply(final BigDecimal... values) {
                return values[0].divide(BigDecimal.valueOf(2), Operators.MC);
            }
        };
        final var e = new ExpressionBuilder("half(x)")
                .variables("x")
                .function(custom1)
                .build()
                .setVariable("x", 1d);
        assertEquals(0.5d, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testFunction10() {
        final var custom1 = new Function("max", 2) {

            @Override
            public BigDecimal apply(final BigDecimal... values) {
                return 0 <= values[0].compareTo(values[1]) ? values[1] : values[0];
            }
        };
        final var e =
                new ExpressionBuilder("max(x,y)")
                        .variables("x", "y")
                        .function(custom1)
                        .build()
                        .setVariable("x", 1d)
                        .setVariable("y", 2d);
        assertEquals(2.0, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testFunction11() {
        final var custom1 = new Function("power", 2) {

            @Override
            public BigDecimal apply(final BigDecimal... values) {
                return BigDecimal.valueOf(Math.pow(values[0].doubleValue(), values[1].doubleValue()));
            }
        };
        final var e =
                new ExpressionBuilder("power(x,y)")
                        .variables("x", "y")
                        .function(custom1)
                        .build()
                        .setVariable("x", 2d)
                        .setVariable("y",
                                4d);
        assertEquals(pow(2, 4), e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testFunction12() {
        final var custom1 = new Function("max", 5) {

            @Override
            public BigDecimal apply(final BigDecimal... values) {
                var max = values[0];
                for (var i = 1; i < numArguments; i++) {
                    if (0 < values[i].compareTo(max)) {
                        max = values[i];
                    }
                }
                return max;
            }
        };
        final var e = new ExpressionBuilder("max(1,2.43311,51.13,43,12)")
                .function(custom1)
                .build();
        assertEquals(51.13d, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testFunction13() {
        final var custom1 = new Function("max", 3) {

            @Override
            public BigDecimal apply(final BigDecimal... values) {
                var max = values[0];
                for (var i = 1; i < numArguments; i++) {
                    if (0 < values[i].compareTo(max)) {
                        max = values[i];
                    }
                }
                return max;
            }
        };
        final var varX = Math.E;
        final var e = new ExpressionBuilder("max(log(x),sin(x),x)")
                .variables("x")
                .function(custom1)
                .build()
                .setVariable("x", varX);
        assertEquals(varX, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testFunction14() {
        final var custom1 = new Function("multiply", 2) {

            @Override
            public BigDecimal apply(final BigDecimal... values) {
                return values[0].multiply(values[1]);
            }
        };
        final double varX = 1;
        final var e = new ExpressionBuilder("multiply(sin(x),x+1)")
                .variables("x")
                .function(custom1)
                .build()
                .setVariable("x", varX);
        final var expected = Math.sin(varX) * (varX + 1);
        final var actual = e.evaluate().doubleValue();
        assertEquals(expected, actual, 0.0);
    }

    @Test
    public void testFunction15() {
        final var custom1 = new Function("timesPi") {

            @Override
            public BigDecimal apply(final BigDecimal... values) {
                return values[0].multiply(BigDecimal.valueOf(Math.PI));
            }
        };
        final double varX = 1;
        final var e = new ExpressionBuilder("timesPi(x^2)")
                .variables("x")
                .function(custom1)
                .build()
                .setVariable("x", varX);
        final var expected = varX * Math.PI;
        final var actual = e.evaluate().doubleValue();
        assertEquals(expected, actual, 0.0);
    }

    @Test
    public void testFunction16() {
        final var custom1 = new Function("multiply", 3) {

            @Override
            public BigDecimal apply(final BigDecimal... values) {
                return values[0].multiply(values[1]).multiply(values[2]);
            }
        };
        final double varX = 1;
        final var e = new ExpressionBuilder("multiply(sin(x),x+1^(-2),log(x))")
                .variables("x")
                .function(custom1)
                .build()
                .setVariable("x", varX);
        final var expected = Math.sin(varX) * Math.pow((varX + 1), -2) * Math.log(varX);
        assertEquals(expected, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testFunction17() {
        final var custom1 = new Function("timesPi") {

            @Override
            public BigDecimal apply(final BigDecimal... values) {
                return values[0].multiply(BigDecimal.valueOf(Math.PI));
            }
        };
        final var varX = Math.E;
        final var e = new ExpressionBuilder("timesPi(log(x^(2+1)))")
                .variables("x")
                .function(custom1)
                .build()
                .setVariable("x", varX);
        final var expected = Math.log(Math.pow(varX, 3)) * Math.PI;
        assertEquals(expected, e.evaluate().doubleValue(), 0.0);
    }

    // thanks to Marcin Domanski who issued
    // http://jira.congrace.de/jira/browse/EXP-11
    // i have this test, which fails in 0.2.9
    @Test
    public void testFunction18() {
        final var minFunction = new Function("min", 2) {

            @Override
            public BigDecimal apply(final BigDecimal[] values) {
                var currentMin = values[0];
                for (final var value : values) {
                    currentMin = currentMin.min(value);
                }
                return currentMin;
            }
        };
        final var b = new ExpressionBuilder("-min(5, 0) + 10")
                .function(minFunction);
        final var calculated = b.build().evaluate().doubleValue();
        assertEquals(10, calculated, 0.0);
    }

    // thanks to Sylvain Machefert who issued
    // http://jira.congrace.de/jira/browse/EXP-11
    // i have this test, which fails in 0.3.2
    @Test
    public void testFunction19() {
        final var minFunction = new Function("power", 2) {

            @Override
            public BigDecimal apply(final BigDecimal[] values) {
                return BigDecimal.valueOf(Math.pow(values[0].doubleValue(), values[1].doubleValue()));
            }
        };
        final var b = new ExpressionBuilder("power(2,3)")
                .function(minFunction);
        final var calculated = b.build().evaluate().doubleValue();
        assertEquals(Math.pow(2, 3), calculated, 0d);
    }

    // thanks to Narendra Harmwal who noticed that getArgumentCount was not
    // implemented
    // this test has been added in 0.3.5
    @Test
    public void testFunction20() {
        final var maxFunction = new Function("max", 3) {

            @Override
            public BigDecimal apply(final BigDecimal... values) {
                var max = values[0];
                for (var i = 1; i < numArguments; i++) {
                    max = max.max(values[i]);
                }
                return max;
            }
        };

        final var b = new ExpressionBuilder("max(1,2,3)")
                .function(maxFunction);
        final var calculated = b.build().evaluate().doubleValue();

        assertEquals(3, maxFunction.getNumArguments());

        assertEquals(3, calculated, 0.0);

    }

    @Test
    public void testOperators1() {
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

        var e = new ExpressionBuilder("1!").operator(factorial)
                .build();
        assertEquals(1d, e.evaluate().doubleValue(), 0.0);
        e = new ExpressionBuilder("2!").operator(factorial)
                .build();
        assertEquals(2d, e.evaluate().doubleValue(), 0.0);
        e = new ExpressionBuilder("3!").operator(factorial)
                .build();
        assertEquals(6d, e.evaluate().doubleValue(), 0.0);
        e = new ExpressionBuilder("4!").operator(factorial)
                .build();
        assertEquals(24d, e.evaluate().doubleValue(), 0.0);
        e = new ExpressionBuilder("5!").operator(factorial)
                .build();
        assertEquals(120d, e.evaluate().doubleValue(), 0.0);
        e = new ExpressionBuilder("11!").operator(factorial)
                .build();
        assertEquals(39916800d, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testOperators2() {
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
        var e = new ExpressionBuilder("2^3!").operator(factorial)
                .build();
        assertEquals(64d, e.evaluate().doubleValue(), 0d);
        e = new ExpressionBuilder("3!^2").operator(factorial)
                .build();
        assertEquals(36d, e.evaluate().doubleValue(), 0.0);
        e = new ExpressionBuilder("-(3!)^-1").operator(factorial)
                .build();
        final var actual = e.evaluate().doubleValue();
        assertEquals(Math.pow(-6d, -1), actual, 0d);
    }

    @Test
    public void testOperators3() {
        final var gteq = new Operator(">=", 2, true, Operator.PRECEDENCE_ADDITION - 1) {

            @Override
            public BigDecimal apply(final BigDecimal[] values) {
                if (0 <= values[0].compareTo(values[1])) {
                    return BigDecimal.ONE;
                } else {
                    return BigDecimal.ZERO;
                }
            }
        };
        var e = new ExpressionBuilder("1>=2").operator(gteq)
                .build();
        assertEquals(0d, e.evaluate().doubleValue(), 0.0);
        e = new ExpressionBuilder("2>=1").operator(gteq)
                .build();
        assertEquals(1d, e.evaluate().doubleValue(), 0.0);
        e = new ExpressionBuilder("-2>=1").operator(gteq)
                .build();
        assertEquals(0d, e.evaluate().doubleValue(), 0.0);
        e = new ExpressionBuilder("-2>=-1").operator(gteq)
                .build();
        assertEquals(0d, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testModulo1() {
        final var result = new ExpressionBuilder("33%(20/2)%2")
                .build().evaluate();
        assertEquals(1d, result.doubleValue(), 0.0);
    }

    @Test
    public void testOperators4() {
        final var greaterEq = new Operator(">=", 2, true, 4) {

            @Override
            public BigDecimal apply(final BigDecimal[] values) {
                if (0 <= values[0].compareTo(values[1])) {
                    return BigDecimal.ONE;
                } else {
                    return BigDecimal.ZERO;
                }
            }
        };
        final var greater = new Operator(">", 2, true, 4) {

            @Override
            public BigDecimal apply(final BigDecimal[] values) {
                if (0 < values[0].compareTo(values[1])) {
                    return BigDecimal.ONE;
                } else {
                    return BigDecimal.ZERO;
                }
            }
        };
        final var newPlus = new Operator(">=>", 2, true, 4) {

            @Override
            public BigDecimal apply(final BigDecimal[] values) {
                return values[0].add(values[1]);
            }
        };
        var e = new ExpressionBuilder("1>2").operator(greater)
                .build();
        assertEquals(0d, e.evaluate().doubleValue(), 0.0);
        e = new ExpressionBuilder("2>=2").operator(greaterEq)
                .build();
        assertEquals(1d, e.evaluate().doubleValue(), 0.0);
        e = new ExpressionBuilder("1>=>2").operator(newPlus)
                .build();
        assertEquals(3d, e.evaluate().doubleValue(), 0.0);
        e = new ExpressionBuilder("1>=>2>2").operator(greater).operator(newPlus)
                .build();
        assertEquals(1d, e.evaluate().doubleValue(), 0.0);
        e = new ExpressionBuilder("1>=>2>2>=1").operator(greater).operator(newPlus)
                .operator(greaterEq)
                .build();
        assertEquals(1d, e.evaluate().doubleValue(), 0.0);
        e = new ExpressionBuilder("1 >=> 2 > 2 >= 1").operator(greater).operator(newPlus)
                .operator(greaterEq)
                .build();
        assertEquals(1d, e.evaluate().doubleValue(), 0.0);
        e = new ExpressionBuilder("1 >=> 2 >= 2 > 1").operator(greater).operator(newPlus)
                .operator(greaterEq)
                .build();
        assertEquals(0d, e.evaluate().doubleValue(), 0.0);
        e = new ExpressionBuilder("1 >=> 2 >= 2 > 0").operator(greater).operator(newPlus)
                .operator(greaterEq)
                .build();
        assertEquals(1d, e.evaluate().doubleValue(), 0.0);
        e = new ExpressionBuilder("1 >=> 2 >= 2 >= 1").operator(greater).operator(newPlus)
                .operator(greaterEq)
                .build();
        assertEquals(1d, e.evaluate().doubleValue(), 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidOperator1() {
        final var fail = new Operator("2", 2, true, 1) {

            @Override
            public BigDecimal apply(final BigDecimal[] values) {
                return BigDecimal.ZERO;
            }
        };
        new ExpressionBuilder("1").operator(fail)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidFunction1() {
        final var func = new Function("1gd") {

            @Override
            public BigDecimal apply(final BigDecimal... args) {
                return BigDecimal.ZERO;
            }
        };
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidFunction2() {
        final var func = new Function("+1gd") {

            @Override
            public BigDecimal apply(final BigDecimal... args) {
                return BigDecimal.ZERO;
            }
        };
    }

    @Test
    public void testExpressionBuilder01() {
        final var e = new ExpressionBuilder("7*x + 3*y")
                .variables("x", "y")
                .build()
                .setVariable("x", 1)
                .setVariable("y", 2);
        final var result = e.evaluate().doubleValue();
        assertEquals(13d, result, 0.0);
    }

    @Test
    public void testExpressionBuilder02() {
        final var e = new ExpressionBuilder("7*x + 3*y")
                .variables("x", "y")
                .build()
                .setVariable("x", 1)
                .setVariable("y", 2);
        final var result = e.evaluate().doubleValue();
        assertEquals(13d, result, 0.0);
    }

    @Test
    public void testExpressionBuilder03() {
        final var varX = 1.3d;
        final var varY = 4.22d;
        final var e = new ExpressionBuilder("7*x + 3*y - log(y/x*12)^y")
                .variables("x", "y")
                .build()
                .setVariable("x", varX)
                .setVariable("y",
                        varY);
        final var result = e.evaluate().doubleValue();
        assertEquals(7 * varX + 3 * varY - pow(log(varY / varX * 12), varY), result, 0.0);
    }

    //@Disabled("NaN")
    @Test
    public void testExpressionBuilder04() {
        var varX = 1.3d;
        var varY = 4.22d;
        final var e =
                new ExpressionBuilder("7*x + 3*y - log(y/x*12)^y")
                        .variables("x", "y")
                        .build()
                        .setVariable("x", varX)
                        .setVariable("y", varY);
        var result = e.evaluate().doubleValue();
        assertEquals(7 * varX + 3 * varY - pow(log(varY / varX * 12), varY), result, 0.0);
        varX = 1.79854d;
        varY = 9281.123d;
        e.setVariable("x", varX);
        e.setVariable("y", varY);
        result = e.evaluate().doubleValue();
        assertEquals(7 * varX + 3 * varY - pow(log(varY / varX * 12), varY), result, 0.0);
    }

    @Test
    public void testExpressionBuilder05() {
        final var varX = 1.3d;
        final var varY = 4.22d;
        final var e = new ExpressionBuilder("3*y")
                .variables("y")
                .build()
                .setVariable("x", varX)
                .setVariable("y", varY);
        final var result = e.evaluate().doubleValue();
        assertEquals(3 * varY, result, 0.0);
    }

    @Test
    public void testExpressionBuilder06() {
        final var varX = 1.3d;
        final var varY = 4.22d;
        final var varZ = 4.22d;
        final var e = new ExpressionBuilder("x * y * z")
                .variables("x", "y", "z")
                .build();
        e.setVariable("x", varX);
        e.setVariable("y", varY);
        e.setVariable("z", varZ);
        final var result = e.evaluate().doubleValue();
        assertEquals(varX * varY * varZ, result, 0.0);
    }

    @Test
    public void testExpressionBuilder07() {
        final var varX = 1.3d;
        final var e = new ExpressionBuilder("log(sin(x))")
                .variables("x")
                .build()
                .setVariable("x", varX);
        final var result = e.evaluate().doubleValue();
        assertEquals(log(sin(varX)), result, 0.0);
    }

    @Test
    public void testExpressionBuilder08() {
        final var varX = 1.3d;
        final var e = new ExpressionBuilder("log(sin(x))")
                .variables("x")
                .build()
                .setVariable("x", varX);
        final var result = e.evaluate().doubleValue();
        assertEquals(log(sin(varX)), result, 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSameName() {
        final var custom = new Function("bar") {

            @Override
            public BigDecimal apply(final BigDecimal... values) {
                return values[0].divide(BigDecimal.valueOf(2), Operators.MC);
            }
        };
        final var varBar = 1.3d;
        final var e = new ExpressionBuilder("bar(bar)")
                .variables("bar")
                .function(custom)
                .build()
                .setVariable("bar", varBar);
        final var res = e.validate();
        assertFalse(res.isValid());
        assertEquals(1, res.getErrors().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidFunction() {
        final var varY = 4.22d;
        final var e = new ExpressionBuilder("3*invalid_function(y)")
                .variables("<")
                .build()
                .setVariable("y", varY);
        e.evaluate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingVar() {
        final var varY = 4.22d;
        final var e = new ExpressionBuilder("3*y*z")
                .variables("y", "z")
                .build()
                .setVariable("y", varY);
        e.evaluate();
    }

    @Test
    public void testUnaryMinusPowerPrecedence() {
        final var e = new ExpressionBuilder("-1^2")
                .build();
        assertEquals(-1d, e.evaluate().doubleValue(), 0d);
    }

    @Test
    public void testUnaryMinus() {
        final var e = new ExpressionBuilder("-1")
                .build();
        assertEquals(-1d, e.evaluate().doubleValue(), 0d);
    }

    @Test
    public void testExpression1() {
        final String expr;
        final double expected;
        expr = "2 + 4";
        expected = 6d;
        final var e = new ExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testExpression10() {
        final String expr;
        final double expected;
        expr = "1 * 1.5 + 1";
        expected = 1 * 1.5 + 1;
        final var e = new ExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testExpression11() {
        final var x = 1d;
        final var y = 2d;
        final var expr = "log(x) ^ sin(y)";
        final var expected = Math.pow(Math.log(x), Math.sin(y));
        final var e = new ExpressionBuilder(expr)
                .variables("x", "y")
                .build()
                .setVariable("x", x)
                .setVariable("y", y);
        assertEquals(expected, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testExpression12() {
        final var expr = "log(2.5333333333)^(0-1)";
        final var expected = Math.pow(Math.log(2.5333333333d), -1);
        final var e = new ExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testExpression13() {
        final var expr = "2.5333333333^(0-1)";
        final var expected = Math.pow(2.5333333333d, -1);
        final var e = new ExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testExpression14() {
        final var expr = "2 * 17.41 + (12*2)^(0-1)";
        final var expected = 2 * 17.41d + Math.pow((12 * 2), -1);
        final var e = new ExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testExpression15() {
        final var expr = "2.5333333333 * 17.41 + (12*2)^log(2.764)";
        final var expected = 2.5333333333d * 17.41d + Math.pow((12 * 2), Math.log(2.764d));
        final var e = new ExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testExpression16() {
        final var expr = "2.5333333333/2 * 17.41 + (12*2)^(log(2.764) - sin(5.6664))";
        final var expected = 2.5333333333d / 2 * 17.41d + Math.pow((12 * 2), Math.log(2.764d) - Math.sin(5.6664d));
        final var e = new ExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testExpression17() {
        final var expr = "x^2 - 2 * y";
        final var x = Math.E;
        final var y = Math.PI;
        final var expected = x * x - 2 * y;
        final var e = new ExpressionBuilder(expr)
                .variables("x", "y")
                .build()
                .setVariable("x", x)
                .setVariable("y", y);
        assertEquals(expected, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testExpression18() {
        final var expr = "-3";
        final double expected = -3;
        final var e = new ExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testExpression19() {
        final var expr = "-3 * -24.23";
        final var expected = -3 * -24.23d;
        final var e = new ExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testExpression2() {
        final String expr;
        final double expected;
        expr = "2+3*4-12";
        expected = 2 + 3 * 4 - 12;
        final var e = new ExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testExpression20() {
        final var expr = "-2 * 24/log(2) -2";
        final var expected = -2 * 24 / Math.log(2) - 2;
        final var e = new ExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testExpression21() {
        final var expr = "-2 *33.34/log(x)^-2 + 14 *6";
        final var x = 1.334d;
        final var expected = -2 * 33.34 / Math.pow(Math.log(x), -2) + 14 * 6;
        final var e = new ExpressionBuilder(expr)
                .variables("x")
                .build()
                .setVariable("x", x);
        assertEquals(expected, e.evaluate().doubleValue(), 0d);
    }

    @Test
    public void testExpressionPower() {
        final var expr = "2^-2";
        final var expected = Math.pow(2, -2);
        final var e = new ExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate().doubleValue(), 0d);
    }

    @Test
    public void testExpressionMultiplication() {
        final var expr = "2*-2";
        final var expected = -4d;
        final var e = new ExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate().doubleValue(), 0d);
    }

    @Test
    public void testExpression22() {
        final var expr = "-2 *33.34/log(x)^-2 + 14 *6";
        final var x = 1.334d;
        final var expected = -2 * 33.34 / Math.pow(Math.log(x), -2) + 14 * 6;
        final var e = new ExpressionBuilder(expr)
                .variables("x")
                .build()
                .setVariable("x", x);
        assertEquals(expected, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testExpression23() {
        final var expr = "-2 *33.34/(log(foo)^-2 + 14 *6) - sin(foo)";
        final var x = 1.334d;
        final var expected = -2 * 33.34 / (Math.pow(Math.log(x), -2) + 14 * 6) - Math.sin(x);
        final var e = new ExpressionBuilder(expr)
                .variables("foo")
                .build()
                .setVariable("foo", x);
        assertEquals(expected, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testExpression24() {
        final var expr = "3+4-log(23.2)^(2-1) * -1";
        final var expected = 3 + 4 - Math.pow(Math.log(23.2), (2 - 1)) * -1;
        final var e = new ExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testExpression25() {
        final var expr = "+3+4-+log(23.2)^(2-1) * + 1";
        final var expected = 3 + 4 - Math.log(23.2d);
        final var e = new ExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testExpression26() {
        final var expr = "14 + -(1 / 2.22^3)";
        final var expected = 14 - (1d / Math.pow(2.22d, 3d));
        final var e = new ExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testExpression27() {
        final var expr = "12^-+-+-+-+-+-+---2";
        final var expected = Math.pow(12, -2);
        final var e = new ExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testExpression28() {
        final var expr = "12^-+-+-+-+-+-+---2 * (-14) / 2 ^ -log(2.22323) ";
        final var expected = Math.pow(12, -2) * -14 / Math.pow(2, -Math.log(2.22323));
        final var e = new ExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testExpression29() {
        final var expr = "24.3343 % 3";
        final var expected = 24.3343 % 3;
        final var e = new ExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testVarName1() {
        final var expr = "12.23 * foo.bar";
        final var e = new ExpressionBuilder(expr)
                .variables("foo.bar")
                .build()
                .setVariable("foo.bar", 1d);
        assertEquals(12.23, e.evaluate().doubleValue(), 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMisplacedSeparator() {
        final var expr = "12.23 * ,foo";
        final var e = new ExpressionBuilder(expr)
                .build()
                .setVariable(",foo", 1d);
        assertEquals(12.23, e.evaluate().doubleValue(), 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidVarName() {
        final var expr = "12.23 * @foo";
        final var e = new ExpressionBuilder(expr)
                .build()
                .setVariable("@foo", 1d);
        assertEquals(12.23, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testVarMap() {
        final var expr = "12.23 * foo - bar";
        final var variables = new HashMap<String, BigDecimal>();
        variables.put("foo", BigDecimal.valueOf(2d));
        variables.put("bar", BigDecimal.valueOf(3.3d));
        final var e = new ExpressionBuilder(expr)
                .variables(variables.keySet())
                .build()
                .setVariables(variables);
        assertEquals(12.23d * 2d - 3.3d, e.evaluate().doubleValue(), 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidNumberOfArguments1() {
        final var expr = "log(2,2)";
        final var e = new ExpressionBuilder(expr)
                .build();
        e.evaluate().doubleValue();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidNumberOfArguments2() {
        final var avg = new Function("avg", 4) {

            @Override
            public BigDecimal apply(final BigDecimal... args) {
                var sum = BigDecimal.ZERO;
                for (final var arg : args) {
                    sum = sum.add(arg);
                }
                return sum.divide(BigDecimal.valueOf(args.length), Operators.MC);
            }
        };
        final var expr = "avg(2,2)";
        final var e = new ExpressionBuilder(expr)
                .function(avg)
                .build();
        e.evaluate();
    }

    @Test
    public void testExpression3() {
        final String expr;
        final double expected;
        expr = "2+4*5";
        expected = 2 + 4 * 5;
        final var e = new ExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testExpression30() {
        final var expr = "24.3343 % 3 * 20 ^ -(2.334 % log(2 / 14))";
        final var expected = 24.3343d % 3 * Math.pow(20, -(2.334 % Math.log(2d / 14d)));
        final var e = new ExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testExpression31() {
        final var expr = "-2 *33.34/log(y_x)^-2 + 14 *6";
        final var x = 1.334d;
        final var expected = -2 * 33.34 / Math.pow(Math.log(x), -2) + 14 * 6;
        final var e = new ExpressionBuilder(expr)
                .variables("y_x")
                .build()
                .setVariable("y_x", x);
        assertEquals(expected, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testExpression32() {
        final var expr = "-2 *33.34/log(y_2x)^-2 + 14 *6";
        final var x = 1.334d;
        final var expected = -2 * 33.34 / Math.pow(Math.log(x), -2) + 14 * 6;
        final var e = new ExpressionBuilder(expr)
                .variables("y_2x")
                .build()
                .setVariable("y_2x", x);
        assertEquals(expected, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testExpression33() {
        final var expr = "-2 *33.34/log(_y)^-2 + 14 *6";
        final var x = 1.334d;
        final var expected = -2 * 33.34 / Math.pow(Math.log(x), -2) + 14 * 6;
        final var e = new ExpressionBuilder(expr)
                .variables("_y")
                .build()
                .setVariable("_y", x);
        assertEquals(expected, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testExpression34() {
        final var expr = "-2 + + (+4) +(4)";
        final double expected = -2 + 4 + 4;
        final var e = new ExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testExpression40() {
        final var expr = "1e1";
        final var expected = 10d;
        final var e = new ExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testExpression41() {
        final var expr = "1e-1";
        final var expected = 0.1d;
        final var e = new ExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate().doubleValue(), 0.0);
    }

    /*
     * Added tests for expressions with scientific notation see http://jira.congrace.de/jira/browse/EXP-17
     */
    @Test
    public void testExpression42() {
        final var expr = "7.2973525698e-3";
        final var expected = 7.2973525698e-3d;
        final var e = new ExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testExpression43() {
        final var expr = "6.02214E23";
        final var expected = 6.02214e23d;
        final var e = new ExpressionBuilder(expr)
                .build();
        final var result = e.evaluate().doubleValue();
        assertEquals(expected, result, 0.0);
    }

    @Test
    public void testExpression44() {
        final var expr = "6.02214E23";
        final var expected = 6.02214e23d;
        final var e = new ExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate().doubleValue(), 0.0);
    }

    @Test(expected = NumberFormatException.class)
    public void testExpression45() {
        final var expr = "6.02214E2E3";
        new ExpressionBuilder(expr)
                .build();
    }

    @Test(expected = NumberFormatException.class)
    public void testExpression46() {
        final var expr = "6.02214e2E3";
        new ExpressionBuilder(expr)
                .build();
    }

    // tests for EXP-20: No exception is thrown for unmatched parenthesis in
    // build
    // Thanks go out to maheshkurmi for reporting
    @Test(expected = IllegalArgumentException.class)
    public void testExpression48() {
        final var expr = "(1*2";
        final var e = new ExpressionBuilder(expr)
                .build();
        final var result = e.evaluate().doubleValue();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExpression49() {
        final var expr = "{1*2";
        final var e = new ExpressionBuilder(expr)
                .build();
        final var result = e.evaluate().doubleValue();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExpression50() {
        final var expr = "[1*2";
        final var e = new ExpressionBuilder(expr)
                .build();
        final var result = e.evaluate().doubleValue();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExpression51() {
        final var expr = "(1*{2+[3}";
        final var e = new ExpressionBuilder(expr)
                .build();
        final var result = e.evaluate().doubleValue();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExpression52() {
        final var expr = "(1*(2+(3";
        final var e = new ExpressionBuilder(expr)
                .build();
        final var result = e.evaluate().doubleValue();
    }

    @Test
    public void testExpression53() {
        final var expr = "14 * 2x";
        final var exp = new ExpressionBuilder(expr)
                .variables("x")
                .build();
        exp.setVariable("x", 1.5d);
        assertTrue(exp.validate().isValid());
        assertEquals(14d * 2d * 1.5d, exp.evaluate().doubleValue(), 0d);
    }

    @Test
    public void testExpression54() {
        final var expr = "2 ((-(x)))";
        final var e = new ExpressionBuilder(expr)
                .variables("x")
                .build();
        e.setVariable("x", 1.5d);
        assertEquals(-3d, e.evaluate().doubleValue(), 0d);
    }

    @Test
    public void testExpression55() {
        final var expr = "2 sin(x)";
        final var e = new ExpressionBuilder(expr)
                .variables("x")
                .build();
        e.setVariable("x", 2d);
        assertEquals(sin(2d) * 2, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testExpression56() {
        final var expr = "2 sin(3x)";
        final var e = new ExpressionBuilder(expr)
                .variables("x")
                .build();
        e.setVariable("x", 2d);
        assertEquals(sin(6d) * 2d, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testDocumentationExample1() {
        final var e = new ExpressionBuilder("3 * sin(y) - 2 / (x - 2)")
                .variables("x", "y")
                .build()
                .setVariable("x", 2.3)
                .setVariable("y", 3.14);
        final var result = e.evaluate().doubleValue();
        final var expected = 3 * Math.sin(3.14d) - 2d / (2.3d - 2d);
        assertEquals(expected, result, 0d);
    }

    @Test
    public void testDocumentationExample2() throws Exception {
        final var exec = Executors.newFixedThreadPool(1);
        final var e = new ExpressionBuilder("3log(y)/(x+1)")
                .variables("x", "y")
                .build()
                .setVariable("x", 2.3)
                .setVariable("y", 3.14);
        final var result = e.evaluateAsync(exec);
        final var expected = 3 * Math.log(3.14d) / (3.3);
        assertEquals(expected, result.get().doubleValue(), 0d);
    }

    @Test
    public void testDocumentationExample3() {
        final var result = new ExpressionBuilder("2cos(xy)")
                .variables("x", "y")
                .build()
                .setVariable("x", 0.5d)
                .setVariable("y", 0.25d)
                .evaluate();
        assertEquals(2d * Math.cos(0.5d * 0.25d), result.doubleValue(), 0d);
    }

    @Test
    public void testDocumentationExample4() {
        final var expr = "pi+π+e+φ";
        final var expected = 2 * Math.PI + Math.E + 1.61803398874d;
        final var e = new ExpressionBuilder(expr).build();
        assertEquals(expected, e.evaluate().doubleValue(), 0d);
    }

    @Test
    public void testDocumentationExample5() {
        final var expr = "7.2973525698e-3";
        final var expected = Double.parseDouble(expr);
        final var e = new ExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate().doubleValue(), 0d);
    }


    @Test
    public void testDocumentationExample6() {
        final var logb = new Function("logb", 2) {
            @Override
            public BigDecimal apply(final BigDecimal... args) {
                return BigDecimal.valueOf(Math.log(args[0].doubleValue()) / Math.log(args[1].doubleValue()));
            }
        };
        final var result = new ExpressionBuilder("logb(8, 2)")
                .function(logb)
                .build()
                .evaluate();
        final double expected = 3;
        assertEquals(expected, result.doubleValue(), 0d);
    }

    @Test
    public void testDocumentationExample7() {
        final var avg = new Function("avg", 4) {

            @Override
            public BigDecimal apply(final BigDecimal... args) {
                var sum = BigDecimal.ZERO;
                for (final var arg : args) {
                    sum = sum.add(arg);
                }
                return sum.divide(BigDecimal.valueOf(args.length), Operators.MC);
            }
        };
        final var result = new ExpressionBuilder("avg(1,2,3,4)")
                .function(avg)
                .build()
                .evaluate();

        final var expected = 2.5d;
        assertEquals(expected, result.doubleValue(), 0d);
    }

    @Test
    public void testDocumentationExample8() {
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

        final var result = new ExpressionBuilder("3!")
                .operator(factorial)
                .build()
                .evaluate();

        final var expected = 6d;
        assertEquals(expected, result.doubleValue(), 0d);
    }

    @Test
    public void testDocumentationExample9() {
        final var gteq = new Operator(">=", 2, true, Operator.PRECEDENCE_ADDITION - 1) {

            @Override
            public BigDecimal apply(final BigDecimal[] values) {
                if (0 <= values[0].compareTo(values[1])) {
                    return BigDecimal.ONE;
                } else {
                    return BigDecimal.ZERO;
                }
            }
        };

        var e = new ExpressionBuilder("1>=2").operator(gteq)
                .build();
        assertEquals(0d, e.evaluate().doubleValue(), 0.0);
        e = new ExpressionBuilder("2>=1").operator(gteq)
                .build();
        assertEquals(1d, e.evaluate().doubleValue(), 0.0);
    }

    @Test(expected = ArithmeticException.class)
    public void testDocumentationExample10() {
        final var reciprocal = new Operator("$", 1, true, Operator.PRECEDENCE_DIVISION) {
            @Override
            public BigDecimal apply(final BigDecimal... args) {
                if (0 == args[0].signum()) {
                    throw new ArithmeticException("Division by zero!");
                }
                return BigDecimal.valueOf(1d / args[0].doubleValue());
            }
        };
        final var e = new ExpressionBuilder("0$").operator(reciprocal).build();
        e.evaluate();
    }

    @Test
    public void testDocumentationExample11() {
        final var e = new ExpressionBuilder("x")
                .variable("x")
                .build();

        var res = e.validate();
        assertFalse(res.isValid());
        assertEquals(1, res.getErrors().size());

        e.setVariable("x", 1d);
        res = e.validate();
        assertTrue(res.isValid());
    }

    @Test
    public void testDocumentationExample12() {
        final var e = new ExpressionBuilder("x")
                .variable("x")
                .build();

        final var res = e.validate(false);
        assertTrue(res.isValid());
        assertNull(res.getErrors());
    }

    // Thanks go out to Johan Björk for reporting the division by zero problem EXP-22
    // https://www.objecthunter.net/jira/browse/EXP-22
    @Test(expected = ArithmeticException.class)
    public void testExpression57() {
        final var expr = "1 / 0";
        final var e = new ExpressionBuilder(expr)
                .build();
        assertEquals(Double.POSITIVE_INFINITY, e.evaluate().doubleValue(), 0.0);
    }

    //@Disabled("NaN")
    @Test
    public void testExpression58() {
        final var expr = "17 * sqrt(-1) * 12";
        final var e = new ExpressionBuilder(expr)
                .build();
        assertTrue(Double.isNaN(e.evaluate().doubleValue()));
    }

    // Thanks go out to Alex Dolinsky for reporting the missing exception when an empty
    // expression is passed as in new ExpressionBuilder("")
    @Test(expected = IllegalArgumentException.class)
    public void testExpression59() {
        final var e = new ExpressionBuilder("")
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExpression60() {
        final var e = new ExpressionBuilder("   ")
                .build();
        e.evaluate();
    }

    @Test(expected = ArithmeticException.class)
    public void testExpression61() {
        final var e = new ExpressionBuilder("14 % 0")
                .build();
        e.evaluate();
    }

    // https://www.objecthunter.net/jira/browse/EXP-24
    // thanks go out to Rémi for the issue report
    @Test
    public void testExpression62() {
        final var e = new ExpressionBuilder("x*1.0e5+5")
                .variables("x")
                .build()
                .setVariable("x", Math.E);
        assertEquals(E * 1.0 * pow(10, 5) + 5, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testExpression63() {
        final var e = new ExpressionBuilder("log10(5)")
                .build();
        assertEquals(Math.log10(5), e.evaluate().doubleValue(), 0d);
    }

    @Test
    public void testExpression64() {
        final var e = new ExpressionBuilder("log2(5)")
                .build();
        assertEquals(Math.log(5) / Math.log(2), e.evaluate().doubleValue(), 0d);
    }

    @Test
    public void testExpression65() {
        final var e = new ExpressionBuilder("2log(e)")
                .variables("e")
                .build()
                .setVariable("e", Math.E);

        assertEquals(2d, e.evaluate().doubleValue(), 0d);
    }

    @Test
    public void testExpression66() {
        final var e = new ExpressionBuilder("log(e)2")
                .variables("e")
                .build()
                .setVariable("e", Math.E);

        assertEquals(2d, e.evaluate().doubleValue(), 0d);
    }

    @Test
    public void testExpression67() {
        final var e = new ExpressionBuilder("2esin(pi/2)")
                .variables("e", "pi")
                .build()
                .setVariable("e", Math.E)
                .setVariable("pi", Math.PI);

        assertEquals(2 * Math.E * Math.sin(Math.PI / 2d), e.evaluate().doubleValue(), 0d);
    }

    @Test
    public void testExpression68() {
        final var e = new ExpressionBuilder("2x")
                .variables("x")
                .build()
                .setVariable("x", Math.E);
        assertEquals(2 * Math.E, e.evaluate().doubleValue(), 0d);
    }

    @Test
    public void testExpression69() {
        final var e = new ExpressionBuilder("2x2")
                .variables("x")
                .build()
                .setVariable("x", Math.E);
        assertEquals(4 * Math.E, e.evaluate().doubleValue(), 0d);
    }

    @Test
    public void testExpression70() {
        final var e = new ExpressionBuilder("2xx")
                .variables("x")
                .build()
                .setVariable("x", Math.E);
        assertEquals(2 * Math.E * Math.E, e.evaluate().doubleValue(), 0d);
    }

    @Test
    public void testExpression71() {
        final var e = new ExpressionBuilder("x2x")
                .variables("x")
                .build()
                .setVariable("x", Math.E);
        assertEquals(2 * Math.E * Math.E, e.evaluate().doubleValue(), 0d);
    }

    @Test
    public void testExpression72() {
        final var e = new ExpressionBuilder("2cos(x)")
                .variables("x")
                .build()
                .setVariable("x", Math.E);
        assertEquals(2 * Math.cos(Math.E), e.evaluate().doubleValue(), 0d);
    }

    @Test
    public void testExpression73() {
        final var e = new ExpressionBuilder("cos(x)2")
                .variables("x")
                .build()
                .setVariable("x", Math.E);
        assertEquals(2 * Math.cos(Math.E), e.evaluate().doubleValue(), 0d);
    }

    @Test
    public void testExpression74() {
        final var e = new ExpressionBuilder("cos(x)(-2)")
                .variables("x")
                .build()
                .setVariable("x", Math.E);
        assertEquals(-2d * Math.cos(Math.E), e.evaluate().doubleValue(), 0d);
    }

    @Test
    public void testExpression75() {
        final var e = new ExpressionBuilder("(-2)cos(x)")
                .variables("x")
                .build()
                .setVariable("x", Math.E);
        assertEquals(-2d * Math.cos(Math.E), e.evaluate().doubleValue(), 0d);
    }

    @Test
    public void testExpression76() {
        final var e = new ExpressionBuilder("(-x)cos(x)")
                .variables("x")
                .build()
                .setVariable("x", Math.E);
        assertEquals(-E * Math.cos(Math.E), e.evaluate().doubleValue(), 0d);
    }

    @Test
    public void testExpression77() {
        final var e = new ExpressionBuilder("(-xx)cos(x)")
                .variables("x")
                .build()
                .setVariable("x", Math.E);
        assertEquals(-E * E * Math.cos(Math.E), e.evaluate().doubleValue(), 0d);
    }

    @Test
    public void testExpression78() {
        final var e = new ExpressionBuilder("(xx)cos(x)")
                .variables("x")
                .build()
                .setVariable("x", Math.E);
        assertEquals(E * E * Math.cos(Math.E), e.evaluate().doubleValue(), 0d);
    }

    @Test
    public void testExpression79() {
        final var e = new ExpressionBuilder("cos(x)(xx)")
                .variables("x")
                .build()
                .setVariable("x", Math.E);
        assertEquals(E * E * Math.cos(Math.E), e.evaluate().doubleValue(), 0d);
    }

    @Test
    public void testExpression80() {
        final var e = new ExpressionBuilder("cos(x)(xy)")
                .variables("x", "y")
                .build()
                .setVariable("x", Math.E)
                .setVariable("y", Math.sqrt(2));
        assertEquals(sqrt(2) * E * Math.cos(Math.E), e.evaluate().doubleValue(), 0d);
    }

    @Test
    public void testExpression81() {
        final var e = new ExpressionBuilder("cos(xy)")
                .variables("x", "y")
                .build()
                .setVariable("x", Math.E)
                .setVariable("y", Math.sqrt(2));
        assertEquals(cos(sqrt(2) * E), e.evaluate().doubleValue(), 0d);
    }

    @Test
    public void testExpression82() {
        final var e = new ExpressionBuilder("cos(2x)")
                .variables("x")
                .build()
                .setVariable("x", Math.E);
        assertEquals(cos(2 * E), e.evaluate().doubleValue(), 0d);
    }

    @Test
    public void testExpression83() {
        final var e = new ExpressionBuilder("cos(xlog(xy))")
                .variables("x", "y")
                .build()
                .setVariable("x", Math.E)
                .setVariable("y", Math.sqrt(2));
        assertEquals(cos(E * log(E * sqrt(2))), e.evaluate().doubleValue(), 0d);
    }

    @Test
    public void testExpression84() {
        final var e = new ExpressionBuilder("3x_1")
                .variables("x_1")
                .build()
                .setVariable("x_1", Math.E);
        assertEquals(3d * E, e.evaluate().doubleValue(), 0d);
    }

    @Test
    public void testExpression85() {
        final var e = new ExpressionBuilder("1/2x")
                .variables("x")
                .build()
                .setVariable("x", 6);
        assertEquals(3d, e.evaluate().doubleValue(), 0d);
    }

    // thanks got out to David Sills
    @Test(expected = IllegalArgumentException.class)
    public void testSpaceBetweenNumbers() {
        final var e = new ExpressionBuilder("1 1")
                .build();
    }

    // thanks go out to Janny for providing the tests and the bug report
    @Test
    public void testUnaryMinusInParenthesisSpace() {
        final var b = new ExpressionBuilder("( -1)^2");
        final var calculated = b.build().evaluate().doubleValue();
        assertEquals(1d, calculated, 0.0);
    }

    @Test
    public void testUnaryMinusSpace() {
        final var b = new ExpressionBuilder(" -1 + 2");
        final var calculated = b.build().evaluate().doubleValue();
        assertEquals(1d, calculated, 0.0);
    }

    @Test
    public void testUnaryMinusSpaces() {
        final var b = new ExpressionBuilder(" -1 + + 2 +   -   1");
        final var calculated = b.build().evaluate().doubleValue();
        assertEquals(0d, calculated, 0.0);
    }

    @Test
    public void testUnaryMinusSpace1() {
        final var b = new ExpressionBuilder("-1");
        final var calculated = b.build().evaluate().doubleValue();
        assertEquals(-1d, calculated, 0.0);
    }

    @Test
    public void testExpression4() {
        final String expr;
        final double expected;
        expr = "2+4 * 5";
        expected = 2 + 4 * 5;
        final var e = new ExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testExpression5() {
        final String expr;
        final double expected;
        expr = "(2+4)*5";
        expected = (2 + 4) * 5;
        final var e = new ExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testExpression6() {
        final String expr;
        final double expected;
        expr = "(2+4)*5 + 2.5*2";
        expected = (2 + 4) * 5 + 2.5 * 2;
        final var e = new ExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testExpression7() {
        final String expr;
        final double expected;
        expr = "(2+4)*5 + 10/2";
        expected = (2 + 4) * 5 + 10 / 2;
        final var e = new ExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testExpression8() {
        final String expr;
        final double expected;
        expr = "(2 * 3 +4)*5 + 10/2";
        expected = (2 * 3 + 4) * 5 + 10 / 2;
        final var e = new ExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testExpression9() {
        final String expr;
        final double expected;
        expr = "(2 * 3 +4)*5 +4 + 10/2";
        expected = 59; //(2 * 3 + 4) * 5 + 4 + 10 / 2 = 59
        final var e = new ExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate().doubleValue(), 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFailUnknownFunction1() {
        final String expr;
        expr = "lig(1)";
        final var e = new ExpressionBuilder(expr)
                .build();
        e.evaluate().doubleValue();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFailUnknownFunction2() {
        final String expr;
        expr = "galength(1)";
        new ExpressionBuilder(expr)
                .build().evaluate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFailUnknownFunction3() {
        final String expr;
        expr = "tcos(1)";
        final var exp = new ExpressionBuilder(expr)
                .build();
        final var result = exp.evaluate();
        System.out.println(result);
    }

    @Test
    public void testFunction22() {
        final String expr;
        expr = "cos(cos_1)";
        final var e = new ExpressionBuilder(expr)
                .variables("cos_1")
                .build()
                .setVariable("cos_1", 1d);
        assertEquals(cos(1d), e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testFunction23() {
        final String expr;
        expr = "log1p(1)";
        final var e = new ExpressionBuilder(expr)
                .build();
        assertEquals(log1p(1d), e.evaluate().doubleValue(), 0d);
    }

    @Test
    public void testFunction24() {
        final String expr;
        expr = "pow(3,3)";
        final var e = new ExpressionBuilder(expr)
                .build();
        assertEquals(27d, e.evaluate().doubleValue(), 0d);
    }

    @Test
    public void testPostfix1() {
        final String expr;
        final double expected;
        expr = "2.2232^0.1";
        expected = Math.pow(2.2232d, 0.1d);
        final var actual = new ExpressionBuilder(expr)
                .build().evaluate().doubleValue();
        assertEquals(expected, actual, 0.0);
    }

    @Test
    public void testPostfixEverything() {
        final String expr;
        final double expected;
        expr = "(sin(12) + log(34)) * 3.42 - cos(2.234-log(2))";
        expected = (Math.sin(12) + Math.log(34)) * 3.42 - Math.cos(2.234 - Math.log(2));
        final var e = new ExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testPostfixExponentiation1() {
        final String expr;
        final double expected;
        expr = "2^3";
        expected = Math.pow(2, 3);
        final var e = new ExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testPostfixExponentiation2() {
        final String expr;
        final double expected;
        expr = "24 + 4 * 2^3";
        expected = 24 + 4 * Math.pow(2, 3);
        final var e = new ExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testPostfixExponentiation3() {
        final String expr;
        final double expected;
        final var x = 4.334d;
        expr = "24 + 4 * 2^x";
        expected = 24 + 4 * Math.pow(2, x);
        final var e = new ExpressionBuilder(expr)
                .variables("x")
                .build()
                .setVariable("x", x);
        assertEquals(expected, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testPostfixExponentiation4() {
        final String expr;
        final double expected;
        final var x = 4.334d;
        expr = "(24 + 4) * 2^log(x)";
        expected = (24 + 4) * Math.pow(2, Math.log(x));
        final var e = new ExpressionBuilder(expr)
                .variables("x")
                .build()
                .setVariable("x", x);
        assertEquals(expected, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testPostfixFunction1() {
        final String expr;
        final double expected;
        expr = "log(1) * sin(0)";
        expected = Math.log(1) * Math.sin(0);
        final var e = new ExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testPostfixFunction10() {
        final String expr;
        double expected;
        expr = "cbrt(x)";
        final var e = new ExpressionBuilder(expr)
                .variables("x")
                .build();
        for (double x = -10; 10 > x; x = x + 0.5d) {
            expected = Math.cbrt(x);
            assertEquals(expected, e.setVariable("x", x).evaluate().doubleValue(), 0.0);
        }
    }

    @Test
    public void testPostfixFunction11() {
        final String expr;
        double expected;
        expr = "cos(x) - (1/cbrt(x))";
        final var e = new ExpressionBuilder(expr)
                .variables("x")
                .build();
        for (double x = -10; 10 > x; x = x + 0.5d) {
            if (0d == x) {
                continue;
            }
            expected = Math.cos(x) - (1 / Math.cbrt(x));
            assertEquals(expected, e.setVariable("x", x).evaluate().doubleValue(), 0.0);
        }
    }

    //@Disabled("NaN")
    @Test
    public void testPostfixFunction12() {
        final String expr;
        double expected;
        expr = "acos(x) * expm1(asin(x)) - exp(atan(x)) + floor(x) + cosh(x) - sinh(cbrt(x))";
        final var e = new ExpressionBuilder(expr)
                .variables("x")
                .build();
        for (double x = -10; 10 > x; x = x + 0.5d) {
            expected =
                    Math.acos(x) * Math.expm1(Math.asin(x)) - Math.exp(Math.atan(x)) + Math.floor(x) + Math.cosh(x)
                            - Math.sinh(Math.cbrt(x));
            if (Double.isNaN(expected)) {
                assertTrue(Double.isNaN(e.setVariable("x", x).evaluate().doubleValue()));
            } else {
                assertEquals(expected, e.setVariable("x", x).evaluate().doubleValue(), 0.0);
            }
        }
    }

    //@Disabled("NaN")
    @Test
    public void testPostfixFunction13() {
        final String expr;
        double expected;
        expr = "acos(x)";
        final var e = new ExpressionBuilder(expr)
                .variables("x")
                .build();
        for (double x = -10; 10 > x; x = x + 0.5d) {
            expected = Math.acos(x);
            if (Double.isNaN(expected)) {
                assertTrue(Double.isNaN(e.setVariable("x", x).evaluate().doubleValue()));
            } else {
                assertEquals(expected, e.setVariable("x", x).evaluate().doubleValue(), 0.0);
            }
        }
    }

    @Test
    public void testPostfixFunction14() {
        final String expr;
        double expected;
        expr = " expm1(x)";
        final var e = new ExpressionBuilder(expr)
                .variables("x")
                .build();
        for (double x = -10; 10 > x; x = x + 0.5d) {
            expected = Math.expm1(x);
            if (Double.isNaN(expected)) {
                assertTrue(Double.isNaN(e.setVariable("x", x).evaluate().doubleValue()));
            } else {
                assertEquals(expected, e.setVariable("x", x).evaluate().doubleValue(), 0.0);
            }
        }
    }

    //@Disabled("NaN")
    @Test
    public void testPostfixFunction15() {
        final String expr;
        double expected;
        expr = "asin(x)";
        final var e = new ExpressionBuilder(expr)
                .variables("x")
                .build();
        for (double x = -10; 10 > x; x = x + 0.5d) {
            expected = Math.asin(x);
            if (Double.isNaN(expected)) {
                assertTrue(Double.isNaN(e.setVariable("x", x).evaluate().doubleValue()));
            } else {
                assertEquals(expected, e.setVariable("x", x).evaluate().doubleValue(), 0.0);
            }
        }
    }

    @Test
    public void testPostfixFunction16() {
        final String expr;
        double expected;
        expr = " exp(x)";
        final var e = new ExpressionBuilder(expr)
                .variables("x")
                .build();
        for (double x = -10; 10 > x; x = x + 0.5d) {
            expected = Math.exp(x);
            assertEquals(expected, e.setVariable("x", x).evaluate().doubleValue(), 0.0);
        }
    }

    @Test
    public void testPostfixFunction17() {
        final String expr;
        double expected;
        expr = "floor(x)";
        final var e = new ExpressionBuilder(expr)
                .variables("x")
                .build();
        for (double x = -10; 10 > x; x = x + 0.5d) {
            expected = Math.floor(x);
            assertEquals(expected, e.setVariable("x", x).evaluate().doubleValue(), 0.0);
        }
    }

    @Test
    public void testPostfixFunction18() {
        final String expr;
        double expected;
        expr = " cosh(x)";
        final var e = new ExpressionBuilder(expr)
                .variables("x")
                .build();
        for (double x = -10; 10 > x; x = x + 0.5d) {
            expected = Math.cosh(x);
            assertEquals(expected, e.setVariable("x", x).evaluate().doubleValue(), 0.0);
        }
    }

    @Test
    public void testPostfixFunction19() {
        final String expr;
        double expected;
        expr = "sinh(x)";
        final var e = new ExpressionBuilder(expr)
                .variables("x")
                .build();
        for (double x = -10; 10 > x; x = x + 0.5d) {
            expected = Math.sinh(x);
            assertEquals(expected, e.setVariable("x", x).evaluate().doubleValue(), 0.0);
        }
    }

    @Test
    public void testPostfixFunction20() {
        final String expr;
        double expected;
        expr = "cbrt(x)";
        final var e = new ExpressionBuilder(expr)
                .variables("x")
                .build();
        for (double x = -10; 10 > x; x = x + 0.5d) {
            expected = Math.cbrt(x);
            assertEquals(expected, e.setVariable("x", x).evaluate().doubleValue(), 0.0);
        }
    }

    @Test
    public void testPostfixFunction21() {
        final String expr;
        double expected;
        expr = "tanh(x)";
        final var e = new ExpressionBuilder(expr)
                .variables("x")
                .build();
        for (double x = -10; 10 > x; x = x + 0.5d) {
            expected = Math.tanh(x);
            assertEquals(expected, e.setVariable("x", x).evaluate().doubleValue(), 0.0);
        }
    }

    @Test
    public void testPostfixFunction2() {
        final String expr;
        final double expected;
        expr = "log(1)";
        expected = 0d;
        final var e = new ExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testPostfixFunction3() {
        final String expr;
        final double expected;
        expr = "sin(0)";
        expected = 0d;
        final var e = new ExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testPostfixFunction5() {
        final String expr;
        final double expected;
        expr = "ceil(2.3) +1";
        expected = Math.ceil(2.3) + 1;
        final var e = new ExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testPostfixFunction6() {
        final String expr;
        final double expected;
        final var x = 1.565d;
        final var y = 2.1323d;
        expr = "ceil(x) + 1 / y * abs(1.4)";
        expected = Math.ceil(x) + 1 / y * Math.abs(1.4);
        final var e = new ExpressionBuilder(expr)
                .variables("x", "y")
                .build();
        assertEquals(expected, e.setVariable("x", x)
                .setVariable("y", y).evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testPostfixFunction7() {
        final String expr;
        final double expected;
        final var x = Math.E;
        expr = "tan(x)";
        expected = Math.tan(x);
        final var e = new ExpressionBuilder(expr)
                .variables("x")
                .build();
        assertEquals(expected, e.setVariable("x", x).evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testPostfixFunction8() {
        final String expr;
        final double expected;
        expr = "2^3.4223232 + tan(e)";
        expected = Math.pow(2, 3.4223232d) + Math.tan(Math.E);
        final var e = new ExpressionBuilder(expr)
                .variables("e")
                .build();
        assertEquals(expected, e.setVariable("e", E).evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testPostfixFunction9() {
        final String expr;
        final double expected;
        final var x = Math.E;
        expr = "cbrt(x)";
        expected = Math.cbrt(x);
        final var e = new ExpressionBuilder(expr)
                .variables("x")
                .build();
        assertEquals(expected, e.setVariable("x", x).evaluate().doubleValue(), 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPostfixInvalidVariableName() {
        final String expr;
        final double expected;
        final var x = 4.5334332d;
        final var log = Math.PI;
        expr = "x * pi";
        expected = x * log;
        final var e = new ExpressionBuilder(expr)
                .variables("x", "pi")
                .build();
        assertEquals(expected, e.setVariable("x", x)
                .setVariable("log", log).evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testPostfixParenthesis() {
        final String expr;
        final double expected;
        expr = "(3 + 3 * 14) * (2 * (24-17) - 14)/((34) -2)";
        expected = 0; //(3 + 3 * 14) * (2 * (24-17) - 14)/((34) -2) = 0
        final var e = new ExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testPostfixVariables() {
        final String expr;
        final double expected;
        final var x = 4.5334332d;
        final var pi = Math.PI;
        expr = "x * pi";
        expected = x * pi;
        final var e = new ExpressionBuilder(expr)
                .variables("x", "pi")
                .build();
        assertEquals(expected, e.setVariable("x", x)
                .setVariable("pi", pi).evaluate().doubleValue(), 0.0);
    }

    @Test
    public void testUnicodeVariable1() {
        final var e = new ExpressionBuilder("λ")
                .variable("λ")
                .build()
                .setVariable("λ", E);
        assertEquals(E, e.evaluate().doubleValue(), 0d);
    }

    @Test
    public void testUnicodeVariable2() {
        final var e = new ExpressionBuilder("log(3ε+1)")
                .variable("ε")
                .build()
                .setVariable("ε", E);
        assertEquals(log(3 * E + 1), e.evaluate().doubleValue(), 0d);
    }

    @Test
    public void testUnicodeVariable3() {
        final var log = new Function("λωγ", 1) {

            @Override
            public BigDecimal apply(final BigDecimal... args) {
                return BigDecimal.valueOf(log(args[0].doubleValue()));
            }
        };

        final var e = new ExpressionBuilder("λωγ(π)")
                .variable("π")
                .function(log)
                .build()
                .setVariable("π", PI);
        assertEquals(log(PI), e.evaluate().doubleValue(), 0d);
    }

    @Test
    public void testUnicodeVariable4() {
        final var log = new Function("λ_ωγ", 1) {

            @Override
            public BigDecimal apply(final BigDecimal... args) {
                return BigDecimal.valueOf(log(args[0].doubleValue()));
            }
        };

        final var e = new ExpressionBuilder("3λ_ωγ(πε6)")
                .variables("π", "ε")
                .function(log)
                .build()
                .setVariable("π", PI)
                .setVariable("ε", E);
        assertEquals(3 * log(PI * E * 6), e.evaluate().doubleValue(), 0d);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testImplicitMultiplicationOffNumber() {
        final var e = new ExpressionBuilder("var_12")
                .variable("var_1")
                .implicitMultiplication(false)
                .build();
        e.evaluate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testImplicitMultiplicationOffVariable() {
        final var e = new ExpressionBuilder("var_1var_1")
                .variable("var_1")
                .implicitMultiplication(false)
                .build();
        e.evaluate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testImplicitMultiplicationOffParentheses() {
        final var e = new ExpressionBuilder("var_1(2)")
                .variable("var_1")
                .implicitMultiplication(false)
                .build();
        e.evaluate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testImplicitMultiplicationOffFunction() {
        final var e = new ExpressionBuilder("var_1log(2)")
                .variable("var_1")
                .implicitMultiplication(false)
                .build()
                .setVariable("var_1", 2);
        e.evaluate();
    }

    @Test
    public void testImplicitMultiplicationOnNumber() {
        final var e = new ExpressionBuilder("var_12")
                .variable("var_1")
                .build()
                .setVariable("var_1", 2);
        assertEquals(4d, e.evaluate().doubleValue(), 0d);
    }

    @Test
    public void testImplicitMultiplicationOnVariable() {
        final var e = new ExpressionBuilder("var_1var_1")
                .variable("var_1")
                .build()
                .setVariable("var_1", 2);
        assertEquals(4d, e.evaluate().doubleValue(), 0d);
    }

    @Test
    public void testImplicitMultiplicationOnParentheses() {
        final var e = new ExpressionBuilder("var_1(2)")
                .variable("var_1")
                .build()
                .setVariable("var_1", 2);
        assertEquals(4d, e.evaluate().doubleValue(), 0d);
    }

    @Test
    public void testImplicitMultiplicationOnFunction() {
        final var e = new ExpressionBuilder("var_1log(2)")
                .variable("var_1")
                .build()
                .setVariable("var_1", 2);
        assertEquals(2 * log(2), e.evaluate().doubleValue(), 0d);
    }

    // thanks go out to vandanagopal for reporting the issue
    // https://github.com/fasseg/exp4j/issues/23
    @Test
    public void testSecondArgumentNegative() {
        final var round = new Function("MULTIPLY", 2) {
            @Override
            public BigDecimal apply(final BigDecimal... args) {
                return BigDecimal.valueOf(Math.round(args[0].doubleValue() * args[1].doubleValue()));
            }
        };
        final var result = new ExpressionBuilder("MULTIPLY(2,-1)")
                .function(round)
                .build()
                .evaluate();
        assertEquals(-2d, result.doubleValue(), 0d);
    }

    // Test for https://github.com/fasseg/exp4j/issues/65
    @Test
    public void testVariableWithDot() {
        final var result = new ExpressionBuilder("2*SALARY.Basic")
                .variable("SALARY.Basic")
                .build()
                .setVariable("SALARY.Basic", 1.5d)
                .evaluate();
        assertEquals(3d, result.doubleValue(), 0d);
    }

    @Test
    public void testTwoAdjacentOperators() {
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

        final var result = new ExpressionBuilder("3!+2")
                .operator(factorial)
                .build()
                .evaluate();

        final var expected = 8d;
        assertEquals(expected, result.doubleValue(), 0d);
    }

    @Test
    public void testGetVariableNames1() {
        final var e = new ExpressionBuilder("b*a-9.24c")
                .variables("b", "a", "c")
                .build();
        final var variableNames = e.getVariableNames();
        assertTrue(variableNames.contains("a"));
        assertTrue(variableNames.contains("b"));
        assertTrue(variableNames.contains("c"));
    }

    @Test
    public void testGetVariableNames2() {
        final var e = new ExpressionBuilder("log(bar)-FOO.s/9.24c")
                .variables("bar", "FOO.s", "c")
                .build();
        final var variableNames = e.getVariableNames();
        assertTrue(variableNames.contains("bar"));
        assertTrue(variableNames.contains("FOO.s"));
        assertTrue(variableNames.contains("c"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSameVariableAndBuiltinFunctionName() {
        final var e = new ExpressionBuilder("log10(log10)")
                .variables("log10")
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSameVariableAndUserFunctionName() {
        final var e = new ExpressionBuilder("2*tr+tr(2)")
                .variables("tr")
                .function(new Function("tr") {
                    @Override
                    public BigDecimal apply(final BigDecimal... args) {
                        return BigDecimal.ZERO;
                    }
                })
                .build();
    }

    @Test
    public void testSignum() {
        var e = new ExpressionBuilder("signum(1)")
                .build();
        assertEquals(1, e.evaluate().doubleValue(), 0d);

        e = new ExpressionBuilder("signum(-1)")
                .build();
        assertEquals(-1, e.evaluate().doubleValue(), 0d);

        e = new ExpressionBuilder("signum(--1)")
                .build();
        assertEquals(1, e.evaluate().doubleValue(), 0d);

        e = new ExpressionBuilder("signum(+-1)")
                .build();
        assertEquals(-1, e.evaluate().doubleValue(), 0d);

        e = new ExpressionBuilder("-+1")
                .build();
        assertEquals(-1, e.evaluate().doubleValue(), 0d);

        e = new ExpressionBuilder("signum(-+1)")
                .build();
        assertEquals(-1, e.evaluate().doubleValue(), 0d);
    }

    @Test
    public void testCustomPercent() {
        final var percentage = new Function("percentage", 2) {
            @Override
            public BigDecimal apply(final BigDecimal... args) {
                final var val = args[0];
                final var percent = args[1];
                if (-1 == percent.signum()) {
                    return BigDecimal.valueOf(val.doubleValue() - val.doubleValue() * Math.abs(percent.doubleValue()) / 100d);
                } else {
                    return BigDecimal.valueOf(val.doubleValue() - val.doubleValue() * percent.doubleValue() / 100d);
                }
            }
        };

        var e = new ExpressionBuilder("percentage(1000,-10)")
                .function(percentage)
                .build();
        assertEquals(0d, 900, e.evaluate().doubleValue());

        e = new ExpressionBuilder("percentage(1000,12)")
                .function(percentage)
                .build();
        assertEquals(0d, 1000d * 0.12d, e.evaluate().doubleValue());
    }
}
