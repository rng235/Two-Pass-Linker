import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Ricardo Guntur <Ricardo.Guntur@nyu.edu>
 * @Professor Allan Gottlieb -- Fall 2015
 * @Course CSCI-UA 202: Operating Systems
 *
 * A two pass Linker.
 * This Two Pass Linker uses scanner standard input. It manipulates the input and formats it properly before
 * storying into a 2D-ArrayList. The linker process the input twice.
 *
 * PASS ONE determines the base address for each module
 * and the absolute address for each external symbol, storing the latter in the symbol table it produces. The first module
 * has base address zero; the base address for module I+1 is equal to the base address of module I plus the length of
 * that module. The absolute address for symbol S defined in module M is the base address of M plus the relative address
 * of S within M.
 *
 * PASS TWO uses the base addresses and the symbol table computed in pass one to generate the actual
 * ouput by relocating relative address and resolving external references. This pass
 * manipulates the data from the first pass to properly deal with immediate operands, absolute, relative,
 * and external address.
 *
 * Error Detection requirements are in the Lab 1 instructions
 * Output is as per Lab 1 instructions
 */

public class Linker{
		
	 static int modCount = 0;  					 								          // Total modules
	 static HashMap<String, Integer> symTable = new HashMap<String, Integer>(); 		  // Stores symbol table in a hashmap
	 static ArrayList listDone = new ArrayList();										  // FINAL memory map list that will be displayed
	 static ArrayList<Integer> modBaseAddress = new ArrayList<Integer>(); 				  // Stores base address at a given module
	 static int addrOffset = 0;						 									  // Offset at any given address
	 static ArrayList<ArrayList<String>> tokenList = new ArrayList<ArrayList<String>>();  // 2-D ArrayList that contains the input in proper format

	/**
	 *
	 * @param fileName: The user input
	 * @return input: If file is found, otherwise print an error and exit
	 */
	 public static Scanner newScanner(String fileName) {
		 try{
			 Scanner input = new Scanner(new BufferedReader(new FileReader(fileName)));
			 return input;
		 }
		 catch(Exception ex) {
			 System.out.printf("Error reading %s\n", fileName);
			 System.exit(0);
		 }
		 return null;
	 }

