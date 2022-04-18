import java.util.NoSuchElementException;
import java.util.Scanner;
import java.io.*;

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
	
	Schedule(int numNodes, int numProcs){
		this.numNodes = numNodes;
		this.numProcs = numProcs;
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
		
	}
	void fillOpen(){
		
	}
	void fillTable(){
		
	}
	void putJobOnTable(int availProc, int currentTime, int jobID, int jobTime){
		
	}
	void printTable(PrintStream outFile1, int currentTime){
		
	}
	boolean checkCycle(){
		
	}
	boolean isGraphEmpty(){
		
	}
	void deleteDoneJobs(){
		
	}
	void deleteJob(int jobID){
		
	}
}