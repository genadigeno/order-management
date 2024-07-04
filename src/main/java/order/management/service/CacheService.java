package order.management.service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import order.management.model.Order;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheService {
    private final HazelcastInstance hazelcastInstance;

    private IMap<String, Order> getOrderCacheMap(){
        return hazelcastInstance.getMap("orderCache");
    }

    public Order getFromCache(String uuid){
        return getOrderCacheMap().get(uuid);
    }

    public void putIntoCache(String uuid, Order order){
        getOrderCacheMap().put(uuid, order);
    }

    public void removeFromCache(String uuid) {
        getOrderCacheMap().delete(uuid);
    }
}
