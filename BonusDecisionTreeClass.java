import java.util.*;

public class BonusDecisionTreeClass {
	private class DecisionTreeNode{
		public ArrayList<Integer> data_list; // list of data IDs
		public int opt_fea_type = -1;	// 0 if continuous, 1 if categorical
		public int opt_fea_id = -1;		// the index of the optimal feature
		public double opt_fea_thd = Double.NEGATIVE_INFINITY;	// the optimal splitting threshold 
		// for continuous feature
		public double opt_improvement = Integer.MIN_VALUE; // the improvement if split based on the optimal feature
		public boolean is_leaf = true;		// is it a leaf
		public int majority_class = -1;		// class prediction based on majority vote
		public int num_accurate = -1;
		public int split_class_id = -1;// number of accurate data using majority_class
		public int height = 0;
		public DecisionTreeNode parent = null;		// parent node
		public ArrayList<DecisionTreeNode> children; 	// list of children when split
		
		//DecisionTreeNode constructor
		public DecisionTreeNode(ArrayList<Integer> d_list, int m_class, int n_acc){
			data_list = new ArrayList<Integer>(d_list);
			majority_class = m_class;
			num_accurate = n_acc;
		}
	}

	public DataWrapperClass train_data;
	public int max_height;
	public int max_num_leaves;
	public int height;
	public int num_leaves;
	public DecisionTreeNode root;
	public HeapPriorityQueue<Double, DecisionTreeNode> heap = 
			new HeapPriorityQueue<Double, DecisionTreeNode>();

