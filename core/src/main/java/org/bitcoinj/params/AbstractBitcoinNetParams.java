/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bitcoinj.params;

import java.math.BigInteger;

import com.lambdaworks.crypto.SCrypt;
import org.bitcoinj.core.*;
import org.bitcoinj.utils.MonetaryFormat;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.BlockStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.hashengineering.crypto.X11.x11Digest;

/**
 * Parameters for Bitcoin-like networks.
 */
public abstract class AbstractBitcoinNetParams extends NetworkParameters {
    /**
     * Scheme part for Bitcoin URIs.
     */
    public static final String BITCOIN_SCHEME = CoinDefinition.coinURIScheme;

    private static final Logger log = LoggerFactory.getLogger(AbstractBitcoinNetParams.class);

    public AbstractBitcoinNetParams() {
        super();
    }

    /** 
     * Checks if we are at a difficulty transition point. 
     * @param storedPrev The previous stored block 
     * @return If this is a difficulty transition point 
     */
    protected boolean isDifficultyTransitionPoint(StoredBlock storedPrev) {
        return ((storedPrev.getHeight() + 1) % this.getInterval()) == 0;
    }


    /*@Override
    public void checkDifficultyTransitions(final StoredBlock storedPrev, final Block nextBlock,
    	final BlockStore blockStore) throws VerificationException, BlockStoreException {
        Block prev = storedPrev.getHeader();

        // Is this supposed to be a difficulty transition point?
        if (!isDifficultyTransitionPoint(storedPrev)) {

            // No ... so check the difficulty didn't actually change.
            if (nextBlock.getDifficultyTarget() != prev.getDifficultyTarget())
                throw new VerificationException("Unexpected change in difficulty at height " + storedPrev.getHeight() +
                        ": " + Long.toHexString(nextBlock.getDifficultyTarget()) + " vs " +
                        Long.toHexString(prev.getDifficultyTarget()));
            return;
        }

        // We need to find a block far back in the chain. It's OK that this is expensive because it only occurs every
        // two weeks after the initial block chain download.
        long now = System.currentTimeMillis();
        StoredBlock cursor = blockStore.get(prev.getHash());
        for (int i = 0; i < this.getInterval() - 1; i++) {
            if (cursor == null) {
                // This should never happen. If it does, it means we are following an incorrect or busted chain.
                throw new VerificationException(
                        "Difficulty transition point but we did not find a way back to the genesis block.");
            }
            cursor = blockStore.get(cursor.getHeader().getPrevBlockHash());
        }
        long elapsed = System.currentTimeMillis() - now;
        if (elapsed > 50)
            log.info("Difficulty transition traversal took {}msec", elapsed);

        Block blockIntervalAgo = cursor.getHeader();
        int timespan = (int) (prev.getTimeSeconds() - blockIntervalAgo.getTimeSeconds());
        // Limit the adjustment step.
        final int targetTimespan = this.getTargetTimespan();
        if (timespan < targetTimespan / 4)
            timespan = targetTimespan / 4;
        if (timespan > targetTimespan * 4)
            timespan = targetTimespan * 4;

        BigInteger newTarget = Utils.decodeCompactBits(prev.getDifficultyTarget());
        newTarget = newTarget.multiply(BigInteger.valueOf(timespan));
        newTarget = newTarget.divide(BigInteger.valueOf(targetTimespan));

        if (newTarget.compareTo(this.getMaxTarget()) > 0) {
            log.info("Difficulty hit proof of work limit: {}", newTarget.toString(16));
            newTarget = this.getMaxTarget();
        }

        int accuracyBytes = (int) (nextBlock.getDifficultyTarget() >>> 24) - 3;
        long receivedTargetCompact = nextBlock.getDifficultyTarget();

        // The calculated difficulty is to a higher precision than received, so reduce here.
        BigInteger mask = BigInteger.valueOf(0xFFFFFFL).shiftLeft(accuracyBytes * 8);
        newTarget = newTarget.and(mask);
        long newTargetCompact = Utils.encodeCompactBits(newTarget);

        if (newTargetCompact != receivedTargetCompact)
            throw new VerificationException("Network provided difficulty bits do not match what was calculated: " +
                    newTargetCompact + " vs " + receivedTargetCompact);
    }*/
    @Override
    public void checkDifficultyTransitions(StoredBlock storedPrev, Block nextBlock, BlockStore blockStore) throws BlockStoreException, VerificationException {
        if (this.getId().equals(NetworkParameters.ID_TESTNET))
        {
            Block prev = storedPrev.getHeader();
            checkTestnetDifficulty(storedPrev, prev, nextBlock, blockStore);
        }
        else if(storedPrev.getHeight()+1 < CoinDefinition.V3_FORK)
        {
            checkDifficultyTransitionsV1(storedPrev,nextBlock, blockStore);
        }
        else
        {
            checkDifficultyTransitionsV2(storedPrev, nextBlock, blockStore);
        }


    }
    private void checkDifficultyTransitionsV1(StoredBlock storedPrev, Block nextBlock, BlockStore blockStore) throws BlockStoreException, VerificationException {

        Block prev = storedPrev.getHeader();

        int nDifficultySwitchHeight = 476280;
        int nInflationFixHeight = 523800;
        int nDifficultySwitchHeightTwo = 625800;

        boolean fNewDifficultyProtocol = ((storedPrev.getHeight() + 1) >= nDifficultySwitchHeight);
        boolean fInflationFixProtocol = ((storedPrev.getHeight() + 1) >= nInflationFixHeight);
        boolean fDifficultySwitchHeightTwo = ((storedPrev.getHeight() + 1) >= nDifficultySwitchHeightTwo /*|| fTestNet*/);

        int nTargetTimespanCurrent = fInflationFixProtocol? this.targetTimespan : (this.targetTimespan*5);
        int interval = fInflationFixProtocol? (nTargetTimespanCurrent / this.TARGET_SPACING) : (nTargetTimespanCurrent / (this.TARGET_SPACING / 2));

        // Is this supposed to be a difficulty transition point?
        if ((storedPrev.getHeight() + 1) % interval != 0 &&
                (storedPrev.getHeight() + 1) != nDifficultySwitchHeight)
        {

            // TODO: Refactor this hack after 0.5 is released and we stop supporting deserialization compatibility.
            // This should be a method of the NetworkParameters, which should in turn be using singletons and a subclass
            // for each network type. Then each network can define its own difficulty transition rules.
            /*if (this.getId().equals(NetworkParameters.ID_TESTNET) && nextBlock.getTime().after(testnetDiffDate)) {
                checkTestnetDifficulty(storedPrev, prev, nextBlock);
                return;
            }*/

            // No ... so check the difficulty didn't actually change.
            if (nextBlock.getDifficultyTarget() != prev.getDifficultyTarget())
                throw new VerificationException("Unexpected change in difficulty at height " + storedPrev.getHeight() +
                        ": " + Long.toHexString(nextBlock.getDifficultyTarget()) + " vs " +
                        Long.toHexString(prev.getDifficultyTarget()) + ", Interval: "+interval);
            return;
        }

        // We need to find a block far back in the chain. It's OK that this is expensive because it only occurs every
        // two weeks after the initial block chain download.
        long now = System.currentTimeMillis();
        StoredBlock cursor = blockStore.get(prev.getHash());

        int goBack = interval - 1;
        if (cursor.getHeight()+1 != interval)
            goBack = interval;

        for (int i = 0; i < goBack; i++) {
            if (cursor == null) {
                // This should never happen. If it does, it means we are following an incorrect or busted chain.
                throw new VerificationException(
                        "Difficulty transition point but we did not find a way back to the genesis block.");
            }
            cursor = blockStore.get(cursor.getHeader().getPrevBlockHash());
        }
        long elapsed = System.currentTimeMillis() - now;
        if (elapsed > 50)
            log.info("Difficulty transition traversal took {}msec", elapsed);

        // Check if our cursor is null.  If it is, we've used checkpoints to restore.
        if(cursor == null) return;

        Block blockIntervalAgo = cursor.getHeader();
        int timespan = (int) (prev.getTimeSeconds() - blockIntervalAgo.getTimeSeconds());
        // Limit the adjustment step.

        int nActualTimespanMax = fNewDifficultyProtocol? (nTargetTimespanCurrent*2) : (nTargetTimespanCurrent*4);
        int nActualTimespanMin = fNewDifficultyProtocol? (nTargetTimespanCurrent/2) : (nTargetTimespanCurrent/4);

        //new for v1.0.0
        if (fDifficultySwitchHeightTwo){
            nActualTimespanMax = ((nTargetTimespanCurrent*75)/60);
            nActualTimespanMin = ((nTargetTimespanCurrent*55)/73);
        }

        if (timespan < nActualTimespanMin)
            timespan = nActualTimespanMin;
        if (timespan > nActualTimespanMax)
            timespan = nActualTimespanMax;

        BigInteger newDifficulty = Utils.decodeCompactBits(prev.getDifficultyTarget());
        newDifficulty = newDifficulty.multiply(BigInteger.valueOf(timespan));
        newDifficulty = newDifficulty.divide(BigInteger.valueOf(nTargetTimespanCurrent));

        if (newDifficulty.compareTo(this.maxTarget) > 0) {
            log.info("Difficulty hit proof of work limit: {}", newDifficulty.toString(16));
            newDifficulty = this.maxTarget;
        }

        int accuracyBytes = (int) (nextBlock.getDifficultyTarget() >>> 24) - 3;
        BigInteger receivedDifficulty = nextBlock.getDifficultyTargetAsInteger();

        // The calculated difficulty is to a higher precision than received, so reduce here.
        BigInteger mask = BigInteger.valueOf(0xFFFFFFL).shiftLeft(accuracyBytes * 8);
        newDifficulty = newDifficulty.and(mask);

        if (newDifficulty.compareTo(receivedDifficulty) != 0)
            throw new VerificationException("Network provided difficulty bits do not match what was calculated: " +
                    receivedDifficulty.toString(16) + " vs " + newDifficulty.toString(16));
    }
    private StoredBlock GetLastBlockForAlgo(StoredBlock block, int algo, BlockStore blockStore)
    {

        for(;;)
        {
            if(block.getHeight() < CoinDefinition.V3_FORK && (algo == ALGO_SHA256D||algo == ALGO_X11))
            {
                return null;
            }
            if(block == null || block.getHeader().getPrevBlockHash().equals(Sha256Hash.ZERO_HASH))
                return null;
            if(getAlgo(block.getHeader()) == algo)
                return block;
            try {
                block = block.getPrev(blockStore);
            }
            catch(BlockStoreException x)
            {
                return null;
            }
        }

    }
    //MultiAlgo Target updates
    final long multiAlgoTargetTimespan = 120; // 2 minutes (NUM_ALGOS(3) * 40 seconds)
    final long multiAlgoTargetSpacing = 120; // 2 minutes (NUM_ALGOS * 30 seconds)
    final long multiAlgoInterval = 1; // retargets every blocks

