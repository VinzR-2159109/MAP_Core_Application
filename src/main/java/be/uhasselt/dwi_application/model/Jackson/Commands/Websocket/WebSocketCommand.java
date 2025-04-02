package be.uhasselt.dwi_application.model.Jackson.Commands.Websocket;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WebSocketCommand {

    @JsonProperty("key")
    private final String key = "websocket";

    @JsonProperty("action")
    private final String action = "connect";

    @JsonProperty("data")
    private final String data;

    public WebSocketCommand(String data) {
        this.data = data;
    }

    public String getKey() {
        return key;
    }

    public String getAction() {
        return action;
    }

    public String getData() {
        return data;
    }
}
