package com.xm.zerocodebackend.langgraph4j.tools;

import com.xm.zerocodebackend.langgraph4j.model.ImageResource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * Mermaid图表工具测试
 */
@SpringBootTest
public class MermaidDiagramToolTest {

    @Resource
    private MermaidDiagramTool mermaidDiagramTool;

    @Test
    public void testGenerateMermaidDiagram() {
        String mermaidCode = """
                graph TB
                    A[用户] --> B[前端应用]
                    B --> C[API网关]
                    C --> D[认证服务]
                    C --> E[博客服务]
                    C --> F[评论服务]
                    E --> G[数据库]
                    E --> H[Redis缓存]
                    F --> G
                    F --> H
                    E --> I[文件存储]
                """;

        String description = "技术博客系统架构图";

        try {
            List<ImageResource> result = mermaidDiagramTool.generateMermaidDiagram(mermaidCode, description);

            System.out.println("生成的架构图数量: " + result.size());
            for (ImageResource imageResource : result) {
                System.out.println("图片URL: " + imageResource.getUrl());
                System.out.println("图片描述: " + imageResource.getDescription());
                System.out.println("图片分类: " + imageResource.getCategory());
            }
        } catch (Exception e) {
            System.err.println("生成架构图时发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
}