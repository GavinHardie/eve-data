package gavinh.eve;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;
import org.jboss.logging.Logger;
import org.springframework.web.client.RestTemplate;

/**
 * 
 * @author Gavin
 */
public class Utils {
    
    private static Logger log = Logger.getLogger(Utils.class);
    
    private static RestTemplate REST_TEMPLATE = new RestTemplate();
    private static Configuration CONF = Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS);
    
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
        try {
            String json = REST_TEMPLATE.getForObject(url, String.class);
            DocumentContext context = JsonPath.using(CONF).parse(json);
            return context;
        } catch (Throwable e) {
            log.error(String.format("[%s] while getting [%s]", e.getMessage(), url));
            throw e;
        }
    }
    
    public static <T> T mapPath(Class<T> clazz, Map<String,Object> map, String... path) {
        Object result = null;
        boolean first = true;
        for(String pathElement : path) {
            if (first) {
                result = map.get(pathElement);
                first = false;
            } else if (result != null) {
                result = ((Map<String,Object>)result).get(pathElement);
            }
        }
        if (result == null)
            return (T) result;
        
        if (clazz.isInstance(result))
            return (T) result;

        if (clazz == Integer.class) {
            Integer temp = Integer.parseInt(result.toString());
            return (T) temp;
        }
        if (clazz == Long.class) {
            Long temp = Long.parseLong(result.toString());
            return (T) temp;
        }
        if (clazz == Float.class) {
            Float temp = Float.parseFloat(result.toString());
            return (T) temp;
        }
        if (clazz == Double.class) {
            Double temp = Double.parseDouble(result.toString());
            return (T) temp;
        }
        
        throw new RuntimeException(String.format("Unable to convert to class [%s]", clazz.getSimpleName()));
    }
}
