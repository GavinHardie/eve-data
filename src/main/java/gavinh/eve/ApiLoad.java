package gavinh.eve;

import com.jayway.jsonpath.DocumentContext;
import gavinh.eve.data.ItemType;
import gavinh.eve.data.ItemTypeRepository;
import gavinh.eve.data.Region;
import gavinh.eve.data.RegionRepository;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

@SpringBootApplication
@Profile("load")
public class ApiLoad implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ApiLoad.class);
    
    private static final String ROOT_URL = "https://crest-tq.eveonline.com";

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private ItemTypeRepository itemTypeRepository;
    
    @Override
    @Transactional
    public void run(String... strings) throws Exception {
        
        int numRecords = 0;
        
        DocumentContext context = Utils.get(ROOT_URL);
        String regions_href = context.read("$.regions.href");
        String marketgroups_href = context.read("$.marketGroups.href");

        // Process regions, deal with pagination
        DocumentContext regions_context = Utils.decodeAndGet(regions_href);
        while(regions_context != null) {
            List<Map<String,Object>> region_maps = regions_context.read("$.items[*]");
            for(Map<String,Object> region_map : region_maps) {
                String regionName = Utils.mapPath(String.class, region_map, "name");
                if (regionName.charAt(1) != '-') {
                    Region region = new Region();
                    region.setId(Utils.mapPath(Integer.class, region_map, "id"));
                    region.setName(regionName);
                    regionRepository.save(region);
                    log.info(String.format("%04d Region [%s]", ++numRecords, region.getName()));
                }
            }
            regions_context = Utils.decodeAndGet(regions_context.read("$.next.href"));
        }

        // Process marketGroups, deal with pagination
        DocumentContext marketgroups_context = Utils.decodeAndGet(marketgroups_href);
        while(marketgroups_context != null) {
            List<String> marketgroup_maps = marketgroups_context.read("$.items[?(!@.parentGroup)].types.href");
            for(String types_href : marketgroup_maps) {
                // Process the itemTypes, deal with pagination
                DocumentContext type_context = Utils.get(types_href);
                while(type_context != null) {
                    List<Map<String,Object>> type_maps = type_context.read("$.items[*].type");
                    for(Map<String,Object> type_map : type_maps) {
                        ItemType itemType = new ItemType();
                        itemType.setId(Utils.mapPath(Integer.class, type_map, "id"));
                        itemType.setName(Utils.mapPath(String.class, type_map, "name"));
                        itemTypeRepository.save(itemType);
                        log.info(String.format("%04d ItemType [%s]", ++numRecords, itemType.getName()));
                    }
                    type_context = Utils.decodeAndGet(type_context.read("$.next.href"));
                }
            }
            marketgroups_context = Utils.decodeAndGet(marketgroups_context.read("$.next.href"));
        }
    }
}
