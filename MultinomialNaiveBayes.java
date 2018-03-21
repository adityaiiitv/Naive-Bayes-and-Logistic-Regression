//	Aditya Prakash		axp171931
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Scanner;
import java.util.HashSet;

public class MultinomialNaiveBayes 
{
	//	Conditional Probabilities
	static HashMap<String,Double> condSpamProb = new HashMap<String,Double>();
	static HashMap<String,Double> condHamProb = new HashMap<String,Double>();
	static int spamSum=0;	//	Sum count
	static int hamSum=0;
	public static TreeMap <String,Integer> spamTokens = new TreeMap<String,Integer>(); // Span Tokens
	public static TreeMap<String,Integer> hamTokens = new TreeMap<String,Integer>(); // Ham Tokens
	static double priorSpamProb=0.0;// Prior of spam
	static double priorHamProb =0.0;// Prior of Ham
	public static Set<String> allStopwords = new HashSet<String>();
	public static Set<String> tokenList = new HashSet<String>();
	
	public static void main(String[] args) throws Exception
	{
		if(args.length!=3)
		{
			System.out.println("Please provide all arguments.");
			return;
		}
		// Get data sets
		String trainFileLocation = args[0];
		String testFileLocation = args[1];
		String useStopWords = args[2];
		File stopWordsFile = new File("stopwords.txt");
		File fileTestSpam = new File(testFileLocation+"/spam");
		File fileTestHam = new File(testFileLocation+"/ham");
		File fileTrainSpam = new File(trainFileLocation+"/spam");
		File fileTrainHam = new File(trainFileLocation+"/ham");
		readMail(fileTrainSpam);
		readMail(fileTrainHam);
		
		if(useStopWords.equals("yes"))	// If yes to stopword removal
		{
			try 
			{
				Scanner s3=null;
				s3 = new Scanner(stopWordsFile);
				while(s3.hasNext())
				{
					String sTemp1 = s3.next();
					allStopwords.add(sTemp1);
				}
				s3.close();
				for(String sTemp2:allStopwords)
				{	
					if(tokenList.contains(sTemp2))
					{
						tokenList.remove(sTemp2);
					}
				}
			} 
			catch (FileNotFoundException e1) 
			{
				e1.printStackTrace();
			}
		}
		storeTokenFreq(fileTrainSpam,1);
		storeTokenFreq(fileTrainHam,0);
		Train(fileTrainSpam,fileTrainHam);
		double accuracy=0.0;
		double totalCount=fileTrainSpam.listFiles().length+fileTrainHam.listFiles().length;
		double numberOfHam = eMailTest(fileTestHam,useStopWords,0);
		double numberOfSpam = eMailTest(fileTestSpam,useStopWords,1);
		accuracy=((double)numberOfHam+(double)numberOfSpam)/totalCount;
		System.out.println("Accuracy: "+accuracy);
	}
	
	public static int startNaiveBayes(File f6, double priorHamProb, double priorSpamProb, Set<String> stopWords, String useSW) throws Exception 
	{
		double hamDecide = 0;
		double spamDecide = 0;
		Scanner s3 = new Scanner(f6);
		while(s3.hasNext())
		{
			String nextLine = s3.nextLine();
            if(useSW.equals("yes"))	// Yes to stopword removal
            {
            	for(String sTemp:nextLine.toLowerCase().split(" "))
				{
					sTemp=sTemp.replaceAll("\\.","");		// Remove .
					sTemp=sTemp.replaceAll("[0-9]+","");	// Remove digits
					sTemp=sTemp.replaceAll("\\'","");		// Remove '
					sTemp=sTemp.replaceAll("-","");			// Remove -
					sTemp=sTemp.replaceAll("[+^:,?;=%#&~`$!@*_)/(}{]","");	// Remove special characters
					sTemp=sTemp.replaceAll("'s","");  		// Remove 's
            		sTemp=sTemp.replaceAll("\\<.*?>","");	// Remove Tags
					
            		if(!stopWords.contains(sTemp))
            		{
						if(condHamProb.containsKey(sTemp))
    					{
    						hamDecide = hamDecide + condHamProb.get(sTemp);
    					}
    					else
    					{
    						hamDecide = hamDecide + Math.log(1.0/(hamSum+tokenList.size()+1.0));
    					}
    					if(condSpamProb.containsKey(sTemp))
    					{
    						spamDecide = spamDecide + condSpamProb.get(sTemp);
    					}
    					else
    					{
							spamDecide = spamDecide + Math.log(1.0/(spamSum+tokenList.size()+1.0));
    					}
            		}
    			}
            }
            else
            {
            	for(String sTemp:nextLine.toLowerCase().split(" "))
				{
            		sTemp=sTemp.replaceAll("\\.","");		// Remove .
					sTemp=sTemp.replaceAll("[0-9]+","");	// Remove digits
					sTemp=sTemp.replaceAll("\\'","");		// Remove '
					sTemp=sTemp.replaceAll("-","");			// Remove -
					sTemp=sTemp.replaceAll("[+^:,?;=%#&~`$!@*_)/(}{]","");	// Remove special characters
					sTemp=sTemp.replaceAll("'s","");  		// Remove 's
            		sTemp=sTemp.replaceAll("\\<.*?>","");	// Remove Tags
					
					if(condHamProb.containsKey(sTemp))
   					{
    					hamDecide = hamDecide + condHamProb.get(sTemp);
    				}
    				else
    				{
    					hamDecide = hamDecide + Math.log(1.0/(hamSum+tokenList.size()+1.0));
    				}
					if(condSpamProb.containsKey(sTemp))
    				{
    					spamDecide = spamDecide + condSpamProb.get(sTemp);
    				}
    				else
    				{
   						spamDecide = spamDecide + Math.log(1.0/(spamSum+tokenList.size()+1.0)) ;
   					}
    			}	
    		}
        }
		s3.close();
		hamDecide = hamDecide + priorHamProb;
		spamDecide = spamDecide + priorSpamProb;

		if(hamDecide > spamDecide)
		{
			return 0;	// Spam
		}
		else
		{
			return 1;	// Ham
		}
	}
	
