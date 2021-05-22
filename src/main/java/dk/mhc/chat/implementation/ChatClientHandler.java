package dk.mhc.chat.implementation;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;
import java.util.List;

public class ChatClientHandler {

    private final String host = "localhost";
    private final int port = 6379;

    final JedisPoolConfig poolConfig = buildPoolConfig();
    JedisPool jedisPool = new JedisPool(poolConfig, "localhost");

    private JedisPoolConfig buildPoolConfig() {
        final JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(128);
        poolConfig.setMaxIdle(128);
        poolConfig.setMinIdle(16);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setMinEvictableIdleTimeMillis(Duration.ofSeconds(60).toMillis());
        poolConfig.setTimeBetweenEvictionRunsMillis(Duration.ofSeconds(30).toMillis());
        poolConfig.setNumTestsPerEvictionRun(3);
        poolConfig.setBlockWhenExhausted(true);
        return poolConfig;
    }


    public ChatClient createClient(String user, String target) {
            ChatClient client = new ChatClient(user,target,jedisPool);
            client.connect();
            return client;
    }

    public List<String> getNotifications(String user){
        try (Jedis jedis = jedisPool.getResource()) {
            String key = "notification:" + user;
            return jedis.lrange(key,0,999);
        }
    }

}
