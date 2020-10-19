package com.wy.breaker;

import com.wy.counter.ExecuteStatusEnum;
import com.wy.state.State;

import java.util.concurrent.Callable;

/**
 * @author wy
 * @date 2020/9/23
 * @description
 */
public interface CircuitBreaker<T> {
    /**
     * 获取当前状态
     */
    State<T> getState();

    /**
     * 设置当前
     *
     * @param state 要被设置的状态
     */
    void setState(State<T> state);

    /**
     * 记录执行状态到队列中
     *
     * @param status
     */
    void recordStatus(ExecuteStatusEnum status);

    /**
     * 判断是否需要熔断，如果需要则进行状态切换
     *
     * @return
     */
    void check();

    /**
     * 执行被保护的代码块
     */
    T execute(Callable<T> callable);

    /**
     * 熔断器打开时的默认逻辑
     *
     * @return
     */
    T fallback();

    /**
     * 获得当前窗口中已经失败的数
     *
     * @return 失败数
     */
    Integer getFailureCount();

    /**
     * 获取失败阈值
     */
    Integer getFailureTh();

    /**
     * 设置失败阈值
     */
    void setFailureTh(Integer failureTh);

    long getDuration();

    void setDuration(long duration);

    /**
     * 重置计数器
     */
    void resetCounter();
}
