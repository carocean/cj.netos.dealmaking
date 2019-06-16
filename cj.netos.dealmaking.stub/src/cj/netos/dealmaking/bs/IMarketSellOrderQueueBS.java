package cj.netos.dealmaking.bs;

import java.util.List;

import cj.netos.dealmaking.args.SellOrderStock;
import cj.netos.dealmaking.args.Stock;

public interface IMarketSellOrderQueueBS {
	static String TABLE_queue_sellOrder = "queue.sellorders";

	void onevent(IQueueEvent e);
	void offer(String market, SellOrderStock sellOrderStock);

	SellOrderStock peek(String market);

	void remove(String market);

	void remove(String market, String stockno);

	void updateStocks(String market, String stockno, List<Stock> stocks);

	List<SellOrderStock> listFiveSellingWindow(String market);

	
}
