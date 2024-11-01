package org.p2p.solanaj.parse;

import org.p2p.solanaj.parse.bean.Consts;
import org.p2p.solanaj.parse.bean.SwapData;
import org.p2p.solanaj.rpc.types.ConfirmedTransaction;

import java.util.ArrayList;
import java.util.List;

public class SwapMonitor {

    public static void main(String[] args) {


    }



    public static List<SwapData> processSwapTx(ConfirmedTransaction tx){
        List<SwapData> swapData = new ArrayList<>();

        boolean skip = false;

        List<ConfirmedTransaction.Instruction> instructions = tx.getTransaction().getMessage().getInstructions();
        List<String> accountKeys = tx.getTransaction().getMessage().getAccountKeys();
        for (int i = 0; i < instructions.size(); i++) {
            String proId = accountKeys.get(i);
            if (proId.equals(Consts.JUPITER_PROGRAM_ID)){
                skip = true;
                swapData.add(JupiterSwapParse.parseSwap(tx,i));
            }
            if (proId.equals(Consts.MOONSHOT_PROGRAM_ID)){
                skip = true;
                swapData.add(MoonshotSwapParse.parseSwap(tx,i));
            }
        }

        if (skip){
            return swapData;
        }

        for (int i = 0; i < instructions.size(); i++) {
            String proId = accountKeys.get(i);
            switch (proId){
                case Consts.RAYDIUM_V4_PROGRAM_ID:
                case Consts.RAYDIUM_CPMM_PROGRAM_ID:
                case Consts.RAYDIUM_AMM_PROGRAM_ID:
                case Consts.RAYDIUM_CONCENTRATED_LIQUIDITY_PROGRAM_ID:
                case Consts.RAYDIUM_CONCENTRATED_LIQUIDITY_PROGRAM_ID_V2:
                case Consts.BANANA_GUN_PROGRAM_ID:
                    swapData.add(RaydSwapParse.parseSwap(tx,i));
                    break;
                case Consts.ORCA_PROGRAM_ID:
                    swapData.add(OrcaSwapParse.parseSwap(tx,i));
                    break;
                case Consts.METEORA_PROGRAM_ID:
                case Consts.METEORA_POOLS_PROGRAM_ID:
                    swapData.add(MeteoraSwapParse.parseSwap(tx,i));
                    break;
                case Consts.PUMP_FUN_PROGRAM_ID:
                case Consts.PUMP_FUN_PROGRAM_ID_V2:
                    swapData.add(PumpfunSwapParse.parseSwap(tx,i));
                    break;

            }

        }

        return swapData;
    }
}
