package cj.netos.dealmaking.args;

import java.math.BigDecimal;
/**
 * 表示一个帑银数量
 * @author caroceanjofers
 *
 */
public class Stock {
	String issueno;
	BigDecimal quantities;
	public Stock() {
	}
	
	public Stock(String issueno, BigDecimal quantities) {
		this.issueno = issueno;
		this.quantities = quantities;
	}

	public String getIssueno() {
		return issueno;
	}
	public void setIssueno(String issueno) {
		this.issueno = issueno;
	}
	public BigDecimal getQuantities() {
		return quantities;
	}
	public void setQuantities(BigDecimal quantities) {
		this.quantities = quantities;
	}
	
}
