package cj.netos.dealmaking.args;

import java.math.BigDecimal;

//委托买入存量单
//是买入队列中的行
public class BuyOrderStock {
	String no;
	String buyorderno;
	String buyer;
	BigDecimal amount;
	BigDecimal buyingPrice;
	BigDecimal feeRate;//手续费率
	long otime;//委托买入时间
	public BuyOrderStock() {
	}
	public BigDecimal getFeeRate() {
		return feeRate;
	}
	public void setFeeRate(BigDecimal feeRate) {
		this.feeRate = feeRate;
	}
	public BigDecimal getBuyingPrice() {
		return buyingPrice;
	}
	public void setBuyingPrice(BigDecimal buyingPrice) {
		this.buyingPrice = buyingPrice;
	}
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
	public String getBuyer() {
		return buyer;
	}
	public void setBuyer(String buyer) {
		this.buyer = buyer;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public long getOtime() {
		return otime;
	}
	public void setOtime(long otime) {
		this.otime = otime;
	}
	
}
