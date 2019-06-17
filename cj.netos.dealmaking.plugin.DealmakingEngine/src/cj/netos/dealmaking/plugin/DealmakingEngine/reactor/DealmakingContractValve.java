package cj.netos.dealmaking.plugin.DealmakingEngine.reactor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cj.netos.dealmaking.args.BuyOrderStock;
import cj.netos.dealmaking.args.DealmakingContract;
import cj.netos.dealmaking.args.SellOrderStock;
import cj.netos.dealmaking.args.Stock;
import cj.netos.dealmaking.bs.IDealmakingContractBS;
import cj.netos.dealmaking.bs.IMarketBuyOrderQueueBS;
import cj.netos.dealmaking.bs.IMarketSellOrderQueueBS;
import cj.netos.dealmaking.util.BigDecimalConstants;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.net.CircuitException;
import cj.studio.util.reactor.Event;
import cj.studio.util.reactor.IPipeline;
import cj.studio.util.reactor.IValve;

@CjService(name = "dealmakingContract")
public class DealmakingContractValve implements IValve {
	@CjServiceRef
	private IMarketBuyOrderQueueBS marketBuyOrderQueueBS;
	@CjServiceRef
	private IMarketSellOrderQueueBS marketSellOrderQueueBS;
	@CjServiceRef
	IDealmakingContractBS dealmakingContractBS;

	@Override
	public void flow(Event e, IPipeline pipeline) throws CircuitException {
		SellOrderStock sell = (SellOrderStock) e.getParameters().get("sell");
		BuyOrderStock buy = (BuyOrderStock) e.getParameters().get("buy");
		boolean exit = false;
		do {
			if (sell == null || buy == null || buy.getBuyingPrice().compareTo(sell.getSellingPrice()) < 0) {
				break;
			}
			signContract(e.getKey(), buy, sell);
			buy = (BuyOrderStock) e.getParameters().get("buy");
			sell = (SellOrderStock) e.getParameters().get("sell");
		} while (exit);

	}

