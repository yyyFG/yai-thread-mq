package cn.y.yai.controller;


import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.concurrent.CompletedFuture;
import org.springframework.context.annotation.Profile;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 队列测试
 */
@RestController
@RequestMapping("/queue")
@Slf4j
@Profile({"dev" , "local"})
public class QueueController {

    // 自动注入一个线程池的实例
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    // 接受一个参数 name，然后将任务添加到线程池中
    @GetMapping("/add")
    public void add(String name) {
        // 使用 CompletableFuture 运行一个异步任务
        CompletableFuture.runAsync(() -> {
            // 打印一条日志信息，包括任务名称和线程的名称
            log.info("任务执行中：" + name + "，执行人：" + Thread.currentThread().getName());
            try {
                // 让线程休眠 10 分钟，模拟长时间运行的任务
                Thread.sleep(600000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 异步任务在 threadPoolExecutor 中执行
        }, threadPoolExecutor);
    }

    // 该方法返回线程池的状态信息
    @GetMapping("/get")
    public String get() {
        // 创建一个 HashMap 存储线程池的状态信息
        Map<String, Object> map = new HashMap<>();
        // 获取线程池的队列长度
        int size = threadPoolExecutor.getQueue().size();
        // 将队伍长度放进 map 中
        map.put("队伍长度", size);
        // 获取线程池已接受的任务总数
        long taskCount = threadPoolExecutor.getTaskCount();
        //  将任务总数放入 map 中
        map.put("任务总数", taskCount);
        // 获取线程池中正在执行任务的线程数
        long completedTaskCount = threadPoolExecutor.getCompletedTaskCount();
        // 将正在工作的线程数放进 map 中
        map.put("已完成任务数", completedTaskCount);
        // 获取线程池中正在执行任务的线程数
        int activeCount = threadPoolExecutor.getActiveCount();
        // 将正在工作的线程数放入 map 中
        map.put("正在工作的线程数", activeCount);
        // 将 map 转换为 JSON 字符串并返回
        return JSONUtil.toJsonStr(map);
    }
}