    final long nAveragingInterval = 10; // 10 blocks
    final long nAveragingTargetTimespan = nAveragingInterval * multiAlgoTargetSpacing; // 20 minutes

    final long nMaxAdjustDown = 40; // 40% adjustment down
    final long nMaxAdjustUp = 20; // 20% adjustment up

    final long nTargetTimespanAdjDown = multiAlgoTargetTimespan * (100 + nMaxAdjustDown) / 100;
    final long nLocalDifficultyAdjustment = 40; // 40% down, 20% up

    final long nMinActualTimespan = nAveragingTargetTimespan * (100 - nMaxAdjustUp) / 100;
    final long nMaxActualTimespan = nAveragingTargetTimespan * (100 + nMaxAdjustDown) / 100;

    private void checkDifficultyTransitionsV2(StoredBlock storedPrev, Block nextBlock, BlockStore blockStore) throws BlockStoreException, VerificationException {

        //Block prev = storedPrev.getHeader();
        int algo = getAlgo(nextBlock);
        BigInteger proofOfWorkLimit = CoinDefinition.getProofOfWorkLimit(algo);


        StoredBlock first = storedPrev;
        for (int i = 0; first != null && i < NUM_ALGOS * nAveragingInterval; i++) {
            first = first.getPrev(blockStore);
        }
        if (first == null)
            return; //using checkpoints file
        StoredBlock lastBlockSolved = GetLastBlockForAlgo(storedPrev, algo, blockStore);
        if (lastBlockSolved == null) {
            //verifyDifficulty(storedPrev.getHeader().getDifficultyTargetAsInteger(), storedPrev, nextBlock, algo);
            return;
        }


        // Limit adjustment step
        // Use medians to prevent time-warp attacks
        try {
            long nActualTimespan = blockStore.getMedianTimePast(storedPrev) - blockStore.getMedianTimePast(first);
            nActualTimespan = nAveragingTargetTimespan + (nActualTimespan - nAveragingTargetTimespan) / 6;
            //LogPrintf("  nActualTimespan = %d before bounds\n", nActualTimespan);
            if (nActualTimespan < nMinActualTimespan)
                nActualTimespan = nMinActualTimespan;
            if (nActualTimespan > nMaxActualTimespan)
                nActualTimespan = nMaxActualTimespan;


            // Global retarget
            BigInteger newDifficulty;
            newDifficulty = lastBlockSolved.getHeader().getDifficultyTargetAsInteger();
            newDifficulty = newDifficulty.multiply(BigInteger.valueOf(nActualTimespan));
            newDifficulty = newDifficulty.divide(BigInteger.valueOf(nAveragingTargetTimespan));

            // Per-algo retarget
            int nAdjustments = lastBlockSolved.getHeight() - storedPrev.getHeight() + NUM_ALGOS - 1;
            if (nAdjustments > 0) {
                for (int i = 0; i < nAdjustments; i++) {
                    //bnNew /= 100 + nLocalDifficultyAdjustment;
                    newDifficulty = newDifficulty.divide(BigInteger.valueOf(100 + nLocalDifficultyAdjustment));
                    //bnNew *= 100;
                    newDifficulty = newDifficulty.multiply(BigInteger.valueOf(100));
                }
            }
            if (nAdjustments < 0) {
                for (int i = 0; i < -nAdjustments; i++) {
                    //bnNew *= 100 + nLocalDifficultyAdjustment;
                    newDifficulty = newDifficulty.multiply(BigInteger.valueOf(100 + nLocalDifficultyAdjustment));
                    //bnNew /= 100;
                    newDifficulty = newDifficulty.divide(BigInteger.valueOf(100));
                }
            }

            verifyDifficulty(newDifficulty, storedPrev, nextBlock, algo);
        } catch (BlockStoreException x) {
            //loading from checkpoints
        }
    }

