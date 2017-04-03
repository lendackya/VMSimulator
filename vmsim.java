import java.util.Random;
import java.util.LinkedList;

public class vmsim{

  static boolean DEBUG = false; // turns debug on and off

  static CPU cpu;
  static Memory mem;
  static VMStatistics stats;
  static PageTable pageTable;
  static int numFrames;
  static int[] pageStream;

  static int clockHand; // index of the array used in clock algorithm

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
      System.out.println("Page Table Size: " + pageTableSize + " pages.\n");

      pageTable = new PageTable(pageTableSize);
      pageTable.setCurrentlyValid(new Page[numFrames]);

      int refreshIndex = 0; // when to refresh

      // cpu.getInstructionSet().size()

      // preprocess instructions if runing opt
      if (algoType.equals("opt")){ pageStream = preprocessInstrctionSet(cpu.getInstructionSet(), pageSize); }

      // MARK:: Begin Running Through Trace
      for (int i = 0; i < cpu.getInstructionSet().size(); i++){

        if (DEBUG) pageTable.printCurrentlyValid();

        stats.memoryAccesses++; // increment memorory acccesses
        refreshIndex++;

        // check if the table needs to be refreshed
        if (algoType.equals("nru") && (refreshIndex == refreshRate)){
           pageTable.refreshReferenceBits();
           refreshIndex = 0;
          }

        // Get the instruction
        Instruction inst = cpu.getNextInstruction();

        // Determine page number and page offset
        long[] translations = cpu.translateVirtualAddress(inst.getAddress(), 4096);
        long pageNumber = translations[0];
        long pageOffset = translations[1];

        // Look into the Page Table and see if that spot in PT is empty
        PTE pte = pageTable.getPageTableEntryAt( (int)pageNumber );

        // theres something in the page table at this page number..
        if (DEBUG) System.out.println("Page: " + pageNumber);
        if (DEBUG) System.out.println("Frame(-1 if not prev. seen): " + pte.getFrameNumber());

        pte.update(true, inst.getMode() == 'W', i); // update reference, dirty and timestamp

        // the page is currently in a frame..
        if (pte.isValid()){

          if (DEBUG) System.out.println("Page Hit! Page is in frame " + Integer.toString(pte.getFrameNumber()));

        }else if (pte.isValid() == false){ // not it frame

          if (DEBUG) System.out.println("Page Fault! Page " + pageNumber + " is not in a frame.");
          stats.pageFaults++;

          // if memory is full..
          if (mem.isFull()){
            if(DEBUG) System.out.println("Frames Full");

            runEvictionAlgo(algoType, pte, pageNumber); // run eviction algo
          }else{ // memory is not full
            Frame freeFrame = mem.getFirstFreeFrame(); // find open frame

            Page page = new Page(pageNumber); // create new page
            mem.putPageInFrame(page, freeFrame.getNumber()); // put page in frame

            pte.setFrameNumber(freeFrame.getNumber()); // update the pte frame number
            pte.setValidBit(true); // set the valid bit

            if (DEBUG) System.out.println(pageNumber + " was put in frame " + freeFrame.getNumber());
            pageTable.getCurrentlyValid()[freeFrame.getNumber()] = page; // update the currently valid array
            }
        }
      }

