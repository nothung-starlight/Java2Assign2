package my;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

class client extends Thread {
    Socket client;
    public PrintWriter pWriter;
    public BufferedReader br;
    String state;
    boolean update;
    String id;
    String win;
    String num_matches;
    List<String> online;
    int[][] match;
    String turn;
    static boolean con = true;
    static String warn = null;

    @Override
    public void run() {
        try {
            InetAddress address = InetAddress.getLocalHost();
            client = new Socket(address, 8888);
            br = new BufferedReader(new InputStreamReader(client.getInputStream()));
            pWriter = new PrintWriter(client.getOutputStream(), true);
            state = "login";
            update = true;
            match = new int[3][3];

            String inStr;
            while ((inStr = br.readLine()) != null) {
                System.out.println(inStr);
                String[] ss = inStr.split(",");
                if (Objects.equals(ss[0], "loginy")) {
                    state = "wait";
                    update = true;
                    id = ss[1];
                    win = ss[2];
                    num_matches = ss[3];

                } else if (Objects.equals(ss[0], "loginn")) {
                    warn = "The password is incorrect or the account has been logged in";
                } else if (Objects.equals(ss[0], "onlinelist")) {
                    online = Arrays.asList(ss).subList(1, ss.length);
                    if (Objects.equals(state, "wait")) {
                        update = true;
                    }
                } else if (Objects.equals(ss[0], "match")) {
                    state = "match";
                    update = true;
                    turn = ss[1];
                    int k = 2;
                    for (int i = 0; i < 3; i++) {
                        for (int j = 0; j < 3; j++) {
                            match[i][j] = Integer.parseInt(ss[k]);
                            k++;
                        }
                    }
                } else if (Objects.equals(ss[0], "end")) {
                    state = "end";
                    update = true;
                    if (Objects.equals(ss[1], id)) {
                        turn = "You Win";
                    } else if (Objects.equals(ss[1], "0")) {
                        turn = "Draw";
                    } else {
                        turn = "You Lost";
                    }
                } else if (Objects.equals(ss[0], "Your opponent is disconnected")) {
                    warn = ss[0];
                }
            }
        } catch (IOException e) {
            con = false;
        }
    }

}

public class fx extends Application {
    private static final int BOUND = 90;
    private static final int OFFSET = 15;
    static client client = new client();

    static {
        client.start();
    }