	// 扣手续费：按交易价从买扣除。使之买的帑银量变少，之后将卖方的帑银给买方。
	/*
	 * 撮合:以买单作为参考等成交方式。如果买单钱/撮合价>1个卖单量,需找下一个卖单同时通知一单合约成功签定（卖单完全成交），直到找到的卖单为空则退出（
	 * 不论买方完全成交与否，但买方的钱已被扣减）。如果买单钱/撮合价=卖单量，则直接双方完全成交，并返回。如果买单钱/撮合价<卖单量，
	 * 则取下一个买单与之成交同时通知一单合约成功签定 （买单完全成交），直到找到买单为空则退出（不论卖方完全成交与否，但卖方的量已被扣减）
	 */
	private void signContract(String market, BuyOrderStock buy, SellOrderStock sell) throws CircuitException {
		BigDecimal stockPrice = (buy.getBuyingPrice().add(sell.getSellingPrice())).divide(new BigDecimal("2"),
				BigDecimalConstants.scale, BigDecimalConstants.roundingMode);
		BigDecimal stockQuantities = sell.totalStockQuantities();

		BigDecimal payableAmount/* 买方应付金 */ = (stockQuantities.multiply(stockPrice)).divide(
				new BigDecimal("1.0").subtract(buy.getFeeRate()), BigDecimalConstants.scale,
				BigDecimalConstants.roundingMode);

		DealmakingContract contract = new DealmakingContract();
		contract.setBuyer(buy.getBuyer());
		contract.setSeller(sell.getSeller());
		contract.setBuyorderno(buy.getBuyorderno());
		contract.setSellorderno(sell.getOrderno());
		contract.setCtime(System.currentTimeMillis());
		contract.setPayableAmount(payableAmount);
		contract.setForSellQuantities(stockQuantities);
		contract.setForBuyAmount(buy.getAmount());
		contract.setStockPrice(stockPrice);
		contract.setBuyFeeRate(buy.getFeeRate());
		contract.setSellFeeRate(sell.getFeeRate());

		BigDecimal buyQuantities = null;// 买方实得量
		BigDecimal payAmount = null;// 买方实付金
		BigDecimal sellAmount = null;// 卖方得金
		BigDecimal buyFeeAmount = null;// 买方手续费
		BigDecimal sellFeeAmount = null;// 卖方手续费
		BigDecimal sellRemainingQuantities = null;// 卖方余量
		BigDecimal buyRemainingAmount = null;// 买方余金
		BigDecimal tailAmount = null;// 尾金，是截取余数的金额，归平台所有。

		int compare = buy.getAmount().compareTo(payableAmount);
		switch (compare) {
		case -1:
			CJSystem.logging().info(getClass(), "买单完全成交。成交价:"+stockPrice);
//			买方实得量=（买方申购金*（1-买方拥金率））/撮合价
			buyQuantities = (buy.getAmount().multiply(new BigDecimal("1.0").subtract(buy.getFeeRate())))
					.divide(stockPrice, BigDecimalConstants.scale, BigDecimalConstants.roundingMode);
			payAmount = buy.getAmount();
			// 卖方得金=买方申购金*（1-（买方拥金率+卖方拥多率））
			sellAmount = buy.getAmount()
					.multiply(new BigDecimal("1.0").subtract(buy.getFeeRate().add(sell.getFeeRate())));
			// 买方手续费=买方申购金*买方拥金率
			buyFeeAmount = buy.getAmount().multiply(buy.getFeeRate());
			// 卖方手续费=买方实得量*成交价*卖方拥金率
			sellFeeAmount = buyQuantities.multiply(stockPrice).multiply(sell.getFeeRate());// 此处会产生尾金
			// 仅在发买单完全成交时有余量，则为：卖方余量=卖方申售量-买方实得量
			sellRemainingQuantities = stockQuantities.subtract(buyQuantities);

			contract.setBuyQuantities(buyQuantities);
			contract.setPayAmount(payAmount.setScale(2, BigDecimalConstants.roundingMode));
			contract.setSellAmount(sellAmount.setScale(2, BigDecimalConstants.roundingMode));
			contract.setBuyFeeAmount(buyFeeAmount.setScale(2, BigDecimalConstants.roundingMode));
			contract.setSellFeeAmount(sellFeeAmount.setScale(2, BigDecimalConstants.roundingMode));
			contract.setSellRemainingQuantities(sellRemainingQuantities);
			contract.setBuyRemainingAmount(new BigDecimal("0.00"));
			tailAmount = contract.getForBuyAmount().subtract(
					contract.getSellAmount().add(contract.getBuyFeeAmount()).add(contract.getSellFeeAmount()));
			contract.setTailAmount(tailAmount);

			updateSellRemainingQuantities(market, sell, sellRemainingQuantities);
			saveContract(market, contract);// 保存合约
			removeBuyOrderStock(market, buy.getNo());

			CJSystem.logging().info(getClass(),
					String.format("--买方实得量:%s --买方实付金:%s --卖方得金:%s --买方手续费:%s --卖方手续费:%s --卖方余量:%s --尾金:%s",
							contract.getBuyQuantities(), contract.getPayAmount(), contract.getSellAmount(),
							contract.getBuyFeeAmount(), contract.getSellFeeAmount(),
							contract.getSellRemainingQuantities(), contract.getTailAmount()));
			break;
		case 1:
			CJSystem.logging().info(getClass(), "卖单完全成交。成交价:"+stockPrice);
			// 买方实得量=卖方申售量
			buyQuantities = stockQuantities;
			payAmount = payableAmount;
			// 卖方得金=买方应付金*（1-（买方拥金率+卖方拥多率））
			sellAmount = payableAmount
					.multiply(new BigDecimal("1.0").subtract(buy.getFeeRate().add(sell.getFeeRate())));
			// 买方手续费=买方应付金*买方拥金率
			buyFeeAmount = payableAmount.multiply(buy.getFeeRate());
			sellFeeAmount = buyQuantities.multiply(stockPrice).multiply(sell.getFeeRate());
			// 仅在卖单完全成交时有余金，则为：买方余金=买方申购金-买方实付金
			buyRemainingAmount = buy.getAmount().subtract(payAmount);

			contract.setBuyQuantities(buyQuantities);
			contract.setPayAmount(payAmount.setScale(2, BigDecimalConstants.roundingMode));
			contract.setSellAmount(sellAmount.setScale(2, BigDecimalConstants.roundingMode));
			contract.setBuyFeeAmount(buyFeeAmount.setScale(2, BigDecimalConstants.roundingMode));
			contract.setSellFeeAmount(sellFeeAmount.setScale(2, BigDecimalConstants.roundingMode));
			contract.setBuyRemainingAmount(buyRemainingAmount);
			contract.setSellRemainingQuantities(new BigDecimal("0.00"));
			tailAmount = contract.getPayAmount().subtract(
					contract.getSellAmount().add(contract.getBuyFeeAmount()).add(contract.getSellFeeAmount()));
			contract.setTailAmount(tailAmount);

			updateBuyRemainingAmount(market, buy, buyRemainingAmount);
			saveContract(market, contract);// 保存合约
			removeSellOrderStock(market, sell.getNo());

			CJSystem.logging().info(getClass(),
					String.format("--买方实得量:%s --买方实付金:%s --卖方得金:%s --买方手续费:%s --卖方手续费:%s --买方余金:%s --尾金:%s",
							contract.getBuyQuantities(), contract.getPayAmount(), contract.getSellAmount(),
							contract.getBuyFeeAmount(), contract.getSellFeeAmount(), contract.getBuyRemainingAmount(),
							contract.getTailAmount()));
			break;
		case 0:
			CJSystem.logging().info(getClass(), "双方完全成交。成交价:"+stockPrice);
			buyQuantities = stockQuantities;
			payAmount = payableAmount;
			sellAmount = payableAmount
					.multiply(new BigDecimal("1.0").subtract(buy.getFeeRate().add(sell.getFeeRate())));
			buyFeeAmount = payableAmount.multiply(buy.getFeeRate());
			sellFeeAmount = buyQuantities.multiply(stockPrice).multiply(sell.getFeeRate());

			contract.setBuyQuantities(buyQuantities);
			contract.setPayAmount(payAmount.setScale(2, BigDecimalConstants.roundingMode));
			contract.setSellAmount(sellAmount.setScale(2, BigDecimalConstants.roundingMode));
			contract.setBuyFeeAmount(buyFeeAmount.setScale(2, BigDecimalConstants.roundingMode));
			contract.setSellFeeAmount(sellFeeAmount.setScale(2, BigDecimalConstants.roundingMode));
			contract.setBuyRemainingAmount(buyRemainingAmount);
			contract.setSellRemainingQuantities(new BigDecimal("0.00"));
			tailAmount = contract.getForBuyAmount().subtract(
					contract.getSellAmount().add(contract.getBuyFeeAmount()).add(contract.getSellFeeAmount()));
			contract.setTailAmount(tailAmount);

			saveContract(market, contract);// 保存合约
			removeSellOrderStock(market, sell.getNo());
			removeBuyOrderStock(market, buy.getNo());

			CJSystem.logging().info(getClass(),
					String.format("--买方实得量:%s --买方实付金:%s --卖方得金:%s --买方手续费:%s --卖方手续费:%s --尾金:%s", buyQuantities,
							contract.getPayAmount(), contract.getSellAmount(), contract.getBuyFeeAmount(),
							contract.getSellFeeAmount(), contract.getTailAmount()));
			break;
		}
	}

