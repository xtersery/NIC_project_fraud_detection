

import static java.lang.Math.*;
import java.util.Arrays;
import java.util.Comparator;


public class Main {
    /* Operations:
    0 +
    1 -
    2 *
    3 /
    4 min
    5 max
     */
    public static int populationSize = 1000; // to fine-tune
    public static int numberOfOperations = 6;
    public static int numberOfAttributes = 10;  // to update
    public static int numberOfTrainDataPoints = 1000; // to update
    public static int numberOfTestDataPoints = 1000; // to update
    public static float chanceToBeOperation = 0.6F; // to fine-tune
    public static float chanceNotToMutate = 0.7F; // to fine-tune
    public static float[][] trainAttributes = new float[numberOfTrainDataPoints][numberOfAttributes];
    public static float[][] testAttributes = new float[numberOfTestDataPoints][numberOfAttributes];
    public static boolean[] trainValues = new boolean[numberOfTrainDataPoints];
    public static boolean[] testValues = new boolean[numberOfTestDataPoints];


    public static class Node{
        public int depth;
        public Node parent;
        public Node child1;
        public Node child2;
        public float value;
        public boolean isOperation;
        public boolean isConstant;
        public float score;

        Node(){
            parent = null;
            child1 = null;
            child2 = null;
        }

        public Node copyOf(Node node) {
            if (node == null) {
                return null;
            }
            Node copy = new Node();
            copy.depth = node.depth;
            copy.value = node.value;
            copy.isOperation = node.isOperation;
            copy.isConstant = node.isConstant;
            copy.score = node.score;

            if (node.child1 != null) {
                copy.child1 = copyOf(node.child1);
                copy.child1.parent = copy;
            }
            if (node.child2 != null) {
                copy.child2 = copyOf(node.child2);
                copy.child2.parent = copy;
            }
            return copy;
        }

        Node(int depth){
            this.depth=depth;
            this.child1 = null;
            this.child2 = null;
            if (random()>pow(chanceToBeOperation, depth-2) || depth>10) {
                this.isOperation=false;
                if (random()>0.5) {
                    this.isConstant=false;
                    this.value =(int)(random()*numberOfAttributes);
                } else {
                    this.isConstant=true;
                    this.value = (float) ((random()-0.5)*10);
                }

            } else {
                this.isOperation=true;
                this.isConstant=false;
                this.value=(int)(random()*numberOfOperations);
                haveChildren();
            }
        }

        public void haveChildren(){
            this.child1 = new Node(depth+1);
            this.child2 = new Node(depth +1);
            child1.parent=this;
            child2.parent=this;
        }

        public Node mutate(){
            if (random()>pow(chanceNotToMutate, depth-1) || !this.isOperation){
                return new Node(this.depth);
            } else {
                if (random()>0.5) child1=child1.mutate(); else child2=child2.mutate();
            }
            return this;
        }

        public Node selectBranch(){
            if (random()>pow(0.8, this.depth) || !this.isOperation) {
                return this;
            } else {
                if (random()>0.5) return child1.selectBranch(); else return child2.selectBranch();
            }
        }

        public Node[] crossover(Node parent1, Node parent2) {
            Node copyParent1 = parent1.copyOf(parent1);
            Node copyParent2 = parent2.copyOf(parent2);
            Node branch1 = copyParent1.selectBranch();
            Node branch2 = copyParent2.selectBranch();

            if (branch1.parent != null && branch2.parent != null) {
                if (branch1.parent.child1 == branch1) {
                    branch1.parent.child1 = branch2;
                } else if (branch1.parent.child2 == branch1) {
                    branch1.parent.child2 = branch2;
                }

                if (branch2.parent.child1 == branch2) {
                    branch2.parent.child1 = branch1;
                } else if (branch2.parent.child2 == branch2) {
                    branch2.parent.child2 = branch1;
                }

                Node tempParent = branch1.parent;
                branch1.parent = branch2.parent;
                branch2.parent = tempParent;
            }
            return new Node[]{copyParent1, copyParent2};
        }

        public float value(int line, boolean train){
            if (this.isOperation){
                float v1 = child1.value(line, train);
                float v2 = child2.value(line, train);
                if (this.value==0){
                    return v1+v2;
                } else if (this.value==1) {
                    return v1-v2;
                } else if (this.value==2) {
                    return v1*v2;
                } else if (this.value==3) {
                    if (v2!=0) return v1/v2;
                    return 10000000;
                } else if (this.value==4) {
                    return min(v1, v2);
                } else if (this.value==5) {
                    return max(v1, v2);
                }
            }
            if (this.isConstant) return this.value;
            if (train) return trainAttributes[line][(int)this.value];
            return testAttributes[line][(int)this.value];
        }

