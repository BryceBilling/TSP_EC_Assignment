import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.Time;
import java.text.*;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.Arrays;
import java.util.ArrayList;
import java.awt.*;


import javax.swing.*;

public class TSP {
    private static int n = 26;
	private static final int cityShiftAmount = 60; //DO NOT CHANGE THIS.
	private static Chromosome pop;
    static Random random = new Random();
    /**
     * How many cities to use.
     */
    protected static int cityCount;

    /**
     * How many chromosomes to use.
     */
    protected static int populationSize = 100; //DO NOT CHANGE THIS.

    /**
     * The part of the population eligable for mating.
     */
    protected static int matingPopulationSize = 1;

    /**
     * The part of the population selected for mating.
     */
    protected static int selectedParents;

    /**
     * The current generation
     */
    protected static int generation;

    /**
     * The list of cities (with current movement applied).
     */
    protected static City[] cities;
    
    /**
     * The list of cities that will be used to determine movement.
     */
    private static City[] originalCities;

    /**
     * The list of chromosomes.
     */
    protected static Chromosome[] chromosomes;

    /**
    * Frame to display cities and paths
    */
    private static JFrame frame;

    /**
     * Integers used for statistical data
     */
    private static double min;
    private static double avg;
    private static double max;
    private static double sum;
    private static double genMin;

    /**
     * Width and Height of City Map, DO NOT CHANGE THESE VALUES!
     */
    private static int width = 600;
    private static int height = 600;


    private static Panel statsArea;
    private static TextArea statsText;


