package org.p2p.solanaj;

import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcApi;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.ConfirmedTransaction;

public class Test {

    public static void main(String[] args) throws Exception {

        RpcClient client = new RpcClient(Cluster.MAINNET);

        RpcApi api = client.getApi();

        ConfirmedTransaction transaction = api.getTransaction("5Na9UJjPTaawz59C3MxHtiiexqgf1Lr12btzpwZ1wTxLHoBkMiJWRS3iyPVeyJRUEWS7vBA6pLANVhGxYrEf6s9d");

        ConfirmedTransaction.Message message = transaction.getTransaction().getMessage();
        System.out.println(message.getInstructions());

        System.out.println(message.getAccountKeys());

        System.out.println(transaction.getMeta().getInnerInstructions());

    }
}
