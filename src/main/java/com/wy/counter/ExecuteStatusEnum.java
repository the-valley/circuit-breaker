package com.wy.counter;

import lombok.Getter;

/**
 * @author wy
 * @date 2020/9/23
 * @description
 */
@Getter
public enum ExecuteStatusEnum {
    /**
     * 失败
     */
    FAIL(1),
    /**
     * 成功
     */
    SUCCESS(0)
    ;
    private final Integer code;

    ExecuteStatusEnum(Integer code) {
        this.code = code;
    }
}
