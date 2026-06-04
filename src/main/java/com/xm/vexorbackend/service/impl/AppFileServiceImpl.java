package com.xm.vexorbackend.service.impl;

import cn.hutool.core.util.StrUtil;
import com.xm.vexorbackend.constant.AppConstant;
import com.xm.vexorbackend.constant.UserConstant;
import com.xm.vexorbackend.exception.BusinessException;
import com.xm.vexorbackend.exception.ErrorCode;
import com.xm.vexorbackend.exception.ThrowUtils;
import com.xm.vexorbackend.model.entity.App;
import com.xm.vexorbackend.model.entity.User;
import com.xm.vexorbackend.model.enums.CodeGenTypeEnum;
import com.xm.vexorbackend.model.vo.AppFileContentVO;
import com.xm.vexorbackend.model.vo.AppFileNodeVO;
import com.xm.vexorbackend.service.AppFileService;
import com.xm.vexorbackend.service.AppService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * 应用生成文件服务实现。
 */
@Service
public class AppFileServiceImpl implements AppFileService {

    private static final Set<String> HIDDEN_DIR_NAMES = Set.of("node_modules", "dist", "build", ".git", ".idea",
            ".vscode");

    @Resource
    @Lazy
    private AppService appService;

    /**
     * 列出应用生成文件树
     * 
     * @param appId     应用 ID
     * @param loginUser 登录用户
     * @return List<AppFileNodeVO> 文件树列表
     */
    @Override
    public List<AppFileNodeVO> listFileTree(Long appId, User loginUser) {
        App app = getAndCheckApp(appId, loginUser);
        Path root = getRootPath(appId, app.getCodeGenType());
        if (!Files.exists(root) || !Files.isDirectory(root)) {
            return List.of();
        }
        return listChildren(root, root);
    }

