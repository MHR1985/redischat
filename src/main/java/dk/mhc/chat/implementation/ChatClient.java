package dk.mhc.chat.implementation;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

import java.util.ArrayList;
import java.util.List;

public class ChatClient {

    private String username;
    private String target;
    private JedisPool jedisPool;
    private Thread chatThread;
    public List<String> chatHistory = new ArrayList();
    public boolean ready = false;

    public ChatClient(String username, String target, JedisPool jedisPool) {
        this.username = username;
        this.target = target;
        this.jedisPool = jedisPool;
    }

    public String getUsername() {
        return username;
    }

    public void connect() {
        chatThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try (Jedis jedis = jedisPool.getResource()) {
                    String channelName = getChannelName();
                    System.out.println("Subscribing to " + channelName);
                    jedis.subscribe(new JedisPubSub() {
                        @Override
                        public void onMessage(String channel, String message) {
                            System.out.println(message);
                            chatHistory.add(message);
                        }

                        @Override
                        public void onSubscribe(String channel, int subscribedChannels) {
                            super.onSubscribe(channel, subscribedChannels);
                            System.out.println("Subscribed to channel: "+ channelName + ". Amount in total: " + subscribedChannels);
                            ready=true;
                        }
                    }, channelName);


                }
            }
        });
        chatThread.start();

    }

    public void close(){
        if(chatThread!= null){
            System.out.println("closing connection for user: " + username + " in channel: " + getChannelName());
            chatThread.stop();
        }
    }

    public void sendMessage(String message) {
        try (Jedis jedis = jedisPool.getResource()) {
            String channelName = getChannelName();
            System.out.println("Publishing to " + channelName);
            String editedMessage = username + ": " + message;
            jedis.publish(channelName, editedMessage);
            addMessageToRedisHistory(editedMessage);
            notifyTarget();
        }
    }

    private String getChannelName() {
        String a = username;
        String b = target;

        int compare = a.compareTo(b);

        if (compare < 0) {
            return "channel:" + username + target;
        } else if (compare > 0) {
            return "channel:" + target + username;
        } else {
            return "channel:" + username + target;
        }
    }

    private void notifyTarget(){
        try (Jedis jedis = jedisPool.getResource()) {
            String key = "notification:" + target;
            jedis.lpush(key,username);
            jedis.ltrim(key,0,999);

        }
    }

    private String getHistoryKey(){
        String a = username;
        String b = target;

        int compare = a.compareTo(b);

        if (compare < 0) {
            return "history:" + username + target;
        } else if (compare > 0) {
            return "history:" + target + username;
        } else {
            return "history:" + username + target;
        }
    }

    private void addMessageToRedisHistory(String message){
        String historyKey = getHistoryKey();
        try (Jedis jedis = jedisPool.getResource()) {
            long listSize = jedis.llen(historyKey);
            if (listSize >= 10) {
                jedis.lpop(historyKey);
            }
            jedis.rpush(historyKey,message);

        }
    }

    public List<String> getJedisChatHistory(){
        List<String> history;
        try (Jedis jedis = jedisPool.getResource()) {
            history = jedis.lrange(getHistoryKey(),0,10);
            return history;
        }
    }

}
