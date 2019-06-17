package cj.netos.dealmaking.plugin.DealmakingEngine.bs;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.model.UpdateOptions;

import cj.lns.chip.sos.cube.framework.IDocument;
import cj.lns.chip.sos.cube.framework.IQuery;
import cj.lns.chip.sos.cube.framework.TupleDocument;
import cj.netos.dealmaking.args.SellOrderStock;
import cj.netos.dealmaking.args.Stock;
import cj.netos.dealmaking.bs.IMarketInitializer;
import cj.netos.dealmaking.bs.IMarketSellOrderQueueBS;
import cj.netos.dealmaking.bs.IQueueEvent;
import cj.netos.dealmaking.plugin.DealmakingEngine.db.IMarketStore;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.ultimate.gson2.com.google.gson.Gson;

@CjService(name = "marketSellOrderQueueBS")
public class MarketSellOrderQueueBS implements IMarketSellOrderQueueBS {
	@CjServiceRef
	IMarketStore marketStore;
	@CjServiceRef
	IMarketInitializer marketInitializer;
	IQueueEvent event;
	@Override
	public void onevent(IQueueEvent e) {
		event=e;
	}
	@Override
	public void offer(String market, SellOrderStock sellOrderStock) {
		if (!marketInitializer.isMarketInitialized(market)) {
			String result = marketStore.market(market).createIndex(TABLE_queue_sellOrder,
					Document.parse("{'tuple.sellingPrice':1,'tuple.otime':1}"));// 价格升序、委托时间升序，意为：最低价且越早的越优先成交，并且价格优先于时间
			marketInitializer.setMarketInitialized(market);
			CJSystem.logging().debug(getClass(),String.format("市场：%s 已建立索引：%s",market,result));
		}
		String id = marketStore.market(market).saveDoc(TABLE_queue_sellOrder, new TupleDocument<>(sellOrderStock));
		sellOrderStock.setNo(id);
		if(event!=null) {
			event.onevent("offer",market);
		}
	}

	@Override
	public SellOrderStock peek(String market) {
		String cjql = String.format(
				"select {'tuple':'*'}.sort({'tuple.sellingPrice':1,'tuple.otime':1}).skip(0).limit(1) from tuple %s %s where {}",
				TABLE_queue_sellOrder, SellOrderStock.class.getName(), market);
		IQuery<SellOrderStock> q = marketStore.market(market).createQuery(cjql);
		IDocument<SellOrderStock> doc = q.getSingleResult();
		if (doc == null)
			return null;
		doc.tuple().setNo(doc.docid());
		return doc.tuple();
	}

	@Override
	public void remove(String market) {
		SellOrderStock stock = peek(market);
		if (stock == null) {
			return;
		}
		remove(market, stock.getNo());
	}

	@Override
	public void remove(String market, String no) {
		marketStore.market(market).deleteDocOne(TABLE_queue_sellOrder, String.format("{'_id':ObjectId('%s')}", no));
	}

	@Override
	public void updateStocks(String market, String stockno, List<Stock> stocks) {
		Bson filter = Document.parse(String.format("{'_id':ObjectId('%s')}", stockno));
		Bson update = Document.parse(String.format("{'$set':{'tuple.stocks':%s}}", new Gson().toJson(stocks)));
		UpdateOptions op = new UpdateOptions();
		op.upsert(false);
		marketStore.market(market).updateDocOne(TABLE_queue_sellOrder, filter, update, op);
	}

	@Override
	public List<SellOrderStock> listFiveSellingWindow(String market) {
		String cjql = String.format(
				"select {'tuple':'*'}.sort({'tuple.sellingPrice':1,'tuple.otime':1}).skip(0).limit(5) from tuple %s %s where {}",
				TABLE_queue_sellOrder, SellOrderStock.class.getName(), market);
		IQuery<SellOrderStock> q = marketStore.market(market).createQuery(cjql);
		List<IDocument<SellOrderStock>> docs = q.getResultList();
		List<SellOrderStock> list = new ArrayList<SellOrderStock>();
		for (IDocument<SellOrderStock> doc : docs) {
			SellOrderStock stock = doc.tuple();
			stock.setNo(doc.docid());
			list.add(stock);
		}
		return list;
	}
}
