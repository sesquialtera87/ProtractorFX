module com.mth.protractorfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires jfxtras.labs;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome;
    requires kotlin.stdlib;
    requires java.logging;

    opens org.mth.protractorfx to javafx.fxml;
    exports org.mth.protractorfx;
}