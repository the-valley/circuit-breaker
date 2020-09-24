package com.wy.counter;

import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author wy
 * @date 2020/9/23
 * @description 用队列实现窗口
 */
public class SlidingWindowCounter {
    /**
     * 窗口队列，容量根据熔断器参数设定，如果程序执行成功则保存1，否则保存0
     */
    private LinkedBlockingDeque<Integer> window;

    public SlidingWindowCounter(Integer windowSize) {
        this.window = new LinkedBlockingDeque<>(windowSize);
    }

    /**
     * 窗口移动
     * @param executeStatus 执行状态
     * @return 是否成功添加
     */
    public boolean add(ExecuteStatusEnum executeStatus) {
        // 添加元素前先判断队列是否已满，满需要把最开始入列的元素删除
        if (window.offer(executeStatus.getCode())) {
            window.poll();
            return window.offer(executeStatus.getCode());
        }
        return false;
    }

    /**
     * 计算队列中失败的个数
     */
    public Integer countFailure() {
        int sum = 0;
        for (Integer item : window) {
            sum += item;
        }
        return sum;
    }

    /**
     * 重置窗口
     */
    public void reset() {
        window.clear();
    }
}
