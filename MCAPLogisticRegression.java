// Aditya Prakash	axp171931
import java.util.Set;
import java.util.TreeMap;
import java.util.Scanner;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.HashMap;
import java.io.FileNotFoundException;
import java.io.File;

public class MCAPLogisticRegression 
{
	static Set<String> hamMail = new HashSet<String>();
	static Set<String> spamMail = new HashSet<String>();
	static Set<String> allMail = new HashSet<String>();
	static HashMap<String,Double> weightList = new HashMap<String,Double>();
	public static HashMap<String,Integer> tokenSpam = new HashMap<String,Integer>();
	public static HashMap<String,HashMap<String, Integer>> tokenSpam1 = new HashMap<String,HashMap<String,Integer>>();	
	public static HashMap<String,Integer> tokenHam = new HashMap<String,Integer>();
	public static HashMap<String,HashMap<String, Integer>> tokenHam1 = new HashMap<String,HashMap<String,Integer>>();
	public static Set<String> tokenList = new HashSet<String>();
	public static Set<String> allStopWords = new HashSet<String>(); 
	static double learnRate;
	static double lambda;
	static double w0 = 0.1;
	static int numFiles1 = 0;	//	 Store number of files
	static int numFiles2 = 0;
	static int iterations;
	
	public static void main(String[] args) throws Exception 
	{
		if (args.length != 6) 
		{
			System.out.println("Please provide all the arguments.");
			return;
		}
		String trainLocation = args[0];
		String testLocation = args[1];
		String useStopWords = args[2];
		learnRate = Double.parseDouble(args[3]);
		lambda = Double.parseDouble(args[4]);
		iterations = Integer.parseInt(args[5]);
		File spamTrainFile = new File(trainLocation+"/spam");
		File spamTestFile = new File(testLocation+"/spam");
		File hamTrainFile = new File(trainLocation+"/ham");
		File hamTestFile = new File(testLocation+"/ham");
		checkEMail(hamTrainFile);	//	Get Tokens from training
		checkEMail(spamTrainFile);
		
		if(useStopWords.equals("yes"))	//	 Stop words removal
		{
			Scanner sc5 = null;
			File stopWordFile = new File("stopwords.txt");
			try 
			{
				sc5 = new Scanner(stopWordFile);
			} 
			catch (FileNotFoundException ex4) 
			{
				ex4.printStackTrace();
			}
			while(sc5.hasNext())
			{
				String stopWordEx = sc5.next();
				allStopWords.add(stopWordEx);
			}
			sc5.close();
			for(String stopWordEx:allStopWords)
			{
				if(tokenList.contains(stopWordEx))
				{				
					tokenList.remove(stopWordEx);
				}
			}
		}

		tokenFreqRecord(hamTrainFile,0);	// Get Token and frequency
		tokenFreqRecord(spamTrainFile,1);
		
		train();
		double hamCount=eMailTest(hamTestFile,0,useStopWords);	// Test data
		double spamCount=eMailTest(spamTestFile,1,useStopWords);
		System.out.println("Accuracy: "+((spamCount+hamCount)/(numFiles1+numFiles2))*100);
	}

	private static void checkEMail(File Email) throws Exception 
	{
		for(File file1: Email.listFiles())
		{
			Scanner sc1 = new Scanner(file1);
			while(sc1.hasNext())
			{
				String nextLine = sc1.nextLine();
				for(String sTemp:nextLine.toLowerCase().trim().split(" "))
				{
					sTemp=sTemp.replaceAll("\\.","");		// Remove .
					sTemp=sTemp.replaceAll("[0-9]+","");	// Remove digits
					sTemp=sTemp.replaceAll("\\'","");		// Remove '
					sTemp=sTemp.replaceAll("-","");			// Remove -
					sTemp=sTemp.replaceAll("[+^:,?;=%#&~`$!@*_)/(}{]","");	// Remove special characters
					sTemp=sTemp.replaceAll("'s","");  		// Remove 's
            		sTemp=sTemp.replaceAll("\\<.*?>","");	// Remove Tags
					if(!sTemp.isEmpty())
					{
						tokenList.add(sTemp);
					}
				}
			}
			sc1.close();
		}
	}
	
