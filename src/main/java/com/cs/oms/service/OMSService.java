package com.cs.oms.service;

import java.math.BigDecimal;

import com.cs.oms.common.Instrument;
import com.cs.oms.common.Order;
import com.cs.oms.common.OrderBook;
import com.cs.oms.common.exception.OMSException;

public interface OMSService {

	boolean addOrder(String symbol, long quantity, BigDecimal price) throws OMSException;

	boolean addOrder(String symbol, long quantity) throws OMSException;

	boolean addExecution(String symbol, long quantity, BigDecimal price) throws OMSException;

	boolean closeOrderBook(String symbol) throws OMSException;

	boolean openOrderBook(String symbol) throws OMSException;

	Instrument getInstrument(String symbol);

	OrderBook getOrderBook(String symbol) throws OMSException;

	Order getOrder(long orderId) throws OMSException;

	void printAllStatistics(String symbol);
}
