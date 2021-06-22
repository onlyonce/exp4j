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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class ConcurrencyTests {

  @Test
  void testFutureEvaluation() throws Exception {
    final var exec = Executors.newFixedThreadPool(10);
    final var numTests = 10000;
    final var correct1 = new BigDecimal[numTests];
    final var results1 = new Future[numTests];
 
    final var correct2 = new BigDecimal[numTests];
    final var results2 = new Future[numTests];

    for (var i = 0; numTests > i; i++) {
      correct1[i] = BigDecimal.valueOf(Math.sin(2 * Math.PI / (i + 1)));
      results1[i] = new ExpressionBuilder("sin(2pi/(n+1))")
              .variables("pi", "n")
              .build()
              .setVariable("pi", BigDecimal.valueOf(Math.PI))
              .setVariable("n", BigDecimal.valueOf(i))
              .evaluateAsync(exec);

      correct2[i] = BigDecimal.valueOf(Math.log(Math.E * Math.PI * (i + 1)));
      results2[i] = new ExpressionBuilder("log(epi(n+1))")
              .variables("pi", "n", "e")
              .build()
              .setVariable("pi", BigDecimal.valueOf(Math.PI))
              .setVariable("e", BigDecimal.valueOf(Math.E))
              .setVariable("n", BigDecimal.valueOf(i))
              .evaluateAsync(exec);
    }

    for (var i = 0; numTests > i; i++) {
      Assertions.assertEquals(correct1[i].doubleValue(), ((BigDecimal) (results1[i].get())).doubleValue(), 0d);
      Assertions.assertEquals(correct2[i].doubleValue(), ((BigDecimal) (results2[i].get())).doubleValue(), 0d);
    }
  }
}
