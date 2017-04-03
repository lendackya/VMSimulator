


public class VMStatistics{

  int pageFaults;
  int writesToDisks;
  int memoryAccesses;
  String evictionAlgo;
  int numFrames;

  public VMStatistics(String evictionAlgo, int numFrames){

    this.evictionAlgo = evictionAlgo;
    this.numFrames = numFrames;
  }


  public void printStats(){

    System.out.println("Algorithm: " + this.evictionAlgo);
    System.out.println("Number of frames: " + this.numFrames);
    System.out.println("Total memory accesses: " + this.memoryAccesses);
    System.out.println("Total page faults: " + this.pageFaults);
    System.out.println("Total writes to disk:" + this.writesToDisks);
  }


}
