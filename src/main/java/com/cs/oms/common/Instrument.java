package com.cs.oms.common;

public class Instrument {
	private long id;
	private String symbol;

	public Instrument(long id, String symbol) {
		this.id = id;
		this.symbol = symbol;
	}

	public long getId() {
		return id;
	}

	public String getSymbol() {
		return symbol;
	}

	@Override
	public String toString() {
		return "Instrument [id=" + id + ", symbol=" + symbol + "]";
	}

}
