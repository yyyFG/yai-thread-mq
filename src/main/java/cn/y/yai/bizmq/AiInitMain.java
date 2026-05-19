package cn.y.yai.bizmq;

import cn.y.yai.exception.BusinessException;
import cn.y.yai.exception.ErrorCode;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * 用于创建测试程序用到的交换机和队列（只用在程序启动前执行一次）
 */
@Slf4j
public class AiInitMain {

    public static void main(String[] args) {
        try {
            // 创建连接工厂
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            // 创建连接
            Connection connection = factory.newConnection();
            // 创建通道
            Channel channel = connection.createChannel();
            // 定义交换机的名称为"code_exchange"
            String EXCHANGE_NAME = AiMqConstant.AI_EXCHANGE_NAME;
            // 声明交换机，指定交换机类型为 direct
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");

            // 创建队列，随机分配一个队列名称
            String queueName = AiMqConstant.AI_QUEUE_NAME;
            // 声明队列，设置队列持久化、非独占、非自动删除，并传入额外的参数为 null
            channel.queueDeclare(queueName, true, false, false, null);
            // 将队列绑定到交换机，指定路由键为 "my_routingKey"
            channel.queueBind(queueName, EXCHANGE_NAME, AiMqConstant.AI_ROUTING_KEY);
            log.info("AiInitMain 启动成功");
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }
    }
}
