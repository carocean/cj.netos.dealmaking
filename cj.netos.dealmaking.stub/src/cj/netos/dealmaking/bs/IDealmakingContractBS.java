package cj.netos.dealmaking.bs;

import cj.netos.dealmaking.args.DealmakingContract;

public interface IDealmakingContractBS {
	static String TABLE_contract="contracts";
	
	void addContract(String market,DealmakingContract contract);
}
