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

public class BigGem extends Enemy {

	private int PATH_FIND_UPDATE_RATE = 10;
	private int ATTACK_RATE = 240;

	private Animation animation;
	private ArrayList<Point> path;
	private int pathFindTimer;
	private int attackTimer;

	public BigGem(Level level, double x, double y) {
		super(level, x, y, 30, 30, 0.25, 10);
		animation = new Animation(Textures.instance.getTexture("big_gem"), Sequence.formatSequences(new Sequence(32, 32, 6, 5)));
		path = new ArrayList<Point>();
		attackTimer = ATTACK_RATE;
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
		if(attackTimer > 0) { --attackTimer; }
		Player targetPlayer = getNearestPlayer();
		
		if(targetPlayer != null) {
			if(pathFindTimer > 0) { --pathFindTimer; }
			if(pathFindTimer == 0 && atTarget(path != null && path.size() > 0 ? path.get(0) : null)) {
				path = level.getTileMap().findPath(position, targetPlayer.position, 10, false);
				pathFindTimer = PATH_FIND_UPDATE_RATE;
			}
			
			if(attackTimer == 0) {
				attackTimer = ATTACK_RATE;
				level.spawn(new PurpleGemProjectile(level, position.x, position.y, position.angleBetween(targetPlayer.position)));
			}
		}

		followPath();
	}

	public void render(LayerRenderer renderer) {
		renderer.getSpriteBatch().draw(animation.getFrame(), (float)(position.x - (animation.getFrameWidth() / 2)), (float)(position.y - (animation.getFrameHeight() / 2)));
		animation.updateTimer();
	}

	protected void onDeath() {
		SoundEffects.instance.play("boom", 1, 1, 0);
		for(int i = 0; i < 10; i++) {
			Coin coin = new CoinProjectile(level, position.x, position.y, RandomUtils.randAngle(), RandomUtils.randDouble(0.5, 2.0));
			level.spawn(coin);
		}

		if(RandomUtils.randDouble() < 0.1) {
			level.spawn(new Tax(level, position.x, position.y));
		}
	}
}
