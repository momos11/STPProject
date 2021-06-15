package de.uniks.stp.controller.snake;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.snake.model.Food;
import de.uniks.stp.controller.snake.model.Game;
import de.uniks.stp.controller.snake.model.Snake;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import util.ResourceManager;

import java.util.ArrayList;
import java.util.Random;

import static de.uniks.stp.controller.snake.Constants.*;

public class SnakeGameController {

    private final Parent view;
    private final ModelBuilder builder;
    private Scene scene;
    private Label scoreLabel;
    private Label highScoreLabel;
    private Canvas gameField;
    private Game game;
    private ArrayList<Snake> snake;
    private Food food;
    private int snakeHead = 0;
    private ArrayList<Snake> addNewBodyQueue;


    public SnakeGameController(Scene scene, Parent view, ModelBuilder builder) {
        this.scene = scene;
        this.view = view;
        this.builder = builder;
    }

    public void init() throws InterruptedException {
        scoreLabel = (Label) view.lookup("#label_score");
        highScoreLabel = (Label) view.lookup("#label_highscore");
        gameField = (Canvas) view.lookup("#gameField");
        GraphicsContext brush = gameField.getGraphicsContext2D();
        scoreLabel.setText("Score:");
        highScoreLabel.setText("Highscore:");

        game = new Game(0, ResourceManager.loadHighScore());
        snake = new ArrayList<>();
        addNewBodyQueue = new ArrayList<>();

        scene.setOnKeyPressed(key -> {
            if (key.getCode() == KeyCode.RIGHT || key.getCode() == KeyCode.D) {
                game.setCurrentDirection(Game.Direction.RIGHT);

            } else if (key.getCode() == KeyCode.LEFT || key.getCode() == KeyCode.A) {
                game.setCurrentDirection(Game.Direction.LEFT);

            } else if (key.getCode() == KeyCode.UP || key.getCode() == KeyCode.W) {
                game.setCurrentDirection(Game.Direction.UP);

            } else if (key.getCode() == KeyCode.DOWN || key.getCode() == KeyCode.S) {
                game.setCurrentDirection(Game.Direction.DOWN);
            }
        });

        drawMap(brush);
        spawnFood(brush);
        spawnSnake(brush);

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(SPEED), run -> loop(brush)));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void loop(GraphicsContext brush) {
        System.out.println("RUN");
        drawMap(brush);
        drawFood(brush);
        moveSnake(brush);
    }

    ////////////////////////////////////////////////
    //// Snake
    ////////////////////////////////////////////////
    private void spawnSnake(GraphicsContext brush) {
        Random rand = new Random();
        snake.add(snakeHead, new Snake().setPosX(rand.nextInt(COLUMN) * FIELD_SIZE).setPosY(rand.nextInt(ROW) * FIELD_SIZE));
        snake.add(1, new Snake().setPosX(snake.get(snakeHead).getPosX() - FIELD_SIZE).setPosY(snake.get(snakeHead).getPosY()));
        snake.add(2, new Snake().setPosX(snake.get(1).getPosX() - FIELD_SIZE).setPosY(snake.get(1).getPosY()));

        brush.setFill(Color.web("000000"));
        for (int i = 0; i < snake.size(); i++) {
            brush.fillRect(snake.get(i).getPosX(), snake.get(i).getPosY(), FIELD_SIZE, FIELD_SIZE);
        }
    }

    private void moveSnake(GraphicsContext brush) {
        for (int i = snake.size() - 1; i > snakeHead; i--) {
            snake.get(i).setPosX(snake.get(i - 1).getPosX()).setPosY(snake.get(i - 1).getPosY());
        }
        switch (game.getCurrentDirection()) {
            case UP:
                snake.get(snakeHead).addPosY(-FIELD_SIZE);
                break;

            case DOWN:
                snake.get(snakeHead).addPosY(FIELD_SIZE);
                break;

            case LEFT:
                snake.get(snakeHead).addPosX(-FIELD_SIZE);
                break;

            case RIGHT:
                snake.get(snakeHead).addPosX(FIELD_SIZE);
                break;
        }
        brush.setFill(Color.web("000000"));
        for (int i = 0; i < snake.size(); i++) {
            brush.fillRect(snake.get(i).getPosX(), snake.get(i).getPosY(), FIELD_SIZE, FIELD_SIZE);
        }
        eatFoot(brush);
        addNewBody();
    }

    private void addNewBody() {
        if (addNewBodyQueue.size() > 0) {
            if (addNewBodyQueue.get(0).getPosX() == snake.get(snake.size() - 1).getPosX() && addNewBodyQueue.get(0).getPosY() == snake.get(snake.size() - 1).getPosY()) {
                snake.add(addNewBodyQueue.remove(0));
            }
        }
    }


    ////////////////////////////////////////////////
    //// Food
    ////////////////////////////////////////////////
    private void spawnFood(GraphicsContext brush) {
        food = new Food();
        drawFood(brush);
    }

    private void drawFood(GraphicsContext brush) {
        brush.drawImage(food.getFoodPic(), food.getPosX(), food.getPosY(), FIELD_SIZE, FIELD_SIZE);
    }

    private void eatFoot(GraphicsContext brush) {
        if (snake.get(snakeHead).getPosX() == food.getPosX() && snake.get(snakeHead).getPosY() == food.getPosY()) {
            addNewBodyQueue.add(new Snake().setPosX(food.getPosX()).setPosY(food.getPosY()));
            spawnFood(brush);
        }
    }

    ////////////////////////////////////////////////
    //// Map
    ////////////////////////////////////////////////
    private void drawMap(GraphicsContext brush) {
        for (int row = 0; row < ROW; row++) {
            for (int column = 0; column < COLUMN; column++) {
                if (row % 2 == 0) {
                    if (column % 2 == 0) {
                        brush.setFill(Color.web("8FDD37"));
                    } else {
                        brush.setFill(Color.web("6DCC01"));
                    }
                } else {
                    if (column % 2 == 1) {
                        brush.setFill(Color.web("8FDD37"));
                    } else {
                        brush.setFill(Color.web("6DCC01"));
                    }
                }
                brush.fillRect(column * FIELD_SIZE, row * FIELD_SIZE, FIELD_SIZE, FIELD_SIZE);
            }
        }
    }

    public void stop() {
        ResourceManager.saveHighScore(game.getHighScore());
        scene.setOnKeyPressed(null);
    }
}
