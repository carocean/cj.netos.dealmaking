package cj.netos.dealmaking.plugin.DealmakingEngine.db;

import cj.lns.chip.sos.cube.framework.ICube;

public interface IMarketStore {

	ICube market(String market);

	ICube home();

}