	/**
	 * The first pass creates the Symbol Table an stores base addresses for pass two.
	 *
	 * @param input
	 */
	 public static void firstPass(Scanner input) {
		 int lineCount = 0; 						 		 // Number of lines
		 int absoluteAddress;        						 // The absolute address of a symbol being defined
		 int inc = -1;
		 int modNum = -1;
		 ArrayList symbolList = new ArrayList<String>();     // List of total symbols
		 ArrayList useList = new ArrayList<String>();
		 ArrayList tokens_1 = new ArrayList<String>();
		 String lineIterate = "";
		 String[] tokens = null;
		 String delims = ("\\s+");					 		 // Delimiter
		 char currentChar;

		 //WHILE not end of file
		 while (input.hasNext()) {
			 
			 //Store string input into an array and splits all white spaces and does not include empty lines
			 String currentLine = input.next();
			 tokens = currentLine.split(delims);			
		 
			 for(int i = 0; i < tokens.length; i++) {		    
				 if(!"".equals(tokens[i])) {					
					 tokens_1.add(tokens[i]);
				 }
			 }
		 }
		 
		 	 //Utilize super complicated 2-D ArrayList approach. Converts tokens_1 that contains whole input as one long string
		     //into proper format such that the first and second pass can recognize it.
		     //There is a much better way of doing this but I didn't turn back
			 for(int i = 0; i < tokens_1.size();) {	
				tokenList.add(new ArrayList<String>());
				int sizeOfLine = 0;
				lineIterate = (String) tokens_1.get(i);										
				int convertedLineIterate = Integer.parseInt(lineIterate);
				
				//Handle Definition Line
				if ((lineCount)%3 == 0 ) {
					sizeOfLine = (convertedLineIterate*2) + 1;
					
					if (sizeOfLine == 0) {
						tokenList.get(lineCount).add("0");
						i++;
					}
					else{
						while (sizeOfLine > 0) {
							tokenList.get(lineCount).add((String) tokens_1.get(i));  
							sizeOfLine--;
							i++;
						}
					}
				}
				//Handle Use Line
				else if ((lineCount-1)%3 == 0) {
					sizeOfLine = (convertedLineIterate);
					
					if (sizeOfLine == 0) {
						tokenList.get(lineCount).add("0");
						i++;
					}
					else {
						while(sizeOfLine > 0) {
							tokenList.get(lineCount).add((String) tokens_1.get(i));
							if(tokens_1.get(i).equals("-1") == true) {
								sizeOfLine--;
							}
							i++;
						}
						
					}	
				}
				//Handle Program Line
				else if ((lineCount-2)%3 == 0) {
					sizeOfLine = convertedLineIterate + 1;
					
					if(sizeOfLine == 0) {
						tokenList.get(lineCount).add("0");
						i++;
					}
						while(sizeOfLine >0) {
							tokenList.get(lineCount).add((String) tokens_1.get(i));
							sizeOfLine--;
							i++;
					}
				}
				lineCount++;
			}
		 	
			 
			 
			  //Print 2d ArrayList
			 	for(int j = 0; j < lineCount; j++)	{					 
			 		for(String s : tokenList.get(j)) {					 			
			 			System.out.print(s + " ");
			 			}
			 		System.out.println("");
			 	 } 
			 	
			 	System.out.println("");
			 	
			 	//Actual First Pass------
			 	for(int j = 0; j < lineCount; j++)	{			 	
		 			//Set 1st address offset to 0
		 			if(j==0) {
		 				modBaseAddress.add(addrOffset);
		 			}
		 			
		 			//Definition Section -- Functioning symbol table
			 		if((j)%3 == 0) {
			 			modNum++;
			 			inc = 0;
			 			for(String currentString : tokenList.get(j)) {
			 				currentChar = tokenList.get(j).get(inc).charAt(0);
			 				inc++;
			 				if (Character.isLetter(currentChar) == true) {
			 					if(!symTable.containsKey(currentString)) {
					 				String symNum = tokenList.get(j).get(inc);
					 				int sizeOfMod = (tokenList.get(j+2).size() - 1);
					 				int convertedInt = Integer.parseInt(symNum);
					 				
					 				if (convertedInt > sizeOfMod) {
					 					System.out.println("Warning: Size of address " + symNum + " is "
					 							+ "larger than the size of module so last address " + sizeOfMod + " is used.");
					 					absoluteAddress = sizeOfMod + (Integer) modBaseAddress.get(modNum);
					 					symTable.put(currentString, absoluteAddress);
					 					symbolList.add(currentString);
					 				}
					 				
					 				else {
					 					absoluteAddress = convertedInt + (Integer) modBaseAddress.get(modNum);
					 					symTable.put(currentString, absoluteAddress);
					 					symbolList.add(currentString);
					 				}	
					 			}
			 					else if (symTable.containsKey(currentString)) {
			 						System.out.println("Error: " + currentString + " is multiply defined; first value is used.");
			 					}
			 				}
			 			}
			 		}
			 	
			 		//Use Section -- Stores all symbols used
			 		if((j-1)%3 == 0) {
			 			for(String currentString : tokenList.get(j)) {
			 				currentChar = currentString.charAt(0);
			 				if (Character.isLetter(currentChar) == true) {
			 					if(!useList.contains(currentString)) {
			 						useList.add(currentString);
			 					}
			 				}
			 			}
			 		}
			 			 			
			 		//Program Test Section
			 		if((j+1)%3 == 0 && j>0) {
			 			String temp = tokenList.get(j).get(0);
			 			char convertThis = temp.charAt(0);
							if (convertThis <= '9' && convertThis >= '0' || convertThis == 45) {
		 					int convertedInt = Integer.parseInt(temp);
		 					addrOffset += convertedInt;
			 				modBaseAddress.add(addrOffset);
			 			}
			 		}
			 	}
		 	
		 	//Tests if a symbol is defined but not used
		 	 for(int i = 0; i < symbolList.size(); i++) {
		 		if(!useList.contains(symbolList.get(i))) {
		 			System.out.println("Warning: " + symbolList.get(i) + " was defined but never utilized.");
		 		}
		 		 
		 	 }
		 	 
	 }//End of firstPass

