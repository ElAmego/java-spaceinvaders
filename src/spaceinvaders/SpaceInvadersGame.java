package spaceinvaders;

import com.javarush.engine.cell.*;
import spaceinvaders.gameobjects.*;

import java.util.ArrayList;
import java.util.List;

public class SpaceInvadersGame extends Game {
    public static final int WIDTH = 64;
    public static final int HEIGHT = 64;
    public static final int COMPLEXITY = 5;
    private static final int PLAYER_BULLETS_MAX = 1;
    private List<Star> stars;
    private List<Bullet> enemyBullets, playerBullets;
    private EnemyFleet enemyFleet;
    private PlayerShip playerShip;
    private boolean isGameStopped;
    private int animationsCount, score;


    @Override
    public void initialize() {
        setScreenSize(WIDTH, HEIGHT);

        createGame();
    }

    private void createGame() {
        enemyFleet = new EnemyFleet();
        enemyBullets = playerBullets = new ArrayList<>();
        playerShip = new PlayerShip();
        isGameStopped = false;
        animationsCount = score = 0;

        createStars();
        drawScene();
        setTurnTimer(40);
    }

    private void drawScene() {
        drawField();

        enemyFleet.draw(this);
        enemyBullets.stream().forEach(bullet -> bullet.draw(this));
        playerBullets.stream().forEach(bullet -> bullet.draw(this));
        playerShip.draw(this);
    }

    private void drawField() {
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                setCellValueEx(x, y, Color.BLACK, "");
            }
        }

        stars.stream().forEach(star -> star.draw(this));
    }

    private void createStars() {
        stars = new ArrayList<>();
        stars.add(new Star(2.5, 4));
        stars.add(new Star(10, 20));
        stars.add(new Star(19, 44.3));
        stars.add(new Star(1, 50));
        stars.add(new Star(60, 22));
        stars.add(new Star(55.5, 26));
        stars.add(new Star(3.7, 3.7));
        stars.add(new Star(0.4, 53.1));
    }

    private void moveSpaceObjects() {
        enemyFleet.move();
        enemyBullets.stream().forEach(bullet -> bullet.move());
        playerBullets.stream().forEach(bullet -> bullet.move());
        playerShip.move();
    }

    private void removeDeadBullets() {
        for (Bullet bullet: new ArrayList<>(enemyBullets)) {
            if (!bullet.isAlive || bullet.y >= HEIGHT-1) enemyBullets.remove(bullet);
        }

        for (Bullet bullet: new ArrayList<>(playerBullets)) {
            if (!bullet.isAlive || (bullet.y + bullet.height) < 0) playerBullets.remove(bullet);
        }
    }

    private void check() {
        playerShip.verifyHit(enemyBullets);
        score += enemyFleet.verifyHit(playerBullets);
        enemyFleet.deleteHiddenShips();
        removeDeadBullets();

        if (!playerShip.isAlive) stopGameWithDelay();

        if (enemyFleet.getBottomBorder() >= playerShip.y) playerShip.kill();

        if (enemyFleet.getShipsCount() == 0) {
            playerShip.win();
            stopGameWithDelay();
        }
    }

    private void stopGame(boolean isWin) {
        isGameStopped = true;
        stopTurnTimer();

        if (isWin) {
            showMessageDialog(Color.BLACK, "Вы победили!", Color.GREEN, 75);
        } else {
            showMessageDialog(Color.BLACK, "Вы проиграли!", Color.RED, 75);
        }
    }

    private void stopGameWithDelay() {
        animationsCount++;
        if (animationsCount >= 10) stopGame(playerShip.isAlive);
    }

    @Override
    public void onTurn(int step) {
        setScore(score);
        moveSpaceObjects();
        check();
        Bullet newBullet = enemyFleet.fire(this);
        if (newBullet != null) enemyBullets.add(newBullet);
        drawScene();
    }

    @Override
    public void onKeyPress(Key key) {
        if (key == Key.SPACE && isGameStopped) {
            createGame();
            return;
        }

        if (key == Key.LEFT) playerShip.setDirection(Direction.LEFT);
        else if (key == Key.RIGHT) playerShip.setDirection(Direction.RIGHT);
        else if (key == Key.SPACE) {
            Bullet bullet = playerShip.fire();

            if (bullet != null && playerBullets.size() < PLAYER_BULLETS_MAX) playerBullets.add(bullet);
        }
    }

    @Override
    public void onKeyReleased(Key key) {
        Direction playerDirection = playerShip.getDirection();

        if (
                (key == Key.LEFT && playerDirection == Direction.LEFT) ||
                        (key == Key.RIGHT && playerDirection == Direction.RIGHT)
        ) playerShip.setDirection(Direction.UP);
    }

    @Override
    public void setCellValueEx(int x, int y, Color color, String text) {
        if (x < 0 || x > WIDTH - 1 || y < 0 || y > HEIGHT - 1) return;
        super.setCellValueEx(x,  y, color, text);
    }
}