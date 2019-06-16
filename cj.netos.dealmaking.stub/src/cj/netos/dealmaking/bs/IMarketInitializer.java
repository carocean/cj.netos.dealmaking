package cj.netos.dealmaking.bs;

import java.util.List;

public interface IMarketInitializer {
	static String TABLE_engine_info="engine.info";
	boolean isMarketInitialized(String market);
	void setMarketInitialized(String market);
	List<String> pageMarket(String dealmakingEngineID);
}
