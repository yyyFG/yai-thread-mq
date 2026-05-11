package cn.y.yai.model.dto.app;

import lombok.Data;
import java.io.Serializable;

/**
 * 编辑应用请求（用户）
 */
@Data
public class AppEditRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 应用名称
     */
    private String appName;

    private static final long serialVersionUID = 1L;
}
