module com.mth.protractorfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires jfxtras.labs;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome;
    requires kotlin.stdlib;

    opens com.mth.protractorfx to javafx.fxml;
    exports com.mth.protractorfx;
}