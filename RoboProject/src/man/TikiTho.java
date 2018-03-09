/**
 * Robot that stays in a wall and tries to kill the enemy
 * by changing between predictive shooting and normal shooting
 * 
 * @author up201302914 up201308089
 * @version 1.2
 */

package man;

import robocode.*;
import robocode.util.Utils;

public class TikiTho extends AdvancedRobot {
	boolean hitWall = false; 						//have we hit a wall yet?
	double direction = 1;							//-1 to go back,+1 go ahead
	double enemyEnergy;								
	
	boolean predictLocation=false;					//type of shots the robot will use
	double directShots=1,directShotsHit=1;			//how many shots did the robot hit
	double predictiveShots=1,predictiveShotsHit=1;	
	
	public void run() {
		setAdjustRadarForGunTurn(true); 			//free the radar from the guns movement
		
		double width  = getBattleFieldWidth();		
		double height = getBattleFieldHeight();		
					
		while(true) {
		if(!hitWall) {
			turnLeft(getHeading() % 90);			//turn to a wall
			ahead(Math.max(height,width)); 			//and go to that wall
		}
			turnRadarLeft(Double.MAX_VALUE);		//spin radar until it finds the enemy
		}
	}
	
	/**
	 * Using some calculations, we switch between shooting directly at the enemy position
	 * and shooting at the predicted location associated with it's movement, using a rating system.
	 * Adding to this we set the robot to shift direction whenever the enemy shoots at us!
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		double absBearing = e.getBearingRadians() + getHeadingRadians();			//enemy's angle relative to our position
		double latVel = e.getVelocity()*Math.sin(e.getHeadingRadians()-absBearing); //lateral velocity of the enemy
		double radarTurn = absBearing - getRadarHeadingRadians(); 					//angle to turn the radar
		
		if(enemyEnergy > (enemyEnergy = e.getEnergy())) //the enemy shot at us
			direction*=-1;
		
		if(e.getDistance()<75)	//he is close fire a powerful bullet
			setFire(3);
																						
		double bulletPower=Math.min(2, enemyEnergy/4);	
		double predictiveRating = predictiveShotsHit/predictiveShots;
		double directRating = directShotsHit/directShots;
		
		if(predictiveRating > directRating)
			predictLocation=true;				
		else										
			predictLocation=false;				
		
		//calculate how much to turn the gun
		if(predictLocation) {	
			setTurnGunRightRadians(Utils.normalRelativeAngle(absBearing-getGunHeadingRadians()+Math.asin(latVel/(20-3*bulletPower))));
			predictiveShots++; 						
		}else {
			setTurnGunRightRadians(Utils.normalRelativeAngle(absBearing-getGunHeadingRadians()));
			directShots++;      					
		}
		setFire(bulletPower);
		setAhead(100*direction);		
		setTurnRadarRightRadians(Utils.normalRelativeAngle(radarTurn)*2); 	//keeps the radar on target		 
	}
	
	/**
	 * If we hit a wall,go back
	 * If it's the beginning of the game stick to the wall
	 */
	public void onHitWall(HitWallEvent e) {
		if(hitWall)								
			direction*=-1;							
		else
			turnLeft(90);							
		hitWall=true;		
	}
	
	/**
	 *If we hit another robot,change direction
	 */
	public void onHitRobot(HitRobotEvent e) {		
		direction*=-1;								
	}												
	
	/**
	 * If we hit the enemy with a bullet
	 * increment the hit variables
	 */
	public void onBulletHit(BulletHitEvent e) {	
		if(predictLocation)				
			predictiveShotsHit++;
		else
			directShotsHit++;
	}												
}
 
 
 
 