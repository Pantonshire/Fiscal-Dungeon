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

public class TreasureChest extends Enemy {

	private Animation animation;
	private ArrayList<Point> path;
	private int timer;
	private int phase;

	public TreasureChest(Level level, double x, double y) {
		super(level, x, y, 30, 30, 2, 25);
		animation = new Animation(Textures.instance.getTexture("treasure_chest"), Sequence.formatSequences(
				new Sequence(32, 32, 0, 1),
				new Sequence(32, 32, 0, 1),
				new Sequence(32, 32, 4, 2)));
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
				timer = 30;
				phase = 1;
			}

			else if(phase == 1) {
				if(targetPlayer != null) {
					phase = 2;
					timer = 90;

					double angleBetween = position.angleBetween(targetPlayer.position);

					switch(RandomUtils.randInt(4)) {
					case 0:
						for(int i = 0; i < 10; i++) {
							level.spawn(new RedGemProjectile(level, position.x, position.y, angleBetween + RandomUtils.randDouble(Math.PI / 4) - Math.PI / 8, RandomUtils.randDouble(2.5, 4)));
						}
						break;
					case 1:
						for(int i = 0; i <= 5; i++) {
							level.spawn(new CoinProjectile(level, position.x, position.y, angleBetween - Math.toRadians(60) + (Math.toRadians(120) / 5 * i), 1.5));
							level.spawn(new CoinProjectile(level, position.x, position.y, angleBetween - Math.toRadians(60) + (Math.toRadians(120) / 5 * i), 1.25));
							level.spawn(new CoinProjectile(level, position.x, position.y, angleBetween - Math.toRadians(60) + (Math.toRadians(120) / 5 * i), 1.0));
							level.spawn(new CoinProjectile(level, position.x, position.y, angleBetween - Math.toRadians(60) + (Math.toRadians(120) / 5 * i), 0.75));
							level.spawn(new CoinProjectile(level, position.x, position.y, angleBetween - Math.toRadians(60) + (Math.toRadians(120) / 5 * i), 0.5));
						}
						break;
					case 2:
						for(int i = 0; i < 40; i++) {
							level.spawn(new CoinProjectile(level, position.x, position.y, angleBetween - RandomUtils.randDouble(Math.toRadians(40)) + Math.toRadians(20), RandomUtils.randDouble(2, 4)));
						}
						break;
					case 3:
						for(int i = 0; i <= 5; i++) {
							level.spawn(new PurpleGemProjectile(level, position.x, position.y, angleBetween - Math.toRadians(20) + (Math.toRadians(40) / 5 * i)));
						}
						break;
					case 4:
						for(int i = 0; i <= 20; i++) {
							level.spawn(new CoinProjectile(level, position.x, position.y, angleBetween + (Math.PI * 2 / 20 * i), 1.5));
							level.spawn(new CoinProjectile(level, position.x, position.y, angleBetween + (Math.PI * 2 / 20 * i), 1.25));
							level.spawn(new CoinProjectile(level, position.x, position.y, angleBetween + (Math.PI * 2 / 20 * i), 1.0));
						}
						break;
					}
				}

				else {
					timer = 30;
					phase = 0;
				}
			}

			else if(phase == 2) {
				timer = 30;
				phase = 3;
			}

			else if(phase == 3) {
				if(targetPlayer != null) {
					timer = 90;
					phase = 4;
					path = level.getTileMap().findPath(position, targetPlayer.position, 19, false);
				}
			}

			else if(phase == 4) {
				path = null;
				setStopped();
				timer = 30;
				phase = 0;
			}
		}

		if(phase == 4) {
			followPath();
		}
	}

	public void render(LayerRenderer renderer) {
		if(phase == 4 && isWalking()) {
			animation.setSequence(2, true);
		}

		else if(phase == 2) {
			animation.setSequence(1, true);
		}

		else {
			animation.setSequence(0, true);
		}

		renderer.getSpriteBatch().draw(animation.getFrame(), (float)(position.x - (animation.getFrameWidth() / 2)), (float)(position.y - (animation.getFrameHeight() / 2)));
		animation.updateTimer();
	}

	protected void onDeath() {
		SoundEffects.instance.play("boom", 1, 1, 0);
		for(int i = 0; i <= 15; i++) {
			double angle = Math.PI * 2 / 15 * i;
			level.spawn(new RedGemProjectile(level, position.x, position.y, angle, 1.25));
			level.spawn(new CoinProjectile(level, position.x, position.y, angle, 1));
		}

		if(RandomUtils.randDouble() < 0.15) {
			level.spawn(new Tax(level, position.x, position.y));
		}
	}
}
