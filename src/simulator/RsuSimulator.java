package simulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class RsuSimulator {
	public int id;
	public double xcord;
	public double ycord;
	public double zcord;
	public double preReceive = 0;
	public double preProcess = 0;
	public List<Message> inputList = new ArrayList<Message>();
	public List<Message> waitList = new ArrayList<Message>();

	public RsuSimulator(int id, double xcord, double ycord, double zcord) {
		this.id = id;
		this.xcord = xcord;
		this.ycord = ycord;
		this.zcord = zcord;
	}

	public void working(double currentTime) {
		Collections.sort(this.inputList);
		simulateTranferTime();
		this.inputList.addAll(this.waitList);
		this.waitList.clear();
		Collections.sort(this.inputList, new Comparator<Message>() {
			public int compare(Message o1, Message o2) {
				if (o1.receiverTime.get(o1.receiverTime.size()-1) > o2.receiverTime.get(o2.receiverTime.size()-1)) return 1;
				if (o1.receiverTime.get(o1.receiverTime.size()-1) < o2.receiverTime.get(o2.receiverTime.size()-1)) return -1;
				return 0;
			}
		});
		simulateProcessTime(currentTime);
		this.inputList.clear();
	}

	public void simulateProcessTime(double currentTime) {
		for (Message mes : this.inputList) {
			if (mes.receiverTime.get(mes.receiverTime.size()-1) > currentTime + Simulator.cycleTime) {
				this.waitList.add(mes);
				continue;
			}
			Random random = new Random();
			double rand = random.nextDouble();
			double selectedTime = Math.max(this.preProcess, mes.receiverTime.get(mes.receiverTime.size()-1));
			if (rand < Simulator.pR) {
				sendMessageToGNB(mes, selectedTime);
				this.preProcess = selectedTime;
			}
			else {
				// Process and return to car
				double processTime = Simulator.getNext(Simulator.rSUProcessPerSecond);
				double processedTime = selectedTime + processTime;
				sendMessageToCar(mes, processedTime);
				this.preProcess = processedTime;
			}
		}
	}

	public void sendMessageToCar(Message mes, double processedTime) {
		mes.sendTime.add(processedTime);
		mes.indexRSU = this.id;
		Simulator.carList.get(mes.indexCar).rSURecQueue.add(mes);
	}

	public void sendMessageToGNB(Message mes, double selectedTime) {
		mes.sendTime.add(selectedTime);
		mes.type = "TYPE_3";
		Simulator.gNB.inputFromRSU.add(mes);
	}

	public void simulateTranferTime() {
		for (Message mes : this.inputList) {
			double tranferTime = Simulator.getNext(1.0/Simulator.carRSUMeanTranfer);
			double selectedTime = Math.max(this.preReceive, mes.sendTime.get(mes.sendTime.size()-1));
			double receiveTime = tranferTime + selectedTime;
			mes.receiverTime.add(receiveTime);
			this.preReceive = receiveTime;
		}
	}

}
