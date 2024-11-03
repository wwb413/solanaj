package org.p2p.solanaj.parse;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.syntifi.near.borshj.BorshInput;
import lombok.Data;
import org.bitcoinj.core.Base58;
import org.p2p.solanaj.parse.bean.Consts;
import org.p2p.solanaj.parse.bean.SwapData;
import org.p2p.solanaj.rpc.types.ConfirmedTransaction;
import org.p2p.solanaj.utils.DecodeUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PumpfunSwapParse{
    @Data
    public static class PumpfunSwapEvent {
        public String mint;
        public long solAmount;
        public long tokenAmount;
        public boolean isBuy;
        public String user;
        public long timestamp;
        public long virtualSolReserves;
        public long virtualTokenReserves;

    }

    public static SwapData parseSwap(ConfirmedTransaction tx, int index,Map<String,Integer> splDecimalsMap) throws Exception {

        List<JSONObject> list = tx.getMeta().getInnerInstructions().stream().map(vo -> JSONUtil.parseObj(vo)).toList();
        List<String> accountKeys = tx.getTransaction().getMessage().getAccountKeys();
        for (int i = 0; i < list.size(); i++) {
            JSONObject entries = list.get(i);
            if (index == entries.getInt("index")){
                for (JSONObject instructions : entries.getJSONArray("instructions").jsonIter()) {
                    if (isPumpfunRouteEventInstruction(instructions,accountKeys)) {
                        return parsePumpfunRouteEventInstruction(instructions,splDecimalsMap);
                    }
                }
            }
        }

        return null;
    }


    private static SwapData parsePumpfunRouteEventInstruction(JSONObject instruction,Map<String,Integer> splDecimalsMap) throws Exception{
        String data = instruction.getStr("data");

        byte[] decodedBytes = Base58.decode(data);

        PumpfunSwapParse.PumpfunSwapEvent deserialize = DecodeUtil.decodeAdvanced(ArrayUtil.sub(decodedBytes,16,decodedBytes.length), PumpfunSwapParse.PumpfunSwapEvent.class);
        String inputMint = "";
        long inputAmount = 0L;
        String outputMint = "";
        long outputAmount = 0L;

        if (deserialize.isBuy()){
            //buy
            inputMint = Consts.NATIVE_SOL_MINT_PROGRAM_ID;
            inputAmount = deserialize.solAmount;
            outputMint = deserialize.mint;
            outputAmount = deserialize.tokenAmount;
        }else {
            inputMint = deserialize.mint;
            inputAmount = deserialize.tokenAmount;
            outputMint = Consts.NATIVE_SOL_MINT_PROGRAM_ID;
            outputAmount = deserialize.solAmount;
        }

        Integer inputDecimal = splDecimalsMap.getOrDefault(inputMint, 0);
        Integer outputDecimal = splDecimalsMap.getOrDefault(outputMint, 0);


        return new SwapData(inputMint,inputAmount,inputDecimal,outputMint,outputAmount,outputDecimal);
    }



    private static boolean isPumpfunRouteEventInstruction(JSONObject instruction,List<String> accountKeys)  {

        String data = instruction.getStr("data");
        Integer programIdIndex = instruction.getInt("programIdIndex");
        if ( programIdIndex > accountKeys.size() || !accountKeys.get(programIdIndex).equals(Consts.PUMP_FUN_PROGRAM_ID) || data.length() < 16){
            return false;
        }

        byte[] decode = Base58.decode(data);

        byte[] sub = ArrayUtil.sub(decode, 0, 16);

        byte[] bytes = {-28,69,-91,46,81,-53,-102,29,-67,-37,127,-45,78,-26,97,-18};

        return Arrays.equals(sub, bytes);
    }
}
