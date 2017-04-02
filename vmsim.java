import java.util.Random;
import java.util.LinkedList;

public class vmsim{

  static CPU cpu;
  static Memory mem;
  static VMStatistics stats;
  static PageTable pageTable;
  static int numFrames;
  static int[] pageStream;

  static boolean DEBUG = true; // turns debug on and off

  static int clockHand; // index of the array

  public static void main(String[] args){

    if (args[0].equals("-n")){

      // Get needed program parameters and print them.
      long pageSize = 4096; // 4KB page size
      numFrames = Integer.parseInt(args[1]);
      String traceName = args[6];
      String algoType = args[3];
      int refreshRate = Integer.parseInt(args[5]);

      // Create CPU, Memory, and Statistics objects
      cpu = new CPU(traceName);
      mem = new Memory(numFrames);
      stats = new VMStatistics(algoType, numFrames);

      // Determine memory size
      int mainMemSizeBytes = 4096*numFrames;
      System.out.println("Physical Address Size: " + mainMemSizeBytes + " bytes");

      // Determinen Page Table Size
      long pageTableSize = (long) Math.pow(2,32)/4096;
      System.out.println("Page Table Size: " + pageTableSize + " pages.\n\n");

      pageTable = new PageTable(pageTableSize);
      pageTable.setCurrentlyValid(new Page[numFrames]);
      // MARK:: Begin Running Through Trace

      // Begin running through InstructionSet
      // cpu.getInstructionSet().size()
      int refreshIndex = 0;

      if (algoType.equals("opt")){ pageStream = preprocessInstrctionSet(cpu.getInstructionSet(), pageSize); }

      for (int i = 0; i < 20; i++){
        if (DEBUG) pageTable.printCurrentlyValid();
        stats.memoryAccesses++; // increment memorory acccesses
        refreshIndex++;

        if (algoType.equals("nru") && (refreshIndex == refreshRate)){
           pageTable.refreshReferenceBits();
           refreshIndex = 0;
          }

        // Get the instruction
        Instruction inst = cpu.getNextInstruction();
        //inst.printInstructionInfo();

        // Determine page number and page offset
        long[] translations = cpu.translateVirtualAddress(inst.getAddress(), 4096);
        long pageNumber = translations[0];
        long pageOffset = translations[1];

        // Look into the Page Table and see if that spot in PT is empty
        PTE pte = pageTable.getPageTableEntryAt( (int)pageNumber );

        // we have seen this page before
        if (pte.getFrameNumber() != -1){

          if (DEBUG) System.out.println("We've seen page " + pageNumber + " before.");

          // It becomes referenced
          pte.setReferenceBit(true);
          pte.setTimestamp(i); // update timestamp

          // if were writing to that page change the dirty bit
          if (inst.getMode() == 'W'){ pte.setDirtyBit(true); } // its now dirty
          else {pte.setDirtyBit(false); } // its clean

          if (DEBUG) System.out.println(pte.getFrameNumber());

          // the page is currently in a frame..
          if (pte.isValid()){

            if (DEBUG) System.out.println("Page Hit! Page is in frame " + Integer.toString(pte.getFrameNumber()));

          }else if (pte.isValid() == false){ // not it frame
            // get it from disk
            if (DEBUG) System.out.println("Page Fault!");
            stats.pageFaults++;

            // All the frames are full
            if (mem.isFull()){

                if (DEBUG) System.out.println("Frames Full\n");

              // Evict a page using the specified Eviction Algorithm
              pte.setTimestamp(i); // update timestamp
              runEvictionAlgo(algoType, pte, pageNumber);

            }else if (mem.isFull() == false){


              // Find open frame, put page in the frame, and set the frame number
              // in the Page Table.
              Frame freeFrame = mem.getFirstFreeFrame();

              Page page = new Page(pageNumber);
              mem.putPageInFrame(page, freeFrame.getNumber());
              pte.setFrameNumber(freeFrame.getNumber()); // update the frame its in
              pte.setValidBit(true); // its in frame, so its valid

              if (DEBUG) System.out.println(pageNumber + " was put in frame " + freeFrame.getNumber());
              pageTable.getCurrentlyValid()[freeFrame.getNumber()] = page;
            }
          }
        }

        // we have not seen this page before
        else{

          // theres something in the page table at this page number..
          if (DEBUG) System.out.println("Have NOT seen page " + pageNumber + " before");

          if (DEBUG) System.out.println(pte.getFrameNumber());

          // It becomes referenced!
          pte.setReferenceBit(true);
          pte.setTimestamp(i); // update timestamp

          // if were writing to that page
          if (inst.getMode() == 'W'){ pte.setDirtyBit(true); } // its now dirty
          else {pte.setDirtyBit(false); } // its clean

          // if memory is full..
          if (mem.isFull()){
            if (DEBUG) System.out.println("Frames Full");

            // run eviction algo and put in evicted frame
            runEvictionAlgo(algoType, pte, pageNumber);

          }else{
            Frame freeFrame = mem.getFirstFreeFrame(); // find open frame
            Page page = new Page(pageNumber);
            mem.putPageInFrame(page, freeFrame.getNumber()); // put page in frame
            pte.setFrameNumber(freeFrame.getNumber()); // update the pte
            pte.setValidBit(true);
            if (DEBUG) System.out.println(pageNumber + " was put in frame " + freeFrame.getNumber());
            pageTable.getCurrentlyValid()[freeFrame.getNumber()] = page; // update the currently valid array
          }
        }
      }

      stats.printStats(); // print the statistics to the user

    }else{

      System.out.println("Command line arguements must follow: –n <numframes> -a <opt|clock|nru|rand> [-r <refresh>] <tracefile>");
    }
  }


