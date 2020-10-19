package com.wy.state;

import com.wy.breaker.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;

/**
 * @author wy
 * @date 2020/9/23
 * @description 熔断开启，服务不可用
 */
@Slf4j
public class OpenState<T> implements State<T> {

    /**
     * 熔断器
     */
    private final CircuitBreaker<T> circuitBreaker;

    /**
     * 记录断线器打开的时间
     */
    private volatile long openStartTime = 0;

    public OpenState(CircuitBreaker<T> circuitBreaker) {
        this.circuitBreaker = circuitBreaker;
//        CompletableFuture.runAsync(() -> {
//            try {
//                // 熔断持续 duration，切换到关闭状态
//                TimeUnit.SECONDS.sleep(duration);
//                log.info("方法已熔断 {} 秒，关闭断线器", duration);
//                circuitBreaker.setState(new CloseState<>(circuitBreaker));
//            } catch (InterruptedException e) {
//                log.error("sleeping interrupted");
//            }
//        });
        this.openStartTime = System.currentTimeMillis();
    }

    @Override
    public void check() {
        final long now = System.currentTimeMillis();
        // 如果超过开启时间，则关闭熔断器，服务可用
        if (now - openStartTime > circuitBreaker.getDuration()) {
            circuitBreaker.setState(new CloseState<>(circuitBreaker));
            log.info("熔断器关闭，服务连通");
        }
    }

    @Override
    public T excute(Callable<T> callable) {
        // 执行快速失败逻辑前，先检查是否超时
        final long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis - openStartTime > circuitBreaker.getDuration()) {
            circuitBreaker.setState(new CloseState<>(circuitBreaker));
        }
        return circuitBreaker.fallback();
    }
}