    private void verifyDifficulty(BigInteger calcDiff, StoredBlock storedPrev, Block nextBlock, int algo)
    {
        if (calcDiff.compareTo(CoinDefinition.getProofOfWorkLimit(algo)) > 0) {
            log.info("Difficulty hit proof of work limit: {}", calcDiff.toString(16));
            calcDiff = CoinDefinition.getProofOfWorkLimit(algo);
        }
        int accuracyBytes = (int) (nextBlock.getDifficultyTarget() >>> 24) - 3;
        BigInteger receivedDifficulty = nextBlock.getDifficultyTargetAsInteger();

        // The calculated difficulty is to a higher precision than received, so reduce here.
        BigInteger mask = BigInteger.valueOf(0xFFFFFFL).shiftLeft(accuracyBytes * 8);
        calcDiff = calcDiff.and(mask);

        if (calcDiff.compareTo(receivedDifficulty) != 0)
            throw new VerificationException("Network provided difficulty bits do not match what was calculated: " +
                    receivedDifficulty.toString(16) + " vs " + calcDiff.toString(16));
    }

    public boolean checkTestnetDifficulty(StoredBlock storedPrev, Block prev, Block next, BlockStore blockStore) throws VerificationException, BlockStoreException {
        verifyDifficulty(Utils.decodeCompactBits(0x1d13ffec), storedPrev, next, getAlgo(next));
        return true;
    }

