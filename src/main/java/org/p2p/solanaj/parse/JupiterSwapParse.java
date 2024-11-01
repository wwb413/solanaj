package org.p2p.solanaj.parse;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.p2p.solanaj.parse.bean.SwapData;
import org.p2p.solanaj.rpc.types.ConfirmedTransaction;

import java.util.List;

public class JupiterSwapParse{

    public static SwapData parseSwap(ConfirmedTransaction tx, int index) {
        List<JSONObject> list = tx.getMeta().getInnerInstructions().stream().map(vo -> JSONUtil.parseObj(vo)).toList();

        for (int i = 0; i < list.size(); i++) {
            if (index == list.get(i).getInt("index")){

            }
        }

        return null;
    }
}
