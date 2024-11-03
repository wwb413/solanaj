package org.p2p.solanaj.parse;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.Data;
import org.bitcoinj.core.Base58;
import org.p2p.solanaj.parse.bean.Consts;
import org.p2p.solanaj.parse.bean.SwapData;
import org.p2p.solanaj.rpc.types.ConfirmedTransaction;
import org.p2p.solanaj.utils.DecodeUtil;

import java.util.List;
import java.util.Map;

public class MoonshotSwapParse {
    @Data
    public static class MoonshotSwapEvent {
        public long tokenAmount;
        public long collateralAmount;
        // 1 buy; 0 sell
        public byte fixedSide;
        public long slippageBps;

        @Override
        public String toString() {
            return "MoonshotSwapEvent{" +
                    "tokenAmount=" + tokenAmount +
                    ", collateralAmount=" + collateralAmount +
                    ", fixedSide=" + fixedSide +
                    ", slippageBps=" + slippageBps +
                    '}';
        }
    }

    public static SwapData parseSwap(ConfirmedTransaction tx, int index,Map<String,Integer> splDecimalsMap) throws Exception {
        SwapData swapData = new SwapData();

        List<ConfirmedTransaction.Instruction> instructions = tx.getTransaction().getMessage().getInstructions();
        List<String> accountKeys = tx.getTransaction().getMessage().getAccountKeys();
        for (ConfirmedTransaction.Instruction instruction : instructions) {
            if (isMoonshotTrade(instruction,accountKeys)){
                swapData = parseMoonshotTradeInstruction(instruction,splDecimalsMap,accountKeys);
            }
        }

        System.out.println(swapData);

        return swapData;
    }


    private static SwapData parseMoonshotTradeInstruction(ConfirmedTransaction.Instruction instruction,Map<String,Integer> splDecimalsMap,List<String> accountKeys) throws Exception{
        String data = instruction.getData();
        List<Long> accounts = instruction.getAccounts();

        byte[] decodedBytes = Base58.decode(data);

        MoonshotSwapParse.MoonshotSwapEvent deserialize = DecodeUtil.decodeAdvanced(ArrayUtil.sub(decodedBytes,8,decodedBytes.length), MoonshotSwapParse.MoonshotSwapEvent.class);

        String tokenMint = accountKeys.get(accounts.get(6).intValue());

        String inputMint = "";
        long inputAmount = 0L;
        String outputMint = "";
        long outputAmount = 0L;

        if (deserialize.getFixedSide() == 1){
            //buy
            inputMint = Consts.NATIVE_SOL_MINT_PROGRAM_ID;
            inputAmount = deserialize.collateralAmount;
            outputMint = tokenMint;
            outputAmount = deserialize.tokenAmount;
        }else {
            inputMint = tokenMint;
            inputAmount = deserialize.tokenAmount;
            outputMint = Consts.NATIVE_SOL_MINT_PROGRAM_ID;
            outputAmount = deserialize.collateralAmount;
        }

        Integer inputDecimal = splDecimalsMap.getOrDefault(inputMint, 0);
        Integer outputDecimal = splDecimalsMap.getOrDefault(outputMint, 0);


        return new SwapData(inputMint,inputAmount,inputDecimal,outputMint,outputAmount,outputDecimal);

    }



    private static boolean isMoonshotTrade(ConfirmedTransaction.Instruction instruction,List<String> accountKeys)  {

        String data = instruction.getData();
        int programIdIndex = (int)instruction.getProgramIdIndex();
        List<Long> accounts = instruction.getAccounts();

        return accountKeys.get(programIdIndex).equals(Consts.MOONSHOT_PROGRAM_ID) && data.length() == 45 && accounts.size() == 11;
    }
}
