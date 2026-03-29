package com.project.algo.domain.enums;

public enum ProgrammingLanguage {

    PYTHON("Python",         "python",     ".py"),
    JAVA("Java",             "java",       ".java"),
    CPP("C++",               "cpp",        ".cpp"),
    C("C",                   "c",          ".c"),
    JAVASCRIPT("JavaScript", "javascript", ".js"),
    TYPESCRIPT("TypeScript", "typescript", ".ts"),
    GO("Go",                 "go",         ".go"),
    KOTLIN("Kotlin",         "kotlin",     ".kt"),
    SWIFT("Swift",           "swift",      ".swift"),
    RUST("Rust",             "rust",       ".rs"),
    RUBY("Ruby",             "ruby",       ".rb");

    private final String displayName;
    private final String syntaxMode;
    private final String fileExtension;

    ProgrammingLanguage(String displayName, String syntaxMode, String fileExtension) {
        this.displayName  = displayName;
        this.syntaxMode   = syntaxMode;
        this.fileExtension = fileExtension;
    }

    public String getDisplayName()   { return displayName; }
    public String getSyntaxMode()    { return syntaxMode; }
    public String getFileExtension() { return fileExtension; }
}
