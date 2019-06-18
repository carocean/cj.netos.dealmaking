package cj.netos.dealmaking.plugin.DealmakingEngine;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import cj.netos.dealmaking.bs.IQueueEvent;
import cj.netos.dealmaking.plugin.DealmakingEngine.bs.IDealmakingQueueTask;
import cj.studio.ecm.IServiceSite;

public class MarketEngine implements IMarketEngine {
	Map<String, Future<?>> futures;
	IQueueEvent buyOrderQueueEvent;
	IQueueEvent sellOrderQueueEvent;
	ExecutorService marketWorks;// 每个线程负责处理一个市场，如果处理完交易则释放线程
	IServiceSite site;

	public MarketEngine(IServiceSite site) {
		futures = new ConcurrentHashMap<String, Future<?>>();
		this.site = site;
		this.marketWorks = Executors.newCachedThreadPool();
		this.buyOrderQueueEvent = createBuyOrderQueueEvent();
		this.sellOrderQueueEvent = createSellOrderQueueEvent();
	}

	@Override
	public IQueueEvent buyOrderQueueEvent() {
		return buyOrderQueueEvent;
	}

	@Override
	public IQueueEvent sellOrderQueueEvent() {
		return sellOrderQueueEvent;
	}

	@Override
	public synchronized void runMarket(String market) {
		Future<?> f = futures.get(market);
		if (f == null || f.isCancelled() || f.isDone()) {
			IDealmakingQueueTask task = new DealmakingQueueTask(market, site);
			f = marketWorks.submit(task);
			futures.put(market, f);
		}
	}

	@Override
	public void stop() {
		for (String market : this.futures.keySet()) {
			Future<?> f = this.futures.get(market);
			if (!f.isDone()) {
				f.cancel(true);
			}
		}
		this.futures.clear();
		this.marketWorks.shutdown();
		this.site = null;
	}

	protected IQueueEvent createSellOrderQueueEvent() {
		return new IQueueEvent() {

			@Override
			public void onevent(String action, Object... args) {
				if("offer".equals(action)) {
					runMarket((String)args[0]);
				}
			}
		};
	}

	protected IQueueEvent createBuyOrderQueueEvent() {
		return new IQueueEvent() {

			@Override
			public void onevent(String action, Object... args) {
				if("offer".equals(action)) {
					runMarket((String)args[0]);
				}
			}
		};
	}
}
