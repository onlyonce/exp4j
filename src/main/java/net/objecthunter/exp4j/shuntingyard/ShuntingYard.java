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

import net.objecthunter.exp4j.function.Function;
import net.objecthunter.exp4j.operator.Operator;
import net.objecthunter.exp4j.tokenizer.OperatorToken;
import net.objecthunter.exp4j.tokenizer.Token;
import net.objecthunter.exp4j.tokenizer.Tokenizer;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * Shunting yard implementation to convert infix to reverse polish notation
 */
public class ShuntingYard {

    /**
     * Convert a Set of tokens from infix to reverse polish notation
     *
     * @param expression             the expression to convert
     * @param userFunctions          the custom functions used
     * @param userOperators          the custom operators used
     * @param variableNames          the variable names used in the expression
     * @param implicitMultiplication set to false to turn off implicit multiplication
     * @return a {@link net.objecthunter.exp4j.tokenizer.Token} array containing the result
     */
    public static Token[] convertToRPN(final String expression, final Map<String, Function> userFunctions,
                                       final Map<String, Operator> userOperators, final Set<String> variableNames, final boolean implicitMultiplication) {
        final var stack = new Stack<Token>();
        final var output = new ArrayList<Token>();

        final var tokenizer = new Tokenizer(expression, userFunctions, userOperators, variableNames, implicitMultiplication);
        while (tokenizer.hasNext()) {
            final var token = tokenizer.nextToken();
            switch (token.getType()) {
                case Token.TOKEN_NUMBER:
                case Token.TOKEN_VARIABLE:
                    output.add(token);
                    break;
                case Token.TOKEN_FUNCTION:
                    stack.add(token);
                    break;
                case Token.TOKEN_SEPARATOR:
                    while (!stack.empty() && Token.TOKEN_PARENTHESES_OPEN != stack.peek().getType()) {
                        output.add(stack.pop());
                    }
                    if (stack.empty() || Token.TOKEN_PARENTHESES_OPEN != stack.peek().getType()) {
                        throw new IllegalArgumentException("Misplaced function separator ',' or mismatched parentheses");
                    }
                    break;
                case Token.TOKEN_OPERATOR:
                    while (!stack.empty() && Token.TOKEN_OPERATOR == stack.peek().getType()) {
                        final var o1 = (OperatorToken) token;
                        final var o2 = (OperatorToken) stack.peek();
                        if (1 == o1.getOperator().getNumOperands() && 2 == o2.getOperator().getNumOperands()) {
                            break;
                        } else if ((o1.getOperator().isLeftAssociative() && o1.getOperator().getPrecedence() <= o2.getOperator().getPrecedence())
                                || (o1.getOperator().getPrecedence() < o2.getOperator().getPrecedence())) {
                            output.add(stack.pop());
                        } else {
                            break;
                        }
                    }
                    stack.push(token);
                    break;
                case Token.TOKEN_PARENTHESES_OPEN:
                    stack.push(token);
                    break;
                case Token.TOKEN_PARENTHESES_CLOSE:
                    while (Token.TOKEN_PARENTHESES_OPEN != stack.peek().getType()) {
                        output.add(stack.pop());
                    }
                    stack.pop();
                    if (!stack.isEmpty() && Token.TOKEN_FUNCTION == stack.peek().getType()) {
                        output.add(stack.pop());
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unknown Token type encountered. This should not happen");
            }
        }
        while (!stack.empty()) {
            final var t = stack.pop();
            if (Token.TOKEN_PARENTHESES_CLOSE == t.getType() || Token.TOKEN_PARENTHESES_OPEN == t.getType()) {
                throw new IllegalArgumentException("Mismatched parentheses detected. Please check the expression");
            } else {
                output.add(t);
            }
        }
        return output.toArray(new Token[0]);
    }
}