	private static void tokenFreqRecord(File eMailFile,int classification) throws Exception 
	{
		if(classification==0) // Ham
		{
			for(File file1:eMailFile.listFiles())
			{
				Scanner sc5 = new Scanner(file1);
				allMail.add(file1.getName());
				hamMail.add(file1.getName());
				HashMap<String,Integer> hMap2 = new HashMap<String,Integer>();
				while(sc5.hasNext())
				{
					String nextLine = sc5.nextLine();
					for(String sTemp:nextLine.toLowerCase().trim().split(" "))
					{
						sTemp=sTemp.replaceAll("\\.","");		// Remove .
						sTemp=sTemp.replaceAll("[0-9]+","");	// Remove digits
						sTemp=sTemp.replaceAll("\\'","");		// Remove '
						sTemp=sTemp.replaceAll("-","");			// Remove -
						sTemp=sTemp.replaceAll("[+^:,?;=%#&~`$!@*_)/(}{]","");	// Remove special characters
						sTemp=sTemp.replaceAll("'s","");  		// Remove 's
						sTemp=sTemp.replaceAll("\\<.*?>","");	// Remove Tags
						
						if(!sTemp.isEmpty())
						{
							if(tokenList.contains(sTemp))
							{
								if(tokenHam.containsKey(sTemp))
								{
									tokenHam.put(sTemp,tokenHam.get(sTemp)+1);
								}
								else
								{
									tokenHam.put(sTemp,1);
								}
							}	
						}	

						if(!sTemp.isEmpty())
						{
							if(tokenList.contains(sTemp))
							{
								if(hMap2.containsKey(sTemp))
								{
									hMap2.put(sTemp,hMap2.get(sTemp)+1);
								}
								else
								{
									hMap2.put(sTemp,1);
								}
							}
						}
						tokenHam1.put(file1.getName(),hMap2);
					}
				}
				sc5.close();
			}
		}
		else //	Spam
		{
			for(File file1:eMailFile.listFiles())
			{
				Scanner sc4 = new Scanner(file1);
				allMail.add(file1.getName());
				spamMail.add(file1.getName());
				HashMap<String,Integer> hMapI = new HashMap<String,Integer>();
				while(sc4.hasNext())
				{
					String nextLine = sc4.nextLine();
					for(String sTemp:nextLine.toLowerCase().trim().split(" "))
					{
						sTemp=sTemp.replaceAll("\\.","");		// Remove .
						sTemp=sTemp.replaceAll("[0-9]+","");	// Remove digits
						sTemp=sTemp.replaceAll("\\'","");		// Remove '
						sTemp=sTemp.replaceAll("-","");			// Remove -
						sTemp=sTemp.replaceAll("[+^:,?;=%#&~`$!@*_)/(}{]","");	// Remove special characters
						sTemp=sTemp.replaceAll("'s","");  		// Remove 's
						sTemp=sTemp.replaceAll("\\<.*?>","");	// Remove Tags
						
						if(tokenList.contains(sTemp))
						{
							if(tokenSpam.containsKey(sTemp))
							{
								tokenSpam.put(sTemp,tokenSpam.get(sTemp)+1);
							}
							else
							{
								tokenSpam.put(sTemp,1);
							}
							if(hMapI.containsKey(sTemp))
							{
								hMapI.put(sTemp,hMapI.get(sTemp)+1);
							}
							else
							{
								hMapI.put(sTemp,1);
							}
						}
						tokenSpam1.put(file1.getName(),hMapI);
					}
				}
				sc4.close();
			}
		}
	}
		
	public static void train() // Randomize Token weight
	{
		for(String temp:tokenList)
		{
			double weight =  2*Math.random()-1;
			weightList.put(temp,weight);
		}
		for(String temp:tokenList)
		{
			double countE = 0;
			for(String docName:allMail)
			{
				int count = getTokenCount(docName,temp);
				double targetValue;
				if(spamMail.contains(docName))
				{
					targetValue = 1;	//	Spam
				}
				else
				{
					targetValue = 0;	//	Ham
				}
				double out1 = fileOutput(docName);
				double netE = targetValue - out1;
				countE = countE + count*netE;
			}
			double newWeight = weightList.get(temp) + learnRate*(countE - (lambda*weightList.get(temp)));
			weightList.put(temp,newWeight);
		}
	}
	
	private static double fileOutput(String docName) 
	{
		if(spamMail.contains(docName))
		{
			double output1 = w0;
			try
			{
				for(Entry<String,Integer> fn:tokenSpam1.get(docName).entrySet())
				{
					output1 += (fn.getValue()*weightList.get(fn.getKey()));
				}	
			}
			catch(Exception ex3)
			{
				ex3.printStackTrace();
			}
			return (startLogisticRegression(output1));
		}
		else
		{
			double output2 = w0;
			try
			{
				for(Entry<String,Integer> e3:tokenHam1.get(docName).entrySet())
				{
					output2 += (e3.getValue()*weightList.get( e3.getKey()));
				}	
			}
			catch(Exception ex4)
			{
				ex4.printStackTrace();	
			}
			return(startLogisticRegression(output2));
		}
	}
	
