package com.nemo.mealzoom;

import com.nemo.mealzoom.entity.Category;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MealzoomApplicationTest {
    @Test
    public void stringFilter() {
        ArrayList<String> strings = new ArrayList<>(Arrays.asList("张三丰", "张无忌", "周芷若"));
        Stream<String> stream1 = strings.stream().filter(s -> s.startsWith("张"));
        stream1.forEach(System.out::println);
    }

    @Test
    public void streamMap() {
        Stream<String> stringStream = Stream.of("1", "2", "3", "4", "5", "6");
        stringStream.map(Integer::parseInt).forEach(System.out::println);
    }

    @Test
    public void streamMap2() {
        List<String> sentences = Arrays.asList("hello world","Jia Gou Wu Dao");
        List<String> lists = sentences.stream().flatMap(s -> Arrays.stream(s.split(" "))).collect(Collectors.toList());
        System.out.println(lists);
    }

    @Test
    public void streamError() {
        List<String> ids = Arrays.asList("205", "10", "308", "49", "627", "193", "111", "193");
        Stream<String> stringStream = ids.stream().filter(s -> s.length() == 2);
        long count = stringStream.count();
        System.out.println(count);
        System.out.println("--------下面会报错------------");
        try{
             stringStream.map(Integer::parseInt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("--------上面会报错------------");
    }
}
