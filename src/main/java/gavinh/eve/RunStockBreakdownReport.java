package gavinh.eve;

import gavinh.eve.data.ItemType;
import gavinh.eve.data.ItemTypeRepository;
import gavinh.eve.data.MarketOrder;
import gavinh.eve.data.MarketOrderRepository;
import gavinh.eve.manufacturing.StockBreakdown;
import gavinh.eve.service.StockBreakdownService;
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
@Profile("run-stockbreakdown-report")
public class RunStockBreakdownReport implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(RunStockBreakdownReport.class);

    @Autowired
    private ItemTypeRepository itemTypeRepository;

    @Autowired
    private StockBreakdownService stockBreakdownService;

    @Override
    public void run(String... strings) throws Exception {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String TODAY = sdf.format(new Date());

        ItemType itemType = itemTypeRepository.findOne(11578L);     // Covops Cloaks

        StockBreakdown stockBreakdown = new StockBreakdown(itemType, 4000000, 100000, 30);

        // Get the data
        stockBreakdownService.makeBands(stockBreakdown, TODAY);

        // Print the report
        log.info("StockBreakdown\n" + stockBreakdown.toString());
    }
}
