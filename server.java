package my;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

class player{
    String id;
    String password;
    int win;
    int match_num;

    public player(String id,String password,
    int win,
    int match_num){
        this.id=id;
        this.win=win;
        this.match_num=match_num;
        this.password=password;

    }
}
public class server {
    ServerSocket serverSocket;
    List<Socket> clients=new ArrayList<>();
    HashMap<String,player> players;
    HashMap<Socket,String> getOnline;
    HashMap<String,Socket> online;
    HashMap<String,match> matches;
    BufferedReader fileread;
    BufferedWriter filewriter;
    public void reread() throws IOException {
        fileread.close();
        fileread=new BufferedReader(new FileReader("C:\\Users\\xlch\\IdeaProjects\\javaAss2\\src\\my\\players.txt"));
    }
    public void rewriter() throws IOException {
        filewriter=new BufferedWriter(new FileWriter("C:\\Users\\xlch\\IdeaProjects\\javaAss2\\src\\my\\players.txt"));
        for (player player:players.values()){
            filewriter.write(player.id+"\n");
            filewriter.write(player.password+"\n");
            filewriter.write(player.win+"\n");
            filewriter.write(player.match_num+"\n");
        }
        filewriter.close();

    }
    public server() throws IOException {
        serverSocket=new ServerSocket(8888);
        players=new HashMap<>();
        fileread=new BufferedReader(new FileReader("C:\\Users\\xlch\\IdeaProjects\\javaAss2\\src\\my\\players.txt"));
        online=new HashMap<>();
        matches=new HashMap<>();
        getOnline=new HashMap<>();
        while (fileread.ready()){
            String id=fileread.readLine();
            String password=fileread.readLine();
            String win=fileread.readLine();
            String match_num=fileread.readLine();
            player player=new player(id,password,Integer.parseInt(win),Integer.parseInt(match_num));
            players.put(id,player);
        }
        reread();
        while(true) {
            Socket client=serverSocket.accept();

            clients.add(client);
            new serverThread(client,this).start();

        }
    }
    public void rec(String imf,Socket client) throws IOException {
        String[] ss=imf.split(",");
        if (Objects.equals(ss[0], "loginp")){
            if (players.containsKey(ss[1])){
                player cp=players.get(ss[1]);
                if (cp.password.equals(ss[2])&&!online.containsKey(ss[1])){
                    if (!matches.containsKey(ss[1])){
                        online.put(cp.id, client);
                        getOnline.put(client, cp.id);
                        send_online_all();
                        send("loginy," + cp.id + "," + cp.win + "," + cp.match_num + ",", client);
                    }
                    else {
                        if (!getOnline.containsValue(ss[1])){
                            match match=matches.get(ss[1]);
                            getOnline.put(client,ss[1]);
                            String imf2="match,"+" ,";
                            send("loginy," + cp.id + "," + cp.win + "," + cp.match_num + ",", client);
                            send(imf2+board(match),client);
                            send("end,"+match.winner+",",client);
                        }
                        else {
                            send("loginn,",client);
                        }
                    }

                }
                else {
                    send("loginn,",client);
                }
            }
            else {
                String id=ss[1];
                String password=ss[2];
                String win="0";
                String match_num="0";
                player player=new player(id,password,Integer.parseInt(win),Integer.parseInt(match_num));
                players.put(id,player);
                online.put(id, client);
                getOnline.put(client, id);
                rewriter();
                send_online_all();
                send("loginy," + id + "," + win + "," + match_num + ",",client);

            }

        }
        else if (Objects.equals(ss[0], "match")){
            double f=Math.random();
            match match;
            String imf1="match,"+"Your turn,";
            String imf2="match,"+"The opponent's turn,";
            if (f>0.5){
            match=new match(ss[1],ss[2],online.get(ss[1]),online.get(ss[2]));

            send(imf1+board(match),online.get(ss[1]));
                send(imf2+board(match),online.get(ss[2]));

            }
            else {
                match=new match(ss[2],ss[1],online.get(ss[2]),online.get(ss[1]));
                send(imf1+board(match),online.get(ss[2]));
                send(imf2+board(match),online.get(ss[1]));

            }online.remove(ss[1]);
            online.remove(ss[2]);
            matches.put(ss[1],match);
            matches.put(ss[2],match);
            players.get(ss[1]).match_num++;
            players.get(ss[2]).match_num++;
            rewriter();
            send_online_all();

        }
        else if (Objects.equals(ss[0], "action")){
            String id=ss[1];
            int x= Integer.parseInt(ss[2]);
            int y= Integer.parseInt(ss[3]);
            String imf1="match,"+"Your turn,";
            String imf2="match,"+"The opponent's turn,";
            match match=matches.get(id);
            if (Objects.equals(match.id1, id)){

                if (match.update(x,y,1)){
                    match.end();
                    if (match.turn==1){
                        send(imf1+board(match),client);
                        send(imf2+board(match),match.client2);
                    }
                    else {
                        send(imf2+board(match),client);
                        send(imf1+board(match),match.client2);
                    }
                    if (Objects.equals(match.winner, match.id1)){
                        players.get(match.id1).win++;
                        rewriter();
                        send("end,"+match.winner+",",match.client1);
                        send("end,"+match.winner+",",match.client2);
                    }
                    else if (Objects.equals(match.winner, "0")){
                        send("end,"+match.winner+",",match.client1);
                        send("end,"+match.winner+",",match.client2);
                    }
                    else if (Objects.equals(match.winner, match.id2)){
                        players.get(match.id2).win++;
                        rewriter();
                        send("end,"+match.winner+",",match.client1);
                        send("end,"+match.winner+",",match.client2);
                    }
                }

            }
            else {
                if (match.update(x,y,-1)){
                    match.end();
                    if (match.turn==-1){
                        send(imf1+board(match),client);
                        send(imf2+board(match),match.client1);
                    }
                    else {
                        send(imf2+board(match),client);
                        send(imf1+board(match),match.client1);
                    }
                    if (Objects.equals(match.winner, match.id2)){
                        players.get(match.id2).win++;
                        rewriter();send("end,"+match.winner+",",match.client1);
                        send("end,"+match.winner+",",match.client2);
                    }
                    else if (Objects.equals(match.winner, "0")){
                        send("end,"+match.winner+",",match.client1);
                        send("end,"+match.winner+",",match.client2);
                    }
                    else if (Objects.equals(match.winner, match.id1)){
                        players.get(match.id1).win++;
                        rewriter();send("end,"+match.winner+",",match.client1);
                        send("end,"+match.winner+",",match.client2);
                    }
                }

            }
        }
        else if (Objects.equals(ss[0], "exit")){
            player cp=players.get(ss[1]);
            online.put(ss[1],client);
            matches.remove(ss[1]);
            send_online_all();
            send("loginy," + cp.id + "," + cp.win + "," + cp.match_num + ",",client);
        }
    }
    public void send(String imf,Socket client) throws IOException {
        PrintWriter pWriter = new PrintWriter(client.getOutputStream(), true);
        pWriter.println(imf);
    }
    public void send_online_all() throws IOException {

        for (Socket client:clients){
            StringBuilder sendimf=new StringBuilder("onlinelist,");
            for (String p:online.keySet()){
                if (online.get(p)!=client)
                    sendimf.append(p).append(",");
            }
            PrintWriter pWriter = new PrintWriter(client.getOutputStream(), true);
            pWriter.println(sendimf);
        }
    }
    public String board(match match){
        StringBuilder es= new StringBuilder();
        for (int i=0;i<3;i++){
            for (int j=0;j<3;j++){
                es.append(match.match[i][j]).append(",");
            }
        }
        return es.toString();
    }
    public void ex(Socket client) throws IOException {
        clients.remove(client);
        String id=getOnline.get(client);
        getOnline.remove(client);
        if (online.containsKey(id)){
            online.remove(id);
            send_online_all();
        }
        else if (matches.containsKey(id)){
            match match=matches.get(id);

            if (!Objects.equals(id, match.id1)){
                if (match.winner==null){
                    String id2 = match.id1;
                    players.get(id2).win++;
                    rewriter();

                    send("end," + id2 + ",", match.client1);
                    send("Your opponent is disconnected,",match.client1);
                }
                else {
                    matches.remove(id);
                }
            }
            else {
                if (match.winner==null){
                    String id2 = match.id2;
                    players.get(id2).win++;
                    rewriter();

                    send("end," + id2 + ",", match.client2);
                    send("Your opponent is disconnected,",match.client2);
                }
                else {
                    matches.remove(id);
                }
            }
        }
    }
    public static void main(String[] args) throws IOException {

        new server();

    }
}
