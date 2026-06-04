package com.xm.vexorbackend.core.handler;

import cn.hutool.core.util.StrUtil;
import com.xm.vexorbackend.ai.model.message.AppGenerationMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * 将 AI 的 file:path 代码块流拆分成文本事件和文件事件。
 */
public class FileBlockStreamParser {

    /** 文件代码块前缀，格式为 ```file:相对路径 */
    private static final String FILE_BLOCK_PREFIX = "```file:";

    private final Long appId;

    /** 等待按行分割的原始数据缓冲区 */
    private final StringBuilder pendingBuffer = new StringBuilder();

    /** 普通文本内容缓冲区 */
    private final StringBuilder textBuffer = new StringBuilder();

    /** 当前文件块的内容缓冲区 */
    private final StringBuilder fileBuffer = new StringBuilder();

    /** 是否正在解析文件代码块 */
    private boolean inFileBlock = false;

    /** 当前正在生成的文件路径 */
    private String currentFilePath;

    public FileBlockStreamParser(Long appId) {
        this.appId = appId;
    }

    /**
     * 接收一块流式文本，返回解析出的事件列表
     * @param chunk 流式文本片段
     * @return 本次解析出的事件列表
     */
    public List<AppGenerationMessage> accept(String chunk) {
        pendingBuffer.append(chunk);
        return drain(false);
    }

    /**
     * 完成解析，刷出所有剩余缓冲区内容
     * @return 剩余的事件列表
     */
    public List<AppGenerationMessage> complete() {
        List<AppGenerationMessage> events = drain(true);
        if (inFileBlock && StrUtil.isNotBlank(currentFilePath)) {
            events.add(AppGenerationMessage.fileDelta(appId, currentFilePath, fileBuffer.toString(), true));
            events.add(AppGenerationMessage.fileDone(appId, currentFilePath));
            resetFileState();
        }
        flushText(events);
        return events;
    }

    /**
     * 按 \n 分割缓冲区并逐行处理
     * @param flushAll true 时将未以换行结尾的残余内容也一并处理
     */
    private List<AppGenerationMessage> drain(boolean flushAll) {
        List<AppGenerationMessage> events = new ArrayList<>();
        while (true) {
            int lineEnd = pendingBuffer.indexOf("\n");
            if (lineEnd < 0) {
                break;
            }
            String line = pendingBuffer.substring(0, lineEnd + 1);
            pendingBuffer.delete(0, lineEnd + 1);
            handleLine(line, events);
        }
        if (flushAll && !pendingBuffer.isEmpty()) {
            String line = pendingBuffer.toString();
            pendingBuffer.setLength(0);
            handleLine(line, events);
        }
        return events;
    }

    /** 逐行判断是普通文本还是文件代码块的开始/结束 */
    private void handleLine(String line, List<AppGenerationMessage> events) {
        String trimmedLine = line.strip();
        if (!inFileBlock) {
            if (isFileBlockStart(trimmedLine)) {
                flushText(events);
                currentFilePath = extractFilePath(trimmedLine);
                inFileBlock = true;
                fileBuffer.setLength(0);
                events.add(AppGenerationMessage.toolCall(appId, "正在生成 " + currentFilePath));
                events.add(AppGenerationMessage.fileStart(appId, currentFilePath));
            } else {
                textBuffer.append(line);
                flushText(events);
            }
            return;
        }

        if ("```".equals(trimmedLine)) {
            String content = trimSingleTrailingLineBreak(fileBuffer.toString());
            events.add(AppGenerationMessage.fileDelta(appId, currentFilePath, content, true));
            events.add(AppGenerationMessage.fileDone(appId, currentFilePath));
            events.add(AppGenerationMessage.toolCall(appId, currentFilePath + " 已生成"));
            resetFileState();
        } else {
            fileBuffer.append(line);
            events.add(AppGenerationMessage.fileDelta(appId, currentFilePath, fileBuffer.toString(), true));
        }
    }

    private boolean isFileBlockStart(String trimmedLine) {
        return trimmedLine.startsWith(FILE_BLOCK_PREFIX) && trimmedLine.length() > FILE_BLOCK_PREFIX.length();
    }

    private String extractFilePath(String trimmedLine) {
        return trimmedLine.substring(FILE_BLOCK_PREFIX.length()).trim();
    }

    private void flushText(List<AppGenerationMessage> events) {
        if (textBuffer.isEmpty()) {
            return;
        }
        String content = textBuffer.toString();
        textBuffer.setLength(0);
        if (StrUtil.isNotBlank(content)) {
            events.add(AppGenerationMessage.assistantMessage(appId, content));
        }
    }

    /** 重置文件块解析状态 */
    private void resetFileState() {
        inFileBlock = false;
        currentFilePath = null;
        fileBuffer.setLength(0);
    }

    /** 去掉末尾的一个换行符（\n 或 \r\n） */
    private String trimSingleTrailingLineBreak(String content) {
        if (content.endsWith("\r\n")) {
            return content.substring(0, content.length() - 2);
        }
        if (content.endsWith("\n")) {
            return content.substring(0, content.length() - 1);
        }
        return content;
    }
}
