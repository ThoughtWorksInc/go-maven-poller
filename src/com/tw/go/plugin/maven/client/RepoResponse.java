package com.tw.go.plugin.maven.client;

public class RepoResponse {
    final String responseBody;
    final String mimeType;
    public final static String TEXT_XML = "text/xml";
    public final static String TEXT_HTML = "text/html";

    public RepoResponse(String responseBody, String mimeType) {
        this.responseBody = responseBody;
        this.mimeType = mimeType;
    }

    public boolean isTextXml() {
        return TEXT_XML.equals(mimeType);
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
