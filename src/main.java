import java.util.NoSuchElementException;
import java.util.Scanner;
import java.io.*;

public class Main{
	public static void main(String args[]){
		if(args.length != 3){
			System.out.println("You must include 3 arguments!");
			System.out.println("inFile1, inFile2, numProcs, outFile1, outFile2");
			return;
		}
		File inFile1 = new File(args[0]);
		if(!inFile1.isFile() && !inFile1.canRead()){
			System.out.println("Error reading inFile1! (Make sure its typed correctly!)");
			return;
		}
		File inFile2 = new File(args[1]);
		if(!inFile2.isFile() && !inFile2.canRead()){
			System.out.println("Error reading inFile2! (Make sure its typed correctly!)");
			return;
		}
		int numProcs = Integer.parseInt(args[2]);
		if(numProcs <= 0){
			System.out.println("Error numProcs cannot be less than 1!");
			return;
		}
		PrintStream output1 = new PrintStream(new FileOutputStream(args[3], true));
		PrintStream output2 = new PrintStream(new FileOutputStream(args[4], true));
		
		Schedule sched = new Schedule(inFile1, inFile2, numProcs);
	}
}

class Node{
	int jobID;
	int jobTime;
	Node next;
	
	Node(int jobID, int jobTime){
		this.jobID = jobID;
		this.jobTime = jobTime;
		this.next = null;
	}
}

class Schedule{
	int numNodes;
	int numProcs;
	int procUsed;
	int totalJobTimes;
	int jobTimeArray[];
	int Matrix[][];
	int Table[][];
	Node Open;
	
