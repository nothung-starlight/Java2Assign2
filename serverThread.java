package my;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class serverThread extends Thread {
    Socket client;
    server server;

    public serverThread(Socket client, server server) {
        this.client = client;
        this.server = server;
    }

    public void run() {
        try {


            String inStr;
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            while ((inStr = in.readLine()) != null) {
                System.out.println(inStr);
                server.rec(inStr, client);
            }
        } catch (IOException e) {
            try {
                server.ex(client);
                System.out.println("break");
            } catch (IOException ignored) {

            }
        }
    }
}
