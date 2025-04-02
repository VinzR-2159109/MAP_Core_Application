package be.uhasselt.dwi_application.model.Jackson.Commands.Websocket;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

public class WebSocketCommand {
    @JsonProperty("key")
    private final String key = "websocket";

    @JsonProperty("action")
    private final WebSocketAction action;

    @JsonProperty("data")
    private final String data;

    public enum WebSocketAction {
        CONNECT("connect"),
        DISCONNECT("disconnect");

        private final String value;

        WebSocketAction(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }
    }

    private WebSocketCommand(WebSocketAction action, String data) {
        this.action = action;
        this.data = data;
    }

    public static WebSocketCommand connect(String url) {
        return new WebSocketCommand(WebSocketAction.CONNECT, url);
    }

    public static WebSocketCommand disconnect() {
        return new WebSocketCommand(WebSocketAction.DISCONNECT, null);
    }

    public String getKey() {
        return key;
    }

    public WebSocketAction getAction() {
        return action;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getData() {
        return action.equals(WebSocketAction.CONNECT) ? data : null;
    }
}
