package com.cs.oms.common;

import java.math.BigDecimal;

public class LimitOrder extends Order {

	private final BigDecimal price;

	public LimitOrder(long id, long instrumentId, long quantity, BigDecimal price) {
		super(OrderType.LIMIT, id, instrumentId, quantity);
		this.price = price;
	}

	
	public BigDecimal getPrice() {
		return price;
	}


	@Override
	public String toString() {
		return "LimitOrder [price=" + price + ","+ super.toString() + "]";
	}

}
