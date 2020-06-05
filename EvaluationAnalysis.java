import java.io.File;
import java.io.FileWriter;

public class EvaluationAnalysis {

	private static int fixed = 10000;
	private static String[] variable = new String[] {"1","2","3","4","5","6","7","8","9","10",
			"10","20","30","40","50","60","70","80","90","100",
			"110","120","130","140","150","160","170","180","190","200",
			"210","220","230","240","250","260","270","280","290","300",
			"310","320","330","340","350","360","370","380","390","400",
			"410","420","430","440","450","460","470","480","490","500",
			"510","520","530","540","550","560","570","580","590","600",
			"610","620","630","640","650","660","670","680","690","700",
			"710","720","730","740","750","760","770","780","790","800",
			"810","820","830","840","850","860","870","880","890","900",
			"910","920","930","940","950","960","970","980","990","1000",};

	/**
	 * Main method that pass in all the test cases in the Data folder to 
	 * getEvaluationData method and writes the result in a new file
	 * @param args
	 */
	public static void main (String[] args) {
		File dir = new File("C:\\Users\\Winona\\Desktop\\Data Structure\\HW3\\HW3\\Data\\Continuous");

		//list all test files in directory in a File type array
		File[] directoryListing = dir.listFiles();
		//Initialize the result file
		File Continuous_results_variable_height = new File("C:\\Users\\Winona\\Desktop\\Data Structure\\HW3\\HW3\\Data\\Bonus_PerfEvalResult"
				+ "\\Continuous_results_variable_height.txt");
		File Continuous_results_variable_height_time = new File("C:\\Users\\Winona\\Desktop\\Data Structure\\HW3\\HW3\\Data\\Bonus_PerfEvalResult"
				+ "\\Continuous_results_variable_height_time.txt");

		try{
			//Create the result files
			if (Continuous_results_variable_height.createNewFile()){
				System.out.println("File is created!");
			} 
			else {
				System.out.println("File already exists.");
			}
			
			if (Continuous_results_variable_height_time.createNewFile()){
				System.out.println("File is created!");
			} 
			else {
				System.out.println("File already exists.");
			}
			//Writes to result file
			FileWriter writer = new FileWriter(Continuous_results_variable_height);
			FileWriter timeWriter = new FileWriter(Continuous_results_variable_height_time);
			if (directoryListing != null) {
				//pass each test file combinations to getAverageRuntim method
				int i = 0;
				while(i < directoryListing.length) {
					writer.write(directoryListing[i].toString() + "\n");
					timeWriter.write(directoryListing[i].toString() + "\n");
					args = new String[6];
					args[2] = directoryListing[i++].getAbsolutePath();
					args[3] = directoryListing[i++].getAbsolutePath();
					args[0] = directoryListing[i++].getAbsolutePath();
					args[1] = directoryListing[i++].getAbsolutePath();
					args[5] = "10000";
					for(int j = 0; j < variable.length; j++) {
						args[4] = variable[j];
						double runTime = getRunTime(args);
						//writes result to file
						writer.write(Double.toString(DTMain.final_accuracy) + "\n ");
						timeWriter.write(Double.toString(runTime) + "\n");
					}

				}
			}
			writer.close();
			timeWriter.close();
		}catch(Exception e) {
			System.out.println("Oops, writer cannot write, or file wasn't opened.");
		}
	}
	/**
	 * Method that runs DTMain with specific test cases that is 
	 * passed in from main method to calculate the runtime 
	 * @param args
	 * @return average run time
	 */
	private static double getRunTime(String[] args) {
		long startTime = System.currentTimeMillis();
		DTMain.main(args);
		long endTime = System.currentTimeMillis();
		double totalTime = endTime - startTime;
		return totalTime;
	}

}

