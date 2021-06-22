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
import net.objecthunter.exp4j.function.Functions;
import net.objecthunter.exp4j.operator.Operator;
import net.objecthunter.exp4j.operator.Operators;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

public class Tokenizer {

    private final char[] expression;

    private final int expressionLength;

    private final Map<String, Function> userFunctions;

    private final Map<String, Operator> userOperators;

    private final Set<String> variableNames;

    private final boolean implicitMultiplication;

    private int pos = 0;

    private Token lastToken;


    public Tokenizer(final String expression, final Map<String, Function> userFunctions,
                     final Map<String, Operator> userOperators, final Set<String> variableNames, final boolean implicitMultiplication) {
        this.expression = expression.trim().toCharArray();
        this.expressionLength = this.expression.length;
        this.userFunctions = userFunctions;
        this.userOperators = userOperators;
        this.variableNames = variableNames;
        this.implicitMultiplication = implicitMultiplication;
    }

    public Tokenizer(final String expression, final Map<String, Function> userFunctions,
                     final Map<String, Operator> userOperators, final Set<String> variableNames) {
        this.expression = expression.trim().toCharArray();
        this.expressionLength = this.expression.length;
        this.userFunctions = userFunctions;
        this.userOperators = userOperators;
        this.variableNames = variableNames;
        this.implicitMultiplication = true;
    }

    public boolean hasNext() {
        return this.expression.length > pos;
    }

    public Token nextToken() {
        var ch = expression[pos];
        while (Character.isWhitespace(ch)) {
            ch = expression[++pos];
        }
        if (Character.isDigit(ch) || '.' == ch) {
            if (null != lastToken) {
                if (Token.TOKEN_NUMBER == lastToken.getType()) {
                    throw new IllegalArgumentException("Unable to parse char '" + ch + "' (Code:" + (int) ch + ") at [" + pos + "]");
                } else if (implicitMultiplication && (Token.TOKEN_OPERATOR != lastToken.getType()
                        && Token.TOKEN_PARENTHESES_OPEN != lastToken.getType()
                        && Token.TOKEN_FUNCTION != lastToken.getType()
                        && Token.TOKEN_SEPARATOR != lastToken.getType())) {
                    // insert an implicit multiplication token
                    lastToken = new OperatorToken(Operators.getBuiltinOperator('*', 2));
                    return lastToken;
                }
            }
            return parseNumberToken(ch);
        } else if (isArgumentSeparator(ch)) {
            return parseArgumentSeparatorToken();
        } else if (isOpenParentheses(ch)) {
            if (null != lastToken && implicitMultiplication &&
                    (Token.TOKEN_OPERATOR != lastToken.getType()
                            && Token.TOKEN_PARENTHESES_OPEN != lastToken.getType()
                            && Token.TOKEN_FUNCTION != lastToken.getType()
                            && Token.TOKEN_SEPARATOR != lastToken.getType())) {
                // insert an implicit multiplication token
                lastToken = new OperatorToken(Operators.getBuiltinOperator('*', 2));
                return lastToken;
            }
            return parseParentheses(true);
        } else if (isCloseParentheses(ch)) {
            return parseParentheses(false);
        } else if (Operator.isAllowedOperatorChar(ch)) {
            return parseOperatorToken(ch);
        } else if (isAlphabetic(ch) || '_' == ch) {
            // parse the name which can be a setVariable or a function
            if (null != lastToken && implicitMultiplication &&
                    (Token.TOKEN_OPERATOR != lastToken.getType()
                            && Token.TOKEN_PARENTHESES_OPEN != lastToken.getType()
                            && Token.TOKEN_FUNCTION != lastToken.getType()
                            && Token.TOKEN_SEPARATOR != lastToken.getType())) {
                // insert an implicit multiplication token
                lastToken = new OperatorToken(Operators.getBuiltinOperator('*', 2));
                return lastToken;
            }
            return parseFunctionOrVariable();

        }
        throw new IllegalArgumentException("Unable to parse char '" + ch + "' (Code:" + (int) ch + ") at [" + pos + "]");
    }

    private Token parseArgumentSeparatorToken() {
        this.pos++;
        this.lastToken = new ArgumentSeparatorToken();
        return lastToken;
    }

    private boolean isArgumentSeparator(final char ch) {
        return ',' == ch;
    }

    private Token parseParentheses(final boolean open) {
        if (open) {
            this.lastToken = new OpenParenthesesToken();
        } else {
            this.lastToken = new CloseParenthesesToken();
        }
        this.pos++;
        return lastToken;
    }

