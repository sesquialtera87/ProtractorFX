package org.mth.protractorfx;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleIntegerProperty;

public class Test {
    public static void main(String[] args) {
        SimpleIntegerProperty size = new SimpleIntegerProperty(10);
        SimpleIntegerProperty index = new SimpleIntegerProperty(10);

        BooleanBinding binding = index.isNotEqualTo(size);

        System.out.println(binding.get());

        size.set(11);
        System.out.println(binding.get());
    }
}
