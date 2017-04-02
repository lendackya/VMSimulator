
public class Instruction{

  private long address;
  private char mode;

  public Instruction(long address, char mode){

    this.address = address;
    this.mode = mode;

  }

  public long getAddress(){

    return this.address;
  }

  public char getMode(){

    return this.mode;
  }

  public boolean isRead(){

    return this.mode == 'R';
  }

  public boolean isWrite(){

    return this.mode == 'W';
  }

  public void printInstructionInfo(){

    System.out.println("Address: " + this.address + "\tMode: " + this.mode);
  }


}