    private boolean isOpenParentheses(final char ch) {
        return '(' == ch || '{' == ch || '[' == ch;
    }

    private boolean isCloseParentheses(final char ch) {
        return ')' == ch || '}' == ch || ']' == ch;
    }

    private Token parseFunctionOrVariable() {
        final var offset = this.pos;
        int testPos;
        var lastValidLen = 1;
        Token lastValidToken = null;
        var len = 1;
        if (isEndOfExpression(offset)) {
            this.pos++;
        }
        testPos = offset + len - 1;
        while (!isEndOfExpression(testPos) &&
                isVariableOrFunctionCharacter(expression[testPos])) {
            final var name = new String(expression, offset, len);
            if (null != variableNames && variableNames.contains(name)) {
                lastValidLen = len;
                lastValidToken = new VariableToken(name);
            } else {
                final var f = getFunction(name);
                if (null != f) {
                    lastValidLen = len;
                    lastValidToken = new FunctionToken(f);
                }
            }
            len++;
            testPos = offset + len - 1;
        }
        if (null == lastValidToken) {
            throw new UnknownFunctionOrVariableException(new String(expression), pos, len);
        }
        pos += lastValidLen;
        lastToken = lastValidToken;
        return lastToken;
    }

    private Function getFunction(final String name) {
        Function f = null;
        if (null != this.userFunctions) {
            f = this.userFunctions.get(name);
        }
        if (null == f) {
            f = Functions.getBuiltinFunction(name);
        }
        return f;
    }

    private Token parseOperatorToken(final char firstChar) {
        final var offset = this.pos;
        var len = 1;
        final var symbol = new StringBuilder();
        Operator lastValid = null;
        symbol.append(firstChar);

        while (!isEndOfExpression(offset + len) && Operator.isAllowedOperatorChar(expression[offset + len])) {
            symbol.append(expression[offset + len++]);
        }

        while (0 < symbol.length()) {
            final var op = this.getOperator(symbol.toString());
            if (null == op) {
                symbol.setLength(symbol.length() - 1);
            } else {
                lastValid = op;
                break;
            }
        }

        pos += symbol.length();
        lastToken = new OperatorToken(lastValid);
        return lastToken;
    }

    private Operator getOperator(final String symbol) {
        Operator op = null;
        if (null != this.userOperators) {
            op = this.userOperators.get(symbol);
        }
        if (null == op && 1 == symbol.length()) {
            var argc = 2;
            if (null == lastToken) {
                argc = 1;
            } else {
                final var lastTokenType = lastToken.getType();
                if (Token.TOKEN_PARENTHESES_OPEN == lastTokenType || Token.TOKEN_SEPARATOR == lastTokenType) {
                    argc = 1;
                } else if (Token.TOKEN_OPERATOR == lastTokenType) {
                    final var lastOp = ((OperatorToken) lastToken).getOperator();
                    if (2 == lastOp.getNumOperands() || (1 == lastOp.getNumOperands() && !lastOp.isLeftAssociative())) {
                        argc = 1;
                    }
                }

            }
            op = Operators.getBuiltinOperator(symbol.charAt(0), argc);
        }
        return op;
    }

    private Token parseNumberToken(final char firstChar) {
        final var offset = this.pos;
        var len = 1;
        this.pos++;
        if (isEndOfExpression(offset + len)) {
            lastToken = new NumberToken(new BigDecimal(String.valueOf(firstChar)));
            return lastToken;
        }
        while (!isEndOfExpression(offset + len) &&
                isNumeric(expression[offset + len], 'e' == expression[offset + len - 1] ||
                        'E' == expression[offset + len - 1])) {
            len++;
            this.pos++;
        }
        // check if the e is at the end
        if ('e' == expression[offset + len - 1] || 'E' == expression[offset + len - 1]) {
            // since the e is at the end it's not part of the number and a rollback is necessary
            len--;
            pos--;
        }
        lastToken = new NumberToken(expression, offset, len);
        return lastToken;
    }

    private static boolean isNumeric(final char ch, final boolean lastCharE) {
        return Character.isDigit(ch) || '.' == ch || 'e' == ch || 'E' == ch ||
                (lastCharE && ('-' == ch || '+' == ch));
    }

    public static boolean isAlphabetic(final int codePoint) {
        return Character.isLetter(codePoint);
    }

    public static boolean isVariableOrFunctionCharacter(final int codePoint) {
        return isAlphabetic(codePoint) ||
                Character.isDigit(codePoint) ||
                '_' == codePoint ||
                '.' == codePoint;
    }

    private boolean isEndOfExpression(final int offset) {
        return this.expressionLength <= offset;
    }
}
