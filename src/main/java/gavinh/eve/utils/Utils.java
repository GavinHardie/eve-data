package gavinh.eve.utils;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jboss.logging.Logger;
import org.springframework.web.client.RestTemplate;

/**
 * 
 * @author Gavin
 */
public class Utils {
    
    private static final int SEC_LIMIT_BEFORE_WARN = 30;
    private static Pattern ID_FROM_HREF = Pattern.compile(".*/(\\d+)/?");
    private static final Logger log = Logger.getLogger(Utils.class);
    
    private static final RestTemplate REST_TEMPLATE = new RestTemplate();
    private static final Configuration CONF = Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS);
    
    public static DocumentContext decodeAndGet(String url) {
        if (url == null)
            return null;
        try {
            return get(URLDecoder.decode(url, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static DocumentContext get(String url) {
        if (url == null)
            return null;
        int retry = 5;
        while(true) {
            long start = System.currentTimeMillis();
            try {
                String json = REST_TEMPLATE.getForObject(url, String.class);
                DocumentContext context = JsonPath.using(CONF).parse(json);
                return context;     // Normal exit
            } catch (Throwable e) {
                if (retry < 0) {
                    log.error(e.getMessage(), e);
                    return null;    // Failed exit
                } else {
                    log.info(String.format("[%s] while getting [%s] Retrying... (%d tries remaining)", e.getMessage(), url, retry));
                }
            } finally {
                long duration = System.currentTimeMillis() - start;
                if (duration > SEC_LIMIT_BEFORE_WARN * 1000) {
                    log.warn(String.format("[%d] sec to retrieve [%s]", duration / 1000, url));
                }
            }
            
            --retry;
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        }
    }
    
    public static <T> List<T> mapPathToList(Class<T> clazz, Map<String,Object> map, String... path) {
        List list = mapPath2(map, path);
        if (list == null || list.isEmpty())
            return (List<T>) null;
        
        List<T> result = new ArrayList<>();
        for(Object object : list) {
            result.add(cast(clazz, object));
        }
        return result;
    }
    
    public static <T> T mapPath(Class<T> clazz, Map<String,Object> map, String... path) {
        List list = mapPath2(map, path);
        if (list == null || list.isEmpty())
            return (T) null;
        
        if (list.size() != 1)
            throw new RuntimeException("Mapped to multiple values");
        
        return cast(clazz, list.get(0));
    }
    
    private static <T> T cast(Class<T> clazz, Object value) {
        if (value == null || clazz == Object.class)
            return (T) value;
        
        if (clazz.isInstance(value))
            return (T) value;

        if (clazz == Integer.class) {
            Integer temp = Integer.parseInt(value.toString());
            return (T) temp;
        }
        if (clazz == Long.class) {
            Long temp = Long.parseLong(value.toString());
            return (T) temp;
        }
        if (clazz == Float.class) {
            Float temp = Float.parseFloat(value.toString());
            return (T) temp;
        }
        if (clazz == Double.class) {
            Double temp = Double.parseDouble(value.toString());
            return (T) temp;
        }
        throw new RuntimeException(String.format("Unable to convert to class [%s]", clazz.getSimpleName()));
    }
    
    private static List mapPath2(Map<String,Object> map, String... path) {
        List result = null;
        boolean first = true;
        for(String pathElement : path) {
            if (first) {
                result = new ArrayList();
                result.add(map);
                first = false;
            }
            List nextResult = new ArrayList<>();
            for(Object currentResult : result) {
                if (currentResult != null) {
                    Object candidate = ((Map<String,Object>)currentResult).get(pathElement);
                    if (List.class.isInstance(candidate)) {
                        List candidateList = (List) candidate;
                        nextResult.addAll(candidateList);
                    } else {
                        nextResult.add(candidate);
                    }
                }
            }
            result = nextResult;
        }

        return result;
    }

    public static int idFromUrl(String href) {
        Matcher matcher = ID_FROM_HREF.matcher(href);
        if (!matcher.matches()) 
            throw new RuntimeException(String.format("Failed to extract ID from [%s]", href));
        return Integer.valueOf(matcher.group(1));
    }
    
}