	/**
	 * Pass Two: Use the base addresses and the symbol table computed in pass one to generate the
	 * ouput by relocating relative address and resolving external references. This pass
	 * manipulates the data from the first pass to properly deal with immediate operands, absolute, relative,
	 * and external address.
	 *
	 */
	 public static void secondPass(Scanner input) {
		 ArrayList programList = new ArrayList<List>();
		 ArrayList useList     = new ArrayList<List>();
		 ArrayList finalList   = new ArrayList();
		 int lineCount	    = 0;
		 int numValues;
		 int address        = 0;
		 int inc 		    = 0;
		 int convertedInt   = 0;
		 int modNum         = 0;
		 int printAddr      = 0;
		 String sentinel = "-1";
		 
		//Loops through each line
		 System.out.println("");
		 for(lineCount = 0; lineCount < tokenList.size(); lineCount++)	{
	 			inc = 0;
	 			
	 		if((lineCount)%3 == 0) {
	 			modNum++;
	 		}

		 	//Use Section. Stores elements of the use section into an array for later use.
		 	if((lineCount-1)%3 == 0) {
		 		for(String currentString : tokenList.get(lineCount)) {
		 			char currentChar = currentString.charAt(0);
		 			
					if ((Character.isLetter(currentChar) && symTable.containsKey(currentString)) == true) {		
							useList.add(currentString);
						}
					
					else if ((Character.isDigit(currentChar) || currentChar == 45) == true) {					
						convertedInt = Integer.parseInt(currentString);
						useList.add(convertedInt);
					}
					
		 			else {
		 				useList.add(currentString);								
		 				System.out.println("Error: " + currentString + " was used but not defined. 111 is used.");
		 				symTable.put(currentString, 111);
					}
				}
		 	}	 		
		 	
		 	//Program Test Section. Parses the words in the program test section and stores it into an array
		 	if((lineCount+1)%3 == 0 && lineCount>0) {
		 		inc = 1;
		 		for(String currentString : tokenList.get(lineCount)) {
		 			if(inc != tokenList.get(lineCount).size()) {
		 			convertedInt = Integer.parseInt(tokenList.get(lineCount).get(inc));
		 			programList.add(convertedInt);
		 			inc++;
		 			}
		 		}
		 	}

            //Runs at the end of each module to modify words and addresses
		 	if((lineCount+1)%3 == 0 && lineCount>0) {
		 		for (int i = 0; i < programList.size(); i++) {
		 			int currentNum = (Integer) programList.get(i);
		 			int lastDigit = (currentNum % 10);
		 			int finalNum 		 = 0;
		 			int symValue 			 = 0; 
		 			int increment  			 = 1;
		 			
		 			//Immediate -- Doesn't change
		 			if (lastDigit == 1) {
		 				finalNum = (currentNum/10);
		 				finalList.add(finalNum);
		 			}	
		 			
		 			//Absolute
		 			if (lastDigit == 2) {
		 				finalNum = (currentNum/10);
		 				if (finalNum%1000 >= 300) {
		 					System.out.println("Error: Absolute address " + finalNum + " exceeds machine size; largest address used.");
		 					finalNum = finalNum - (finalNum%1000);
		 					finalNum = finalNum + 299;
		 				}
		 				finalList.add(finalNum);
		 			}
				 
		 			//Relocate Relative Addresses
		 			if (lastDigit == 3) {
		 				finalNum = (currentNum/10);
		 				if ((finalNum%1000) > modBaseAddress.get(modNum)) {
		 					System.out.println("Error: Relative address " + finalNum + " exceeds module size, largest module size is used ");
		 					finalNum = finalNum/1000;
		 					finalNum = finalNum*1000;
		 					finalNum = (finalNum + modBaseAddress.get(modNum-1));
		 					finalList.add(finalNum);
		 				}
		 				
		 				else {
		 				finalNum = (finalNum + modBaseAddress.get(modNum-1));
		 				finalList.add(finalNum);
		 				}
		 			}
			 
		 			//Resolve External References
		 			if (lastDigit == 4) {
		 				int dec = 0;
		 				
		 				while (increment < useList.size()) { 		 					
		 					if (useList.get(increment).equals(i)) {				
		 						dec = increment;
		 						dec--;
		 						
		 						while((useList.get(dec) instanceof Integer) == true) { 
		 							dec--;
		 						
		 						}
		 						
		 						symValue = symTable.get(useList.get(dec));	
		 						finalNum = (Integer) programList.get(i);
					 			finalNum = (currentNum/10);
					 			finalNum = (finalNum/1000);
								finalNum = (finalNum * 1000);	
			 					finalNum = (symValue + finalNum);
			 					finalList.add(finalNum);
			 					break;
		 					}
		 					increment++;	 					
		 				}
		 			}
		 		}
		 		
                //Catches an error regarding element from use list
		 		int increment = 1;
		 		while(increment < useList.size()) {
		 			if((useList.get(increment) instanceof Integer) == true) {
						if ((Integer) useList.get(increment) > (programList.size())) {
							System.out.println("The address " + useList.get(increment) + " from the use list is greater than the size of the current module. Use ignored.\n");
						}
					}
		 			increment++;
		 		}
                //Clears programList and useList of current module so it can be used by the next module.
		 		programList.clear();
		 		useList.clear();
		 	}
		 }
         //Prints out the finalList formatted.
		 for (int i = 0; i < finalList.size(); i++) {
		 	System.out.printf(String.format("%3d", printAddr));
		 	System.out.print(": " + finalList.get(i));
		 	System.out.println();
		 	printAddr++;
		 }
	 }
	 
    //Prints the symboltable
	public static void printMap(Map<?,?>map) {
		System.out.println();
		System.out.println("  Symbol Table:");
		for (Map.Entry<?,?> entry : map.entrySet()) {
			System.out.println("  " + entry.getKey() + ": " + entry.getValue());
		}
	}
	
     //Uses standard input and is passed into first and second pass.
	 public static void main(String[] args) {
         
         System.out.println("Please enter the input file to be test. ie: input-1.txt");

		 //Handle input
		 Scanner scn = new Scanner(System.in);
         String filename = scn.nextLine();
		 Scanner input = newScanner(filename);

		 //Run first pass
		 firstPass(input);

		 //Print symbol table
		 printMap(symTable);

		 //Run second pass
		 secondPass(input);

		 input.close();
	 }
}


