package com.cs.oms.common;

public class Execution {
	private final int id;
	private final int instrumentId;
	private final long quantity;
	private final double price;

	public Execution(int id, int instrumentId, long quantity, double price) {
		this.id = id;
		this.instrumentId = instrumentId;
		this.quantity = quantity;
		this.price = price;
	}

	public int getId() {
		return id;
	}

	public int getInstrumentId() {
		return instrumentId;
	}

	public long getQuantity() {
		return quantity;
	}

	public double getPrice() {
		return price;
	}

	@Override
	public String toString() {
		return "Execution [id=" + id + ", instrumentId=" + instrumentId + ", quantity=" + quantity + ", price=" + price
				+ "]";
	}

}