	Schedule(File inFile1, File inFile2, int numProcs){
		Scanner in1Scan = new Scanner(inFile1);
		Scanner in2Scan = new Scanner(inFile2);
		this.numNodes = in1Scan.nextInt();
		this.numProcs = numProcs;
		if(this.numProcs > this.numNodes)
			this.numProcs = numNodes;
		this.procUsed = 0;
		this.totalJobTimes = 0;
		this.jobTimeArray = new int[this.numNodes + 1];
		
		this.Open = new Node(-1,-1);
		
		this.Matrix = new int[this.numNodes + 1][this.numNodes + 1];
		for(int i=0;i<this.numNodes+1;i++){
			for(int j=0;j<this.numNodes+1;j++){
				this.Matrix[i][j] = 0;
			}
		}
	}
	void loadMatrix(File inFile1) throws FileNotFoundException{
		Scanner scan = new Scanner(inFile1);
		int size = scan.nextInt();
		//define the size
		this.Matrix[0][0] = size;
		//define dependencies
		while(scan.hasNextInt()){
			int parent = scan.nextInt();
			int dependent = scan.nextInt();
			this.Matrix[parent][dependent] = 1;
		}
		this.setMatrix();
	}
	int loadJobTime(File inFile2) throws FileNotFoundException{
		Scanner scan = new Scanner(inFile2);
		int totalTime = 0;
		scan.nextInt();
		while(scan.hasNextInt()){
			int jobID = scan.nextInt();
			int jobTime = scan.nextInt();
			
			this.jobTimeArray[jobID] = jobTime;
			totalTime += jobTime;
		}
		return totalTime;
	}
	void setMatrix(){
		//count depends
		for(int i=1;i<size+1;i++){
			int depTotal = 0;
			for(int j=1;j<size+1;j++){
				depTotal += this.Matrix[i][j];
			}
			this.Matrix[i][0] = depTotal;
		}
		//count parents
		for(int j=1;j<size+1;j++){
			int depTotal = 0;
			for(int i=1;i<size+1;i++){
				depTotal += this.Matrix[i][j];
			}
			this.Matrix[0][j] = depTotal;
		}
		//set diagnol
		for(int i=1;i<size+1;i++){
			this.Matrix[i][i] = 1;
		}
	}
	void printMatrix(PrintStream outFile2){
		System.setOut(outFile2);
		System.out.println("Printing Matrix...");
		for(int i=0;i<this.numNodes+1;i++){
			for(int j=0;j<this.numNodes+1;j++){
				System.out.print(this.Matrix[i][j] + "\t");
			}
			System.out.print("\n");
		}
	}
	int findOrphan(){
		for(int j=1;j<this.numNodes+1;j++){
			if(this.Matrix[0][j] == 0 && this.Matrix[j][j] == 1){
				this.Matrix[j][j] = 2;
				return j;
			}
		}
		return -1;
	}
	void openInsert(Node node){
		if(this.Open.next == null){
			//first node
			this.Open.next = node;
		}
		else{
			//find spot
			Node reader = this.Open;
			while(Open.next != null){
				if(Open.next.jobTime > node.jobTime){
					break;
				}
			}
			//insert
			node.next = reader.next;
			reader.next = node;
		}
	}
	void printOpen(PrintStream outFile2){
		System.setOut(outFile2);
		Node reader = this.Open;
		System.out.println("Printing OPEN LinkedList......");
		while(this.Open != null){
			System.out.print("("+reader.jobID+", "+reader.jobTime+")->");
		}
		System.out.print("NULL\n");
	}
	int getNextProc(int currentTime){
		for(int i=0;i<this.numProcs;i++){
			if(this.Table[i][currentTime] == 0)
				return i;
		}
		return -1;
	}
	void fillOpen(PrintStream outFile2){
		int jobID = this.findOrphan();
		if(jobID > 0){
			Node newNode = new Node(jobID,this.jobTimeArray[jobID]);
			this.openInsert(newNode);
			this.printOpen(outFile2);
			fillOpen(outFile2);
		}
	}
	void fillTable(int currentTime){
		int availProc = this.getNextProc(currentTime);
		while(availProc >= 0 && this.Open.next != null && this.procUsed < this.numProcs){
			Node newJob = this.Open.next;
			this.Open.next = this.Open.next.next;
			newjob.next = null;
			this.putJobOnTable(availProc, currentTime, newJob.jobID, newJob.jobTime);
			if(availProc > procUsed)
				procUsed++;
			availProc = this.getNextProc(currentTime);
		}
	}
	void putJobOnTable(int availProc, int currentTime, int jobID, int jobTime){
		int time = currentTime;
		int endTime = currentTime + jobTime;
		while(time < endTime){
			this.Table[availProc][time] = jobID;
			time++;
		}
		
	}
	void printTable(PrintStream outFile1, int currentTime){
		System.setOut(outFile1);
		System.out.println("Printing Table");
		System.out.println("Proc Used: " + this.procUsed + "  currentTime: " + currentTime);
		System.out.print("Time:\t|");
		for(int i=0;i<currentTime;i++){
			System.out.print(i + "|");
		}
		for(int i=0;i<procUsed;i++){
			System.out.print("\nProc: "+(i+1)+" |");
			for(int j=0;j<currentTime;j++){
				System.out.print(this.Table[i][j] + "|");
			}
			System.out.print("\n");
		}
	}
	boolean checkCycle(){
		if(this.Open.next != null)
			return false;
		if(this.Matrix[0][0] != 0)
			return false;
		if(this.procUsed != 0)
			return false
		return true;
	}
	boolean isGraphEmpty(){
		if(this.Matrix[0][0] == 0)
			return true;
		return false;
	}
	void deleteDoneJobs(int currentTime, PrintStream outFile2){
		int proc = 0;
		while(proc < this.procUsed){
			if(this.Table[proc][currentTime] <= 0 && this.Table[proc][currentTime-1]>0){
				int jobID = this.Table[proc][currentTime-1];
				this.deleteJob(jobID);
			}
			this.printMatrix(outFile2);
			proc++;
		}
	}
	void deleteJob(int jobID){
		this.Matrix[jobID][jobID] = 0;
		for(int j=1;j<this.numNodes;j++){
			if(this.Matrix[jobID][j] > 0)
				this.Matrix[0][j]--;
		}
	}
}