package com.wy.state;

import com.wy.breaker.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author wy
 * @date 2020/9/23
 * @description 熔断开启，服务不可用
 */
@Slf4j
public class OpenState<T> implements State<T> {

    public OpenState(CircuitBreaker<T> circuitBreaker, long duration) {
        CompletableFuture.runAsync(() -> {
            try {
                // 熔断持续 duration，切换到关闭状态
                TimeUnit.SECONDS.sleep(duration);
                log.info("方法已熔断 {} 秒，关闭断线器", duration);
                circuitBreaker.setState(new CloseState<>(circuitBreaker));
            } catch (InterruptedException e) {
                log.error("sleeping interrupted");
            }
        });
    }

    @Override
    public T excute(Callable<T> callable) {
        // 快速失败
        log.error("服务不可用");
        return null;
    }
}
