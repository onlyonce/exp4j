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

import org.junit.Test;

import java.math.BigDecimal;
import java.util.Formatter;
import java.util.Random;

public class PerformanceTest {

    private static final long BENCH_TIME = 2L;
    private static final String EXPRESSION = "log(x) - y * (sqrt(x^cos(y)))";

    @Test
    public void testBenches() throws Exception {
        final var sb = new StringBuilder();
        final var fmt = new Formatter(sb);
        fmt.format("+------------------------+---------------------------+--------------------------+%n");
        fmt.format("| %-22s | %-25s | %-24s |%n", "Implementation", "Calculations per Second", "Percentage of Math");
        fmt.format("+------------------------+---------------------------+--------------------------+%n");
        System.out.print(sb);
        sb.setLength(0);

        final var math = benchJavaMath();
        final var mathRate = (double) math / BENCH_TIME;
        fmt.format("| %-22s | %25.2f | %22.2f %% |%n", "Java Math", mathRate, 100f);
        System.out.print(sb);
        sb.setLength(0);

        final var db = benchDouble();
        final var dbRate = (double) db / BENCH_TIME;
        fmt.format("| %-22s | %25.2f | %22.2f %% |%n", "exp4j", dbRate, dbRate * 100 / mathRate);
        System.out.print(sb);
        sb.setLength(0);
    }

    private int benchDouble() {
        final var expression = new ExpressionBuilder(EXPRESSION)
                .variables("x", "y")
                .build();
        final var rnd = new Random();
        final var timeout = BENCH_TIME;
        final var time = System.currentTimeMillis() + (1000 * timeout);
        var count = 0;
        while (time > System.currentTimeMillis()) {
            expression.setVariable("x", BigDecimal.valueOf(rnd.nextDouble()));
            expression.setVariable("y", BigDecimal.valueOf(rnd.nextDouble()));
            final var val = expression.evaluate();
            count++;
        }
        final double rate = count / timeout;
        return count;
    }

    private int benchJavaMath() {
        final var time = System.currentTimeMillis() + (1000 * BENCH_TIME);
        double x, y, rate;
        var count = 0;
        final var rnd = new Random();
        while (time > System.currentTimeMillis()) {
            x = rnd.nextDouble();
            y = rnd.nextDouble();
            final var val = Math.log(x) - y * (Math.sqrt(Math.pow(x, Math.cos(y))));
            count++;
        }
        return count;
    }
}
