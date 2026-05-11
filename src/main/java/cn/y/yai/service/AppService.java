package cn.y.yai.service;

import cn.y.yai.model.dto.app.AppAddRequest;
import cn.y.yai.model.dto.app.AppQueryRequest;
import cn.y.yai.model.entity.User;
import cn.y.yai.model.vo.AppVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import cn.y.yai.model.entity.App;
import reactor.core.publisher.Flux;

import java.io.Serializable;
import java.util.List;

/**
 * 应用 服务层。
 *
 * @author <a href="https://github.com">yyy</a>
 */
public interface AppService extends IService<App> {

    /**
     * 获取查询条件
     *
     * @param appQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 获取应用视图
     *
     * @param app
     * @return
     */
    AppVO getAppVO(App app);

    /**
     * 获取应用视图列表
     *
     * @param appList
     * @return
     */
    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 应用生成（门面模式）
     * @param appId
     * @param message
     * @param loginUser
     * @return
     */
    Flux<String> chatToGenCode(Long appId, String message, User loginUser);

    /**
     * 部署应用
     * @param appId
     * @param loginUser
     * @return
     */
    String deployApp(Long appId, User loginUser);

    /**
     * 删除应用时关联删除对话历史
     * @param id 数据主键
     * @return
     */
    boolean removeById(Serializable id);

    /**
     * 异步生成应用截图并上传
     * @param appId
     * @param appUrl
     */
    void generateAppScreenshotAsync(Long appId, String appUrl);

    /**
     *
     * @param appAddRequest
     * @param LoginUser
     * @return
     */
    Long createApp(AppAddRequest appAddRequest, User LoginUser);
}
