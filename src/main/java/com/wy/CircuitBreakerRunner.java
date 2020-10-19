package com.wy;

import com.wy.breaker.CircuitBreaker;
import com.wy.breaker.FailureRateCircuitBreaker;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author wy
 * @date 2020/9/23
 * @description
 */
public class CircuitBreakerRunner {
    /**
     * 熔断策略的执行方法
     *
     * @param breaker  熔断器
     * @param callable 所保护的方法
     */
    public static <T> T run(CircuitBreaker<T> breaker, Callable<T> callable) {
        // 执行前先检查状态
        breaker.check();
        return breaker.execute(callable);
    }

    public static void main(String[] args) {

        CircuitBreaker<String> breaker = new FailureRateCircuitBreaker<>(10, 50, 10);

        final CompletableFuture<Void> task1 = CompletableFuture.runAsync(() -> {
            for (int i = 0; i < 100; i++) {
                final String result = CircuitBreakerRunner.run(breaker, () -> "hello");
                System.out.println(result);
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        final CompletableFuture<Void> task2 = CompletableFuture.runAsync(() -> {
            for (int i = 0; i < 100; i++) {
                final String result = CircuitBreakerRunner.run(breaker, () -> {
                    // 下面的语句会抛出异常
                    int result1 = 10 / 0;
                    return Integer.toString(result1);
                });
                System.out.println(result);
                try {
                    TimeUnit.SECONDS.sleep(4);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        CompletableFuture.allOf(task1, task2).join();
    }
}
