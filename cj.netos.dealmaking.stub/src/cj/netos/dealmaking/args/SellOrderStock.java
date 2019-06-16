package cj.netos.dealmaking.args;

import java.math.BigDecimal;
import java.util.List;

/**
 * 委托卖出存量单<br>
 * 是卖出队列中的行
 * 
 * @author caroceanjofers
 *
 */
public class SellOrderStock {
	String no;
	String orderno;
	String seller;
	List<Stock> stocks;
	BigDecimal feeRate;//手续费率
	long otime;//委托时间
	BigDecimal sellingPrice;

	public SellOrderStock() {
	}
	public BigDecimal getFeeRate() {
		return feeRate;
	}
	public void setFeeRate(BigDecimal feeRate) {
		this.feeRate = feeRate;
	}
	public String getNo() {
		return no;
	}
	public void setNo(String no) {
		this.no = no;
	}
	public String getOrderno() {
		return orderno;
	}

	public void setOrderno(String orderno) {
		this.orderno = orderno;
	}

	public String getSeller() {
		return seller;
	}

	public void setSeller(String seller) {
		this.seller = seller;
	}

	public List<Stock> getStocks() {
		return stocks;
	}
	public void setStocks(List<Stock> stocks) {
		this.stocks = stocks;
	}

	public long getOtime() {
		return otime;
	}
	public void setOtime(long otime) {
		this.otime = otime;
	}

	public BigDecimal getSellingPrice() {
		return sellingPrice;
	}

	public void setSellingPrice(BigDecimal sellingPrice) {
		this.sellingPrice = sellingPrice;
	}

}
