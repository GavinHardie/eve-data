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
    
    private static final int NUM_THREADS = 10;
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

        log.info("Loading 12965 itemtypes (approx 2 minutes) ...");
        loadService.loadItemTypes(marketgroups_href);
        
        while(!runner.isFinished()) {
            Thread.sleep(1000);
        }
        
        log.info("Loading 13826 stargates (approx 30 minutes) ...");
        for(SolarSystem solarSystem : solarSystemRepository.findAll()) {
            runner.run(new LoadStargates(solarSystem));
        }
        
        while(!runner.isFinished()) {
            Thread.sleep(1000);
        }

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
}