    @Override
    public void start(Stage stage) {


        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!my.client.con) {
                    stage.close();
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Lost connection to server");
                    alert.show();
                    my.client.con = true;
                    alert.setOnCloseRequest(event -> {
                        System.exit(0);
                    });

                }
                if (my.client.warn != null) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, my.client.warn);
                    alert.show();
                    my.client.warn = null;
                }
                if (client.update) {
                    client.update = false;
                    if (Objects.equals(client.state, "login")) {
                        stage.setTitle("Login");
                        Pane pane = new Pane();
                        TextField account = new TextField();
                        account.relocate(100, 50);
                        pane.getChildren().add(account);
                        TextField password = new TextField();
                        password.relocate(100, 150);
                        pane.getChildren().add(password);
                        Button login = new Button("   login   ");
                        pane.getChildren().add(login);
                        login.relocate(150, 250);
                        login.setOnAction(e -> {
                            if (!account.getText().equals("") && !password.getText().equals("")) {
                                client.pWriter.println("loginp," + account.getText() + "," + password.getText() + ",");
                            }


                        });
                        Scene scene = new Scene(pane, 400, 300);
                        stage.setScene(scene);

                    } else if (Objects.equals(client.state, "wait")) {
                        Label idl = new Label("ID: " + client.id + "\n" + "win: " + client.win + "\n" + "match: " + client.num_matches);
                        Pane pane = new Pane();
                        pane.setPrefSize(400, 400);


                        Scene scene = new Scene(pane);
                        VBox vbox = new VBox();
                        ObservableList<String> obList = FXCollections.observableArrayList(client.online);
                        ListView<String> listView = new ListView<>(obList);
                        listView.setPrefSize(400, 200);
                        vbox.getChildren().add(listView);
                        listView.getSelectionModel().selectedItemProperty().addListener((arg0, old_str, new_str) -> {
                            // getSelectedIndex方法可获得选中项的序号，getSelectedItem方法可获得选中项的对象
                            String id = listView.getSelectionModel().getSelectedItem();
                            client.pWriter.println("match," + client.id + "," + id + ",");

                        });
                        vbox.setLayoutY(100);
                        pane.getChildren().add(idl);
                        pane.getChildren().add(vbox);
                        stage.setScene(scene);
                        stage.setTitle("Welcome");

                    } else if (Objects.equals(client.state, "match")) {
                        Label t = new Label(client.turn);
                        t.setLayoutY(300);
                        Rectangle game_panel = new Rectangle();
                        game_panel.setArcHeight(5);
                        game_panel.setArcWidth(5);
                        game_panel.setFill(Paint.valueOf("#f2f2f2"));
                        game_panel.setWidth(270);
                        game_panel.setHeight(270);
                        game_panel.setLayoutX(15);
                        game_panel.setLayoutY(15);
                        game_panel.setStroke(Paint.valueOf("BLACK"));
                        game_panel.setStrokeType(StrokeType.valueOf("INSIDE"));
                        Line line1 = new Line();
                        line1.setEndX(170);
                        line1.setStartX(-100);
                        line1.setLayoutX(115);
                        line1.setLayoutY(105);
                        Line line2 = new Line();
                        line2.setEndX(170);
                        line2.setStartX(-100);
                        line2.setLayoutX(115);
                        line2.setLayoutY(195);
                        Line line3 = new Line();
                        line3.setEndX(170);
                        line3.setStartX(-100);
                        line3.setLayoutX(70);
                        line3.setLayoutY(150);
                        line3.setRotate(270);
                        Line line4 = new Line();
                        line4.setEndX(170);
                        line4.setStartX(-100);
                        line4.setLayoutX(160);
                        line4.setLayoutY(150);
                        line4.setRotate(90);
                        Pane base_square = new Pane();
                        base_square.getChildren().add(game_panel);
                        base_square.getChildren().add(line3);
                        base_square.getChildren().add(line1);
                        base_square.getChildren().add(line4);
                        base_square.getChildren().add(line2);
                        base_square.getChildren().add(t);
                        base_square.setLayoutX(150);
                        base_square.setLayoutX(50);
                        base_square.setPrefSize(400, 400);
                        game_panel.setOnMouseClicked(event -> {
                            int x = (int) (event.getX() / BOUND);
                            int y = (int) (event.getY() / BOUND);
                            client.pWriter.println("action," + client.id + "," + x + "," + y + ",");
                        });
                        Scene scene = new Scene(base_square);
                        stage.setScene(scene);
                        for (int i = 0; i < 3; i++) {
                            for (int j = 0; j < 3; j++) {
                                if (client.match[i][j] == 1) {
                                    Circle circle = new Circle();
                                    base_square.getChildren().add(circle);
                                    circle.setCenterX(i * BOUND + BOUND / 2.0 + OFFSET);
                                    circle.setCenterY(j * BOUND + BOUND / 2.0 + OFFSET);
                                    circle.setRadius(BOUND / 2.0 - OFFSET / 2.0);
                                    circle.setStroke(Color.RED);
                                    circle.setFill(Color.TRANSPARENT);
                                } else if (client.match[i][j] == -1) {

                                    Line line_a = new Line();
                                    Line line_b = new Line();
                                    base_square.getChildren().add(line_a);
                                    base_square.getChildren().add(line_b);
                                    line_a.setStartX(i * BOUND + OFFSET * 1.5);
                                    line_a.setStartY(j * BOUND + OFFSET * 1.5);
                                    line_a.setEndX((i + 1) * BOUND + OFFSET * 0.5);
                                    line_a.setEndY((j + 1) * BOUND + OFFSET * 0.5);
                                    line_a.setStroke(Color.BLUE);

                                    line_b.setStartX((i + 1) * BOUND + OFFSET * 0.5);
                                    line_b.setStartY(j * BOUND + OFFSET * 1.5);
                                    line_b.setEndX(i * BOUND + OFFSET * 1.5);
                                    line_b.setEndY((j + 1) * BOUND + OFFSET * 0.5);
                                    line_b.setStroke(Color.BLUE);
                                }

                            }
                        }
                        stage.setTitle(client.id);
                    } else if (Objects.equals(client.state, "end")) {
                        Label t = new Label(client.turn);
                        t.setLayoutY(300);
                        Rectangle game_panel = new Rectangle();
                        game_panel.setArcHeight(5);
                        game_panel.setArcWidth(5);
                        game_panel.setFill(Paint.valueOf("#f2f2f2"));
                        game_panel.setWidth(270);
                        game_panel.setHeight(270);
                        game_panel.setLayoutX(15);
                        game_panel.setLayoutY(15);
                        game_panel.setStroke(Paint.valueOf("BLACK"));
                        game_panel.setStrokeType(StrokeType.valueOf("INSIDE"));
                        Line line1 = new Line();
                        line1.setEndX(170);
                        line1.setStartX(-100);
                        line1.setLayoutX(115);
                        line1.setLayoutY(105);
                        Line line2 = new Line();
                        line2.setEndX(170);
                        line2.setStartX(-100);
                        line2.setLayoutX(115);
                        line2.setLayoutY(195);
                        Line line3 = new Line();
                        line3.setEndX(170);
                        line3.setStartX(-100);
                        line3.setLayoutX(70);
                        line3.setLayoutY(150);
                        line3.setRotate(270);
                        Line line4 = new Line();
                        line4.setEndX(170);
                        line4.setStartX(-100);
                        line4.setLayoutX(160);
                        line4.setLayoutY(150);
                        line4.setRotate(90);
                        Pane base_square = new Pane();
                        base_square.getChildren().add(game_panel);
                        base_square.getChildren().add(line3);
                        base_square.getChildren().add(line1);
                        base_square.getChildren().add(line4);
                        base_square.getChildren().add(line2);
                        base_square.getChildren().add(t);
                        base_square.setLayoutX(150);
                        base_square.setLayoutX(50);
                        base_square.setPrefSize(400, 400);
                        Button button = new Button("exit");
                        button.setLayoutY(350);
                        button.setOnAction(e -> {
                            client.pWriter.println("exit," + client.id + ",");
                        });
                        base_square.getChildren().add(button);
                        Scene scene = new Scene(base_square);
                        stage.setScene(scene);
                        for (int i = 0; i < 3; i++) {
                            for (int j = 0; j < 3; j++) {
                                if (client.match[i][j] == 1) {
                                    Circle circle = new Circle();
                                    base_square.getChildren().add(circle);
                                    circle.setCenterX(i * BOUND + BOUND / 2.0 + OFFSET);
                                    circle.setCenterY(j * BOUND + BOUND / 2.0 + OFFSET);
                                    circle.setRadius(BOUND / 2.0 - OFFSET / 2.0);
                                    circle.setStroke(Color.RED);
                                    circle.setFill(Color.TRANSPARENT);
                                } else if (client.match[i][j] == -1) {

                                    Line line_a = new Line();
                                    Line line_b = new Line();
                                    base_square.getChildren().add(line_a);
                                    base_square.getChildren().add(line_b);
                                    line_a.setStartX(i * BOUND + OFFSET * 1.5);
                                    line_a.setStartY(j * BOUND + OFFSET * 1.5);
                                    line_a.setEndX((i + 1) * BOUND + OFFSET * 0.5);
                                    line_a.setEndY((j + 1) * BOUND + OFFSET * 0.5);
                                    line_a.setStroke(Color.BLUE);

                                    line_b.setStartX((i + 1) * BOUND + OFFSET * 0.5);
                                    line_b.setStartY(j * BOUND + OFFSET * 1.5);
                                    line_b.setEndX(i * BOUND + OFFSET * 1.5);
                                    line_b.setEndY((j + 1) * BOUND + OFFSET * 0.5);
                                    line_b.setStroke(Color.BLUE);
                                }

                            }
                        }
                        stage.setTitle(client.id);
                    }
                    stage.show();
                }
            }
        };
        timer.start();
        stage.setOnCloseRequest(event -> {

            try {

                System.exit(0);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }


        });


    }

    public static void main(String[] args) throws IOException {

        launch(args);

    }

}
