package group24.offering;

import genius.core.actions.Action;
import group24.opponentmodel.OpponentModelInterface;

public abstract class OfferingStrategy {

    /**
     * decide what bid can be offer
     * @param opponentModelInterface
     * @return
     */
    public abstract Action offer(OpponentModelInterface opponentModelInterface);
}
