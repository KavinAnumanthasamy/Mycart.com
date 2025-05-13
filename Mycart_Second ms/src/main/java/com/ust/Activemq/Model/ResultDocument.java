package com.ust.Activemq.Model;

public class ResultDocument {
    private String requestId;
    private String status;

    // Constructor
    public ResultDocument(String requestId, String status) {
        this.requestId = requestId;
        this.status = status;
    }

    // Getters and setters
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
