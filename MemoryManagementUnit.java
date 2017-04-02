
public class MemoryManagementUnit{


  public MemoryManagementUnit(){

  }

  public long[] getTranslation(long virtualAddress, int pageSize){

    long[] translations = new long[2];

    long pageNumber =  (virtualAddress / pageSize);
    long pageOffset =  (virtualAddress % pageSize);

    //System.out.println("Page Number: " + pageNumber);
    //System.out.println("Page Offset: " + pageOffset);

    translations[0] = pageNumber;
    translations[1] = pageOffset;

    return translations;
  }


 public static void main(String[] args){

    MemoryManagementUnit mmu = new MemoryManagementUnit();

    long[] translations = mmu.getTranslation(10145020, 4000);

    System.out.println(translations[0] + " " + translations[1]);
  }

}
