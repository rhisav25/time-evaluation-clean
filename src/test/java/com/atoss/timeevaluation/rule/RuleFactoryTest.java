package com.atoss.timeevaluation.rule;

import com.atoss.timeevaluation.TestRules;
import com.atoss.timeevaluation.config.EvaluationProperties.RuleDefinition;
import com.atoss.timeevaluation.config.RuleType;
import com.atoss.timeevaluation.config.Scope;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RuleFactoryTest {

    private static RuleDefinition attendance() {
        return TestRules.rule("attendance", RuleType.ATTENDANCE_SUM, Scope.DAY);
    }

    private static RuleDefinition dayRuleReading(String name, String input) {
        return new RuleDefinition(name, RuleType.EXCESS_OVER_EXPECTED, Scope.DAY,
                input, null, null, null, null, true);
    }

    private static void assertRejected(List<RuleDefinition> rules, String expectedMessagePart) {
        IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> RuleFactory.createAll(rules));
        assertTrue(exception.getMessage().contains(expectedMessagePart),
                "Unexpected message: " + exception.getMessage());
    }

    @Test
    void shouldAcceptTheDefaultRuleSet() {
        assertEquals(9, RuleFactory.createAll(TestRules.defaultRules()).size());
    }

    @Test
    void shouldRejectEmptyConfiguration() {
        assertRejected(List.of(), "No rules configured");
    }

    @Test
    void shouldRejectUnknownReference() {
        assertRejected(List.of(attendance(), dayRuleReading("overtime", "does-not-exist")),
                "defined above it");
    }

    @Test
    void shouldRejectReferenceToRuleDefinedLater() {
        assertRejected(List.of(dayRuleReading("overtime", "attendance"), attendance()),
                "defined above it");
    }

    @Test
    void shouldRejectDuplicateNames() {
        assertRejected(List.of(attendance(), attendance()), "more than once");
    }

    @Test
    void shouldRejectWrongScopeForType() {
        assertRejected(
                List.of(TestRules.rule("attendance", RuleType.ATTENDANCE_SUM, Scope.WEEK)),
                "must have scope DAY");
    }

    @Test
    void shouldRejectMissingThreshold() {
        List<RuleDefinition> rules = new ArrayList<>(List.of(attendance()));
        rules.add(new RuleDefinition("weekly", RuleType.EXCESS_OVER_THRESHOLD, Scope.DAY,
                "attendance", null, null, null, null, true));

        assertRejected(rules, "thresholdMinutes");
    }

    @Test
    void shouldRejectWeekRuleThatReadsADayRuleDirectly() {
        List<RuleDefinition> rules = new ArrayList<>(List.of(attendance()));
        rules.add(new RuleDefinition("weekly", RuleType.EXCESS_OVER_THRESHOLD, Scope.WEEK,
                "attendance", null, 2400L, null, null, true));

        assertRejected(rules, "must be a WEEK rule");
    }
}