  /**
   * Runs the specified eviction algorithm on the Page Table.
   * This involves picking a Frame to evict, and then replacing that frame with the new page.
   **/
  public static void runEvictionAlgo(String evictionAlgo, PTE pte, long pageNumber){

    //pageTable.printCurrentlyValid();

    if (evictionAlgo.equals("clock")){ Clock(pte, pageNumber); } // run CLocl

    if (evictionAlgo.equals("opt")){ Opt(pte, pageNumber); } // run Opt

    if (evictionAlgo.equals("random")){ Random(pte, pageNumber); } // run Random

    if (evictionAlgo.equals("nru")){ NRU(pte, pageNumber); }  // run NRU

  } // end runEvictionAlgo

  /**
   * Runs the NRU replacement algotithm.
   **/
  public static void NRU(PTE pte, long pageNumber){
    PTE evictedPTE;

    // find entries clean and unreferenced entries - 1st priority
    PTE cleanUnreferencedPTE = pageTable.searchPageTableFor(false, false, true);
    //System.out.println("C & U: " + cleanUnreferencedPTEs.size() );
    evictedPTE = cleanUnreferencedPTE;

    // if we dont find the highest priority, we go down to the 2nd, 3rd, 4th..
    if (cleanUnreferencedPTE == null){

      // find clean and referenced entries - 2nd priority
      PTE dirtyUnreferencedPTE = pageTable.searchPageTableFor(true, false, true);
      //System.out.println("D & U: " + dirtyUnreferencedPTEs.size() );
      evictedPTE = dirtyUnreferencedPTE;

      if (dirtyUnreferencedPTE == null){

        // find unclean unreferenced entries - 3rd priority
        PTE cleanReferencedPTE = pageTable.searchPageTableFor(false, true, true);
        //System.out.println("C & R: " + cleanReferencedPTEs.size() );
        evictedPTE = cleanReferencedPTE;

        if (cleanReferencedPTE == null){

          // find unclean referenced entries - 4th priority
          PTE dirtyReferencedPTE = pageTable.searchPageTableFor(true, true, true);
          //System.out.println("D & R: " + dirtyReferencedPTEs.size() );
          evictedPTE = dirtyReferencedPTE;
        }
      }
    }

    // Give the PTE a frame number and evict the page from the frame, setting the evicted page number to unvalid
    // and add a new Page in the evicted frame
    if (DEBUG) System.out.println("Evicting from frame: " + evictedPTE.getFrameNumber());
    pte.setFrameNumber(evictedPTE.getFrameNumber());
    Page evictedPage = mem.evictPageFromFrame(evictedPTE.getFrameNumber());
    Page newPage = new Page(pageNumber);
    mem.putPageInFrame(newPage, pte.getFrameNumber() );
    pte.setValidBit(true);
    pageTable.getEntries()[(int)evictedPage.getPageNumber()].setValidBit(false); // no longer in memory, un valid bit
    pageTable.getCurrentlyValid()[evictedPTE.getFrameNumber()] = newPage;

    // if the evicted page is dirty
    if (pageTable.getEntries()[(int)evictedPage.getPageNumber()].isDirty()){
      // write back to disk
      if (DEBUG) System.out.println("Writing back to disk.");
      stats.writesToDisks++;
      pageTable.getEntries()[(int)evictedPage.getPageNumber()].setDirtyBit(false); // unmark as dirty
    }


  }

