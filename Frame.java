

public class Frame {

  private Page page;
  private int number;

  public Frame(int frameNum){

    this.number = frameNum;
    this.page = null;
  }

  public Frame(Page page, int frameNum){

    this.number = frameNum;
    this.page = page;
  }

  public void setPage(Page page){

    this.page = page;
  }

  public Page getPage(){

    return this.page;
  }

  public void setNumber(int num){

    this.number = num;
  }

  public int getNumber(){

    return this.number;
  }

  public boolean isOccupied(){

    return this.page != null;
  }



}
