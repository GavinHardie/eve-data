package gavinh.eve.service;

import com.jayway.jsonpath.DocumentContext;
import gavinh.eve.Utils;
import gavinh.eve.data.ItemType;
import gavinh.eve.data.ItemTypeRepository;
import gavinh.eve.data.Region;
import gavinh.eve.data.RegionRepository;
import gavinh.eve.data.SolarSystem;
import gavinh.eve.data.SolarSystemRepository;
import gavinh.eve.data.Stargate;
import gavinh.eve.data.StargateRepository;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LoadService {
    
    private static final Logger log = LoggerFactory.getLogger(LoadService.class);
    private static final Pattern STARGATE_NAME = Pattern.compile("Stargate \\(([^\\)]*)\\)");
    
    @Autowired
    private RegionRepository regionRepository;
    
    @Autowired
    private SolarSystemRepository solarSystemRepository;
    
    @Autowired
    private ItemTypeRepository itemTypeRepository;

    @Autowired
    private StargateRepository stargateRepository;
    
    public void loadRegion(Map<String,Object> region_map) {

        // Create the region
        Region region = new Region();
        region.setId(Utils.mapPath(Integer.class, region_map, "id"));
        region.setName(Utils.mapPath(String.class, region_map, "name"));
        region.setHref(Utils.mapPath(String.class, region_map, "href"));
        regionRepository.save(region);
                
        DocumentContext region_context = Utils.decodeAndGet(Utils.mapPath(String.class, region_map, "href"));
        List<String> constellation_hrefs = region_context.read("$.constellations[*].href");
        for(String constellation_href : constellation_hrefs) {
            DocumentContext constellation_context = Utils.decodeAndGet(constellation_href);
            List<String> system_hrefs = constellation_context.read("$.systems[*].href");
            for(String system_href : system_hrefs) {
                DocumentContext system_context = Utils.decodeAndGet(system_href);
                
                // Create the solarsystem
                SolarSystem solarSystem = new SolarSystem();
                solarSystem.setId(system_context.read("$.id", Integer.class));
                solarSystem.setName(system_context.read("$.name", String.class));
                solarSystem.setHref(system_href);
                solarSystem.setRegion(region);
                solarSystem.setSecurity(system_context.read("$.securityStatus", Float.class));
                
                if (solarSystem.getSecurity() < 0.0)
                    solarSystem.setZone('N');
                else if (solarSystem.getSecurity() < 0.5)
                    solarSystem.setZone('L');
                else
                    solarSystem.setZone(' ');
                
                solarSystemRepository.save(solarSystem);

            }
        }
    }
    
    public void loadItemTypes(String marketgroups_href) {
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
                    }
                    type_context = Utils.decodeAndGet(type_context.read("$.next.href"));
                }
            }
            marketgroups_context = Utils.decodeAndGet(marketgroups_context.read("$.next.href"));
        }
    }

    public void loadStargates(SolarSystem solarSystem) {
        DocumentContext solarsystem_context = Utils.decodeAndGet(solarSystem.getHref());
        List<String> stargate_hrefs = solarsystem_context.read("$.stargates[*].href");
        StringBuilder builder = new StringBuilder();
        for(String stargate_href : stargate_hrefs) {
            DocumentContext stargate_context = Utils.decodeAndGet(stargate_href);
            String stargateName = stargate_context.read("$.name", String.class);
            Matcher matcher = STARGATE_NAME.matcher(stargateName);
            if (!matcher.matches())
                throw new RuntimeException(String.format("Stargate name [%s] doesnt match pattern", stargateName));
            String otherSystemName = matcher.group(1);
            SolarSystem remoteSolarSystem = solarSystemRepository.findByName(otherSystemName);

            Stargate stargate = new Stargate();
            stargate.setId(Utils.idFromUrl(stargate_href));
            stargate.setSolarSystem(solarSystem);
            stargate.setRemoteSolarSystem(remoteSolarSystem);
            stargateRepository.save(stargate);
            builder.append(builder.length() == 0 ? "to " : ", ");
            builder.append(remoteSolarSystem.getName());
        }
    }
    
    public void identifyHighsecIslands() {
        
        SolarSystem solarSystem = solarSystemRepository.findByName("Jita");
        solarSystem.setZone('H');
        solarSystemRepository.save(solarSystem);
        
        List<Stargate> stargates = stargateRepository.findWhereHighsec();
        while(stargates != null && !stargates.isEmpty()) {
            for(Stargate stargate : stargates) {
                SolarSystem remoteSolarSystem = stargate.getRemoteSolarSystem();
                remoteSolarSystem.setZone('H');
                solarSystemRepository.save(remoteSolarSystem);
            }
            stargates = stargateRepository.findWhereHighsec();
        }
        
        List<SolarSystem> remaining = solarSystemRepository.findByZone(' ');
        for(SolarSystem islandSystem : remaining) {
            islandSystem.setZone('I');
            solarSystemRepository.save(islandSystem);
        }
    }
}
