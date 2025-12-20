package com.xm.zerocodebackend.ai.tools;

import com.xm.zerocodebackend.service.AppService;
import com.xm.zerocodebackend.model.entity.App;
import com.xm.zerocodebackend.constant.AppConstant;

import cn.hutool.json.JSONObject;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件读取工具
 * 支持 AI 通过工具调用的方式读取文件内容
 */
@Slf4j
@Component
public class FileReadTool extends BaseTool {

    @Resource
    @Lazy
    private AppService appService;

    @Tool("读取指定路径的文件内容")
    public String readFile(
            @P("文件的相对路径")
            String relativeFilePath,
            @ToolMemoryId Long appId
    ) {
        try {
            Path path = Paths.get(relativeFilePath);
            if (!path.isAbsolute()) {
                // 查询应用信息获取项目类型
                App app = appService.getById(appId);
                String projectDirName;

                if (app != null && app.getCodeGenType() != null) {
                    // 根据数据库中的codeGenType字段确定项目类型
                    String codeGenType = app.getCodeGenType().toLowerCase();
                    if (codeGenType.contains("react")) {
                        projectDirName = "react_project_" + appId;
                    } else {
                        // 默认使用vue
                        projectDirName = "vue_project_" + appId;
                    }
                } else {
                    // 如果没有查询到应用信息，默认使用vue
                    projectDirName = "vue_project_" + appId;
                }

                path = Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR, projectDirName, relativeFilePath);
            }
            if (!Files.exists(path) || !Files.isRegularFile(path)) {
                return "错误：文件不存在或不是文件 - " + relativeFilePath;
            }
            return Files.readString(path);
        } catch (IOException e) {
            String errorMessage = "读取文件失败: " + relativeFilePath + ", 错误: " + e.getMessage();
            log.error(errorMessage, e);
            return errorMessage;
        }
    }

    @Override
    public String getToolName() {
        return "readFile";
    }

    @Override
    public String getDisplayName() {
        return "读取文件";
    }

    @Override
    public String getIconName() {
        return "EyeOutlined";
    }

    @Override
    public String generateToolExecutedResult(JSONObject arguments) {
        String relativeFilePath = arguments.getStr("relativeFilePath");
        return String.format("[工具调用] %s %s", getDisplayName(), relativeFilePath);
    }
}
