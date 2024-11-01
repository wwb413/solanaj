package org.p2p.solanaj;

import cn.hutool.json.JSONArray;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcApi;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.ConfirmedTransaction;

import java.security.MessageDigest;
import java.util.Base64;

public class Test {

    public static void main(String[] args) throws Exception {

        String input = "QMqFu4fYGGeUEysFnenhAvDWgqp1W7DbrMv3z8JcyrP4Bu3Yyyj7irLW76wEzMiFqkMXcsUXJG1WLwjdCWzNTL6957kdfWSD7SPFG2av5YHKd4yTDSavYUfe2agybsjSwMvyoL2Nw47Gn9sEUxgM3DqyjrmuKozHUZb2UHhyXXPCjyH";

        try {
            // Step 1: Base64 decode
            byte[] decodedBytes = Base64.getDecoder().decode(input);

            // Step 2: Compute SHA-256 hash
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(decodedBytes);

            // Convert hash to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            // Output the result
            System.out.println("Hash: " + hexString.toString());
        } catch (IllegalArgumentException e) {
            System.out.println("Base64 decoding failed: " + e.getMessage());
        }

    }
}
