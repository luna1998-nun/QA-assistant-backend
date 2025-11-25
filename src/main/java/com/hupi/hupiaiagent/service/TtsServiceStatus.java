package com.hupi.hupiaiagent.service;

/**
 * TTS服务状态信息
 */
public class TtsServiceStatus {

    private String serviceName;
    private String status;
    private String provider;
    private String model;
    private Long timestamp;
    private String tempDirectory;
    private Boolean tempDirectoryExists;
    private Integer tempFileCount;
    private String version;
    private String description;

    public TtsServiceStatus() {
    }

    public TtsServiceStatus(String serviceName, String status, String provider, String model) {
        this.serviceName = serviceName;
        this.status = status;
        this.provider = provider;
        this.model = model;
        this.timestamp = System.currentTimeMillis();
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getTempDirectory() {
        return tempDirectory;
    }

    public void setTempDirectory(String tempDirectory) {
        this.tempDirectory = tempDirectory;
    }

    public Boolean getTempDirectoryExists() {
        return tempDirectoryExists;
    }

    public void setTempDirectoryExists(Boolean tempDirectoryExists) {
        this.tempDirectoryExists = tempDirectoryExists;
    }

    public Integer getTempFileCount() {
        return tempFileCount;
    }

    public void setTempFileCount(Integer tempFileCount) {
        this.tempFileCount = tempFileCount;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}