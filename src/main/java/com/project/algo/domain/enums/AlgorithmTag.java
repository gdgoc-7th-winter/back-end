package com.project.algo.domain.enums;

public enum AlgorithmTag {

    DP("다이나믹 프로그래밍"),
    BFS("너비 우선 탐색"),
    DFS("깊이 우선 탐색"),
    GRAPH("그래프"),
    GREEDY("그리디"),
    BINARY_SEARCH("이분 탐색"),
    TWO_POINTER("투 포인터"),
    SLIDING_WINDOW("슬라이딩 윈도우"),
    DIVIDE_AND_CONQUER("분할 정복"),
    BACKTRACKING("백트래킹"),
    SORTING("정렬"),
    MATH("수학"),
    STRING("문자열"),
    TREE("트리"),
    HEAP("힙"),
    STACK("스택"),
    QUEUE("큐"),
    DEQUE("덱"),
    HASHING("해싱"),
    SIMULATION("시뮬레이션"),
    IMPLEMENTATION("구현"),
    BIT_MANIPULATION("비트마스킹"),
    SEGMENT_TREE("세그먼트 트리"),
    FENWICK_TREE("펜윅 트리"),
    UNION_FIND("유니온 파인드"),
    SHORTEST_PATH("최단 경로"),
    MST("최소 신장 트리"),
    TOPOLOGICAL_SORT("위상 정렬"),
    NUMBER_THEORY("정수론"),
    GEOMETRY("기하학"),
    TRIE("트라이"),
    MEMOIZATION("메모이제이션");

    private final String displayName;

    AlgorithmTag(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }
}
