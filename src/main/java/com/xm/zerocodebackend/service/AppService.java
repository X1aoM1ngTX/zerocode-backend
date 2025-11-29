package com.xm.zerocodebackend.service;

import java.util.List;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.xm.zerocodebackend.model.dto.app.AppQueryRequest;
import com.xm.zerocodebackend.model.entity.App;
import com.xm.zerocodebackend.model.entity.User;
import com.xm.zerocodebackend.model.vo.AppVO;

import reactor.core.publisher.Flux;

/**
 * 应用 服务层。
 *
 * @author <a href="https://github.com/X1aoM1ngTX">X1aoM1ngTX</a>
 */
public interface AppService extends IService<App> {

    /**
     * 获取应用视图对象
     *
     * @param app 应用实体
     * @return 应用视图对象
     */
    AppVO getAppVO(App app);

    /**
     * 获取应用查询包装器
     *
     * @param appQueryRequest 应用查询请求
     * @return 应用查询包装器
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 获取应用视图对象列表
     *
     * @param appList 应用实体列表
     * @return 应用视图对象列表
     */
    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 对话生成代码
     *
     * @param appId     应用ID
     * @param massage   用户消息
     * @param loginUser 登录用户
     * @return 流式响应
     */
    Flux<String> chatToGenCode(Long appId, String massage, User loginUser);

    /**
     * 部署应用
     *
     * @param appId    应用 ID
     * @param loginUser 登录用户
     * @return 可访问的 URL
     */
    String deployApp(Long appId, User loginUser);
}
