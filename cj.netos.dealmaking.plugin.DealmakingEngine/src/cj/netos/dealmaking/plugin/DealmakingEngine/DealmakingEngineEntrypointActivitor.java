package cj.netos.dealmaking.plugin.DealmakingEngine;

import java.util.List;

import cj.netos.dealmaking.bs.IMarketBuyOrderQueueBS;
import cj.netos.dealmaking.bs.IMarketInitializer;
import cj.netos.dealmaking.bs.IMarketSellOrderQueueBS;
import cj.studio.ecm.IChip;
import cj.studio.ecm.IEntryPointActivator;
import cj.studio.ecm.IServiceSite;
import cj.studio.ecm.context.IElement;

public class DealmakingEngineEntrypointActivitor implements IEntryPointActivator {
	IMarketEngine engine;
	
	@Override
	public void activate(IServiceSite site, IElement args) {
		engine=new MarketEngine(site);
		IMarketBuyOrderQueueBS marketBuyOrderQueueBS = (IMarketBuyOrderQueueBS) site.getService("marketBuyOrderQueueBS");
		IMarketSellOrderQueueBS marketSellOrderQueueBS = (IMarketSellOrderQueueBS) site.getService("marketSellOrderQueueBS");
		marketBuyOrderQueueBS.onevent(engine.buyOrderQueueEvent());
		marketSellOrderQueueBS.onevent(engine.sellOrderQueueEvent());
		
		IChip chip = (IChip) site.getService(IChip.class.getName());
		String dealmakingEngineID = chip.info().getId();
		IMarketInitializer marketInitializer = (IMarketInitializer) site.getService("marketInitializer");
		List<String> marketsInDB = marketInitializer.pageMarket(dealmakingEngineID);
		for (String market : marketsInDB) {
			engine.runMarket(market);
		}
	}

	@Override
	public void inactivate(IServiceSite site) {
		engine.stop();
	}

}
