/*
 * Copyright 2025 Firefly Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.firefly.rails.domain.railspecific;

import lombok.Builder;
import lombok.Data;

/** 
 * Cryptocurrency/Blockchain specific attributes.
 * Bitcoin, Ethereum, stablecoins (USDC, USDT), and other crypto assets.
 */
public class CryptoBlockchainSpecific {

    @Data
    @Builder
    public static class CryptoPayment {
        /** Blockchain network */
        private BlockchainNetwork network;
        
        /** Cryptocurrency/token */
        private Cryptocurrency cryptocurrency;
        
        /** From address (wallet address) */
        private String fromAddress;
        
        /** To address (wallet address) */
        private String toAddress;
        
        /** Transaction hash (txHash) */
        private String transactionHash;
        
        /** Block number */
        private Long blockNumber;
        
        /** Block timestamp */
        private String blockTimestamp;
        
        /** Number of confirmations */
        private Integer confirmations;
        
        /** Required confirmations for finality */
        private Integer requiredConfirmations;
        
        /** Gas/transaction fee */
        private GasFee gasFee;
        
        /** Memo/tag (for XRP, XLM, etc.) */
        private String memo;
        
        /** Smart contract interaction */
        private SmartContractInfo smartContractInfo;
        
        /** Token standard (ERC-20, BEP-20, etc.) */
        private String tokenStandard;
    }

    @Data
    @Builder
    public static class GasFee {
        /** Gas fee amount in native currency */
        private String feeAmount;
        
        /** Gas fee currency */
        private String feeCurrency;
        
        /** Gas price (in Gwei for Ethereum) */
        private String gasPrice;
        
        /** Gas limit */
        private Long gasLimit;
        
        /** Gas used */
        private Long gasUsed;
    }

    @Data
    @Builder
    public static class SmartContractInfo {
        /** Contract address */
        private String contractAddress;
        
        /** Contract method called */
        private String method;
        
        /** ABI encoded parameters */
        private String encodedParameters;
        
        /** Event logs */
        private String eventLogs;
    }

    public enum BlockchainNetwork {
        /** Bitcoin mainnet */
        BITCOIN,
        
        /** Ethereum mainnet */
        ETHEREUM,
        
        /** Ethereum Layer 2 - Polygon */
        POLYGON,
        
        /** Ethereum Layer 2 - Arbitrum */
        ARBITRUM,
        
        /** Ethereum Layer 2 - Optimism */
        OPTIMISM,
        
        /** Binance Smart Chain */
        BSC,
        
        /** Solana */
        SOLANA,
        
        /** Cardano */
        CARDANO,
        
        /** Ripple (XRP Ledger) */
        RIPPLE,
        
        /** Stellar */
        STELLAR,
        
        /** Avalanche */
        AVALANCHE,
        
        /** Cosmos */
        COSMOS,
        
        /** Polkadot */
        POLKADOT,
        
        /** Tron */
        TRON,
        
        /** Lightning Network (Bitcoin Layer 2) */
        LIGHTNING_NETWORK
    }

    public enum Cryptocurrency {
        /** Bitcoin */
        BTC,
        
        /** Ethereum */
        ETH,
        
        /** USD Coin (stablecoin) */
        USDC,
        
        /** Tether (stablecoin) */
        USDT,
        
        /** DAI (stablecoin) */
        DAI,
        
        /** Binance Coin */
        BNB,
        
        /** XRP */
        XRP,
        
        /** Cardano */
        ADA,
        
        /** Solana */
        SOL,
        
        /** Polkadot */
        DOT,
        
        /** Avalanche */
        AVAX,
        
        /** Polygon */
        MATIC,
        
        /** Litecoin */
        LTC,
        
        /** Bitcoin Cash */
        BCH,
        
        /** Stellar Lumens */
        XLM,
        
        /** Chainlink */
        LINK,
        
        /** Wrapped Bitcoin */
        WBTC,
        
        /** BUSD (stablecoin) */
        BUSD,
        
        /** USDD (stablecoin) */
        USDD
    }
}
