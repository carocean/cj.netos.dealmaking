package cj.netos.dealmaking.plugin.DealmakingEngine.bs;

import cj.lns.chip.sos.cube.framework.TupleDocument;
import cj.netos.dealmaking.args.DealmakingContract;
import cj.netos.dealmaking.bs.IDealmakingContractBS;
import cj.netos.dealmaking.plugin.DealmakingEngine.db.IMarketStore;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;

@CjService(name = "dealmakingContractBS")
public class DealmakingContractBS implements IDealmakingContractBS {
	@CjServiceRef
	IMarketStore marketStore;

	@Override
	public void addContract(String market, DealmakingContract contract) {
		String id = marketStore.market(market).saveDoc(TABLE_contract, new TupleDocument<>(contract));
		contract.setNo(id);
	}

}
