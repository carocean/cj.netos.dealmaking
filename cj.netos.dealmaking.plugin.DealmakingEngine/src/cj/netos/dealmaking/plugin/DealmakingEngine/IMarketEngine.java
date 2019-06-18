package cj.netos.dealmaking.plugin.DealmakingEngine;

import cj.netos.dealmaking.bs.IQueueEvent;

public interface IMarketEngine {

	IQueueEvent buyOrderQueueEvent();

	IQueueEvent sellOrderQueueEvent();

	void runMarket(String market);
	void stop();
	
}
