import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Scanner;

public class Optimizer {
    abstract static class ObjectiveFunction {
        public abstract double compute(double[] variables);

        public abstract double[] computeGradient(double[] variables);

        public abstract double computeGradientMagnitude(double[] variables);

        public abstract double[] getBounds();

        public abstract String getName();
    }

    public static class QuadraticFunction extends ObjectiveFunction {
        @Override
        public double compute(double[] variables) {
            double obj_func_value = 0;
            for (double variable : variables) {
                obj_func_value += Math.pow(variable, 2);
            }
            return obj_func_value;
        }

        @Override
        public double[] computeGradient(double[] variables) {
            double[] variables_copy = variables.clone();
            for (int i = 0; i < variables_copy.length; i++) {
                variables_copy[i] = variables_copy[i] * 2;
            }
            return variables_copy;
        }

        @Override
        public double computeGradientMagnitude(double[] variables) {
            double[] variables_copy = variables.clone();
            double magnitude = 0;
            for (double variable : variables_copy) {
                magnitude += Math.pow(variable, 2);
            }
            return Math.sqrt(magnitude);
        }

        @Override
        public double[] getBounds() {
            return new double[] { -5, 5 };
        }

        @Override
        public String getName() {
            return "Quadratic";
        }
    }

    public static class RosenbrockFunction extends ObjectiveFunction {
        @Override
        public double compute(double[] variables) {
            double obj_func_value = 0;
            for (int i = 0; i < variables.length - 1; i++) {
                obj_func_value += (100 * Math.pow(variables[i + 1] - Math.pow(variables[i], 2), 2)
                        + Math.pow(1 - variables[i], 2));
            }
            return obj_func_value;
        }

        @Override
        public double[] computeGradient(double[] variables) {
            double[] variables_copy = variables.clone();
            for (int i = 0; i < variables_copy.length; i++) {
                if (i == variables_copy.length - 1) {
                    variables_copy[i] = (200 * (variables[i] - Math.pow(variables[i - 1], 2)));
                } else {
                    variables_copy[i] = (-400 * variables_copy[i]
                            * (variables_copy[i + 1] - Math.pow(variables_copy[i], 2)) - 2 * (1 - variables_copy[i]));
                }
            }
            return variables_copy;
        }

        @Override
        public double computeGradientMagnitude(double[] variables) {
            double[] variables_copy = variables.clone();
            double magnitude = 0;
            for (double variable : variables_copy) {
                magnitude += Math.pow(variable, 2);
            }
            return Math.sqrt(magnitude);
        }

        @Override
        public double[] getBounds() {
            return new double[] { -5, 5 };
        }

        @Override
        public String getName() {
            return "Rosenbrock";
        }
    }

    public static class Rosenbrock_Bonus extends RosenbrockFunction {
        @Override
        public double[] computeGradient(double[] variables) {
            double[] variables_copy = variables.clone();
            for (int i = 0; i < variables_copy.length; i++) {
                if (i == 0) {
                    variables_copy[i] = (-400 * variables_copy[i]
                            * (variables_copy[i + 1] - Math.pow(variables_copy[i], 2)) - 2 * (1 - variables_copy[i]));
                } else if (i == variables_copy.length - 1) {
                    variables_copy[i] = (200 * (variables[i] - Math.pow(variables[i - 1], 2)));
                } else {
                    variables_copy[i] = (-400 * variables_copy[i]
                            * (variables_copy[i + 1] - Math.pow(variables_copy[i], 2)) - 2 * (1 - variables_copy[i])
                            + 200 * (variables[i] - Math.pow(variables[i - 1], 2)));
                }
            }
            return variables_copy;
        }

        @Override
        public String getName() {
            return "Rosenbrock_Bonus";
        }
    }

