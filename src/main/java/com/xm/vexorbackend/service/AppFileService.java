package com.xm.vexorbackend.service;

import com.xm.vexorbackend.model.entity.User;
import com.xm.vexorbackend.model.enums.CodeGenTypeEnum;
import com.xm.vexorbackend.model.vo.AppFileContentVO;
import com.xm.vexorbackend.model.vo.AppFileNodeVO;

import java.util.List;

/**
 * 应用生成文件服务。
 */
public interface AppFileService {

    /**
     * 列出应用文件树
     * @param appId 应用ID
     * @param loginUser 当前登录用户
     * @return 文件树节点列表
     */
    List<AppFileNodeVO> listFileTree(Long appId, User loginUser);

    /**
     * 获取文件内容（含权限校验）
     * @param appId 应用ID
     * @param path 文件相对路径
     * @param loginUser 当前登录用户
     * @return 文件内容
     */
    AppFileContentVO getFileContent(Long appId, String path, User loginUser);

    /**
     * 读取文件内容（无权限校验，内部调用）
     * @param appId 应用ID
     * @param path 文件相对路径
     * @return 文件内容字符串
     */
    String readFileContent(Long appId, String path);

    /**
     * 写入文件
     * @param appId 应用ID
     * @param codeGenType 代码生成类型
     * @param path 文件相对路径
     * @param content 文件内容
     */
    void writeFile(Long appId, CodeGenTypeEnum codeGenType, String path, String content);
}
