package com.project.algo.domain.enums;

public enum Difficulty {

    // ───────── 백준 (Solved.ac 티어) ─────────
    BRONZE_V("Bronze V"),   BRONZE_IV("Bronze IV"),   BRONZE_III("Bronze III"),   BRONZE_II("Bronze II"),   BRONZE_I("Bronze I"),
    SILVER_V("Silver V"),   SILVER_IV("Silver IV"),   SILVER_III("Silver III"),   SILVER_II("Silver II"),   SILVER_I("Silver I"),
    GOLD_V("Gold V"),       GOLD_IV("Gold IV"),       GOLD_III("Gold III"),       GOLD_II("Gold II"),       GOLD_I("Gold I"),
    PLATINUM_V("Platinum V"), PLATINUM_IV("Platinum IV"), PLATINUM_III("Platinum III"), PLATINUM_II("Platinum II"), PLATINUM_I("Platinum I"),
    DIAMOND_V("Diamond V"), DIAMOND_IV("Diamond IV"), DIAMOND_III("Diamond III"), DIAMOND_II("Diamond II"), DIAMOND_I("Diamond I"),
    RUBY_V("Ruby V"),       RUBY_IV("Ruby IV"),       RUBY_III("Ruby III"),       RUBY_II("Ruby II"),       RUBY_I("Ruby I"),

    // ───────── 프로그래머스 ─────────
    LV0("Lv.0"), LV1("Lv.1"), LV2("Lv.2"), LV3("Lv.3"), LV4("Lv.4"), LV5("Lv.5"),

    // ───────── LeetCode ─────────
    EASY("Easy"), MEDIUM("Medium"), HARD("Hard"),

    // ───────── Codeforces (문제 레이팅) ─────────
    CF_800("800"),   CF_900("900"),   CF_1000("1000"), CF_1100("1100"), CF_1200("1200"),
    CF_1300("1300"), CF_1400("1400"), CF_1500("1500"), CF_1600("1600"), CF_1700("1700"),
    CF_1800("1800"), CF_1900("1900"), CF_2000("2000"), CF_2100("2100"), CF_2200("2200"),
    CF_2300("2300"), CF_2400("2400"), CF_2500("2500"), CF_2600("2600"), CF_2700("2700"),
    CF_2800("2800"), CF_2900("2900"), CF_3000("3000"), CF_3200("3200"), CF_3500("3500"),

    // ───────── SW Expert Academy ─────────
    D1("D1"), D2("D2"), D3("D3"), D4("D4"), D5("D5"), D6("D6");

    private final String displayName;

    Difficulty(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }
}
