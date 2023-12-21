package ru.itis.artists.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Paint;
import javafx.util.Duration;
import javafx.embed.swing.SwingFXUtils;
import javax.imageio.ImageIO;

import ru.itis.artists.protocol.Message;
import ru.itis.artists.protocol.Type;
import ru.itis.artists.socket.SocketClient;

import java.io.File;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PaintController {

    private final int ROUND_TIME = 80;

    private final String[] tasks = new String[]{"кошку", "собаку", "сову", "машину", "рыбу", "виноград", "солнце",
            "дождь", "ветер", "человека", "врача", "дом", "строителя", "компьютер", "программиста", "церковь", "мечеть",
            "статую", "медаль", "царя", "телевизор", "чайник", "духовку", "дерево", "розу", "ромашку"};

    private SocketClient socketClient;

    private ScheduledExecutorService service;

    private Timeline timer;

    @FXML
    public BorderPane pane;

    @FXML
    private Canvas canvas;

    @FXML
    private ColorPicker colorPicker;

    @FXML
    private TextField brushSize;

    @FXML
    private CheckBox eraser;

    @FXML
    private Label timerLabel;

    @FXML
    private Label taskLabel;

    private GraphicsContext graphicsContext;

    public void initialize() {
        canvas.setDisable(true);

        graphicsContext = canvas.getGraphicsContext2D();

        canvas.setOnMouseDragged(e -> {
            double size = Double.parseDouble(brushSize.getText());
            double x = e.getX() - size / 2;
            double y = e.getY() - size / 2;

            if (eraser.isSelected()) {
                Message message = new Message();
                message.setType(Type.PLAYER_CLEAR);
                message.setBody(x + "," + y + "," + size + "," + colorPicker.getValue());
                socketClient.sendMessage(message);
                graphicsContext.clearRect(x, y, size, size);
            } else {
                Message message = new Message();
                message.setType(Type.PLAYER_DRAWS);
                message.setBody(x + "," + y + "," + size + "," + colorPicker.getValue());
                socketClient.sendMessage(message);
                graphicsContext.setFill(colorPicker.getValue());
                graphicsContext.fillRect(x, y, size, size);
            }
        });

        socketClient = new SocketClient("localhost", 71);
        socketClient.setController(this);
        socketClient.start();
        service = Executors.newScheduledThreadPool(2);

        Message connectMessage = new Message();
        connectMessage.setType(Type.PLAYER_CONNECTED);
        socketClient.sendMessage(connectMessage);
    }

    public void startGame() {
        String task = tasks[new Random().nextInt(tasks.length)];
        Message message = new Message();
        message.setType(Type.START);
        message.setBody(task);
        socketClient.sendMessage(message);

        canvas.setDisable(false);
        service.schedule(() -> Platform.runLater(() -> stopGame()), ROUND_TIME + 1, TimeUnit.SECONDS);

        timerLabel.setText(formatTime(ROUND_TIME));
        timer = new Timeline(new KeyFrame(Duration.seconds(1), animation ->
                timerLabel.setText(formatTime(unFormatTime(timerLabel.getText()) - 1))));
        timer.setCycleCount(ROUND_TIME);
        timer.play();
    }

    public void fillRect(String x, String y, String size, String color) {
        double newSize = Double.parseDouble(size);
        double newX = Double.parseDouble(x);
        double newY = Double.parseDouble(y);

        graphicsContext.setFill(Paint.valueOf(color));
        graphicsContext.fillRect(newX, newY, newSize, newSize);
    }

    public void clearRect(String x, String y, String size) {
        double newSize = Double.parseDouble(size);
        double newX = Double.parseDouble(x);
        double newY = Double.parseDouble(y);
        graphicsContext.clearRect(newX, newY, newSize, newSize);
    }

    public void setTask(String task) {
        taskLabel.setText("Нарисуйте " + task);
    }

    private String formatTime(Integer seconds) {
        int minutes = seconds / 60;
        String minutesAsString = String.valueOf(minutes);
        if (minutes < 10) {
            minutesAsString = "0" + minutesAsString;
        }

        seconds = seconds % 60;
        String secondsAsString = String.valueOf(seconds);
        if (seconds < 10) {
            secondsAsString = "0" + secondsAsString;
        }

        return minutesAsString + ":" + secondsAsString;
    }

    private Integer unFormatTime(String time) {
        String[] timeArray = time.split(":");
        return Integer.parseInt(timeArray[0]) * 60 + Integer.parseInt(timeArray[1]);
    }

    public void stopGame() {
        timer.stop();
        timerLabel.setText("Игра закончилась!");
        canvas.setDisable(true);
        Message stopMessage = new Message();
        stopMessage.setType(Type.STOP);
        socketClient.sendMessage(stopMessage);
        socketClient.stop();
    }

    public void onExit() {
        stopGame();
        Platform.exit();
    }

    public void onSave() {
        try {
            Image snapshot = canvas.snapshot(null, null);

            ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png",
                    new File("artists.png"));
        } catch (Exception e) {
            System.out.println("Failed to save image: " + e);
        }
    }
}
