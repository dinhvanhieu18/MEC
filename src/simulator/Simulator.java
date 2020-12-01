package simulator;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Simulator {
	
	public static GnbSimulator gNB = new GnbSimulator();
	public static List<RsuSimulator> rSUList = new ArrayList<RsuSimulator>();
	public static List<CarSimulator> carList = new ArrayList<CarSimulator>();
	public static List<Message> output = new ArrayList<Message>();
	public static List<Double> listTimeMessages = new ArrayList<Double>();
	
	// GNB config
	public static int gNBProcessPerSecond = Config.getInstance().getAsInteger("gnb_process_per_second");
	public static double gNBCarMeanTranfer = Config.getInstance().getAsDouble("gnb_car_mean_tranfer");
	
	// RSU config
	public static int rSUNumbers = Config.getInstance().getAsInteger("rsu_numbers");
	public static String xList = Config.getInstance().getAsString("list_rsu_xcoord");
	public static String yList = Config.getInstance().getAsString("list_rsu_ycoord");
	public static String zList = Config.getInstance().getAsString("list_rsu_zcoord");
	public static float rSUCoverRadius = Config.getInstance().getAsFloat("rsu_cover_radius");
	public static int rSUProcessPerSecond = Config.getInstance().getAsInteger("rsu_process_per_second");
	public static float rSUCarMeanTranfer = Config.getInstance().getAsFloat("rsu_car_mean_tranfer");
	public static float rSUGNBMeanTranfer = Config.getInstance().getAsFloat("rsu_gnb_mean_tranfer");
	
	// Car config
	public static int carSpeed = Config.getInstance().getAsInteger("car_speed");
	public static String carAppearStrategy = Config.getInstance().getAsString("car_appear_strategy");
	public static String carPacketStrategy = Config.getInstance().getAsString("car_packet_strategy");
	public static float carGNBMeanTranfer = Config.getInstance().getAsFloat("car_gnb_mean_tranfer");
	public static float carRSUMeanTranfer = Config.getInstance().getAsFloat("car_rsu_mean_tranfer");
	
	public static double pL = Config.getInstance().getAsDouble("default_pl");
	public static double pR = Config.getInstance().getAsDouble("default_pr");
	public static double simTime = Config.getInstance().getAsDouble("simTime");
	public static double cycleTime = Config.getInstance().getAsDouble("cycle_time");
	public static double roadLength = Config.getInstance().getAsDouble("road_length");
	
	public static String dumpDelayDetail = Config.getInstance().getAsString("dump_delay_detail");
	public static String dumpDelayGeneral = Config.getInstance().getAsString("dump_delay_general");
	private static FileWriter fileWriterDetail;
	private static FileWriter fileWriterGeneral;
	
	public static void main(String[] args) {
		// Get RSU list
        String[] xs = xList.split(";");
    	String[] ys = yList.split(";");
    	String[] zs = zList.split(";");
    	for (int i=0; i<rSUNumbers; i++) {
    		double xcord = Double.parseDouble(xs[i]);
    		double ycord = Double.parseDouble(ys[i]);
    		double zcord = Double.parseDouble(zs[i]);
    		RsuSimulator rsu = new RsuSimulator(i, xcord, ycord, zcord);
    		rSUList.add(rsu);
    	}
    	prepareTimeMessages();
    	System.out.println(listTimeMessages.size());
    	carAppear();
    	System.out.println(carList.size());
    	// Loop 1s simulator: send message, RSU, GNB process and car receive
    	double currentTime = 0;
    	while (currentTime < simTime) {
    		for (CarSimulator car : carList) {
    			car.working(currentTime);
    		}
    		for (RsuSimulator rsu: rSUList) {
    			rsu.working(currentTime);
    		}
    		Simulator.gNB.working(currentTime);
    	
    		for (CarSimulator car: carList) {
    			car.receiveMessage();
        	}
    		currentTime += cycleTime;
    	}
    	dumpOutput();
    }


	private static void prepareTimeMessages() {
		try (Scanner scanner = new Scanner(new File(carPacketStrategy))) {
			double currentTime = 0;
			while (scanner.hasNext()) {
				double tmp = scanner.nextDouble();
				double timeFromStartCar = currentTime + tmp;
				currentTime = timeFromStartCar;
				listTimeMessages.add(timeFromStartCar);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static void carAppear() {
		try (Scanner scanner = new Scanner(new File(carAppearStrategy))) {
			double currentTime = 0;
			int index = 0;
			while (scanner.hasNext()) {
				double tmp = scanner.nextDouble();
				double startCar = currentTime + tmp;
				if (startCar > simTime) {
					return;
				}
				CarSimulator car = new CarSimulator(index, startCar);
				carList.add(car);
				index ++;
				currentTime = startCar;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static void dumpOutput() {
		try {
			System.out.println("Dumping output ...");
			double meanDelay = 0;
			Collections.sort(output);
			int countDropt = 0;
			for (Message mes : output) {
				meanDelay += (mes.receiverTime.get(mes.receiverTime.size()-1) - mes.firstSentTime) / output.size();
				if (mes.isDropt) countDropt ++;
			}
			fileWriterDetail = new FileWriter(new File(dumpDelayDetail));
			fileWriterGeneral = new FileWriter(new File(dumpDelayGeneral), true);
			fileWriterDetail.write(meanDelay + "\n");
			fileWriterDetail.write(countDropt + "\t" + output.size() + "\n");
			fileWriterGeneral.write( carPacketStrategy + "\t" + carAppearStrategy + "\t" + pL + "\t" + pR + "\t");
			fileWriterGeneral.write(meanDelay + "\t" + countDropt + "\t" + output.size() + "\n");
			fileWriterGeneral.flush();
			for (Message mes : output) {
				double sendTime = mes.firstSentTime;
				double receiverTime = mes.receiverTime.get(mes.receiverTime.size()-1);
				double delay = receiverTime - sendTime;
				String message = sendTime + "\t" + receiverTime + "\t" + delay + "\t" + mes.type + "\n";
				fileWriterDetail.write(message);
				fileWriterDetail.flush();
			}
			System.out.println("Done dumping ouput!!!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static double getNext(double x) {
		Random random = new Random();
		return -Math.log(1.0 - random.nextDouble()) / x;
	}
}
