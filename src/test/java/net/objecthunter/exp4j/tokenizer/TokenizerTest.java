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
package net.objecthunter.exp4j.tokenizer;

import net.objecthunter.exp4j.function.Function;
import net.objecthunter.exp4j.operator.Operator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static net.objecthunter.exp4j.TestUtil.*;

class TokenizerTest {

  @Test
  void testTokenization1() {
    final var tokenizer = new Tokenizer("1.222331", null, null, null);
    assertNumberToken(tokenizer.nextToken(), 1.222331d);
  }

  @Test
  void testTokenization2() {
    final var tokenizer = new Tokenizer(".222331", null, null, null);
    assertNumberToken(tokenizer.nextToken(), .222331d);
  }

  @Test
  void testTokenization3() {
    final var tokenizer = new Tokenizer("3e2", null, null, null);
    assertNumberToken(tokenizer.nextToken(), 300d);
  }
 
  @Test
  void testTokenization4() {
    final var tokenizer = new Tokenizer("3+1", null, null, null);

    Assertions.assertTrue(tokenizer.hasNext());
    assertNumberToken(tokenizer.nextToken(), 3d);

    Assertions.assertTrue(tokenizer.hasNext());
    assertOperatorToken(tokenizer.nextToken(), "+", 2, Operator.PRECEDENCE_ADDITION);

    Assertions.assertTrue(tokenizer.hasNext());
    assertNumberToken(tokenizer.nextToken(), 1d);

    Assertions.assertFalse(tokenizer.hasNext());
  }

  @Test
  void testTokenization5() {
    final var tokenizer = new Tokenizer("+3", null, null, null);

    Assertions.assertTrue(tokenizer.hasNext());
    assertOperatorToken(tokenizer.nextToken(), "+", 1, Operator.PRECEDENCE_UNARY_PLUS);

    Assertions.assertTrue(tokenizer.hasNext());
    assertNumberToken(tokenizer.nextToken(), 3d);

    Assertions.assertFalse(tokenizer.hasNext());
  }

  @Test
  void testTokenization6() {
    final var tokenizer = new Tokenizer("-3", null, null, null);

    Assertions.assertTrue(tokenizer.hasNext());
    assertOperatorToken(tokenizer.nextToken(), "-", 1, Operator.PRECEDENCE_UNARY_MINUS);

    Assertions.assertTrue(tokenizer.hasNext());
    assertNumberToken(tokenizer.nextToken(), 3d);

    Assertions.assertFalse(tokenizer.hasNext());
  }

  @Test
  void testTokenization7() {
    final var tokenizer = new Tokenizer("---++-3", null, null, null);

    Assertions.assertTrue(tokenizer.hasNext());
    assertOperatorToken(tokenizer.nextToken(), "-", 1, Operator.PRECEDENCE_UNARY_MINUS);

    Assertions.assertTrue(tokenizer.hasNext());
    assertOperatorToken(tokenizer.nextToken(), "-", 1, Operator.PRECEDENCE_UNARY_MINUS);

    Assertions.assertTrue(tokenizer.hasNext());
    assertOperatorToken(tokenizer.nextToken(), "-", 1, Operator.PRECEDENCE_UNARY_MINUS);

    Assertions.assertTrue(tokenizer.hasNext());
    assertOperatorToken(tokenizer.nextToken(), "+", 1, Operator.PRECEDENCE_UNARY_PLUS);

    Assertions.assertTrue(tokenizer.hasNext());
    assertOperatorToken(tokenizer.nextToken(), "+", 1, Operator.PRECEDENCE_UNARY_PLUS);

    Assertions.assertTrue(tokenizer.hasNext());
    assertOperatorToken(tokenizer.nextToken(), "-", 1, Operator.PRECEDENCE_UNARY_MINUS);

    Assertions.assertTrue(tokenizer.hasNext());
    assertNumberToken(tokenizer.nextToken(), 3d);

    Assertions.assertFalse(tokenizer.hasNext());
  }

