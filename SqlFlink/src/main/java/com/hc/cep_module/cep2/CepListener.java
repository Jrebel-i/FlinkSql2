package com.hc.cep_module.cep2;

import org.apache.flink.cep.pattern.Pattern;

import java.io.Serializable;

//todo 1，定义一个接口，实现序列化
public interface CepListener<T> extends Serializable {
    //todo 初始化操作
    void init();

    //todo 根据传入的数据判断是否需要修改
    Boolean needChange(T element);

    //todo 当条件为true时候，返回一个新的Pattern对象
    Pattern<T,?> returnPattern(T flagElement) throws Exception;
}
