package com.xm.zerocodebackend.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

@Data
@Description("HTML 代码结果")
public class HtmlCodeResult {

    /**
     * HTML 代码
     */
    @Description("HTML 代码")
    private String htmlCode;

    /**
     * 描述
     */
    @Description("生成代码的描述")
    private String description;
}
