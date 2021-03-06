package com.game.entities;

import java.awt.Point;
import java.util.ArrayList;

import com.game.audio.SoundEffects;
import com.game.graphics.Animation;
import com.game.graphics.LayerRenderer;
import com.game.graphics.Sequence;
import com.game.graphics.Textures;
import com.game.level.Level;
import com.game.utils.RandomUtils;
import com.game.vector.Vector;

public class EvilPlayerBoss extends Enemy {

	private Animation animation;
	private Animation bow;
	private double armRotation;
	private int facing;
	private ArrayList<Point> path;
	private int timer;
	private int phase;

	public EvilPlayerBoss(Level level, double x, double y) {
		super(level, x, y, 10, 30, 1, 60);
		animation = new Animation(Textures.instance.getTexture("evil_player_boss"),
				Sequence.formatSequences(
						new Sequence(16, 35, 5, 4),
						new Sequence(16, 35, 5, 4),
						new Sequence(16, 35, 5, 4),
						new Sequence(16, 35, 5, 4),
						new Sequence(16, 35, 5, 4),
						new Sequence(16, 35, 5, 4)));

		bow = new Animation(Textures.instance.getTexture("evil_bow"),
				Sequence.formatSequences(
						new Sequence(18, 14, 0, 1),
						new Sequence(18, 14, 2, 5).setNoLoop()));

		path = new ArrayList<Point>();
		timer = 120;
	}

	private void followPath() {
		if(path == null) {
			return;
		}

		if(path.size() == 0) {
			setStopped();
			path = null;
			return;
		}

		Point targetTile = path.get(0);

		if(targetTile != null) {
			Point targetPos = getTargetPos(targetTile);

			if(atTarget(targetPos)) {
				if(path != null) {
					path.remove(0);
					targetTile = path.size() > 0 ? path.get(0) : null;
					targetPos = getTargetPos(targetTile);

					if(path.size() == 0) {
						timer = 0;
					}
				}
			}

			if(targetPos != null) {
				if(Math.abs(position.x - targetPos.x) >= walkSpeed) {
					if(position.x < targetPos.x) { setWalkingRight(); }
					else if(position.x > targetPos.x) { setWalkingLeft(); }
				}

				else {
					setStoppedHorizontal();
				}

				if(Math.abs(position.y - targetPos.y) >= walkSpeed) {
					if(position.y < targetPos.y) { setWalkingUp(); }
					else if(position.y > targetPos.y) { setWalkingDown(); }
				}

				else {
					setStoppedVertical();
				}
			}
		}
	}

	private Point getTargetPos(Point tile) {
		if(tile == null) { return null; }
		int tileSize = level.getTileMap().getTileSize();
		int targetX = tile.x * tileSize + (tileSize / 2);
		int targetY = tile.y * tileSize + (tileSize / 2);
		return new Point(targetX, targetY);
	}

	private boolean atTarget(Point targetPos) {
		if(targetPos == null) { return true; }
		boolean atTargetX = Math.abs(position.x - targetPos.x) < walkSpeed;
		boolean atTargetY = Math.abs(position.y - targetPos.y) < walkSpeed;
		return atTargetX && atTargetY;
	}