  @Test
  void testTokenization8() {
    final var tokenizer = new Tokenizer("---++-3.004", null, null, null);

    Assertions.assertTrue(tokenizer.hasNext());
    assertOperatorToken(tokenizer.nextToken(), "-", 1, Operator.PRECEDENCE_UNARY_MINUS);

    Assertions.assertTrue(tokenizer.hasNext());
    assertOperatorToken(tokenizer.nextToken(), "-", 1, Operator.PRECEDENCE_UNARY_MINUS);

    Assertions.assertTrue(tokenizer.hasNext());
    assertOperatorToken(tokenizer.nextToken(), "-", 1, Operator.PRECEDENCE_UNARY_MINUS);

    Assertions.assertTrue(tokenizer.hasNext());
    assertOperatorToken(tokenizer.nextToken(), "+", 1, Operator.PRECEDENCE_UNARY_PLUS);

    Assertions.assertTrue(tokenizer.hasNext());
    assertOperatorToken(tokenizer.nextToken(), "+", 1, Operator.PRECEDENCE_UNARY_PLUS);

    Assertions.assertTrue(tokenizer.hasNext());
    assertOperatorToken(tokenizer.nextToken(), "-", 1, Operator.PRECEDENCE_UNARY_MINUS);

    Assertions.assertTrue(tokenizer.hasNext());
    assertNumberToken(tokenizer.nextToken(), 3.004d);

    Assertions.assertFalse(tokenizer.hasNext());
  }

  @Test
  void testTokenization9() {
    final var tokenizer = new Tokenizer("3+-1", null, null, null);

    Assertions.assertTrue(tokenizer.hasNext());
    assertNumberToken(tokenizer.nextToken(), 3d);

    Assertions.assertTrue(tokenizer.hasNext());
    assertOperatorToken(tokenizer.nextToken(), "+", 2, Operator.PRECEDENCE_ADDITION);

    Assertions.assertTrue(tokenizer.hasNext());
    assertOperatorToken(tokenizer.nextToken(), "-", 1, Operator.PRECEDENCE_UNARY_MINUS);

    Assertions.assertTrue(tokenizer.hasNext());
    assertNumberToken(tokenizer.nextToken(), 1d);

    Assertions.assertFalse(tokenizer.hasNext());
  }

  @Test
  void testTokenization10() {
    final var tokenizer = new Tokenizer("3+-1-.32++2", null, null, null);

    Assertions.assertTrue(tokenizer.hasNext());
    assertNumberToken(tokenizer.nextToken(), 3d);

    Assertions.assertTrue(tokenizer.hasNext());
    assertOperatorToken(tokenizer.nextToken(), "+", 2, Operator.PRECEDENCE_ADDITION);

    Assertions.assertTrue(tokenizer.hasNext());
    assertOperatorToken(tokenizer.nextToken(), "-", 1, Operator.PRECEDENCE_UNARY_MINUS);

    Assertions.assertTrue(tokenizer.hasNext());
    assertNumberToken(tokenizer.nextToken(), 1d);

    Assertions.assertTrue(tokenizer.hasNext());
    assertOperatorToken(tokenizer.nextToken(), "-", 2, Operator.PRECEDENCE_SUBTRACTION);

    Assertions.assertTrue(tokenizer.hasNext());
    assertNumberToken(tokenizer.nextToken(), 0.32d);

    Assertions.assertTrue(tokenizer.hasNext());
    assertOperatorToken(tokenizer.nextToken(), "+", 2, Operator.PRECEDENCE_ADDITION);

    Assertions.assertTrue(tokenizer.hasNext());
    assertOperatorToken(tokenizer.nextToken(), "+", 1, Operator.PRECEDENCE_UNARY_PLUS);

    Assertions.assertTrue(tokenizer.hasNext());
    assertNumberToken(tokenizer.nextToken(), 2d);

    Assertions.assertFalse(tokenizer.hasNext());
  }

  @Test
  void testTokenization11() {
    final var tokenizer = new Tokenizer("2+", null, null, null);

    Assertions.assertTrue(tokenizer.hasNext());
    assertNumberToken(tokenizer.nextToken(), 2d);

    Assertions.assertTrue(tokenizer.hasNext());
    assertOperatorToken(tokenizer.nextToken(), "+", 2, Operator.PRECEDENCE_ADDITION);

    Assertions.assertFalse(tokenizer.hasNext());
  }

  @Test
  void testTokenization12() {
    final var tokenizer = new Tokenizer("log(1)", null, null, null);

    Assertions.assertTrue(tokenizer.hasNext());
    assertFunctionToken(tokenizer.nextToken(), "log", 1);

    Assertions.assertTrue(tokenizer.hasNext());
    assertOpenParenthesesToken(tokenizer.nextToken());

    Assertions.assertTrue(tokenizer.hasNext());
    assertNumberToken(tokenizer.nextToken(), 1d);

    Assertions.assertTrue(tokenizer.hasNext());
    assertCloseParenthesesToken(tokenizer.nextToken());

    Assertions.assertFalse(tokenizer.hasNext());
  }

