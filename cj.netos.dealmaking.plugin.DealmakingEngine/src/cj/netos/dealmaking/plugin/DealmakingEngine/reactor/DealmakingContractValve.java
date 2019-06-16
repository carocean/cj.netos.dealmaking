package cj.netos.dealmaking.plugin.DealmakingEngine.reactor;

import java.math.BigDecimal;

import cj.netos.dealmaking.args.BuyOrderStock;
import cj.netos.dealmaking.args.SellOrderStock;
import cj.netos.dealmaking.bs.IMarketBuyOrderQueueBS;
import cj.netos.dealmaking.bs.IMarketSellOrderQueueBS;
import cj.netos.dealmaking.util.BigDecimalConstants;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.net.CircuitException;
import cj.studio.util.reactor.Event;
import cj.studio.util.reactor.IPipeline;
import cj.studio.util.reactor.IValve;

@CjService(name = "dealmakingContract")
public class DealmakingContractValve implements IValve {
	private IMarketBuyOrderQueueBS marketBuyOrderQueueBS;
	private IMarketSellOrderQueueBS marketSellOrderQueueBS;

	@Override
	public void flow(Event e, IPipeline pipeline) throws CircuitException {
		SellOrderStock sell = (SellOrderStock) e.getParameters().get("sell");
		BuyOrderStock buy = (BuyOrderStock) e.getParameters().get("buy");
		boolean exit = false;
		do {
			if(sell==null||buy==null||buy.getBuyingPrice().compareTo(sell.getSellingPrice()) < 0) {
				break;
			}
			signContract(e.getKey(), buy, sell);
			buy = (BuyOrderStock) e.getParameters().get("buy");
			sell = (SellOrderStock) e.getParameters().get("sell");
		} while (exit);

	}
	// 扣手续费：按交易价从买扣除。使之买的帑银量变少，之后将卖方的帑银给买方。
	/*
	 * 撮合:以买单作为参考等成交方式。如果买单钱/撮合价>1个卖单量,需找下一个卖单同时通知一单合约成功签定（卖单完全成交），直到找到的卖单为空则退出（
	 * 不论买方完全成交与否，但买方的钱已被扣减）。如果买单钱/撮合价=卖单量，则直接双方完全成交，并返回。如果买单钱/撮合价<卖单量，
	 * 则取下一个买单与之成交同时通知一单合约成功签定 （买单完全成交），直到找到买单为空则退出（不论卖方完全成交与否，但卖方的量已被扣减）
	 */
	private void signContract(String market, BuyOrderStock buy, SellOrderStock sell) throws CircuitException {
		BigDecimal stockPrice = (buy.getBuyingPrice().add(sell.getSellingPrice())).divide(new BigDecimal("2"),
				BigDecimalConstants.scale, BigDecimalConstants.roundingMode);
		
	}

}
