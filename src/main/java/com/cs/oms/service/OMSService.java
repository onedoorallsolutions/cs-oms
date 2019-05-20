package com.cs.oms.service;

import java.math.BigDecimal;

import com.cs.oms.common.Order;
import com.cs.oms.common.OrderBook;
import com.cs.oms.common.exception.OMSException;

public interface OMSService {

	boolean addLimitOrder(String symbol, long quantity, BigDecimal price) throws OMSException;

	boolean addMarketOrder(String symbol, long quantity) throws OMSException;

	boolean addExecution(String symbol, long quantity, BigDecimal price) throws OMSException;

	boolean closeOrderBook(String symbol) throws OMSException;

	boolean openOrderBook(String symbol) throws OMSException;
	
	OrderBook getOrderBook(String symbol) throws OMSException;
	
	Order getOrder(long orderId) throws OMSException;

	void printAllStatistics(String symbol);
}