    @Override
    public Coin getMaxMoney() {
        return MAX_MONEY;
    }

    @Override
    public Coin getMinNonDustOutput() {
        return Transaction.MIN_NONDUST_OUTPUT;
    }

    @Override
    public MonetaryFormat getMonetaryFormat() {
        return new MonetaryFormat();
    }

    @Override
    public String getUriScheme() {
        return BITCOIN_SCHEME;
    }

    @Override
    public boolean hasMaxMoney() {
        return true;
    }

    public static final int ALGO_SHA256D = 0;
    public static final int ALGO_SCRYPT  = 1;
    public static final int ALGO_X11 = 2;
    public static final int NUM_ALGOS = 3;

    public static int BLOCK_VERSION_DEFAULT = 1;

    // algo
    public static final int             BLOCK_VERSION_ALGO           = (7 << 9);
    public static final int             BLOCK_VERSION_SHA256D         = (1 << 9);
    public static final int             BLOCK_VERSION_X11        = (2 << 9);


    public static int getAlgo(long nVersion)
    {
        switch ((int)nVersion & BLOCK_VERSION_ALGO)
        {
            case 1:
                return ALGO_SCRYPT;
            case BLOCK_VERSION_SHA256D:
                return ALGO_SHA256D;
            case BLOCK_VERSION_X11:
                return ALGO_X11;
        }
        return ALGO_SCRYPT;
    }

