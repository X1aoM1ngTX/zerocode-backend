package com.xm.zerocodebackend.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.xm.zerocodebackend.model.dto.user.UserQueryRequest;
import com.xm.zerocodebackend.model.entity.User;
import com.xm.zerocodebackend.model.vo.LoginUserVO;
import com.xm.zerocodebackend.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 用户 服务层。
 *
 * @author <a href="https://github.com/X1aoM1ngTX">X1aoM1ngTX</a>
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 密码加密
     *
     * @param userPassword 用户密码
     * @return 加密后的密码
     */
    String getEncryptPassword(String userPassword);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获取脱敏的已登录用户信息
     *
     * @param user 用户实体
     * @return 脱敏后的登录用户信息
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param user 用户实体
     * @return 脱敏后的用户信息
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏的用户信息列表
     *
     * @param userList 用户实体列表
     * @return 脱敏后的用户信息列表
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 获取查询用户的查询条件
     *
     * @param userQueryRequest 查询用户的请求参数
     * @return 查询用户的查询条件
     */
    QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 用户登出
     *
     * @param request HttpServletRequest 请求对象
     * @return 是否登出成功
     */
    boolean userLogout(HttpServletRequest request);

}
