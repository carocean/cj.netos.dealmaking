package cj.netos.dealmaking.plugin.DealmakingEngine;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import cj.netos.dealmaking.args.BuyOrderStock;
import cj.netos.dealmaking.args.SellOrderStock;
import cj.netos.dealmaking.bs.IMarketBuyOrderQueueBS;
import cj.netos.dealmaking.bs.IMarketInitializer;
import cj.netos.dealmaking.bs.IMarketSellOrderQueueBS;
import cj.netos.dealmaking.bs.IQueueEvent;
import cj.netos.dealmaking.plugin.DealmakingEngine.bs.IDealmakingQueueTask;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.IChip;
import cj.studio.ecm.IServiceSite;
import cj.studio.util.reactor.Event;
import cj.studio.util.reactor.IReactor;

public class DealmakingQueueTask implements IDealmakingQueueTask {
	IReactor reactor;
	ReentrantLock lock;
	Condition empty;
	private IMarketBuyOrderQueueBS marketBuyOrderQueueBS;
	private IMarketSellOrderQueueBS marketSellOrderQueueBS;
	IMarketInitializer marketInitializer;
	String dealmakingEngineID;

	public DealmakingQueueTask(IServiceSite site) {
		lock = new ReentrantLock();
		empty = lock.newCondition();
		reactor = (IReactor) site.getService("$.reactor");
		IChip chip = (IChip) site.getService(IChip.class.getName());
		dealmakingEngineID = chip.info().getId();
		this.marketInitializer = (IMarketInitializer) site.getService("marketInitializer");
		this.marketBuyOrderQueueBS = (IMarketBuyOrderQueueBS) site.getService("marketBuyOrderQueueBS");
		marketBuyOrderQueueBS.onevent(new IQueueEvent() {

			@Override
			public void onevent(String action, Object... args) {
				if (!"offer".equals(action)) {
					return;
				}
				try {
					lock.lock();
					// 通知撮合引擎线程继续执行
					empty.signal();
					CJSystem.logging().info(getClass(), "有买单入队");
				} finally {
					lock.unlock();
				}
			}
		});
		this.marketSellOrderQueueBS = (IMarketSellOrderQueueBS) site.getService("marketSellOrderQueueBS");
		marketSellOrderQueueBS.onevent(new IQueueEvent() {

			@Override
			public void onevent(String action, Object... args) {
				if (!"offer".equals(action)) {
					return;
				}
				// 通知撮合引擎线程继续执行
				try {
					lock.lock();
					empty.signal();
					CJSystem.logging().info(getClass(), "有卖单入队");
				} finally {
					lock.unlock();
				}
			}
		});
	}

	@Override
	public void run() {
		// 检查卖出和买入队列是否有单，如果有单则输入，无单则等待，直到收到有新单入队通知时继续执行

		while (!Thread.interrupted()) {
			List<String> markets = this.marketInitializer.pageMarket(this.dealmakingEngineID);
			try {
				lock.lock();
				if (markets.isEmpty()) {
					CJSystem.logging().info(getClass(), "没有市场，等待...");
					empty.await();
					continue;
				}
				for (String market : markets) {
					CJSystem.logging().info(getClass(), "发现市场：" + market);
					while (!Thread.interrupted()) {
						BuyOrderStock buy = this.marketBuyOrderQueueBS.peek(market);//主线程与reactor线程都在同一队列上操作，存在删取争用，故错误。
						if (buy == null) {
							CJSystem.logging().info(getClass(), "\t\t买入队列为空");
							break;
						}
						SellOrderStock sell = this.marketSellOrderQueueBS.peek(market);
						if (sell == null) {
							CJSystem.logging().info(getClass(), "\t\t卖出队列为空");
							break;
						}
						if (buy.getBuyingPrice().compareTo(sell.getSellingPrice()) < 0) {
							CJSystem.logging().info(getClass(), "\t\t未达到交易条件");
							break;
						}
						checkAndCommitDealmakingable(market,buy,sell);
					}
				}
				empty.await(6000L, TimeUnit.MILLISECONDS);
			} catch (Exception e) {
				CJSystem.logging().error(getClass(), e);
			} finally {
				lock.unlock();
			}
		}

	}

	private void checkAndCommitDealmakingable(String market, BuyOrderStock buy, SellOrderStock sell)
			throws InterruptedException {
		// 该市场有撮合交易，则进入撮合线程执行
		CJSystem.logging().info(getClass(), "\t\t进入撮合程序");

		// 根据撮合价再计算买方能买多少，且考虑手续费扣减问题
		Event e = new Event(market, "dealmakingContract");
		e.getParameters().put("buy", buy);
		e.getParameters().put("sell", sell);
		reactor.input(e);

	}

}
