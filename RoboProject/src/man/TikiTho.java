package robo;

import robocode.*;
import robocode.util.Utils;

public class TikiTho extends AdvancedRobot {
	boolean hitWall = false; 						//queremos estar a andar numa parede
	double direction = 1;							//para permitir inverter a direcao
	double enemyEnergy;								//energia do inimigo
	
	
	boolean predictLocation=false;					//vai definir a estrategia que o robot ira usar
	double directShots=1,directShotsHit=1;			//tiros para a posicao que vimos o inimigo
	double predictiveShots=1,predictiveShotsHit=1;	//tiros para a posicao para onde o inimigo se encontra a ir
	
	public void run() {
		double width  = getBattleFieldWidth();		
		double height = getBattleFieldHeight();		
		
		setAdjustRadarForRobotTurn(true);			//desassociar o movimento do radar dos movimentos da arma e do corpo
		setAdjustRadarForGunTurn(true);				
		
		while(true) {
		if(!hitWall) {
			turnLeft(getHeading() % 90);			//vamos virar nos perpendicularmente para uma parede e
			ahead(Math.max(height,width)); 			//vamos usar uma parede como o nosso espaco de movimento
		}
			turnRadarLeft(Double.MAX_VALUE);		//roda muito o radar ate encontrar o outro robo
		}
	}
	 
	public void onScannedRobot(ScannedRobotEvent e) {
		double absBearing = e.getBearingRadians() + getHeadingRadians();		      	//angulo do inimigo relativo a nossa posicao
		double latVel = e.getVelocity() * Math.sin(e.getHeadingRadians() - absBearing); //velocidade lateral do inimigo 
		double radarTurn = absBearing - getRadarHeadingRadians(); 						//graus a virar o radar
		
		if(enemyEnergy > (enemyEnergy = e.getEnergy()))							 		//vamos usar a energia do inimigo como referencia para saber se
			direction*=-1;																//disparou e mudar de direção 180 graus a cada tiro
		if(e.getDistance()<75)															//caso a distancia seja muito curta , tiro forte
			setFire(3);
																						
		double bulletPower=Math.min(3, e.getEnergy()/4);								//queremos minimizar a energia que gastamos
																						//o poder da bala diminui com a energia do inimigo
		
		double predictiveRating = predictiveShotsHit/predictiveShots;					//percentagem de acerto de tiro por estrategia escolhida
		double directRating = directShotsHit/directShots;								//usa sempre a estratégia que tem melhor probabilidade de acerto
		
		if(predictiveRating > directRating)			//usa tiro em antecipacao ao movimento do inimigo
			predictLocation=true;					//senao dispara directamente para a posicao em que se encontra o inimigo
		else										//incrementa o contador da estrategia que esta a ser usada actualmente
			predictLocation=false;					
		if(predictLocation) {
			setTurnGunRightRadians(Utils.normalRelativeAngle(absBearing-getGunHeadingRadians()+Math.asin(latVel/(20-3*bulletPower))));
			predictiveShots++; 						
		}else {										
			setTurnGunRightRadians(Utils.normalRelativeAngle(absBearing-getGunHeadingRadians()));
			directShots++;      					
		}
		setFire(bulletPower);
		setAhead(100*direction);					
		setTurnRadarRightRadians(Utils.normalRelativeAngle(radarTurn)*2); 	//mantem o radar no inimigo		 
	}
	
	public void onHitWall(HitWallEvent e) { 		//muda a direcao quando bate na parede
		if(hitWall)									
			direction*=-1;							
		else 
			turnLeft(90);							
		hitWall=true;		
	}
	
	public void onHitRobot(HitRobotEvent e) {		//queremos evitar dano desnecesario, mudar de direcao
		direction*=-1;								
	}												
	
	
	public void onBulletHit(BulletHitEvent e) {		//se uma bala atingir o inimigo , incrementa as variaveis que escolhem a estratégia
		if(predictLocation)							//isto vai permitir mudar a estrategia durante a batalha
			predictiveShotsHit++;
		else
			directShotsHit++;
	}												
}
 
 
 
 