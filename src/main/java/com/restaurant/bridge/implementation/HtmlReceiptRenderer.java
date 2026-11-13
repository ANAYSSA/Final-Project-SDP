package com.restaurant.bridge.implementation;

import java.util.stream.Collectors;

public final class HtmlReceiptRenderer implements ReceiptRenderer {
    public String render(ReceiptContent content) {
        String lines = content.lines().stream().map(line -> "<p>" + escape(line) + "</p>").collect(Collectors.joining());
        return "<!doctype html><html lang=\"en\"><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">"
            + "<title>" + escape(content.title()) + "</title><style>body{font:16px/1.6 system-ui;background:#f4efe6;color:#242a29;margin:0;padding:32px}"
            + "main{max-width:680px;margin:auto;padding:32px;background:#fff;border-radius:16px}h1{font-family:Georgia,serif}p{white-space:pre-wrap;overflow-wrap:anywhere}"
            + "small{color:#666}</style></head><body><main><h1>" + escape(content.title()) + "</h1>" + lines
            + "<small>Hearth &amp; Table · Educational ordering demo · No payment collected</small></main></body></html>";
    }
    public String contentType() { return "text/html;charset=UTF-8"; }
    private String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
}
