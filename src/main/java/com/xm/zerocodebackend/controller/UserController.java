package com.xm.zerocodebackend.controller;

import com.mybatisflex.core.paginate.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;

import com.xm.zerocodebackend.annotation.AuthCheck;
import com.xm.zerocodebackend.common.BaseResponse;
import com.xm.zerocodebackend.common.DeleteRequest;
import com.xm.zerocodebackend.common.ResultUtils;
import com.xm.zerocodebackend.constant.UserConstant;
import com.xm.zerocodebackend.exception.BusinessException;
import com.xm.zerocodebackend.exception.ErrorCode;
import com.xm.zerocodebackend.exception.ThrowUtils;
import com.xm.zerocodebackend.model.dto.user.UserAddRequest;
import com.xm.zerocodebackend.model.dto.user.UserLoginRequest;
import com.xm.zerocodebackend.model.dto.user.UserQueryRequest;
import com.xm.zerocodebackend.model.dto.user.UserRegisterRequest;
import com.xm.zerocodebackend.model.dto.user.UserUpdateRequest;
import com.xm.zerocodebackend.model.entity.User;
import com.xm.zerocodebackend.model.vo.LoginUserVO;
import com.xm.zerocodebackend.model.vo.UserVO;
import com.xm.zerocodebackend.service.UserService;

import cn.hutool.core.bean.BeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * 用户 控制层。
 *
 * @author <a href="https://github.com/X1aoM1ngTX">X1aoM1ngTX</a>
 */
@RestController
@RequestMapping("/user")
@Tag(name = "User", description = "用户服务")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("register")
    @Operation(summary = "用户注册", description = "用户注册")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 获取当前登录用户
     */
    @GetMapping("/get/login")
    @Operation(summary = "获取当前登录用户", description = "获取当前登录用户")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(loginUser));
    }

    /**
     * 创建用户
     */
    @PostMapping("admin/add")
    @Operation(summary = "创建用户", description = "创建用户")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空", "请检查请求参数");
        User user = new User();
        BeanUtil.copyProperties(userAddRequest, user);
        // 默认密码 12345678
        final String DEFAULT_PASSWORD = "12345678";
        String encryptPassword = userService.getEncryptPassword(DEFAULT_PASSWORD);
        user.setUserPassword(encryptPassword);
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "创建用户失败", "创建用户失败，请检查数据库");
        return ResultUtils.success(user.getId());
    }

    /**
     * 根据 id 获取用户（仅管理员）
     */
    @GetMapping("admin/get")
    @Operation(summary = "根据 ID 获取用户（仅管理员）", description = "根据 ID 获取用户（仅管理员）")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR, "用户 ID 不合法", "用户 ID 不合法，请检查用户 ID");
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在", "用户不存在，请检查用户 ID");
        return ResultUtils.success(user);
    }

    /**
     * 根据 id 获取包装类
     */
    @GetMapping("get")
    @Operation(summary = "根据 ID 获取用户包装类", description = "根据 ID 获取用户包装类")
    public BaseResponse<UserVO> getUserVOById(long id) {
        BaseResponse<User> response = getUserById(id);
        User user = response.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 分页获取用户封装列表（仅管理员）
     */
    @PostMapping("admin/list/users")
    @Operation(summary = "分页获取用户封装列表（仅管理员）", description = "分页获取用户封装列表（仅管理员）")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空", "请求参数为空,请检查请求参数");
        long current = userQueryRequest.getCurrent();
        long pageSize = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(Page.of(current, pageSize),
                userService.getQueryWrapper(userQueryRequest));
        // 数据脱敏
        Page<UserVO> userVOPage = new Page<>(current, pageSize, userPage.getTotalRow());
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }
    
    /**
     * 更新用户
     */
    @PostMapping("admin/update")
    @Operation(summary = "根据 ID 更新用户（仅管理员）", description = "根据 ID 更新用户（仅管理员）")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户 ID 不合法", "用户 ID 不合法，请检查用户 ID");
        }
        User user = new User();
        BeanUtil.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "更新用户失败", "更新用户失败，请检查数据库");
        return ResultUtils.success(true);
    }

    /**
     * 删除用户
     */
    @PostMapping("admin/delete")
    @Operation(summary = "根据 ID 删除用户（仅管理员）", description = "根据 ID 删除用户（仅管理员）")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户 ID 不合法", "用户 ID 不合法，请检查用户 ID");
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 用户登出
     */
    @PostMapping("logout")
    @Operation(summary = "用户登出", description = "用户登出")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

}
