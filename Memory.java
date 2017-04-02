

public class Memory{

  private Frame[] frames;
  private int numFrames;
  private int framesOccupied;


  public Memory(int numFrames){
    this.framesOccupied = 0;
    this.numFrames = numFrames;
    this.initFrames();
  }

  private void initFrames(){

    this.frames = new Frame[this.numFrames];
    Frame frame;

    // init the frames
    for (int i = 0; i < this.frames.length; i++){

      frame = new Frame(i); // create frames to fill into the array
      this.frames[i] = frame; // put in array
    }
  }

  public void printUsedFrames(){

    for (int i = 0; i < this.frames.length; i++){

      if (this.frames[i].isOccupied()){

        Frame frame = this.frames[i];
        Page page = frame.getPage();
        System.out.println("Frame " + i + " is occupied.");
      }
    }
  }

  public Frame[] getFrames(){

    return this.frames;
  }

  public Page evictPageFromFrame(int framePos){

    Frame frame = this.frames[framePos];

    if (frame.isOccupied() == true){

      Page evictedPage = frame.getPage();
      frame.setPage(null);
      this.framesOccupied--;
      //System.out.println("Number of Frames Full: " + this.framesOccupied);
      return evictedPage;
    }else {

      System.out.println("Frame is empty. Can't evict.");
      return null;
    }
  }


  public boolean isFull(){

    if (this.framesOccupied == this.frames.length){
      return true;
    }else{
      return false;
    }
  }

  public Frame getFirstFreeFrame(){

    for (int i = 0; i < this.numFrames; i++){

      if (this.frames[i].isOccupied() == false){ return this.frames[i]; }
    }

    return null;
  }

  /**
   * Puts a Page Table Entry (PTE) into a given frame.
   *
   * @params pte the PTE object being put into the frame.
   * @params framePos the frame index to pull the PTE from.
   *
   * FIXME: Might have to check if the page is dirty, referenced, etc if removing a PTE already in the frame.
  **/
  public void putPageInFrame(Page page, int framePos){

    if (framePos >= this.frames.length || framePos < 0){ System.out.println("Bounds Error!"); }
    else{

      this.frames[framePos].setPage(page);
      this.framesOccupied++;
      //System.out.println("Number of Frames Full: " + this.framesOccupied);
    }
  }

  /**
   * Returns the Page Table Entry (PTE) that is located at a given frame.
   *
   * @params framePos the frame index to pull the PTE from.
   *
   * @return returns the PTE object at the specified frame position, null if there is not PTE at the given frame.
  **/
  public Page getPageAtFrame(int framePos){

    if (this.frames[framePos].isOccupied()){ return this.frames[framePos].getPage(); }
    else{ return null; }
  }


  public static void main(String[] args){

    // Memory mem = new Memory(10);
    // System.out.println(mem.numFrames);
    // //Page p1 = new Page();
    // //Page p2 = new Page();
    // //Page p3 = new Page();
    // //Page p4 = new Page();
    // //Page p5 = new Page();
    //
    // //mem.putPageInFrame(p1, 0);
    // //mem.putPageInFrame(p2, 1);
    // //mem.putPageInFrame(p8, 9);
    // mem.putPageInFrame(p5, 6);
    // //mem.putPageInFrame(p7, 4);
    // mem.printUsedFrames();
    // //System.out.println(mem.evictPageFromFrame(1).getPageSize());
    // mem.evictPageFromFrame(2);
    //
    // Page remPage = mem.getPageAtFrame(1);
    // System.out.println(remPage);
  }

}
