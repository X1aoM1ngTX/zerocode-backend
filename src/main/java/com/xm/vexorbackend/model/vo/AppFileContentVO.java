package com.xm.vexorbackend.model.vo;

import lombok.Data;

/**
 * 应用文件内容。
 */
@Data
public class AppFileContentVO {

    /** 文件相对路径 */
    private String path;

    /** 文件名 */
    private String name;

    /** 编程语言类型（用于语法高亮） */
    private String language;

    /** 文件内容 */
    private String content;
}
