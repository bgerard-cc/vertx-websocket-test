package org.example;


public class Main {
    public static void main(String[] args) {
        WebSocketClient client = new WebSocketClient("localhost", 4000, "/");
        client.connect();
    }
}