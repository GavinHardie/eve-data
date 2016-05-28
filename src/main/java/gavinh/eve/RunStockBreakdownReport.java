package gavinh.eve;

import gavinh.eve.data.ItemType;
import gavinh.eve.data.ItemTypeRepository;
import gavinh.eve.data.MarketOrder;
import gavinh.eve.data.MarketOrderRepository;
import gavinh.eve.manufacturing.StockBreakdown;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
@Profile("run-stocklevel-report")
public class RunStockLevelReport implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(RunStockLevelReport.class);

    @Autowired
    private ItemTypeRepository itemTypeRepository;

    @Autowired
    private MarketOrderRepository marketOrderRepository;

    @Override
    public void run(String... strings) throws Exception {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String TODAY = sdf.format(new Date());

        ItemType itemType = itemTypeRepository.findOne(11578L);     // Covops Cloaks

        // Get the data
        StockBreakdown stockBreakdown = new StockBreakdown(itemType, 4000000, 100000, 30);
        List<MarketOrder> marketOrders = marketOrderRepository.findByFetchedAndBuysellAndItemType(TODAY, "sell", itemType);
        for(MarketOrder marketOrder : marketOrders) {
            stockBreakdown.addStock(marketOrder.getStation(), marketOrder.getPrice(), marketOrder.getQuantity());
        }

        // Print the report
        for(StockBreakdown.StockBand stockBand : stockBreakdown.stockBands) {
            StringBuilder summary = new StringBuilder();
            for(StockBreakdown.StockZone zone : StockBreakdown.StockZone.values()) {
                Integer quantity = stockBand.quantity.get(zone);
                if (quantity != null) {
                    summary.append(String.format("   %d in %s", quantity, zone.toString()));
                }
            }
            log.info(String.format("[%s]%s", stockBand.getDesc(), summary.toString()));
        }

        StringBuilder summary = new StringBuilder();
        for(StockBreakdown.StockZone zone : StockBreakdown.StockZone.values()) {
            Integer quantity = stockBreakdown.totals.get(zone);
            if (quantity != null) {
                summary.append(String.format("   %d in %s", quantity, zone.toString()));
            }
        }
        log.info(String.format("TOTALS%s", summary.toString()));
    }
}
