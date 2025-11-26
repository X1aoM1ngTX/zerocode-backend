package com.xm.zerocodebackend.core.parser;

public interface CodeParser<T> {

    /**
     * 解析代码内容
     *
     * @param codeContent 代码内容
     * @return 解析后的结果对象
     */
    T parseCode(String codeContent);

}
