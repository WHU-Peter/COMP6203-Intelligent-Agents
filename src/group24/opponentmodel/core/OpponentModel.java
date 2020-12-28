package group24.opponentmodel.core;

import genius.core.Bid;
import genius.core.Domain;
import genius.core.utility.AdditiveUtilitySpace;
import java.util.*;

/**
 * opponent model entity
 */
public abstract class OpponentModel {

    protected Domain domain;

    protected AdditiveUtilitySpace additiveUtilitySpace;

    protected List<Bid> bidHistory;

    public OpponentModel(Domain domain) {
        this.domain = domain;
        additiveUtilitySpace = new AdditiveUtilitySpace(domain);
        bidHistory = new ArrayList<>();
    }

    public abstract void handleBidInternal(Bid bid);

    public double predict(Bid bid) {
        return additiveUtilitySpace.getUtility(bid);
    }
}
