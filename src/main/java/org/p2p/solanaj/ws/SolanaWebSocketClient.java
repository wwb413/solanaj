package org.p2p.solanaj.ws;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.p2p.solanaj.parse.SwapMonitor;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.types.ConfirmedTransaction;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class SolanaWebSocketClient {
    private static final String SOLANA_RPC_WS_URL = "wss://cosmopolitan-lingering-moon.solana-mainnet.quiknode.pro/f6f220d1f0293c8fa4c1a92e305caf11885a8e93";
    private static final String SOLANA_RPC_HTTP_URL = "https://cosmopolitan-lingering-moon.solana-mainnet.quiknode.pro/f6f220d1f0293c8fa4c1a92e305caf11885a8e93";
    private WebSocketClient webSocketClient;

    public void connect() {
        try {
            webSocketClient = new WebSocketClient(new URI(SOLANA_RPC_WS_URL)) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    System.out.println("Connected to Solana WebSocket");
                    subscribeToTransactions();
                }

                @Override
                public void onMessage(String message) {
                    handleMessage(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    try {
                        Thread.sleep(5000); // 等待5秒
                        reconnect(); // 重新连接的方法
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Connection closed: " + reason);
                }

                @Override
                public void onError(Exception ex) {
                    System.err.println("Error occurred: " + ex.getMessage());
                }
            };

            webSocketClient.connect();
        } catch (URISyntaxException e) {
            System.err.println("Invalid WebSocket URL: " + e.getMessage());
        }
    }

    private void subscribeToTransactions() {

        webSocketClient.send("{\"jsonrpc\": \"2.0\", \"id\": 1, \"method\": \"logsSubscribe\", \"params\": [\"all\", {\"commitment\": \"finalized\"}]}");
    }

    private void handleMessage(String message) {
        try {

            JSONObject jsonMessage = JSONUtil.parseObj(message);

            // 检查是否是订阅确认消息
            if (jsonMessage.containsKey("result")) {
                System.out.println("Subscription ID: " + jsonMessage.get("result"));
                return;
            }

            // 处理交易数据
            if (jsonMessage.containsKey("params")) {
                JSONObject params = jsonMessage.getJSONObject("params");
                JSONObject result = params.getJSONObject("result");
                JSONObject value = result.getJSONObject("value");
                JSONObject context = result.getJSONObject("context");
                // 获取交易相关信息
                String signature = value.containsKey("signature") ?
                        value.getStr("signature") : "Unknown";


                if (value.isNull("err") && !signature.equals("1111111111111111111111111111111111111111111111111111111111111111")){

                    String slot = context.containsKey("slot") ?
                            context.getStr("slot"): "Unknown";

                    ConfirmedTransaction transaction = new RpcClient(SOLANA_RPC_HTTP_URL).getApi().getTransaction(signature);

                    if (transaction != null) SwapMonitor.processSwapTx(transaction);

                    System.out.println("signature: " + signature);
                    System.out.println("slot: " + slot);
                    System.out.println("------------------------");
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        if (webSocketClient != null && !webSocketClient.isClosed()) {
            webSocketClient.close();
        }
    }

    public static void main(String[] args) {
        SolanaWebSocketClient client = new SolanaWebSocketClient();
        client.connect();

        // 保持程序运行
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            client.disconnect();
        }
    }
}