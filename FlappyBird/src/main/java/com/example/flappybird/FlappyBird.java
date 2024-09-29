package com.example.flappybird;

import javafx.animation.AnimationTimer;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;

public class FlappyBird extends Pane{
    //background size
    int boardWidth = 360;
    int boardHeight = 640;

    //Images
    Image backgroundImg;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;

    //drawing variables
    private Canvas canvas;
    private GraphicsContext gc;

    //bird variables
    int birdX = boardWidth/8;
    int birdY = boardHeight/2;
    int birdWidth = 34;
    int birdHeight = 24;

    //animation timer
    private long lastUpdate = 0;
    private final double UPDATE_INTERVAL = 1_000_000_000 / 30; // Target 30 FPS

    //game timer
    AnimationTimer gameLoop = new AnimationTimer() {
        @Override
        public void handle(long now) {
            if (now - lastUpdate >= UPDATE_INTERVAL) {
                move();
                draw();
                lastUpdate = now;
                if(gameOver){
                    pipeSpawner.stop();
                    gameLoop.stop();
                }
            }
        }
    };

    //bird class
    class Bird{
        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img){
            this.img = img;
        }
    }

    //pipes
    int pipeX = boardWidth;
    int pipeY = 0;
    int pipeWidth = 64;     //scaled by 1/6
    int pipeHeight = 512;
    private Timeline pipeSpawner;
    ArrayList<Pipe> pipes;
    Random random = new Random();

    class Pipe{
        int x = pipeX;
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;
        boolean passed = false;

        Pipe(Image img){
            this.img = img;
        }
    }

    //game logic
    Bird bird;
    int velocityX = -4; //move pipes to the left
    int velocityY = 0; //move bird up/down speed
    int gravity = 1;
    boolean gameOver = false;
    double score = 0;

    FlappyBird() {
        setPrefSize(boardWidth, boardHeight);
        setFocusTraversable(true);


        // Initialize Canvas and GraphicsContext
        canvas = new Canvas(getPrefWidth(), getPrefHeight());
        gc = canvas.getGraphicsContext2D();
        getChildren().add(canvas);

        //loading images
        backgroundImg = new Image(getClass().getResourceAsStream("/flappybirdbg.png"));
        birdImg = new Image(getClass().getResourceAsStream("/flappybird.png"));
        topPipeImg = new Image(getClass().getResourceAsStream("/toppipe.png"));
        bottomPipeImg = new Image(getClass().getResourceAsStream("/bottompipe.png"));

        ImageView backgroundView = new ImageView(backgroundImg);
        ImageView birdView = new ImageView(birdImg);
        ImageView topPipeView = new ImageView(topPipeImg);
        ImageView bottomPipeView = new ImageView(bottomPipeImg);

        //calling bird
        bird = new Bird(birdImg);

        //pipes
        pipes = new ArrayList<Pipe>();

        //key event handlers
        setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.SPACE) {
                if (gameOver) {
                    //restart the game
                    bird.y = birdY; //reset bird position
                    velocityY = 0; //reset velocity
                    pipes.clear(); //clear all pipes
                    gameOver = false; //reset gameOver status
                    score = 0; //reset score
                    gameLoop.start(); //start the game loop again
                    pipeSpawner.play(); //restart pipe spawner
                } else {
                    velocityY = -9; //bird jumps
                }
            }
        });

            // Pipe spawner
        pipeSpawner = new Timeline(new KeyFrame(Duration.millis(1500), e -> placePipes()));
        pipeSpawner.setCycleCount(Timeline.INDEFINITE);
        pipeSpawner.play();

        gameLoop.start();
    }

    public void placePipes() {
        // Space between the pipes
        int pipeGap = 150;
        int minTopPipeY = -pipeHeight / 2; // Top pipe starts above the screen
        int maxTopPipeY = boardHeight - pipeHeight - pipeGap; // Bottom pipe starts below the screen

        int randomPipeY = random.nextInt(maxTopPipeY - minTopPipeY + 1) + minTopPipeY;

        // Create top and bottom pipes
        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.x = boardWidth; // Start from the right edge
        topPipe.y = randomPipeY;

        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.x = boardWidth; // Start from the right edge
        bottomPipe.y = topPipe.y + pipeHeight + pipeGap;

        // Add pipes to the list
        pipes.add(topPipe);
        pipes.add(bottomPipe);
    }


    public void draw(){
        //background
        gc.drawImage(backgroundImg, 0, 0, boardWidth, boardHeight);

        //bird
        gc.drawImage(birdImg, bird.x, bird.y, bird.width, bird.height);

        //pipes
        for(int i = 0; i < pipes.size(); i++){
            Pipe pipe = pipes.get(i);
            gc.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height);
        }

        //score
        gc.setFill(javafx.scene.paint.Color.WHITE);

        //set font
        gc.setFont(new javafx.scene.text.Font("Arial", 32));

        //display score and game over message
        if (gameOver) {
            gc.fillText("Game Over: " + String.valueOf((int) score), 10, 35);
        }
        else {
            gc.fillText(String.valueOf((int) score), 10, 35);
        }
    }

    public void move(){
        //bird
        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0);

        //pipes
        for (int i = 0;i < pipes.size(); i++){
            Pipe pipe = pipes.get(i);
            pipe.x += velocityX;

            if(!pipe.passed && bird.x > pipe.x + pipe.width){
                pipe.passed = true;
                score += 0.5;
            }

            if(collision(bird,pipe)){
                gameOver = true;
            }
        }

        if(bird.y > boardHeight){
            gameOver = true;
        }
    }

    public boolean collision(Bird a, Pipe b){
        return  a.x < b.x + b.width && //a's top left corner doesnt reach b's top right corner
                a.x + a.width > b.x && //a's top right corner passes b's top left corner
                a.y < b.y + b.height && //a's top left corner doesnt reach b's bottom left corner
                a.y + a.height > b.y; //a's bottom left corner passes b's top left corner
    }

    public void KeyPressed(Event e) {

    }


}