  @Test
  void testTokenization13() {
    final var tokenizer = new Tokenizer("x", null, null, new HashSet<>(Collections.singletonList("x")));

    Assertions.assertTrue(tokenizer.hasNext());
    assertVariableToken(tokenizer.nextToken(), "x");

    Assertions.assertFalse(tokenizer.hasNext());
  }

  @Test
  void testTokenization14() {
    final var tokenizer = new Tokenizer("2*x-log(3)", null, null, new HashSet<>(Collections.singletonList("x")));

    Assertions.assertTrue(tokenizer.hasNext());
    assertNumberToken(tokenizer.nextToken(), 2d);

    Assertions.assertTrue(tokenizer.hasNext());
    assertOperatorToken(tokenizer.nextToken(), "*", 2, Operator.PRECEDENCE_MULTIPLICATION);

    Assertions.assertTrue(tokenizer.hasNext());
    assertVariableToken(tokenizer.nextToken(), "x");

    Assertions.assertTrue(tokenizer.hasNext());
    assertOperatorToken(tokenizer.nextToken(), "-", 2, Operator.PRECEDENCE_SUBTRACTION);

    Assertions.assertTrue(tokenizer.hasNext());
    assertFunctionToken(tokenizer.nextToken(), "log", 1);

    Assertions.assertTrue(tokenizer.hasNext());
    assertOpenParenthesesToken(tokenizer.nextToken());

    Assertions.assertTrue(tokenizer.hasNext());
    assertNumberToken(tokenizer.nextToken(), 3d);

    Assertions.assertTrue(tokenizer.hasNext());
    assertCloseParenthesesToken(tokenizer.nextToken());

    Assertions.assertFalse(tokenizer.hasNext());
  }

  @Test
  void testTokenization15() {
    final var tokenizer = new Tokenizer("2*xlog+log(3)", null, null, new HashSet<>(Collections.singletonList("xlog")));

    Assertions.assertTrue(tokenizer.hasNext());
    assertNumberToken(tokenizer.nextToken(), 2d);

    Assertions.assertTrue(tokenizer.hasNext());
    assertOperatorToken(tokenizer.nextToken(), "*", 2, Operator.PRECEDENCE_MULTIPLICATION);

    Assertions.assertTrue(tokenizer.hasNext());
    assertVariableToken(tokenizer.nextToken(), "xlog");

    Assertions.assertTrue(tokenizer.hasNext());
    assertOperatorToken(tokenizer.nextToken(), "+", 2, Operator.PRECEDENCE_ADDITION);

    Assertions.assertTrue(tokenizer.hasNext());
    assertFunctionToken(tokenizer.nextToken(), "log", 1);

    Assertions.assertTrue(tokenizer.hasNext());
    assertOpenParenthesesToken(tokenizer.nextToken());

    Assertions.assertTrue(tokenizer.hasNext());
    assertNumberToken(tokenizer.nextToken(), 3d);

    Assertions.assertTrue(tokenizer.hasNext());
    assertCloseParenthesesToken(tokenizer.nextToken());

    Assertions.assertFalse(tokenizer.hasNext());
  }

  @Test
  void testTokenization16() {
    final var tokenizer = new Tokenizer("2*x+-log(3)", null, null, new HashSet<>(Collections.singletonList("x")));

    Assertions.assertTrue(tokenizer.hasNext());
    assertNumberToken(tokenizer.nextToken(), 2d);

    Assertions.assertTrue(tokenizer.hasNext());
    assertOperatorToken(tokenizer.nextToken(), "*", 2, Operator.PRECEDENCE_MULTIPLICATION);

    Assertions.assertTrue(tokenizer.hasNext());
    assertVariableToken(tokenizer.nextToken(), "x");

    Assertions.assertTrue(tokenizer.hasNext());
    assertOperatorToken(tokenizer.nextToken(), "+", 2, Operator.PRECEDENCE_ADDITION);

    Assertions.assertTrue(tokenizer.hasNext());
    assertOperatorToken(tokenizer.nextToken(), "-", 1, Operator.PRECEDENCE_UNARY_MINUS);

    Assertions.assertTrue(tokenizer.hasNext());
    assertFunctionToken(tokenizer.nextToken(), "log", 1);

    Assertions.assertTrue(tokenizer.hasNext());
    assertOpenParenthesesToken(tokenizer.nextToken());

    Assertions.assertTrue(tokenizer.hasNext());
    assertNumberToken(tokenizer.nextToken(), 3d);

    Assertions.assertTrue(tokenizer.hasNext());
    assertCloseParenthesesToken(tokenizer.nextToken());

    Assertions.assertFalse(tokenizer.hasNext());
  }

