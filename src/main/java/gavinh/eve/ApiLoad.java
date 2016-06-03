package gavinh.eve;

import gavinh.eve.utils.Utils;
import gavinh.eve.utils.Runner;
import com.jayway.jsonpath.DocumentContext;
import gavinh.eve.data.ItemType;
import gavinh.eve.data.ItemTypeRepository;
import gavinh.eve.data.MarketGroup;
import gavinh.eve.data.MarketGroupRepository;
import gavinh.eve.data.SolarSystem;
import gavinh.eve.data.SolarSystemRepository;
import gavinh.eve.service.LoadService;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
@Profile("load")
public class ApiLoad implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ApiLoad.class);
    
    private static final int NUM_THREADS = 20;
    private static final String ROOT_URL = "https://crest-tq.eveonline.com";

    @Autowired
    private LoadService loadService;

    @Autowired
    private SolarSystemRepository solarSystemRepository;

    @Autowired
    private MarketGroupRepository marketGroupRepository;

    @Autowired
    private ItemTypeRepository itemTypeRepository;
    
    @Override
    public void run(String... strings) throws Exception {

        
        DocumentContext context = Utils.get(ROOT_URL);
        String regions_href = context.read("$.regions.href", String.class);
        String marketgroups_href = context.read("$.marketGroups.href", String.class);
        String markettypes_href = context.read("$.marketTypes.href", String.class);

        log.info("Loading 2101 marketgroups (approx 30 minutes) ...");
        Runner marketGroupsRunner = new Runner(1);
        loadMarketgroups(marketGroupsRunner, marketgroups_href);
        
        Runner runner = new Runner(20);
        
        log.info("Loading 67 regions and 5431 solarsystems (approx 10 minutes) ...");
        loadRegions(runner, regions_href);
        
        runner.finish();
        
        log.info("Loading 13826 stargates (approx 15 minutes) ...");
        loadStargates(runner);
        
        runner.finish();
        
        log.info("Identifying highsec islands (approx 1 minute) ...");
        loadService.identifyHighsecIslands();

        marketGroupsRunner.finish();
        
        log.info("Loading 12965 itemtypes (approx XXX minutes) ...");
        loadItemTypes(runner, markettypes_href);       
        
        runner.finish();
        
        log.info("Finshed");
    }

    private void loadMarketgroups(Runner runner, String marketgroups_href) {
        runner.run(new Runnable() {
            @Override
            public void run() {
                DocumentContext marketgroups_context = Utils.decodeAndGet(marketgroups_href);
                while(marketgroups_context != null) {
                    List<Map<String,Object>> marketgroup_maps = marketgroups_context.read("$.items[*]");
                    for(Map<String,Object> marketgroup_map : marketgroup_maps) {
                        String name = Utils.mapPath(String.class, marketgroup_map, "name");
                        String marketgroup_href = Utils.mapPath(String.class, marketgroup_map, "href");
                        loadMarketGroup(marketgroup_href);
                    }
                    marketgroups_context = Utils.decodeAndGet(marketgroups_context.read("$.next.href"));
                }
            }
            
        });
    }
    
    private MarketGroup loadMarketGroup(String marketgroup_href) {
        
        int id = Utils.idFromUrl(marketgroup_href);
        
        MarketGroup marketGroup;
        marketGroup = marketGroupRepository.findOne(id);
        if (marketGroup != null)
            return marketGroup;

        DocumentContext marketgroup_context = Utils.decodeAndGet(marketgroup_href);
        String parentgroup_href = marketgroup_context.read("$.parentGroup.href", String.class);
        MarketGroup parentMarketGroup = null;
        if (parentgroup_href != null) {
            parentMarketGroup = loadMarketGroup(parentgroup_href);
        }

        marketGroup = new MarketGroup();
        marketGroup.setParentMarketGroup(parentMarketGroup);
        marketGroup.setId(id);
        marketGroup.setName(marketgroup_context.read("$.name", String.class));
        marketGroup.setHref(marketgroup_href);
        marketGroup = marketGroupRepository.save(marketGroup);
        return marketGroup;
    }
    
    private void loadItemTypes(Runner runner, String markettypes_href) {
        DocumentContext markettypes_context = Utils.decodeAndGet(markettypes_href);
        while(markettypes_context != null) {
            List<Map<String,Object>> markettype_maps = markettypes_context.read("$.items[*]");
            for(Map<String,Object> markettype_map : markettype_maps) {
                Integer id = Utils.mapPath(Integer.class, markettype_map, "type", "id");
                String name = Utils.mapPath(String.class, markettype_map, "type", "name");
                String href = Utils.mapPath(String.class, markettype_map, "type", "href");
                Integer marketGroupId = Utils.mapPath(Integer.class, markettype_map, "marketGroup", "id");
                runner.run(new LoadItemtype(id, name, href, marketGroupId));
            }
            markettypes_context = Utils.decodeAndGet(markettypes_context.read("$.next.href"));
        }
    }

    private void loadRegions(Runner runner, String regions_href) {
        DocumentContext regions_context = Utils.decodeAndGet(regions_href);
        while(regions_context != null) {
            List<Map<String,Object>> region_maps = regions_context.read("$.items[*]");
            for(Map<String,Object> region_map : region_maps) {
                String regionName = Utils.mapPath(String.class, region_map, "name");
                if (regionName.charAt(1) != '-') {
                    runner.run(new LoadRegion(region_map));
                }
            }
            regions_context = Utils.decodeAndGet(regions_context.read("$.next.href"));
        }
    }

    private void loadStargates(Runner runner) {
        for(SolarSystem solarSystem : solarSystemRepository.findAll()) {
            runner.run(new LoadStargates(solarSystem));
        }
    }

    public class LoadRegion implements Runnable {

        private final Map<String,Object> region_map;
        
        LoadRegion(Map<String,Object> region_map) {
            this.region_map = region_map;
        }
        
        @Override
        public void run() {
            loadService.loadRegion(region_map);
        }
        
    }
    
    public class LoadStargates implements Runnable {

        private final SolarSystem solarSystem;
        
        LoadStargates(SolarSystem solarSystem) {
            this.solarSystem = solarSystem;
        }
        
        @Override
        public void run() {
            loadService.loadStargates(solarSystem);
        }
        
    }
    
    public class LoadItemtype implements Runnable {
        
        private final Integer id;
        private final String name;
        private final String href;
        private final Integer marketGroupId;
        
        LoadItemtype(Integer id, String name, String href, Integer marketGroupId) {
            this.id = id;
            this.name = name;
            this.href = href;
            this.marketGroupId = marketGroupId;
        }
        
        @Override
        public void run() {
            loadService.loadItemType(id, name, href, marketGroupId);
        }
    }
    
}
