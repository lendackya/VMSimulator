

public class PTE{

  private int frameNumber; // frame of the reference
  private long timestamp; // the clock cycle it was reference
  private boolean reference; // if it was referenced or not
  private boolean dirty; // is it's dirty or not
  private boolean valid; // is the page in a frame or not
  private int pageNumber;

  public PTE(int frameNumber, int pageNumber, boolean reference, boolean dirty, boolean valid, long timestamp){

    this.frameNumber = frameNumber;
    this.pageNumber = pageNumber;
    this.timestamp = timestamp;
    this.reference = reference;
    this.dirty = dirty;
    this.valid = valid;
  }

  public PTE(){

    this.frameNumber = -1;
    this.pageNumber = -1;
    this.timestamp = 0;
    this.reference = false;
    this.dirty = false;
    this.valid = false;
  }

  // SETTERS AND GETTERS:

  public void setDirtyBit(boolean isDirty){

    this.dirty = isDirty;
  }

  public boolean getDirtyBit(){

    return this.dirty;
  }

  public void setReferenceBit(boolean isRefed){

    this.reference = isRefed;
  }

  public boolean getReferenceBit(){

    return this.reference;
  }

  public void setTimestamp(long timestamp){

      this.timestamp = timestamp;
  }

  public long getTimestamp(){

    return this.timestamp;
  }

  public void setFrameNumber(int frame){

    this.frameNumber = frame;
  }

  public int getFrameNumber(){

    return this.frameNumber;
  }

  public void setValidBit(boolean valid){

    this.valid = valid;
  }

  public boolean getValidBit(){

      return this.valid;
  }

  /**
   * Determines if the PTE is dirty or clean.
   *
   * @return returns true if the bit is dirty, false if it is clean
  **/
  public boolean isDirty(){

    if (this.dirty == true){ return true; }
    else { return false; }

  }

  /**
   * Determines if the PTE is reference or unreferenced.
   *
   * @return returns true if the bit is referenced, false if it is unreferenced
  **/
  public boolean isReferenced(){

    if (this.reference == true){ return true; }
    else { return false; }
  }

  /**
   * Determines if the PTE is valid or not, i.e., if the page is currently in a frame.
   *
   * @return returns true if the bit is valid, false if it is not valid.
   *
  **/
  public boolean isValid(){

    if (this.valid == true){ return true; }
    else {return false; }
  }


  public void updatePTE(int frameNumber, boolean referenced, boolean dirty, long timestamp ){

    this.frameNumber = frameNumber;
    this.reference = referenced;
    this.dirty = dirty;
    this.timestamp = timestamp;
  }

  public void printInfo(){

    System.out.println("Frame: " + this.frameNumber + "\tPage:" + this.pageNumber + "\tReferenced: " + this.reference + "\tDirty: " + this.dirty + "\tValid: " + this.valid + "\tTimestamp: " + this.timestamp);
  }
}
