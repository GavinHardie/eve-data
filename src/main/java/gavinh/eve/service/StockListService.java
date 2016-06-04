package gavinh.eve.service;

import gavinh.eve.data.ItemType;
import gavinh.eve.data.ItemTypeRepository;
import gavinh.eve.data.MarketOrder;
import gavinh.eve.data.MarketOrderRepository;
import gavinh.eve.data.Station;
import gavinh.eve.data.StationRepository;
import gavinh.eve.manufacturing.StockList;
import gavinh.eve.utils.MarketOrderComparator;
import gavinh.eve.utils.Transaction;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * @author Gavin
 */
@Service
public class StockListService {
    
    private static final Logger log = LoggerFactory.getLogger(StockListService.class);
    
    @Autowired
    private MarketOrderRepository marketOrderRepository;
    
    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private ItemTypeRepository itemTypeRepository;

    private final String TODAY;
    private static final String SELL_ORDERS = "sell";
    private static final String BUY_ORDERS = "buy";
    
    public StockListService() {
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        TODAY = sdf.format(new Date());
        
    }
    
    public StockList makeSales(StockList stockList, Integer stationId) {
        return makeTransactions(stockList, stationId, BUY_ORDERS);
    }
    
    public StockList makePurchases(StockList stockList, Integer stationId) {
        return makeTransactions(stockList, stationId, SELL_ORDERS);
    }
    
    private StockList makeTransactions(StockList stockList, Integer stationId, String orderType) {
        Station station = stationRepository.findOne(stationId);
        for(StockList.Item item : stockList.items.values()) {
            ItemType itemType = itemTypeRepository.findOne(item.itemTypeId);
            List<MarketOrder> marketOrders = marketOrderRepository.findByFetchedAndBuysellAndItemTypeAndStation(TODAY, orderType, itemType, station);
            Collections.sort(marketOrders, new MarketOrderComparator(orderType));
            item.transactions = new ArrayList<>();
            item.transactions.addAll(processMarketOrders(itemType, marketOrders, item.quantity));
            Collections.sort(item.transactions, new Transaction.TransactionComparator(orderType));
        }
        return stockList;
    }
    
    public StockList makePurchases(StockList stockList, List<Integer> stationIds) {
        return makeTransactions(stockList, stationIds, SELL_ORDERS);
    }
    
    public StockList makeSales(StockList stockList, List<Integer> stationIds) {
        return makeTransactions(stockList, stationIds, BUY_ORDERS);
    }
    
    private StockList makeTransactions(StockList stockList, List<Integer> stationIds, String orderType) {
        
        List<Station> stations = new ArrayList<>();
        for(Integer stationId : stationIds) {
            stations.add(stationRepository.findOne(stationId));
        }
        
        for(StockList.Item item : stockList.items.values()) {

            ItemType itemType = itemTypeRepository.findOne(item.itemTypeId);
            List<MarketOrder> marketOrders = new ArrayList<>();
            for(Station station : stations) {
                marketOrders.addAll(marketOrderRepository.findByFetchedAndBuysellAndItemTypeAndStation(TODAY, orderType, itemType, station));
            }
            
            Collections.sort(marketOrders, new MarketOrderComparator(orderType));

            item.transactions = new ArrayList<>();
            item.transactions.addAll(processMarketOrders(itemType, marketOrders, item.quantity));
            Collections.sort(item.transactions, new Transaction.TransactionComparator(orderType));
        }
        return stockList;
    }
    
    public StockList makeHighsecPurchases(StockList stockList) {
        return makeHighsecTransactions(stockList, SELL_ORDERS);
    }
    
    public StockList makeHighsecSales(StockList stockList) {
        return makeHighsecTransactions(stockList, BUY_ORDERS);
    }
    
    private StockList makeHighsecTransactions(StockList stockList, String orderType) {
        for(StockList.Item item : stockList.items.values()) {
            
            ItemType itemType = itemTypeRepository.findOne(item.itemTypeId);
            List<MarketOrder> marketOrders = marketOrderRepository.findByFetchedAndBuysellAndItemTypeInHighsec(TODAY, orderType, itemType);
            Collections.sort(marketOrders, new MarketOrderComparator(orderType));
            
            item.transactions = new ArrayList<>();
            item.transactions.addAll(processMarketOrders(itemType, marketOrders, item.quantity));
            Collections.sort(item.transactions, new Transaction.TransactionComparator(orderType));
        }
        return stockList;
    }
        
    private Collection<Transaction> processMarketOrders(ItemType itemType, List<MarketOrder> marketOrders, int quantity) {
        
        Map<Integer,Transaction> result = new HashMap<>();        // StationId,Transaction
        
        int outstanding = quantity;
        for(MarketOrder marketOrder : marketOrders) {
            int q = Math.min(marketOrder.getQuantity(), outstanding);
            if (q > 0) {
                Transaction transaction = result.get(marketOrder.getStation().getId());
                if (transaction == null) {
                    transaction = new Transaction();
                    transaction.station = marketOrder.getStation();
                    transaction.itemType = itemType;
                    transaction.minPrice = marketOrder.getPrice();
                    transaction.maxPrice = marketOrder.getPrice();
                    result.put(marketOrder.getStation().getId(), transaction);
                }
                if (marketOrder.getPrice() > transaction.maxPrice)
                    transaction.maxPrice = marketOrder.getPrice();
                if (marketOrder.getPrice() < transaction.minPrice)
                    transaction.minPrice = marketOrder.getPrice();
                transaction.totalQuantity += q;
                transaction.totalPrice += (q * marketOrder.getPrice());
                outstanding -= q;
            }
        }
        for(Transaction transaction : result.values()) {
            transaction.outstanding = outstanding > 0;
        }
        return result.values();
    }
    
}