    public static int getAlgo(Block block)
    {
        return getAlgo(block.getVersion());
    }
    protected static String [] algoNames = {"sha256d", "scrypt", "x11"};

    public static String getAlgoName(Block block) { return algoNames[getAlgo(block.getVersion())]; }

    public Sha256Hash getProofOfWork(Block header)
    {
        int algo = getAlgo(header);
        Sha256Hash hash;
        switch (algo)
        {
            case ALGO_SHA256D:
                hash = header.getHash();
                break;
            case ALGO_SCRYPT:
            {
                hash = calculateScryptHash(header);
                break;
            }
            case ALGO_X11:
                hash = calculateX11Hash(header);

                break;
            default:
                hash = header.getHash();
                break;
        }
        return hash;
    }
    private Sha256Hash calculateScryptHash(Block block) {
        byte [] bos = block.cloneAsHeader().bitcoinSerialize();
        return new Sha256Hash(Utils.reverseBytes(scryptDigest(bos)));
    }
    private Sha256Hash calculateX11Hash(Block block) {
        byte [] bos = block.cloneAsHeader().bitcoinSerialize();
        return new Sha256Hash(Utils.reverseBytes(x11Digest(bos)));

    }
    public static byte[] scryptDigest(byte[] input) {
        try {
            return SCrypt.scrypt(input, input, 1024, 1, 1, 32);
        } catch (Exception e) {
            return null;
        }
    }

    //main.cpp GetBlockValue(height, fee)
    public static Coin getBlockInflation(int height)
    {

        Coin subsidy = Coin.valueOf(15, 0);

        if(height < 1080)
        {
            subsidy = Coin.valueOf(2, 0); //2
        }
        else if(height < 2160)
        {
            subsidy   = Coin.valueOf(1, 0); //2
        }
        else if(height < 3240)
        {
            subsidy   = Coin.valueOf(2, 0); //2
        }
        else if(height < 4320)
        {
            subsidy  = Coin.valueOf(5, 0); //5
        }
        else if(height < 5400)
        {
            subsidy  = Coin.valueOf(8, 0); //8
        }
        else if(height < 6480)
        {
            subsidy = Coin.valueOf(11, 0); //11
        }
        else if(height < 7560)
        {
            subsidy  = Coin.valueOf(14, 0); //14
        }
        else if(height < 8640)
        {
            subsidy = Coin.valueOf(17, 0); //17
        }
        else if(height < 523800)
        {
            subsidy = Coin.valueOf(20, 0); //20
        }
        else if(height >= CoinDefinition.V3_FORK)
        {
            subsidy = Coin.valueOf(5, 0); //5;
        }
        else
        {
            return subsidy.shiftRight(height / CoinDefinition.subsidyDecreaseBlockCount);
        }
        return subsidy;
    }

}
