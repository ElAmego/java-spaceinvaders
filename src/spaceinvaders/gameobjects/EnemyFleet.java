package spaceinvaders.gameobjects;

import com.javarush.engine.cell.Game;
import spaceinvaders.*;

import java.util.ArrayList;
import java.util.List;

public class EnemyFleet {
    private static final int ROWS_COUNT = 3;
    private static final int COLUMNS_COUNT = 10;
    private static final int STEP = ShapeMatrix.ENEMY.length+1;
    private List<EnemyShip> ships;
    private Direction direction = Direction.RIGHT;

    public EnemyFleet() {
        createShips();
    }

    private void createShips() {
        ships = new ArrayList<>();
        for (int y = 0; y < ROWS_COUNT; y++) {
            for (int x = 0; x < COLUMNS_COUNT; x++) {
                ships.add(new EnemyShip( x * STEP, y * STEP + 12));
            }
        }

        ships.add(new Boss(STEP * COLUMNS_COUNT / 2 - ShapeMatrix.BOSS_ANIMATION_FIRST.length / 2 - 1, 5));
    }

    public void draw(Game game) {
        ships.stream().forEach(enemyShip -> enemyShip.draw(game));
    }

    private double getLeftBorder() {
        return ships.stream().sorted(((o1, o2) -> (int) (o1.x - o2.x))).findFirst().get().x;
    }

    private double getRightBorder() {
        double max = ships.get(0).x + ships.get(0).width;

        for (int i = 1; i < ships.size(); i++) {
            max = ships.get(i).x + ships.get(i).width > max ? ships.get(i).x + ships.get(i).width : max;
        }

        return max;
    }

    private double getSpeed() {
        return 2.0 > (3.0 / ships.size()) ? (3.0 / ships.size()) : 2.0;
    }

    public void move() {
        if (ships.isEmpty()) return;

        Direction odlDirection = direction;

        if (direction == Direction.LEFT && getLeftBorder() < 0) direction = Direction.RIGHT;
        else if (direction == Direction.RIGHT && getRightBorder() > SpaceInvadersGame.WIDTH) direction = Direction.LEFT;

        if (odlDirection != direction) {
            ships.stream().forEach(enemyShip -> enemyShip.move(Direction.DOWN, getSpeed()));
        } else {
            ships.stream().forEach(enemyShip -> enemyShip.move(direction, getSpeed()));
        }

        getSpeed();
    }

    public Bullet fire(Game game) {
        if (ships.isEmpty()) return null;

        if (game.getRandomNumber(100 / SpaceInvadersGame.COMPLEXITY) > 0) return null;

        return ships.get(game.getRandomNumber(ships.size())).fire();
    }

    public int verifyHit(List<Bullet> bullets) {
        int score = 0;

        if (bullets.isEmpty()) return score;
        for (EnemyShip enemyShip: ships) {
            for (Bullet bullet: bullets) {
                if (enemyShip.isCollision(bullet) && enemyShip.isAlive && bullet.isAlive) {
                    enemyShip.kill();
                    bullet.kill();
                    score += enemyShip.score;
                }
            }
        }

        return score;
    }

    public void deleteHiddenShips() {
        for (EnemyShip enemyShip: new ArrayList<>(ships)) {
            if (enemyShip.isVisible() == false) ships.remove(enemyShip);
        }
    }

    public double getBottomBorder() {
        double result = 0;

        for (EnemyShip enemyShip: ships) {
            double sum = enemyShip.y + enemyShip.height;

            if (sum > result) result = sum;
        }

        return result;
    }

    public int getShipsCount() {
        return ships.size();
    }
}