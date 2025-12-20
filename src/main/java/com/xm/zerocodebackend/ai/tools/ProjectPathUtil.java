package com.xm.zerocodebackend.ai.tools;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.xm.zerocodebackend.constant.AppConstant;
import com.xm.zerocodebackend.service.AppService;
import com.xm.zerocodebackend.model.entity.App;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 项目路径工具类
 * 统一处理Vue和React项目的路径生成逻辑
 */
@Slf4j
@Component
public class ProjectPathUtil {

    @Resource
    private AppService appService;

    /**
     * 根据应用ID和相对路径获取完整路径
     *
     * @param appId 应用ID
     * @param relativeFilePath 相对文件路径
     * @return 完整的项目文件路径
     */
    public Path getProjectFilePath(Long appId, String relativeFilePath) {
        Path path = Paths.get(relativeFilePath);
        if (!path.isAbsolute()) {
            String projectDirName = getProjectDirName(appId);
            path = Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR, projectDirName, relativeFilePath);
        }
        return path;
    }

    /**
     * 根据应用ID和相对目录路径获取完整路径
     *
     * @param appId 应用ID
     * @param relativeDirPath 相对目录路径
     * @return 完整的项目目录路径
     */
    public Path getProjectDirPath(Long appId, String relativeDirPath) {
        String dirPath = relativeDirPath == null ? "" : relativeDirPath;
        Path path = Paths.get(dirPath);
        if (!path.isAbsolute()) {
            String projectDirName = getProjectDirName(appId);
            path = Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR, projectDirName, dirPath);
        }
        return path;
    }

    /**
     * 根据应用ID获取项目目录名称
     *
     * @param appId 应用ID
     * @return 项目目录名称
     */
    private String getProjectDirName(Long appId) {
        try {
            // 查询应用信息获取项目类型
            App app = appService.getById(appId);
            String projectType = "vue"; // 默认类型

            if (app != null && app.getCodeGenType() != null) {
                // 根据数据库中的codeGenType字段确定项目类型
                String codeGenType = app.getCodeGenType().toLowerCase();
                if (codeGenType.contains("react")) {
                    projectType = "react";
                } else {
                    // 默认使用vue
                    projectType = "vue";
                }
            } else {
                // 如果没有查询到应用信息，默认使用vue
                projectType = "vue";
            }

            return projectType + "_project_" + appId;
        } catch (Exception e) {
            log.error("获取项目目录名称失败，appId: {}, 错误: {}", appId, e.getMessage(), e);
            // 出错时默认返回vue项目目录
            return "vue_project_" + appId;
        }
    }
}