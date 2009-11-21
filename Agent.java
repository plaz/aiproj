import java.util.*;

public class Agent{
  static Scanner in;

  public static final int MAX_POP=25;

  //Cutoff years
  public static final int MIN_HOUSE_YEARS=75;
  public static final int MIN_WORKER_YEARS=40;
  public static final int MIN_FARM_YEARS=30;
  
  //Cutoff resource amounts
  public static final int FARM_WOOD_THRESH=100;
  public static final int MIN_HOUSE_FOOD=50;
  public static final int MIN_PERSON_FOOD=MIN_HOUSE_FOOD;

  //Possible situations
  public static final int TIMEOUT=0;
  public static final int HOUSE_BUILT=1;
  public static final int FARM_BUILT=2;
  public static final int SHEEP_FOUND=3;
  public static final int WORKER_BUILT=4;
  public static final int FARM_DEAD=5;
  
  //Output strings
  public static final String names[] = {"wood","food","gold","stone"};
  public static final String directions[] = {"east","north","west","south"};
  
  static int resources[] = new int[4];
  static int pop=0, popLimit=0;
  static int years=0;
  static int situation;
  static int cycles;
  
  
  static int lastHouseStarted=Integer.MAX_VALUE;

  public static void main(String[] args){
    in = new Scanner(System.in);
    cycles = 0;
    
    while(true){  
      if (cycles!=0){
	getSituation();
	System.out.println();
      }
      cycles++;
      
      getState();
      System.out.println();
      dump();

      
      
      for (int i=0; i<5; i++){
	if (decideOnScreenAction()){
	  System.out.println();
	  instruct();
	  System.out.println();
	  break;
	} else {
	  if (i==4){
	    decideFallBack();
	    System.out.println();
	    instruct();
	    System.out.println();
	    break;
	  }
	  System.out.println("Move your view to the frame one screen "+directions[i]+" of the village center");
	}
      }
      
    }
  }

  public static void instruct(){
    System.out.println("Pause and report back if a message appears on-screen or years left are "+(years-10)+" or less");
  }

  public static int getSituation(){
    System.out.print("Why are we stopped? (m)essage or (t)imeout? ");
    String response = in.nextLine().trim().toLowerCase();
    while (!(response.equals("m") || response.equals("t"))){
      System.out.print("Please respond m/t. ");
      response = in.nextLine().trim().toLowerCase();
    }
    if (response.equals("t"))
      return TIMEOUT;
    else {
      System.out.print("What is the message, (s)heep found, (f)arm built, (h)ouse built, (v)illager created, farm (e)xhausted, or (o)ther? " );
      response = in.nextLine().trim().toLowerCase();
      while (!(response.equals("s") 
	       || response.equals("f") 
	       || response.equals("h") 
	       || response.equals("v") 
	       || response.equals("e") 
	       || response.equals("o"))){
	System.out.print("Please respond s/f/h/v/e/o. ");
	response = in.nextLine().trim().toLowerCase();
      }
      if (response.equals("s"))
	return SHEEP_FOUND;
      else if (response.equals("f"))
	return FARM_BUILT;
      else if (response.equals("h"))
	return HOUSE_BUILT;
      else if (response.equals("v"))
	return WORKER_BUILT;
      else if (response.equals("e"))
	return FARM_DEAD;
      else 
	return TIMEOUT;
    }
  }

  public static void decideFallBack(){
    //TODO Fill method
  }

  public static boolean decideOnScreenAction(){
    
    if(canBuildHouse()){
      buildHouse();
    } else if(canBuildWorker()){
      buildWorker();
    } else if(onScreen("deer")){
      hunt();
    } else if(onScreen("sheep")){
      herd();
    } else if(onScreen("bushes")){
      forage();
    } else if(farmIsPriority() && canBuildFarm()){
      farm();
    } else if(onScreen("wood")){
      chop();
    } else{
      return false;
    }

    return true;
  }

  public static boolean canBuildHouse()
  {
    //FIXME fill method
    return false;
  }
  public static void buildHouse(){
    //FIXME fill method
  }


  public static boolean canBuildWorker()
  {
    //FIXME fill method
    return false;
  }
  public static void buildWorker(){
    //FIXME fill method
  }

  public static void hunt(){
    //FIXME fill method
  }

  public static void herd(){
    //FIXME fill method
  }
  public static void forage(){
    //FIXME fill method
  }
  public static void farm(){
    //FIXME fill method
  }
  public static void chop(){
    //FIXME fill method
  }
  public static boolean farmIsPriority(){
    //FIXME fill method
    return false;
  }

  public static boolean canBuildFarm(){
    //FIXME fill method
    return false;
  }

  public static boolean onScreen(String thing){
    System.out.print("Are there "+thing+" on the screen? ");
    String response = in.nextLine().trim().toLowerCase();
    while (!(response.equals("y") || response.equals("n"))){
      System.out.print("Please respond y/n. ");
      response = in.nextLine().trim().toLowerCase();
    }
    if (response.equals("y"))
      return true;
    else
      return false;
  }

  public static void getState(){
    for (int i=0; i<4; i++){
      System.out.print("Amount of stored "+names[i]+"? ");
      resources[i]=Integer.parseInt(in.nextLine());      
    }
    
    System.out.print("Current population? ");
    pop = Integer.parseInt(in.nextLine());
    
    System.out.print("Current population limit? ");
    popLimit = Integer.parseInt(in.nextLine());

    System.out.print("Years remaining? ");
    years = Integer.parseInt(in.nextLine());
  }

  public static void dump(){
    System.err.println("---------------------------------------");
    for (int i=0; i<4; i++){
      System.err.print(names[i]+":"+resources[i]+" ");
    }
    System.err.println(pop+"/"+popLimit+" "+years);
    System.err.println("Cycles:"+cycles);
    System.err.println("---------------------------------------");
  }
}