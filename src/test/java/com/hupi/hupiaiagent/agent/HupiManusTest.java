package com.hupi.hupiaiagent.agent;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class HupiManusTest {

    @Resource
    private HupiManus hupiManus;

    @Test
    public void run() {
        String userPrompt = """
                我的 Linux 服务器 ssh 连不上了，怎么回事？
                用 xshell 连的，提示‘Connection refused’，服务器是 CentOS 7 系统，
                昨天还能连，今天早上突然不行，没动过防火墙，ssh 服务没手动关过，
                并结合一些网络图片，制定一份详细的解决方案，
                并以 PDF 格式输出""";
        String answer = hupiManus.run(userPrompt);
        Assertions.assertNotNull(answer);
    }
}