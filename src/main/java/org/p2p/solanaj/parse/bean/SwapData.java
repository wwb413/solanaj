package org.p2p.solanaj.parse.bean;

import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;

@Data
public class SwapData {

    private String inputMint;

    private long inputAmount;

    private int inputMintDecimals;

    private String outputMint;

    private long outputAmount;

    private int outputMintDecimals;

    public SwapData() {
    }

    public SwapData(String inputMint, long inputAmount, int inputMintDecimals, String outputMint, long outputAmount, int outputMintDecimals) {
        this.inputMint = inputMint;
        this.inputAmount = inputAmount;
        this.inputMintDecimals = inputMintDecimals;
        this.outputMint = outputMint;
        this.outputAmount = outputAmount;
        this.outputMintDecimals = outputMintDecimals;
    }

    @Override
    public String toString() {
        return "SwapData{" +
                "inputMint='" + inputMint + '\'' +
                ", inputAmount=" + inputAmount +
                ", inputMintDecimals=" + inputMintDecimals +
                ", outputMint='" + outputMint + '\'' +
                ", outputAmount=" + outputAmount +
                ", outputMintDecimals=" + outputMintDecimals +
                '}';
    }
}
