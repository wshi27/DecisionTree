import java.util.*;
import java.io.File;
import java.util.Scanner;

public class DataWrapperClass {
	public int num_data;		// number of data (N)
	public int num_features;	// number of features (D)
	public int num_classes;		// number of different classes (K)
	public int num_cont_fea; 	// number of continuous features
	public int num_cat_fea;		// number of categorical features
	public ArrayList<ArrayList<Double> > continuous_features;	// only continuous features
	public ArrayList<ArrayList<Integer> > categorical_features;	// only categorical features
	public ArrayList<Integer> labels;	// labels of all data
	public ArrayList<Integer> labelClassList;
	
	// read features and labels from input files
	public DataWrapperClass(String feature_fname, String label_fname){
		// FILL IN
		// read feature and label file
		// store feature in continuous_/categorical_features, 
		// store labels 
		// if file name starts with 'CAT_', all features are categorical
		// otherwise, all features are continuous
		try {
			File f = new File(feature_fname);
			String type = f.getName();
			Scanner sc = new Scanner(f);
			if(type.contains("CAT")) {
				categorical_features = new ArrayList<ArrayList<Integer>>();
				while(sc.hasNextLine()) {
					ArrayList<Integer> row = new ArrayList<Integer>();
					String line = sc.nextLine();
					String[] stringArray = line.split("\\s+");
					for(int i = 0; i < stringArray.length; i++)
						row.add(Integer.parseInt(stringArray[i]));
					categorical_features.add(row);
				}
				num_data = categorical_features.size();
				num_features = categorical_features.get(0).size();
				num_cat_fea = categorical_features.get(0).size();
			}
			else {
				continuous_features = new ArrayList<ArrayList<Double>>();
				while(sc.hasNextLine()) {
					ArrayList<Double> row = new ArrayList<Double>();
					String line = sc.nextLine();
				
					String[] stringArray = line.split("\\s+", 0);
					for(int i = 0; i < stringArray.length; i++) {
						if("".equals(stringArray[i]))
							continue;
						row.add(Double.parseDouble(stringArray[i]));
					}
					continuous_features.add(row);
				}
				num_data = continuous_features.size();
				num_features = continuous_features.get(0).size();
				num_cont_fea = continuous_features.get(0).size();
			}
			sc.close();
		} catch (Exception e) {
			System.out.println("Something went wrong while reading the feature file.");
		}
		try {
			File f = new File(label_fname);
			Scanner sc = new Scanner(f);

			labels = new ArrayList<Integer>();
			labelClassList = new ArrayList<Integer>() ;
			int i = 0;
			while(sc.hasNext()) {
				i = sc.nextInt();
				labels.add(i);
				if(!labelClassList.contains(i)) {
					labelClassList.add(i);
				}
			}
			num_classes = labelClassList.size();
			sc.close();
		} catch (Exception e) {
			System.out.println("Something went wrong while reading the label file.");
		}
	}

	// static function, compare two label lists, report how many are correct
	public static int evaluate(ArrayList<Integer> l1, ArrayList<Integer> l2){
		int len = l1.size();
		assert len == l2.size();	// length should be equal
		assert len > 0;				// length should be bigger than zero
		int ct = 0;
		for(int i = 0; i < len; ++i){
			if(l1.get(i).equals(l2.get(i))) ++ct;
		}
		return ct;
	}

	// static function, compare two label lists, report score (between 0 and 1)
	public static double accuracy(ArrayList<Integer> l1, ArrayList<Integer> l2){
		int len = l1.size();
		assert len == l2.size();	// label lists should have equal length
		assert len > 0;				// lists should be non-empty
		double score = evaluate(l1,l2);
		score = score / len;		// normalize by divided by the length
		return score;
	}
}