	protected void updateEntity() {
		if(timer > 0) { --timer; }
		Player targetPlayer = getNearestPlayer();

		if(timer == 0) {
			if(phase == 0) {
				if(targetPlayer != null) {
					timer = 60;
					phase = 1;
					path = level.getTileMap().findPath(position, targetPlayer.position, 38, false);
				}
			}

			else if(phase == 1) {
				if(targetPlayer != null) {
					bow.setSequence(1, true);
					phase = 0;

					double angleBetween = position.angleBetween(targetPlayer.position);

					switch(RandomUtils.randInt(2)) {
					case 0:
						level.spawn(new PurpleGemProjectile(level, position.x, position.y, angleBetween));
						level.spawn(new PurpleGemProjectile(level, position.x, position.y, angleBetween - Math.toRadians(45)));
						level.spawn(new PurpleGemProjectile(level, position.x, position.y, angleBetween + Math.toRadians(45)));
						timer = 45;
						break;
					case 1:
						level.spawn(new CoinProjectile(level, position.x, position.y, angleBetween, 4.5));
						level.spawn(new CoinProjectile(level, position.x, position.y, angleBetween + Math.toRadians(5), 4.0));
						level.spawn(new CoinProjectile(level, position.x, position.y, angleBetween + Math.toRadians(10), 3.5));
						level.spawn(new CoinProjectile(level, position.x, position.y, angleBetween - Math.toRadians(5), 4.0));
						level.spawn(new CoinProjectile(level, position.x, position.y, angleBetween - Math.toRadians(10), 3.5));
						timer = 20;
						break;
					case 2:
						for(int i = 0; i <= 4; i++) {
							level.spawn(new RedGemProjectile(level, position.x, position.y, angleBetween - Math.toRadians(30) + (Math.toRadians(60) / 4 * i), 3));
						}
						timer = 20;
						break;
					}
				}
			}
		}

		followPath();

		if(targetPlayer != null) {
			armRotation = position.copy().add(-3, 11).angleBetween(targetPlayer.getPosition());
		}

		if(armRotation < Math.toRadians(-135)) { facing = 2; }
		else if(armRotation < Math.toRadians(-90)) { facing = 0; }
		else if(armRotation < Math.toRadians(0)) { facing = 1; }
		else if(armRotation < Math.toRadians(45)) { facing = 3; }
		else if(armRotation < Math.toRadians(90)) { facing = 5; }
		else if(armRotation < Math.toRadians(135)) { facing = 4; }
		else if(armRotation < Math.toRadians(225)) { facing = 2; }
		else { facing = 1; }
	}

	public void render(LayerRenderer renderer) {
		if(facing == 0 || facing == 1 || facing == 3) {
			renderBody(renderer);
			renderArm(renderer);
		}

		else {
			renderArm(renderer);
			renderBody(renderer);
		}
	}

	private void renderBody(LayerRenderer renderer) {
		boolean walking = isWalking();
		if(!animation.isPaused() && !walking) { animation.resetFrame(); }
		animation.setPaused(!walking);
		animation.setSequence(facing, false);

		renderer.getSpriteBatch().draw(animation.getFrame(), (float)(position.x - (animation.getFrameWidth() / 2)), (float)(position.y - (animation.getFrameHeight() / 2)));
		animation.updateTimer();
	}

	private void renderArm(LayerRenderer renderer) {
		float renderRotation = (float)Math.toDegrees(armRotation) - 90;
		Vector armPos = position.copy().add(-3, 11);
		renderer.getSpriteBatch().draw(bow.getFrame(), (float)armPos.x - (bow.getFrameWidth() / 2), (float)armPos.y - (bow.getFrameHeight() / 2), 9, 1, bow.getFrameWidth(), bow.getFrameHeight(), 1, 1, renderRotation);
		bow.updateTimer();

		if(bow.getSequence() == 1 && bow.isDone()) {
			bow.setSequence(0, true);
		}
	}

	protected void onDeath() {
		SoundEffects.instance.play("boom", 1, 1, 0);
		for(int i = 0; i <= 10; i++) {
			double angle = Math.PI * 2 / 10 * i;
			level.spawn(new RedGemProjectile(level, position.x, position.y, angle, 2));
		}

		int x = level.getTileMap().getMapCoordinate(position.x), y = level.getTileMap().getMapCoordinate(position.y);
		level.getTileMap().setTile(x, y, (byte)-9);
		level.spawn(new Trapdoor(level, level.getTileMap().getWorldCoordinate(x), level.getTileMap().getWorldCoordinate(y)));

		ArrayList<Enemy> enemies = level.getEnemies();
		for(Enemy enemy : enemies) {
			if(enemy != this) {
				enemy.destroy();
				enemy.onDeath();
			}
		}
	}
}
