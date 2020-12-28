package group24.opponentmodel.core;

import genius.core.Bid;
import genius.core.Domain;
import genius.core.bidding.BidDetails;
import group24.opponentmodel.OpponentModelInterface;
import group24.opponentmodel.core.impl.JonnyBlackOpponentModel;

import java.util.*;

/**
 * the interface of the whole opponent module
 */
public class OpponentModelManager implements OpponentModelInterface {

    private Map<String, OpponentModel> opponentMap;
    private List<String> opponentAgentIdList;

    public OpponentModelManager(int numberOfParties) {
        this.opponentMap = new HashMap<>(numberOfParties);;
        this.opponentAgentIdList = new ArrayList<>(numberOfParties);
    }

    @Override
    public double predict(Bid bid, OpponentModel opponentModel) {
        return opponentModel.predict(bid);
    }

    @Override
    public double predict(Bid bid) {
        double minPredict = 0;
        for (OpponentModel opponentModel : this.opponentMap.values()) {
            double predict = opponentModel.predict(bid);
            if (minPredict == 0 || predict < minPredict) {
                minPredict = predict;
            }
        }
        return minPredict;
    }

    @Override
    public double calculateNash(Bid bid) {
        double nash = 1;
        for (OpponentModel opponentModel : this.opponentMap.values()) {
            nash = nash * opponentModel.predict(bid);
        }

        return nash;
    }

    @Override
    public double calculateDistanceNash(Bid bid, Bid finalNash) {
        double distance = 0;
        for (OpponentModel opponentModel : this.opponentMap.values()) {
            distance = distance + Math.pow(opponentModel.predict(bid) - opponentModel.predict(finalNash), 2);
        }

        return distance;
    }

    @Override
    public Set<BidDetails> queryCommonBestBidsSet(List<BidDetails> allBids, int number_of_bids) {

        if (null == this.opponentMap) {
            return null;
        }

        Set<BidDetails> retainSet = null;
        for (OpponentModel opponentModel : this.opponentMap.values()) {

            // Evaluate every bid in BidsFeasible using the model of each user.
            Collections.sort(allBids, new Comparator<BidDetails>() {
                @Override
                public int compare(BidDetails o1, BidDetails o2) {
                    return Double.compare(opponentModel.predict(o2.getBid()), opponentModel.predict(o1.getBid()));
                }
            });

            // Find Set : the set which contains the N best bids for opponent u.
            Set<BidDetails> bestSet = new HashSet<>(allBids.subList(0, number_of_bids));

            // calculate the common set
            if (null == retainSet) {
                retainSet = bestSet;
            }else {
                retainSet.retainAll(bestSet);
            }
        }

        return retainSet;
    }

    @Override
    public int getOpponentsCounts() {
        return null == opponentAgentIdList? 0 :opponentAgentIdList.size();
    }

    @Override
    public OpponentModel getOpponent(int realOpponentIndex) throws IllegalAccessException {

        if (null == opponentAgentIdList || null == opponentMap) {
            throw new IllegalAccessException("opponent is not init");
        }

        if (realOpponentIndex < 0 || realOpponentIndex > opponentAgentIdList.size() - 1) {
            throw new IllegalAccessException("realOpponentIndex is illegal");
        }

        String agentId = opponentAgentIdList.get(realOpponentIndex);
        return opponentMap.get(agentId);
    }

    @Override
    public void handleBidFromSender(String sender, Bid lastOffer, Domain domain) {

        // check the opponent is existed
        OpponentModel opponentModel = opponentMap.get(sender);

        // if it is not exist, save to list
        if (null == opponentModel) {
            opponentModel = new JonnyBlackOpponentModel(domain);
            opponentAgentIdList.add(sender);
        }
        // update opponent model entity related
        this.dispatchBid(opponentModel, lastOffer);
        // save the relationship of opponent and opponent model
        opponentMap.put(sender, opponentModel);
    }

    protected void dispatchBid(OpponentModel opponentModel, Bid bid) {
        // just forward
        opponentModel.handleBidInternal(bid);
    }
}
