package com.xm.zerocodebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.xm.zerocodebackend.constant.UserConstant;
import com.xm.zerocodebackend.exception.BusinessException;
import com.xm.zerocodebackend.exception.ErrorCode;
import com.xm.zerocodebackend.exception.ThrowUtils;
import com.xm.zerocodebackend.mapper.UserMapper;
import com.xm.zerocodebackend.model.dto.user.UserQueryRequest;
import com.xm.zerocodebackend.model.entity.User;
import com.xm.zerocodebackend.model.enums.UserRoleEnum;
import com.xm.zerocodebackend.model.vo.LoginUserVO;
import com.xm.zerocodebackend.model.vo.UserVO;
import com.xm.zerocodebackend.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
 * 用户 服务层实现。
 *
 * @author <a href="https://github.com/X1aoM1ngTX">X1aoM1ngTX</a>
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    // 盐值
    @Value("${xm.zerocode.salt}")
    private String SALT;

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空", "请求参数为空，请检查并重新输入");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短", "账号不能小于4位，请重新输入账号");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短", "密码不能小于8位，请重新输入密码");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码不一致", "两次输入的密码不一致，请检查并重新输入");
        }
        // 2. 检查是否重复
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.mapper.selectCountByQuery(queryWrapper);
        ThrowUtils.throwIf(count > 0, ErrorCode.PARAMS_ERROR, "账号重复", "账号已重复，请更换账号");
        // 3. 加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 4. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName("用户" + userAccount);
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败", "注册失败，数据库错误，请联系管理员");
        }
        return user.getId();
    }

    /**
     * 密码加密
     *
     * @param userPassword 用户密码
     * @return 加密后的密码
     */
    @Override
    public String getEncryptPassword(String userPassword) {
        // 盐值，混淆密码
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StrUtil.hasBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空", "请求参数为空，请检查并重新输入");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误", "账号不能小于4位，请重新输入账号");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码过短", "密码不能小于8位，请重新输入密码");
        }
        // 2. 检查是否重复
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);
        User user = this.mapper.selectOneByQuery(queryWrapper);
        ThrowUtils.throwIf(user == null, ErrorCode.PARAMS_ERROR, "账号不存在", "账号不存在，请重新输入账号");
        // 3. 校验密码
        String encryptPassword = getEncryptPassword(userPassword);
        ThrowUtils.throwIf(!user.getUserPassword().equals(encryptPassword), ErrorCode.PARAMS_ERROR, "密码错误",
                "密码错误，请重新输入密码");
        // 4. 登录成功，存储会话信息
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
        // 5. 返回脱敏后的用户信息
        return this.getLoginUserVO(user);
    }

    /**
     * 获取当前登录用户
     *
     * @param request HttpServletRequest 请求对象
     * @return 当前登录用户实体
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "未登录", "请先登录");
        }
        // 从数据库查询 (追求性能的话可以注释，直接返回上述结果)
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "未登录", "请先登录");
        }
        return currentUser;
    }

    /**
     * 获取脱敏的已登录用户信息
     * 
     * @param user 用户实体
     * @return 脱敏后的登录用户信息
     */
    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    /**
     * 获取脱敏的用户信息
     *
     * @param request HttpServletRequest 请求对象
     * @return 脱敏后的用户信息
     */
    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    /**
     * 获取脱敏的用户信息列表
     *
     * @param userList 用户实体列表
     * @return 脱敏后的用户信息列表
     */
    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    /**
     * 获取查询用户的查询条件
     *
     * @param userQueryRequest 查询用户的请求参数
     * @return 查询用户的查询条件
     */
    @Override
    public QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空", "请求参数为空，请检查");
        }
        Long userId = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();

        return QueryWrapper.create()
                .eq("id", userId) // where id = userId
                .eq("userRole", userRole) // where userRole = userRole
                .like("userProfile", userProfile) // where userProfile like '%userProfile%'
                .like("userAccount", userAccount) // where userAccount like '%userAccount%'
                .like("userName", userName) // where userName like '%userName%'
                .orderBy(sortField, "ascend".equals(sortOrder)); // order by sortField ascend/descend
    }

    /**
     * 用户登出
     *
     * @param request HttpServletRequest 请求对象
     * @return 是否登出成功
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "未登录", "没有登录");
        }
        // 移除登录状态
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }
}
