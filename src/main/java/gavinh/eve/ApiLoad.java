package gavinh.eve;

import com.jayway.jsonpath.DocumentContext;
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
    
    @Override
    public void run(String... strings) throws Exception {

        Runner runner = new Runner(NUM_THREADS);
        
        DocumentContext context = Utils.get(ROOT_URL);
        String regions_href = context.read("$.regions.href", String.class);
        String marketgroups_href = context.read("$.marketGroups.href", String.class);

        log.info("Loading 12965 itemtypes ...");
        DocumentContext marketgroups_context = Utils.decodeAndGet(marketgroups_href);
        while(marketgroups_context != null) {
            List<String> marketgroup_maps = marketgroups_context.read("$.items[?(!@.parentGroup)].types.href");
            for(String types_href : marketgroup_maps) {
                runner.run(new LoadItemtype(types_href));
            }
            marketgroups_context = Utils.decodeAndGet(marketgroups_context.read("$.next.href"));
        }

        // Process regions, deal with pagination
        log.info("Loading 67 regions and 5431 solarsystems (approx 10 minutes) ...");
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

        runner.finish();
        
        log.info("Loading 13826 stargates (approx 15 minutes) ...");
        for(SolarSystem solarSystem : solarSystemRepository.findAll()) {
            runner.run(new LoadStargates(solarSystem));
        }

        runner.finish();

        log.info("Identifying highsec islands ...");
        loadService.identifyHighsecIslands();
        
        log.info("Finshed");
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
        
        private final String types_href;
        
        LoadItemtype(String types_href) {
            this.types_href = types_href;
        }
        
        @Override
        public void run() {
            loadService.loadItemType(types_href);
        }
    }
}