	// constructor, build the decision tree using train_data, max_height and max_num_leaves
	public BonusDecisionTreeClass(DataWrapperClass t_d, int m_h, int m_n_l){
		train_data = t_d;
		max_height = m_h;
		max_num_leaves = m_n_l;

		// create the root node, store all data_ids [0..N-1] in data_list
		ArrayList<Integer> idxList = new ArrayList<Integer>();
		for(int i = 0; i < train_data.labels.size(); i++)
			idxList.add(i);
		root = new DecisionTreeNode(idxList, -1, -1);
		//set opt_feature_type based for the root node
		if(train_data.categorical_features != null)
			root.opt_fea_type = 1;
		else
			root.opt_fea_type = 0;
		
		findOptFeature(root);
		splitNextNode();
	}
/*
 * helper method to find the optimum feature to split for each node
 */
	private void findOptFeature (DecisionTreeNode currNode) {
		// find the majority class in the labels given, also how many accurate using the majority class
		int[] labelCount = new int[20];
		for(int idx: currNode.data_list) {
			labelCount[train_data.labels.get(idx)] +=1;
		}
		//set values for majority_class and num_accurate for the current node
		for(int j = 0; j < labelCount.length; j++) {
			if(labelCount[j] > currNode.num_accurate) {
				currNode.majority_class = j;
				currNode.num_accurate = labelCount[j];
			}
		}

		// if the majority class is correct for all data, 
		//		no improvement possible 
		// 		the optimal accuracy improvement = 0
		if(currNode.num_accurate >= currNode.data_list.size())
			currNode.opt_improvement = 0;
		// else 
		//		find the optimal feature to split
		// 		for each feature 
		//			if categorical
		//			  	split the data_list into sub-lists using different values of the feature 
		//				for each sub-list
		//					find the majority class
		//					compute the number of accurate prediction using this majority class
		//				sum up number of accurate predictions for all sub-lists as the score
		else {
			double currImpScore = 0;
			double currThreshold = 0;
			for(int feature = 0; feature < train_data.num_features; feature++) {
				double score = 0;
				if(currNode.opt_fea_type == 1) {
					//for each categorical feature, count the class labels based on different values of feature
					int[][] freCount = frequencyCount(currNode.data_list, train_data.categorical_features, feature,
							train_data.labels);
					//find the best score on this feature
					for (int r = 0; r < freCount.length; r++) {
						int higherNum = 0;
						for(int c = 0; c < freCount[r].length; c++) {
							if(freCount[r][c] > higherNum)
								higherNum = freCount[r][c];
						}
						score += higherNum;
					}
				}
				//	if continuous
				//		sort the data based on the continuous feature		
				//		find the optimal threshold to split the data_list into two sub-lists
				//		for each of sub-list
				//			find the majority class
				//			compute the number of accurate prediction using this majority class
				//		sum up number of accurate predictions for all sub-lists as the score
				if(currNode.opt_fea_type == 0) {
					//for each continuous data, keep a running count of the class labels at all possible thresholds
					double [][] sortedFeature = sortFeature(currNode.data_list, train_data.continuous_features, feature, 
							train_data.labels, train_data.num_classes);

					//do another running count of the class labels by going up the list, this holds the number accurate below all possible thresholds
					int i = sortedFeature.length - 1;
					int thisLabel = (int)sortedFeature[i][2];
					//store the count up data 20 idx away from the count down data, the 20 offset is to account for extra label classes
					int offset = 20;
					//array used to keep count of label classes when feature values are the same 
					int [] SVLabelCount = new int[15];

					while(i > 0) {
						thisLabel = (int)sortedFeature[i][2];

						//when same feature values exist, keep running count the same as the previous in sortedFeature array
						//and add the total count of label classes at the last entry of the same feature values
						if(sortedFeature[i][0] == sortedFeature[i-1][0]) {
							++SVLabelCount[thisLabel];
							for(int label: train_data.labelClassList) {
								//if the first element of array is part of the same feature values, start the count as 0 
								if(i == sortedFeature.length - 1)
									sortedFeature[i][label + offset] = 0;
								else
									sortedFeature[i][label + offset] = sortedFeature[i+1][label + offset];
							}
						}
						//this marks the last entry of the same feature value
						else if(i != sortedFeature.length-1 && sortedFeature[i][0] == sortedFeature[i+1][0]) {
							++SVLabelCount[thisLabel];
							for(int label: train_data.labelClassList) 
								sortedFeature[i][label + offset] = sortedFeature[i+1][label + offset] + SVLabelCount[label];
							//reset SVLabelCount for the next same feature values
							for(int j = 0; j < SVLabelCount.length; j++) {
								SVLabelCount[j] = 0;
							}
						}
						//if feature values are not the same, perform increment the count as normal
						else {
							for(int label: train_data.labelClassList) {
								//initialize the count at the beginning of array when there's no previous count to refer to
								if(i == sortedFeature.length -1) {
									if(label == thisLabel) 
										++sortedFeature[i][label + offset];
								}
								else {
									if(label != thisLabel)
										sortedFeature[i][label + offset] = sortedFeature[i+1][label + offset];
									else
										sortedFeature[i][label + offset] = sortedFeature[i+1][label + offset] + 1;
								}
							}
						}
						i--;
					}// end while

					//find the best threshold to split by adding the higher number of each row for both the down and up count
					//and store the running maximum in the score variable and the threshold in currThreshold 
					int higherNumAbove, higherNumBelow; 
					double higherDeciAbove, higherDeciBelow;
					for(int z = 0; z < sortedFeature.length-1; z++) {
						higherNumAbove = 0;
						higherDeciAbove = 0;
						higherNumBelow = 0;
						higherDeciBelow = 0;
						for(int label2: train_data.labelClassList) {
							if(sortedFeature[z][label2+3] > higherNumBelow) {
								higherNumBelow = (int)sortedFeature[z][label2+3];
								higherDeciBelow = higherNumBelow/(double)(z+1);
							}
							if(sortedFeature[z+1][label2 + offset] > higherNumAbove) {
								higherNumAbove = (int)sortedFeature[z+1][label2 + offset];
								higherDeciAbove = higherNumAbove/(double)(sortedFeature.length - z);
							}
						}
						if(((higherDeciAbove + higherDeciBelow)/2) > score) {
							score = (higherDeciAbove + higherDeciBelow)/2;
							currThreshold = sortedFeature[z][0] - 0.00001;
						}
					}
				}
				// 		find the feature with the largest score (best total num of accurate prediction after splitting)
				// 		optimal accuracy improvement = the difference between the best total num of accurate prediction after splitting
				//								and the number of accurate prediction using the majority class of the current node
				// put the root node and the optimal accuracy improvement into a max-heap
				if(currNode.opt_fea_type == 1)
					currImpScore = Math.max((score - currNode.num_accurate), 0);
				else
					currImpScore = Math.max((score - (currNode.num_accurate/(double)currNode.data_list.size())), 0);
				if(currImpScore > currNode.opt_improvement) {
					currNode.opt_improvement = currImpScore;
					if(currNode.opt_improvement > 0) {
						currNode.opt_fea_id = feature;
						if(currNode.opt_fea_type == 0)
							currNode.opt_fea_thd = currThreshold;
					}

				}
			}
		}
		heap.insert(currNode.opt_improvement, currNode);
	}
	
/*
 * helper method to split a node when opt_improvment is greater than 0
 */
	private void splitNextNode () {
		// while the heap is not empty
		// 		extract the maximum entry (the leaf node with the maximal optimal accuracy improvement) from the heap
		while(!heap.isEmpty()) {
			DecisionTreeNode nextNode = heap.max().getValue();
			//if the optimal accuracy improvement is zero (no improvement possible)
			//	break;
			if(nextNode.opt_improvement == 0) 
				break;
			//else 
			//	split the node
			//	create children based on the optimal feature and split (each sub-list creates one child)
			//		for each child node
			//			find its optimal accuracy improvement (and the optimal feature) (as you do for the root)
			//			put the node into the max-heap
			else {
				heap.removeMax();
				nextNode.is_leaf = false;
				@SuppressWarnings("unchecked")
				ArrayList<Integer>[] childSubLists= new ArrayList[20];
				
				//if categorical, split the data according to different values of the opt_feature, opt_feature was found 
				//previously by helper method findOptFeature
				if(nextNode.opt_fea_type == 1) {
					int subListRowIdx;
					for(int rowIdx: nextNode.data_list) {
						subListRowIdx = train_data.categorical_features.get(rowIdx).get(nextNode.opt_fea_id);
						if(childSubLists[subListRowIdx] == null)
							childSubLists[subListRowIdx] = new ArrayList<Integer>();
						childSubLists[subListRowIdx].add(rowIdx);
					}
				}
				//if continuous, split the data according to threshold. All data less than threshold is held in
				//childSubList 0, and all data more than threshold is held in childSubList 1
				else {
					double value;
					childSubLists[0] = new ArrayList<Integer>();
					childSubLists[1] = new ArrayList<Integer>();
					for(int rowIdx: nextNode.data_list) {
						value = train_data.continuous_features.get(rowIdx).get(nextNode.opt_fea_id);
						if(value < nextNode.opt_fea_thd) 
							childSubLists[0].add(rowIdx);
						else
							childSubLists[1].add(rowIdx);
					}
				}
				//create a child node for each subsets of the data and link with parent node
				DecisionTreeNode childNode;
				nextNode.children = new ArrayList<DecisionTreeNode>();
				for(int i = 0; i < childSubLists.length; i++) {
					if(childSubLists[i] == null)
						continue;
					childNode = new DecisionTreeNode(childSubLists[i], -1, -1);
					childNode.split_class_id = i;
					childNode.height = nextNode.height + 1;
					childNode.parent = nextNode;
					nextNode.children.add(childNode);
					childNode.opt_fea_type = nextNode.opt_fea_type;
					findOptFeature(childNode);
				}
			}
			//	if the number of leaves > max_num_leaves
			//		break;
			num_leaves = heap.size();
			if(num_leaves > max_num_leaves)
				break;
			//	if the height > max_height
			//		break;
			height = nextNode.height+1;
			if(height > max_height)
				break;
		}
	}
	/*
	 * Helper method for categorical data. Method counts the number of different labels that corresponds to different feature categories 
	 */
	private int[][] frequencyCount(ArrayList<Integer> indices, ArrayList<ArrayList<Integer>> data, int dataCol,
			ArrayList<Integer> labelsList) {
		//get the number of columns needed by obtaining the largest value in label file
		int col = 0;
		for(int i = 0; i < train_data.labelClassList.size(); i++) {
			if( train_data.labelClassList.get(i) > col)
				col = train_data.labelClassList.get(i) + 1;
		}
		//using array column index as the label header, count the number of labels that correspond to each feature value
		int[][] countArray= new int[20][col];
		for(int rowIdx: indices) {
			int r = data.get(rowIdx).get(dataCol);
			int c = labelsList.get(rowIdx);
			countArray[r][c]++;
		}
		return countArray;
	}
/*
 * Helper method for continuous data. Method sorts the data according its feature values, and keep a count of number accurate going
 * down the list
 */
	private double[][] sortFeature(ArrayList<Integer> indices, ArrayList<ArrayList<Double>> data, int dataCol,
			ArrayList<Integer> labelsList, int col){
		//picked column length of 43 to account for extra label classes 
		double[][] result = new double[indices.size()][43];
		
		//sort the feature values by entering all entries into a Heap priority queue and extracting the max value one by one
		HeapPriorityQueue<Double, Integer> sortHeap = new HeapPriorityQueue<Double, Integer>();
		for(int idx: indices) 
			sortHeap.insert((Double)data.get(idx).get(dataCol), idx);

		//col 0 stores feature value, col 1 stores data_list idx, col 2 stores the corresponding label, col 3 to 19 stores the down counts
		int[] SVLabelCount = new int[15];
		int i = 0;
		result[i][0] = sortHeap.max().getKey();
		result[i][1] = sortHeap.removeMax().getValue();
		int thisLabel = labelsList.get((int) result[i][1]);
		result[i][2] = thisLabel;
		result[i][thisLabel + 3]++;
		while(!sortHeap.isEmpty()) {
			i++;
			result[i][0] = sortHeap.max().getKey();
			result[i][1] = sortHeap.removeMax().getValue();	
			thisLabel = labelsList.get((int) result[i][1]);
			result[i][2] = thisLabel;
			
			//if same feature values exist, keep counts the same as previous, and the total count at the last same feature value entry
			if(!sortHeap.isEmpty() && result[i][0] == sortHeap.max().getKey()) {
				//update previous count to be 0 if previous is the first entry in the array
				if(i == 1 && result[i][0] == result[i-1][0]) {
					result[i-1][(int)result[i-1][2] + 3] = 0;
				}
				for(int label: train_data.labelClassList) 
					result[i][label + 3] = result[i-1][label +3];
				SVLabelCount[thisLabel]++;
			}
			//add the total count of the same feature values at the last entry of same feature value
			else if(result[i][0] == result[i-1][0]) {
				SVLabelCount[thisLabel]++;
				for(int label: train_data.labelClassList) {
					result[i][label+3] = result[i-1][label+3] + SVLabelCount[label];
				}

				//reset SVLabelCount for the next same feature values
				for(int z = 0; z < SVLabelCount.length; z++) {
					SVLabelCount[z] = 0;
				}
			}
			//if feature values are not the same, continue count down normally
			else {
				for(int label: train_data.labelClassList) {
					if(label != thisLabel)
						result[i][label + 3] = result[i-1][label +3];
					else
						result[i][label + 3] = result[i-1][label + 3] + 1;
				}
			}
		}

		return result;		
	}
/*
 * method that predicts the label when given a set of test data.
 */
	public ArrayList<Integer> predict(DataWrapperClass test_data){
		// for each data in the test_data
		//	   starting from the root,
		//	   at each node, go to the right child based on the splitting feature
		//	   continue until a leaf is reached
		//	   assign the label to the data based on the majority-class of the leaf node
		// return the list of predicted label
		ArrayList<Integer> predictedLabels = new ArrayList<Integer>();
		for(int i = 0; i < test_data.labels.size(); i++) {
			int prediction;
			if (test_data.categorical_features != null) 
				prediction = predictHelper(test_data.categorical_features.get(i), root);
			else{
				prediction = contPredictHelper(test_data.continuous_features.get(i), root);
			}
			predictedLabels.add(prediction);
		}
		return predictedLabels;
	}
/*
 * predict helper method to use for categorical data. This method traverse the decision tree by finding the feature value from test data
 *  using Opt_fea_id, and matching that value with the childIdx of the node's children. Return the majority class as the answer once a 
 *  leaf is reached
 */
	private int predictHelper(ArrayList<Integer> list, DecisionTreeNode node) {
		if(node.is_leaf)
			return node.majority_class;

		int col = node.opt_fea_id;
		int val = list.get(col);
		int childIdx = -1;
		for(int i = 0; i < node.children.size(); i++) {
			if(node.children.get(i).split_class_id == val) {
				childIdx = i;
				break;
			}
		}
		if(childIdx != -1)
			return predictHelper(list, node.children.get(childIdx));
		return node.majority_class;
	}
/*
 * predict helper method to use for continuous data. This method traverse the decision tree by finding the feature value from test data
 * using opt_fea_id, and comparing it with the opt_fea_thd. If less than, go to child 0, if greater than go to child 1. Returns the 
 * majority class as the answer once a leaf is reached
 */
	private int contPredictHelper(ArrayList<Double> list, DecisionTreeNode node) {
		if(node.is_leaf) 
			return node.majority_class;
		
		int col = node.opt_fea_id;
		double val = list.get(col);
		if(val < node.opt_fea_thd)
			return contPredictHelper(list, node.children.get(0));
		else
			return contPredictHelper(list, node.children.get(1));
	}
	
/*
 * test method for TestMain class. Since DecisionTreeNode root is private and can only be called in this class, this method calls the testHelper
 * method and pass in the results text document data and the DecisionTreeNode root
 */
	public boolean test(ArrayList<String> args) {
		return testHelper(args, root);
	}
/*
 * helper method for test method . Used to find all necessary info. from all nodes in the final decision tree and compare it with the results
 * text document in the TestCases folder. Returns true if all info matches
 */
	private boolean testHelper(ArrayList<String> resultList, DecisionTreeNode node) {
	
		if(node.opt_fea_id != Integer.parseInt(resultList.remove(0))) 
			return false;


		if(node.is_leaf != Boolean.parseBoolean(resultList.remove(0))) 
			return false;

		if(node.majority_class != Integer.parseInt(resultList.remove(0)))
			return false;

		if(node.num_accurate != Integer.parseInt(resultList.remove(0))) 
			return false;
		
		if(node.opt_fea_type == 1) {

			if(node.split_class_id != Integer.parseInt(resultList.remove(0)))
				return false;
		}
		else {

			if(node.opt_fea_thd != Double.parseDouble(resultList.remove(0)))
				return false;
		}


		if(node.height != Integer.parseInt(resultList.remove(0)))
			return false;
		
		//if a node has no more children, a leaf is reached. If this matches the result, return true
		if(node.children == null) {

			if(Integer.parseInt(resultList.remove(0)) != 0)
				return false;
			return true;
		}


		if(node.children.size() != Integer.parseInt(resultList.remove(0))) 
			return false;

		for(DecisionTreeNode child: node.children) {
			if(testHelper(resultList, child) == false)
				return false;
		}
		return true;
	}
}

