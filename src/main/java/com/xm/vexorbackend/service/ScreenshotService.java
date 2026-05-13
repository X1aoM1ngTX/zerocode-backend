package com.xm.vexorbackend.service;

public interface ScreenshotService {

    /**
     * 生成并上传截图
     *
     * @param webUrl 网页 URL
     * @return 截图 URL
     */
    String generateAndUploadScreenshot(String webUrl);
}
