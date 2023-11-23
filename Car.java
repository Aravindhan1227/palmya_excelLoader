package com.palmyra.excel;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

class Manufacturer {
    private int id;
    private String name;

    public Manufacturer(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}

class CarMade {
    public int manufacturerId;
    public int year;
    public int numberOfCars;

    public CarMade(int manufacturerId, int year, int numberOfCars) {
        this.manufacturerId = manufacturerId;
        this.year = year;
        this.numberOfCars = numberOfCars;
    }

    public int getManufacturerId() {
        return manufacturerId;
    }

    public int getYear() {
        return year;
    }

    public int getNumberOfCars() {
        return numberOfCars;
    }
}

class ManufacturerProd {
    Manufacturer manufacturer;
    CarMade carMade;
}

public class Car {
    public static void main(String[] args) {
        List<Manufacturer> manufacturers = new ArrayList<>();
        manufacturers.add(new Manufacturer(1, "Toyota"));
        manufacturers.add(new Manufacturer(2, "Honda"));
        manufacturers.add(new Manufacturer(3, "Ford"));
        manufacturers.add(new Manufacturer(4, "Chevrolet"));
        manufacturers.add(new Manufacturer(5, "Tesla"));
        manufacturers.add(new Manufacturer(6, "Volkswagen"));
        manufacturers.add(new Manufacturer(7, "BMW"));
        manufacturers.add(new Manufacturer(8, "Mercedes benz"));
        manufacturers.add(new Manufacturer(9, "Audi"));
        manufacturers.add(new Manufacturer(10, "Porsche"));

        List<CarMade> carsMade = new ArrayList<>();
        carsMade.add(new CarMade(1, 2020, 100));
        carsMade.add(new CarMade(1, 2020, 200));
        carsMade.add(new CarMade(1, 2021, 300));
        carsMade.add(new CarMade(2, 2023, 400));
        carsMade.add(new CarMade(5, 2022, 150));
        carsMade.add(new CarMade(6, 2022, 200));
        carsMade.add(new CarMade(7, 2022, 250));
        carsMade.add(new CarMade(8, 2022, 300));
        carsMade.add(new CarMade(9, 2022, 350));
        carsMade.add(new CarMade(10, 2022, 400));
        carsMade.add(new CarMade(3, 2021, 120));
        carsMade.add(new CarMade(4, 2022, 220));
        carsMade.add(new CarMade(5, 2023, 330));
        carsMade.add(new CarMade(6, 2020, 440));
        carsMade.add(new CarMade(7, 2021, 550));
        carsMade.add(new CarMade(8, 2023, 660));
        carsMade.add(new CarMade(9, 2022, 770));
        carsMade.add(new CarMade(10, 2020, 880));
        carsMade.add(new CarMade(3, 2022, 990));
        carsMade.add(new CarMade(4, 2021, 1100));

        int targetYear = 2023;

        Predicate<CarMade> carInYear = car -> car.getYear() == targetYear;

        List<CarMade> carsInYear = carsMade.stream()
                .filter(carInYear)
                .collect(Collectors.toList());

        List<ManufacturerProd> allObjectsInYear = LeftOuterJoin.leftJoin( manufacturers,carsInYear ,
        		car -> car.getYear() == targetYear,
        	    (manufacturer, car) -> {
        	        ManufacturerProd manufacturerProd = new ManufacturerProd();
        	        manufacturerProd.manufacturer = manufacturer;
        	        manufacturerProd.carMade = car;
        	        return manufacturerProd;
        	    });


        System.out.println("Car-made records in " + targetYear + ":");
        allObjectsInYear.forEach(manufacturerProd -> {
            System.out.println("Manufacturer ID: " + manufacturerProd.manufacturer.getId() +
                    ", Name: " + manufacturerProd.manufacturer.getName() +
                    ", Cars Made: " + manufacturerProd.carMade.getNumberOfCars());
        });

        List<Manufacturer> manufacturersWithoutCarsInYear = manufacturers.stream()
                .filter(manufacturer -> allObjectsInYear.stream()
                        .noneMatch(manufacturerProd -> manufacturerProd.manufacturer.getId() == manufacturer.getId()))
                .collect(Collectors.toList());

        System.out.println("\nManufacturers without producing cars in " + targetYear + ":");
        manufacturersWithoutCarsInYear.forEach(manufacturer ->
                System.out.println("Manufacturer ID: " + manufacturer.getId() +
                        ", Name: " + manufacturer.getName()));
    }
}
