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

public class RaydSwapParse {

    public static SwapData parseSwap(ConfirmedTransaction tx, int index,Map<String,Integer> splDecimalsMap,Map<String,String> tokenInfo) throws Exception {

        List<JSONObject> list = tx.getMeta().getInnerInstructions().stream().map(vo -> JSONUtil.parseObj(vo)).toList();
        List<String> accountKeys = tx.getTransaction().getMessage().getAccountKeys();


        return null;
    }

}
