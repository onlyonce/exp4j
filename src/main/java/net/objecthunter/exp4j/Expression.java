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
import net.objecthunter.exp4j.tokenizer.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class Expression {

    private final Token[] tokens;

    private final Map<String, BigDecimal> variables;

    private final Set<String> userFunctionNames;

    private static Map<String, BigDecimal> createDefaultVariables() {
        final var vars = new HashMap<String, BigDecimal>(4);
        vars.put("pi", BigDecimal.valueOf(Math.PI));
        vars.put("π", BigDecimal.valueOf(Math.PI));
        vars.put("φ", BigDecimal.valueOf(1.61803398874d));
        vars.put("e", BigDecimal.valueOf(Math.E));
        return vars;
    }

    /**
     * Creates a new expression that is a copy of the existing one.
     *
     * @param existing the expression to copy
     */
    public Expression(final Expression existing) {
        this.tokens = Arrays.copyOf(existing.tokens, existing.tokens.length);
        this.variables = new HashMap<>();
        this.variables.putAll(existing.variables);
        this.userFunctionNames = new HashSet<>(existing.userFunctionNames);
    }

    Expression(final Token[] tokens) {
        this.tokens = tokens;
        this.variables = createDefaultVariables();
        this.userFunctionNames = Collections.emptySet();
    }

    Expression(final Token[] tokens, final Set<String> userFunctionNames) {
        this.tokens = tokens;
        this.variables = createDefaultVariables();
        this.userFunctionNames = userFunctionNames;
    }

    public Expression setVariable(final String name, final BigDecimal value) {
        this.checkVariableName(name);
        this.variables.put(name, value);
        return this;
    }

    public Expression setVariable(final String name, final double value) {
        this.checkVariableName(name);
        this.variables.put(name, BigDecimal.valueOf(value));
        return this;
    }

    public Expression setVariable(final String name, final int value) {
        this.checkVariableName(name);
        this.variables.put(name, BigDecimal.valueOf(value));
        return this;
    }

    private void checkVariableName(final String name) {
        if (this.userFunctionNames.contains(name) || null != Functions.getBuiltinFunction(name)) {
            throw new IllegalArgumentException("The variable name '" + name + "' is invalid. Since there exists a function with the same name");
        }
    }

    public Expression setVariables(final Map<String, BigDecimal> variables) {
        for (final var v : variables.entrySet()) {
            this.setVariable(v.getKey(), v.getValue());
        }
        return this;
    }

    public Expression clearVariables() {
        this.variables.clear();
        return this;
    }

    public Set<String> getVariableNames() {
        final Set<String> variables = new HashSet<>();
        for (final var t : tokens) {
            if (Token.TOKEN_VARIABLE == t.getType()) {
                variables.add(((VariableToken) t).getName());
            }
        }
        return variables;
    }

    public ValidationResult validate(final boolean checkVariablesSet) {
        final List<String> errors = new ArrayList<>(0);
        if (checkVariablesSet) {
            /* check that all vars have a value set */
            for (final var t : this.tokens) {
                if (Token.TOKEN_VARIABLE == t.getType()) {
                    final var var = ((VariableToken) t).getName();
                    if (!variables.containsKey(var)) {
                        errors.add("The setVariable '" + var + "' has not been set");
                    }
                }
            }
        }

        /* Check if the number of operands, functions and operators match.
           The idea is to increment a counter for operands and decrease it for operators.
           When a function occurs the number of available arguments has to be greater
           than or equals to the function's expected number of arguments.
           The count has to be larger than 1 at all times and exactly 1 after all tokens
           have been processed */
        var count = 0;
        for (final var tok : this.tokens) {
            switch (tok.getType()) {
                case Token.TOKEN_NUMBER:
                case Token.TOKEN_VARIABLE:
                    count++;
                    break;
                case Token.TOKEN_FUNCTION:
                    final var func = ((FunctionToken) tok).getFunction();
                    final var argsNum = func.getNumArguments();
                    if (argsNum > count) {
                        errors.add("Not enough arguments for '" + func.getName() + "'");
                    }
                    if (1 < argsNum) {
                        count -= argsNum - 1;
                    } else if (0 == argsNum) {
                        // see https://github.com/fasseg/exp4j/issues/59
                        count++;
                    }
                    break;
                case Token.TOKEN_OPERATOR:
                    final var op = ((OperatorToken) tok).getOperator();
                    if (2 == op.getNumOperands()) {
                        count--;
                    }
                    break;
            }
            if (1 > count) {
                errors.add("Too many operators");
                return new ValidationResult(false, errors);
            }
        }
        if (1 < count) {
            errors.add("Too many operands");
        }
        return errors.isEmpty() ? ValidationResult.SUCCESS : new ValidationResult(false, errors);

    }

    public ValidationResult validate() {
        return validate(true);
    }

    public Future<BigDecimal> evaluateAsync(final ExecutorService executor) {
        return executor.submit(this::evaluate);
    }

    public BigDecimal evaluate() {
        final var output = new ArrayStack();
        for (final var t : tokens) {
            if (Token.TOKEN_NUMBER == t.getType()) {
                output.push(((NumberToken) t).getValue());
            } else if (Token.TOKEN_VARIABLE == t.getType()) {
                final var name = ((VariableToken) t).getName();
                final var value = this.variables.get(name);
                if (null == value) {
                    throw new IllegalArgumentException("No value has been set for the setVariable '" + name + "'.");
                }
                output.push(value);
            } else if (Token.TOKEN_OPERATOR == t.getType()) {
                final var op = (OperatorToken) t;
                if (output.size() < op.getOperator().getNumOperands()) {
                    throw new IllegalArgumentException("Invalid number of operands available for '" + op.getOperator().getSymbol() + "' operator");
                }
                if (2 == op.getOperator().getNumOperands()) {
                    /* pop the operands and push the result of the operation */
                    final var rightArg = output.pop();
                    final var leftArg = output.pop();
                    output.push(op.getOperator().apply(leftArg, rightArg));
                } else if (1 == op.getOperator().getNumOperands()) {
                    /* pop the operand and push the result of the operation */
                    final var arg = output.pop();
                    output.push(op.getOperator().apply(arg));
                }
            } else if (Token.TOKEN_FUNCTION == t.getType()) {
                final var func = (FunctionToken) t;
                final var numArguments = func.getFunction().getNumArguments();
                if (output.size() < numArguments) {
                    throw new IllegalArgumentException("Invalid number of arguments available for '" + func.getFunction().getName() + "' function");
                }
                /* collect the arguments from the stack */
                final var args = new BigDecimal[numArguments];
                for (var j = numArguments - 1; 0 <= j; j--) {
                    args[j] = output.pop();
                }
                output.push(func.getFunction().apply(args));
            }
        }
        if (1 < output.size()) {
            throw new IllegalArgumentException(
                    "Invalid number of items on the output queue. Might be caused by an invalid number of arguments for a function.");
        }
        return output.pop();
    }
}
