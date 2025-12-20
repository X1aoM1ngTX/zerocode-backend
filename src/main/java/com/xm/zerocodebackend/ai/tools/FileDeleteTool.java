package com.xm.zerocodebackend.ai.tools;

import com.xm.zerocodebackend.constant.AppConstant;
import com.xm.zerocodebackend.service.AppService;
import com.xm.zerocodebackend.model.entity.App;

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
 * 文件删除工具
 * 支持 AI 通过工具调用的方式删除文件
 */
@Slf4j
@Component
public class FileDeleteTool extends BaseTool {

    @Resource
    @Lazy
    private AppService appService;

    @Tool("删除指定路径的文件")
    public String deleteFile(
            @P("文件的相对路径") String relativeFilePath,
            @ToolMemoryId Long appId) {
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
            if (!Files.exists(path)) {
                return "警告：文件不存在，无需删除 - " + relativeFilePath;
            }
            if (!Files.isRegularFile(path)) {
                return "错误：指定路径不是文件，无法删除 - " + relativeFilePath;
            }
            // 安全检查：避免删除重要文件
            String fileName = path.getFileName().toString();
            if (isImportantFile(fileName)) {
                return "错误：不允许删除重要文件 - " + fileName;
            }
            Files.delete(path);
            log.info("成功删除文件: {}", path.toAbsolutePath());
            return "文件删除成功: " + relativeFilePath;
        } catch (IOException e) {
            String errorMessage = "删除文件失败: " + relativeFilePath + ", 错误: " + e.getMessage();
            log.error(errorMessage, e);
            return errorMessage;
        }
    }

    /**
     * 判断是否是重要文件，不允许删除
     */
    private boolean isImportantFile(String fileName) {
        String[] importantFiles = {
                "package.json", "package-lock.json", "yarn.lock", "pnpm-lock.yaml",
                "vite.config.js", "vite.config.ts", "vue.config.js",
                "tsconfig.json", "tsconfig.app.json", "tsconfig.node.json",
                "index.html", "main.js", "main.ts", "App.vue", ".gitignore", "README.md"
        };
        for (String important : importantFiles) {
            if (important.equalsIgnoreCase(fileName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getToolName() {
        return "deleteFile";
    }

    @Override
    public String getDisplayName() {
        return "文件删除工具";
    }

    @Override
    public String getIconName() {
        return "DeleteOutlined";
    }

    @Override
    public String generateToolExecutedResult(JSONObject arguments) {
        String relativeFilePath = arguments.getStr("relativeFilePath");
        return String.format("<SettingOutlined /> [工具调用] %s %s", getDisplayName(), relativeFilePath);
    }
}
