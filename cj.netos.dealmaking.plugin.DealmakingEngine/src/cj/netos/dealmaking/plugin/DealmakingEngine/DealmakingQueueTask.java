package cj.netos.dealmaking.plugin.DealmakingEngine;

import java.math.BigDecimal;
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
import cj.netos.dealmaking.util.BigDecimalConstants;
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
					dealmaking(market);
				}
				empty.await(6000L, TimeUnit.MILLISECONDS);
			} catch (Exception e) {
				CJSystem.logging().error(getClass(), e);
			} finally {
				lock.unlock();
			}
		}

	}

	private void dealmaking(String market) throws InterruptedException {
		BuyOrderStock buy = this.marketBuyOrderQueueBS.peek(market);

		if (buy == null) {
			CJSystem.logging().info(getClass(), "\t\t买入队列为空");
			return;
		}
		SellOrderStock sell = this.marketSellOrderQueueBS.peek(market);
		if (sell == null) {
			CJSystem.logging().info(getClass(), "\t\t卖出队列为空");
			return;
		}
		if (buy.getBuyingPrice().compareTo(sell.getSellingPrice()) < 0) {
			CJSystem.logging().info(getClass(), "\t\t未达到交易条件");
			return;
		}
		/*
		 * 撮合:以买单作为参考等成交方式。如果买单钱/撮合价>1个卖单量,需找下一个卖单同时通知一单合约成功签定（卖单完全成交），直到找到的卖单为空则退出（
		 * 不论买方完全成交与否，但买方的钱已被扣减）。如果买单钱/撮合价=卖单量，则直接双方完全成交，并返回。如果买单钱/撮合价<卖单量，则取下一个买单与之成交同时通知一单合约成功签定
		 * （买单完全成交），直到找到买单为空则退出（不论卖方完全成交与否，但卖方的量已被扣减）
		 */
		CJSystem.logging().info(getClass(), "\t\t进入撮合");

		BigDecimal stockPrice = (buy.getBuyingPrice().add(sell.getSellingPrice())).divide(new BigDecimal("2"),
				BigDecimalConstants.scale, BigDecimalConstants.roundingMode);

		// 根据撮合价再计算买方能买多少，且考虑手续费扣减问题
		Event e = new Event(market, "dealmakingContract");
		e.getParameters().put("buy", buy);
		e.getParameters().put("sell", sell);
		e.getParameters().put("stockPrice", stockPrice);
		reactor.input(e);
		this.marketBuyOrderQueueBS.remove(market, buy.getNo());
		this.marketSellOrderQueueBS.remove(market, sell.getNo());
		CJSystem.logging().info(getClass(), "\t\t成功撮合一个合约，成交价：" + stockPrice);

	}

}
