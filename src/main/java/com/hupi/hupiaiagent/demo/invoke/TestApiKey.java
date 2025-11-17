package com.hupi.hupiaiagent.demo.invoke;

/**
 * 仅用于测试获取 API Key
 */
public interface TestApiKey {

    // 修改为你的 API Key
    String API_KEY = "你的 API KEY";

    // 重写接口的 API_KEY，返回配置值
    String getApiKey();
}
