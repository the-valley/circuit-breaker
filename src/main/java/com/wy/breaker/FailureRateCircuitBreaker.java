package com.wy.breaker;

import com.wy.counter.ExecuteStatusEnum;
import com.wy.counter.SlidingWindowCounter;
import com.wy.state.CloseState;
import com.wy.state.OpenState;
import com.wy.state.State;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author wy
 * @date 2020/9/23
 * @description 失败率断线器
 */
@Slf4j
public class FailureRateCircuitBreaker<T> implements CircuitBreaker<T> {
    /**
     * 窗口内失败的次数
     */
    private Integer failureTh = 10;
    /**
     * 熔断时间，单位秒
     */
    private long duration = 60;

    /**
     * 窗口计数器
     */
    private SlidingWindowCounter windowCounter;

    private volatile State<T> state;

    /**
     * 读写锁，状态轮转时阻塞线程
     */
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

    public FailureRateCircuitBreaker(Integer failureTh, Integer windowsSize, long duration) {
        this.windowCounter = new SlidingWindowCounter(windowsSize);
        this.failureTh = failureTh;
        this.duration = duration;
        // 默认状态为熔断关闭
        this.state = new CloseState<>(this);
    }

    public FailureRateCircuitBreaker(Integer failureTh, Integer windowsSize, long duration, State<T> state) {
        this.failureTh = failureTh;
        this.windowCounter = new SlidingWindowCounter(windowsSize);
        this.duration = duration;
        this.state = state;
    }

    @Override
    public State<T> getState() {
        try {
            readLock.lock();
            return this.state;
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void setState(State<T> state) {
        try {
            writeLock.lock();
            this.state = state;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void recordStatus(ExecuteStatusEnum status) {
        windowCounter.add(status);
    }

    @Override
    public void check() {
        try {
            readLock.lock();
            this.state.check();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public T execute(Callable<T> callable) {
        return this.state.excute(callable);
    }

    @Override
    public T fallback() {
        log.error("服务不可用");
        return null;
    }

    @Override
    public Integer getFailureCount() {
        return windowCounter.countFailure();
    }

    @Override
    public Integer getFailureTh() {
        return failureTh;
    }

    @Override
    public void setFailureTh(Integer failureTh) {
        this.failureTh = failureTh;
    }

    @Override
    public long getDuration() {
        return duration;
    }

    @Override
    public void setDuration(long duration) {
        this.duration = duration;
    }

    @Override
    public void resetCounter() {
        windowCounter.reset();
    }
}