    public static class SteepestDescentOptimizer {
        public static void optimizeSteepestDescent(ObjectiveFunction objectiveFunction, double[] variables,
                int iterations, double tolerance, double stepSize, int dimensionality, ArrayList<String> outputArray) {
            DecimalFormat df = new DecimalFormat("0.00000");
            int k = iterations;
            // floor variables for rounding to be accurate
            for (int i = 0; i < variables.length; i++) {
                variables[i] = floorTo5Decimals(variables[i]);
            }
            double[] variables_gradient = new double[dimensionality];
            for (int i = 0; i < variables_gradient.length; i++) {
                variables_gradient[i] = floorTo5Decimals(objectiveFunction.computeGradient(variables)[i]);
            }
            double current_tolerance = Double.MAX_VALUE;
            // while loop implements stopping conditions
            while (current_tolerance > tolerance && k > 0) {
                for (int i = 0; i < variables_gradient.length; i++) {
                    variables_gradient[i] = floorTo5DecimalsXValues(objectiveFunction.computeGradient(variables)[i]);
                }
                outputArray.add("Iteration " + (iterations - k + 1) + ":");
                outputArray.add("Objective Function Value: "
                        + df.format(floorTo5Decimals(objectiveFunction.compute(variables))));
                outputArray.add("x-values: " + arrayToString(variables));
                if (iterations - k > 0) {
                    outputArray.add("Current Tolerance: " + df.format(floorTo5Decimals(current_tolerance)));
                }
                for (int i = 0; i < dimensionality; i++) {
                    // Update rule
                    variables[i] = floorTo5Decimals(variables[i] - (stepSize * variables_gradient[i]));
                }
                current_tolerance = objectiveFunction.computeGradientMagnitude(variables_gradient);
                outputArray.add("");
                k--;
            }
            // output last iteration of optimization process
            if (current_tolerance < tolerance) { // could be more efficient
                outputArray.add("Iteration " + (iterations - k + 1) + ":");
                outputArray.add("Objective Function Value: " + floorTo5Decimals(objectiveFunction.compute(variables)));
                outputArray.add("x-values: " + arrayToString(variables));
                if (iterations - k > 0) {
                    outputArray.add("Current Tolerance: " + df.format(floorTo5Decimals(current_tolerance)));
                }
                outputArray.add("");
            }
            // output result messages based on stopping conditions
            if (k == 0) {
                outputArray.add("Maximum iterations reached without satisfying the tolerance.");
                outputArray.add("");
            } else {
                outputArray.add("Convergence reached after " + (iterations - k + 1) + " iterations.");
                outputArray.add("");
            }
            outputArray.add("Optimization process completed.");
        }

