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
	boolean hitWall = false; 						//have we hit a wall yet
	double direction = 1;							//-1 to go back,+1 go ahead
	double enemyEnergy;								
	
	boolean predictLocation=false;					//type of shoots the robot will use
	double directShots=1,directShotsHit=1;			//how many did the robot shoot and hit
	double predictiveShots=1,predictiveShotsHit=1;	
	
	public void run() {
		setAdjustRadarForGunTurn(true); //free the radar from the gun	
		
		double width  = getBattleFieldWidth();		
		double height = getBattleFieldHeight();		
					
		while(true) {
		if(!hitWall) {
			turnLeft(getHeading() % 90);			//turn to a wall
			ahead(Math.max(height,width)); 			//and go to that wall
		}
			turnRadarLeft(Double.MAX_VALUE);		//spin radar until finding another robot
		}
	}
	 
	
	public void onScannedRobot(ScannedRobotEvent e) {
		double absBearing = e.getBearingRadians() + getHeadingRadians();		      	//enemy's angle relative to our position
		double latVel = e.getVelocity() * Math.sin(e.getHeadingRadians() - absBearing); //lateral velocity of the enemy
		double radarTurn = absBearing - getRadarHeadingRadians(); 						//angle to turn the radar
		
		if(enemyEnergy > (enemyEnergy = e.getEnergy()))							 		//if the enemy's energy is lower than before
			direction*=-1;																//he shoot a bullet,change direction
		
		if(e.getDistance()<75)															//if he is too close shoot a powerful bullet
			setFire(3);
																						
		double bulletPower=Math.min(2, enemyEnergy/4);									//lets minimize our energy
																						//bullet power changes with enemy's energy
		
		double predictiveRating = predictiveShotsHit/predictiveShots;					//calculate the rating of the two strategies
		double directRating = directShotsHit/directShots;								
		
		if(predictiveRating > directRating)			//use the highest rating
			predictLocation=true;					//shoot to the predicted location
		else										
			predictLocation=false;					//shoot directly at the enemy
		if(predictLocation) {//calculate how much to turn the gun
			setTurnGunRightRadians(Utils.normalRelativeAngle(absBearing-getGunHeadingRadians()+Math.asin(latVel/(20-3*bulletPower))));
			predictiveShots++; 						
		}else {
			setTurnGunRightRadians(Utils.normalRelativeAngle(absBearing-getGunHeadingRadians()));
			directShots++;      					
		}
		setFire(bulletPower);//fire!
		setAhead(100*direction);//don't stop moving					
		setTurnRadarRightRadians(Utils.normalRelativeAngle(radarTurn)*2); 	//keeps the radar on target		 
	}
	
	
	
	public void onHitWall(HitWallEvent e) { //if we hit a wall 
		if(hitWall)//change direction									
			direction*=-1;							
		else //if its the first time we hit a wall , just turn left
			turnLeft(90);							
		hitWall=true;		
	}
	
	public void onHitRobot(HitRobotEvent e) {		//if we hit another robot, go the other way
		direction*=-1;								
	}												
	
	public void onBulletHit(BulletHitEvent e) {		//if we hit the enemy with a bullet
		if(predictLocation)							//increment the "hit" variables
			predictiveShotsHit++;
		else
			directShotsHit++;
	}												
}
 
 
 
 