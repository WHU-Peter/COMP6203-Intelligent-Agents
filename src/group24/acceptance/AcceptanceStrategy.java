package group24.acceptance;

import genius.core.Bid;
import genius.core.actions.Action;

public abstract class AcceptanceStrategy {

    /**
     * handle the bid from opponent, decide whether to accept
     * @param lastOffer
     * @return
     */
    public abstract Action handleOfferFromOpponent(Bid lastOffer);
}