  @Test
  void testTokenization17() {
    final var tokenizer = new Tokenizer("2 * x + -log(3)", null, null, new HashSet<>(Collections.singletonList("x")));

    Assertions.assertTrue(tokenizer.hasNext());
    assertNumberToken(tokenizer.nextToken(), 2d);

    Assertions.assertTrue(tokenizer.hasNext());
    assertOperatorToken(tokenizer.nextToken(), "*", 2, Operator.PRECEDENCE_MULTIPLICATION);

    Assertions.assertTrue(tokenizer.hasNext());
    assertVariableToken(tokenizer.nextToken(), "x");

    Assertions.assertTrue(tokenizer.hasNext());
    assertOperatorToken(tokenizer.nextToken(), "+", 2, Operator.PRECEDENCE_ADDITION);

    Assertions.assertTrue(tokenizer.hasNext());
    assertOperatorToken(tokenizer.nextToken(), "-", 1, Operator.PRECEDENCE_UNARY_MINUS);

    Assertions.assertTrue(tokenizer.hasNext());
    assertFunctionToken(tokenizer.nextToken(), "log", 1);

    Assertions.assertTrue(tokenizer.hasNext());
    assertOpenParenthesesToken(tokenizer.nextToken());

    Assertions.assertTrue(tokenizer.hasNext());
    assertNumberToken(tokenizer.nextToken(), 3d);

    Assertions.assertTrue(tokenizer.hasNext());
    assertCloseParenthesesToken(tokenizer.nextToken());

    Assertions.assertFalse(tokenizer.hasNext());
  }

  @Test
  void testTokenization18() {
    final var log2 = new Function("log2") {

      @Override
      public BigDecimal apply(final BigDecimal... args) {
        return BigDecimal.valueOf(Math.log(args[0].doubleValue()) / Math.log(2d));
      }
    };

    final Map<String, Function> funcs = new HashMap<>(1);
    funcs.put(log2.getName(), log2);
    final var tokenizer = new Tokenizer("log2(4)", funcs, null, null);

    Assertions.assertTrue(tokenizer.hasNext());
    assertFunctionToken(tokenizer.nextToken(), "log2", 1);

    Assertions.assertTrue(tokenizer.hasNext());
    assertOpenParenthesesToken(tokenizer.nextToken());

    Assertions.assertTrue(tokenizer.hasNext());
    assertNumberToken(tokenizer.nextToken(), 4d);

    Assertions.assertTrue(tokenizer.hasNext());
    assertCloseParenthesesToken(tokenizer.nextToken());

    Assertions.assertFalse(tokenizer.hasNext());
  }

  @Test
  void testTokenization19() {
    final var avg = new Function("avg", 2) {

      @Override
      public BigDecimal apply(final BigDecimal... args) {
        var sum = BigDecimal.ZERO;
        for (final var arg : args) {
          sum = sum.add(arg);
        }
        return sum.divide(BigDecimal.valueOf(args.length));
      }
    };
    final Map<String, Function> funcs = new HashMap<>(1);
    funcs.put(avg.getName(), avg);
    final var tokenizer = new Tokenizer("avg(1,2)", funcs, null, null);

    Assertions.assertTrue(tokenizer.hasNext());
    assertFunctionToken(tokenizer.nextToken(), "avg", 2);

    Assertions.assertTrue(tokenizer.hasNext());
    assertOpenParenthesesToken(tokenizer.nextToken());

    Assertions.assertTrue(tokenizer.hasNext());
    assertNumberToken(tokenizer.nextToken(), 1d);

    Assertions.assertTrue(tokenizer.hasNext());
    assertFunctionSeparatorToken(tokenizer.nextToken());

    Assertions.assertTrue(tokenizer.hasNext());
    assertNumberToken(tokenizer.nextToken(), 2d);

    Assertions.assertTrue(tokenizer.hasNext());
    assertCloseParenthesesToken(tokenizer.nextToken());

    Assertions.assertFalse(tokenizer.hasNext());
  }

  @Test
  void testTokenization20() {
    final var factorial = new Operator("!", 1, true, Operator.PRECEDENCE_POWER + 1) {
      @Override
      public BigDecimal apply(final BigDecimal... args) {
        return BigDecimal.ZERO;
      }
    };
    final Map<String, Operator> operators = new HashMap<>(1);
    operators.put(factorial.getSymbol(), factorial);

    final var tokenizer = new Tokenizer("2!", null, operators, null);

    Assertions.assertTrue(tokenizer.hasNext());
    assertNumberToken(tokenizer.nextToken(), 2d);

    Assertions.assertTrue(tokenizer.hasNext());
    assertOperatorToken(tokenizer.nextToken(), "!", factorial.getNumOperands(), factorial.getPrecedence());

    Assertions.assertFalse(tokenizer.hasNext());
  }

