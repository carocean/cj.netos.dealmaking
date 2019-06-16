package cj.netos.dealmaking.plugin.DealmakingEngine.db;

import java.util.HashMap;
import java.util.Map;

import cj.lns.chip.sos.cube.framework.ICube;
import cj.studio.ecm.IServiceSite;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.annotation.CjServiceSite;

@CjService(name = "marketStore")
public class MarketStore implements IMarketStore {
	@CjServiceSite
	IServiceSite site;
	@CjServiceRef(refByName = "mongodb.silvermarket.home")
	ICube home;
	Map<String, ICube> markets;

	public MarketStore() {
		markets = new HashMap<String, ICube>();
	}

	@Override
	public synchronized ICube market(String market) {
		if (markets.containsKey(market)) {
			return markets.get(market);
		}
		return (ICube) site.getService("mongodb.silvermarket." + market + ":autocreate");
	}

	@Override
	public synchronized ICube home() {
		return home;
	}
}