        public double evaluation(int line, boolean train){
            double prediction = (1/(1+pow(E, -1*this.value(line, train))));
            double value = 0;
            if (trainValues[line] && train) value=1;
            if (testValues[line] && !train) value=1;
            return (-1)*(value*(log(prediction))+(1-value)*(log(1-prediction)));
        }

        public String toString(){
            if (this.isOperation){
                if (this.value==0){
                    return "+";
                } else if (this.value==1) {
                    return "-";
                } else if (this.value==2) {
                    return "*";
                } else if (this.value==3) {
                    return "/";
                } else if (this.value==4) {
                    return "min";
                } else if (this.value==5) {
                    return "max";
                }
            }
            if (this.isConstant) return Float.toString(this.value);
            return (this.value)+"T";
        }

        public void printTree(int indent) {
            for (int i = 0; i < indent; i++) {
                System.out.print("  ");
            }
            System.out.println(this.toString());
            if (this.child1 != null) {
                this.child1.printTree(indent + 1);
            }
            if (this.child2 != null) {
                this.child2.printTree(indent + 1);
            }
        }


    }

    public static void main(String[] args) {

        // temporary
        // p(y) = (1/(1+pow(E, x0+3x1*x2/x3 + r*-5)));
        for (int i = 0; i< numberOfTrainDataPoints; i++){

            for (int j=0; j<numberOfAttributes; j++){
                trainAttributes[i][j]= (float) (random()*10);
                testAttributes[i][j]= (float) (random()*10);
            }
            trainValues[i]=(random()<(1/(1+pow(E, -1*(trainAttributes[i][0]+3*trainAttributes[i][1]*trainAttributes[i][2]/trainAttributes[i][3] + random()*-5)))));
            testValues[i]=(random()<(1/(1+pow(E, -1*(testAttributes[i][0]+3*testAttributes[i][1]*testAttributes[i][2]/testAttributes[i][3] + random()*-5)))));
        }
        // initialize data before this line

        Node[] population = new Node[populationSize];

        for (int i = 0; i < populationSize; i++) population[i] = new Node(0);

        Node bestTree = null;
        double bestScore = Double.POSITIVE_INFINITY;

        for (int generation = 0; generation < 50; generation++) {
            for (Node tree : population) {
                double score = 0;
                for (int test = 0; test < 50; test++) {
                    int randomLine = (int) (random() * (numberOfTrainDataPoints));
                    score += tree.evaluation(randomLine, true);
                }
                tree.score = (float) score;
            }

            Arrays.sort(population, Comparator.comparingDouble(
                    (Node tree) -> Double.isNaN(tree.score) ? Double.POSITIVE_INFINITY : tree.score
            ));

            if (population[0].score < bestScore) {
                bestScore = population[0].score;
                bestTree = population[0].copyOf(population[0]);
            }

            System.out.println("Generation " + (generation+1) + "; Best score: " + bestScore);

            Node[] newPopulation = new Node[populationSize];
            for (int i = 0; i < populationSize; i += 2) {
                Node parent1 = population[(int)(random()*(populationSize / 2))];
                Node parent2 = population[(int)(random()*(populationSize / 2))];
                Node[] offspring = parent1.crossover(parent1, parent2);
                newPopulation[i] = offspring[0];
                if (i + 1 < populationSize) {
                    newPopulation[i + 1] = offspring[1];
                }
            }

            for (int i = 0; i < populationSize; i++) {
                newPopulation[i] = newPopulation[i].mutate();
            }

            population = newPopulation;
        }

        int TP=0, TN=0, FP=0, FN=0;
        for (int i=0; i<numberOfTestDataPoints; i++){
            assert bestTree != null;
            boolean correct = (bestTree.evaluation(i, false) <= 0.65);
            if (trainValues[i]){
                if (correct) TP++; else FN++;
            } else {
                if (correct) TN++; else FP++;
            }
        }


        double accuracy = (double) (TP + TN) /numberOfTestDataPoints;
        double precision = (double) TP /(TP+FP);
        double recall = (double) TP /(TP+FN);
        double f1Score = 2 * (precision * recall) / (precision + recall);

        System.out.println("Best Tree Evaluation:");
        System.out.println("Accuracy: " + accuracy);
        System.out.println("Precision: " + precision);
        System.out.println("Recall: " + recall);
        System.out.println("F1 Score: " + f1Score);

        assert bestTree != null;
        bestTree.printTree(0);
    }


}