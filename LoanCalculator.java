import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.geometry.HPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.*;
import java.util.Arrays;
import javafx.collections.FXCollections;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import java.util.function.*;
import java.io.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

class Loan {
  private double annualInterestRate;
  private int numberOfMonths;
  private double loanAmount;

  public Loan(double annualInterestRate, int numberOfMonths, double loanAmount) {
    this.annualInterestRate = annualInterestRate;
    this.numberOfMonths = numberOfMonths;
    this.loanAmount = loanAmount;
  }

  public double getMonthlyPaymentA() {
    double monthlyInterestRate = annualInterestRate / 1200;
    double k = 
      (monthlyInterestRate * Math.pow((1 + monthlyInterestRate), numberOfMonths)) / (Math.pow((1 + monthlyInterestRate), numberOfMonths) - 1);
    return k * loanAmount;
  }

  public double getTotalPaymentA() {
    return getMonthlyPaymentA() * numberOfMonths;
  }

  public double getCreditL() {
    return loanAmount / numberOfMonths;
  }

  public double getCreditA() {
    return loanAmount / numberOfMonths;
  }

  public double getInterestA() {
    return loanAmount * (annualInterestRate / 1200);
  }

  public double getLoanLeftA(int month) {
    return loanAmount - (getCreditA() * (month - 1));
  }

  public double getLoanLeftL(int month) {
    return loanAmount - (loanAmount / numberOfMonths * (month - 1));
  }

  public double getMonthlyPaymentL(int month) {
    return getCreditL() + getLoanLeftL(month) * (annualInterestRate / 1200);
  }

  public double getInterestL(int month) {
    return getLoanLeftL(month) * (annualInterestRate / 1200);
  }

  public double getTotalPaymentL() {
    double totalPaymentL = 0;
    for(int i = 1; i <= numberOfMonths; i++) {
      totalPaymentL += getMonthlyPaymentL(i);
    }
    return totalPaymentL;
  }
}

public class LoanCalculator extends Application {
  private TextField tfAnnualInterestRate = new TextField();
  private TextField tfNumberOfMonths = new TextField();
  private TextField tfLoanAmount = new TextField();
  private RadioButton rbLinear = new RadioButton("Linear");
  private RadioButton rbAnnuity = new RadioButton("Annuity");
  private TextField tfMonthlyPayment = new TextField();
  private TextField tfTotalPayment = new TextField();
  private Button btShow = new Button("Show");
  private Button btGraph = new Button("Graph");
  private TextField tfFrom = new TextField();
  private TextField tfTo = new TextField();
  private Button btFilter = new Button("Filter");
  private Button btSaveToFile = new Button("Save to file");
  private String resultText = new String();
  
