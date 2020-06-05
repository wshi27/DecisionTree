import java.util.*;
public class DTMain {

	public static double final_accuracy;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// parameters: train_feature_fname, train_label_fname, 
		// 			   test_feature_fname, test_label_fname,
		//			   max_height(int), max_num_leaves(int)
		
		if(args.length < 6){
			System.out.println("java DTMain train_feature_fname train_label_fname test_feature_fname test_label_fname max_height max_num_leaves");
			return;
		}
		try{
			String train_feature_fname = args[0];
			String train_label_fname = args[1];
			String test_feature_fname = args[2];
			String test_label_fname = args[3];
			int max_height = Integer.parseInt(args[4]);
			int max_num_leaves = Integer.parseInt(args[5]);
			
			DataWrapperClass train_data = new DataWrapperClass(train_feature_fname, train_label_fname);
			DataWrapperClass test_data = new DataWrapperClass(test_feature_fname, test_label_fname);
			
			BonusDecisionTreeClass my_dt = new BonusDecisionTreeClass(train_data, max_height, max_num_leaves);
			//DecisionTreeClass my_dt = new DecisionTreeClass(train_data, max_height, max_num_leaves);
			ArrayList<Integer> prediction = my_dt.predict(test_data);
			
			final_accuracy = DataWrapperClass.accuracy(prediction, test_data.labels);
			System.out.println("Test Accuracy = " + final_accuracy);
			
	    } catch (Exception e) {
	    	System.out.println("NULL: Something is wrong");
	        return;
	    }
		
	}
}
