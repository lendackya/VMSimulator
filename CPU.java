

public class CPU {

  InstructionSet instns;
  MemoryManagementUnit mmu;
  int instructionCounter; // used to index the current instruction from the trace

  public CPU(String instructions){

    this.instns = new InstructionSet(instructions);
    this.mmu = new MemoryManagementUnit();
    this.instructionCounter = 0;
  }

  public Instruction getNextInstruction(){

    Instruction instn = this.instns.get(this.instructionCounter);
    this.instructionCounter++;
    return instn;
  }

  public long[] translateVirtualAddress(long virtualAddress, int pageSize){

    return this.mmu.getTranslation(virtualAddress, pageSize);
  }

  public InstructionSet getInstructionSet(){

    return this.instns;
  }

  public static void main(String[] args){

    CPU cpu = new CPU("gcc.trace");

    System.out.println(cpu.getNextInstruction().getAddress());
    System.out.println(cpu.getNextInstruction().getAddress());
    System.out.println(cpu.getNextInstruction().getAddress());
    System.out.println(cpu.getNextInstruction().getAddress());
    System.out.println(cpu.getNextInstruction().getAddress());

  }

}
