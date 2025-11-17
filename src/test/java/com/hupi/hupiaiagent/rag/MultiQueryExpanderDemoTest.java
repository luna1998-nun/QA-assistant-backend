package com.hupi.hupiaiagent.rag;

import com.hupi.hupiaiagent.demo.rag.MultiQueryExpanderDemo;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.rag.Query;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class MultiQueryExpanderDemoTest {

    @Resource
    private MultiQueryExpanderDemo multiQueryExpanderDemo;

    @Test
    void expand() {
        List<Query> queries = multiQueryExpanderDemo.expand("嗯。。。你好，啥是Linux 服务器故障？请。。回答我");
        Assertions.assertNotNull(queries);
    }
}
