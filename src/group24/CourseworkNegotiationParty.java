package group24;

import genius.core.Bid;
import genius.core.bidding.BidDetails;
import genius.core.boaframework.SortedOutcomeSpace;
import genius.core.parties.AbstractNegotiationParty;
import genius.core.uncertainty.User;
import genius.core.uncertainty.UserModel;
import genius.core.utility.UncertainAdditiveUtilitySpace;
import group24.acceptance.AcceptanceStrategy;
import group24.offering.OfferingStrategy;
import group24.opponentmodel.OpponentModelInterface;
import group24.usermodel.UserModelInterface;
import java.util.List;

/**
 * base class of negotiation party for Coursework
 * includes all common field and method
 */
public abstract class CourseworkNegotiationParty extends AbstractNegotiationParty {

    // parameters
    protected double Minimum_Offer_Threshold = 0.6;
    protected double Agreement_Value = 0.8;
    protected double Care_Value = 0.4;
    protected int Number_of_Bids = 5;
    protected double Reluctance = 1.1;

    protected Bid lastOffer;
    protected List<BidDetails> feasibleBids;
    protected SortedOutcomeSpace sortedOutcomeSpace;

    protected OfferingStrategy offeringStrategy;
    protected AcceptanceStrategy acceptanceStrategy;

    protected OpponentModelInterface opponentModelInterface;
    protected UserModelInterface userModelInterface;

    public double getMinimum_Offer_Threshold() {
        return Minimum_Offer_Threshold;
    }

    public double getAgreement_Value() {
        return Agreement_Value;
    }

    public double getCare_Value() {
        return Care_Value;
    }

    public int getNumber_of_Bids() {
        return Number_of_Bids;
    }

    public double getReluctance() {
        return Reluctance;
    }

    public List<BidDetails> getFeasibleBids() {
        return feasibleBids;
    }

    public SortedOutcomeSpace getSortedOutcomeSpace() {
        return sortedOutcomeSpace;
    }

    public User getUser() {
        return user;
    }

    public void setUserModel(UserModel userModel) {
        this.userModel = userModel;
    }

    public UncertainAdditiveUtilitySpace getRealUSpace() {
        return userModelInterface.queryRealUtilitySpace();
    }
}
