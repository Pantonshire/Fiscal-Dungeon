package com.game.entities.projectiles;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.game.entities.Enemy;
import com.game.entities.Entity;
import com.game.entities.Hitbox;
import com.game.entities.SparkParticle;
import com.game.graphics.LayerRenderer;
import com.game.graphics.Textures;
import com.game.level.Level;
import com.game.light.LevelLightManager;
import com.game.light.LightSource;
import com.game.utils.RandomUtils;
import com.game.utils.RayCaster;
import com.game.vector.Vector;

public class DartTrap extends Entity implements LightSource {

	private Texture texture;
	private Hitbox hitbox;
	private double angle;
	private double launchSpeed, chaseSpeed;
	private double deceleration;
	private double range;
	private int despawnTime;
	private boolean chasing;
	private int time;
	private Enemy targetEnemy;

	public DartTrap(Level level, double x, double y, double angle, double launchSpeed, double chaseSpeed, double deceleration, int despawnTime, double range) {
		super(level, x, y);
		hitbox = new Hitbox(this, 3, 3);
		texture = Textures.instance.getTexture("dart_trap");
		velocity.setAngle(angle, launchSpeed);
		this.launchSpeed = launchSpeed;
		this.chaseSpeed = chaseSpeed;
		this.deceleration = deceleration;
		this.despawnTime = despawnTime;
		this.range = range;
		this.angle = angle;
		level.getLightManager().addDynamicLight(this);
	}

	public void updateTileCollisions() {
		boolean touchedCollidable = false;

		if(velocity.x != 0) {
			if(hitbox.collidedHorizontal(level.getTileMap())) {
				velocity.x = velocity.y = 0;
				touchedCollidable = true;
			}
		}

		if(velocity.y != 0) {
			if(hitbox.collidedVertical(level.getTileMap())) {
				velocity.x = velocity.y = 0;
				touchedCollidable = true;
			}
		}

		if(touchedCollidable && chasing) {
			destroy();
		}
	}

	protected void updateEntity() {
		if(chasing && ++time > 540 / chaseSpeed) {
			destroy();
		}

		if(!shouldRemove()) {
			if(chasing || time % 3 == 0) {
				level.spawn(new SparkParticle("dart_trap_particle", level, position.x, position.y, RandomUtils.randAngle(), RandomUtils.randDouble(0.1, 0.5), 30));
			}
			
			ArrayList<Enemy> enemies = level.getEnemies();
			if(!chasing) {
				if((launchSpeed -= deceleration) < 0) {
					launchSpeed = 0;
					velocity.set(0, 0);
				}

				else if(launchSpeed > 0) {
					velocity.setAngle(angle, launchSpeed);
				}

				if(velocity.isZero()) {
					for(Enemy enemy : enemies) {
						if(!enemy.invulnerable() && position.distBetween(enemy.getPosition()) < range && RayCaster.canSee(level, position, enemy.getPosition(), -1)) {
							chasing = true;
							time = 0;
							angle = position.angleBetween(enemy.getPosition());
							velocity.setAngle(angle, chaseSpeed);
							targetEnemy = enemy;
							break;
						}
					}
					
					if(!chasing && ++time > despawnTime) {
						destroy();
					}
				}
			}

			else if(chasing) {
				if(targetEnemy != null && !targetEnemy.shouldRemove() && !targetEnemy.invulnerable()) {
					angle = position.angleBetween(targetEnemy.getPosition());
					velocity.setAngle(angle, chaseSpeed);
				}
				
				for(Enemy enemy : enemies) {
					if(hitbox.intersectsHitbox(enemy.getHitbox())) {
						if(enemy.damage(3)) {
							destroy();
							break;
						}
					}
				}
			}
			
			updateTileCollisions();
		}
	}

	public void render(LayerRenderer renderer) {
		int width = texture.getWidth(), height = texture.getHeight();
		float renderAngle = (float)Math.toDegrees(angle) - 90;
		renderer.getSpriteBatch().draw(texture, (float)position.x - width / 2, (float)position.y - height / 2, width / 2, height / 2, width, height, 1, 1, renderAngle, 0, 0, width, height, false, false);
	}
	
	public Vector lightPosition() {
		return position;
	}
	
	public int numLightRays() {
		return LevelLightManager.DEFAULT_RADIAL_SOURCE;
	}
	
	public float lightStrength() {
		return 70;
	}
	
	public Color lightColor() {
		return new Color(0.6F, 0.1F, 0.95F, 0.3F);
	}
}