	private static void readMail(File eMail) throws Exception 
	{
		for(File f3:eMail.listFiles())
		{
			Scanner s2 = new Scanner(f3);
			while(s2.hasNext())
			{
				String nextLine = s2.nextLine();
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
			s2.close();
		}
	}
	
	// Store Token frequencies
	private static void storeTokenFreq(File eMail,int classification) throws Exception 
	{
		if(classification==0)	// Ham
		{
			for(File f2:eMail.listFiles())
			{
				Scanner s2 = new Scanner(f2);
				while(s2.hasNext())
				{
					String nextLine = s2.nextLine();
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
								if(hamTokens.containsKey(sTemp))
								{
									hamTokens.put(sTemp,hamTokens.get(sTemp)+1);
								}
								else
								{
									hamTokens.put(sTemp,1);
								}
							}
						}	
					}
				}
				s2.close();
			}
		}
		else //	Spam
		{
			for(File f1:eMail.listFiles())
			{
				Scanner s1 = new Scanner(f1);
				while(s1.hasNext())
				{
					String nextLine = s1.nextLine();
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
								if(spamTokens.containsKey(sTemp))
								{
									spamTokens.put(sTemp,spamTokens.get(sTemp)+1);
								}	
								else
								{
									spamTokens.put(sTemp,1);
								}
							}
						}
					}
				}
				s1.close();
			}		
		}
	}
	
	public static void Train(File fileTrainSpam,File fileTrainHam) throws Exception
	{
		double priorH = 1.0 - priorSpamProb;
		priorHamProb = Math.log(priorH);
		double priorS = 1.0*(fileTrainSpam.listFiles().length)/(fileTrainSpam.listFiles().length+fileTrainHam.listFiles().length);
		priorSpamProb = Math.log(priorS);
		tokenFrequency(1);
		tokenFrequency(0);
		
		for(String sTemp:tokenList)
		{
			if(hamTokens.containsKey(sTemp))
			{
				double dTemp1 = (hamTokens.get(sTemp)+1.0)/(hamSum+tokenList.size()+1.0);
				double dTemp2 = Math.log(dTemp1);
				condHamProb.put(sTemp,dTemp2);
			}
		}
		for(String sTemp:tokenList)
		{
			if(spamTokens.containsKey(sTemp))
			{
				double dTemp1 = (spamTokens.get(sTemp)+1.0)/(spamSum+tokenList.size()+1.0);
				double dTemp2 = Math.log(dTemp1);
				condSpamProb.put(sTemp,dTemp2);
			}			
		}
	}
	
	public static void tokenFrequency(int classification)
	{
		if(classification==0)	//	Ham
		{
			for(Entry<String,Integer> e2:hamTokens.entrySet())
			{
				hamSum = hamSum + e2.getValue();
			}
		}
		else // Spam
		{
			for(Entry<String,Integer> e1:spamTokens.entrySet())
			{
				spamSum = spamSum + e1.getValue();
			}
		}
	}
	
	public static double eMailTest(File eMail,String useStopWords,int classification) throws Exception
	{
		if(classification==0)	// Ham
		{
			int nHam=0;
			double numberOfHam =0;
			for(File f5:eMail.listFiles())
			{
				if(startNaiveBayes(f5,priorHamProb,priorSpamProb,allStopwords,useStopWords)==0)
				{
					numberOfHam += 1.0;
					nHam += 1;
				}
			}
			return numberOfHam;
		}
		else //	Spam
		{
			int nSpam=0;
			double numberOfSpam =0;
			for(File f4:eMail.listFiles())
			{
				if(startNaiveBayes(f4,priorHamProb,priorSpamProb,allStopwords,useStopWords)==1)
				{
					numberOfSpam += 1.0;
					nSpam += 1;
				}
			}
			return numberOfSpam;
		}
	}
}
