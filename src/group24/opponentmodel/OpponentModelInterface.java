package group24.opponentmodel;

import genius.core.Bid;
import genius.core.Domain;
import genius.core.bidding.BidDetails;
import group24.opponentmodel.core.OpponentModel;

import java.util.List;
import java.util.Set;

public interface OpponentModelInterface {

    /**
     * update opponent model by new bid from opponent
     * @param sender
     * @param lastOffer
     * @param domain
     */
    void handleBidFromSender(String sender, Bid lastOffer, Domain domain);

    /**
     * query the common set among all the opponent, top number_of_bids big items.
     * @param allBids
     * @param number_of_bids
     * @return
     */
    Set<BidDetails> queryCommonBestBidsSet(List<BidDetails> allBids, int number_of_bids);

    /**
     * query the number of opponents
     * @return
     */
    int getOpponentsCounts();

    /**
     * query the opponent model entity by index
     * @param realOpponentIndex
     * @return
     * @throws IllegalAccessException
     */
    OpponentModel getOpponent(int realOpponentIndex) throws IllegalAccessException;

    /**
     * calculate the predict utility by bid and opponent model entity
     * @param bid
     * @param opponent
     * @return
     */
    double predict(Bid bid, OpponentModel opponent);

    double predict(Bid bid);

    double calculateNash(Bid bid);

    double calculateDistanceNash(Bid o1, Bid finalNash);
}
