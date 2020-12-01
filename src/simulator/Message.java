package simulator;

import java.util.ArrayList;
import java.util.List;

public class Message implements Comparable<Message>{
	public int indexCar;
	public int indexRSU;
	public double firstSentTime; // Time send from car
	public List<Double> sendTime = new ArrayList<Double>(); // Time send from pre nodes
	public List<Double> receiverTime = new ArrayList<Double>();
	String type;
//	TYPE_1: CAR_RSU_CAR
//	TYPE_2: CAR-GNB-CAR
//	TYPE_3: CAR-RSU-GNB-CAR
	public boolean isDropt = false;

	public int compareTo(Message o) {
		if (sendTime.get(sendTime.size()-1) > o.sendTime.get(o.sendTime.size()-1)) return 1;
		else if (sendTime.get(sendTime.size()-1) < o.sendTime.get(o.sendTime.size()-1)) return -1;
		return 0;
	}

}
