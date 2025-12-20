package com.xm.zerocodebackend.langgraph4j.node;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

import com.xm.zerocodebackend.core.builder.ReactProjectBuilder;
import com.xm.zerocodebackend.core.builder.VueProjectBuilder;
import com.xm.zerocodebackend.exception.BusinessException;
import com.xm.zerocodebackend.exception.ErrorCode;
import com.xm.zerocodebackend.model.enums.CodeGenTypeEnum;
import com.xm.zerocodebackend.utils.SpringContextUtil;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import com.xm.zerocodebackend.langgraph4j.state.WorkflowContext;

import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
public class ProjectBuilderNode {

    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 项目构建");

            // 获取必要的参数
            String generatedCodeDir = context.getGeneratedCodeDir();
            CodeGenTypeEnum generationType = context.getGenerationType();
            String buildResultDir;
            
            try {
                // 根据生成类型选择相应的构建器
                boolean buildSuccess = false;
                if (generationType == CodeGenTypeEnum.VUE_PROJECT) {
                    VueProjectBuilder vueBuilder = SpringContextUtil.getBean(VueProjectBuilder.class);
                    // 执行 Vue 项目构建（npm install + npm run build）
                    buildSuccess = vueBuilder.buildProject(generatedCodeDir);
                    if (buildSuccess) {
                        // 构建成功，返回 dist 目录路径
                        buildResultDir = generatedCodeDir + File.separator + "dist";
                        log.info("Vue 项目构建成功，dist 目录: {}", buildResultDir);
                    } else {
                        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Vue 项目构建失败");
                    }
                } else if (generationType == CodeGenTypeEnum.REACT_PROJECT) {
                    ReactProjectBuilder reactBuilder = SpringContextUtil.getBean(ReactProjectBuilder.class);
                    // 执行 React 项目构建（npm install + npm run build）
                    buildSuccess = reactBuilder.buildProject(generatedCodeDir);
                    if (buildSuccess) {
                        // 构建成功，返回 build 目录路径
                        buildResultDir = generatedCodeDir + File.separator + "build";
                        log.info("React 项目构建成功，build 目录: {}", buildResultDir);
                    } else {
                        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "React 项目构建失败");
                    }
                } else {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的生成类型: " + generationType.getValue());
                }
            } catch (Exception e) {
                log.error("项目构建异常: {}", e.getMessage(), e);
                buildResultDir = generatedCodeDir; // 异常时返回原路径
            }

            // 更新状态
            context.setCurrentStep("项目构建");
            context.setBuildResultDir(buildResultDir);
            log.info("项目构建节点完成，最终目录: {}", buildResultDir);
            return WorkflowContext.saveContext(context);
        });
    }
}