  /**
   * Runs the Random replacement algotithm.
   **/
  public static void Random(PTE pte, long pageNumber){

    // pick a random frame
    Random rand = new Random();
    int n = rand.nextInt(numFrames);

    // evict from random frame
    Frame frameToEvictFrom = mem.getFrames()[n];
    if (DEBUG) System.out.println("Evicting From Frame: " + frameToEvictFrom.getNumber());
    Page evictedPage = mem.evictPageFromFrame(frameToEvictFrom.getNumber());

    // put page in frame and write it to the Page Table
    mem.putPageInFrame(new Page(pageNumber), frameToEvictFrom.getNumber() );
    pte.setFrameNumber(frameToEvictFrom.getNumber()); // update the frame number
    pte.setValidBit(true); // set the valid bit to true
    pageTable.getEntries()[(int)evictedPage.getPageNumber()].setValidBit(false);

    // if the evicted page is dirty
    if (pageTable.getEntries()[(int)evictedPage.getPageNumber()].isDirty()){
      // write back to disk
      if (DEBUG) System.out.println("Writing back to disk.");
      stats.writesToDisks++;
      pageTable.getEntries()[(int)evictedPage.getPageNumber()].setDirtyBit(false); // unmark as dirty
    }
  }

  /**
   * Runs the Clock replacement algotithm.
   **/
  public static void Clock(PTE pte, long pageNumber){
    boolean cont;
    cont = true;
    PTE currPTE;

    while(cont){
      currPTE = pageTable.getEntries()[(int)pageTable.getCurrentlyValid()[clockHand].getPageNumber()]; // keeps track of the current PTE being pointed at in array
      // check to see if theres something in that spot
      if (currPTE != null){

        if ( currPTE.getReferenceBit() == true) { // if the PTE is referenced

          currPTE.setReferenceBit(false); // change it to false

          // advance clock hand
          if (clockHand == pageTable.getCurrentlyValid().length - 1){ clockHand = 0; }
          else{ clockHand++; }

        }else{

          // its 0, evict from frame
          int frameNum = currPTE.getFrameNumber();
          System.out.println("Evicting from frame " + frameNum);
          Page evictedPage = mem.evictPageFromFrame(frameNum);

          pte.setFrameNumber(frameNum);
          pte.setValidBit(true);

          // advance clock hand
          if (clockHand == pageTable.getCurrentlyValid().length - 1){ clockHand = 0;}
          else{ clockHand++; }

          if (pageTable.getEntries()[(int)evictedPage.getPageNumber()].isDirty()){
            // write it to disk
            stats.writesToDisks++;
            pageTable.getEntries()[(int)evictedPage.getPageNumber()].setDirtyBit(false); // clean
          }
          cont = false; // we can stop cause we found a page to evict
        }
      }
    } // end while


  }

  /**
   * Runs the Opt replacement algotithm.
   **/
  public static void Opt(PTE pte, long pageNumber){
    // look at the pages that are in each frame

    double min = Double.POSITIVE_INFINITY;
    Page pageToEvict;

    int frameNumToEvictFrom = 0;

    // for each entry in a frame
    for (int i = 0; i < numFrames; i++){

      Page currPage = pageTable.getCurrentlyValid()[i];
      int currPageNumber = (int) currPage.getPageNumber();
      int seenIn = 0;
      // count when it appears next in the pageStream
      for (int j = stats.memoryAccesses; j < pageStream.length; j++){ if (pageStream[j] == currPageNumber){ seenIn = j; break; } }

      if (seenIn < min){

        pageToEvict = currPage;
        frameNumToEvictFrom = i;
       }
    }

    System.out.println("Evicting from frame: " + frameNumToEvictFrom);

    // evict the page
    Page evictedPage = mem.evictPageFromFrame(frameNumToEvictFrom);

    Page newPage = new Page(pageNumber); // create new page

    mem.putPageInFrame(newPage, frameNumToEvictFrom);
    pte.setValidBit(true);
    pte.setFrameNumber(frameNumToEvictFrom);

    if (pageTable.getEntries()[(int)evictedPage.getPageNumber()].isDirty()){

      // write it to disk
      stats.writesToDisks++;
      pageTable.getEntries()[(int)evictedPage.getPageNumber()].setDirtyBit(false); // clean
    }
  }


  private static int[] preprocessInstrctionSet(InstructionSet instructionSet, long pageSize){

    if (DEBUG) System.out.println("Preprocessing File\n");

    int[] pageStream = new int[instructionSet.size()];

    for (int i = 0; i < pageStream.length; i++){

      Instruction currInst = instructionSet.get(i);
      long address = currInst.getAddress();
      long pageNumber =  (address / pageSize);
      pageStream[i] = (int) pageNumber;
    }
    return pageStream;
  }

} // end vmsim
