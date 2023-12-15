package com.cariochi.recordo.core.utils;

import java.util.List;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.RandomUtils.nextInt;

@UtilityClass
public class LoremIpsum {

    private static final List<String> LOREM_IPSUM = List.of(
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
            "Sed blandit sed magna ut posuere.",
            "Nulla suscipit auctor semper.",
            "Aliquam ex erat, posuere vitae augue sit amet, commodo tincidunt est.",
            "Praesent pharetra metus sed placerat lacinia.",
            "Suspendisse id elementum nulla.",
            "Phasellus sed pellentesque nisi, a malesuada nibh.",
            "Praesent pharetra, justo vel ultricies venenatis, tortor odio mollis velit, id semper diam dolor id est.",
            "Sed nisl enim, condimentum sit amet hendrerit non, ultricies nec lacus.",
            "Donec convallis, ligula et feugiat scelerisque, tortor elit mattis diam, et rhoncus mauris turpis ac dui.",
            "Sed tempus magna nec tempor dignissim.",
            "Ut sed dui lacinia, tincidunt erat venenatis, viverra purus.",
            "Quisque et commodo diam.",
            "Nullam vel ligula urna.",
            "Sed vel lectus vel enim sagittis lobortis.",
            "Mauris eget congue enim, a sollicitudin metus.",
            "Suspendisse pulvinar commodo ex a tincidunt.",
            "Nullam non ante mattis, commodo ligula ut, faucibus urna.",
            "Aliquam malesuada massa eget neque dictum aliquam.",
            "Aenean porta cursus turpis et pharetra.",
            "Proin auctor massa leo.",
            "Sed a justo sit amet mauris vestibulum ullamcorper quis vel nunc.",
            "Aliquam porttitor in massa eu volutpat."
    );

    private static final List<String> WORDS = LOREM_IPSUM.stream()
            .flatMap(sentence -> Stream.of(sentence.split("\\W")))
            .filter(StringUtils::isNotBlank)
            .collect(toList());


    public static String generateWords(int min, int max) {
        final int amount = nextInt(min, max + 1);
        final int startIndex = nextInt(0, WORDS.size() - amount);
        return WORDS.stream().skip(startIndex).limit(amount).collect(joining(" "));
    }

    public static String generateText() {
        final int startIndex = nextInt(0, LOREM_IPSUM.size());
        return LOREM_IPSUM.get(startIndex);
    }

}
