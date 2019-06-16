package cj.netos.dealmaking.stub;

import cj.netos.dealmaking.args.BuyOrderStock;
import cj.netos.dealmaking.args.SellOrderStock;
import cj.studio.ecm.net.CircuitException;
import cj.studio.gateway.stub.annotation.CjStubCircuitStatusMatches;
import cj.studio.gateway.stub.annotation.CjStubInContentKey;
import cj.studio.gateway.stub.annotation.CjStubInParameter;
import cj.studio.gateway.stub.annotation.CjStubMethod;
import cj.studio.gateway.stub.annotation.CjStubService;

@CjStubService(bindService = "/queue.service", usage = "委托买卖队列")
public interface IDealmakingQueueStub {

	@CjStubMethod(command = "post", usage = "向买方队列添加")
	@CjStubCircuitStatusMatches(status = { "503 error.", "500 error.", "404 not found." })
	void putBuyingQueue(@CjStubInParameter(key = "market", usage = "市场编号") String market,
			@CjStubInContentKey(key = "stock", usage = "委托买单存量") BuyOrderStock stock)
			throws CircuitException;

	@CjStubMethod(command = "post", usage = "向卖方队列添加")
	@CjStubCircuitStatusMatches(status = { "503 error.", "500 error.", "404 not found." })
	void putSellingQueue(@CjStubInParameter(key = "market", usage = "市场编号") String market,
			@CjStubInContentKey(key = "stock", usage = "委托卖单存量") SellOrderStock stock)
			throws CircuitException;

}
