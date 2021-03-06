package com.game.entities;

import com.game.audio.SoundEffects;
import com.game.graphics.Animation;
import com.game.graphics.LayerRenderer;
import com.game.graphics.Sequence;
import com.game.graphics.Textures;
import com.game.level.Level;
import com.game.utils.RandomUtils;
import com.game.vector.Vector;

public class DemonCoin extends Enemy {

	private Animation animation;
	private boolean invisible;
	private int timer;
	private int phase;

	public DemonCoin(Level level, double x, double y) {
		super(level, x, y, 30, 30, 0.25, 9);
		animation = new Animation(Textures.instance.getTexture("demon_coin"), Sequence.formatSequences(
				new Sequence(32, 32, 0, 1),
				new Sequence(32, 32, 0, 1),
				new Sequence(32, 32, 2, 6).setNoLoop(),
				new Sequence(32, 32, 2, 6).setNoLoop()));
		timer = 120;
	}
	
	@Override
	public boolean invulnerable() {
		return invisible;
	}

	protected void updateEntity() {
		if(timer > 0) { --timer; }
		Player targetPlayer = getNearestPlayer();
		
		if(timer == 0) {
			if(phase == 0) {
				if(targetPlayer != null) {
					phase = 1;
					timer = 120;
					for(int i = 0; i < 12; i++) {
						level.spawn(new CoinProjectile(level, position.x, position.y, position.angleBetween(targetPlayer.position) - RandomUtils.randDouble(Math.toRadians(30)) + Math.toRadians(15), RandomUtils.randDouble(1, 3)));
					}
				}
			}

			else if(phase == 1) {
				phase = 3;
				timer = 11;
			}

			else if(phase == 3 && !invisible) {
				invisible = true;
				timer = 60;
			}
			
			else if(phase == 3 && invisible) {
				Vector newPos = position.copy().add(RandomUtils.randVector(16, 160));
				int x = level.getTileMap().getMapCoordinate(newPos.x);
				int y = level.getTileMap().getMapCoordinate(newPos.y);
				if(!level.getTileMap().isTileCollidable(x, y) && level.getTileMap().isInMapBounds(x, y)) {
					phase = 2;
					invisible = false;
					timer = 11;
					position.set(newPos.x, newPos.y);
				}
			}
			
			else if(phase == 2) {
				phase = 0;
				timer = 30;
			}
		}
	}

	public void render(LayerRenderer renderer) {
		animation.setSequence(phase, true);

		if(!invisible) {
			renderer.getSpriteBatch().draw(animation.getFrame(), (float)(position.x - (animation.getFrameWidth() / 2)), (float)(position.y - (animation.getFrameHeight() / 2)));
			animation.updateTimer();
		}
	}
	
	protected void onDeath() {
		SoundEffects.instance.play("boom", 1, 1, 0);
		for(int i = 0; i < 20; i++) {
			Coin coin = RandomUtils.randDouble() < 0.05 ? new RedGemProjectile(level, position.x, position.y, RandomUtils.randAngle(), RandomUtils.randDouble(0.5, 2.5)) : new CoinProjectile(level, position.x, position.y, RandomUtils.randAngle(), RandomUtils.randDouble(0.5, 2.5));
			level.spawn(coin);
		}
		
		if(RandomUtils.randDouble() < 0.1) {
			level.spawn(new Tax(level, position.x, position.y));
		}
	}
}
