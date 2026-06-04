package com.xm.vexorbackend.model.vo;

import lombok.Data;

import java.util.List;

/**
 * 应用文件树节点。
 */
@Data
public class AppFileNodeVO {

    /** 文件或目录名称 */
    private String name;

    /** 相对于应用代码根目录的路径 */
    private String path;

    /** 节点类型（file / directory） */
    private String type;

    /** 编程语言类型（仅文件节点，用于语法高亮） */
    private String language;

    /** 生成状态（generating / done / error） */
    private String status;

    /** 子节点列表（仅目录节点有值） */
    private List<AppFileNodeVO> children;
}