  @Test
  void testTokenization21() {
    final var tokenizer = new Tokenizer("log(x) - y * (sqrt(x^cos(y)))", null, null, new HashSet<>(Arrays.asList("x", "y")));

    Assertions.assertTrue(tokenizer.hasNext());
    assertFunctionToken(tokenizer.nextToken(), "log", 1);

    Assertions.assertTrue(tokenizer.hasNext());
    assertOpenParenthesesToken(tokenizer.nextToken());

    Assertions.assertTrue(tokenizer.hasNext());
    assertVariableToken(tokenizer.nextToken(), "x");

    Assertions.assertTrue(tokenizer.hasNext());
    assertCloseParenthesesToken(tokenizer.nextToken());

    Assertions.assertTrue(tokenizer.hasNext());
    assertOperatorToken(tokenizer.nextToken(), "-", 2, Operator.PRECEDENCE_SUBTRACTION);

    Assertions.assertTrue(tokenizer.hasNext());
    assertVariableToken(tokenizer.nextToken(), "y");

    Assertions.assertTrue(tokenizer.hasNext());
    assertOperatorToken(tokenizer.nextToken(), "*", 2, Operator.PRECEDENCE_MULTIPLICATION);

    Assertions.assertTrue(tokenizer.hasNext());
    assertOpenParenthesesToken(tokenizer.nextToken());

    Assertions.assertTrue(tokenizer.hasNext());
    assertFunctionToken(tokenizer.nextToken(), "sqrt", 1);

    Assertions.assertTrue(tokenizer.hasNext());
    assertOpenParenthesesToken(tokenizer.nextToken());

    Assertions.assertTrue(tokenizer.hasNext());
    assertVariableToken(tokenizer.nextToken(), "x");

    Assertions.assertTrue(tokenizer.hasNext());
    assertOperatorToken(tokenizer.nextToken(), "^", 2, Operator.PRECEDENCE_POWER);

    Assertions.assertTrue(tokenizer.hasNext());
    assertFunctionToken(tokenizer.nextToken(), "cos", 1);

    Assertions.assertTrue(tokenizer.hasNext());
    assertOpenParenthesesToken(tokenizer.nextToken());

    Assertions.assertTrue(tokenizer.hasNext());
    assertVariableToken(tokenizer.nextToken(), "y");

    Assertions.assertTrue(tokenizer.hasNext());
    assertCloseParenthesesToken(tokenizer.nextToken());

    Assertions.assertTrue(tokenizer.hasNext());
    assertCloseParenthesesToken(tokenizer.nextToken());

    Assertions.assertTrue(tokenizer.hasNext());
    assertCloseParenthesesToken(tokenizer.nextToken());

    Assertions.assertFalse(tokenizer.hasNext());
  }

  @Test
  void testTokenization22() {
    final var tokenizer = new Tokenizer("--2 * (-14)", null, null, null);

    Assertions.assertTrue(tokenizer.hasNext());
    assertOperatorToken(tokenizer.nextToken(), "-", 1, Operator.PRECEDENCE_UNARY_MINUS);

    Assertions.assertTrue(tokenizer.hasNext());
    assertOperatorToken(tokenizer.nextToken(), "-", 1, Operator.PRECEDENCE_UNARY_MINUS);

    Assertions.assertTrue(tokenizer.hasNext());
    assertNumberToken(tokenizer.nextToken(), 2d);

    Assertions.assertTrue(tokenizer.hasNext());
    assertOperatorToken(tokenizer.nextToken(), "*", 2, Operator.PRECEDENCE_MULTIPLICATION);

    Assertions.assertTrue(tokenizer.hasNext());
    assertOpenParenthesesToken(tokenizer.nextToken());

    Assertions.assertTrue(tokenizer.hasNext());
    assertOperatorToken(tokenizer.nextToken(), "-", 1, Operator.PRECEDENCE_UNARY_MINUS);

    Assertions.assertTrue(tokenizer.hasNext());
    assertNumberToken(tokenizer.nextToken(), 14d);

    Assertions.assertTrue(tokenizer.hasNext());
    assertCloseParenthesesToken(tokenizer.nextToken());

    Assertions.assertFalse(tokenizer.hasNext());
  }
}
