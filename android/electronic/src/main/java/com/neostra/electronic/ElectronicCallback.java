package com.neostra.electronic;

/**
 * @author njmsir
 * Created by njmsir on 2019/5/23.
 */
public interface ElectronicCallback {
    /**
     * 电子秤称重回调方法
     *
     * @param weight       重量
     * @param weightStatus 称重状态码
     */
    void electronicStatus(String weight, String weightStatus);
}
