package org.p2p.solanaj.parse;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.Data;
import lombok.Getter;
import org.bitcoinj.core.Base58;
import org.p2p.solanaj.parse.bean.Consts;
import org.p2p.solanaj.parse.bean.SwapData;
import org.p2p.solanaj.rpc.types.ConfirmedTransaction;
import org.p2p.solanaj.utils.DecodeUtil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TransferSwapParse {

    @Data
    public static class Transfer {
        private long amount;
        private String authority;
        private String destination;
        private String source;
        private String mint;
        private int decimal;

        public Transfer(long amount, String authority, String destination, String source, String mint, int decimal) {
            this.amount = amount;
            this.authority = authority;
            this.destination = destination;
            this.source = source;
            this.mint = mint;
            this.decimal = decimal;
        }
    }



    public static SwapData parseTransfer(ConfirmedTransaction tx, int index,Map<String,Integer> splDecimalsMap,Map<String,String> tokenInfo) throws Exception {
        List<JSONObject> list = tx.getMeta().getInnerInstructions().stream().map(vo -> JSONUtil.parseObj(vo)).toList();
        List<String> accountKeys = tx.getTransaction().getMessage().getAccountKeys();
        List<Transfer> transfers = new LinkedList<>();
        for (int i = 0; i < list.size(); i++) {
            JSONObject entries = list.get(i);
            if (index == entries.getInt("index")){
                for (JSONObject instructions : entries.getJSONArray("instructions").jsonIter()) {
                    if (isTransfer(instructions,accountKeys)){
                        transfers.add(processTransfer(instructions,accountKeys,splDecimalsMap,tokenInfo));
                    }

                    if (isTransferCheck(instructions,accountKeys)){
                        transfers.add(processTransferCheck(instructions,accountKeys,splDecimalsMap,tokenInfo));
                    }
                }
            }
        }

        if (transfers.size() != 2) return null;

        Transfer input = transfers.get(0);
        String inputMint = input.getMint();
        long inputAmount = input.getAmount();
        int inputDecimal = input.getDecimal();

        Transfer output = transfers.get(1);
        String outputMint = output.getMint();
        long outputAmount = output.getAmount();
        int outputDecimal = output.getDecimal();

        return new SwapData(inputMint,inputAmount,inputDecimal,outputMint,outputAmount,outputDecimal);

    }

    public static Transfer processTransfer(JSONObject instruction,List<String> accountKeys,Map<String,Integer> splDecimalsMap,Map<String,String> tokenInfo) throws Exception {
        Integer programIdIndex = instruction.getInt("programIdIndex");
        String data = instruction.getStr("data");
        JSONArray accounts = instruction.getJSONArray("accounts");

        String source = accountKeys.get(accounts.getInt(0));
        String destination = accountKeys.get(accounts.getInt(1));
        String authority = accountKeys.get(accounts.getInt(2));

        Long amount = DecodeUtil.decodeAdvanced(ArrayUtil.sub(Base58.decode(data), 1, 9), Long.class);

        String mint = "";

        if (tokenInfo.containsKey(destination)){
            mint = tokenInfo.get(destination);
        }else if (tokenInfo.containsKey(source)){
            mint = tokenInfo.get(source);
        }
        int decimal = 0;
        try {
            decimal = splDecimalsMap.get(mint);

        }catch (Exception e){
            System.out.println("mint:"+mint+" value:"+splDecimalsMap.get(mint));

            System.out.println("tokenInfo:"+tokenInfo);
            System.out.println("splDecimalsMap:"+splDecimalsMap);
        }

        return new Transfer(amount,authority,destination,source,mint,decimal);
    }

    public static Transfer processTransferCheck(JSONObject instruction,List<String> accountKeys,Map<String,Integer> splDecimalsMap,Map<String,String> tokenInfo) throws Exception {
        Integer programIdIndex = instruction.getInt("programIdIndex");
        String data = instruction.getStr("data");
        JSONArray accounts = instruction.getJSONArray("accounts");

        String source = accountKeys.get(accounts.getInt(0));
        String mint = accountKeys.get(accounts.getInt(1));
        String destination = accountKeys.get(accounts.getInt(2));
        String authority = accountKeys.get(accounts.getInt(3));

        Long amount = DecodeUtil.decodeAdvanced(ArrayUtil.sub(Base58.decode(data), 1, 9), Long.class);

        int decimal = splDecimalsMap.get(mint);

        return new Transfer(amount,authority,destination,source,mint,decimal);
    }

     public static boolean isTransfer(JSONObject instruction, List<String> accountKeys)  {
        Integer programIdIndex = instruction.getInt("programIdIndex");
        String data = instruction.getStr("data");
        JSONArray accounts = instruction.getJSONArray("accounts");
        if (programIdIndex >= accountKeys.size()) return false;
        String proId = accountKeys.get(programIdIndex);
        if (!proId.equals(Consts.TOKEN_PROGRAM_ID)) return false;

        if (accounts.size() < 3 || data.length() < 9) return false;

//        if (!data.startsWith("3")) return false;

        for (int i = 0; i < 3; i++) {
            if ( accounts.getInt(i) >= accountKeys.size()) return false;
        }


        return true;

    }

    public static boolean isTransferCheck(JSONObject instruction,List<String> accountKeys)  {
        Integer programIdIndex = instruction.getInt("programIdIndex");
        String data = instruction.getStr("data");
        JSONArray accounts = instruction.getJSONArray("accounts");
        if (programIdIndex >= accountKeys.size()) return false;
        String proId = accountKeys.get(programIdIndex);
        if (!proId.equals(Consts.TOKEN_PROGRAM_ID) && !proId.equals(Consts.TOKEN2022_PROGRAM_ID)) return false;

        if (accounts.size() < 4 || data.length() < 9) return false;

//        if (!data.startsWith("12")) return false;

        for (int i = 0; i < 4; i++) {
            if (accounts.getInt(i) >= accountKeys.size()) return false;
        }

        return true;
    }

}