    /**
     * 获取应用生成文件内容
     * 
     * @param appId     应用 ID
     * @param path      文件相对路径
     * @param loginUser 登录用户
     * @return AppFileContentVO 文件内容对象
     */
    @Override
    public AppFileContentVO getFileContent(Long appId, String path, User loginUser) {
        App app = getAndCheckApp(appId, loginUser);
        Path root = getRootPath(appId, app.getCodeGenType());
        Path filePath = resolveSafePath(root, path);
        ThrowUtils.throwIf(!Files.exists(filePath) || !Files.isRegularFile(filePath), ErrorCode.NOT_FOUND_ERROR,
                "文件不存在", "读取文件内容时文件不存在");
        try {
            AppFileContentVO vo = new AppFileContentVO();
            vo.setPath(normalizePath(root.relativize(filePath).toString()));
            vo.setName(filePath.getFileName().toString());
            vo.setLanguage(getLanguage(vo.getName()));
            vo.setContent(Files.readString(filePath, StandardCharsets.UTF_8));
            return vo;
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "读取文件失败", e.getMessage());
        }
    }

    /**
     * 读取文件内容（仅返回字符串，供代码编辑器使用）
     * @param appId 应用 ID
     * @param path 文件相对路径
     * @return 文件内容字符串，如果文件不存在或读取失败则返回空字符串
     */
    @Override
    public String readFileContent(Long appId, String path) {
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在", "读取文件内容时应用不存在");
        Path root = getRootPath(appId, app.getCodeGenType());
        Path filePath = resolveSafePath(root, path);
        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            return "";
        }
        try {
            return Files.readString(filePath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "";
        }
    }

    /**
     * 写入文件内容（供代码编辑器使用）
     * @param appId 应用 ID
     * @param codeGenType 代码生成类型
     * @param path 文件相对路径
     * @param content 文件内容字符串
     */
    @Override
    public void writeFile(Long appId, CodeGenTypeEnum codeGenType, String path, String content) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 错误", "应用 ID 错误");
        ThrowUtils.throwIf(codeGenType == null, ErrorCode.UNSUPPORTED_TYPE, "代码生成类型不能为空", "代码生成类型不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(path), ErrorCode.PARAMS_ERROR, "文件路径不能为空", "文件路径不能为空");
        Path root = getRootPath(appId, codeGenType.getValue());
        Path filePath = resolveSafePath(root, path);
        try {
            Path parent = filePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(filePath, content == null ? "" : content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "写入文件失败", e.getMessage());
        }
    }

    /**
     * 获取并校验应用信息，确保用户有权限访问
     * @param appId 应用 ID
     * @param loginUser 登录用户
     * @return App 应用对象
     */
    private App getAndCheckApp(Long appId, User loginUser) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 错误", "应用 ID 错误");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN, "用户未登录", "用户未登录");
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在", "应用不存在");
        boolean isOwner = app.getUserId() != null && app.getUserId().equals(loginUser.getId());
        boolean isAdmin = UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());
        ThrowUtils.throwIf(!isOwner && !isAdmin, ErrorCode.NO_AUTH, "无权限访问该应用文件", "无权限访问该应用文件");
        return app;
    }

    /**
     * 获取应用生成文件的根目录路径
     * @param appId 应用 ID
     * @param codeGenType 代码生成类型
     * @return 根目录路径
     */
    private Path getRootPath(Long appId, String codeGenType) {
        String type = StrUtil.blankToDefault(codeGenType, CodeGenTypeEnum.HTML.getValue());
        return Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR, type + "_" + appId).normalize().toAbsolutePath();
    }

    /**
     * 安全地解析文件路径，防止路径穿越攻击
     * @param root 根目录路径
     * @param relativePath 文件相对路径
     * @return 解析后的绝对路径
     */
    private Path resolveSafePath(Path root, String relativePath) {
        ThrowUtils.throwIf(StrUtil.isBlank(relativePath), ErrorCode.PARAMS_ERROR, "文件路径不能为空", "文件路径不能为空");
        ThrowUtils.throwIf(relativePath.contains(".."), ErrorCode.PARAMS_ERROR, "文件路径非法", "文件路径非法");
        Path inputPath = Paths.get(relativePath);
        ThrowUtils.throwIf(inputPath.isAbsolute(), ErrorCode.PARAMS_ERROR, "文件路径非法", "文件路径不能是绝对路径");
        Path resolvedPath = root.resolve(inputPath).normalize().toAbsolutePath();
        ThrowUtils.throwIf(!resolvedPath.startsWith(root), ErrorCode.PARAMS_ERROR, "文件路径非法", "文件路径越界");
        return resolvedPath;
    }

    /**
     * 列出目录下的子文件和子目录，过滤掉隐藏目录，并将结果转换为 AppFileNodeVO 对象列表
     * @param root 根目录路径（用于计算相对路径）
     * @param dir 当前目录路径
     * @return AppFileNodeVO 对象列表，表示当前目录下的文件和子目录
     */
    private List<AppFileNodeVO> listChildren(Path root, Path dir) {
        try {
            List<AppFileNodeVO> nodes = new ArrayList<>();
            try (var stream = Files.list(dir)) {
                stream.filter(path -> !shouldHide(path))
                        .sorted(Comparator.comparing(path -> Files.isRegularFile(path)))
                        .forEach(path -> nodes.add(toNode(root, path)));
            }
            return nodes;
        } catch (IOException e) {
            return List.of();
        }
    }

    /**
     * 将文件或目录路径转换为 AppFileNodeVO 对象
     * @param root 根目录路径（用于计算相对路径）
     * @param path 文件或目录路径
     * @return AppFileNodeVO 对象，包含文件或目录的名称、相对路径、类型（文件或目录）、语言（仅文件）和子节点（仅目录）
     */
    private AppFileNodeVO toNode(Path root, Path path) {
        AppFileNodeVO node = new AppFileNodeVO();
        node.setName(path.getFileName().toString());
        node.setPath(normalizePath(root.relativize(path).toString()));
        node.setStatus("done");
        if (Files.isDirectory(path)) {
            node.setType("directory");
            node.setChildren(listChildren(root, path));
        } else {
            node.setType("file");
            node.setLanguage(getLanguage(node.getName()));
        }
        return node;
    }

    /**
     * 判断是否应该隐藏该路径（如果是目录且名称在隐藏目录列表中）
     * @param path 文件或目录路径
     * @return true 如果应该隐藏，false 如果不应该隐藏
     */
    private boolean shouldHide(Path path) {
        String name = path.getFileName().toString();
        return Files.isDirectory(path) && HIDDEN_DIR_NAMES.contains(name);
    }

    /**
     * 规范化路径字符串，将反斜杠替换为斜杠，确保路径格式统一
     * @param path 原始路径字符串
     * @return 规范化后的路径字符串
     */
    private String normalizePath(String path) {
        return path.replace("\\", "/");
    }

    /**
     * 根据文件名获取编程语言类型，用于代码编辑器的语法高亮
     * @param fileName 文件名（包含扩展名）
     * @return 语言类型字符串，例如 "javascript", "html", "css" 等，如果无法识别则返回扩展名小写
     */
    private String getLanguage(String fileName) {
        String suffix = "";
        int index = fileName.lastIndexOf('.');
        if (index >= 0 && index < fileName.length() - 1) {
            suffix = fileName.substring(index + 1).toLowerCase();
        }
        return switch (suffix) {
            case "html", "htm" -> "html";
            case "css" -> "css";
            case "js", "mjs", "cjs" -> "javascript";
            case "ts" -> "typescript";
            case "vue" -> "vue";
            case "jsx" -> "jsx";
            case "tsx" -> "tsx";
            case "json" -> "json";
            case "md" -> "markdown";
            default -> suffix;
        };
    }
}
