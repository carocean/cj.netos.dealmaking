package cj.netos.dealmaking.args;
/**
 * 撮合交易单
 * @author caroceanjofers
 *
 */

import java.math.BigDecimal;

public class DealmakingContract {
	String no;
	String buyorderno;//原买单单号
	String sellorderno;//原卖单单号
	String seller;//卖方
	String buyer;//买方
	BigDecimal forSellQuantities;//卖方申售量
	BigDecimal forBuyAmount;//卖方申购金
	BigDecimal stockPrice;//成交价格
	BigDecimal payableAmount;//买方应付金
	BigDecimal buyQuantities;// 买方实得量
	BigDecimal payAmount;// 买方实付金
	BigDecimal sellAmount;// 卖方得金
	BigDecimal buyFeeAmount;// 买方手续费
	BigDecimal sellFeeAmount;// 卖方手续费
	BigDecimal sellRemainingQuantities;// 卖方余量
	BigDecimal buyRemainingAmount;// 买方余金
	BigDecimal sellFeeRate;//卖方税率
	BigDecimal buyFeeRate;//买方税率
	long ctime;//撮合时间
	BigDecimal tailAmount;//尾金
	
	public String getNo() {
		return no;
	}
	public void setNo(String no) {
		this.no = no;
	}
	public String getBuyorderno() {
		return buyorderno;
	}
	public void setBuyorderno(String buyorderno) {
		this.buyorderno = buyorderno;
	}
	public String getSellorderno() {
		return sellorderno;
	}
	public void setSellorderno(String sellorderno) {
		this.sellorderno = sellorderno;
	}
	public String getSeller() {
		return seller;
	}
	public void setSeller(String seller) {
		this.seller = seller;
	}
	public String getBuyer() {
		return buyer;
	}
	public void setBuyer(String buyer) {
		this.buyer = buyer;
	}
	public BigDecimal getForSellQuantities() {
		return forSellQuantities;
	}
	public void setForSellQuantities(BigDecimal forSellQuantities) {
		this.forSellQuantities = forSellQuantities;
	}
	public BigDecimal getForBuyAmount() {
		return forBuyAmount;
	}
	public void setForBuyAmount(BigDecimal forBuyAmount) {
		this.forBuyAmount = forBuyAmount;
	}
	public BigDecimal getStockPrice() {
		return stockPrice;
	}
	public void setStockPrice(BigDecimal stockPrice) {
		this.stockPrice = stockPrice;
	}
	public BigDecimal getPayableAmount() {
		return payableAmount;
	}
	public void setPayableAmount(BigDecimal payableAmount) {
		this.payableAmount = payableAmount;
	}
	public BigDecimal getBuyQuantities() {
		return buyQuantities;
	}
	public void setBuyQuantities(BigDecimal buyQuantities) {
		this.buyQuantities = buyQuantities;
	}
	public BigDecimal getPayAmount() {
		return payAmount;
	}
	public void setPayAmount(BigDecimal payAmount) {
		this.payAmount = payAmount;
	}
	public BigDecimal getSellAmount() {
		return sellAmount;
	}
	public void setSellAmount(BigDecimal sellAmount) {
		this.sellAmount = sellAmount;
	}
	public BigDecimal getBuyFeeAmount() {
		return buyFeeAmount;
	}
	public void setBuyFeeAmount(BigDecimal buyFeeAmount) {
		this.buyFeeAmount = buyFeeAmount;
	}
	public BigDecimal getSellFeeAmount() {
		return sellFeeAmount;
	}
	public void setSellFeeAmount(BigDecimal sellFeeAmount) {
		this.sellFeeAmount = sellFeeAmount;
	}
	public BigDecimal getSellRemainingQuantities() {
		return sellRemainingQuantities;
	}
	public void setSellRemainingQuantities(BigDecimal sellRemainingQuantities) {
		this.sellRemainingQuantities = sellRemainingQuantities;
	}
	public BigDecimal getBuyRemainingAmount() {
		return buyRemainingAmount;
	}
	public void setBuyRemainingAmount(BigDecimal buyRemainingAmount) {
		this.buyRemainingAmount = buyRemainingAmount;
	}
	public BigDecimal getSellFeeRate() {
		return sellFeeRate;
	}
	public void setSellFeeRate(BigDecimal sellFeeRate) {
		this.sellFeeRate = sellFeeRate;
	}
	public BigDecimal getBuyFeeRate() {
		return buyFeeRate;
	}
	public void setBuyFeeRate(BigDecimal buyFeeRate) {
		this.buyFeeRate = buyFeeRate;
	}
	public long getCtime() {
		return ctime;
	}
	public void setCtime(long ctime) {
		this.ctime = ctime;
	}
	public BigDecimal getTailAmount() {
		return tailAmount;
	}
	public void setTailAmount(BigDecimal tailAmount) {
		this.tailAmount = tailAmount;
	}
	
}
