package dk.mhc.chat;

import dk.mhc.chat.implementation.ChatClient;
import dk.mhc.chat.implementation.ChatClientHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class ChatApplication {

    public static void main(String[] args) throws InterruptedException {
        String user1 = "MasterZer0";
        String user2 = "TheDude";
        user2 = "Duderino";
        SpringApplication.run(ChatApplication.class, args);
        ChatClientHandler server = new ChatClientHandler();
        System.out.println("Creating clients...");
        ChatClient client1 = server.createClient(user1,user2);
//        ChatClient client2 = server.createClient("Dude","MasterZer0");

        while(!client1.ready){
            Thread.sleep(1000);
        }
        boolean check = true;
        ChatClient client2;

        System.out.println("Calling send message multiple times...");
        client1.sendMessage("hello world");
        Thread.sleep(1000);
        while(check){
            Thread.sleep(1000);
            List<String> notifications = server.getNotifications(user2);
            if(notifications != null && notifications.size()>0){
                String target = notifications.get(0);
                client2 = server.createClient(user2, target);
                client2.sendMessage("Hello back :)");
                Thread.sleep(1000);
                client1.sendMessage("How are you?");
                Thread.sleep(1000);
                client2.sendMessage("I am fine!");
                Thread.sleep(1000);
                client1.sendMessage("That is good to hear");
                Thread.sleep(1000);
                client2.sendMessage("What do you want to chat about");
                Thread.sleep(1000);
                client1.sendMessage("The moon");
                Thread.sleep(1000);
                client2.sendMessage("I like the moon");
                Thread.sleep(1000);
                client1.sendMessage("Me too!");
                Thread.sleep(1000);
                client2.sendMessage("That is great!");
                Thread.sleep(1000);
                client1.sendMessage("Seems first message is now deleted from history");

                System.out.println("Finished sending messages...");
                Thread.sleep(5000);
                System.out.println("List chat history for client 1...");
                System.out.println(client1.getJedisChatHistory());
                Thread.sleep(1000);
                System.out.println("List chat history for client 2...");
                System.out.println(client2.getJedisChatHistory());
                Thread.sleep(5000);


                System.out.println("Closing connections...");
                client1.close();
                client2.close();
                check = false;
            }
        }



    }

}
