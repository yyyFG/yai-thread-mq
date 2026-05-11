package cn.y.yai.ai.model.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 流式消息响应基类
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class StreamMessage {

    private String type;
}