        private static int getValidatedInput(Scanner scanner, String prompt) {
            // get validated binary prompts
            int value = Integer.MAX_VALUE;
            try {
                prompt = scanner.next();
                value = Integer.valueOf(prompt);
                if (!(value == 0 || value == 1)) {
                    System.out.println("Please enter a valid input (0 or 1).");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid input (0 or 1).");
            }
            return value;
        }

        private static boolean getValidatedInput(String objectiveFunctionName, ArrayList<String> errorArray) {
            // get validated objective function name
            boolean isValid = true;
            if (!(objectiveFunctionName.toLowerCase().equals("quadratic")
                    || objectiveFunctionName.toLowerCase().equals("rosenbrock")
                    || objectiveFunctionName.toLowerCase().equals("rosenbrock_bonus"))) {
                errorArray.add("Error: Unknown objective function.");
                isValid = false;
            }
            return isValid;
        }

        private static void getValidatedInput(ObjectiveFunction objFunc, int dimensionality, double[] startingPoint,
                ArrayList<String> errorArray) {
            // Validate dimensionality
            if (startingPoint.length != dimensionality) {
                errorArray.add("Error: Initial point dimensionality mismatch.");
            }
            // validate in bounds
            boolean[] inBounds = checkBounds(startingPoint, objFunc.getBounds());
            for (int i = 0; i < inBounds.length; i++) {
                if (!inBounds[i] && errorArray.isEmpty()) {
                    errorArray.add("Error: Initial point " + startingPoint[i] + " is outside the bounds [-5.0, 5.0]");
                }
            }
        }

        private static void getFileInput(Scanner scanner, int txt_or_console_output, ArrayList<String> outputArray,
                ArrayList<String> errorArray) throws Exception {
            System.out.println("Please provide the path to the config file:");
            File file = new File(scanner.next());
            BufferedReader br;
            String line = "";
            try {
                br = new BufferedReader(new FileReader(file));
                line = br.readLine().trim();
                String objectiveFunctionName = line;
                line = br.readLine().trim();
                int dimensionality = Integer.valueOf(line);
                line = br.readLine().trim();
                int iterations = Integer.valueOf(line);
                line = br.readLine().trim();
                double tolerance = Double.valueOf(line);
                line = br.readLine().trim();
                double stepSize = Double.valueOf(line);
                // Create valid initial point array to ensure dimensionality mismatch is
                // validated
                ArrayList<Double> startingPoint = new ArrayList<>();
                String[] stringArray = br.readLine().trim().split(" ");
                for (String s : stringArray) {
                    startingPoint.add(Double.valueOf(s));
                }
                double[] startingPointArray = new double[startingPoint.size()];
                for (int i = 0; i < startingPoint.size(); i++) {
                    startingPointArray[i] = startingPoint.get(i);
                }
                br.close();
                // Check if objective function name is valid
                if (getValidatedInput(objectiveFunctionName, errorArray)) {
                    if (objectiveFunctionName.toLowerCase().equals("quadratic")) {
                        QuadraticFunction objFunc = new QuadraticFunction();
                        getValidatedInput(objFunc, dimensionality, startingPointArray, errorArray);
                    } else if (objectiveFunctionName.toLowerCase().equals("rosenbrock")) {
                        RosenbrockFunction objFunc = new RosenbrockFunction();
                        getValidatedInput(objFunc, dimensionality, startingPointArray, errorArray);
                    } else if (objectiveFunctionName.toLowerCase().equals("rosenbrock_bonus")) {
                        Rosenbrock_Bonus objFunc = new Rosenbrock_Bonus();
                        getValidatedInput(objFunc, dimensionality, startingPointArray, errorArray);
                    }
                    if (!(errorArray.isEmpty())) {
                        outputArray(scanner, 1, errorArray);
                        System.exit(0);
                    } else {
                        printOutput(scanner, objectiveFunctionName, startingPointArray, iterations, tolerance, stepSize,
                                dimensionality, txt_or_console_output, outputArray, errorArray);
                    }
                } else {
                    // Terminate program before taking input for startingPoint if error in objective
                    // function name
                    outputArray(scanner, 1, errorArray);
                }
            } catch (FileNotFoundException e) {
                System.out.println("Error reading the file.");
                System.exit(0);
                e.printStackTrace();
            }
        }

        private static void getManualInput(Scanner scanner, int txt_or_console_output, ArrayList<String> outputArray,
                ArrayList<String> errorArray) {
            System.out.println("Enter the choice of objective function (quadratic or rosenbrock):");
            String objectiveFunctionName = scanner.next();
            System.out.println("Enter the dimensionality of the problem:");
            int dimensionality = scanner.nextInt();
            System.out.println("Enter the number of iterations:");
            int iterations = scanner.nextInt();
            System.out.println("Enter the tolerance:");
            double tolerance = scanner.nextDouble();
            System.out.println("Enter the step size:");
            double stepSize = scanner.nextDouble();
            // validate objective function name
            if (getValidatedInput(objectiveFunctionName, errorArray)) {
                System.out.println("Enter the initial point as " + dimensionality + " space-separated values:");
                ArrayList<Double> startingPoint = new ArrayList<>();
                scanner.nextLine(); // ghost line
                String input = scanner.nextLine();
                String[] stringArray = input.split(" ");
                for (String s : stringArray) {
                    startingPoint.add(Double.valueOf(s));
                }
                double[] startingPointArray = new double[startingPoint.size()];
                for (int i = 0; i < startingPoint.size(); i++) {
                    startingPointArray[i] = startingPoint.get(i);
                }
                printOutput(scanner, objectiveFunctionName, startingPointArray, iterations, tolerance, stepSize,
                        dimensionality, txt_or_console_output, outputArray, errorArray);
            } else {
                // Terminate program before taking input for startingPoint if error in objective
                // function name
                outputArray(scanner, txt_or_console_output, errorArray);
            }
        }

        private static void printOutput(Scanner scanner, String objectiveFunctionName, double[] variables,
                int iterations, double tolerance, double stepSize, int dimensionality, int txt_or_console_output,
                ArrayList<String> outputArray, ArrayList<String> errorArray) {
            // output different calculations based on objective function chosen
            switch (objectiveFunctionName.toLowerCase()) {
                case "quadratic": {
                    QuadraticFunction objFunc = new QuadraticFunction();
                    getValidatedOutput(scanner, objFunc, variables, iterations, tolerance, stepSize, dimensionality,
                            txt_or_console_output, outputArray, errorArray);
                    break;
                }
                case "rosenbrock": {
                    RosenbrockFunction objFunc = new RosenbrockFunction();
                    getValidatedOutput(scanner, objFunc, variables, iterations, tolerance, stepSize, dimensionality,
                            txt_or_console_output, outputArray, errorArray);
                    break;
                }
                case "rosenbrock_bonus": {
                    Rosenbrock_Bonus objFunc = new Rosenbrock_Bonus();
                    getValidatedOutput(scanner, objFunc, variables, iterations, tolerance, stepSize, dimensionality,
                            txt_or_console_output, outputArray, errorArray);
                    break;
                }
            }
        }

        private static void outputArray(Scanner scanner, int txt_or_console_output, ArrayList<String> arrayList) {
            // output to text file
            if (txt_or_console_output == 0) {
                System.out.println("Please provide the path for the output file:");
                File file = new File(scanner.next());
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                    file.createNewFile();
                    for (String s : arrayList) {
                        bw.write(s);
                        bw.newLine();
                    }
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                // output to console
                for (String s : arrayList) {
                    System.out.println(s);
                }
            }
        }

        private static void getValidatedOutput(Scanner scanner, ObjectiveFunction objFunc, double[] variables,
                int iterations, double tolerance, double stepSize, int dimensionality, int txt_or_console_output,
                ArrayList<String> outputArray, ArrayList<String> errorArray) {
            DecimalFormat df = new DecimalFormat("0.00000");
            getValidatedInput(objFunc, dimensionality, variables, errorArray);
            // only output calculations if there are no errors, otherwise output it
            if (!(errorArray.isEmpty())) {
                outputArray(scanner, txt_or_console_output, errorArray);
            } else {
                outputArray.add("Objective Function: " + objFunc.getName());
                outputArray.add("Dimensionality: " + dimensionality);
                outputArray.add("Initial Point: " + arrayToStringNoFloor(variables));
                outputArray.add("Iterations: " + iterations);
                outputArray.add("Tolerance: " + df.format(tolerance));
                outputArray.add("Step Size: " + df.format(stepSize));
                outputArray.add("");
                outputArray.add("Optimization process:");
                optimizeSteepestDescent(objFunc, variables, iterations, tolerance, stepSize, dimensionality,
                        outputArray);
                outputArray(scanner, txt_or_console_output, outputArray);
            }
        }

        private static boolean[] checkBounds(double[] variables, double[] bounds) {
            boolean[] inBounds = new boolean[variables.length];
            for (int i = 0; i < variables.length; i++) {
                if (variables[i] < bounds[0] || variables[i] > bounds[1]) {
                    inBounds[i] = false;
                } else {
                    inBounds[i] = true;
                }
            }
            return inBounds;
        }

        private static String arrayToStringNoFloor(double[] variables) {
            String variablesString = "";
            for (double variable : variables) {
                variablesString += variable + " ";
            }
            return variablesString;
        }

        private static String arrayToString(double[] variables) {
            DecimalFormat df = new DecimalFormat("0.00000");
            String variablesString = "";
            for (double variable : variables) {
                variablesString += df.format(floorTo5DecimalsXValues(variable)) + " ";
            }
            return variablesString;
        }

        private static double floorTo5Decimals(double value) {
            BigDecimal bd = new BigDecimal(value).setScale(5, RoundingMode.FLOOR);
            return bd.doubleValue();
        }

        private static double floorTo5DecimalsXValues(double value) {

            double scale = Math.pow(10, 5);

            return Math.round(value * scale) / scale;

        }
    }

