package com.mycompany.networkedtournament; // اتأكد إن اسم الباكدج دي هو نفس الاسم اللي عندك فوق خالص

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        // هنعمل زرار بسيط
        Button btn = new Button("اضغط هنا لاختبار الكومبايلر!");
        
        // لما تدوس على الزرار، هيغير النص ويطبع رسالة
        btn.setOnAction(e -> {
            System.out.println("الكومبايلر شغال زي الفل!");
            btn.setText("تم الاختبار بنجاح!");
            btn.setStyle("-fx-background-color: lightgreen; -fx-font-size: 14px;");
        });
        
        // نحط الزرار في النص
        StackPane root = new StackPane();
        root.getChildren().add(btn);
        
        // نعمل المشهد (Scene) ونعرض الشاشة
        Scene scene = new Scene(root, 350, 200);
        
        stage.setTitle("اختبار NetBeans");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}