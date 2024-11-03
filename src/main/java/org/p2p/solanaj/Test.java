package org.p2p.solanaj;


import org.p2p.solanaj.parse.SwapMonitor;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.ConfirmedTransaction;

public class Test {
    public static void main(String[] args) throws Exception {

        ConfirmedTransaction transaction = new RpcClient(Cluster.MAINNET).getApi().getTransaction("kkWt7wLEeYCmbGWGvsJC9gQrciYJHxPWNUQEz9n9XgPjBVFeEmF8MR594LbSBcarqEEdoCyThvyCPTwo55tZd6i");

        SwapMonitor.processSwapTx(transaction);

    }


}
