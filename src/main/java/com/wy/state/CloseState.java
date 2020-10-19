package com.wy.state;

import com.wy.breaker.CircuitBreaker;
import com.wy.counter.ExecuteStatusEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;

/**
 * @author wy
 * @date 2020/9/23
 * @description 熔断关闭，服务正常
 */
@Slf4j
public class CloseState<T> implements State<T> {

    /**
     * 熔断器
     */
    private final CircuitBreaker<T> circuitBreaker;



    public CloseState(CircuitBreaker<T> circuitBreaker) {
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    public T excute(Callable<T> callable) {
        try {
            final T result = callable.call();
            // 执行成功，向窗口中添加一个成功状态
            circuitBreaker.recordStatus(ExecuteStatusEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            log.error("调用方法异常", e);
            // 执行失败，向窗口中添加一个失败状态
            circuitBreaker.recordStatus(ExecuteStatusEnum.FAIL);
            // 如果添加了一个失败状态，需要判断是否开启熔断
            circuitBreaker.check();
            return null;
        }

    }

    @Override
    public void check() {
        // 判断已有的失败数量是不是达到阈值
        final Integer countFailure = circuitBreaker.getFailureCount();
        if (countFailure >=circuitBreaker.getFailureTh()) {
            log.info("失败率达到阈值，服务熔断");
            circuitBreaker.setState(new OpenState<>(circuitBreaker));
            // 切换到打开状态后，重置窗口
            circuitBreaker.resetCounter();
        }
    }
}
