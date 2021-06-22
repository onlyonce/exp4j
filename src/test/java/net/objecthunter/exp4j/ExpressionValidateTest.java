/*
 * Copyright 2014 Bartosz Firyn
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;


class ExpressionValidateTest {

    /**
     * Dummy function with 2 arguments.
     */
    private final Function beta = new Function("beta", 2) {

        @Override
        public BigDecimal apply(final BigDecimal... args) {
            return args[1].subtract(args[0]);
        }
    };

    /**
     * Dummy function with 3 arguments.
     */
    private final Function gamma = new Function("gamma", 3) {

        @Override
        public BigDecimal apply(final BigDecimal... args) {
            return args[0].multiply(args[1]).divide(args[2]);
        }
    };

    /**
     * Dummy function with 7 arguments.
     */
    private final Function eta = new Function("eta", 7) {

        @Override
        public BigDecimal apply(final BigDecimal... args) {
            var eta = BigDecimal.ZERO;
            for (final var a : args) {
                eta = eta.add(a);
            }
            return eta;
        }
    };

    // valid scenarios

    @Test
    void testValidateNumber() {
        final var exp = new ExpressionBuilder("1")
                .build();
        final var result = exp.validate(false);
        Assertions.assertTrue(result.isValid());
    }

    @Test
    void testValidateNumberPositive() {
        final var exp = new ExpressionBuilder("+1")
                .build();
        final var result = exp.validate(false);
        Assertions.assertTrue(result.isValid());
    }

    @Test
    void testValidateNumberNegative() {
        final var exp = new ExpressionBuilder("-1")
                .build();
        final var result = exp.validate(false);
        Assertions.assertTrue(result.isValid());
    }

    @Test
    void testValidateOperator() {
        final var exp = new ExpressionBuilder("x + 1 + 2")
                .variable("x")
                .build();
        final var result = exp.validate(false);
        Assertions.assertTrue(result.isValid());
    }

    @Test
    void testValidateFunction() {
        final var exp = new ExpressionBuilder("sin(x)")
                .variable("x")
                .build();
        final var result = exp.validate(false);
        Assertions.assertTrue(result.isValid());
    }

    @Test
    void testValidateFunctionPositive() {
        final var exp = new ExpressionBuilder("+sin(x)")
                .variable("x")
                .build();
        final var result = exp.validate(false);
        Assertions.assertTrue(result.isValid());
    }

    @Test
    void testValidateFunctionNegative() {
        final var exp = new ExpressionBuilder("-sin(x)")
                .variable("x")
                .build();
        final var result = exp.validate(false);
        Assertions.assertTrue(result.isValid());
    }

    @Test
    void testValidateFunctionAndOperator() {
        final var exp = new ExpressionBuilder("sin(x + 1 + 2)")
                .variable("x")
                .build();
        final var result = exp.validate(false);
        Assertions.assertTrue(result.isValid());
    }

    @Test
    void testValidateFunctionWithTwoArguments() {
        final var exp = new ExpressionBuilder("beta(x, y)")
                .variables("x", "y")
                .functions(beta)
                .build();
        final var result = exp.validate(false);
        Assertions.assertTrue(result.isValid());
    }

    @Test
    void testValidateFunctionWithTwoArgumentsAndOperator() {
        final var exp = new ExpressionBuilder("beta(x, y + 1)")
                .variables("x", "y")
                .functions(beta)
                .build();
        final var result = exp.validate(false);
        Assertions.assertTrue(result.isValid());
    }

    @Test
    void testValidateFunctionWithThreeArguments() {
        final var exp = new ExpressionBuilder("gamma(x, y, z)")
                .variables("x", "y", "z")
                .functions(gamma)
                .build();
        final var result = exp.validate(false);
        Assertions.assertTrue(result.isValid());
    }

    @Test
    void testValidateFunctionWithThreeArgumentsAndOperator() {
        final var exp = new ExpressionBuilder("gamma(x, y, z + 1)")
                .variables("x", "y", "z")
                .functions(gamma)
                .build();
        final var result = exp.validate(false);
        Assertions.assertTrue(result.isValid());
    }

    @Test
    void testValidateFunctionWithTwoAndThreeArguments() {
        final var exp = new ExpressionBuilder("gamma(x, beta(y, h), z)")
                .variables("x", "y", "z", "h")
                .functions(gamma, beta)
                .build();
        final var result = exp.validate(false);
        Assertions.assertTrue(result.isValid());
    }

    @Test
    void testValidateFunctionWithTwoAndThreeArgumentsAndOperator() {
        final var exp = new ExpressionBuilder("gamma(x, beta(y, h), z + 1)")
                .variables("x", "y", "z", "h")
                .functions(gamma, beta)
                .build();
        final var result = exp.validate(false);
        Assertions.assertTrue(result.isValid());
    }

    @Test
    void testValidateFunctionWithTwoAndThreeArgumentsAndMultipleOperator() {
        final var exp = new ExpressionBuilder("gamma(x * 2 / 4, beta(y, h + 1 + 2), z + 1 + 2 + 3 + 4)")
                .variables("x", "y", "z", "h")
                .functions(gamma, beta)
                .build();
        final var result = exp.validate(false);
        Assertions.assertTrue(result.isValid());
    }

    @Test
    void testValidateFunctionWithSevenArguments() {
        final var exp = new ExpressionBuilder("eta(1, 2, 3, 4, 5, 6, 7)")
                .functions(eta)
                .build();
        final var result = exp.validate(false);
        Assertions.assertTrue(result.isValid());
    }

    @Test
    void testValidateFunctionWithSevenArgumentsAndOperator() {
        final var exp = new ExpressionBuilder("eta(1, 2, 3, 4, 5, 6, 7) * 2 * 3 * 4")
                .functions(eta)
                .build();
        final var result = exp.validate(false);
        Assertions.assertTrue(result.isValid());
    }

    // invalid scenarios

    @Test
    void testValidateInvalidFunction() {
        final var exp = new ExpressionBuilder("sin()")
                .build();
        final var result = exp.validate(false);
        Assertions.assertFalse(result.isValid());
    }

    @Test
    void testValidateInvalidOperand() {
        final var exp = new ExpressionBuilder("1 + ")
                .build();
        final var result = exp.validate(false);
        Assertions.assertFalse(result.isValid());
    }

    @Test
    void testValidateInvalidFunctionWithTooFewArguments() {
        final var exp = new ExpressionBuilder("beta(1)")
                .functions(beta)
                .build();
        final var result = exp.validate(false);
        Assertions.assertFalse(result.isValid());
    }

    @Test
    void testValidateInvalidFunctionWithTooFewArgumentsAndOperands() {
        final var exp = new ExpressionBuilder("beta(1 + )")
                .functions(beta)
                .build();
        final var result = exp.validate(false);
        Assertions.assertFalse(result.isValid());
    }

    @Test
    void testValidateInvalidFunctionWithManyArguments() {
        final var exp = new ExpressionBuilder("beta(1, 2, 3)")
                .functions(beta)
                .build();
        final var result = exp.validate(false);
        Assertions.assertFalse(result.isValid());
    }

    @Test
    void testValidateInvalidOperator() {
        final var exp = new ExpressionBuilder("+")
                .build();
        final var result = exp.validate(false);
        Assertions.assertFalse(result.isValid());
    }

    // Thanks go out to werwiesel for reporting the issue
    // https://github.com/fasseg/exp4j/issues/59
    @Test
    void testNoArgFunctionValidation() {
        final var now = new Function("now", 0) {
            @Override
            public BigDecimal apply(final BigDecimal... args) {
                return BigDecimal.valueOf(new Date().getTime());
            }
        };
        var e = new ExpressionBuilder("14*now()")
                .function(now)
                .build();
        Assertions.assertTrue(e.validate().isValid());

        e = new ExpressionBuilder("now()")
                .function(now)
                .build();
        Assertions.assertTrue(e.validate().isValid());

        e = new ExpressionBuilder("sin(now())")
                .function(now)
                .build();
        Assertions.assertTrue(e.validate().isValid());

        e = new ExpressionBuilder("sin(now()) % 14")
                .function(now)
                .build();
        Assertions.assertTrue(e.validate().isValid());
    }

}
