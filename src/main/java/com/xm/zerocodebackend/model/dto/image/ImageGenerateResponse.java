package com.xm.zerocodebackend.model.dto.image;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Z-Image-Turbo 图像生成响应
 * API 文档地址：https://bailian.console.aliyun.com/?tab=api#/api/?type=model&url=3002354
 *
 * @author <a href="https://github.com/X1aoM1ngTX">X1aoM1ngTX</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageGenerateResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 生成图像的 URL，图像格式为PNG
     * 链接有效期为24小时，请及时下载并保存图像
     */
    private String imageUrl;

    /**
     * 提示词
     * 当 promptExtend=false 时，为输入的提示词
     * 当 promptExtend=true 时，为改写后的提示词
     */
    private String text;

    /**
     * 模型的思考过程，仅在 promptExtend=true 时返回思考文本
     */
    private String reasoningContent;

    /**
     * 生成图像的宽度（像素）
     */
    private Integer width;

    /**
     * 生成图像的高度（像素）
     */
    private Integer height;

    /**
     * 生成图像的数量，固定为1
     */
    private Integer imageCount;

    /**
     * 请求唯一标识，可用于请求明细溯源和问题排查
     */
    private String requestId;
}
