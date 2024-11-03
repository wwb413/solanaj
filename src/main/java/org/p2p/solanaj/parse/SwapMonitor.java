package org.p2p.solanaj.parse;

import org.p2p.solanaj.parse.bean.Consts;
import org.p2p.solanaj.parse.bean.SwapData;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcApi;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.ConfirmedTransaction;
import org.p2p.solanaj.rpc.types.LatestBlockhash;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SwapMonitor {

    public static Map<String,Integer> splDecimalsMap = new HashMap<>();

    static {
        splDecimalsMap.put(Consts.NATIVE_SOL_MINT_PROGRAM_ID,9);
    }

    public static void main(String[] args) throws Exception {
        RpcClient client = new RpcClient(Cluster.ANKR);

        RpcApi api = client.getApi();

        while (true){

            LatestBlockhash latestBlockhash = api.getLatestBlockhash();

            System.out.println(latestBlockhash);

        }

    }


    private static void extractSPLDecimals(ConfirmedTransaction tx){
        List<ConfirmedTransaction.TokenBalance> postTokenBalances = tx.getMeta().getPostTokenBalances();

        for (ConfirmedTransaction.TokenBalance postTokenBalance : postTokenBalances) {
            String mint = postTokenBalance.getMint();
            int decimals = postTokenBalance.getUiTokenAmount().getDecimals();

            if (!splDecimalsMap.containsKey(mint)){
                splDecimalsMap.put(mint,decimals);
            }
        }
    }


    private static Map<String,String> extractSPLTokenInfo(ConfirmedTransaction tx,List<String> accountKeys){
        HashMap<String, String> tokenInfoMap = new HashMap<>();

        List<ConfirmedTransaction.TokenBalance> postTokenBalances = tx.getMeta().getPostTokenBalances();

        for (ConfirmedTransaction.TokenBalance postTokenBalance : postTokenBalances) {
            if (!postTokenBalance.getMint().isEmpty()){
                int index = postTokenBalance.getAccountIndex().intValue();
                if (index < accountKeys.size()){
                    String accountKey = accountKeys.get(index);
                    tokenInfoMap.put(accountKey, postTokenBalance.getMint());
                }
            }
        }

        return tokenInfoMap;
    }


    public static List<SwapData> processSwapTx(ConfirmedTransaction tx) throws Exception {
        List<SwapData> swapDatas = new ArrayList<>();
        boolean skip = false;
        List<String> accountKeys = tx.getTransaction().getMessage().getAccountKeys();
        // 解析代币精度
        extractSPLDecimals(tx);

        // 解析account 对应 mint
        Map<String, String> tokenInfo = extractSPLTokenInfo(tx, accountKeys);

        List<ConfirmedTransaction.Instruction> instructions = tx.getTransaction().getMessage().getInstructions();

        for (int i = 0; i < instructions.size(); i++) {
            SwapData swapData = null;
            String proId = accountKeys.get((int)instructions.get(i).getProgramIdIndex());
            if (proId.equals(Consts.JUPITER_PROGRAM_ID)){
                skip = true;
                swapData = JupiterSwapParse.parseSwap(tx, i, splDecimalsMap);
            }
            if (proId.equals(Consts.MOONSHOT_PROGRAM_ID)){
                skip = true;
                swapData = MoonshotSwapParse.parseSwap(tx,i,splDecimalsMap);
            }

            if (swapData != null) swapDatas.add(swapData);
        }

        if (skip){
            return swapDatas;
        }

        for (int i = 0; i < instructions.size(); i++) {
            SwapData swapData = null;
            String proId = accountKeys.get((int)instructions.get(i).getProgramIdIndex());
            switch (proId){
                case Consts.RAYDIUM_V4_PROGRAM_ID:
                case Consts.RAYDIUM_CPMM_PROGRAM_ID:
                case Consts.RAYDIUM_AMM_PROGRAM_ID:
                case Consts.RAYDIUM_CONCENTRATED_LIQUIDITY_PROGRAM_ID:
                case Consts.RAYDIUM_CONCENTRATED_LIQUIDITY_PROGRAM_ID_V2:
                case Consts.BANANA_GUN_PROGRAM_ID:
                    swapData = TransferSwapParse.parseTransfer(tx,i,splDecimalsMap,tokenInfo);
                    break;
                case Consts.ORCA_PROGRAM_ID:
                    swapData = TransferSwapParse.parseTransfer(tx,i,splDecimalsMap,tokenInfo);
                    break;
                case Consts.METEORA_PROGRAM_ID:
                case Consts.METEORA_POOLS_PROGRAM_ID:
                    swapData = TransferSwapParse.parseTransfer(tx,i,splDecimalsMap,tokenInfo);
                    break;
                case Consts.PUMP_FUN_PROGRAM_ID:
                case Consts.PUMP_FUN_PROGRAM_ID_V2:
                    swapData = PumpfunSwapParse.parseSwap(tx,i,splDecimalsMap);
                    break;

            }

            if (swapData != null) swapDatas.add(swapData);

        }

        if (!swapDatas.isEmpty()){

            System.out.println(swapDatas);
        }

        return swapDatas;
    }
}