	private void saveContract(String market, DealmakingContract contract) {
		this.dealmakingContractBS.addContract(market, contract);
	}

	private void removeBuyOrderStock(String market, String no) {
		this.marketBuyOrderQueueBS.remove(market, no);
	}

	private void removeSellOrderStock(String market, String no) {
		this.marketSellOrderQueueBS.remove(market, no);
	}

	private void updateSellRemainingQuantities(String market, SellOrderStock sell,
			BigDecimal remaining/* 即要在队列中保留的量 */) {
		// 算法：从存量中取出remaining的量，然后覆盖
		List<Stock> stocks = sell.getStocks();
		BigDecimal add = new BigDecimal("0");
		List<Stock> list = new ArrayList<Stock>();
		for (int i = 0; i < stocks.size(); i++) {
			Stock s = stocks.get(i);
			add = add.add(s.getQuantities());
			if (add.compareTo(remaining) >= 0) {
				BigDecimal j = add.subtract(remaining);
				j = s.getQuantities().subtract(j);
				s.setQuantities(j);
				list.add(s);
				break;
			} else {
				list.add(s);
			}
		}
		this.marketSellOrderQueueBS.updateStocks(market, sell.getNo(), list);
	}

	private void updateBuyRemainingAmount(String market, BuyOrderStock buy, BigDecimal remaining) {
		buy.setAmount(remaining);
		this.marketBuyOrderQueueBS.updateAmount(market, buy.getNo(), remaining);
	}

}
