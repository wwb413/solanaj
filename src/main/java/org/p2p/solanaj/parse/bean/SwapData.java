package org.p2p.solanaj.parse.bean;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SwapData {

    private String inputToken;

    private BigDecimal inputAmount;

    private int inputTokenDecimals;

    private String outputToken;

    private BigDecimal outputAmount;

    private int outputTokenDecimals;


}
