import java.util.LinkedList;


/**
  * This class holds Page Table Entries (PTE) in an array, and provides methods for returning
  * a PTE from the
  *
  *
  *
  *
**/
public class PageTable {

  private PTE[] entries; // the whole page table
  private Page[] currentlyValid; // holds what pages are in each frame.

  public PageTable(long numPages){

    this.entries = new PTE[(int)numPages];
    this.currentlyValid = null;
    for (int i = 0; i < this.entries.length; i++){

      this.entries[i] = new PTE(-1, i, false, false, false, 0);
    }
  }

  public long getNumPages(){

    return this.entries.length;
  }

  public PTE[] getEntries(){

    return this.entries;
  }

  public Page[] getCurrentlyValid(){

    return this.currentlyValid;
  }

  public void setCurrentlyValid(Page[] currValid){

    this.currentlyValid = currValid;
  }

  public PTE getPageTableEntryAt(long pagePos){

    return this.entries[(int)pagePos];
  }

  /**
    * Checks if a position in the page table is occupied.
    *
    * @params pagePos the index in the Page Table.
    *
    * @return returns true is there is an entry, false if there isn't an entry.
  **/
  public boolean isOccupied(long pagePos){

    if (this.entries[(int) pagePos] != null){ return true; }
    else{ return false; }
  }

  /**
   * Scans the Page Table and returns a the first PTE object that matches the requirements
   *
   * @return returns a PTE object that meets the requirements
   **/
  public PTE searchPageTableFor(boolean clean, boolean referenced, boolean valid){
    PTE pte;

    for (int i = 0; i < this.currentlyValid.length; i++){

      if (this.currentlyValid[i] != null){

        pte = this.entries[(int)this.currentlyValid[i].getPageNumber()];

        if ((pte.getDirtyBit() == clean) && (pte.getReferenceBit() == referenced) && (pte.getValidBit() == valid)){ return pte; }
      }
    }
    return null;
  }

  public void refreshReferenceBits(){

    // looks through the set of currently valid pages and refreshes their reference bit
    for (int i = 0; i < this.currentlyValid.length; i++){

       if (this.currentlyValid[i] != null){
         int pageNum = (int)this.currentlyValid[i].getPageNumber();
         this.entries[pageNum].setReferenceBit(false);
       }
     }
  }

  public void printCurrentlyValid(){

    for (int i = 0; i < this.currentlyValid.length; i++){
        if (this.currentlyValid[i] != null){
          int pageNum = (int)this.currentlyValid[i].getPageNumber();
          this.entries[pageNum].printInfo(); // unreferenced the entry
      }
    }
  }

  public void printPageTable(){

      for (int i = 0; i < this.entries.length; i++){
          if (this.entries[i].getFrameNumber() != -1){
            this.entries[i].printInfo(); // unreferenced the entry
        }
      }
  }

  public static void main(String args[]){

    PageTable pt = new PageTable(10);
    PTE pte = new PTE(1010940, 0, true, false, false, 0);
    //pt.putPageTableEntryIn(pte, 0);
    //pt.putPageTableEntryIn(pte, 1);
    //pt.putPageTableEntryIn(pte, 2);


    pt.getPageTableEntryAt(0).setDirtyBit(false);

    System.out.println(pt.getPageTableEntryAt(0).isDirty());

    pt.getPageTableEntryAt(0).printInfo();
  }


}