    public static void main(String args[]) {
        Scanner scanner = new Scanner(System.in);
        // Enter or exit program
        int enter_or_exit;
        do {
            System.out.println("Press 0 to exit or 1 to enter the program: ");
            enter_or_exit = SteepestDescentOptimizer.getValidatedInput(scanner, "");
        } while (!(enter_or_exit == 0 || enter_or_exit == 1));
        // Exit if 0
        if (enter_or_exit == 0) {
            System.out.println("Exiting Program...");
            System.exit(0);
        }
        // create output ArrayLists
        ArrayList<String> outputArray = new ArrayList<>();
        ArrayList<String> errorArray = new ArrayList<>();
        // select .txt or manual input
        int txt_or_manual_input;
        do {
            System.out.println("Press 0 for .txt input or 1 for manual input: ");
            txt_or_manual_input = SteepestDescentOptimizer.getValidatedInput(scanner, "");
        } while (!(txt_or_manual_input == 0 || txt_or_manual_input == 1));
        // select .txt or console output
        int txt_or_console_output;
        do {
            System.out.println("Press 0 for .txt output or 1 for console output: ");
            txt_or_console_output = SteepestDescentOptimizer.getValidatedInput(scanner, "");
        } while (!(txt_or_console_output == 0 || txt_or_console_output == 1));
        // decide between manual or file input
        if (txt_or_manual_input == 1) {
            SteepestDescentOptimizer.getManualInput(scanner, txt_or_console_output, outputArray, errorArray);
        } else {
            try {
                SteepestDescentOptimizer.getFileInput(scanner, txt_or_console_output, outputArray, errorArray);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}