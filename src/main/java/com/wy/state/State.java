package com.wy.state;

import java.util.concurrent.Callable;

/**
 * @author wy
 * @date 2020/9/23
 * @description
 */
public interface State<T> {
    /**
     * 执行当前状态逻辑
     */
    T excute(Callable<T> callable);

    /**
     * 检查是否需要切换状态
     */
    void check();
}
