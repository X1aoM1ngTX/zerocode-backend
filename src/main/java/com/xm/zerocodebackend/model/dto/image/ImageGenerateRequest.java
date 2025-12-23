package com.xm.zerocodebackend.model.dto.image;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * Z-Image-Turbo 图像生成请求
 * API 文档地址：https://bailian.console.aliyun.com/?tab=api#/api/?type=model&url=3002354
 *
 * @author <a href="https://github.com/X1aoM1ngTX">X1aoM1ngTX</a>
 */
@Data
public class ImageGenerateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 提示词，用于描述期望生成的图像内容、风格和构图
     * 支持中英文，长度不超过800个字符
     */
    @NotBlank(message = "提示词不能为空")
    @Size(max = 800, message = "提示词长度不能超过800个字符")
    private String userPrompt;

    /**
     * 负向提示词，用于描述不希望在图像中出现的元素
     */
    private String negativePrompt;

    /**
     * 输出图像的分辨率，格式为"宽*高"
     * 默认值：1024*1536
     * 总像素范围限制：[512*512, 2048*2048]
     * 推荐分辨率范围：[1024*1024, 1536*1536]
     */
    private String size = "1024*1536";

    /**
     * 是否启用智能提示词改写
     * true: 开启智能改写，输出图像、优化后的文本提示词、思考过程
     * false: 关闭智能改写，输出图像和原始文本提示词
     * 注意：prompt_extend=true 时价格高于 false
     */
    private Boolean promptExtend = false;

    /**
     * 随机数种子，取值范围[0, 2147483647]
     * 使用相同的 seed 参数值可使生成内容保持相对稳定
     */
    @Min(value = 0, message = "种子值不能小于0")
    @Max(value = 2147483647, message = "种子值不能大于2147483647")
    private Integer seed;
}
