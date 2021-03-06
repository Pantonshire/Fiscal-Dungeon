package com.game.entities;

import com.game.audio.SoundEffects;
import com.game.graphics.Animation;
import com.game.graphics.LayerRenderer;
import com.game.graphics.Sequence;
import com.game.graphics.Textures;
import com.game.level.Level;
import com.game.utils.RandomUtils;

public class BigRedGem extends Enemy {

	private int ATTACK_RATE = 5;

	private Animation animation;
	private int attackTimer;
	private double angle;

	public BigRedGem(Level level, double x, double y) {
		super(level, x, y, 30, 30, 0.25, 10);
		animation = new Animation(Textures.instance.getTexture("big_red_gem"), Sequence.formatSequences(new Sequence(32, 32, 6, 5)));
	}

	protected void updateEntity() {
		if(attackTimer > 0) { --attackTimer; }
		Player targetPlayer = getNearestPlayer();

		if(targetPlayer != null) {
			if(attackTimer == 0) {
				attackTimer = ATTACK_RATE;
				level.spawn(new CoinProjectile(level, position.x, position.y, angle, 1.5));
				level.spawn(new CoinProjectile(level, position.x, position.y, angle + Math.PI, 1.5));
				angle += Math.toRadians(16);

				if(angle > Math.PI) {
					angle -= Math.PI * 2;
				}
			}
		}
	}

	public void render(LayerRenderer renderer) {
		renderer.getSpriteBatch().draw(animation.getFrame(), (float)(position.x - (animation.getFrameWidth() / 2)), (float)(position.y - (animation.getFrameHeight() / 2)));
		animation.updateTimer();
	}

	protected void onDeath() {
		SoundEffects.instance.play("boom", 1, 1, 0);
		for(int i = 0; i < 5; i++) {
			Coin projectile = new CoinProjectile(level, position.x, position.y, 2 * Math.PI / 5 * i, 4);
			level.spawn(projectile);
		}

		if(RandomUtils.randDouble() < 0.1) {
			level.spawn(new Tax(level, position.x, position.y));
		}
	}
}