	public static double eMailTest(File eMailFile,int classification,String useStopWords) throws Exception
	{
		if(classification==0)	// Ham
		{
			numFiles1 = eMailFile.listFiles().length;
			int classCount1 = 0;
			for(File file1:eMailFile.listFiles())
			{
				Scanner sc3 = new Scanner(file1);
				HashMap<String,Integer> hashMap1 = new HashMap<String,Integer>();
				while(sc3.hasNext())
				{
					String nextLine = sc3.nextLine();
					for(String sTemp:nextLine.toLowerCase().trim().split(" "))
					{
						sTemp=sTemp.replaceAll("\\.","");		// Remove .
						sTemp=sTemp.replaceAll("[0-9]+","");	// Remove digits
						sTemp=sTemp.replaceAll("\\'","");		// Remove '
						sTemp=sTemp.replaceAll("-","");			// Remove -
						sTemp=sTemp.replaceAll("[+^:,?;=%#&~`$!@*_)/(}{]","");	// Remove special characters
						sTemp=sTemp.replaceAll("'s","");  		// Remove 's
						sTemp=sTemp.replaceAll("\\<.*?>","");	// Remove Tags

						if(hashMap1.containsKey(sTemp))
						{
							hashMap1.put(sTemp,hashMap1.get(sTemp)+1);
						}
						else
						{
							hashMap1.put(sTemp,1);
						}
					}	
				}
				int result = test(hashMap1);
				sc3.close();
				if(result == 0)
				{
					classCount1++;
				}
			}
			return classCount1;
		}
		else // Spam
		{
			int classCount = 0 ;
			for(File file1:eMailFile.listFiles())
			{
				Scanner sc2 = new Scanner(file1);
				HashMap<String,Integer> hashMap1 = new HashMap<String,Integer>();
				numFiles2++;
				while(sc2.hasNext())
				{
					String nextLine = sc2.nextLine();
					for(String sTemp:nextLine.toLowerCase().trim().split(" "))
					{
						sTemp=sTemp.replaceAll("\\.","");		// Remove .
						sTemp=sTemp.replaceAll("[0-9]+","");	// Remove digits
						sTemp=sTemp.replaceAll("\\'","");		// Remove '
						sTemp=sTemp.replaceAll("-","");			// Remove -
						sTemp=sTemp.replaceAll("[+^:,?;=%#&~`$!@*_)/(}{]","");	// Remove special characters
						sTemp=sTemp.replaceAll("'s","");  		// Remove 's
						sTemp=sTemp.replaceAll("\\<.*?>","");	// Remove Tags
						
						if(hashMap1.containsKey(sTemp))
						{
							hashMap1.put(sTemp,hashMap1.get(sTemp)+1);
						}
						else
						{
							hashMap1.put(sTemp,1);
						}
					}	
				}
				sc2.close();
			
				if(useStopWords.equals("yes"))
				{
					for(String stopWordI:allStopWords)
					{
						if(hashMap1.containsKey(stopWordI))
						{
							hashMap1.remove(stopWordI);
						}
					}
				}
				int result = test(hashMap1);
				if(result == 1)
				{
					classCount++;
				}
			}
			return classCount;
		}
	}
	
	public static int test(HashMap<String,Integer> hashMap1) 
	{
		double result = 0;
		for(Entry<String,Integer> e4:hashMap1.entrySet())
		{
			if(weightList.containsKey(e4.getKey()))
			{
				result += (e4.getValue()*weightList.get(e4.getKey()));
			}
		}
		result+=w0;
		if(result>=0)
		{
			return 1;
		}
		else
		{	
			return 0;
		}
	}
	
	private static int getTokenCount(String docName, String temp) 
	{
		int count = 0;
		if(spamMail.contains(docName))
		{
			try 
			{
				for(Entry<String,Integer> e1:tokenSpam1.get(docName).entrySet())
				{
					if(e1.getKey().equals(temp))
					{
						count = e1.getValue();
						return count;
					}
				}
			} 
			catch (Exception ex1) 
			{	
				ex1.printStackTrace();
			}
		}
		else if(hamMail.contains(docName))
		{
			try 
			{
				for(Entry<String,Integer> e2:tokenHam1.get(docName).entrySet())
				{
					if(e2.getKey().equals(temp))
					{
						count = e2.getValue();
						return count;
					}
				}
			} 
			catch (Exception ex2) 
			{	
				ex2.printStackTrace();
			}
		}
		return 0;
	}

	private static double startLogisticRegression(double output3) 
	{
		if(output3>100)	//	 Not taking high values for exponents
		{
			return 1.0;
		}
		else if(output3<-100)
		{
			return 0.0;
		}
		else
		{
			return (1.0/(1.0 + Math.exp(-output3)));
		}
	}
}	
