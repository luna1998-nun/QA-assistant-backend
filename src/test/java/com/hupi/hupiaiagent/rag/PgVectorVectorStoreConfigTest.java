package com.hupi.hupiaiagent.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

@SpringBootTest
class PgVectorVectorStoreConfigTest {

    @Resource
    private VectorStore pgVectorVectorStore;

    @Test
    void pgVectorVectorStore() {
        List<Document> documents = List.of(
                new Document("服务器 CPU 使用率突然飙升时，先查看占用 CPU 的进程，结束异常进程并排查原因。", Map.of("meta1", "meta1")),
                new Document("数据库连接超时的情况下，需要检查数据库服务是否正常，验证连接参数并重启相关服务。"),
                new Document("网站无法访问的时候，可以先 ping 服务器看网络通不通，再检查 web 服务和端口状态。", Map.of("meta2", "meta2")));
        // 添加文档
        pgVectorVectorStore.add(documents);
        // 相似度查询
        List<Document> results = pgVectorVectorStore.similaritySearch(SearchRequest.builder().query("我遇到了服务器 CPU 使用率异常暴涨的情况，我该怎么办？").topK(3).build());
        Assertions.assertNotNull(results);
    }
}