  @Override
  public void start(Stage primaryStage) {
    GridPane gridPane = new GridPane();
    gridPane.setHgap(15);
    gridPane.setVgap(15);
    gridPane.add(new Label("Annual Interest Rate:"), 0, 0);
    gridPane.add(tfAnnualInterestRate, 1, 0);
    gridPane.add(new Label("Number of Months:"), 0, 1);
    gridPane.add(tfNumberOfMonths, 1, 1);
    gridPane.add(new Label("Loan Amount:"), 0, 2);
    gridPane.add(tfLoanAmount, 1, 2);
    gridPane.add(rbLinear, 0, 3);
    gridPane.add(rbAnnuity, 1, 3);
    gridPane.add(btShow, 1, 4);
    gridPane.add(btGraph, 2, 4);

    gridPane.setAlignment(Pos.CENTER);
    tfAnnualInterestRate.setAlignment(Pos.BOTTOM_RIGHT);
    tfNumberOfMonths.setAlignment(Pos.BOTTOM_RIGHT);
    tfLoanAmount.setAlignment(Pos.BOTTOM_RIGHT);

    GridPane.setHalignment(rbLinear, HPos.RIGHT);
    GridPane.setHalignment(btShow, HPos.RIGHT);
    GridPane.setHalignment(btGraph, HPos.RIGHT);

    ToggleGroup radioGroup = new ToggleGroup();
    rbLinear.setToggleGroup(radioGroup);
    rbAnnuity.setToggleGroup(radioGroup);

    btShow.setOnAction(e -> show());
    btGraph.setOnAction(e -> graph());

    Scene scene = new Scene(gridPane, 400, 250);
    primaryStage.setTitle("LoanCalculator by Agota");
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  public void show() {
    double interest = Double.parseDouble(tfAnnualInterestRate.getText());
    int months = Integer.parseInt(tfNumberOfMonths.getText());
    double loanAmount = Double.parseDouble(tfLoanAmount.getText());

    Loan loan = new Loan(interest, months, loanAmount);
    Stage showStage = new Stage();
    GridPane gridPaneShow = new GridPane();
    gridPaneShow.setHgap(20);
    gridPaneShow.setVgap(50);
    gridPaneShow.add(new Label("Show from: "), 1, 0);
    gridPaneShow.add(tfFrom, 2, 0);
    tfFrom.setMaxWidth(40);
    gridPaneShow.add(new Label("Show to: "), 3, 0);
    gridPaneShow.add(tfTo, 4, 0);
    tfTo.setMaxWidth(40);
    gridPaneShow.add(btFilter, 5, 0);
    gridPaneShow.add(btSaveToFile, 6, 0);

    if(rbLinear.isSelected()) {
      gridPaneShow.add(new Text("Linear " + interest + "% " + months + " months " + loanAmount + " money"), 0, 0);
      gridPaneShow.add(new Text("Month"), 1, 1);
      gridPaneShow.add(new Text("Loan Left"), 2, 1);
      gridPaneShow.add(new Text("Credit"), 3, 1);
      gridPaneShow.add(new Text("Interest"), 4, 1);
      gridPaneShow.add(new Text("Monthly Payment"), 5, 1);
      resultText += "Month       Loan Left       Credit          Interest        Monthly Payment \r\n";
      int i, j;
      for(i = 1, j = 2; i <= months; i++, j++) {
        if(i <= months) {
          gridPaneShow.add(new Text("" + i), 1, j);
          gridPaneShow.add(new Text(String.format("$%.2f", loan.getLoanLeftL(i))), 2, j);
          gridPaneShow.add(new Text(String.format("$%.2f", loan.getCreditL())), 3, j);
          gridPaneShow.add(new Text(String.format("$%.2f", loan.getInterestL(i))), 4, j);
          gridPaneShow.add(new Text(String.format("$%.2f", loan.getMonthlyPaymentL(i))), 5, j);
          resultText += "" + i + "           " + String.format("$%.2f", loan.getLoanLeftL(i)) +
            "      " + String.format("$%.2f", loan.getCreditL()) + "       " +
            String.format("$%.2f", loan.getInterestL(i)) + "        " +
            String.format("$%.2f", loan.getMonthlyPaymentL(i)) + "\r\n";
        } else {
          gridPaneShow.add(new Text("" + i), 1, j);
          gridPaneShow.add(new Text("loan paid"), 2, j);
        }
      }
      gridPaneShow.add(new Text("Total Payment: "), 4, j+1);
      gridPaneShow.add(new Text(String.format("$%.2f", loan.getTotalPaymentL())), 5, j+1);
      resultText += "TotalPayment: " + String.format("$%.2f", loan.getTotalPaymentL());
    }

    if(rbAnnuity.isSelected()) {
      gridPaneShow.add(new Text("Annuity " + interest + "% " + months + " months " + loanAmount + " money"), 0, 0);
      gridPaneShow.add(new Text("Month"), 1, 1);
      gridPaneShow.add(new Text("Loan Left"), 2, 1);
      gridPaneShow.add(new Text("Credit"), 3, 1);
      gridPaneShow.add(new Text("Interest"), 4, 1);
      gridPaneShow.add(new Text("Monthly Payment"), 5, 1);
      resultText += "Month       Loan Left       Credit          Interest        Monthly Payment \r\n";
      int i, j;
      for(i = 1, j = 2; i <= months; i++, j++) {
        if(i <= months) {
          gridPaneShow.add(new Text("" + i), 1, j);
          gridPaneShow.add(new Text(String.format("$%.2f", loan.getLoanLeftA(i))), 2, j);
          gridPaneShow.add(new Text(String.format("$%.2f", loan.getCreditA())), 3, j);
          gridPaneShow.add(new Text(String.format("$%.2f", loan.getInterestA())), 4, j);
          gridPaneShow.add(new Text(String.format("$%.2f", loan.getMonthlyPaymentA())), 5, j);
          resultText += "" + i + "           " + String.format("$%.2f", loan.getLoanLeftA(i)) +
            "      " + String.format("$%.2f", loan.getCreditA()) + "       " +
            String.format("$%.2f", loan.getInterestA()) + "        " +
            String.format("$%.2f", loan.getMonthlyPaymentA()) + "\r\n";
        } else {
          gridPaneShow.add(new Text("" + i), 1, j);
          gridPaneShow.add(new Text("loan paid"), 2, j);
        }
      }
      gridPaneShow.add(new Text("Total Payment: "), 4, j+1);
      gridPaneShow.add(new Text(String.format("$%.2f", loan.getTotalPaymentA())), 5, j+1);
      resultText += "TotalPayment: " + String.format("$%.2f", loan.getTotalPaymentA());
    }

    btSaveToFile.setOnAction(e -> saveToFile(resultText));

    btFilter.setOnAction(e -> {
      gridPaneShow.getChildren().clear();
      gridPaneShow.add(new Label("Show from: "), 1, 0);
      gridPaneShow.add(tfFrom, 2, 0);
      gridPaneShow.add(new Label("Show to: "), 3, 0);
      gridPaneShow.add(tfTo, 4, 0);
      gridPaneShow.add(btFilter, 5, 0);
      gridPaneShow.add(btSaveToFile, 6, 0);
      int from = Integer.parseInt(tfFrom.getText());
      int to = Integer.parseInt(tfTo.getText());

      if(rbLinear.isSelected()) {
      gridPaneShow.add(new Text("Linear " + interest + "% " + months + " months " + loanAmount + " money"), 0, 0);
      gridPaneShow.add(new Text("Month"), 1, 1);
      gridPaneShow.add(new Text("Loan Left"), 2, 1);
      gridPaneShow.add(new Text("Credit"), 3, 1);
      gridPaneShow.add(new Text("Interest"), 4, 1);
      gridPaneShow.add(new Text("Monthly Payment"), 5, 1);
      int i, j;
      for(i = from, j = 2; i <= to; i++, j++) {
        if(i <= months) {
          gridPaneShow.add(new Text("" + i), 1, j);
          gridPaneShow.add(new Text(String.format("$%.2f", loan.getLoanLeftL(i))), 2, j);
          gridPaneShow.add(new Text(String.format("$%.2f", loan.getCreditL())), 3, j);
          gridPaneShow.add(new Text(String.format("$%.2f", loan.getInterestL(i))), 4, j);
          gridPaneShow.add(new Text(String.format("$%.2f", loan.getMonthlyPaymentL(i))), 5, j);
        } else {
          gridPaneShow.add(new Text("" + i), 1, j);
          gridPaneShow.add(new Text("loan paid"), 2, j);
        }
      }
      gridPaneShow.add(new Text("Total Payment: "), 4, j+1);
      gridPaneShow.add(new Text(String.format("$%.2f", loan.getTotalPaymentL())), 5, j+1);
    }

    if(rbAnnuity.isSelected()) {
      gridPaneShow.add(new Text("Annuity " + interest + "% " + months + " months " + loanAmount + " money"), 0, 0);
      gridPaneShow.add(new Text("Month"), 1, 1);
      gridPaneShow.add(new Text("Loan Left"), 2, 1);
      gridPaneShow.add(new Text("Credit"), 3, 1);
      gridPaneShow.add(new Text("Interest"), 4, 1);
      gridPaneShow.add(new Text("Monthly Payment"), 5, 1);
      int i, j;
      for(i = from, j = 2; i <= to; i++, j++) {
        if(i <= months) {
          gridPaneShow.add(new Text("" + i), 1, j);
          gridPaneShow.add(new Text(String.format("$%.2f", loan.getLoanLeftA(i))), 2, j);
          gridPaneShow.add(new Text(String.format("$%.2f", loan.getCreditA())), 3, j);
          gridPaneShow.add(new Text(String.format("$%.2f", loan.getInterestA())), 4, j);
          gridPaneShow.add(new Text(String.format("$%.2f", loan.getMonthlyPaymentA())), 5, j);
        } else {
          gridPaneShow.add(new Text("" + i), 1, j);
          gridPaneShow.add(new Text("loan paid"), 2, j);
        }
      }
      gridPaneShow.add(new Text("Total Payment: "), 4, j+1);
      gridPaneShow.add(new Text(String.format("$%.2f", loan.getTotalPaymentA())), 5, j+1);
    }
    });

    Scene showScene = new Scene(new ScrollPane(gridPaneShow), 1000, 500);
    showStage.setTitle("LoanCalculator by Agota SHOW");
    showStage.setScene(showScene);
    showStage.show();
  }

  public void graph() {
    double interest = Double.parseDouble(tfAnnualInterestRate.getText());
    int months = Integer.parseInt(tfNumberOfMonths.getText());
    double loanAmount = Double.parseDouble(tfLoanAmount.getText());

    Loan loan = new Loan(interest, months, loanAmount);
    Stage graphStage = new Stage();

    CategoryAxis xAxis = new CategoryAxis();
    NumberAxis yAxis = new NumberAxis();
    StackedBarChart<String, Number> sbc =
      new StackedBarChart<String, Number>(xAxis, yAxis);
    XYChart.Series<String, Number> credits =
      new XYChart.Series<String, Number>();
    XYChart.Series<String, Number> interests =
      new XYChart.Series<String, Number>();

    String[] xAxisNameArray = new String[months];
    for(int i = 0; i < months; i++) {
      int j = i + 1;
      xAxisNameArray[i] = "Month " + j;
    }

    xAxis.setLabel("Month");
    xAxis.setCategories(FXCollections.<String>observableArrayList(
      Arrays.asList(xAxisNameArray)));
    yAxis.setLabel("Money");
    if(rbLinear.isSelected()) {
      credits.setName("Credits");
      for(int i = 0; i < months; i++) {
        credits.getData().add(new XYChart.Data<String, Number>(xAxisNameArray[i], loan.getCreditL()));
      }
      interests.setName("Interests");
      for(int i = 0; i < months; i++) {
        interests.getData().add(new XYChart.Data<String, Number>(xAxisNameArray[i], loan.getInterestL(i+1)));
      }
    }
    if(rbAnnuity.isSelected()) {
      credits.setName("Credits");
      for(int i = 0; i < months; i++) {
        credits.getData().add(new XYChart.Data<String, Number>(xAxisNameArray[i], loan.getCreditA()));
      }
      interests.setName("Interests");
      for(int i = 0; i < months; i++) {
        interests.getData().add(new XYChart.Data<String, Number>(xAxisNameArray[i], loan.getInterestA()));
      }
    }

    ScrollPane sp = new ScrollPane();
    sp.setHbarPolicy(ScrollBarPolicy.ALWAYS);
    sp.setVbarPolicy(ScrollBarPolicy.NEVER);
    sp.setContent(sbc);
    Scene graphScene = new Scene(sp, 1000, 600);
    sbc.getData().addAll(credits, interests);
    sbc.setCategoryGap(10);
    sbc.setPrefHeight(500);
    sbc.setPrefWidth(1500);
    sbc.setTitle("Credit vs interest");
    graphStage.setTitle("LoanCalculator by Agota GRAPH");
    graphStage.setScene(graphScene);
    graphStage.show();
  }

  public void saveToFile(String resultText) {
    try { 
      FileWriter fstream = new FileWriter("resultFile.txt");
      BufferedWriter out = new BufferedWriter(fstream);
      out.write(resultText);
      out.close();
      } catch (Exception e) {
        System.err.println("Error: " + e.getMessage());
        }
      GridPane filePane = new GridPane();
      filePane.add(new Text("Saved in file"), 0, 0);
      Scene fileScene = new Scene(filePane, 200, 100);
      Stage fileStage = new Stage();
      fileStage.setTitle("File Saved");
      fileStage.setScene(fileScene);
      fileStage.show();
    }
}