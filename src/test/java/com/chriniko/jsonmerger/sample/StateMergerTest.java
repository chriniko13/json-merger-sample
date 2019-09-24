package com.chriniko.jsonmerger.sample;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class StateMergerTest {

    private StateMerger stateMerger;

    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setup() {
        stateMerger = new StateMerger(new TypeConverter());
    }

    @Test
    public void merge_put_case_one() {

        // given
        UUID existingId = UUID.randomUUID();

        Profile asIs = new Profile(existingId, 10, "name", new int[]{1, 2}, Arrays.asList("challenge1", "challenge2"),
                new Stats(100, 320, new int[]{14, 34}, Arrays.asList(1, 2), new LastUpdated("2019-06-18", new By("someone"))),
                Arrays.asList(new Picture("1"), new Picture("2")),
                new PowerUp[]{new PowerUp("strength", new Until("2019-06-24"))}
        );

        Profile toBe = new Profile(existingId, 12, "new name", new int[]{3, 4}, Arrays.asList("challenge1", "challenge2", "challenge3"),
                null,
                null,
                null
        );

        // when
        Profile result = stateMerger.merge(asIs, toBe, Sets.newHashSet());

        // then
        assertEquals(toBe, result);
    }

    @Test
    public void merge_put_case_two() {

        // given
        UUID existingId = UUID.randomUUID();

        Profile asIs = new Profile(existingId, 10, "name", new int[]{1, 2}, Arrays.asList("challenge1", "challenge2"),
                new Stats(100, 200, new int[]{14, 34}, Arrays.asList(1, 2), new LastUpdated("2019-06-15", new By("someone"))),
                Arrays.asList(new Picture("1"), new Picture("2")),
                new PowerUp[]{new PowerUp("strength", new Until("2019-06-24"))}
        );

        Profile toBe = new Profile(existingId, 12, "new name", new int[]{3, 4}, Arrays.asList("challenge1", "challenge2", "challenge3"),
                new Stats(500, 1400, new int[]{24, 56}, Arrays.asList(3, 5), new LastUpdated("2019-06-18", new By("someone-else"))),
                Arrays.asList(new Picture("1"), new Picture("2"), new Picture("3")),
                new PowerUp[]{new PowerUp("strength", new Until("2019-06-24")), new PowerUp("stamina", new Until("2019-06-24"))}
        );

        // when
        Profile result = stateMerger.merge(asIs, toBe, Sets.newHashSet());

        // then
        assertEquals(toBe, result);
    }

    @Test(expected = IllegalStateException.class) // then
    public void merge_put_case_recursion_max_depth_violation_case() {

        // given
        _1 asIs = new _1(new _2(new _3(new _4(new _5(new _6(new _7(new _8(new _9(new _10("something"))))))))));
        _1 toBe = new _1(new _2(new _3(new _4(new _5(new _6(new _7(new _8(new _9(new _10("something-else"))))))))));

        // when
        stateMerger.merge(asIs, toBe, Sets.newHashSet());
    }


    @Test
    public void merge_patch_case_one() {

        // given
        UUID existingId = UUID.randomUUID();

        Profile asIs = new Profile(existingId, 10, "name", new int[]{3, 4}, Arrays.asList("challenge1", "challenge2"),
                new Stats(300, 700, new int[]{14, 34}, Arrays.asList(1, 2), new LastUpdated("2019-06-18", new By("someone"))),
                Arrays.asList(new Picture("1"), new Picture("2")),
                new PowerUp[]{new PowerUp("strength", new Until("2019-06-24"))}
        );

        ObjectNode toBe = mapper.createObjectNode().put("name", "new name");

        // when
        Profile result = stateMerger.merge(asIs, toBe, Sets.newHashSet());

        // then
        asIs.setName("new name");
        assertEquals(asIs, result);
    }

    @Test
    public void merge_patch_case_two() {

        // given
        UUID existingId = UUID.randomUUID();

        Profile asIs = new Profile(existingId, 10, "name", new int[]{3, 4}, Arrays.asList("challenge1", "challenge2"),
                new Stats(300, 700, new int[]{14, 34}, Arrays.asList(1, 2), new LastUpdated("2019-06-18", new By("someone"))),
                Arrays.asList(new Picture("1"), new Picture("2")),
                new PowerUp[]{new PowerUp("strength", new Until("2019-06-24"))}
        );

        ObjectNode toBe = (ObjectNode) mapper.createObjectNode()
                .put("name", "new name")
                .put("age", 20)
                .set("achievements", mapper.createArrayNode().add("challenge3"));

        // when
        Profile result = stateMerger.merge(asIs, toBe, Sets.newHashSet());

        // then
        asIs.setName("new name");
        asIs.setAge(20);
        asIs.setAchievements(Collections.singletonList("challenge3"));
        assertEquals(asIs, result);
    }

    @Test
    public void merge_patch_case_three() {

        // given
        UUID existingId = UUID.randomUUID();

        Profile asIs = new Profile(existingId, 10, "name", new int[]{3, 4}, Arrays.asList("challenge1", "challenge2"),
                new Stats(300, 700, new int[]{14, 34}, Arrays.asList(1, 2), new LastUpdated("2019-06-18", new By("someone"))),
                Arrays.asList(new Picture("1"), new Picture("2")),
                new PowerUp[]{new PowerUp("strength", new Until("2019-06-24"))}
        );

        ObjectNode toBe = (ObjectNode) mapper.createObjectNode()
                .put("name", "new name")
                .put("age", 20)
                .set("stats",
                        mapper.createObjectNode().set("lastUpdated",
                                mapper.createObjectNode().set("by",
                                        mapper.createObjectNode().put("name", "admin"))));

        // when
        Profile result = stateMerger.merge(asIs, toBe, Sets.newHashSet());

        // then
        asIs.setName("new name");
        asIs.setAge(20);
        asIs.getStats().getLastUpdated().getBy().setName("admin");

        assertEquals(asIs, result);
    }

    @Test
    public void extractAllFields_works_as_expected_case() {

        // given
        JsonNode node = mapper.createObjectNode()
                .put("f1", "1")
                .set("f2",
                        mapper.createObjectNode()
                                .put("f3", "3")
                                .set("f4", mapper.createArrayNode().add(1).add(2).add(3))
                );

        // when
        Set<String> result = stateMerger.extractAllFields(node);

        // then
        assertEquals(4, result.size());
        assertEquals(Sets.newLinkedHashSet(Arrays.asList("f1", "f2", "f3", "f4")), result);

    }

    @Test(expected = IllegalStateException.class) // then
    public void extractAllFields_works_as_expected_recursion_max_depth_reached_case() {

        // given
        JsonNode root = mapper.createObjectNode();

        JsonNode walker = root;
        for (int i =1; i<=10; i++) {

            ObjectNode objectNode = mapper.createObjectNode().put("f" + (i + 1), i + 1);

            ((ObjectNode) walker).set("f" + i, objectNode);

            walker = objectNode;
        }

        // when
        stateMerger.extractAllFields(root);
    }


    // -------------------- util data structures --------------------
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class Profile {
        private UUID id;
        private int age;
        private String name;
        private int[] perks;
        private List<String> achievements;
        private Stats stats;
        private List<Picture> pictures;
        private PowerUp[] powerUps;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class Stats {
        private long hoursPlayed;
        private long combos;
        private int[] arenaWins;
        private List<Integer> deaths;
        private LastUpdated lastUpdated;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class LastUpdated {
        private String timestamp;
        private By by;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class By {
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class Picture {
        private String url;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class PowerUp {
        private String skill;
        private Until until;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class Until {
        private String timestamp;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class _1 {
        private _2 val;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class _2 {
        private _3 val;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class _3 {
        private _4 val;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class _4 {
        private _5 val;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class _5 {
        private _6 val;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class _6 {
        private _7 val;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class _7 {
        private _8 val;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class _8 {
        private _9 val;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class _9 {
        private _10 val;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class _10 {
        private String val;
    }
}