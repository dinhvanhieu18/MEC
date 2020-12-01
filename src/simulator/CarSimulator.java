package simulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class CarSimulator {
	
	public int id;
	public double startTime;
	public int numMessage = 0;
	public double preReceiveFromGNB = 0;
	public double preReceiveFromRSU = 0;
	// Queue contains message receive from RSU after process
	public List<Message> rSURecQueue = new ArrayList<Message>();
	// Queue contains message receive from GNB after process
	public List<Message> gNBRecQueue = new ArrayList<Message>();
	
	public CarSimulator(int id, double startTime) {
		this.id = id;
		this.startTime = startTime;
	}
	
	public void working(double currentTime) {
		if (getPosition(currentTime) > Simulator.roadLength) {
			return;
		}
		if (this.numMessage >= Simulator.listTimeMessages.size()) {
			return;
		}
		
		double curTime = Simulator.listTimeMessages.get(numMessage);
		while (true) {
			double sendTime = this.startTime + curTime;
			if (sendTime > currentTime + Simulator.cycleTime) {
				return;
			}
			Message mes = new Message();
			mes.indexCar = this.id;
			mes.firstSentTime = sendTime;
			mes.sendTime.add(sendTime);
			
			Random random = new Random();
			double rand = random.nextDouble();
			if (rand < Simulator.pL) {
				sendMessageToGNB(mes);
			}
			else {
				sendMessageToRSU(mes);
			}
			
			this.numMessage ++;
			if (this.numMessage >= Simulator.listTimeMessages.size()) {
				return;
			}
			curTime = Simulator.listTimeMessages.get(numMessage);
		}
	}

	public void sendMessageToRSU(Message mes) {
		double minDistance = 1000000;
		int RSUId = 0;
		for (RsuSimulator rsu : Simulator.rSUList) {
			double distance = calculateDistance(mes.firstSentTime, rsu.id);
			if (distance < minDistance) {
				minDistance = distance;
				RSUId = rsu.id;
			}
		}
		mes.type = "TYPE_1";
		Simulator.rSUList.get(RSUId).inputList.add(mes);
	}

	public void sendMessageToGNB(Message mes) {
		mes.type = "TYPE_2";
		Simulator.gNB.inputFromCar.add(mes);
	}

	public double getPosition(double currentTime) {
		return Simulator.carSpeed * (currentTime - this.startTime);
	}
	
	public double calculateDistance(double currentTime, int RSUId) {
		RsuSimulator rsu = Simulator.rSUList.get(RSUId);
		double position = getPosition(currentTime);
		return Math.sqrt(Math.pow(position - rsu.xcord, 2) + Math.pow(rsu.ycord, 2) + Math.pow(rsu.zcord, 2));
	}

	public void receiveMessage() {
		Collections.sort(this.rSURecQueue);
		simulateTranferTimeFromRSU();
		Collections.sort(this.gNBRecQueue);
		simulateTranferTimeFromGNB();
		Simulator.output.addAll(this.rSURecQueue);
		Simulator.output.addAll(this.gNBRecQueue);
		this.rSURecQueue.clear();
		this.gNBRecQueue.clear();
	}
	
	public void simulateTranferTimeFromGNB() {
		for (Message mes : this.gNBRecQueue) {
			double tranferTime = Simulator.getNext(1.0/Simulator.gNBCarMeanTranfer);
			double selectedTime = Math.max(this.preReceiveFromGNB, mes.sendTime.get(mes.sendTime.size()-1));
			double receiveTime = tranferTime + selectedTime;
			mes.receiverTime.add(receiveTime);
			this.preReceiveFromGNB = receiveTime;
		}
	}
	
	public void simulateTranferTimeFromRSU() {
		for (Message mes : this.rSURecQueue) {
			double tranferTime = Simulator.getNext(1.0/Simulator.rSUCarMeanTranfer);
			double selectedTime = Math.max(this.preReceiveFromRSU, mes.sendTime.get(mes.sendTime.size()-1));
			double receiveTime = tranferTime + selectedTime;
			mes.receiverTime.add(receiveTime);
			this.preReceiveFromRSU = receiveTime;
			double distance = calculateDistance(receiveTime, mes.indexRSU);
			if (distance > Simulator.rSUCoverRadius) {
				mes.isDropt = true;
			}
		}
	}
}
