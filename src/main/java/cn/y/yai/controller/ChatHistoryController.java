package cn.y.yai.controller;

import cn.y.yai.annotation.AuthCheck;
import cn.y.yai.common.BaseResponse;
import cn.y.yai.common.ResultUtils;
import cn.y.yai.constant.UserConstant;
import cn.y.yai.exception.ErrorCode;
import cn.y.yai.exception.ThrowUtils;
import cn.y.yai.model.dto.chathistory.ChatHistoryQueryRequest;
import cn.y.yai.model.entity.ChatHistory;
import cn.y.yai.model.entity.User;
import cn.y.yai.service.ChatHistoryService;
import cn.y.yai.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 聊天历史 控制层
 */
@RestController
@RequestMapping("/chatHistory")
public class ChatHistoryController {

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private UserService userService;


    /**
     * 分页查询应用聊天历史 (本人
     * @param appId
     * @param pageSize
     * @param lastCreateTime
     * @param request
     * @return
     */
    @GetMapping("/app/{appId}")
    public BaseResponse<Page<ChatHistory>> listChatHistoryByPage(@PathVariable Long appId,
                                                                   @RequestParam(defaultValue = "10") int pageSize,
                                                                   @RequestParam(required = false) LocalDateTime lastCreateTime,
                                                                   HttpServletRequest request) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Page<ChatHistory> result = chatHistoryService.listAppChatHistoryByPage(appId, pageSize, lastCreateTime, loginUser);

        return ResultUtils.success(result);
    }

    /**
     * 分页查询所有聊天历史（仅管理员）
     *
     * @param chatHistoryQueryRequest
     * @return
     */
    @PostMapping("/admin/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<ChatHistory>> listChatHistoryByPageAdmin(@RequestBody ChatHistoryQueryRequest chatHistoryQueryRequest) {
        ThrowUtils.throwIf(chatHistoryQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = chatHistoryQueryRequest.getPageNum();
        long size = chatHistoryQueryRequest.getPageSize();
        // 查询数据
        QueryWrapper queryWrapper = chatHistoryService.getQueryWrapper(chatHistoryQueryRequest);
        Page<ChatHistory> chatHistoryPage = chatHistoryService.page(new Page<>(current, size), queryWrapper);
        return ResultUtils.success(chatHistoryPage);
    }

}
