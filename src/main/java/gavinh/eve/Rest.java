/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
 * @author gavin
 */
public class Rest {
    
    private static Logger log = Logger.getLogger(Rest.class);
    
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
            String root = REST_TEMPLATE.getForObject(url, String.class);
            DocumentContext context = JsonPath.using(CONF).parse(root);
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
