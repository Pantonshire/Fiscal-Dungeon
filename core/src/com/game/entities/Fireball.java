package com.game.entities;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.game.audio.SoundEffects;
import com.game.graphics.LayerRenderer;
import com.game.graphics.Textures;
import com.game.level.Level;
import com.game.light.LevelLightManager;
import com.game.light.LightSource;
import com.game.utils.RandomUtils;
import com.game.vector.Vector;

public class Fireball extends Entity implements LightSource {

	private Texture texture;
	private Hitbox hitbox;
	private int time;
	private int damageCooldown;
	private int bounces;

	public Fireball(Level world, double x, double y, double angle, double speed) {
		super(world, x, y);
		hitbox = new Hitbox(this, 3, 3);
		texture = Textures.instance.getTexture("fireball");
		velocity.setAngle(angle, speed);
		world.getLightManager().addDynamicLight(this);
	}

	@Override
	public void destroy() {
		super.destroy();
		if(isOnScreen(world.gameRenderer)) {
			SoundEffects.instance.play("fire_blast", 1, 1, 0);
		}
	}

	private void bounce() {
		if(++bounces > 8) {
			destroy();
		}
	}

	private void updateTileCollisions() {
		boolean touchedCollidable = false;

		if(velocity.x != 0) {
			if(hitbox.collidedHorizontal(world.getTileMap())) {
				velocity.x = -velocity.x;
				touchedCollidable = true;
			}
		}

		if(velocity.y != 0) {
			if(hitbox.collidedVertical(world.getTileMap())) {
				velocity.y = -velocity.y;
				touchedCollidable = true;
			}
		}

		if(touchedCollidable) {
			bounce();
		}
	}

	protected void updateEntity() {
		if(++time > 1800) { destroy(); }
		if(damageCooldown > 0) { --damageCooldown; }

		updateTileCollisions();

		if(!shouldRemove()) {
			world.spawn(new SparkParticle(RandomUtils.randBoolean() ? "fireball_particle_red" : "fireball_particle_yellow", world, position.x, position.y, RandomUtils.randAngle(), RandomUtils.randDouble(0.25, 1), 40));
			
			if(damageCooldown == 0) {
				ArrayList<Enemy> enemies = world.getEnemies();
				for(Enemy enemy : enemies) {
					if(hitbox.intersectsHitbox(enemy.hitbox)) {
						if(enemy.damage(3)) {
							velocity.x = -velocity.x;
							velocity.y = -velocity.y;
							damageCooldown = 10;
						}
					}
				}
			}
		}
	}

	public void render(LayerRenderer renderer) {
		int width = texture.getWidth(), height = texture.getHeight();
		renderer.getSpriteBatch().draw(texture, (float)position.x - width / 2, (float)position.y - height / 2);
	}
	
	public Vector lightPosition() {
		return position;
	}
	
	public int numLightRays() {
		return LevelLightManager.DEFAULT_RADIAL_SOURCE;
	}
	
	public float lightStrength() {
		return 120;
	}
	
	public Color lightColor() {
		return new Color(1.0F, 0.6F, 0.2F, 0.75F);
//		return new Color(1.0F, 1.0F, 1.0F, 0.75F);
	}
}