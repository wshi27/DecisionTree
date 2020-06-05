import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class TestMain {
	public static void main(String[] args) {
		if(args.length < 7){
			System.out.println("java TestMain train_feature_fname train_label_fname test_feature_fname test_label_fname max_height max_num_leaves result_fname");
			return;
		}
		try{
			String train_feature_fname = args[0];
			String train_label_fname = args[1];
			String test_feature_fname = args[2];
			String test_label_fname = args[3];
			int max_height = Integer.parseInt(args[4]);
			int max_num_leaves = Integer.parseInt(args[5]);
			String result_fname = args[6];

			DataWrapperClass train_data = new DataWrapperClass(train_feature_fname, train_label_fname);
			DataWrapperClass test_data = new DataWrapperClass(test_feature_fname, test_label_fname);

			DecisionTreeClass my_dt = new DecisionTreeClass(train_data, max_height, max_num_leaves);
			//BonusDecisionTreeClass my_dt = new BonusDecisionTreeClass(train_data, max_height, max_num_leaves);
			ArrayList<Integer> prediction = my_dt.predict(test_data);
	
			

			double final_accuracy = DataWrapperClass.accuracy(prediction, test_data.labels);
			System.out.println("Final accuracy is:" + final_accuracy);
			
			boolean match = my_dt.test(resultData(result_fname));
			if(match == true)
				System.out.println("Success! All test results match!");
			else
				System.out.println("Sorry, some test results do not match :(");

		} catch (Exception e) {
			System.out.println("NULL: Something is wrong while running the TestMain ");
			return;
		}
	}
	public static ArrayList<String> resultData(String fName){
		ArrayList<String> result = new ArrayList<String>();
			try {
				File f = new File(fName);
				String type = f.getName();
				Scanner sc = new Scanner(f);

					while(sc.hasNextLine()) {
						result.add(sc.nextLine());
					}		
				sc.close();
			} catch (Exception e) {
				System.out.println("Something went wrong while reading the result file.");
			}
		return result;
	}
}


