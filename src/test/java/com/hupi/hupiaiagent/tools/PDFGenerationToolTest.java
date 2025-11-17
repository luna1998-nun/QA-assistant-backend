package com.hupi.hupiaiagent.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class PDFGenerationToolTest {

    @Test
    void generatePDF() {
        PDFGenerationTool tool = new PDFGenerationTool();
        String fileName = "pdf生成测试.pdf";
        String content = "测试生成pdf";
        String result = tool.generatePDF(fileName, content);
        assertNotNull(result);
    }
}