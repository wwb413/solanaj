package org.p2p.solanaj.parse;

import cn.hutool.core.codec.Base64Decoder;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.bitcoinj.core.Base58;
import org.p2p.solanaj.parse.bean.Consts;
import org.p2p.solanaj.parse.bean.SwapData;
import org.p2p.solanaj.rpc.types.ConfirmedTransaction;

import java.util.Arrays;
import java.util.List;

public class JupiterSwapParse{

    public static SwapData parseSwap(ConfirmedTransaction tx, int index) {
        List<JSONObject> list = tx.getMeta().getInnerInstructions().stream().map(vo -> JSONUtil.parseObj(vo)).toList();
        List<String> accountKeys = tx.getTransaction().getMessage().getAccountKeys();
        for (int i = 0; i < list.size(); i++) {
            JSONObject entries = list.get(i);
            if (index == entries.getInt("index")){
                for (JSONObject instructions : entries.getJSONArray("instructions").jsonIter()) {
                    isJupiterRouteEventInstruction(instructions,accountKeys);

                    System.out.println(instructions);
                }
            }
        }

        return null;
    }


    private static SwapData parseJupiterRouteEventInstruction(JSONObject instruction){



        return null;
    }


    private static boolean isJupiterRouteEventInstruction(JSONObject instruction,List<String> accountKeys){
        boolean result = false;
        String data = instruction.getStr("data");
        Integer programIdIndex = instruction.getInt("programIdIndex");
        if ( programIdIndex > accountKeys.size() || !accountKeys.get(programIdIndex).equals(Consts.JUPITER_PROGRAM_ID) || data.length() < 16){
            return result;
        }

        byte[] decode = Base58.decode(data);



        System.out.println(ArrayUtil.join(decode, ", "));

        return result;
    }
}
