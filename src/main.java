import java.util.NoSuchElementException;
import java.util.Scanner;
import java.io.*;

public class Main{
	public static void main(String args[]) throws FileNotFoundException{
		if(args.length != 5){
			System.out.println("You must include 5 arguments!");
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
		
		Schedule sched = new Schedule(inFile1, inFile2, numProcs, output2);
		
		int currentTime = 0;
		//step 3
		System.setOut(output2);
		System.out.println("Starting Step 3");
		currentTime = 0;
		sched.Open = new Node(-1,-1);
		sched.procUsed = 0;
		//step 4
		System.setOut(output2);
		System.out.println("Starting Step 4");
		sched.totalJobTimes = sched.loadJobTime(inFile2);
		sched.Table = new int[sched.numProcs][sched.totalJobTimes];
		for(int i=0;i<sched.numProcs;i++){
			for(int j=0;j<sched.totalJobTimes;j++){
				sched.Table[i][j] = 0;
			}
		}
		sched.printTable(output1,currentTime);
		//step 10
		while(!sched.isGraphEmpty()){
			//step 5
			System.setOut(output2);
			System.out.println("Starting Step 5");
			sched.fillOpen(output2);
			sched.printOpen(output2);
			//step 6
			System.setOut(output2);
			System.out.println("Starting Step 6");
			sched.fillTable(currentTime, output2);
			sched.printTable(output1, currentTime);
			//step 9
			System.setOut(output2);
			System.out.println("Starting Step 9");
			if(sched.checkCycle(currentTime)){
				System.setOut(output1);
				System.out.println("Error! There is a cycle in the graph");
				return;
			}
			//step 7
			System.setOut(output2);
			System.out.println("Starting Step 7");
			currentTime++;
			//step 8
			System.setOut(output2);
			System.out.println("Starting Step 8");
			sched.deleteDoneJobs(currentTime, output2);
		}
		//step 11
		sched.printTable(output1, currentTime);
		//step 12
		output1.close();
		output2.close();
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
	
	Schedule(File inFile1, File inFile2, int numProcs, PrintStream outFile2) throws FileNotFoundException{
		Scanner in1Scan = new Scanner(inFile1);
		Scanner in2Scan = new Scanner(inFile2);
		this.numNodes = in1Scan.nextInt();
		this.numProcs = numProcs;
		if(this.numProcs > this.numNodes)
			this.numProcs = numNodes;
		this.procUsed = 0;
		
		//jobTime setup
		this.jobTimeArray = new int[this.numNodes + 1];
		this.totalJobTimes = 0;
		
		//OPEN setup
		this.Open = null;
		
		//matrix setup
		this.Matrix = new int[this.numNodes + 1][this.numNodes + 1];
		for(int i=0;i<this.numNodes+1;i++){
			for(int j=0;j<this.numNodes+1;j++){
				this.Matrix[i][j] = 0;
			}
		}
		this.loadMatrix(inFile1);
		this.printMatrix(outFile2);
	}
	/*
		job time related methods
	*/
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
		for(int i=0;i<this.numNodes+1;i++){
			System.out.print(this.jobTimeArray[i] + ", ");
		}
		return totalTime;
	}
	/*
		matrix related methods
	*/
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
		this.setMatrix(size);
	}
	void setMatrix(int size){
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
		System.out.print("Printing Matrix...\n \t");
		for(int i=0;i<this.numNodes+1;i++){
			System.out.print(i+"\t");
		}
		System.out.print("\n");
		for(int i=0;i<this.numNodes+1;i++){
			System.out.print(i+"\t");
			for(int j=0;j<this.numNodes+1;j++){
				System.out.print(this.Matrix[i][j] + "\t");
			}
			System.out.print("\n");
		}
	}
	/*
		open related methods
	*/
	void openInsert(Node node){
		if(this.Open.next == null){
			//first node
			this.Open.next = node;
		}
		else{
			//find spot
			Node reader = this.Open.next;
			while(reader.next != null){
				if(reader.next.jobTime > node.jobTime){
					break;
				}
				reader = reader.next;
			}
			//insert
			node.next = reader.next;
			reader.next = node;
		}
	}
	int findOrphan(){
		System.out.println("Finding next orphan");
		for(int j=1;j<this.numNodes+1;j++){
			System.out.println("looping:"+j);
			if(this.Matrix[0][j] == 0 && this.Matrix[j][j] == 1){
				this.Matrix[j][j] = 2;
				System.out.println("found");
				return j;
			}
		}
		return -1;
	}
	void fillOpen(PrintStream outFile2){
		int jobID = this.findOrphan();
		while(jobID > 0){
			if(jobID > 0){
				Node newNode = new Node(jobID,this.jobTimeArray[jobID]);
				System.out.println("fillOpen new node: "+jobID+", "+this.jobTimeArray[jobID]);
				this.openInsert(newNode);
				this.printOpen(outFile2);
				this.printMatrix(outFile2);
			}
			jobID = this.findOrphan();
		}
	}
	void printOpen(PrintStream outFile2){
		System.setOut(outFile2);
		Node reader = this.Open;
		System.out.println("Printing OPEN LinkedList......");
		while(reader != null){
			System.out.print("("+reader.jobID+", "+reader.jobTime+")->");
			reader = reader.next;
		}
		System.out.print("NULL\n");
	}
	/*
		table related methods
	*/
	int getNextProc(int currentTime){
		for(int i=0;i<this.numProcs;i++){
			if(this.Table[i][currentTime] == 0)
				return i;
		}
		return -1;
	}
	void fillTable(int currentTime, PrintStream outFile2){
		int availProc = this.getNextProc(currentTime);
		System.out.println("Next Proc:"+availProc);
		while(availProc >= 0 && this.Open.next != null && this.procUsed <= this.numProcs){
			Node newJob = this.Open.next;
			this.Open.next = newJob.next;
			
			System.out.println("Removed Node:"+newJob.jobID+", "+newJob.jobTime);
			
			this.putJobOnTable(availProc, currentTime, newJob.jobID, newJob.jobTime);
			if(availProc+1 > this.procUsed)
				this.procUsed++;
			
			System.out.println("procs used:"+this.procUsed);
			
			this.printTable(outFile2, currentTime);
			availProc = this.getNextProc(currentTime);
			
			System.out.println("Next Proc:"+availProc);
			
			if(availProc == -1){
				break;
			}
		}
		this.printTable(outFile2, currentTime);
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
		System.out.println("=====TABLE START=====");
		System.out.println("Proc Used: " + this.procUsed + "  currentTime: " + currentTime);
		System.out.print("Time:\t|");
		for(int i=0;i<=currentTime;i++){
			System.out.print(i + "\t|");
		}
		for(int i=0;i<this.numProcs;i++){
			System.out.print("\nProc: "+(i+1)+" |");
			for(int j=0;j<=currentTime;j++){
				System.out.print(this.Table[i][j] + "\t|");
			}
		}
		System.out.println("\n=====TABLE END=====");
	}
	/*
		check related methods
	*/
	boolean checkCycle(int currentTime){
		if(this.Open.next == null && this.Matrix[0][0] != 0 && this.checkAllProcAvailable(currentTime)){
			return true;
		}
		return false;
	}
	boolean isGraphEmpty(){
		if(this.Matrix[0][0] == 0)
			return true;
		return false;
	}
	/*
		job related methods
	*/
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
		this.Matrix[0][0]--;
		for(int j=1;j<this.numNodes+1;j++){
			if(this.Matrix[jobID][j] > 0)
				this.Matrix[0][j]--;
		}
	}
	boolean checkAllProcAvailable(int currentTime){
		for(int i=0;i<this.numProcs;i++){
			if(Table[i][currentTime] != 0){
				return false;
			}
		}
		return true;
	}
}