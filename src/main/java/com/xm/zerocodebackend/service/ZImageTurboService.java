package com.xm.zerocodebackend.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.xm.zerocodebackend.exception.BusinessException;
import com.xm.zerocodebackend.exception.ErrorCode;
import com.xm.zerocodebackend.model.dto.image.ImageGenerateRequest;
import com.xm.zerocodebackend.model.dto.image.ImageGenerateResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Z-Image-Turbo 图像生成服务
 * API 文档地址：https://bailian.console.aliyun.com/?tab=api#/api/?type=model&url=3002354
 *
 * @author <a href="https://github.com/X1aoM1ngTX">X1aoM1ngTX</a>
 */
@Slf4j
@Service
public class ZImageTurboService {

    private static final String API_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation";

    @Value("${dashscope.api-key}")
    private String apiKey;

    @Value("${dashscope.image-model:z-image-turbo}")
    private String imageModel;

    /**
     * 生成图像
     *
     * @param request 图像生成请求
     * @return 图像生成响应
     */
    public ImageGenerateResponse generateImage(ImageGenerateRequest request) {
        try {
            log.info("开始调用 Z-Image-Turbo 生成图像，提示词: {}", request.getUserPrompt());

            // 构建请求体
            Map<String, Object> requestBody = buildRequestBody(request);

            // 发送 HTTP 请求
            HttpResponse response = HttpRequest.post(API_URL)
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .body(JSONUtil.toJsonStr(requestBody))
                    .timeout(300000) // 5分钟超时
                    .execute();

            // 检查响应状态
            if (!response.isOk()) {
                log.error("API 调用失败，状态码: {}, 响应: {}", response.getStatus(), response.body());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "API 调用失败: " + response.getStatus());
            }

            // 解析响应
            return parseResponse(response.body());

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("生成图像时发生未知错误", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "生成图像失败: " + e.getMessage());
        }
    }

    /**
     * 构建请求体
     *
     * @param request 图像生成请求
     * @return 请求体 Map
     */
    private Map<String, Object> buildRequestBody(ImageGenerateRequest request) {
        Map<String, Object> requestBody = new HashMap<>();

        // 模型名称
        requestBody.put("model", imageModel);

        // 输入参数
        Map<String, Object> input = new HashMap<>();

        // 消息列表
        List<Map<String, Object>> messages = new ArrayList<>();
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");

        // 内容列表
        List<Map<String, String>> content = new ArrayList<>();
        Map<String, String> textContent = new HashMap<>();
        textContent.put("text", request.getUserPrompt());
        content.add(textContent);

        message.put("content", content);
        messages.add(message);
        input.put("messages", messages);
        requestBody.put("input", input);

        // 参数
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("prompt_extend", request.getPromptExtend() != null ? request.getPromptExtend() : false);
        parameters.put("negative_prompt", request.getNegativePrompt() != null ? request.getNegativePrompt() : "");
        parameters.put("size", request.getSize() != null ? request.getSize() : "1024*1536");

        if (request.getSeed() != null) {
            parameters.put("seed", request.getSeed());
        }

        requestBody.put("parameters", parameters);

        return requestBody;
    }

    /**
     * 解析 API 响应
     *
     * @param responseBody API 响应体
     * @return 图像生成响应
     */
    private ImageGenerateResponse parseResponse(String responseBody) {
        JSONObject jsonResponse = JSONUtil.parseObj(responseBody);

        // 检查是否有错误
        if (jsonResponse.containsKey("code")) {
            String code = jsonResponse.getStr("code");
            String message = jsonResponse.getStr("message");
            log.error("API 返回错误: code={}, message={}", code, message);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "API 返回错误: " + message);
        }

        // 解析输出
        JSONObject output = jsonResponse.getJSONObject("output");
        if (output == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "API 返回结果为空");
        }

        List<Map> choices = output.getBeanList("choices", Map.class);
        if (choices == null || choices.isEmpty()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "API 返回结果为空");
        }

        Map<String, Object> choice = choices.get(0);
        @SuppressWarnings("unchecked")
        Map<String, Object> message = (Map<String, Object>) choice.get("message");

        String imageUrl = null;
        String text = null;
        String reasoningContent = null;

        // 解析内容
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> contentList = (List<Map<String, Object>>) message.get("content");
        if (contentList != null) {
            for (Map<String, Object> item : contentList) {
                if (item.containsKey("image")) {
                    imageUrl = (String) item.get("image");
                } else if (item.containsKey("text")) {
                    text = (String) item.get("text");
                }
            }
        }

        // 解析推理内容
        reasoningContent = (String) message.get("reasoning_content");

        // 解析使用信息
        JSONObject usage = jsonResponse.getJSONObject("usage");
        Integer width = null;
        Integer height = null;
        Integer imageCount = null;

        if (usage != null) {
            width = usage.getInt("width");
            height = usage.getInt("height");
            imageCount = usage.getInt("image_count");
        }

        return ImageGenerateResponse.builder()
                .imageUrl(imageUrl)
                .text(text)
                .reasoningContent(reasoningContent)
                .width(width)
                .height(height)
                .imageCount(imageCount)
                .requestId(jsonResponse.getStr("request_id"))
                .build();
    }
}