    /*
     * Writing to an output file with the costs.
     */
    private static void writeLog(String content) {
        String filename = "results.out";
        FileWriter out;

        try {
            out = new FileWriter(filename, true);
            out.write(content + "\n");
            out.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /*
     *  Deals with printing same content to System.out and GUI
     */
    private static void print(boolean guiEnabled, String content) {
        if(guiEnabled) {
            statsText.append(content + "\n");
        }

        System.out.println(content);
    }
    //Evolve
    private static void evolve(){
        InverseMutate();
    }
    //Inverse Mutates the parents
    public static void InverseMutate() {
        Chromosome[] children = new Chromosome[populationSize];
        Chromosome.sortChromosomes(chromosomes,populationSize);
        int x = 0;
        int count =0;
        do{
            if(x==chromosomes.length){
                x=0;
            }
            int[] positions = GenerateNumbers();
            float prob = determineProb(x);
            double randomNumber = random.nextDouble();

            int[] citiesToInv = new int[(positions[1] + 1) - positions[0]];
            int j = 0;
            //If it is allowed to mutated i.e. the random number is less than the probability of mutating then inverse mutate and add to children
            if(randomNumber<prob) {
                Chromosome child = new Chromosome(chromosomes[x]);
                //Both for loops allow for mutation by inversion
                for (int i = positions[0]; i <= positions[1]; i++) {
                    citiesToInv[j] = child.getCity(i);
                    j++;
                }
                int k = 0;
                for (int i = positions[1]; i >= positions[0]; i--) {
                    child.setCity(i, citiesToInv[k]);
                    k++;
                }
                children[count] = new Chromosome(child);
                count++;
            }
            x++;
        }while(count<populationSize); // Go until there are as many children as there are parent

        Select(children);
    }
    //Determines the probabilty of mutating a chromosome based on position in the list
    static float determineProb(int x){
        return 1-(float)(x)/100;
    }

    public static void Select(Chromosome[] children){
        Chromosome[] parentsAndChildren = new Chromosome[populationSize*2];
        //Adds parents to the array
        for(int i =0;i < chromosomes.length;i++){
            parentsAndChildren[i] = new Chromosome(chromosomes[i]);
        }
        //Adds children at the end of the array
        for(int i = populationSize;i < parentsAndChildren.length;i++){
            parentsAndChildren[i] = new Chromosome(children[i-populationSize]);
        }
        //Recalculates the cost for all the parents and children as it is a dynamic TSP
        for(int i = 0; i<parentsAndChildren.length; i ++){
            parentsAndChildren[i].calculateCost(cities);
        }
        //Sorts and then takes the top 100 chromosomes of the parent and children for the next generation
        Chromosome.sortChromosomes(parentsAndChildren,parentsAndChildren.length);
        for(int i = 0; i<chromosomes.length;i++){

            chromosomes[i]= parentsAndChildren[i];
        }
    }

    //Helper method that prints the child and parent chromososmes and the points at which they invert
    public static void printChromosomes(Chromosome child, Chromosome parent, int start,int end) {
        System.out.println("Start: "+start+"End: "+end);
        System.out.println("Parent");
        for(int i = 0; i<50;i++){
            System.out.print(i+": "+parent.getCity(i)+ " ");
        }
        System.out.println();
        System.out.println("Child");
        for(int i = 0; i<50;i++){
            System.out.print(i+": "+child.getCity(i)+ " ");
        }
        System.out.println();
    }

    //Generate  2 random numbers for the inversion mutation
    public static int[] GenerateNumbers(){
        Random rand = new Random();
        int end = rand.nextInt(cityCount-2)+1;
        int start =0;
        if(end>1) {
            start = rand.nextInt(end - 1);
        }else{
            start = 0;
        }

        int[] temp = new int[2];
        temp[0]=start;
        temp[1]=end;
        return temp;
    }

    /**
     * Update the display
     */
    public static void updateGUI() {
        Image img = frame.createImage(width, height);
        Graphics g = img.getGraphics();
        FontMetrics fm = g.getFontMetrics();

        g.setColor(Color.black);
        g.fillRect(0, 0, width, height);

        if (true && (cities != null)) {
            for (int i = 0; i < cityCount; i++) {
                int xpos = cities[i].getx();
                int ypos = cities[i].gety();
                g.setColor(Color.green);
                g.fillOval(xpos - 5, ypos - 5, 10, 10);
                
                //// SHOW Outline of movement boundary
                // xpos = originalCities[i].getx();
                // ypos = originalCities[i].gety();
                // g.setColor(Color.darkGray);
                // g.drawLine(xpos + cityShiftAmount, ypos, xpos, ypos + cityShiftAmount);
                // g.drawLine(xpos, ypos + cityShiftAmount, xpos - cityShiftAmount, ypos);
                // g.drawLine(xpos - cityShiftAmount, ypos, xpos, ypos - cityShiftAmount);
                // g.drawLine(xpos, ypos - cityShiftAmount, xpos + cityShiftAmount, ypos);
            }

            g.setColor(Color.gray);
            for (int i = 0; i < cityCount; i++) {
                int icity = chromosomes[0].getCity(i);
                if (i != 0) {
                    int last = chromosomes[0].getCity(i - 1);
                    g.drawLine(
                        cities[icity].getx(),
                        cities[icity].gety(),
                        cities[last].getx(),
                        cities[last].gety());
                }
            }
                        
            int homeCity = chromosomes[0].getCity(0);
            int lastCity = chromosomes[0].getCity(cityCount - 1);
                        
            //Drawing line returning home
            g.drawLine(
                    cities[homeCity].getx(),
                    cities[homeCity].gety(),
                    cities[lastCity].getx(),
                    cities[lastCity].gety());
        }
        frame.getGraphics().drawImage(img, 0, 0, frame);
    }

    private static City[] LoadCitiesFromFile(String filename, City[] citiesArray) {
        ArrayList<City> cities = new ArrayList<City>();
        try 
        {
            FileReader inputFile = new FileReader(filename);
            BufferedReader bufferReader = new BufferedReader(inputFile);
            String line;
            while ((line = bufferReader.readLine()) != null) { 
                String [] coordinates = line.split(", ");
                cities.add(new City(Integer.parseInt(coordinates[0]), Integer.parseInt(coordinates[1])));
            }

            bufferReader.close();

        } catch (Exception e) {
            System.out.println("Error while reading file line by line:" + e.getMessage());                      
        }
        
        citiesArray = new City[cities.size()];
        return cities.toArray(citiesArray);
    }

    private static City[] MoveCities(City[]cities) {
    	City[] newPositions = new City[cities.length];
        Random randomGenerator = new Random();

        for(int i = 0; i < cities.length; i++) {
        	int x = cities[i].getx();
        	int y = cities[i].gety();
        	
            int position = randomGenerator.nextInt(5);
            
            if(position == 1) {
            	y += cityShiftAmount;
            } else if(position == 2) {
            	x += cityShiftAmount;
            } else if(position == 3) {
            	y -= cityShiftAmount;
            } else if(position == 4) {
            	x -= cityShiftAmount;
            }
            
            newPositions[i] = new City(x, y);
        }
        
        return newPositions;
    }

    public static void main(String[] args) {
        DateFormat df = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
        Date today = Calendar.getInstance().getTime();
        String currentTime  = df.format(today);

        int runs;
        boolean display = false;
        String formatMessage = "Usage: java TSP 1 [gui] \n java TSP [Runs] [gui]";

        if (args.length < 1) {
            System.out.println("Please enter the arguments");
            System.out.println(formatMessage);
            display = false;
        } else {

            if (args.length > 1) {
                display = true; 
            }

            try {
                cityCount = 50;
                populationSize = 100;
                runs = Integer.parseInt(args[0]);

                if(display) {
                    frame = new JFrame("Traveling Salesman");
                    statsArea = new Panel();

                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.pack();
                    frame.setSize(width + 300, height);
                    frame.setResizable(false);
                    frame.setLayout(new BorderLayout());
                    
                    statsText = new TextArea(35, 35);
                    statsText.setEditable(false);

                    statsArea.add(statsText);
                    frame.add(statsArea, BorderLayout.EAST);
                    
                    frame.setVisible(true);
                }


                min = 0;
                avg = 0;
                max = 0;
                sum = 0;

                originalCities = cities = LoadCitiesFromFile("CityList.txt", cities);

                writeLog("Run Stats for experiment at: " + currentTime);
                for (int y = 1; y <= runs; y++) {
                    genMin = 0;
                    print(display,  "Run " + y + "\n");

                // create the initial population of chromosomes
                    chromosomes = new Chromosome[populationSize];
                    for (int x = 0; x < populationSize; x++) {
                        chromosomes[x] = new Chromosome(cities);
                    }

                    generation = 0;
                    double thisCost = 0.0;

                    while (generation < 100) {
                        evolve();
                        if(generation % 5 == 0 ) {
                            cities = MoveCities(originalCities); //Move from original cities, so they only move by a maximum of one unit.
                        }
                        generation++;

                        Chromosome.sortChromosomes(chromosomes, populationSize);
                        double cost = chromosomes[0].getCost();
                        thisCost = cost;

                        if (thisCost < genMin || genMin == 0) {
                            genMin = thisCost;
                        }

                        NumberFormat nf = NumberFormat.getInstance();
                        nf.setMinimumFractionDigits(2);
                        nf.setMinimumFractionDigits(2);

                        print(display, "Gen: " + generation + " Cost: " + (int) thisCost);

                        if(display) {
                            updateGUI();
                        }
                    }

                    writeLog(genMin + "");

                    if (genMin > max) {
                        max = genMin;
                    }

                    if (genMin < min || min == 0) {
                        min = genMin;
                    }

                    sum +=  genMin;

                    print(display, "");
                }

                avg = sum / runs;
                print(display, "Statistics after " + runs + " runs");
                print(display, "Solution found after " + generation + " generations." + "\n");
                print(display, "Statistics of minimum cost from each run \n");
                print(display, "Lowest: " + min + "\nAverage: " + avg + "\nHighest: " + max + "\n");

            } catch (NumberFormatException e) {
                System.out.println("Please ensure you enter integers for cities and population size");
                System.out.println(formatMessage);
            }
        }
    }
}