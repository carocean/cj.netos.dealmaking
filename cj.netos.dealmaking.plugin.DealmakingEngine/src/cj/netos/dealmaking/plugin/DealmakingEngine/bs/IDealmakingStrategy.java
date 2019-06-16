package cj.netos.dealmaking.plugin.DealmakingEngine.bs;

import cj.netos.dealmaking.bs.IMarketBuyOrderQueueBS;
import cj.netos.dealmaking.bs.IMarketSellOrderQueueBS;
import cj.studio.util.reactor.IReactor;

/**
 * 撮合策略
 * 
 * @author caroceanjofers
 *
 */
public interface IDealmakingStrategy {
	void dealmaking(IMarketBuyOrderQueueBS buyingqueue, IMarketSellOrderQueueBS sellingqueue,
			IReactor reactor);
	
}
