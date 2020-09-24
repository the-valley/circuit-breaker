package com.wy.counter;

import java.util.Objects;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author wy
 * @date 2020/9/23
 * @description 用队列实现窗口
 */
public class SlidingWindowCounter {
    /**
     * 窗口队列，容量根据熔断器参数设定，如果程序执行成功则保存1，否则保存0
     */
    private final LinkedBlockingDeque<Integer> window;

    private final LongAdder failureCounter = new LongAdder();

    public SlidingWindowCounter(Integer windowSize) {
        this.window = new LinkedBlockingDeque<>(windowSize);
    }

    /**
     * 窗口移动
     *
     * @param executeStatus 执行状态
     */
    public synchronized void add(ExecuteStatusEnum executeStatus) {
        // 添加元素前先判断队列是否已满，满需要把最开始入列的元素删除
        final boolean offer = window.offer(executeStatus.getCode());
        // 如果添加成功且添加的状态为失败，那失败数加1
        if (offer) {
            if (ExecuteStatusEnum.FAIL.equals(executeStatus)) {
                failureCounter.increment();
            }
        } else {
            final Integer poll = window.poll();
            // 如果取出的第一个元素不为null，且状态为失败那失败数减1
            if (!Objects.isNull(poll) && ExecuteStatusEnum.FAIL.getCode().equals(poll)) {
                failureCounter.decrement();
            }
            // 添加新的元素
            final boolean offer1 = window.offer(executeStatus.getCode());
            if (offer1 && ExecuteStatusEnum.FAIL.equals(executeStatus)) {
                failureCounter.increment();
            }
        }
    }

    /**
     * 计算队列中失败的个数
     */
    public synchronized Integer countFailure() {
        return failureCounter.intValue();
    }

    /**
     * 重置窗口
     */
    public void reset() {
        window.clear();
    }
}
