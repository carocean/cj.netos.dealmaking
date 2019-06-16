package cj.netos.dealmaking.program.stub;

import cj.netos.dealmaking.args.BuyOrderStock;
import cj.netos.dealmaking.args.SellOrderStock;
import cj.netos.dealmaking.bs.IMarketBuyOrderQueueBS;
import cj.netos.dealmaking.bs.IMarketSellOrderQueueBS;
import cj.netos.dealmaking.stub.IDealmakingQueueStub;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.net.CircuitException;
import cj.studio.gateway.stub.GatewayAppSiteRestStub;

@CjService(name = "/queue.service")
public class DealmakingQueueStub extends GatewayAppSiteRestStub implements IDealmakingQueueStub {
	@CjServiceRef(refByName = "DealmakingEngine.marketBuyOrderQueueBS")
	IMarketBuyOrderQueueBS marketBuyOrderQueueBS;
	@CjServiceRef(refByName = "DealmakingEngine.marketSellOrderQueueBS")
	IMarketSellOrderQueueBS marketSellOrderQueueBS;

	@Override
	public void putBuyingQueue(String market, BuyOrderStock stock) throws CircuitException {
		marketBuyOrderQueueBS.offer(market, stock);
	}

	@Override
	public void putSellingQueue(String market, SellOrderStock stock) throws CircuitException {
		marketSellOrderQueueBS.offer(market, stock);
	}

}
