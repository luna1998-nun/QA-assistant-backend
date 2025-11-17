package com.hupi.hupiaiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

//@SpringBootTest
class WebSearchToolTest {

    @Value("${search-api.api-key}")
    private String searchApiKey;

    @Test
    void searchWeb() {
        WebSearchTool webSearchTool = new WebSearchTool(searchApiKey);
        String query = "我的 Linux 服务器 ssh 连不上了，怎么回事？";
        String result = webSearchTool.searchWeb(query);
        Assertions.assertNotNull(result);
    }
}
