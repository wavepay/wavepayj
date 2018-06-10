package org.bitcoinj.core;

import java.math.BigInteger;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: wavepay
 * Date: 8/13/13
 * Time: 7:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class CoinDefinition {


    public static final String coinName = "wavepay";
    public static final String coinTicker = "DGC";
    public static final String coinURIScheme = "wavepay";
    //public static final String cryptsyMarketId = "26";
    //public static final String cryptsyMarketCurrency = "BTC";
    public static final String PATTERN_PRIVATE_KEY_START = "6";
    public static final String PATTERN_PRIVATE_KEY_START_COMPRESSED = "[Q]";
    public static final String PATTERN_PRIVATE_KEY_START_TESTNET = "9";
    public static final String PATTERN_PRIVATE_KEY_START_COMPRESSED_TESTNET = "c";

    public static String lowerCaseCoinName() { return coinName.toLowerCase(); }

    public enum CoinPrecision {
        Coins,
        Millicoins,
    }
    public static final CoinPrecision coinPrecision = CoinPrecision.Coins;


    public static final String BLOCKEXPLORER_BASE_URL_PROD = "http://explorer.wavepay.org/";    //explorer.wavepay.org
    public static final String BLOCKEXPLORER_ADDRESS_PATH = "address/info/";             //explorer.wavepay.org path
    public static final String BLOCKEXPLORER_TRANSACTION_PATH = "tx/info/";              //explorer.wavepay.org path
    public static final String BLOCKEXPLORER_BLOCK_PATH = "block/info/";                 //explorer.wavepay.org path
    public static final String BLOCKEXPLORER_BASE_URL_TEST = BLOCKEXPLORER_BASE_URL_PROD;

    public static final String DONATION_ADDRESS = "Wdo2CtRXjYkQYZPnBaPNcQW1gaFmYCyfmt";  //wavepay donation DGC address

    public static final String UNSPENT_API_URL = "http://explorer.wavepay.org/api/v1/address/unspent/";
    public enum UnspentAPIType {
        BitEasy,
        Blockr,
        Abe
    };
    public static final UnspentAPIType UnspentAPI = UnspentAPIType.Blockr;

    enum CoinHash {
        SHA256,
        scrypt,
	x11
    };
    public static final CoinHash coinPOWHash = CoinHash.x11;

    public static boolean checkpointFileSupport = false;
    public static int checkpointDaysBack = 21;
    //Original Values
    public static final int TARGET_TIMESPAN_0 = (int)(3 * 60);  // 3 minutes per difficulty cycle, on average.
    public static final int TARGET_SPACING_0 = (int)(3 * 60);  // 180 seconds per block.
    public static final int INTERVAL_0 = TARGET_TIMESPAN_0 / TARGET_SPACING_0;  //1080 blocks

    public static final int TARGET_TIMESPAN_1 = (int)(108 * 20);  // 36 minutes per difficulty cycle, on average.
    public static final int TARGET_SPACING_1 = (int)(1 * 20);  // 20 seconds per block.
    public static final int INTERVAL_1 = TARGET_TIMESPAN_1 / TARGET_SPACING_1;  //108 blocks

    public static final int TARGET_TIMESPAN = (int)(3 * 60);  // 3 minutes per difficulty cycle, on average.
    public static final int TARGET_SPACING = (int)(3 * 60);  // 180 seconds per block.
    public static final int INTERVAL = TARGET_TIMESPAN / TARGET_SPACING;  //108 blocks

    private static int nDifficultySwitchHeight = 3000000;    //retarget every 108 instead of 1080 blocks; adjust by +100%/-50% instead of +400/-75%
    private static int nInflationFixHeight = 523800;        //increase block time to 40 from 20 seconds; decrease reward from 20 to 15 DGC
    private static int nDifficultySwitchHeightTwo = 3500000; //retarget adjust changed
    public static final int V3_FORK = 1028000;
    public static final int MAX_BLOCK_ALGO_COUNT = 3;



    public static final int getInterval(int height, boolean testNet) {
        if(height < nDifficultySwitchHeight)
            return INTERVAL_0;    //1080
        else
            return INTERVAL;      //108
    }
    public static final int getIntervalCheckpoints() {
            return INTERVAL_0*3;    //1080

    }
    public static final int getTargetTimespan(int height, boolean testNet) {
        if(height < nDifficultySwitchHeight)
            return TARGET_TIMESPAN_0;  //3.5 days
        else
            return TARGET_TIMESPAN;    //72 min
    }
    public static int getMaxTimeSpan(int value, int height, boolean testNet)
    {
        if(height < nDifficultySwitchHeight)
            return value * 4;
        else if(height < nInflationFixHeight)
            return value * 2;
        else
            return value * 75 / 60;
    }
    public static int getMinTimeSpan(int value, int height, boolean testNet)
    {
        if(height < nDifficultySwitchHeight)
            return value / 4;
        else if(height < nInflationFixHeight)
            return value / 2;
        else
            return value * 55 / 73;
    }
    public static int spendableCoinbaseDepth = 100; //main.h: static const int COINBASE_MATURITY
    public static final int MAX_MONEY = 25000000;//).multiply(Utils.COIN);                 //main.h:  MAX_MONEY
    //public static final String MAX_MONEY_STRING = "200000000";     //main.h:  MAX_MONEY

    public static final Coin DEFAULT_MIN_TX_FEE = Coin.valueOf(10000000);   // MIN_TX_FEE
    public static final Coin DUST_LIMIT = Coin.valueOf(1000000); //main.h CTransaction::GetMinFee        0.01 coins

    public static final int PROTOCOL_VERSION = 20000;          //version.h PROTOCOL_VERSION
    public static final int MIN_PROTOCOL_VERSION = 20000;        //version.h MIN_PROTO_VERSION - eliminate 60001 which are on the wrong fork
    public static final int INIT_PROTO_VERSION = 208;            //version.h

    public static final int BLOCK_CURRENTVERSION = 3;   //CBlock::CURRENT_VERSION
    public static final int MAX_BLOCK_SIZE = 1 * 1000 * 1000;


    public static final boolean supportsBloomFiltering = true; //Requires PROTOCOL_VERSION 20000 in the client
    public static boolean supportsIrcDiscovery() {
        return PROTOCOL_VERSION <= 20000;
    }

    public static final int Port    = 5279;       //protocol.h GetDefaultPort(testnet=false)
    public static final int TestPort = 15279;     //protocol.h GetDefaultPort(testnet=true)

    //
    //  Production
    //
    public static final int AddressHeader = 73;             //base58.h CBitcoinAddress::PUBKEY_ADDRESS
    public static final int p2shHeader = 1;             //base58.h CBitcoinAddress::SCRIPT_ADDRESS
    public static final boolean allowBitcoinPrivateKey = true; //for backward compatibility with previous version of wavepay
    public static final long PacketMagic = 0xefbeeeaf;      //0xfb, 0xc0, 0xb6, 0xdb

    //Genesis Block Information from main.cpp: LoadBlockIndex
    static public long genesisBlockDifficultyTarget = (0x1e0ffff0L);         //main.cpp: LoadBlockIndex
    static public long genesisBlockTime = 1528207200L;                       //main.cpp: LoadBlockIndex
    static public long genesisBlockNonce = (3250);                         //main.cpp: LoadBlockIndex
    static public String genesisHash = "825917d024dfbb86f1276f271a0366878a8a9bd3b43497f3c1f7790e07d22185"; //main.cpp: hashGenesisBlock
    static public int genesisBlockValue = 1;                                                              //main.cpp: LoadBlockIndex
    //taken from the raw data of the block explorer
    static public String genesisTxInBytes = "04ffff001d0104294469676974616c636f696e2c20412043757272656e637920666f722061204469676974616c20416765";   //"Wavepay, A Currency for a wave Age"
    static public String genesisTxOutBytes = "04A5902C295770787BA3234C753A846F5FEC7954BEFDB3A6114115C0DA6B09529B64F64C577ADA2B41815D29F73BEAC05E4A13E7CC97145FEBCE37B35F688479AB";

    //net.cpp strDNSSeed
    static public String[] dnsSeeds = new String[] {
            "wavepay.org",
            "node.wavepay.org",
            "explorer.wavepay.org",
            "45.64.254.98",
            "95.211.224.212",
            "192.95.33.131",
            "172.93.49.129"

     };

    public static int minBroadcastConnections = 1;   //0 for default; we need more peers.

    //
    // TestNet - wavepay - not tested
    //
    public static final boolean supportsTestNet = false;
    public static final int testnetAddressHeader = 74;             //base58.h CBitcoinAddress::PUBKEY_ADDRESS_TEST
    public static final int testnetp2shHeader = 1;             //base58.h CBitcoinAddress::SCRIPT_ADDRESS_TEST
    public static final long testnetPacketMagic = 0xf4aef2bf;      //0xfc, 0xc1, 0xb7, 0xdc
    public static final String testnetGenesisHash = "c66613dc024d139a2745ca29d51395e0b494fd536e27bba8f70c536b4c15c763";
    static public long testnetGenesisBlockDifficultyTarget = (0x1e0ffff0L);         //main.cpp: LoadBlockIndex
    static public long testnetGenesisBlockTime = 1527344400L;                       //main.cpp: LoadBlockIndex
    static public long testnetGenesisBlockNonce = (56359);                         //main.cpp: LoadBlockIndex





    public static final boolean usingNewDifficultyProtocol(int height)
    { return height >= nDifficultySwitchHeight;}

    public static final boolean usingInflationFixProtocol(int height)
    { return height >= nInflationFixHeight;}

    //main.cpp GetBlockValue(height, fee)
    /*public static final BigInteger GetBlockReward(int height)
    {
        int COIN = 1;
        BigInteger nSubsidy = Utils.toNanoCoins(15, 0);

        if(height < 1080)
        {
            nSubsidy = Utils.toNanoCoins(2, 0); //2
        }
        else if(height < 2160)
        {
            nSubsidy   = Utils.toNanoCoins(1, 0); //2
        }
        else if(height < 3240)
        {
            nSubsidy   = Utils.toNanoCoins(2, 0); //2
        }
        else if(height < 4320)
        {
            nSubsidy  = Utils.toNanoCoins(5, 0); //5
        }
        else if(height < 5400)
        {
            nSubsidy  = Utils.toNanoCoins(8, 0); //8
        }
        else if(height < 6480)
        {
            nSubsidy = Utils.toNanoCoins(11, 0); //11
        }
        else if(height < 7560)
        {
            nSubsidy  = Utils.toNanoCoins(14, 0); //14
        }
        else if(height < 8640)
        {
            nSubsidy = Utils.toNanoCoins(17, 0); //17
        }
        else if(height < 523800)
        {
            nSubsidy = Utils.toNanoCoins(20, 0); //20
        }
        else if(height >= V3_FORK)
        {
            nSubsidy = Utils.toNanoCoins(5, 0); //5;
        }
        else
        {
            return nSubsidy.shiftRight(height / subsidyDecreaseBlockCount);
        }
        return nSubsidy;
    } */

    public static int subsidyDecreaseBlockCount = 4730400;     //main.cpp GetBlockValue(height, fee)

    public static BigInteger proofOfWorkLimit = Utils.decodeCompactBits(0x1e0fffffL);  //main.cpp bnProofOfWorkLimit (~uint256(0) >> 20); // wavepay: starting difficulty is 1 / 2^12

    public static BigInteger [] proofOfWorkLimits = new BigInteger[] {
            proofOfWorkLimit,proofOfWorkLimit,proofOfWorkLimit,proofOfWorkLimit,proofOfWorkLimit };

    public static BigInteger getProofOfWorkLimit(int algo)
    {
        return proofOfWorkLimits[algo];
    }

    static public String[] testnetDnsSeeds = new String[] {
          "not supported"
    };
    //from main.h: CAlert::CheckSignature
    public static final String SATOSHI_KEY = "04A5902C295770787BA3234C753A846F5FEC7954BEFDB3A6114115C0DA6B09529B64F64C577ADA2B41815D29F73BEAC05E4A13E7CC97145FEBCE37B35F688479AB";
    public static final String TESTNET_SATOSHI_KEY = "";

    /** The string returned by getId() for the main, production network where people trade things. */
    public static final String ID_MAINNET = "org.wavepay.production";
    /** The string returned by getId() for the testnet. */
    public static final String ID_TESTNET = "org.wavepay.test";
    /** Unit test network. */
    public static final String ID_UNITTESTNET = "com.google.wavepay.unittest";

    //checkpoints.cpp Checkpoints::mapCheckpoints
    public static void initCheckpoints(Map<Integer, Sha256Hash> checkpoints)
    {
        checkpoints.put( 0, new Sha256Hash("825917d024dfbb86f1276f271a0366878a8a9bd3b43497f3c1f7790e07d22185"));
        checkpoints.put( 1, new Sha256Hash("0000070d0086f008db30ecab4d78bcf0ab389ef4d7de6e22382c253a52b831a9"));
 
    }

    //Unit Test Information
    public static final String UNITTEST_ADDRESS = "WWbmzvCoXjBUgmcCCwbLfFXMy6RLgo89Gz";
    public static final String UNITTEST_ADDRESS_PRIVATE_KEY = "D2k5NYF2vKTr3qG17kY1kpySd5GrVeXmP3fBd1uP8Pog88ibcazH";

}
