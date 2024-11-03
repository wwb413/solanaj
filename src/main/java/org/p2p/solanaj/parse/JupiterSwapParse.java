package org.p2p.solanaj.parse;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.codec.Base64Decoder;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ByteUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.google.protobuf.UInt64Value;
import com.syntifi.near.borshj.Borsh;
import com.syntifi.near.borshj.BorshInput;
import com.syntifi.near.borshj.BorshReader;
import com.syntifi.near.borshj.annotation.BorshField;
import com.syntifi.near.borshj.util.BorshUtil;
import lombok.Data;
import lombok.NonNull;
import org.bitcoinj.core.Base58;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.parse.bean.Consts;
import org.p2p.solanaj.parse.bean.SwapData;
import org.p2p.solanaj.rpc.types.ConfirmedTransaction;
import org.p2p.solanaj.utils.DecodeUtil;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.stream.Collectors;

public class JupiterSwapParse{

    @Data
    public static class JupiterSwapEvent {
        public String amm;
        public String inputMint;
        public long inputAmount;
        public String outputMint;
        public long outputAmount;

        @Override
        public String toString() {
            return "JupiterSwapEvent{" +
                    "amm=" + amm +
                    ", inputMint=" + inputMint +
                    ", inputAmount=" + inputAmount +
                    ", outputMint=" + outputMint +
                    ", outputAmount=" + outputAmount +
                    '}';
        }
    }

    public static SwapData parseSwap(ConfirmedTransaction tx, int index,Map<String,Integer> splDecimalsMap) throws Exception {
        SwapData swapData = new SwapData();

        List<JSONObject> list = tx.getMeta().getInnerInstructions().stream().map(vo -> JSONUtil.parseObj(vo)).toList();
        List<String> accountKeys = tx.getTransaction().getMessage().getAccountKeys();
        for (int i = 0; i < list.size(); i++) {
            JSONObject entries = list.get(i);
            if (index == entries.getInt("index")){
                for (JSONObject instructions : entries.getJSONArray("instructions").jsonIter()) {
                    if (isJupiterRouteEventInstruction(instructions,accountKeys)) {
                        swapData = parseJupiterRouteEventInstruction(instructions,splDecimalsMap);
                    }
                }
            }
        }

        System.out.println(swapData);

        return swapData;
    }


    private static SwapData parseJupiterRouteEventInstruction(JSONObject instruction,Map<String,Integer> splDecimalsMap) throws Exception{
        String data = instruction.getStr("data");

        SwapData swapData = new SwapData();

        byte[] decodedBytes = Base58.decode(data);

        JupiterSwapEvent deserialize = DecodeUtil.decodeAdvanced(ArrayUtil.sub(decodedBytes,16,decodedBytes.length), JupiterSwapEvent.class);

        BeanUtil.copyProperties(deserialize,swapData);

        swapData.setInputMintDecimals(splDecimalsMap.getOrDefault(swapData.getInputMint(), 0));
        swapData.setOutputMintDecimals(splDecimalsMap.getOrDefault(swapData.getOutputMint(), 0));

        return swapData;
    }



    private static boolean isJupiterRouteEventInstruction(JSONObject instruction,List<String> accountKeys)  {

        String data = instruction.getStr("data");
        Integer programIdIndex = instruction.getInt("programIdIndex");
        if ( programIdIndex > accountKeys.size() || !accountKeys.get(programIdIndex).equals(Consts.JUPITER_PROGRAM_ID) || data.length() < 16){
            return false;
        }

        return true;
    }


}
