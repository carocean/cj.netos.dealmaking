package cj.netos.dealmaking.plugin.DealmakingEngine.bs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cj.lns.chip.sos.cube.framework.IDocument;
import cj.lns.chip.sos.cube.framework.IQuery;
import cj.lns.chip.sos.cube.framework.TupleDocument;
import cj.netos.dealmaking.args.DealmakingEngineInfo;
import cj.netos.dealmaking.bs.IMarketInitializer;
import cj.netos.dealmaking.plugin.DealmakingEngine.db.IMarketStore;
import cj.studio.ecm.IChip;
import cj.studio.ecm.IServiceSite;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.annotation.CjServiceSite;

@CjService(name = "marketInitializer")
public class MarketInitializer implements IMarketInitializer {
	@CjServiceRef
	IMarketStore marketStore;
	@CjServiceSite
	IServiceSite site;

	@Override
	public boolean isMarketInitialized(String market) {
		String cjql = String.format("select {'tuple.isInitialized':1} from tuple %s %s where {'tuple.market':'%s'}",
				TABLE_engine_info, HashMap.class.getName(), market);
		IQuery<HashMap<String, Object>> q = marketStore.home().createQuery(cjql);
		IDocument<HashMap<String, Object>> doc = q.getSingleResult();
		if (doc == null || doc.tuple() == null) {
			return false;
		}
		Object v = doc.tuple().get("isInitialized");
		if (v == null) {
			return false;
		}
		return (boolean) v;
	}

	@Override
	public void setMarketInitialized(String market) {
		IChip chip = (IChip) site.getService(IChip.class.getName());
		String dealmakingEngineID = chip.info().getId();
		DealmakingEngineInfo info = new DealmakingEngineInfo();
		info.setDealmakingEngineID(dealmakingEngineID);
		info.setInitialized(true);
		info.setMarket(market);

		marketStore.home().saveDoc(TABLE_engine_info, new TupleDocument<>(info));
	}

	@Override
	public List<String> pageMarket(String dealmakingEngineID) {
		String cjql = String.format("select {'tuple.market':1} from tuple %s %s where {'tuple.dealmakingEngineID':'%s'}",
				TABLE_engine_info, HashMap.class.getName(), dealmakingEngineID);
		IQuery<HashMap<String, String>> q = marketStore.home().createQuery(cjql);
		List<IDocument<HashMap<String, String>>> docs = q.getResultList();
		List<String> list=new ArrayList<String>();
		for(IDocument<HashMap<String, String>> doc:docs) {
			HashMap<String, String> obj=doc.tuple();
			list.add(obj.get("market"));
		}
		return list;
	}

}
