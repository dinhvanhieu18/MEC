package simulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GnbSimulator {
	
	public List<Message> inputFromCar = new ArrayList<Message>();
	public List<Message> inputFromRSU = new ArrayList<Message>();
	public List<Message> inputList = new ArrayList<Message>();
	public List<Message> waitList = new ArrayList<Message>();
	public double preReceiveFromCar = 0;
	public double preReceiveFromRSU = 0;
	public double preProcess = 0;
	public void working(double currentTime) {
		Collections.sort(this.inputFromCar);
		simulateTranferTimeFromCar();
		Collections.sort(this.inputFromRSU);
		simulateTranferTimeFromRSU();
		this.inputList.addAll(this.inputFromCar);
		this.inputList.addAll(this.inputFromRSU);
		this.inputList.addAll(this.waitList);
		this.inputFromCar.clear();
		this.inputFromRSU.clear();
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
			double selectedTime = Math.max(this.preProcess, mes.receiverTime.get(mes.receiverTime.size()-1));
			// Process and return to car
			double processTime = Simulator.getNext(Simulator.gNBProcessPerSecond);
			double processedTime = selectedTime + processTime;
			sendMessageToCar(mes, processedTime);
			this.preProcess = processedTime;
		}
	}

	public void sendMessageToCar(Message mes, double processedTime) {
		mes.sendTime.add(processedTime);
		Simulator.carList.get(mes.indexCar).gNBRecQueue.add(mes);
	}

	public void simulateTranferTimeFromCar() {
		for (Message mes : this.inputFromCar) {
			double tranferTime = Simulator.getNext(1.0/Simulator.carGNBMeanTranfer);
			double selectedTime = Math.max(this.preReceiveFromCar, mes.sendTime.get(mes.sendTime.size()-1));
			double receiveTime = tranferTime + selectedTime;
			mes.receiverTime.add(receiveTime);
			this.preReceiveFromCar = receiveTime;
		}
	}
	
	public void simulateTranferTimeFromRSU() {
		for (Message mes : this.inputFromRSU) {
			double tranferTime = Simulator.getNext(1.0/Simulator.rSUGNBMeanTranfer);
			double selectedTime = Math.max(this.preReceiveFromRSU, mes.sendTime.get(mes.sendTime.size()-1));
			double receiveTime = tranferTime + selectedTime;
			mes.receiverTime.add(receiveTime);
			this.preReceiveFromRSU = receiveTime;
		}
	}
}
