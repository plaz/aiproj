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

  //Costs
  public static final int FARM_COST=60;
  public static final int HOUSE_COST=30;
  public static final int WORKER_COST=50;

  //Possible states
  public static final int TIMEOUT=0;
  public static final int HOUSE_BUILT=1;
  public static final int FARM_BUILT=2;
  public static final int SHEEP_FOUND=3;
  public static final int WORKER_BUILT=4;
  public static final int FARM_DEAD=5;
  public static final int WAITING = 6;
  public static final int LOOPBACK= 7;
  
  //Output strings and indices
  public static final String names[] = {"wood","food","gold","stone"};
  public static final int WOOD=0;
  public static final int FOOD=1;
  public static final int GOLD=2;
  public static final int STONE=3;
  public static final String directions[] = {"east","north","west","south"};
  
  static int resources[] = new int[4];
  static int pop=0, popLimit=0;
  static int years=0;
  static int state;
  static int cycles;
  static boolean haveIdle;
  static int scoutDir=0;
  static int lastScoutYear=Integer.MAX_VALUE;
  
  
  static int lastHouseStarted=Integer.MAX_VALUE;

  public static void main(String[] args){
    in = new Scanner(System.in);
    cycles = 0;
    
    while(true){  
      if (cycles==0){
	System.out.println("In all prompts, when requested to pause, press 'F3'.");
	System.out.println("Also note that to select the village center, you can press 'h' instead of clicking.");
	System.out.println();
	System.out.println("First, pause the game.");
	System.out.println("Click the menu button in the top-right corner of the screen, select options,");
	System.out.println("and change the resolution to 1280x1024.");
	System.out.println("Click the select idle worker button.");
      } else {
	getState();
	System.out.println();
      }
      cycles++;
      
      getResources();
      System.out.println();
      dump();

      int distance = 1;
      
      for (int i=0; i<5; i++){
	if (decideOnScreenAction()){
	  instruct();
	  break;
	} else {
	  if (i==4){
	    if (desperate()){
	      distance++;
	      i=-1;
	      continue;
	    } else {
	      decideFallBack();
	    }
	    instruct();
	    break;
	  }
	  System.out.println("Move your view to the frame "+distance+" screen "+directions[i]+" of the village center");
	}
      }
      
    }
  }

  public static void instruct(){
    System.out.println();
    scout();
    System.out.println();
    System.out.println("Unpause the game. Then,");
    if (!haveIdle){
      System.out.println("if a message appears on-screen or years left are "+(years-10)+" or less,");
      System.out.println("then click on any open ground, click the idle worker button, pause, and report back.");
      state = WAITING;
    }
    else{
      System.out.println("immediately after performing the above action,");
      System.out.println("pause, click on any open ground, click the idle worker button, and report back.");
      state = LOOPBACK;
    }
    System.out.println();
  }

  public static int getState(){
    if (state == LOOPBACK){
      return LOOPBACK;
    }
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
    haveIdle = false;
    if (state == HOUSE_BUILT
	|| state == WORKER_BUILT
	|| state == FARM_DEAD)
      haveIdle=true;
    else {
      System.out.print("Is a villager selected? ");
      haveIdle = getYesNo();
    }

    if (haveIdle && onScreen("gold")){
      mineGold();
    } else if (haveIdle && onScreen("rock")) {
      mineRock();
    } else if (haveIdle && onScreen("fish")) {
      fish();
    }
  }

  public static boolean decideOnScreenAction(){
    haveIdle = false;
    if (state == HOUSE_BUILT
	|| state == WORKER_BUILT
	|| state == FARM_DEAD)
      haveIdle=true;
    else {
      System.out.print("Is a villager selected? ");
      haveIdle = getYesNo();
    }


    System.out.println();
    if(canBuildHouse()){
      buildHouse();
    } else if(canBuildWorker()){
      buildWorker();
    } else if (haveIdle){ 
      System.out.println("\nClick the select idle worker button.\n");
      if(onScreen("deer")){
	hunt();
      } else if(onScreen("sheep")){
	herd();
      } else if(onScreen("bushes")){
	forage();
      } else if(farmIsPriority() && canBuildFarm()){
	farm();
      } else if(onScreen("trees")){
	chop();
      } else if(canBuildFarm()){
	farm();
      } else{
	return false;
      }
    }

    return true;
  }

  public static boolean canBuildHouse()
  {
    if (pop==popLimit 
	&& popLimit<MAX_POP 
	&& years>MIN_HOUSE_YEARS
	&& resources[WOOD]>=HOUSE_COST
	&& resources[FOOD]>=MIN_HOUSE_FOOD)
    {
      System.out.println("Search revealed area. Is there a partially completed house being worked on? ");
      return !getYesNo();
    }
    else return false;
  }

  public static void buildHouse(){
    if(!haveIdle){
      System.out.println("\nUnpause the game. Wait until any worker approaches the town center.");
      System.out.println("After the worker turns away from the town center, click it to select and repause.");
      System.out.println("Then carry out the following task:\n");
    }
    System.out.println("Click the buildings button, click the house button.");
    System.out.println("Move your view frame to center on the town center, and move the outline of the house");
    System.out.println("to the top left corner of the screen. If it is not red, click to build a house.");
    System.out.println("If it is red, move the outline clockwise around the screen until it turns white, then click.");
    System.out.println("If you return to the top left corner, make the same loop one house-width out.");
    System.out.println("Repeat until success or you leave the revealed area.");
  }

  public static boolean desperate(){
    //Worker
    if (pop<popLimit
	&& resources[FOOD]<WORKER_COST
	&& years >= MIN_WORKER_YEARS+100)
      return true;
    
    //House
    if (popLimit<MAX_POP
	&& resources[WOOD]<HOUSE_COST
	&& years >= MIN_HOUSE_YEARS+100)
      return true;

    return false;
  }


  public static boolean canBuildWorker() {
    if (pop<popLimit
	&& resources[FOOD]>=WORKER_COST
	&& years >= MIN_WORKER_YEARS)
      {
	System.out.print("Select the town center. Is a villager being built? ");
	boolean villagerBuilt = getYesNo();  
	if (villagerBuilt){
	  System.out.println("Click the select idle worker button.");
	}
	return !villagerBuilt;
      }
    else
      return false;
  }

  public static void scout(){
    if (lastScoutYear == Integer.MAX_VALUE)
      {}
    else if (years > (lastScoutYear - 5))
      return;

    System.out.println("Search the revealed area to find and click on the scout.");
    System.out.println("On the minimap, put the point of your cursor on the white dot,");
    System.out.println("Then move the cursor "+directions[scoutDir]+" until the point is parallel to ");
    System.out.println("the "+directions[scoutDir]+"ernmost revealed land.");
    System.out.println("If the tip of the cursor is now off the minimap, move in the reverse direction");
    System.out.println("until it is back on the minimap. Right click to send the scout.");

    scoutDir = (scoutDir+1)%4;
    lastScoutYear = years;
  }

  public static void buildWorker(){
    System.out.println();
    int numWorkers = Math.min(resources[FOOD]/WORKER_COST, popLimit-pop);
    System.out.println("Select the town center and then click the build a villager button "+numWorkers+" times.");
  }

  public static void hunt(){
    System.out.println("Right-click on the deer closest to the selected worker.");
  }

  public static void herd(){
    System.out.println("Right-click on the sheep closest to the selected worker.");
  }
  public static void forage(){
    System.out.println("Right-click on the bush closest to the selected worker.");
  }
  public static void farm(){
    System.out.println("Click the buildings button. Click the farm button.");
    System.out.println("Move your view frame to the town center.");
    System.out.println("Move the outline of the farm over the town center, then move it up just until it no longer is.");
    System.out.println("If the outline is white, click to build the farm.");
    System.out.println("Otherwise, move the outline clockwise around the town center until it becomes white.");
    System.out.println("If you return to where you started, move the outline north and repeat until success.");
  }
  public static void chop(){
    System.out.println("Right-click on the tree closest to the selected worker.");
  }

  public static void mineGold(){
    System.out.println("Right-click on the gold pile closest to the selected worker.");
  }

  public static void mineRock(){
    System.out.println("Right-click on the rock pile closest to the selected worker.");
  }

  public static void fish(){
    System.out.println("Right-click on the fish closest to the selected worker.");
  }

  public static boolean farmIsPriority(){
    return (resources[WOOD]>=FARM_WOOD_THRESH 
	    && years > MIN_FARM_YEARS);
  }

  public static boolean canBuildFarm(){
    return resources[WOOD] > FARM_COST;
  }

  public static boolean onScreen(String thing){
    System.out.print("Are there "+thing+" on the screen? ");
    return getYesNo();
  }

  public static boolean getYesNo(){
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

  public static void getResources(){
    for (int i=0; i<4; i++){
      resources[i]=getNum("Amount of stored "+names[i]+"? ");
    }
    
    pop=getNum("Current population? ");
    popLimit=getNum("Current population limit? ");
    years=getNum("Years remaining? ");
  }

  public static int getNum(String prompt){
    while(true){
      System.out.print(prompt);
      String response = in.nextLine();
      try{
	return Integer.parseInt(response);
      } catch (Exception e) {
	System.out.println("Parsing error. Try again.");
      }
    }
  }

  public static void dump(){
    System.err.println("---------------------------------------");
    for (int i=0; i<4; i++){
      System.err.print(names[i]+":"+resources[i]+" ");
    }
    System.err.println(pop+"/"+popLimit+" "+years);
    System.err.println("Cycles:"+cycles+" Idle:"+haveIdle+" Last scout:"+lastScoutYear);
    System.err.println("---------------------------------------");
  }
}