package co.casterlabs.sora.api.websockets;

public interface WebsocketFrame {

    public WebsocketFrameType getFrameType();

    public String getAsText();

    public byte[] getBytes();

    public int getSize();

}
