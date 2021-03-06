package ch.epfl.moocprog;
import static ch.epfl.moocprog.random.UniformDistribution.getValue;
import static ch.epfl.moocprog.app.Context.getConfig;
import static ch.epfl.moocprog.config.Config.ANIMAL_LIFESPAN_DECREASE_FACTOR;
import static ch.epfl.moocprog.config.Config.ANIMAL_NEXT_ROTATION_DELAY;
import static ch.epfl.moocprog.utils.Vec2d.fromAngle;
import static java.lang.Math.toRadians;
import static ch.epfl.moocprog.utils.Utils.*;
import ch.epfl.moocprog.utils.Time;
import ch.epfl.moocprog.utils.Vec2d;

public abstract class Animal extends Positionable {
	
	private final Time nextRotationDelay = getConfig().getTime(ANIMAL_NEXT_ROTATION_DELAY);
	
	private double direction;
	
	private int hitpoints;
	
	private Time lifespan;
	
	private Time rotationDelay;
	
	public Animal(ToricPosition pos) {
		super(pos);
	}
	
	public Animal(ToricPosition pos, int hitpoints, Time lifespan) {
		super(pos);
		this.hitpoints=hitpoints;
		this.lifespan=lifespan;
		this.direction = getValue(0, 2*Math.PI);
		this.rotationDelay = Time.ZERO;
	}
	
	public final double getDirection() {
		return direction;
	}
	
	public final void setDirection(double angle) {
		this.direction = angle;
	}
	
	public final boolean isDead() {
		boolean state = false;
		if((hitpoints <= 0.0) || !(lifespan.isPositive())) state = true;
		return state;
	}
	
	public final int getHitpoints() {
		return hitpoints;
	}
	
	public final Time getLifespan() {
		return lifespan;
	}
	
	final public void update(AnimalEnvironmentView env, Time dt) {	
		lifespan.minus(dt.times(getConfig().getDouble(ANIMAL_LIFESPAN_DECREASE_FACTOR)));
		this.specificBehaviorDispatch(env, dt);
	}
	
	protected final void move(AnimalEnvironmentView env, Time dt) {
		
		this.rotationDelay = this.rotationDelay.plus(dt);
		if (!isDead()) {
			while(this.rotationDelay.compareTo(nextRotationDelay) >= 0.0) {
				rotate(env);
				this.rotationDelay = this.rotationDelay.minus(nextRotationDelay);
			}
		}
		//Angle de direction de l'animal
		Vec2d v = fromAngle(direction);
		//Distance parcourue par l'animal pendant dt
		double distance = dt.toSeconds()*this.getSpeed();
		//Ajout du deplacement a la position courante de l'animal
		this.setPosition(this.getPosition().add(v.scalarProduct(distance)));
		afterMoveDispatch(env, dt);
	}
	
	private void rotate(AnimalEnvironmentView env) {
		this.direction += pickValue(this.computeRotationProbsDispatch(env).getAngles(),this.computeRotationProbsDispatch(env).getProbabilities());
	}
	
	public abstract void accept(AnimalVisitor visitor, RenderingMedia s);
	
	public abstract double getSpeed();
	
	protected abstract void specificBehaviorDispatch(AnimalEnvironmentView env, Time dt);
	
	protected final RotationProbability computeDefaultRotationProbs() {
		double[] angles = new double[] {-180.00, -100.00, -55.00, -25.00, -10.00, 0.00, 10.00, 25.00, 55.00, 100.00, 180.00};
		for (int i = 0; i<angles.length;i++) {
			angles[i] = toRadians(angles[i]);
		}
		double[] probabilities = new double[] {0.0000, 0.0000, 0.0005, 0.0010, 0.0050,0.9870,0.0050, 0.0010, 0.0005, 0.0000, 0.0000};
		return new RotationProbability(angles,probabilities);
	}
	
	protected abstract RotationProbability computeRotationProbsDispatch(AnimalEnvironmentView env);
	
	protected abstract void afterMoveDispatch(AnimalEnvironmentView env, Time dt);
	
	public void makeUturn() {
		double angle = this.getDirection();
		if(angle + Math.PI > 2*Math.PI) {
			this.setDirection(this.getDirection()-2*Math.PI);
		}
		else {
			this.setDirection(this.getDirection()+Math.PI);
		}
	}


	@Override
	public String toString() {
		return super.toString()+"\n"+String.format("Speed : %.1f", this.getSpeed())+"\n"+String.format("Hitpoints : %d", hitpoints)+"\n"+String.format("LifeSpan : %.1f", lifespan.toSeconds());
	}
	
	

}