      stats.printStats(); // print the statistics to the user

    }else{
      System.out.println("Command line arguements must follow: –n <numframes> -a <opt|clock|nru|rand> [-r <refresh>] <tracefile>");
    } // end if-else
  } // end main

  /**
   * Runs the specified eviction algorithm on the Page Table.
   * This involves picking a Frame to evict, and then replacing that frame with the new page.
   **/
  public static void runEvictionAlgo(String evictionAlgo, PTE pte, long pageNumber){

    if (evictionAlgo.equals("clock")){ Clock(pte, pageNumber); } // run CLocl

    if (evictionAlgo.equals("opt")){ Opt(pte, pageNumber); } // run Opt

    if (evictionAlgo.equals("rand")){ Random(pte, pageNumber); } // run Random

    if (evictionAlgo.equals("nru")){ NRU(pte, pageNumber); }  // run NRU

  }

  // MARK :: Eviction Algorithms Code

  /**
   * Runs the NRU replacement algotithm.
   **/
  public static void NRU(PTE pte, long pageNumber){

    PTE evictedPTE;

    // find entries clean and unreferenced entries - 1st priority
    PTE cleanUnreferencedPTE = pageTable.searchPageTableFor(false, false, true);
    //if (DEBUG) System.out.println("C & U: " + cleanUnreferencedPTEs.size() );
    evictedPTE = cleanUnreferencedPTE;

    // if we dont find the highest priority, we go down to the 2nd, 3rd, 4th..
    if (cleanUnreferencedPTE == null){

      // find clean and referenced entries - 2nd priority
      PTE dirtyUnreferencedPTE = pageTable.searchPageTableFor(true, false, true);
      //if (DEBUG) System.out.println("D & U: " + dirtyUnreferencedPTEs.size() );
      evictedPTE = dirtyUnreferencedPTE;

      if (dirtyUnreferencedPTE == null){

        // find unclean unreferenced entries - 3rd priority
        PTE cleanReferencedPTE = pageTable.searchPageTableFor(false, true, true);
        //if (DEBUG) System.out.println("C & R: " + cleanReferencedPTEs.size() );
        evictedPTE = cleanReferencedPTE;

        if (cleanReferencedPTE == null){

          // find unclean referenced entries - 4th priority
          PTE dirtyReferencedPTE = pageTable.searchPageTableFor(true, true, true);
          //if (DEBUG) System.out.println("D & R: " + dirtyReferencedPTEs.size() );
          evictedPTE = dirtyReferencedPTE;
        }
      }
    }

    // Give the PTE a frame number and evict the page from the frame, setting the evicted page number to unvalid
    // and add a new Page in the evicted frame
    if (DEBUG) System.out.println("Evicting from frame: " + evictedPTE.getFrameNumber());

    pte.setFrameNumber(evictedPTE.getFrameNumber()); // update frame

    Page evictedPage = mem.evictPageFromFrame(evictedPTE.getFrameNumber());
    Page newPage = new Page(pageNumber);

    mem.putPageInFrame(newPage, pte.getFrameNumber() );
    pte.setValidBit(true);
    pageTable.getEntries()[(int)evictedPage.getPageNumber()].setValidBit(false); // no longer in memory, un valid bit
    pageTable.getCurrentlyValid()[evictedPTE.getFrameNumber()] = newPage;

    // if the evicted page is dirty
    if (pageTable.getEntries()[(int)evictedPage.getPageNumber()].isDirty()){
      // write back to disk
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
    int frameNum = frameToEvictFrom.getNumber();

    // if (DEBUG)
    System.out.println("Evicting From Frame: " + frameNum);
    Page evictedPage = mem.evictPageFromFrame(frameNum);
    pageTable.getEntries()[(int)evictedPage.getPageNumber()].setValidBit(false); // evicted page is not in memory anymore

    // put page in frame and write it to the Page Table
    Page newPage = new Page(pageNumber);

    mem.putPageInFrame(newPage, frameNum );
    pte.setFrameNumber(frameToEvictFrom.getNumber()); // update the frame number
    pte.setValidBit(true); // set the valid bit to true

    pageTable.getCurrentlyValid()[frameNum] = newPage;

    // if the evicted page is dirty
    if (pageTable.getEntries()[(int)evictedPage.getPageNumber()].isDirty()){
      // write back to disk
      System.out.println("Writing back to disk");
      stats.writesToDisks++;
      pageTable.getEntries()[(int)evictedPage.getPageNumber()].setDirtyBit(false); // unmark as dirty
    }
  }

  /**
   * Runs the Clock replacement algotithm.
   **/
  public static void Clock(PTE pte, long pageNumber){
    boolean notFound = true;
    PTE currPTE;

    while(notFound){

      // keeps track of the current PTE being pointed at in array
      currPTE = pageTable.getEntries()[(int)pageTable.getCurrentlyValid()[clockHand].getPageNumber()];

      // check to see if theres something in that spot
      if (currPTE != null){

        if ( currPTE.getReferenceBit() == true) { // if the PTE is referenced

          currPTE.setReferenceBit(false); // change it to false

          // advance clock hand
          if (clockHand == pageTable.getCurrentlyValid().length - 1){ clockHand = 0; }
          else{ clockHand++; }

        }else{ // reference bit is 0, evict from frame

          int frameNum = currPTE.getFrameNumber(); // frame were evicting from
          if (DEBUG) System.out.println("Evicting from frame " + frameNum);

          Page evictedPage = mem.evictPageFromFrame(frameNum);
          pageTable.getEntries()[(int)evictedPage.getPageNumber()].setValidBit(false); // not in memory anymore

          Page newPage = new Page(pageNumber);
          mem.putPageInFrame(newPage, frameNum); // put in frame

          pte.setFrameNumber(frameNum); // update the frame number in pte
          pte.setValidBit(true); // set the valid bit

          pageTable.getCurrentlyValid()[(int)frameNum] = newPage;

          // advance clock hand
          if (clockHand == pageTable.getCurrentlyValid().length - 1){ clockHand = 0;}
          else{ clockHand++; }

          if (pageTable.getEntries()[(int)evictedPage.getPageNumber()].isDirty()){
            // write it to disk
            stats.writesToDisks++;
            pageTable.getEntries()[(int)evictedPage.getPageNumber()].setDirtyBit(false); // clean
          }
          notFound = false; // we can stop cause we found a page to evict
        }
      }
    } // end while


  }

  /**
   * Runs the Opt replacement algotithm.
   **/
  public static void Opt(PTE pte, long pageNumber){

    // used to keep track to see if a bigger instsSince is reached
    double maxInstsSince = Double.NEGATIVE_INFINITY;

    Page pageToEvict; // what page we are going to evict
    Frame frameToEvictFrom = null; // what frame were going to evict from

    // go through each page in the frame
    for (int i = 0; i < numFrames; i++){

      Page currPage = pageTable.getCurrentlyValid()[i]; // the page currently being looked at
      int currPageNumber = (int) currPage.getPageNumber(); // the page number of the current page
      int instsSince = 0;

      // loop through the pageStream, dont look to previous instructions
      for (int j = stats.memoryAccesses - 1; j < pageStream.length; j++){

        instsSince++;

        if (pageStream[j] == currPageNumber){ // we found the page in the instruction set

          // update max if this is the furthest away from being used
          if(instsSince > maxInstsSince){ maxInstsSince = instsSince; }

          pageToEvict = currPage; // update the page were gonna evict
          frameToEvictFrom = mem.getFrames()[i]; // update frame to evict from
          break; // dont care about further instance of this page
        } // end if

        // so we dont have to search all instructions in page stream
        // this page can be referenced later on or not anymore
        // as soon as this surpasses the current highest value, we can stop looking and make this
        // one the best page to remove.
        if (instsSince > maxInstsSince){
          maxInstsSince = instsSince;
          pageToEvict = currPage; // update the page were gonna evict
          frameToEvictFrom = mem.getFrames()[i]; // update frame
          break;
        }
       } // end inner loop
    } // end outer loop

    // evict the page and update the page tabel
    Page evictedPage = mem.evictPageFromFrame(frameToEvictFrom.getNumber());
    pageTable.getEntries()[(int) evictedPage.getPageNumber()].setValidBit(false); // not in memory anymore

    if (DEBUG) System.out.println("Evicting from frame: " + frameToEvictFrom.getNumber());

    Page newPage = new Page(pageNumber); // create new page
    mem.putPageInFrame(newPage, frameToEvictFrom.getNumber()); // put new page in evicted frame

    if (DEBUG) System.out.println("Page " + newPage.getPageNumber() + " was put in frame " + frameToEvictFrom.getNumber() + " " + newPage.getPageNumber());
    pte.setValidBit(true); // its in memory now
    pte.setFrameNumber(frameToEvictFrom.getNumber()); // update frame number

    pageTable.getCurrentlyValid()[frameToEvictFrom.getNumber()] = newPage;

    // write evicted page to disk if dirty
    if (pageTable.getEntries()[(int)evictedPage.getPageNumber()].isDirty()){
      // write it to disk
      stats.writesToDisks++;
      pageTable.getEntries()[(int)evictedPage.getPageNumber()].setDirtyBit(false); // clean
    }
  }


  /**
   * Preprocesses the InstructionSet to determine what pages are needed at each cycle.
   **/
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
