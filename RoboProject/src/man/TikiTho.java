package man;

import robocode.*;
import robocode.util.Utils;

public class TikiTho extends AdvancedRobot {
	boolean hitWall = false; 						//para sabermos se ja se encontrou uma parede
	double direction = 1;							//altera a direção
	double enemyEnergy;								//energia do inimigo
	boolean predictLocation=false;					//
	double directShots=1,directShotsHit=1;			//tiros para a posição que vimos o inimigo
	double predictiveShots=1,predictiveShotsHit=1;	//tiros para a posição para onde o inimigo se encontra a ir
	public void run() {
		double width  = getBattleFieldWidth();		//tamanho do campo em pixeis
		double height = getBattleFieldHeight();		//comprimento e largura
		setAdjustRadarForRobotTurn(true);			//desassociar a rotaçao do radar com os movimentos do corpo do robot
		setAdjustRadarForGunTurn(true);				//desassociar a rotação do radar com a rotação da arma
		while(true) {
		if(!hitWall) {
			turnLeft(getHeading() % 90);			//vira-se para uma parede
			ahead(Math.max(height,width)); 			//se ainda nao bateu numa parede, tenta bater 
		}
			turnRadarLeft(Double.MAX_VALUE);		//roda muito o radar até encontrar o outro robo
		}
	}
	 
	public void onScannedRobot(ScannedRobotEvent e) {
		double absBearing = e.getBearingRadians() + getHeadingRadians();		      	//angulo do inimigo
		double latVel = e.getVelocity() * Math.sin(e.getHeadingRadians() - absBearing); //velocidade lateral do inimigo 
		double radarTurn = absBearing - getRadarHeadingRadians(); 						//graus a virar o radar
		if(enemyEnergy > (enemyEnergy = e.getEnergy()))							 		//se o inimigo perder energia, muda de direcao
			direction*=-1;
		if(e.getDistance()<75)//caso a distancia seja muito curta , tiro forte
			setFire(3);
		double bulletPower=Math.min(3, e.getEnergy()/4);
		//escolher a estrategia
		double predictiveRating = predictiveShotsHit/predictiveShots;
		double directRating = directShotsHit/directShots;
		if(predictiveRating > directRating)
			predictLocation=true;
		else
			predictLocation=false;
		//fim 
		//System.out.println("predictiveRating = "+predictiveRating+"\ndirectRating = "+directRating+"\npredictLocation = "+predictLocation+"\n\n");
		if(predictLocation) {
			setTurnGunRightRadians(Utils.normalRelativeAngle(absBearing-getGunHeadingRadians()+Math.asin(latVel/(20-3*bulletPower)))); //apontar a arma para a frente do inimigo 
			predictiveShots++;
		}else {
			setTurnGunRightRadians(Utils.normalRelativeAngle(absBearing-getGunHeadingRadians())); //apontar a arma para o inimigo
			directShots++;
		}
		setFire(bulletPower);
		setAhead(100*direction);
		setTurnRadarRightRadians(Utils.normalRelativeAngle(radarTurn)*2); //virar o radar para nao perder o inimigo de vista
		 
	}
	
	//se bater numa parede, muda a direcao
	public void onHitWall(HitWallEvent e) {
		if(hitWall)
			direction*=-1;
		else 
			turnLeft(90);		
		hitWall=true;		
	}
	
	//se bater noutro robot, muda a direcao 
	public void onHitRobot(HitRobotEvent e) {
		direction*=-1;
	}
	
	
	//se uma bala atingir o inimigo , incrementa as variaveis
	public void onBulletHit(BulletHitEvent e) {
		if(predictLocation)
			predictiveShotsHit++;//se a bala for do tipo que preve a trajectoria, incrementa
		else
			directShotsHit++;//se a bala for normal, incrementa
	}
}
 
 
 
 